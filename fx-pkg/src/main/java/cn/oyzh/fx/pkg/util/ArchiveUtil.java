package cn.oyzh.fx.pkg.util;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.system.OSUtil;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * 跨平台文件归档工具（保留 Unix 权限）
 * 支持 ZIP、TAR、TAR.GZ 三种格式。
 * 依赖：org.apache.commons:commons-compress:1.26.1+
 */
public class ArchiveUtil {

    // ==================== ZIP ====================


    /**
     * 将目录打包为 ZIP（保留 Unix 权限）。
     * 解压后会在当前目录生成同名文件夹（例如打包 YourApp.app，解压得 YourApp.app）。
     *
     * @param sourceDir 待打包的源目录
     * @param targetZip 目标 ZIP 文件路径
     */
    public static void createZip(File sourceDir, File targetZip) throws IOException {
        createZip(sourceDir.toPath(), targetZip.toPath());
    }

    /**
     * 将目录打包为 ZIP（保留 Unix 权限）。
     * 解压后会在当前目录生成同名文件夹（例如打包 YourApp.app，解压得 YourApp.app）。
     *
     * @param sourceDir 待打包的源目录
     * @param targetZip 目标 ZIP 文件路径
     */
    public static void createZip(Path sourceDir, Path targetZip) throws IOException {
        try (OutputStream os = Files.newOutputStream(targetZip);
             ZipArchiveOutputStream zos = new ZipArchiveOutputStream(os)) {
            Stream<Path> stream = Files.walk(sourceDir);
            stream.forEach(path -> {
                // 计算相对路径，根目录本身不打包（空条目跳过）
                String entryName = sourceDir.relativize(path).toString();
                // 跳过根目录本身（如果 appDir 是 .app 目录，这里会为空）
                if (entryName.isEmpty()) {
                    return;
                }

                // 目录条目加斜杠
                if (Files.isDirectory(path)) {
                    entryName += "/";
                }

                ZipArchiveEntry entry = new ZipArchiveEntry(path.toFile(), entryName);
                if (OSUtil.isLinux() || OSUtil.isMacOS()) {
                    entry.setUnixMode(FileUtil.getUnixMode(path));
                }
                try {
                    zos.putArchiveEntry(entry);
                    if (Files.isRegularFile(path)) {
                        Files.copy(path, zos);
                    }
                    zos.closeArchiveEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            stream.close();
        }
    }

    // ==================== TAR ====================

    /**
     * 将目录打包为 TAR（不压缩，保留 Unix 权限）。
     *
     * @param sourceDir 源目录
     * @param targetTar 目标文件
     */
    public static void createTar(File sourceDir, File targetTar) throws IOException {
        createTar(sourceDir.toPath(), targetTar.toPath());
    }

    /**
     * 将目录打包为 TAR（不压缩，保留 Unix 权限）。
     *
     * @param sourceDir 源目录
     * @param targetTar 目标文件
     */
    public static void createTar(Path sourceDir, Path targetTar) throws IOException {
        try (OutputStream os = Files.newOutputStream(targetTar);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(os)) {
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            addFilesToTar(sourceDir, sourceDir, tos);
        }
    }

    // ==================== TAR.GZ ====================

    /**
     * 将目录打包为 TAR.GZ（gzip 压缩，保留 Unix 权限）。
     *
     * @param sourceDir 源目录
     * @param targetTar 目标文件
     */
    public static void createTarGz(File sourceDir, File targetTar) throws IOException {
        createTarGz(sourceDir.toPath(), targetTar.toPath());
    }

    /**
     * 将目录打包为 TAR.GZ（gzip 压缩，保留 Unix 权限）。
     *
     * @param sourceDir   源目录
     * @param targetTarGz 目标文件
     */
    public static void createTarGz(Path sourceDir, Path targetTarGz) throws IOException {
        try (OutputStream os = Files.newOutputStream(targetTarGz);
             OutputStream gzos = new GzipCompressorOutputStream(os);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos)) {
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            addFilesToTar(sourceDir, sourceDir, tos);
        }
    }

    // ==================== 私有递归方法（TAR 系列复用） ====================

    private static void addFilesToTar(Path rootDir, Path currentDir, TarArchiveOutputStream tos) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)) {
            for (Path path : stream) {
                String entryName = rootDir.relativize(path).toString();
                // 目录以斜杠结尾
                if (Files.isDirectory(path)) {
                    entryName += "/";
                }

                TarArchiveEntry entry = new TarArchiveEntry(path.toFile(), entryName);
                if (OSUtil.isLinux() || OSUtil.isMacOS()) {
                    entry.setMode(FileUtil.getUnixMode(path));
                }

                tos.putArchiveEntry(entry);
                if (Files.isRegularFile(path)) {
                    Files.copy(path, tos);
                }
                tos.closeArchiveEntry();

                // 递归处理子目录
                if (Files.isDirectory(path)) {
                    addFilesToTar(rootDir, path, tos);
                }
            }
        }
    }

    // ==================== 简单示例 ====================

    public static void main(String[] args) throws IOException {
        Path source = Paths.get("/path/to/YourApp.app");  // 替换为你的实际路径
        Path zipFile = Paths.get("/tmp/archive.zip");
        Path tarFile = Paths.get("/tmp/archive.tar");
        Path tarGzFile = Paths.get("/tmp/archive.tar.gz");

        createZip(source, zipFile);
        createTar(source, tarFile);
        createTarGz(source, tarGzFile);

        System.out.println("所有归档文件已生成，权限完整保留。");
    }
}