package cn.oyzh.fx.pkg.clip.clipper;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.StaticLog;
import cn.oyzh.fx.pkg.clip.util.JarUtil;
import lombok.Getter;
import lombok.NonNull;

import java.util.Set;

/**
 * jar裁剪工具
 *
 * @author oyzh
 * @since 2022/12/7
 */
//@Slf4j
public class JarClipper extends BaseJarClipper {

    /**
     * 类库裁剪器
     */
    @Getter
    private final LibJarClipper libJarClipper = new LibJarClipper();

    /**
     * 主jar裁剪器
     */
    @Getter
    private final MainJarClipper mainJarClipper = new MainJarClipper();

    @Override
    public void addExcludeJars(Set<String> excludeJars) {
        this.libJarClipper.addExcludeJars(excludeJars);
        this.mainJarClipper.addExcludeJars(excludeJars);
    }

    @Override
    public void addExcludeClasses(Set<String> excludeClasses) {
        this.libJarClipper.addExcludeClasses(excludeClasses);
        this.mainJarClipper.addExcludeClasses(excludeClasses);
    }

    @Override
    public void addExcludeFiles(Set<String> excludeFiles) {
        this.libJarClipper.addExcludeFiles(excludeFiles);
        this.mainJarClipper.addExcludeFiles(excludeFiles);
    }

    /**
     * 裁剪
     *
     * @param config  配置
     * @param jdkPath jdk路径
     */
    public void clip(@NonNull JarClipConfig config, String jdkPath) throws Exception {
        this.addExcludeJars(config.getExcludeJars());
        this.addExcludeFiles(config.getExcludeFiles());
        this.addExcludeClasses(config.getExcludeClasses());
        String src = config.getSrc();
        String dest = config.getDest();
        StaticLog.info("clip start.");
        long start = System.currentTimeMillis();
        this.clip(src, dest);
        // jar解压目录
        String jarUnDir = config.getSrc().replace(".jar", "");
        // jar目标文件
        if (dest == null) {
            dest = src.replace(".jar", "_clip.jar");
        }
        // 删除空类库jar
        if (config.isDelEmptyJar()) {
            this.libJarClipper.delEmptyLibs(jarUnDir);
        }
        // 合并类库jar
        this.mainJarClipper.mergeLibs(jarUnDir, dest, jdkPath);
        // 删除解压目录
        // if (!config.isRetainTemp()) {
        FileUtil.del(jarUnDir);
        // }
        long end = System.currentTimeMillis();
        StaticLog.info("clip end, used time: {}ms.", end - start);
    }

    @Override
    public void clip(@NonNull String src, String dest) throws Exception {
        this.checkPath(src, dest);
        // 删除旧文件
        FileUtil.del(dest);
        // jar解压目录
        String jarUnDir = src.replace(".jar", "");
        // jar目标文件
        if (dest == null) {
            dest = src.replace(".jar", "_clip.jar");
        }
        // 删除解压目录
        FileUtil.del(jarUnDir);
        // 解压主jar
        JarUtil.unJar(src, jarUnDir);
        // 裁剪主jar
        this.mainJarClipper.clip(src, dest);
        // 裁剪类库jar
        this.libJarClipper.clipLibs(jarUnDir);
        // 覆盖类库jar
        this.libJarClipper.coverLibs(jarUnDir);
    }
}
