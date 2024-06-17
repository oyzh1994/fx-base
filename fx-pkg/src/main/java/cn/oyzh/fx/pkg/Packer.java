package cn.oyzh.fx.pkg;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.pkg.comporess.CompressHandler;
import cn.oyzh.fx.pkg.comporess.CompressNameHandler;
import cn.oyzh.fx.pkg.config.ConfHandler;
import cn.oyzh.fx.pkg.config.ExtPackrConfig;
import cn.oyzh.fx.pkg.config.ExtPackrConfigParser;
import cn.oyzh.fx.pkg.jar.JarHandler;
import cn.oyzh.fx.pkg.jlink.JLinkHandler;
import cn.oyzh.fx.pkg.jre.JreHandler;
import cn.oyzh.fx.pkg.pack.EndHandler;
import cn.oyzh.fx.pkg.pack.PackHandler;
import cn.oyzh.fx.pkg.pack.StartHandler;

import java.util.ArrayList;
import java.util.List;

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
    private final ExtPackrConfigParser extPackrConfigParser = new ExtPackrConfigParser();

    {
        this.registerEndHandler();
        this.registerPackHandler();
        this.registerConfHandler();
        this.registerStartHandler();
        this.registerCompressHandler();
        this.registerCompressNameHandler();
    }

    public void registerEndHandler() {
        this.registerHandler(new EndHandler());
    }

    public void registerStartHandler() {
        this.registerHandler(new StartHandler());
    }

    public void registerPackHandler() {
        this.registerHandler(new PackHandler());
    }

    public void registerConfHandler() {
        this.registerHandler(new ConfHandler());
    }

    public void registerJreHandler(String configFile) {
        this.registerHandler(new JreHandler(configFile));
    }

    public void registerJarHandler(String configFile) {
        this.registerHandler(new JarHandler(configFile));
    }

    public void registerJLinkHandler(String configFile) {
        this.registerHandler(new JLinkHandler(configFile));
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
            if (this.handlers.parallelStream().anyMatch(h -> h.unique() && (h == handler || StrUtil.equals(h.name(), handler.name())))) {
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
        // 解析配置
        ExtPackrConfig packrConfig = this.extPackrConfigParser.parse(configFile);
        for (PreHandler preHandler : this.preHandlers()) {
            this.doHandle(preHandler, packrConfig);
        }
        for (PackHandler packHandler : this.packHandlers()) {
            this.doHandle(packHandler, packrConfig);
        }
        for (PostHandler postHandler : this.postHandlers()) {
            this.doHandle(postHandler, packrConfig);
        }
    }

    /**
     * 执行业务
     *
     * @param handler     处理器
     * @param packrConfig 打包配置
     * @throws Exception 异常
     */
    private void doHandle(Handler handler, ExtPackrConfig packrConfig) throws Exception {
        long start = System.currentTimeMillis();
        StaticLog.info("开始执行任务-{}", handler.name());
        handler.handle(packrConfig);
        long end = System.currentTimeMillis();
        StaticLog.info("任务执行结束-{}, 耗时:{}毫秒", handler.name(), (end - start));
    }
}
