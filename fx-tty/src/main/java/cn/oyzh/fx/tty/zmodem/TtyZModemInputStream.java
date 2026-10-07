package cn.oyzh.fx.tty.zmodem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ZModem输入流，在底层输入流之前追加已预读的字节数据。
 *
 * @author oyzh
 * @since 2026-07-06
 */
public class TtyZModemInputStream extends InputStream {

    /** 底层输入流 */
    private final InputStream input;

    /** 预读字节缓冲区 */
    private final List<Byte> buffer;

    /**
     * 构造 ZModem 输入流。
     *
     * @param input  底层输入流
     * @param buffer 预读字节数据
     */
    public TtyZModemInputStream(InputStream input, byte[] buffer) {
        this.input = input;
        this.buffer = new ArrayList<>();
        for (byte b : buffer) {
            this.buffer.add(b);
        }
    }

    @Override
    public int read() throws IOException {
        if (!this.buffer.isEmpty()) {
            return this.buffer.removeFirst();
        }
        return this.input.read();
    }
}