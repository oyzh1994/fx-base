package cn.oyzh.fx.tty.zmodem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Objects;

import static zmodem.xfer.zm.util.ZModemCharacter.ZDLE;
import static zmodem.xfer.zm.util.ZModemCharacter.ZPAD;
import static zmodem.xfer.zm.util.ZModemCharacter.ZRINIT;
import static zmodem.xfer.zm.util.ZModemCharacter.ZRQINIT;

/**
 * 在原始字节流上检测 ZModem 帧的输入流。
 * <p>
 * 普通数据按原样交给终端读取；检测到帧前缀后暂停字符读取，先完成 ZModem 传输。
 * 所有数据都来自同一条原始流，避免字符 reader 预读导致二进制协议数据丢失。
 *
 * @author oyzh
 * @since 2026-10-21
 */
public class TtyZModemDetectingInputStream extends InputStream {

    /** ZModem协议前缀 */
    private static final byte[] PREFIX = {ZPAD.value(), ZPAD.value(), ZDLE.value()};

    /** 取消后等待流静默的默认时间 */
    private static final long DEFAULT_CANCEL_QUIET_MILLIS = 250;

    /** 取消后清理残留数据的最长时间 */
    private static final long DEFAULT_CANCEL_MAX_MILLIS = 5000;

    /** 检测到ZModem帧后的处理器 */
    private final Handler handler;

    /** 原始输入流 */
    private final InputStream input;

    /** 等待判断是否为ZModem前缀的字节 */
    private final ArrayDeque<Integer> pending = new ArrayDeque<>(PREFIX.length);

    /**
     * 构造检测流。
     *
     * @param input   原始输入流
     * @param handler ZModem帧处理器
     */
    public TtyZModemDetectingInputStream(InputStream input, Handler handler) {
        this.input = Objects.requireNonNull(input, "input");
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public int read() throws IOException {
        return this.nextTerminalByte();
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, buffer.length);
        if (length == 0) {
            return 0;
        }

        int count = 0;
        while (count < length) {
            // 已经有数据时不再等待后续字节，避免交互式终端被批量读取阻塞。
            if (count > 0 && this.input.available() <= 0) {
                break;
            }
            int value = this.nextTerminalByte();
            if (value < 0) {
                if (count == 0) {
                    return -1;
                }
                break;
            }
            buffer[offset + count++] = (byte) value;
        }
        return count;
    }

    /**
     * 获取下一个终端字节，遇到ZModem前缀时先执行文件传输。
     *
     * @return 字节值，流结束时返回-1
     * @throws IOException IO异常
     */
    private int nextTerminalByte() throws IOException {
        while (true) {
            int matched = this.matchPrefix();
            if (matched == PREFIX.length) {
                byte[] frame = this.readFrameHeader();
                // 只有合法的 ZRQINIT/ZRINIT 才启动传输。否则把候选字节还给终端，
                // 避免文件内容或提示符中偶然出现的 "* * ZDLE" 被误判成新的 rz/sz。
                if (isTransferRequest(frame)) {
                    this.handler.handle(frame);
                    continue;
                }
                return this.rejectFrame(frame);
            }
            if (!this.pending.isEmpty() && (matched < this.pending.size() || this.pending.size() >= PREFIX.length)) {
                return this.pending.removeFirst();
            }

            int value = this.input.read();
            if (value < 0) {
                if (!this.pending.isEmpty()) {
                    return this.pending.removeFirst();
                }
                return -1;
            }
            this.pending.addLast(value);
        }
    }

    /**
     * 读取ZModem帧头前6个字节，用于判断接收还是发送。
     *
     * @return 帧头字节
     * @throws IOException IO异常
     */
    private byte[] readFrameHeader() throws IOException {
        byte[] frame = new byte[6];
        for (int i = 0; i < frame.length; i++) {
            // 重叠前缀可能已经预读了部分帧头，优先从 pending 中取，避免丢字节。
            Integer value = this.pending.pollFirst();
            if (value == null) {
                int read = this.input.read();
                if (read < 0) {
                    throw new IOException("ZModem stream closed before frame header");
                }
                value = read;
            }
            frame[i] = (byte) (int) value;
        }
        return frame;
    }

