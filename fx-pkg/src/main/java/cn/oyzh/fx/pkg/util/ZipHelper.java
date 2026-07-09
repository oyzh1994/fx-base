package cn.oyzh.fx.pkg.util;

import cn.oyzh.common.file.FileUtil;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 *
 * @author oyzh
 * @since 2026-07-09
 */
public class ZipHelper {


    public static void zipWithUnixMode(File appDir, File targetZip) throws IOException {
        Path appPath = appDir.toPath();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(targetZip)) {
            Stream<Path> stream = Files.walk(appPath);
            stream.forEach(path -> {
                // relativize 会保留以 YourApp.app/ 开头的相对路径
                String entryName = appPath.relativize(path).toString();
                // 跳过根目录本身（如果 appDir 是 .app 目录，这里会为空）
                if (entryName.isEmpty()) {
                    return;
                }
                ZipArchiveEntry entry = new ZipArchiveEntry(path.toFile(), entryName);
                entry.setUnixMode(FileUtil.getUnixMode(path));
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
}
