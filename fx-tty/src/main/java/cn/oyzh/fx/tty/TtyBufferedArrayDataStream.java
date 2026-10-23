package cn.oyzh.fx.tty;

import com.jediterm.terminal.ArrayTerminalDataStream;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.util.CharUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * 基于 {@link TtyConnector} 的终端数据流。
 * <p>
 * Jediterm 默认实现只使用 1024 字符缓冲，首屏内容超过该长度时会被拆成多次读取和绘制，
 * 导致终端内容分阶段显示。这里使用更大的缓冲，让一次读取尽可能覆盖完整首屏。
 * <p>
 * 该实现默认不启用，需要设置 {@code -Dfx-tty.firstScreenBuffer=true}。
 *
 * @author oyzh
 * @since 2026-10-24
 */
public class TtyBufferedArrayDataStream extends ArrayTerminalDataStream {

    /**
     * 默认读取缓冲大小
     */
    private static final int DEFAULT_BUFFER_SIZE = 16 * 1024;

    /**
     * 终端连接器
     */
    private final TtyConnector connector;

    /**
     * 阻塞等待前的回调
     */
    private final @Nullable Runnable onBeforeBlockingWait;

    /**
     * 重绘开关
     */
    private final @Nullable TtyRepaintGate repaintGate;

    public TtyBufferedArrayDataStream(TtyConnector connector, @Nullable Runnable onBeforeBlockingWait) {
        this(connector, onBeforeBlockingWait, null);
    }

    public TtyBufferedArrayDataStream(TtyConnector connector, @Nullable Runnable onBeforeBlockingWait,
                                      @Nullable TtyRepaintGate repaintGate) {
        super(new char[DEFAULT_BUFFER_SIZE], 0, 0);
        this.connector = connector;
        this.onBeforeBlockingWait = onBeforeBlockingWait;
        this.repaintGate = repaintGate;
    }

    private void fillBuf() throws IOException {
        this.myOffset = 0;

        // 上一块已经处理完，先恢复重绘；若本次又有数据则重新暂停。
        if (this.repaintGate != null) {
            this.repaintGate.resume();
        }

        if (!this.connector.ready() && this.onBeforeBlockingWait != null) {
            this.onBeforeBlockingWait.run();
        }
        this.myLength = this.connector.read(this.myBuf, this.myOffset, this.myBuf.length);

        if (this.myLength <= 0) {
            this.myLength = 0;
            throw new EOF();
        }
        if (this.repaintGate != null) {
            this.repaintGate.pause();
        }
    }

    @Override
    public char getChar() throws IOException {
        if (this.myLength == 0) {
            this.fillBuf();
        }
        return super.getChar();
    }

    @Override
    public String readNonControlCharacters(int maxChars) throws IOException {
        if (this.myLength == 0) {
            this.fillBuf();
        }
        return super.readNonControlCharacters(maxChars);
    }

    @Override
    public String toString() {
        return CharUtils.toHumanReadableText(new String(this.myBuf, this.myOffset, this.myLength));
    }
}
