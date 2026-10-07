package cn.oyzh.fx.tty;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.IOUtil;
import com.jediterm.core.util.TermSize;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * 基于输入输出流的终端连接器抽象类，提供读写、终端尺寸与字符集等通用实现。
 *
 * @author oyzh
 * @since 2025-03-04
 */
public abstract class TtyStreamConnector implements TtyTerminalSizeable, TtyStreamable, TtyCharsetble {

    /**
     * 字符集
     */
    private final Charset charset;

    /**
     * 读取器
     */
    protected InputStreamReader reader;

    /**
     * 写入器
     */
    protected OutputStreamWriter writer;

    /**
     * 使用系统默认字符集构造连接器。
     */
    public TtyStreamConnector() {
        this(Charset.defaultCharset());
    }

    /**
     * 构造连接器。
     *
     * @param charset 字符集
     */
    public TtyStreamConnector(Charset charset) {
        this.charset = charset;
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        if (this.reader == null) {
            return this.doRead(buf, offset, length);
        }
        int len = this.reader.read(buf, offset, length);
        if (len > 0) {
            return this.doRead(buf, offset, len);
        }
        return len;
    }

    /**
     * 读取后的处理钩子，子类可覆写以处理读取到的数据。
     *
     * @param buf    数据缓冲区
     * @param offset 数据起始偏移
     * @param length 数据长度
     * @return 处理后的长度
     * @throws IOException IO 异常
     */
    protected int doRead(char[] buf, int offset, int length) throws IOException {
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("shell read: {}", new String(buf));
        }
        return length;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if (this.writer == null) {
            return;
        }
        String str = new String(bytes, this.charset());
        this.write(str);
    }

    @Override
    public void write(String str) throws IOException {
        if (this.writer == null) {
            return;
        }
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("shell write : {}", str);
        }
        this.writer.write(str);
        this.writer.flush();
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    @Override
    public boolean ready() throws IOException {
        return true;
    }

    @Override
    public void resize(@NotNull TermSize termSize) {
        this.terminalSizeProperty().set(termSize);
    }

    @Override
    public void close() {
        IOUtil.close(this.input());
        IOUtil.close(this.output());
        IOUtil.close(this.reader);
        IOUtil.close(this.writer);
        this.reader = null;
        this.writer = null;
    }

    /** 终端尺寸属性 */
    private SimpleObjectProperty<TermSize> terminalSizeProperty;

    @Override
    public SimpleObjectProperty<TermSize> terminalSizeProperty() {
        if (this.terminalSizeProperty == null) {
            this.terminalSizeProperty = new SimpleObjectProperty<>();
        }
        return this.terminalSizeProperty;
    }

    @Override
    public Charset charset() {
        return this.charset;
    }
}