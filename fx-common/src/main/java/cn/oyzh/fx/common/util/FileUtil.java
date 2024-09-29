package cn.oyzh.fx.common.util;

import lombok.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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

    public static boolean del(String file) {
        if (file != null) {
            File file1 = new File(file);
            if (file1.exists()) {
                return file1.delete();
            }
        }
        return false;
    }

    public static byte[] readBytes(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        byte[] bytes;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (fis; bos) {
                byte[] buffer = new byte[1024];
                while (fis.read(buffer) != -1) {
                    bos.write(buffer, 0, buffer.length);
                }
                bytes = bos.toByteArray();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return bytes;
    }


    public static boolean exist(File file) {
        return file != null && file.exists();
    }

    public static List<String> readLines(URL url, Charset charset) {
        try {
            InputStreamReader reader = new InputStreamReader(url.openStream(), charset);
            BufferedReader bufferedReader = new BufferedReader(reader);
            try (reader; bufferedReader) {
                List<String> list = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    list.add(line);
                }
                return list;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
