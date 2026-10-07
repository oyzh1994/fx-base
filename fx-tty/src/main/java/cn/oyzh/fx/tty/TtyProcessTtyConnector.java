package cn.oyzh.fx.tty;

import cn.oyzh.common.log.JulLog;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.ProcessTtyConnector;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 基于进程的终端连接器抽象类，在 {@link ProcessTtyConnector} 基础上提供终端尺寸、
 * 字符集以及底层输入输出流访问能力。
 *
 * @author oyzh
 * @since 2025-03-04
 */
public abstract class TtyProcessTtyConnector extends ProcessTtyConnector implements TtyTerminalSizeable, TtyStreamable, TtyCharsetble {

    /**
     * 字符集
     */
    private final Charset charset;

    /**
     * 构造基于进程的终端连接器。
     *
     * @param process      终端进程
     * @param charset      字符集
     * @param commandLines 启动命令
     */
    public TtyProcessTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
        this.charset = charset;
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        int len = super.read(buf, offset, length);
        if (len > 0) {
            this.doRead(buf, offset, len);
        }
        return len;
    }

    /**
     * 读取后的处理钩子，子类可覆写以处理读取到的数据。
     *
     * @param buf    数据缓冲区
     * @param offset 数据起始偏移
     * @param len    数据长度
     * @return 处理后的长度
     * @throws IOException IO 异常
     */
    protected int doRead(char[] buf, int offset, int len) throws IOException {
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("shell read: {}", new String(buf));
        }
        return len;
    }

    @Override
    public void write(String str) throws IOException {
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("shell write : {}", str);
        }
        super.write(str);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        String str = new String(bytes, this.myCharset);
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("shell write : {}", str);
        }
        super.write(bytes);
    }

    @Override
    public void resize(@NotNull TermSize termSize) {
        try {
            this.getProcess().setWinSize(new WinSize(termSize.getColumns(), termSize.getRows()));
            if (this.terminalSizeProperty != null) {
                this.terminalSizeProperty.set(termSize);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public TermSize getTermSize() {
        WinSize winSize = this.getWinSize();
        if (winSize != null) {
            return new TermSize(winSize.getColumns(), winSize.getRows());
        }
        return this.terminalSizeProperty != null ? this.terminalSizeProperty.get() : null;
    }

    /** 终端尺寸属性 */
    private SimpleObjectProperty<TermSize> terminalSizeProperty;

    @Override
    public SimpleObjectProperty<TermSize> terminalSizeProperty() {
        if (this.terminalSizeProperty == null) {
            this.terminalSizeProperty = new SimpleObjectProperty<>(this.getTermSize());
        }
        return this.terminalSizeProperty;
    }

    /**
     * 获取终端进程窗口尺寸。
     *
     * @return 窗口尺寸，获取失败时返回 null
     */
    public WinSize getWinSize() {
        try {
            return this.getProcess().getWinSize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public @NotNull PtyProcess getProcess() {
        return (PtyProcess) super.getProcess();
    }

    @Override
    public Charset charset() {
        return this.charset;
    }

    @Override
    public InputStream input() {
        return this.getProcess().getInputStream();
    }

    @Override
    public OutputStream output() {
        return this.getProcess().getOutputStream();
    }
}