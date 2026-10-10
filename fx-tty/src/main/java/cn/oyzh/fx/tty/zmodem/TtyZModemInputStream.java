package cn.oyzh.fx.tty.zmodem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ZModem输入流，在底层输入流之前追加已预读的字节数据。
 *
 * @author oyzh
 * @since 2026-07-06
 */
public class TtyZModemInputStream extends InputStream {

    /**
     * 底层输入流
     */
    private final InputStream input;

    /**
     * 预读字节缓冲区
     */
    private final ByteArrayInputStream buffer;

    /**
     * 构造 ZModem 输入流。
     *
     * @param input  底层输入流
     * @param buffer 预读字节数据
     */
    public TtyZModemInputStream(InputStream input, byte[] buffer) {
        this.input = input;
        this.buffer = new ByteArrayInputStream(buffer);
    }

    @Override
    public int read() throws IOException {
        int b = this.buffer.read();
        return b != -1 ? b : this.input.read();
    }
}