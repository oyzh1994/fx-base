package cn.oyzh.fx.pkg.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.compress.CompressUtil;
import cn.hutool.extra.compress.archiver.Archiver;
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
    public static void zipDest(String name, String appDest, boolean withSrcDir) {
        String compressName = name + ".zip";
        log.info("zipDest start, config.compressType is:{} compressName:{}.", "zip", compressName);
        File dest = new File(appDest);
        File compressFile = new File(dest.getParentFile(), compressName);
        // 进行zip压缩，如果是macos则保留目录名称，否则不保留
        ZipUtil.zip(dest.getPath(), compressFile.getPath(), withSrcDir);
        log.info("zipDest finish.");
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
        Archiver archiver = CompressUtil.createArchiver(StandardCharsets.UTF_8, "tar.gz", compressFile)
                .add(dest);
        archiver.finish().close();
        log.info("gzipDest finish.");
    }
}
