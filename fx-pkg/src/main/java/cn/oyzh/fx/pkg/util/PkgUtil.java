package cn.oyzh.fx.pkg.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.compress.CompressUtil;
import cn.hutool.extra.compress.archiver.Archiver;
import cn.oyzh.fx.pkg.jlink.JLinkConfig;
import cn.oyzh.fx.pkg.packager.BasePackager;
import cn.oyzh.fx.pkg.packager.LinuxPackager;
import cn.oyzh.fx.pkg.packager.MacPackager;
import cn.oyzh.fx.pkg.packager.WinPackager;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author oyzh
 * @since 2023/11/17
 */
@Slf4j
@UtilityClass
public class PkgUtil {

    public static File copyJarToJpackageInputDir(String destPath, String platform, File jarFile) {
        File inputDir = getJpackageInputDir(destPath, platform);
        // jpackage主jar
        return FileUtil.copy(jarFile, inputDir, true);
    }

    public static File getJpackageInputDir(String destPath, String platform) {
        String inputPath = destPath + platform + "_jpackage_input_dir";
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
     */
    public static void zipDest(String name, String appDest) {
        String compressName = name + ".zip";
        log.info("zipDest start, config.compressType is:{} compressName:{}.", "zip", compressName);
        File dest = new File(appDest);
        File compressFile = new File(dest.getParentFile(), compressName);
        // 进行zip压缩，如果是macos则保留目录名称，否则不保留
        ZipUtil.zip(dest.getPath(), compressFile.getPath(), false);
        log.info("zipDest finish.");
    }

    /**
     * 压缩打包目录，zip格式，mac专用
     */
    public static void zipDestByMacos(String name, String appDest) {
        String compressName = name + ".zip";
        log.info("zipDestByMacos start, config.compressType is:{} compressName:{}.", "zip", compressName);
        File dest = new File(appDest);
        File compressFile = new File(dest.getParentFile(), compressName);
        // 进行zip压缩，如果是macos则保留目录名称，否则不保留
        ZipUtil.zip(dest.getPath(), compressFile.getPath(), true);
        log.info("zipDestByMacos finish.");
    }

    /**
     * 压缩打包目录，tar格式
     */
    public static void tarDest(String name, String appDest) {
        String compressName = name + ".tar";
        log.info("tarDest start, config.compressType is:{} compressName:{}.", "tar", compressName);
        File dest = new File(appDest);
        File compressFile = new File(dest.getParentFile(), compressName);
        // 进行tar压缩
        Archiver archiver = CompressUtil.createArchiver(StandardCharsets.UTF_8, ArchiveStreamFactory.TAR, compressFile)
                .add(dest);
        archiver.finish().close();
        log.info("tarDest finish.");
    }

    /**
     * 压缩打包文件，tar.gz格式
     */
    public static void gzipDest(String name, String appDest) {
        String compressName = name + ".tar.gz";
        log.info("gzipDest start, config.compressType is:{} compressName:{}.", "tar.gz", compressName);
        File dest = new File(appDest);
        File compressFile = new File(dest.getParentFile(), compressName);
        // 进行tar.gz压缩
        Archiver archiver = CompressUtil.createArchiver(StandardCharsets.UTF_8, "tar.gz", compressFile);
        // 把目录的一级文件或者目录添加进去，以免生成解压后还存在一个子目录
        File[] files = dest.listFiles();
        if (files != null) {
            for (File file : files) {
                archiver.add(file);
            }
        }
        archiver.finish().close();
        log.info("gzipDest finish.");
    }

    /**
     * 获取打包器
     *
     * @param platform 平台
     * @return 打包器
     */
    public static BasePackager getPackager(String platform) {
        return switch (platform) {
            case "win_amd64" -> new WinPackager();
            case "macos_amd64" -> new MacPackager();
            case "linux_amd64" -> new LinuxPackager();
            default -> throw new IllegalStateException("Unexpected value: " + platform);
        };
    }

    /**
     * 获取jlink命令
     *
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
        if (CollUtil.isNotEmpty(config.getAddModules())) {
            cmdStr += " --add-modules " + CollUtil.join(config.getAddModules(), ",");
        }
        if (CollUtil.isNotEmpty(config.getExcludeFiles())) {
            cmdStr += " --exclude-files=" + CollUtil.join(config.getExcludeFiles(), ",");
        }
        cmdStr += " --output " + config.getOutput();
        return cmdStr;
    }
}
