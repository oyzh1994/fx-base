package cn.oyzh.fx.common.util;

import lombok.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class FileUtil {

    public static File touch(String filePath) {
        if (StringUtil.isBlank(filePath)) {
            return null;
        }
        return touch(new File(filePath));
    }

    public static File touch(File file) {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            return file;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static BufferedWriter getWriter(File file, Charset charset, boolean append) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), charset));
    }

    public static BufferedReader getReader(@NonNull File file, Charset charset) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
    }

    public static File writeString(String content, File file, Charset charset) {
        try {
            if (content != null && file != null) {
                if (!file.exists()) {
                    touch(file);
                }
                try (FileWriter fileWriter = new FileWriter(file, charset)) {
                    fileWriter.write(content);
                }
                return file;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean isDirectory(File dir) {
        return dir != null && dir.isDirectory();
    }
}