    /**
     * 丢弃当前候选帧的首字节，并把其余字节放回待处理队列。
     * <p>
     * 不把全部字节放回，否则前缀会再次命中并造成死循环；只丢弃首字节还能保留
     * "* * ZDLE" 之间的重叠匹配。
     *
     * @param frame 候选帧
     * @return 应当返回给终端读取器的首字节
     */
    private int rejectFrame(byte[] frame) {
        // 已匹配的前缀仍保存在 pending 中，先清掉，避免前缀被重复识别。
        this.pending.clear();
        for (int i = frame.length - 1; i >= 1; i--) {
            this.pending.addFirst(frame[i] & 0xFF);
        }
        return frame[0] & 0xFF;
    }

    /**
     * 判断候选帧是否是 ZModem 的传输入口（ZRQINIT 或 ZRINIT）。
     *
     * @param frame 候选帧
     * @return 是否是传输入口
     */
    static boolean isTransferRequest(byte[] frame) {
        if (frame == null || frame.length < 6) {
            return false;
        }
        int type = headerType(frame);
        return type == (ZRQINIT.value() & 0xFF) || type == (ZRINIT.value() & 0xFF);
    }

    /**
     * 判断候选帧是否表示远端发送文件（ZRQINIT）。
     *
     * @param frame 候选帧
     * @return 是否接收文件
     */
    static boolean isReceiveRequest(byte[] frame) {
        return headerType(frame) == (ZRQINIT.value() & 0xFF);
    }

    /**
     * 解析候选帧中的头部类型。
     *
     * @param frame 候选帧
     * @return 头部类型，无法识别时返回 -1
     */
    static int headerType(byte[] frame) {
        if (frame == null || frame.length < 6) {
            return -1;
        }
        switch (frame[3]) {
            case 'B':
                int high = hexValue(frame[4]);
                int low = hexValue(frame[5]);
                return high < 0 || low < 0 ? -1 : (high << 4) | low;
            case 'A':
            case 'C':
                return frame[4] & 0xFF;
            default:
                return -1;
        }
    }

    private static int hexValue(byte value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        if (value >= 'a' && value <= 'f') {
            return value - 'a' + 10;
        }
        if (value >= 'A' && value <= 'F') {
            return value - 'A' + 10;
        }
        return -1;
    }

    /**
     * 计算待处理字节与ZModem前缀的匹配长度。
     *
     * @return 匹配长度
     */
    private int matchPrefix() {
        int matched = 0;
        for (int value : this.pending) {
            if (matched >= PREFIX.length || (byte) value != PREFIX[matched]) {
                break;
            }
            matched++;
        }
        return matched;
    }

    @Override
    public int available() throws IOException {
        return this.pending.size() + this.input.available();
    }

    /**
     * 丢弃取消传输后已经到达的残留协议数据。
     * <p>
     * 远端 lrzsz 收到 CAN 后可能直接退出并回到 shell，不保证再回复 CAN，
     * 因此这里不能无限等待协议响应，只能等待输入短暂静默后恢复终端。
     *
     * @param quietMillis 连续无数据多久后认为清理完成
     * @param maxMillis   最长清理时间
     * @throws IOException IO异常
     */
    public void discardAvailable(long quietMillis, long maxMillis) throws IOException {
        this.pending.clear();
        byte[] scratch = new byte[8192];
        long start = System.currentTimeMillis();
        long lastData = start;
        while (true) {
            long now = System.currentTimeMillis();
            int available = this.input.available();
            if (available > 0) {
                int length = Math.min(available, scratch.length);
                int read = this.input.read(scratch, 0, length);
                if (read > 0) {
                    lastData = System.currentTimeMillis();
                } else if (read < 0) {
                    return;
                }
            } else if (now - lastData >= quietMillis || now - start >= maxMillis) {
                return;
            } else {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /**
     * 丢弃取消传输后已经到达的残留协议数据。
     *
     * @throws IOException IO异常
     */
    public void discardAvailable() throws IOException {
        this.discardAvailable(DEFAULT_CANCEL_QUIET_MILLIS, DEFAULT_CANCEL_MAX_MILLIS);
    }

    @Override
    public void close() throws IOException {
        this.input.close();
    }

    /**
     * ZModem帧处理器。
     */
    @FunctionalInterface
    public interface Handler {

        /**
         * 处理ZModem帧。
         *
         * @param frame 帧头字节
         * @throws IOException IO异常
         */
        void handle(byte[] frame) throws IOException;
    }
}
