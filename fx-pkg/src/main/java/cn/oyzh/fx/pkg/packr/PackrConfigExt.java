package cn.oyzh.fx.pkg.packr;

import cn.oyzh.fx.pkg.packager.PackageConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * packr打包配置扩展
 *
 * @author oyzh
 * @since 2023/11/17
 */
public class PackrConfigExt extends PackrConfig {

    /**
     * 从打包配置生成配置对象
     * @param packageConfig 打包
     * @return
     */
    public static PackrConfigExt form(PackageConfig packageConfig) {
        PackrConfigExt config = new PackrConfigExt();
        config.verbose = true;
        config.vmArgs = new ArrayList<>();
        config.useZgcIfSupportedOs = true;
        config.jdk = packageConfig.getJrePath();
        config.mainClass = packageConfig.getMainClass();
        config.iconResource = packageConfig.getIconFile();
        config.executable = packageConfig.getExecutable();
        config.outDir = new File(packageConfig.getAppDest());
        config.classpath = List.of(packageConfig.getJarPath());
        return config;
    }
}
