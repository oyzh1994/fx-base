package cn.oyzh.fx.common.util;

import cn.hutool.core.io.FileUtil;
import lombok.Getter;
import lombok.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-02
 */
public class SkipAbleFileReader implements AutoCloseable {

    private BufferedReader reader;

    @Getter
    private int currentLine = 0;

    public SkipAbleFileReader(@NonNull String filePath) {
        this(new File(filePath), StandardCharsets.UTF_8);
    }

    public SkipAbleFileReader(@NonNull File file) {
        this(file, StandardCharsets.UTF_8);
    }

    public SkipAbleFileReader(@NonNull File file, Charset charset) {
        this.reader = FileUtil.getReader(file, charset);
    }

    public boolean ready() throws IOException {
        return this.reader.ready();
    }

    public void skipLine() throws IOException {
        this.skipLine(1);
    }

    public void skipLine(long lineCount) throws IOException {
        if (lineCount > 0) {
            long count = lineCount;
            while (--count > 0) {
                this.readLine();
            }
        }
    }

    public void jumpLine(long toLine) throws IOException {
        if (this.currentLine != toLine) {
            this.skipLine(toLine - this.currentLine);
        }
    }

    public String readLine() throws IOException {
        String line = this.reader.readLine();
        this.currentLine++;
        return line;
    }

    public List<String> readLines(int count) throws IOException {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String line = this.readLine();
            if (line != null) {
                lines.add(line);
            } else {
                break;
            }
        }
        return lines;
    }

    @Override
    public void close() {
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
