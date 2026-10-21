package cn.oyzh.fx.tty.zmodem;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.tty.TtyCharsetble;
import cn.oyzh.fx.tty.TtyStreamable;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TtyConnector;
import org.jetbrains.annotations.NotNull;
import zmodem.xfer.zm.util.ZModemCharacter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * ZModem协议tty连接器，读取数据时检测 ZModem 帧并交由处理器执行文件传输。
 *
 * @author oyzh
 * @since 2026-07-06
 */
public class TtyZModemTtyConnector implements TtyConnector {

    /**
     * ZModem协议前缀
     */
    public static final char[] ZMODEM_PREFIX = new char[]{
            (char) ZModemCharacter.ZPAD.value(),
            (char) ZModemCharacter.ZPAD.value(),
            (char) ZModemCharacter.ZDLE.value()
    };

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
     * ZModem传输使用的原始输入流
     */
    private InputStream zmodemInput;

    /**
     * ZModem传输结束后继续读取的reader
     */
    private Reader reader;

    /**
     * 跨读取保留的可能构成ZModem前缀的数据
     */
    private char[] prefixTail = new char[0];

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
        // 处理ZModem
        if (this.processor != null) {
            try {
                this.processor.process();
            } finally {
                this.processor = null;
                this.resetReader();
            }
        }

        // 部分SSH实现（例如JSch）每次调用input()都会创建新的流并替换channel的接收端。
        // ZModem结束后必须继续读取协议使用的同一条流，不能回到连接器缓存的旧reader。
        if (this.prefixTail.length >= length) {
            return 0;
        }

        char[] chunk = new char[length - this.prefixTail.length];
        int i = this.readSource(chunk, 0, chunk.length);
        if (i <= 0) {
            return i;
        }

        char[] data = new char[this.prefixTail.length + i];
        System.arraycopy(this.prefixTail, 0, data, 0, this.prefixTail.length);
        System.arraycopy(chunk, 0, data, this.prefixTail.length, i);
        int e = indexOfZModem(data);
        if (e == -1) {
            int hold = prefixTailLength(data);
            int resultLength = data.length - hold;
            System.arraycopy(data, 0, buffer, offset, resultLength);
            this.prefixTail = Arrays.copyOfRange(data, resultLength, data.length);
            return resultLength;
        }

        // sz/rz类型位于ZModem帧头的第6个字符，头部未到齐时继续缓存，避免误判传输方向。
        if (data.length - e < 6) {
            System.arraycopy(data, 0, buffer, offset, e);
            this.prefixTail = Arrays.copyOfRange(data, e, data.length);
            return e;
        }

        char[] frame = Arrays.copyOfRange(data, e, data.length);
        this.prefixTail = new char[0];
        System.arraycopy(data, 0, buffer, offset, e);
        this.zmodemInput = this.connector.input();
        // 创建ZModem处理器
        this.processor = this.createProcessor(frame, this.zmodemInput, this.connector.output(), this.terminal);
        return e;
    }

    /**
     * 从当前数据源读取字符。
     *
     * @param buffer 目标缓冲区
     * @param offset 偏移
     * @param length 长度
     * @return 读取长度
     * @throws IOException IO异常
     */
    private int readSource(char[] buffer, int offset, int length) throws IOException {
        if (this.reader != null) {
            return this.reader.read(buffer, offset, length);
        }
        return this.connector.read(buffer, offset, length);
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
    protected TtyZModemProcessor createProcessor(char[] frame, InputStream input, OutputStream output, Terminal terminal) {
        return new TtyZModemProcessor(frame, input, output, terminal);
    }

    /**
     * 使用ZModem协议流重置读取器，用于传输结束后恢复普通终端读取。
     */
    private void resetReader() {
        if (this.zmodemInput != null) {
            Charset charset = this.connector instanceof TtyCharsetble charsetble
                    ? charsetble.charset() : Charset.defaultCharset();
            this.reader = new InputStreamReader(this.zmodemInput, charset);
        }
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        // 取消ZModem
        if (this.processor != null && bytes.length > 0 && bytes[0] == 0x03) {
            this.processor.cancel();
            return;
        }
        this.connector.write(bytes);
    }

    @Override
    public void write(String string) throws IOException {
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
            this.terminal = null;
            this.connector = null;
            this.processor = null;
            this.zmodemInput = null;
            this.reader = null;
            this.prefixTail = new char[0];
            if (JulLog.isInfoEnabled()) {
                JulLog.info("close ZModem tty");
            }
        }
    }

    /**
     * 获取ZModem协议前缀位置
     *
     * @param a 字符数组
     * @return 前缀起始位置，未找到时返回 -1
     */
    private static int indexOfZModem(char[] a) {
        if (a.length >= ZMODEM_PREFIX.length) {
            for (int i = 0; i <= a.length - ZMODEM_PREFIX.length; i++) {
                if (a[i] != ZMODEM_PREFIX[0]) {
                    continue;
                }
                if (a[i + 1] != ZMODEM_PREFIX[1]) {
                    continue;
                }
                if (a[i + 2] != ZMODEM_PREFIX[2]) {
                    continue;
                }
                // char[] range = Arrays.copyOfRange(a, i, i + ZMODEM_PREFIX.length);
                // if (Arrays.equals(range, ZMODEM_PREFIX)) {
                //     return i;
                // }
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取数组末尾可能是ZModem前缀的字符数。
     *
     * @param data 数据
     * @return 需要保留到下次读取的字符数
     */
    private static int prefixTailLength(char[] data) {
        int max = Math.min(ZMODEM_PREFIX.length - 1, data.length);
        for (int length = max; length > 0; length--) {
            boolean matches = true;
            for (int i = 0; i < length; i++) {
                if (data[data.length - length + i] != ZMODEM_PREFIX[i]) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return length;
            }
        }
        return 0;
    }

    @Override
    public void resize(@NotNull TermSize termSize) {
        this.connector.resize(termSize);
    }
}
