package cn.oyzh.fx.pkg.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.compress.CompressUtil;
import cn.hutool.extra.compress.archiver.Archiver;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.pkg.jdeps.JDepsConfig;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.jpackage.JPackageConfig;
import lombok.experimental.UtilityClass;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author oyzh
 * @since 2023/11/17
 */
@UtilityClass
public class PkgUtil {

    /**
     * 复制jar到jpackage输出目录
     *
     * @param destPath 目标路径
     * @param platform 平台
     * @param jarFile  jar文件
     * @return 新jar文件
     */
    public static File copyJarToJpackageInputDir(String destPath, String platform, File jarFile) {
        File inputDir = getJpackageInputDir(destPath, platform);
        // jpackage主jar
        return FileUtil.copy(jarFile, inputDir, true);
    }

    /**
     * 获取jpackage输出目录
     *
     * @param destPath 目标路径
     * @param platform 平台
     * @return jpackage输出目录
     */
    public static File getJpackageInputDir(String destPath, String platform) {
        String inputPath = FileNameUtil.concat(destPath, platform + "_jpackage_input_dir");
        File inputDir;
        if (FileUtil.exist(inputPath)) {
            FileUtil.clean(inputPath);
            inputDir = new File(inputPath);
        } else {
            inputDir = FileUtil.mkdir(inputPath);
        }
        return inputDir;
    }

    /**
     * 压缩打包目录，zip格式
     *
     * @param name    文件名称
     * @param appDest app目录
     * @return 压缩后的文件
     */
    public static File zipDest(String name, String appDest) {
        String compressName = name + ".zip";
        JulLog.info("zipDest start, config.compressType is:{} compressName:{}.", "zip", compressName);
        File dest = new File(appDest);
        File compressFile = new File(dest.getParentFile(), compressName);
        // 进行zip压缩，如果是macos则保留目录名称，否则不保留
        ZipUtil.zip(dest.getPath(), compressFile.getPath(), false);
        JulLog.info("zipDest finish appDest:{}", compressFile.getPath());
        return compressFile;
    }

    /**
     * 压缩打包目录，zip格式，macos专用
     *
     * @param name    文件名称
     * @param appDest app目录
     * @return 压缩后的文件
     */
    public static File zipDestByMacos(String name, String appDest) {
        String compressName = name + ".zip";
        JulLog.info("zipDestByMacos start, config.compressType is:{} compressName:{}.", "zip", compressName);
        File dest = new File(appDest);
        File compressFile = new File(dest.getParentFile(), compressName);
        // 进行zip压缩，如果是macos则保留目录名称，否则不保留
        ZipUtil.zip(dest.getPath(), compressFile.getPath(), true);
        JulLog.info("zipDestByMacos finish appDest:{}", compressFile.getPath());
        return compressFile;
    }

    /**
     * 压缩打包目录，tar格式
     *
     * @param name    文件名称
     * @param appDest app目录
     * @return 压缩后的文件
     */
    public static File tarDest(String name, String appDest) {
        String compressName = name + ".tar";
        JulLog.info("tarDest start, config.compressType is:{} compressName:{}.", "tar", compressName);
        File dest = new File(appDest);
        File compressFile = new File(dest.getParentFile(), compressName);
        // 进行tar压缩
        Archiver archiver = CompressUtil.createArchiver(StandardCharsets.UTF_8, ArchiveStreamFactory.TAR, compressFile)
                .add(dest);
        archiver.finish().close();
        JulLog.info("tarDest finish appDest:{}", compressFile.getPath());
        return compressFile;
    }

    /**
     * 压缩打包文件，tar.gz格式
     *
     * @return 压缩后的文件
     */
    public static File gzipDest(String name, String appDest) {
        String compressName = name + ".tar.gz";
        JulLog.info("gzipDest start, config.compressType is:{} compressName:{}.", "tar.gz", compressName);
        File dest = new File(appDest);
        File compressFile = new File(dest.getParentFile(), compressName);
        // // 进行tar.gz压缩
        // Archiver archiver = CompressUtil.createArchiver(StandardCharsets.UTF_8, "tar.gz", compressFile);
        // // 把目录的一级文件或者目录添加进去，以免生成解压后还存在一个子目录
        // File[] files = dest.listFiles();
        // if (files != null) {
        //     for (File file : files) {
        //         archiver.add(file);
        //     }
        // }
        // archiver.finish().close();
        // 进行tar.gz压缩
        Archiver archiver = CompressUtil.createArchiver(StandardCharsets.UTF_8, "tar.gz", compressFile)
                .add(dest);
        archiver.finish().close();
        JulLog.info("gzipDest finish appDest:{}", compressFile.getPath());
        return compressFile;
    }

