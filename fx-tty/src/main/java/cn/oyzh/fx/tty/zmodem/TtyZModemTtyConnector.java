package cn.oyzh.fx.tty.zmodem;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.tty.TtyCharsetble;
import cn.oyzh.fx.tty.TtyStreamable;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TtyConnector;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * ZModem协议tty连接器，读取数据时检测 ZModem 帧并交由处理器执行文件传输。
 *
 * @author oyzh
 * @since 2026-07-06
 */
public class TtyZModemTtyConnector implements TtyConnector {

    /**
     * 终端容器
     */
    private Terminal terminal;

    /**
     * tty连接器
     */
    private TtyStreamable connector;

    /**
     * ZModem处理器
     */
    private volatile TtyZModemProcessor processor;

    /**
     * ZModem检测输入流
     */
    private TtyZModemDetectingInputStream zmodemInput;

    /**
     * 原始输入流
     */
    private InputStream input;

    /**
     * 原始输出流
     */
    private OutputStream output;

    /**
     * 终端字符读取器
     */
    private Reader reader;

    /**
     * 获取被包装的真实连接器。
     *
     * @return 真实连接器
     */
    public TtyStreamable getConnector() {
        return connector;
    }

    /**
     * 构造 ZModem 连接器。
     *
     * @param terminal  终端对象
     * @param connector 被包装的连接器
     */
    public TtyZModemTtyConnector(Terminal terminal, TtyStreamable connector) {
        this.terminal = terminal;
        this.connector = connector;
    }

    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
        this.ensureReader();
        int lengthRead = this.reader.read(buffer, offset, length);
        if (lengthRead > 0) {
            this.connector.onRead(buffer, offset, lengthRead);
        }
        return lengthRead;
    }

    /**
     * 初始化基于原始输入流的终端读取器。
     *
     * @throws IOException IO异常
     */
    private void ensureReader() throws IOException {
        if (this.reader != null) {
            return;
        }
        this.input = this.connector.input();
        if (this.input == null) {
            throw new IOException("ZModem tty input stream is unavailable");
        }
        this.output = this.connector.output();
        this.zmodemInput = new TtyZModemDetectingInputStream(this.input, this::processZModem);
        Charset charset = this.connector instanceof TtyCharsetble charsetble
                ? charsetble.charset() : Charset.defaultCharset();
        this.reader = new InputStreamReader(this.zmodemInput, charset);
    }

    /**
     * 同步处理检测到的ZModem帧。
     *
     * @param frame 帧头字节
     * @throws IOException IO异常
     */
    private void processZModem(byte[] frame) throws IOException {
        TtyZModemProcessor current = this.createProcessor(
                frame, this.input, this.output, this.terminal);
        this.processor = current;
        try {
            current.process();
            if (current.isCancelled()) {
                this.zmodemInput.discardAvailable();
                synchronized (this.output) {
                    this.output.write('\r');
                    this.output.flush();
                }
            }
        } finally {
            this.processor = null;
            this.onProcessorFinished(current);
        }
    }

    /**
     * ZModem处理器结束后的回调。
     *
     * @param processor 处理器
     */
    protected void onProcessorFinished(TtyZModemProcessor processor) {
    }

    /**
     * 创建ZModem处理器。
     *
     * @param frame    ZModem帧数据
     * @param input    输入流
     * @param output   输出流
     * @param terminal 终端对象
     * @return ZModem处理器
     */
    protected TtyZModemProcessor createProcessor(byte[] frame, InputStream input, OutputStream output, Terminal terminal) {
        return new TtyZModemProcessor(frame, input, output, terminal);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if (this.processor != null) {
            // 文件传输期间普通输入会破坏ZModem数据，只允许Ctrl+C取消传输。
            if (bytes.length > 0 && bytes[0] == 0x03) {
                this.processor.cancel();
            }
            return;
        }
        this.connector.write(bytes);
    }

    @Override
    public void write(String string) throws IOException {
        if (this.processor != null) {
            if (!string.isEmpty() && string.charAt(0) == 0x03) {
                this.processor.cancel();
            }
            return;
        }
        this.connector.write(string);
    }

    @Override
    public boolean isConnected() {
        return this.connector != null && this.connector.isConnected();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return this.connector.waitFor();
    }

    @Override
    public boolean ready() throws IOException {
        return this.connector.ready();
    }

    @Override
    public String getName() {
        return this.connector.getName();
    }

    @Override
    public void close() {
        if (this.connector != null) {
            TtyZModemProcessor currentProcessor = this.processor;
            if (currentProcessor != null) {
                try {
                    currentProcessor.cancel();
                } catch (IOException ignore) {
                    // 连接已经不可用时无需再次处理
                }
            }
            TtyStreamable currentConnector = this.connector;
            this.terminal = null;
            this.connector = null;
            this.processor = null;
            this.zmodemInput = null;
            this.input = null;
            this.output = null;
            this.reader = null;
            currentConnector.close();
            if (JulLog.isInfoEnabled()) {
                JulLog.info("close ZModem tty");
            }
        }
    }

    @Override
    public void resize(@NotNull TermSize termSize) {
        this.connector.resize(termSize);
    }
}
