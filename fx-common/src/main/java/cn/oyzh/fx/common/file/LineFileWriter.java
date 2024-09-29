package cn.oyzh.fx.common.file;

import cn.oyzh.fx.common.util.FileUtil;
import lombok.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author oyzh
 * @since 2024-09-04
 */
public class LineFileWriter implements AutoCloseable {

    protected BufferedWriter writer;

    public LineFileWriter(@NonNull String filePath) throws FileNotFoundException {
        this(new File(filePath), StandardCharsets.UTF_8);
    }

    public LineFileWriter(@NonNull File file) throws FileNotFoundException {
        this(file, StandardCharsets.UTF_8);
    }

    public LineFileWriter(@NonNull File file, Charset charset) throws FileNotFoundException {
        this.writer = FileUtil.getWriter(file, charset, false);
    }

    public void write(String str) throws IOException {
        if (str != null) {
            this.writer.write(str);
        }
    }

    public void writeLine(String line) throws IOException {
        if (line != null) {
            if (line.endsWith(System.lineSeparator())) {
                this.writer.write(line);
            } else {
                this.writer.write(line + System.lineSeparator());
            }
        }
    }

    @Override
    public void close() {
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static LineFileWriter create(@NonNull String filePath, String charset) throws FileNotFoundException {
        return new LineFileWriter(new File(filePath), Charset.forName(charset));
    }

    public static LineFileWriter create(@NonNull String filePath, Charset charset) throws FileNotFoundException {
        return new LineFileWriter(new File(filePath), charset);
    }

    public static LineFileWriter create(@NonNull File file, String charset) throws FileNotFoundException {
        return new LineFileWriter(file, Charset.forName(charset));
    }

    public static LineFileWriter create(@NonNull File file, Charset charset) throws FileNotFoundException {
        return new LineFileWriter(file, charset);
    }
}
