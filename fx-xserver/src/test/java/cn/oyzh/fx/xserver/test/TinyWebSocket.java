package cn.oyzh.fx.xserver.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

/**
 * A WebSocket client small enough to read, for the tests that need one.
 *
 * <p>Part of the verification kit's test side: the browser path (backlog D2) is a WebSocket server in
 * the display server, and a test of it needs a client that speaks the protocol on the wire rather
 * than a library that hides it - the handshake, the masking, the length encodings and the control
 * frames are the parts worth testing, and a client that did them for us would be testing itself.
 *
 * <p>Everything a client must do, it does: the upgrade with a real {@code Sec-WebSocket-Key}, frames
 * **masked** (a server may reject an unmasked one, and this server does), and the payload lengths
 * encoded the way the protocol says.
 */
public final class TinyWebSocket implements AutoCloseable {

    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private final Random random = new Random(20260926L);

    private TinyWebSocket(Socket socket, InputStream in, OutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    /** Connects, sends {@code GET} with a key, and checks the accept the server answered with. */
    public static TinyWebSocket connect(String host, int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 4000);
        socket.setSoTimeout(4000);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        byte[] keyBytes = new byte[16];
        new Random(7L).nextBytes(keyBytes);
        String key = Base64.getEncoder().encodeToString(keyBytes);
        out.write(("GET / HTTP/1.1\r\n"
                + "Host: " + host + ":" + port + "\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Key: " + key + "\r\n"
                + "Sec-WebSocket-Version: 13\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
        out.flush();

        String response = readHeaderBlock(in);
        if (response == null || !response.startsWith("HTTP/1.1 101")) {
            throw new IOException("the upgrade was refused: " + response);
        }
        String expected = "Sec-WebSocket-Accept: "
                + cn.oyzh.fx.xserver.net.WebSocketListener.accept(key);
        if (!response.contains(expected)) {
            throw new IOException("the handshake's accept does not match: " + response);
        }
        return new TinyWebSocket(socket, in, out);
    }

    /** One masked binary frame, whatever the length. */
    public void sendBinary(byte[] payload) throws IOException {
        sendFrame(0x2, payload, true);
    }

    /** One frame of a fragmented message; {@code last} marks the end of it. */
    public void sendFragment(byte[] payload, boolean last) throws IOException {
        sendFrame(last ? 0x0 : 0x0, payload, last);
    }

    /** The first frame of a fragmented message (opcode binary, not last). */
    public void sendFirstFragment(byte[] payload) throws IOException {
        sendFrame(0x2, payload, false);
    }

    public void sendPing(byte[] payload) throws IOException {
        sendFrame(0x9, payload, true);
    }

    /**
     * A frame with the mask bit off, which a client must not send and this server refuses.
     *
     * <p>Written here rather than by a raw socket in the test: the refusal only happens inside an
     * upgraded connection, so the frame has to arrive after the handshake - and a socket that skips
     * the handshake looks to the server like a client that has not finished asking.
     */
    public void sendUnmaskedFrame(byte[] payload) throws IOException {
        out.write(0x82);
        out.write(payload.length);
        out.write(payload);
        out.flush();
    }

    public void sendClose() throws IOException {
        sendFrame(0x8, new byte[0], true);
    }

    private void sendFrame(int opcode, byte[] payload, boolean last) throws IOException {
        out.write((last ? 0x80 : 0x00) | opcode);
        if (payload.length < 126) {
            out.write(0x80 | payload.length);
        } else if (payload.length <= 0xFFFF) {
            out.write(0x80 | 126);
            out.write((payload.length >> 8) & 0xFF);
            out.write(payload.length & 0xFF);
        } else {
            out.write(0x80 | 127);
            for (int i = 7; i >= 0; i--) {
                out.write((int) (((long) payload.length >> (8 * i)) & 0xFF));
            }
        }
        byte[] mask = new byte[4];
        random.nextBytes(mask);
        out.write(mask);
        byte[] masked = payload.clone();
        for (int i = 0; i < masked.length; i++) {
            masked[i] ^= mask[i & 3];
        }
        out.write(masked);
        out.flush();
    }

    /** The next frame: its opcode and its payload. */
    public Frame readFrame() throws IOException {
        int first = in.read();
        int second = in.read();
        if (first < 0 || second < 0) {
            throw new IOException("the server closed the connection");
        }
        if ((second & 0x80) != 0) {
            throw new IOException("a server must not mask its frames");
        }
        long length = second & 0x7F;
        if (length == 126) {
            length = ((long) in.read() << 8) | in.read();
        } else if (length == 127) {
            length = 0;
            for (int i = 0; i < 8; i++) {
                length = (length << 8) | in.read();
            }
        }
        byte[] payload = new byte[(int) length];
        int read = 0;
        while (read < payload.length) {
            int count = in.read(payload, read, payload.length - read);
            if (count < 0) {
                throw new IOException("the server closed mid-frame");
            }
            read += count;
        }
        return new Frame(first & 0x0F, payload);
    }

    /**
     * The data frames' payloads as one byte stream, which is how a client that speaks a *stream*
     * protocol (RFB) reads them: frame boundaries are the transport's business, not the protocol's.
     */
    public Payloads payloads() {
        return new Payloads();
    }

    public final class Payloads {

        private final java.io.ByteArrayOutputStream buffered = new java.io.ByteArrayOutputStream();
        private int at;

        /** Exactly {@code count} bytes, reading as many frames as that takes. */
        public byte[] read(int count) throws IOException {
            byte[] result = new byte[count];
            int read = 0;
            while (read < count) {
                if (at >= buffered.size()) {
                    buffered.reset();
                    at = 0;
                    Frame frame = readFrame();
                    if (frame.opcode() != 0x2 && frame.opcode() != 0x1) {
                        throw new IOException("a control frame where data was expected: "
                                + frame.opcode());
                    }
                    buffered.write(frame.payload());
                }
                byte[] data = buffered.toByteArray();
                int take = Math.min(count - read, data.length - at);
                System.arraycopy(data, at, result, read, take);
                at += take;
                read += take;
            }
            return result;
        }

        public String readAscii(int count) throws IOException {
            return new String(read(count), StandardCharsets.US_ASCII);
        }
    }

    /** One frame: what it is, and what it carried. */
    public record Frame(int opcode, byte[] payload) {
        public String text() {
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    /** The read timeout for the next byte, in milliseconds. */
    public void readTimeout(int millis) throws IOException {
        socket.setSoTimeout(millis);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    private static String readHeaderBlock(InputStream in) throws IOException {
        StringBuilder header = new StringBuilder();
        int matched = 0;
        while (header.length() < 16384) {
            int b = in.read();
            if (b < 0) {
                return null;
            }
            header.append((char) b);
            // CR LF CR LF, and the second CR continues the sequence rather than restarting it.
            if (b == '\r') {
                matched = matched == 2 ? 3 : 1;
            } else if (b == '\n') {
                matched = matched == 1 || matched == 3 ? matched + 1 : 0;
            } else {
                matched = 0;
            }
            if (matched == 4) {
                return header.toString();
            }
        }
        return null;
    }
}
