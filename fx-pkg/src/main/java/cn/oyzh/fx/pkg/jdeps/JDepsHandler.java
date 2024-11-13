package cn.oyzh.fx.pkg.jdeps;

import cn.hutool.core.io.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.RuntimeUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.pkg.PackOrder;
import cn.oyzh.fx.pkg.PreHandler;
import cn.oyzh.fx.pkg.config.PackConfig;
import cn.oyzh.fx.pkg.filter.RegFilter;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.util.JarUtil;
import cn.oyzh.fx.pkg.util.PkgUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * jdeps处理器
 *
 * @author oyzh
 * @since 2024/06/20
 */
public class JDepsHandler implements PreHandler {

    @Getter
    @Setter
    @Accessors(chain = false, fluent = true)
    private int order = PackOrder.ORDER_P6;

    @Override
    public String name() {
        return "jdeps处理器";
    }

    @Override
    public void handle(PackConfig packConfig) throws Exception {
        JLinkConfig jLinkConfig = packConfig.getJLinkConfig();
        if (jLinkConfig == null) {
            return;
        }
        JDepsConfig jDepsConfig = packConfig.getJDepsConfig();
        RegFilter fileFilter = new RegFilter();
        RegFilter moduleFilter = new RegFilter();
        if (jDepsConfig != null) {
            fileFilter.addExcludes(jDepsConfig.getSkips());
            moduleFilter.addExcludes(jDepsConfig.getExcludes());
        } else {
            jDepsConfig = new JDepsConfig();
        }

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
        StringBuilder cmdStr = new StringBuilder("java --list-modules");
        String result = RuntimeUtil.execForStr(cmdStr.toString());
        System.out.println(result);
        result.lines().forEach(r -> {
            if (StringUtil.isNotBlank(r)) {
                modules.add(r.split("@")[0]);
            }
        });
        // 遍历所有文件，然后找出所有依赖模块
        Set<String> deps = new HashSet<>();
        List<File> files = FileUtil.loopFiles(jarUnDir);
        cmdStr = new StringBuilder(PkgUtil.getJDepsCMD(jDepsConfig));
        for (File file : files) {
            try {
                // 非jar，跳过
                if (!JarUtil.isJar(file)) {
                    continue;
                }
                JulLog.info("jdeps jar: {}.", file.getName());
                // 被过滤
                if (!fileFilter.apply(file.getName())) {
                    continue;
                }
                // 拼接到命令
                cmdStr.append(" ").append(file.getPath());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // 列举模块
        cmdStr = new StringBuilder(PkgUtil.getJDKExecCMD(jdkPath, cmdStr.toString()));
        result = RuntimeUtil.execForStr(cmdStr.toString());
        System.out.println(result);
        result.lines().forEach(r -> {
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
                System.out.println("module added:" + module);
                deps.add(module);
            }
        });
        // 合并依赖模块
        jLinkConfig.margeAddModules(deps);
    }
}