    // /**
    //  * 获取打包器
    //  *
    //  * @param platform 平台
    //  * @return 打包器
    //  */
    // public static BasePackager getPackager(String platform) {
    //     return switch (platform) {
    //         case "win_amd64" -> new WinPackager();
    //         case "macos_amd64" -> new MacPackager();
    //         case "linux_amd64" -> new LinuxPackager();
    //         default -> throw new IllegalStateException("Unexpected value: " + platform);
    //     };
    // }

    /**
     * 获取jlink命令
     *
     * @param config jlink配置
     * @return jlink命令
     */
    public static String getJLinkCMD(JLinkConfig config) {
        String cmdStr = "jlink";
        if (config.isVerbose()) {
            cmdStr += " --verbose";
        }
        if (config.getVm() != null) {
            cmdStr += " --vm=" + config.getVm();
        }
        if (config.getCompress() != null) {
            cmdStr += " --compress=" + config.getCompress();
        }
        if (config.isNoHeaderFiles()) {
            cmdStr += " --no-header-files";
        }
        if (config.isNoManPages()) {
            cmdStr += " --no-man-pages";
        }
        if (config.isStripDebug()) {
            cmdStr += " --strip-debug";
        }
        if (config.isStripJavaDebugAttributes()) {
            cmdStr += " --strip-java-debug-attributes";
        }
        if (CollectionUtil.isNotEmpty(config.getAddModules())) {
            cmdStr += " --add-modules " + CollectionUtil.join(config.getAddModules(), ",");
        }
        if (CollectionUtil.isNotEmpty(config.getExcludeFiles())) {
            cmdStr += " --exclude-files=" + CollectionUtil.join(config.getExcludeFiles(), ",");
        }
        cmdStr += " --output " + config.getOutput();
        return cmdStr;
    }

    /**
     * 获取jdeps命令
     *
     * @param config jdeps配置
     * @return jdeps命令
     */
    public static String getJDepsCMD(JDepsConfig config) {
        String cmdStr = "jdeps";
        if (config.isVerbose()) {
            cmdStr += " -verbose";
        }
        if (config.isSummary()) {
            cmdStr += " -summary";
        }
        if (config.getMultiRelease() != null) {
            cmdStr += " --multi-release " + config.getMultiRelease();
        }
        return cmdStr;
    }

    /**
     * 获取jpackage命令
     *
     * @param config jpackage配置
     * @return jlink命令
     */
    public static String getJPackageCMD(JPackageConfig config) {
        String cmdStr = "jpackage";
        if (config.isVerbose()) {
            cmdStr += " --verbose";
        }
        if (config.isWinMenu()) {
            cmdStr += " --win-menu";
        }
        if (config.isWinShortcut()) {
            cmdStr += " --win-shortcut";
        }
        if (config.isWinDirChooser()) {
            cmdStr += " --win-dir-chooser";
        }
        if (config.getVendor() != null) {
            cmdStr += " --vendor " + config.getVendor();
        }
        if (config.getDescription() != null) {
            cmdStr += " --description " + config.getDescription();
        }
        if (config.getIcon() != null) {
            cmdStr += " --icon " + config.getIcon();
        }
        if (config.getInput() != null) {
            cmdStr += " -i " + config.getInput();
        }
        if (config.getMainJar() != null) {
            cmdStr += " --main-jar " + config.getMainJar();
        }
        if (config.getName() != null) {
            cmdStr += " -n " + config.getName();
        }
        if (config.getType() != null) {
            cmdStr += " -t " + config.getType();
        }
        if (config.getAppVersion() != null) {
            cmdStr += " --app-version " + config.getAppVersion();
        }
        if (config.getRuntimeImage() != null) {
            cmdStr += " --runtime-image " + config.getRuntimeImage();
        }
        cmdStr += " -d " + config.getDest();
        return cmdStr;
    }

    /**
     * 获取jdk执行命令
     *
     * @param jdkPath jdk目录
     * @param cmd     命令
     * @return 执行命令
     */
    public static String getJDKExecCMD(String jdkPath, String cmd) {
        // 执行jpackage
        if (jdkPath == null) {
            return cmd;
        }
//        if (OSUtil.isWindows()) {
        return FileNameUtil.concat(jdkPath, "bin", cmd);
//        }
//        return "sh " + FileNameUtil.concat(jdkPath, "bin", cmd);
    }
}
