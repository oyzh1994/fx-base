package cn.oyzh.fx.pkg;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.appImage.AppImageHandler;
import cn.oyzh.fx.pkg.comporess.CompressHandler;
import cn.oyzh.fx.pkg.comporess.CompressNameHandler;
import cn.oyzh.fx.pkg.config.AppConfigHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.config.PackConfigHandler;
import cn.oyzh.fx.pkg.config.PackConfigParser;
import cn.oyzh.fx.pkg.config.ProjectHandler;
import cn.oyzh.fx.pkg.github.GitHubHandler;
import cn.oyzh.fx.pkg.jar.JarHandler;
import cn.oyzh.fx.pkg.jdeps.JDepsHandler;
import cn.oyzh.fx.pkg.jlink.JLinkHandler;
import cn.oyzh.fx.pkg.jpackage.JPackageHandler;
import cn.oyzh.fx.pkg.jre.JreHandler;
import cn.oyzh.fx.pkg.mvn.MvnHandler;
import cn.oyzh.fx.pkg.pack.EndHandler;
import cn.oyzh.fx.pkg.pack.StartHandler;
import cn.oyzh.fx.pkg.packr.PackrHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2024/6/14
 */
public class Packer {

    /**
     * 处理器
     */
    private final List<Handler> handlers = new ArrayList<>();

    /**
     * 配置解析器
     */
    private final PackConfigParser configParser = new PackConfigParser();

    {
        this.registerEndHandler();
        this.registerJarHandler();
        this.registerJreHandler();
        this.registerJLinkHandler();
        this.registerStartHandler();
        this.registerJdepsHandler();
        this.registerCompressHandler();
        this.registerAppConfigHandler();
        this.registerPackConfigHandler();
        this.registerCompressNameHandler();
    }

    public void registerEndHandler() {
        this.registerHandler(new EndHandler());
    }

    public void registerStartHandler() {
        this.registerHandler(new StartHandler());
    }

    public void registerPackrHandler() {
        this.registerHandler(new PackrHandler());
    }

    public void registerJPackageHandler() {
        this.registerHandler(new JPackageHandler());
    }

    public void registerAppConfigHandler() {
        this.registerHandler(new AppConfigHandler());
    }

    public void registerJreHandler() {
        this.registerHandler(new JreHandler());
    }

    public void registerJarHandler() {
        this.registerHandler(new JarHandler());
    }

    public void registerMvnHandler(String projectDir, List<String> dependencies) {
        this.registerHandler(new MvnHandler(projectDir, dependencies));
    }

    public void registerProjectHandler() {
        this.registerHandler(new ProjectHandler());
    }

    public void registerProjectHandler(String file) {
        this.registerHandler(new ProjectHandler(file));
    }

    public void registerJdepsHandler() {
        this.registerHandler(new JDepsHandler());
    }

    public void registerGitHubHandler() {
        this.registerHandler(new GitHubHandler());
    }

    public void registerAppImageHandler() {
        this.registerHandler(new AppImageHandler());
    }

    public void registerJLinkHandler() {
        this.registerHandler(new JLinkHandler());
    }

    public void registerPackConfigHandler() {
        this.registerHandler(new PackConfigHandler());
    }

    public void registerCompressHandler() {
        this.registerHandler(new CompressHandler());
    }

    public void registerCompressNameHandler() {
        this.registerHandler(new CompressNameHandler());
    }

    /**
     * 注册处理器
     *
     * @param handler 处理器
     */
    public void registerHandler(Handler handler) {
        if (handler != null) {
            if (this.handlers.parallelStream().anyMatch(h -> h.unique() && (h == handler || StringUtil.equals(h.name(), handler.name())))) {
                throw new RuntimeException("处理器:" + handler.name() + "已存在");
            }
            this.handlers.add(handler);
            this.handlers.sort((o1, o2) -> Integer.compare(o2.order(), o1.order()));
        }
    }

    /**
     * 获取前置处理器
     *
     * @return 前置处理器列表
     */
    public List<PreHandler> preHandlers() {
        List<PreHandler> list = new ArrayList<>();
        for (Handler handler : this.handlers) {
            if (handler instanceof PreHandler preHandler) {
                list.add(preHandler);
            }
        }
        return list;
    }

    /**
     * 获取后置处理器
     *
     * @return 后置处理器列表
     */
    public List<PostHandler> postHandlers() {
        List<PostHandler> list = new ArrayList<>();
        for (Handler handler : this.handlers) {
            if (handler instanceof PostHandler postHandler) {
                list.add(postHandler);
            }
        }
        return list;
    }

    /**
     * 获取打包处理器
     *
     * @return 后置处理器列表
     */
    public List<PackHandler> packHandlers() {
        List<PackHandler> list = new ArrayList<>();
        for (Handler handler : this.handlers) {
            if (handler instanceof PackHandler packHandler) {
                list.add(packHandler);
            }
        }
        return list;
    }

    /**
     * 执行打包
     *
     * @param configFile 打包配置
     * @throws Exception 异常
     */
    public void pack(String configFile) throws Exception {
        this.pack(configFile, null);
    }

    /**
     * 执行打包
     *
     * @param configFile 打包配置
     * @param properties 属性
     * @throws Exception 异常
     */
    public void pack(String configFile, Map<String, Object> properties) throws Exception {
        // 解析配置
        PackConfig packConfig = this.configParser.parse(configFile);
        if (CollectionUtil.isNotEmpty(properties)) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                packConfig.putProperty(entry.getKey(), entry.getValue());
            }
        }
        if (packConfig.isParkByPackr()) {
            this.registerPackrHandler();
        } else {
            this.registerJPackageHandler();
        }
        for (PreHandler preHandler : this.preHandlers()) {
            this.doHandle(preHandler, packConfig);
        }
        for (PackHandler packHandler : this.packHandlers()) {
            this.doHandle(packHandler, packConfig);
        }
        for (PostHandler postHandler : this.postHandlers()) {
            this.doHandle(postHandler, packConfig);
        }
    }

    /**
     * 执行业务
     *
     * @param handler    处理器
     * @param packConfig 打包配置
     * @throws Exception 异常
     */
    private void doHandle(Handler handler, PackConfig packConfig) throws Exception {
        long start = System.currentTimeMillis();
        JulLog.info("开始执行任务-{}", handler.name());
        handler.handle(packConfig);
        long end = System.currentTimeMillis();
        JulLog.info("任务执行结束-{}, 耗时:{}毫秒", handler.name(), (end - start));
    }
}
