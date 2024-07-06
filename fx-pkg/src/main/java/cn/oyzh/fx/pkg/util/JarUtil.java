package cn.oyzh.fx.pkg.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.StaticLog;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Function;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * jar工具类
 *
 * @author oyzh
 * @since 2022/12/7
 */
@UtilityClass
//@Slf4j
public class JarUtil {

    /**
     * 是否有class
     *
     * @param jarFile jar文件
     * @return 结果
     */
    public static boolean hasClass(@NonNull String jarFile) throws IOException {
        if (!FileUtil.exist(jarFile)) {
            throw new RuntimeException("jarFile " + jarFile + " is not exist.");
        }
        if (!FileUtil.isFile(jarFile)) {
            throw new RuntimeException("jarFile " + jarFile + " is not file.");
        }
        try (
                FileInputStream fis = new FileInputStream(jarFile);
                JarInputStream jarIn = new JarInputStream(fis)
        ) {
            while (true) {
                ZipEntry entry = jarIn.getNextJarEntry();
                if (entry == null) {
                    break;
                }
                if (isClass(entry.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 解压jar
     *
     * @param src     源文件
     * @param destDir 目标目录
     */
    public static File unJar(@NonNull String src, @NonNull String destDir) {
        if (!FileUtil.exist(src)) {
            throw new RuntimeException("src " + src + " is not exist.");
        }
        if (!FileUtil.isFile(src)) {
            throw new RuntimeException("src " + src + " is not file.");
        }
        if (FileUtil.exist(destDir) && !FileUtil.isDirectory(destDir)) {
            throw new RuntimeException("destDir " + destDir + " is not dir.");
        }
        return ZipUtil.unzip(src, destDir);
    }

    /**
     * 是否jar
     *
     * @param file 文件
     */
    public static boolean isJar(File file) {
        if (file == null || !file.isFile() || !file.exists()) {
            return false;
        }
        return isJar(file.getName());
    }

    /**
     * 是否jar
     *
     * @param name 名称
     */
    public static boolean isJar(String name) {
        if (StrUtil.isBlank(name)) {
            return false;
        }
        return name.toLowerCase().endsWith(FileUtil.JAR_FILE_EXT);
    }

    /**
     * 是否class
     *
     * @param file 文件
     */
    public static boolean isClass(File file) {
        if (file == null || !file.isFile() || !file.exists()) {
            return false;
        }
        return isClass(file.getName());
    }

    /**
     * 是否class
     *
     * @param name 名称
     */
    public static boolean isClass(String name) {
        if (StrUtil.isBlank(name)) {
            return false;
        }
        return name.toLowerCase().endsWith(".class");
    }

    /**
     * 最小化处理
     *
     * @param src      来源
     * @param dest     目标
     * @param function 过滤函数
     * @throws IOException 异常
     */
    public static void minimize(String src, String dest, Function<String, Boolean> function) throws IOException {
        StaticLog.info("minimize jar start, src:{}", src);
        File destFile = new File(dest);
        File tempFile = FileUtil.createTempFile(destFile.getName(), true);
        JarInputStream jarIn = new JarInputStream(new BufferedInputStream(new FileInputStream(src)));
        Manifest manifest = jarIn.getManifest();
        JarOutputStream jarOut;
        if (manifest == null) {
            jarOut = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
        } else {
            jarOut = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)), manifest);
        }
        try {
            byte[] bytes = new byte[1024];
            while (true) {
                // 重点
                ZipEntry entry = jarIn.getNextJarEntry();
                if (entry == null) {
                    break;
                }
                String name = entry.getName();
                // 执行过滤
                if (function.apply(name)) {
                    // 添加到新jar文件
                    jarOut.putNextEntry(entry);
                    int len = jarIn.read(bytes, 0, bytes.length);
                    while (len != -1) {
                        jarOut.write(bytes, 0, len);
                        len = jarIn.read(bytes, 0, bytes.length);
                    }
                }
            }
        } finally {
            IoUtil.close(jarIn);
            jarOut.finish();
            IoUtil.close(jarOut);
        }
        StaticLog.info("minimize jar finish dest:{}", dest);
        FileUtil.move(tempFile, destFile, true);
    }
}
