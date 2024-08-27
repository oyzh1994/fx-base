package cn.oyzh.fx.common.util;

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
public class FastFileWriter {

    // private Queue<String> queue;
    //
    // private AtomicBoolean running;

    private final FileWriter writer;

    public FastFileWriter(File file) throws IOException {
        this(file, StandardCharsets.UTF_8);
    }

    public FastFileWriter(File file, Charset charset) throws IOException {
        this.writer = new FileWriter(file, charset);
    }

    public void appendLine(String line) throws IOException {
        this.appendLine(line, false);
    }

    public void appendLine(String line, boolean async) throws IOException {
        if (line != null) {
            if (!line.endsWith("\n")) {
                line += "\n";
            }
            // if (async) {
            //     this.appendAsync(line);
            // } else {
            this.writer.append(line);
            // }
        }
    }

    public void appendLines(Collection<String> lines) throws IOException {
        this.appendLines(lines, false);
    }

    public void appendLines(Collection<String> lines, boolean async) throws IOException {
        if (lines != null) {
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                if (line.endsWith("\n")) {
                    sb.append(line);
                } else {
                    sb.append(line).append("\n");
                }
            }
            // if (async) {
            //     this.appendAsync(sb.toString());
            // } else {
            this.writer.append(sb.toString());
            // }
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

    // public void waitingComplete() {
    //     if (this.queue != null && !this.queue.isEmpty()) {
    //         while (!this.queue.isEmpty()) {
    //             ThreadUtil.sleep(10);
    //         }
    //     }
    // }

    public void close() {
        try {
            if (this.writer != null) {
                this.writer.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // private void appendAsync(String line) {
    //     if (this.queue == null) {
    //         this.queue = new LinkedBlockingDeque<>();
    //         this.running = new AtomicBoolean(false);
    //     }
    //     this.queue.add(line);
    //     if (!this.running.get()) {
    //         this.running.set(true);
    //         ThreadUtil.start(() -> {
    //             do {
    //                 String text = this.queue.poll();
    //                 try {
    //                     this.appendLine(text);
    //                 } catch (Exception ex) {
    //                     ex.printStackTrace();
    //                 }
    //             } while (!this.queue.isEmpty());
    //             this.running.set(false);
    //         });
    //     }
    // }
}
