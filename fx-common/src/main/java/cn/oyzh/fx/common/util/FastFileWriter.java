package cn.oyzh.fx.common.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * @author oyzh
 * @since 2024/08/23
 */
public class FastFileWriter implements Closeable {

    private final FileWriter writer;

    public FastFileWriter(String filePath) throws IOException {
        this(new File(filePath), StandardCharsets.UTF_8);
    }

    public FastFileWriter(File file) throws IOException {
        this(file, StandardCharsets.UTF_8);
    }

    public FastFileWriter(File file, Charset charset) throws IOException {
        this.writer = new FileWriter(file, charset);
    }

    public void appendLine(String line) throws IOException {
        if (line != null) {
            if (!line.endsWith("\n")) {
                line += "\n";
            }
            this.writer.append(line);
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

    public void writeLines(Collection<String> lines) throws IOException {
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

    @Override
    public void close() {
        try {
            if (this.writer != null) {
                this.writer.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}
