package cn.oyzh.fx.pkg.jdeps;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.filter.RegFilter;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.util.JarUtil;
import cn.oyzh.fx.pkg.util.PkgUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * jdeps处理器
 *
 * @author oyzh
 * @since 2024/06/20
 */
public class JDepsHandler implements PreHandler {

    private int order = PackOrder.ORDER_P6;

    @Override
    public int order() {
        return order;
    }

    @Override
    public void order(int order) {
        this.order = order;
    }

    @Override
    public String name() {
        return "jdeps处理器";
    }

    @Override
    public boolean unique() {
        return true;
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        JLinkConfig jLinkConfig = packConfig.getJLinkConfig();
        if (jLinkConfig == null) {
            return;
        }
        JDepsConfig jDepsConfig = packConfig.getJDepsConfig();
        if (jDepsConfig == null) {
            return;
        }
        if (!jDepsConfig.isEnable()) {
            JulLog.warn("jdeps未启用，已跳过");
            return;
        }
        RegFilter fileFilter = new RegFilter();
        RegFilter moduleFilter = new RegFilter();
        fileFilter.addExcludes(jDepsConfig.getSkips());
        moduleFilter.addExcludes(jDepsConfig.getExcludes());

        String jdkPath = packConfig.getJdkPath();
        if (StringUtil.isBlank(jdkPath)) {
            throw new Exception("jdkPath为空！");
        }
        String jarUnDir = packConfig.getJarUnDir();
        if (StringUtil.isBlank(jarUnDir)) {
            throw new Exception("jarUnDir为空！");
        }
        // 列举系统模块
        Set<String> modules = new HashSet<>();
        StringBuilder cmdStr;
        if (OSUtil.isLinux()) {
            cmdStr = new StringBuilder(jdkPath + "/bin/java --list-modules");
        } else {
            cmdStr = new StringBuilder("java --list-modules");
        }
        String result = RuntimeUtil.execForStr(cmdStr.toString());
        JulLog.info("list modules:{}", result);
        if (result != null) {
            result.lines().forEach(r -> {
                if (StringUtil.isNotBlank(r)) {
                    modules.add(r.split("@")[0]);
                }
            });
        }
        // 文件列表
        List<File> files = FileUtil.loopFiles(jarUnDir);
        // 文件路径
        List<String> filePaths = new ArrayList<>();
        for (File file : files) {
            // 非jar，跳过
            if (!JarUtil.isJar(file)) {
                continue;
            }
            // 判断是否被过滤
            if (!fileFilter.apply(file.getName())) {
                continue;
            }
            // 追加到路径
            filePaths.add(file.getPath());
        }
        // 遍历所有文件，然后找出所有依赖模块
        Set<String> deps = new CopyOnWriteArraySet<>();
        // 任务列表
        List<Runnable> tasks = new ArrayList<>();
        // 添加任务
        for (String filePath : filePaths) {
            Runnable func = () -> {
                JulLog.info("jdeps jar: {}.", filePath);
                String[] cmd = PkgUtil.getJDepsCMD(jDepsConfig, filePath);
                // 列举模块
                cmd = PkgUtil.getJDKExecCMD(jdkPath, cmd);
                String commands = StringUtil.join(" ", cmd);
                String execResult = RuntimeUtil.execForStr(commands);
                JulLog.info("jdeps result:{}", execResult);
                if (execResult != null) {
                    execResult.lines().forEach(r -> {
                        // 处理内容
                        if (!r.contains("-> ") || r.startsWith(" ") || r.endsWith(".jar")) {
                            return;
                        }
                        String module = r.split("-> ")[1];
                        // 处理内容
                        module = module.trim();
                        // 被过滤
                        if (!moduleFilter.apply(module)) {
                            return;
                        }
                        // 如果是系统模块，则添加到模块列表
                        if (modules.contains(module) && !deps.contains(module)) {
                            JulLog.info("module added:{}", module);
                            deps.add(module);
                        }
                    });
                }
            };
            tasks.add(func);
        }
        // 异步执行
        ThreadUtil.submit(tasks);
        // 合并依赖模块
        jLinkConfig.margeAddModules(deps);
    }
}
