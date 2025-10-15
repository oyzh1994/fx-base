package cn.oyzh.fx.pkg.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.compress.CompressUtil;
import cn.hutool.extra.compress.archiver.Archiver;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.fx.pkg.jdeps.JDepsConfig;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.jpackage.JPackageConfig;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/11/17
 */

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
    @Deprecated
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
        // cmdStr += " --bind-services";
        // cmdStr += " --exclude-modules jdk.localedata";
        return cmdStr;
    }

    /**
     * 获取jlink命令
     *
     * @param config jlink配置
     * @return jlink命令
     */
    public static String[] getJLinkCMD1(JLinkConfig config) {
        List<String> cmdList = new ArrayList<>();
        cmdList.add("jlink");
        if (config.isVerbose()) {
            cmdList.add("--verbose");
        }
        if (config.getVm() != null) {
            cmdList.add("--vm=" + config.getVm());
        }

        // 参数已过时
        // if (config.getCompress() != null) {
        //     cmdList.add("--compress=" + config.getCompress());
        // }
        if (config.isNoHeaderFiles()) {
            cmdList.add("--no-header-files");
        }
        if (config.isNoManPages()) {
            cmdList.add("--no-man-pages");
        }
        if (config.isStripDebug()) {
            cmdList.add("--strip-debug");
        }
        if (config.isStripJavaDebugAttributes()) {
            cmdList.add("--strip-java-debug-attributes");
        }
        if (CollectionUtil.isNotEmpty(config.getAddModules())) {
            cmdList.add("--add-modules");
            cmdList.add(CollectionUtil.join(config.getAddModules(), ","));
        }
        if (CollectionUtil.isNotEmpty(config.getExcludeFiles())) {
            cmdList.add("--exclude-files=" + CollectionUtil.join(config.getExcludeFiles(), ","));
        }
        cmdList.add("--output=" + config.getOutput());
        return ArrayUtil.toArray(cmdList, String.class);
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

    // /**
    //  * 获取jdeps命令
    //  *
    //  * @param config jdeps配置
    //  * @return jdeps命令
    //  */
    // public static String[] getJDepsCMD1(JDepsConfig config) {
    //     List<String> cmdList = new ArrayList<>();
    //     cmdList.add("jdeps");
    //     if (config.isVerbose()) {
    //         cmdList.add("-verbose");
    //     }
    //     if (config.isSummary()) {
    //         cmdList.add("-summary");
    //     }
    //     if (config.getMultiRelease() != null) {
    //         cmdList.add("--multi-release");
    //         cmdList.add(config.getMultiRelease() + "");
    //     }
    //     return ArrayUtil.toArray(cmdList, String.class);
    // }

    /**
     * 获取jpackage命令
     *
     * @param config jpackage配置
     * @return jlink命令
     */
    @Deprecated
    public static String getJPackageCMD(JPackageConfig config) {
        String cmdStr = "jpackage";
        if (config.isVerbose()) {
            cmdStr += " --verbose";
        }
        if (config.getVendor() != null) {
            cmdStr += " --vendor " + config.getVendor();
        }
        if (CollectionUtil.isNotEmpty(config.getJavaOptions())) {
            for (String javaOption : config.getJavaOptions()) {
                String[] options = javaOption.split(" ");
                for (String option : options) {
                    cmdStr += " --java-options " + option;
                }
            }
        }
        if (config.getDescription() != null) {
            cmdStr += " --description \"" + config.getDescription() + "\"";
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
        if (OSUtil.isWindows()) {
            if (config.isWinMenu()) {
                cmdStr += " --win-menu";
            }
            if (config.isWinShortcut()) {
                cmdStr += " --win-shortcut";
            }
            if (config.isWinDirChooser()) {
                cmdStr += " --win-dir-chooser";
            }
        }
        if (OSUtil.isMacOS()) {
            if (config.getMacPackageIdentifier() != null) {
                cmdStr += " --mac-package-identifier " + config.getMacPackageIdentifier();
            }
        }
        cmdStr += " -d " + config.getDest();
        return cmdStr;
    }

    /**
     * 获取jpackage命令
     *
     * @param config jpackage配置
     * @return jlink命令
     */
    public static String[] getJPackageCMD1(JPackageConfig config) {
        List<String> cmdList = new ArrayList<>();
        cmdList.add("jpackage");
        if (config.isVerbose()) {
            cmdList.add("--verbose");
        }
        if (config.getVendor() != null) {
            cmdList.add("--vendor");
            cmdList.add(config.getVendor());
        }
        if (CollectionUtil.isNotEmpty(config.getJavaOptions())) {
            for (String javaOption : config.getJavaOptions()) {
                String[] options = javaOption.split(" ");
                for (String option : options) {
                    cmdList.add("--java-options");
                    cmdList.add(option);
                }
            }
        }
        if (config.getDescription() != null) {
            cmdList.add("--description");
            cmdList.add(config.getDescription());
        }
        if (config.getIcon() != null) {
            cmdList.add("--icon");
            cmdList.add(config.getIcon());
        }
        if (config.getInput() != null) {
            cmdList.add("--input");
            cmdList.add(config.getInput());
        }
        if (config.getMainJar() != null) {
            cmdList.add("--main-jar");
            cmdList.add(config.getMainJar());
        }
        if (config.getName() != null) {
            cmdList.add("--name");
            cmdList.add(config.getName());
        }
        if (config.getType() != null) {
            cmdList.add("--type");
            cmdList.add(config.getType());
        }
        if (config.getAppVersion() != null) {
            cmdList.add("--app-version");
            cmdList.add(config.getAppVersion());
        }
        if (config.getRuntimeImage() != null) {
            cmdList.add("--runtime-image");
            cmdList.add(config.getRuntimeImage());
        }
        if (OSUtil.isWindows()) {
            if (config.isWinMenu()) {
                cmdList.add("--win-menu");
            }
            if (config.isWinShortcut()) {
                cmdList.add("--win-shortcut");
            }
            if (config.isWinDirChooser()) {
                cmdList.add("--win-dir-chooser");
            }
        }
        if (OSUtil.isMacOS()) {
            if (config.getMacPackageIdentifier() != null) {
                cmdList.add("--mac-package-identifier");
                cmdList.add(config.getMacPackageIdentifier());
            }
        }
        cmdList.add("--dest");
        cmdList.add(config.getDest());
        return ArrayUtil.toArray(cmdList, String.class);
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
        return FileNameUtil.concat(jdkPath, "bin", cmd);
    }

    /**
     * 获取jdk执行命令
     *
     * @param jdkPath jdk目录
     * @param cmd     命令
     * @return 执行命令
     */
    public static String[] getJDKExecCMD(String jdkPath, String[] cmd) {
        if (jdkPath != null) {
            cmd[0] = FileNameUtil.concat(jdkPath, "bin", cmd[0]);
        }
        return cmd;
    }
}
