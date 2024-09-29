package cn.oyzh.fx.common.file;

import cn.oyzh.fx.common.util.FileUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

    protected BufferedReader reader;

    @Getter
    @Accessors(fluent = true, chain = true)
    private int currentLine = 0;

    @Getter
    @Setter
    @Accessors(fluent = true, chain = true)
    private String lineBreak;

    public SkipAbleFileReader(@NonNull String filePath) throws FileNotFoundException {
        this(new File(filePath), StandardCharsets.UTF_8);
    }

    public SkipAbleFileReader(@NonNull File file) throws FileNotFoundException {
        this(file, StandardCharsets.UTF_8);
    }

    public SkipAbleFileReader(@NonNull File file, Charset charset) throws FileNotFoundException {
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

    private String _readLine() throws IOException {
        if (this.lineBreak == null) {
            return this.reader.readLine();
        }
        StringBuilder builder = new StringBuilder();
        char lineChar1 = this.lineBreak.charAt(0);
        if (this.lineBreak.length() == 1) {
            while (true) {
                int i = this.reader.read();
                if (i == -1) {
                    break;
                }
                char c = (char) i;
                if (c == lineChar1) {
                    break;
                }
                builder.append(c);
            }
        } else {
            Character prevChar = null;
            char lineChar2 = this.lineBreak.charAt(1);
            while (true) {
                int i = this.reader.read();
                if (i == -1) {
                    break;
                }
                char c = (char) i;
                if (prevChar != null && prevChar == lineChar1 && c == lineChar2) {
                    break;
                }
                builder.append(c);
                prevChar = c;
            }
        }
        return builder.toString();
    }

    public String readLine() throws IOException {
        String line = this._readLine();
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

    public int read() throws IOException {
        return this.reader.read();
    }
}
