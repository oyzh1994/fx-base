package cn.oyzh.fx.tty;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.tty.ghostty.Terminal;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.LockSupport;

/**
 * 把 JediTerm 的 {@link TtyConnector} 适配为 Ghostty 终端所需的 {@link Terminal}。
 *
 * <p>整个 easyshell 的后端（本地进程、SSH、Telnet、RLogin、串口、Mosh 等）都统一以
 * {@link TtyConnector} 暴露，因此通过本适配器可以让 Ghostty 引擎复用全部既有连接器，
 * 而不需要为每种后端单独实现一套 Ghostty 后端。</p>
 *
 * <p>输出方向：{@link TtyConnector#read(char[], int, int)} 读到的字符先还原为 Unicode，
 * 再编码为 UTF-8 交给 libghostty-vt，避免远端非 UTF-8 字符集被终端误解析。
 * 输入方向：Ghostty 产生的 UTF-8 按键字节先解码为字符串，再通过
 * {@link TtyConnector#write(String)} 按连接器自身字符集写回，保证多字节输入正确。</p>
 *
 * @author oyzh
 * @since 2026-11-11
 */
public final class TtyConnectorTerminal implements Terminal {

    /**
     * 单次读取的字符数
     */
    private static final int READ_BUFFER_SIZE = 8192;

    private final TtyConnector connector;

    private final int columns;

    private final int rows;

    private final Object startLock = new Object();

    private boolean started;

    private volatile boolean closed;

    public TtyConnectorTerminal(TtyConnector connector, int columns, int rows) {
        if (connector == null) {
            throw new IllegalArgumentException("connector must not be null");
        }
        this.connector = connector;
        this.columns = columns;
        this.rows = rows;
    }

    /**
     * 获取被适配的连接器。
     *
     * @return 连接器
     */
    public TtyConnector connector() {
        return this.connector;
    }

    @Override
    public InputStream output() throws Exception {
        this.ensureStarted();
        return new ConnectorInputStream();
    }

    @Override
    public OutputStream input() throws Exception {
        this.ensureStarted();
        return new ConnectorOutputStream();
    }

    @Override
    public void resize(int columns, int rows, int widthPx, int heightPx) {
        if (this.closed) {
            return;
        }
        try {
            this.connector.resize(new TermSize(columns, rows));
        } catch (Exception ex) {
            JulLog.debug("resize tty connector failed", ex);
        }
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        try {
            this.connector.close();
        } catch (Exception ex) {
            JulLog.debug("close tty connector failed", ex);
        }
    }

    /**
     * 初始化连接器。部分连接器（SSH、Mosh、串口等）只有在 {@code ready()} 之后才会
     * 真正创建 shell 或数据通道。
     *
     * @throws IOException 初始化失败
     */
    private void ensureStarted() throws IOException {
        synchronized (this.startLock) {
            if (this.started) {
                if (this.closed) {
                    throw new IOException("tty connector is closed");
                }
                return;
            }
            this.started = true;
            try {
                this.connector.ready();
            } catch (Exception ex) {
                throw new IOException("tty connector is not ready", ex);
            }
            try {
                this.connector.resize(new TermSize(this.columns, this.rows));
            } catch (Exception ex) {
                JulLog.debug("initial resize of tty connector failed", ex);
            }
        }
        if (this.closed) {
            throw new IOException("tty connector is closed");
        }
    }

    /**
     * 把字符流编码为 UTF-8 字节流，供 libghostty-vt 解析。
     */
    private final class ConnectorInputStream extends InputStream {

        private final char[] chars = new char[READ_BUFFER_SIZE];

        private byte[] pending = new byte[0];

        private int pendingOffset;

        private char pendingHighSurrogate;

        @Override
        public int read() throws IOException {
            if (!this.ensurePending()) {
                return -1;
            }
            return this.pending[this.pendingOffset++] & 0xFF;
        }

        @Override
        public int read(byte[] bytes, int offset, int length) throws IOException {
            if (length == 0) {
                return 0;
            }
            if (!this.ensurePending()) {
                return -1;
            }
            int count = Math.min(length, this.pending.length - this.pendingOffset);
            System.arraycopy(this.pending, this.pendingOffset, bytes, offset, count);
            this.pendingOffset += count;
            return count;
        }

        @Override
        public int available() {
            return this.pending.length - this.pendingOffset;
        }

        private boolean ensurePending() throws IOException {
            while (this.pendingOffset >= this.pending.length) {
                if (closed) {
                    return false;
                }
                int offset = 0;
                if (this.pendingHighSurrogate != 0) {
                    this.chars[0] = this.pendingHighSurrogate;
                    this.pendingHighSurrogate = 0;
                    offset = 1;
                }
                int read = connector.read(this.chars, offset, this.chars.length - offset);
                if (read < 0) {
                    return false;
                }
                if (read == 0) {
                    // 部分连接器以 0 表示暂时没有数据，避免忙等。
                    LockSupport.parkNanos(1_000_000L);
                    continue;
                }
                int count = read + offset;
                // 读取边界可能把代理对拆开，保留高代理位等待下一次读取。
                if (Character.isHighSurrogate(this.chars[count - 1])) {
                    this.pendingHighSurrogate = this.chars[count - 1];
                    count--;
                }
                if (count == 0) {
                    continue;
                }
                this.pending = new String(this.chars, 0, count).getBytes(StandardCharsets.UTF_8);
                this.pendingOffset = 0;
            }
            return true;
        }
    }

    /**
     * 把 Ghostty 产生的 UTF-8 按键字节写回连接器。
     */
    private final class ConnectorOutputStream extends OutputStream {

        @Override
        public void write(int value) throws IOException {
            this.write(new byte[]{(byte) value}, 0, 1);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            if (length == 0) {
                return;
            }
            if (closed) {
                throw new IOException("tty connector is closed");
            }
            // Ghostty 输出的是 UTF-8 字节，先解码为 Unicode，再交给连接器按自身字符集编码。
            String text = new String(bytes, offset, length, StandardCharsets.UTF_8);
            connector.write(text);
        }

        @Override
        public void flush() {
            // connector.write 已经同步写入
        }

        @Override
        public void close() {
            // 连接器由 TtyConnectorTerminal.close() 统一关闭
        }
    }
}
