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
 * @author oyzh
 * @since 2025-03-04
 */
public class TtyDefaultTtyConnector extends ProcessTtyConnector {

    public TtyDefaultTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        int len = super.read(buf, offset, length);
        if (len > 0) {
            this.doRead(buf, offset, len);
        }
        return len;
    }

    protected int doRead(char[] buf, int offset, int len) throws IOException {
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("shell read: {}", new String(buf));
        }
        return len;
    }

    public void writeLine(String str) throws IOException {
        this.write(str + "\r");
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

    private SimpleObjectProperty<TermSize> terminalSizeProperty;

    public SimpleObjectProperty<TermSize> terminalSizeProperty() {
        if (this.terminalSizeProperty == null) {
            this.terminalSizeProperty = new SimpleObjectProperty<>(this.getTermSize());
        }
        return this.terminalSizeProperty;
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

    public TermSize getTermSize() {
        WinSize winSize = this.getWinSize();
        if (winSize != null) {
            return new TermSize(winSize.getColumns(), winSize.getRows());
        }
        return null;
    }

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
    public String getName() {
        return "javafx-tty";
    }

    /**
     * 获取真实的输入流
     *
     * @return 输入流
     */
    public InputStream input() {
        return null;
    }

    /**
     * 获取真实的输出流
     *
     * @return 输出流
     */
    public OutputStream output() {
        return null;
    }
}