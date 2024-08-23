package cn.oyzh.fx.common.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/23
 */
public class FastFileWriter {

    private FileWriter writer;

    public FastFileWriter(File file) throws IOException {
        this(file, StandardCharsets.UTF_8);
    }

    public FastFileWriter(File file, Charset charset) throws IOException {
        this.writer = new FileWriter(file, charset);
    }

    public void appendLine(String line) throws IOException {
        if (line != null) {
            if (line.endsWith("\n")) {
                this.writer.append(line);
            } else {
                this.writer.append(line + "\n");
            }
        }
    }

    public void appendLines(Collection<String> lines) throws IOException {
        if (lines != null) {
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                if (line.endsWith("\n")) {
                    sb.append(line);
                } else {
                    sb.append(line).append("\n");
                }
            }
            this.writer.append(sb.toString());
        }
    }

    public void writeLine(String line) throws IOException {
        if (line != null) {
            if (line.endsWith("\n")) {
                this.writer.write(line);
            } else {
                this.writer.write(line + "\n");
            }
            this.appendLine(line);
        }
    }

    public void writeLines(List<String> lines) throws IOException {
        if (lines != null) {
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                if (line.endsWith("\n")) {
                    sb.append(line);
                } else {
                    sb.append(line).append("\n");
                }
            }
            this.writer.write(sb.toString());
        }
    }

    public void close() throws IOException {
        if (this.writer != null) {
            this.writer.close();
        }
    }
}
