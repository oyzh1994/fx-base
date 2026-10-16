package cn.oyzh.fx.xserver.net;

import cn.oyzh.common.log.JulLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * A WebSocket server that carries a byte stream, which is what a browser needs to reach this server
 * (backlog D2's browser path).
 *
 * <p>VNC in a browser means a page that speaks RFB over a WebSocket - noVNC is the one everybody
 * uses - and browsers cannot open a TCP socket. Until now the way through was a bridge process
 * ({@code websockify}) in front of the RFB port; this is that bridge inside the server, so a browser
 * can be pointed at it directly and nothing has to be installed.
 *
 * <p><b>It carries bytes, not messages.</b> RFC 6455 is a message protocol and RFB is a byte stream,
 * so the two adapters here do the only sensible translation: every incoming message's payload is
 * appended to the stream a reader sees, and {@code flush} on the writer sends everything written
 * since the last flush as one binary frame. RFB's own messages already end with a flush, so one RFB
 * message is one WebSocket frame - but nothing depends on that, which is the point: the session
 * above does not know it is inside frames at all.
 *
 * <p>What it implements is the part a client sends and a server must understand: the HTTP upgrade
 * (with the {@code Sec-WebSocket-Accept} every browser checks), masked frames with 7, 16 and 64 bit
 * payload lengths, fragmentation, and the control frames - a ping is answered, a close ends the
 * stream. What it does not: extensions, subprotocols, and any server-to-client masking (which the
 * protocol forbids).
 *
 * <p>Loopback is the caller's decision, not this class's: it binds whatever address it is given, and
 * the VNC backend gives it the same one the RFB port uses.
 */
public final class WebSocketListener {


    /** The magic string RFC 6455 appends to a client's key to prove the server understood the ask. */
    private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    /** The largest message this server will read in one piece, and the largest it will send. */
    private static final int MAX_MESSAGE_BYTES = 16 * 1024 * 1024;

    private static final int OP_CONTINUATION = 0x0;
    private static final int OP_TEXT = 0x1;
    private static final int OP_BINARY = 0x2;
    private static final int OP_CLOSE = 0x8;
    private static final int OP_PING = 0x9;
    private static final int OP_PONG = 0xA;

    private final int port;
    private final boolean loopbackOnly;
    private final Consumer<Connection> onConnection;
    private final AtomicBoolean running = new AtomicBoolean();
    private volatile ServerSocket socket;
    private Thread acceptThread;
    private volatile int boundPort;

    /**
     * @param port         the port to listen on, or 0 for any free one
     * @param loopbackOnly true to bind loopback only
     * @param onConnection what to do with an upgraded connection; called on its own thread, and
     *                     <b>it must not return until the connection is done</b> - the listener closes
     *                     the socket when it does
     */
    public WebSocketListener(int port, boolean loopbackOnly, Consumer<Connection> onConnection) {
        this.port = port;
        this.loopbackOnly = loopbackOnly;
        this.onConnection = onConnection;
    }

    /** Binds the port and accepts connections in the background. */
    public void start() throws IOException {
        ServerSocket bound = new ServerSocket();
        bound.setReuseAddress(true);
        bound.bind(new InetSocketAddress(loopbackOnly ? InetAddress.getLoopbackAddress() : null, port));
        socket = bound;
        boundPort = bound.getLocalPort();
        running.set(true);
        acceptThread = new Thread(this::accept, "xserver-websocket");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public int boundPort() {
        return boundPort;
    }

    public void stop() {
        running.set(false);
        ServerSocket open = socket;
        if (open != null) {
            try {
                open.close();
            } catch (IOException e) {
                JulLog.debug("closing the websocket listener", e);
            }
        }
    }

    private void accept() {
        while (running.get()) {
            try {
                Socket client = socket.accept();
                new Thread(() -> upgrade(client), "xserver-websocket-client").start();
            } catch (IOException e) {
                if (running.get()) {
                    JulLog.debug("accepting a websocket client failed", e);
                }
            }
        }
    }

    /** The upgrade, and then the caller's connection handler on this client's own thread. */
    private void upgrade(Socket client) {
        Connection connection = null;
        try {
            client.setTcpNoDelay(true);
            InputStream in = new BufferedInputStream(client.getInputStream(), 8192);
            OutputStream out = new BufferedOutputStream(client.getOutputStream(), 8192);
            if (!handshake(in, out)) {
                client.close();
                return;
            }
            connection = new Connection(client, in, out);
            onConnection.accept(connection);
        } catch (IOException e) {
            JulLog.debug("websocket connection failed", e);
        } finally {
            if (connection != null) {
                connection.close();
            } else {
                try {
                    client.close();
                } catch (IOException ignored) {
                    // Nothing left to do about it.
                }
            }
        }
    }

    /**
     * The HTTP upgrade. Returns false when the request is not a WebSocket upgrade, in which case a
     * plain answer has already been sent - a browser that opens the port by hand gets a sentence
     * rather than a dropped connection.
     */
    private boolean handshake(InputStream in, OutputStream out) throws IOException {
        String request = readHeaderBlock(in);
        if (request == null) {
            return false;
        }
        String key = null;
        for (String line : request.split("\r\n")) {
            int colon = line.indexOf(':');
            if (colon > 0 && line.substring(0, colon).trim().equalsIgnoreCase("Sec-WebSocket-Key")) {
                key = line.substring(colon + 1).trim();
            }
        }
        if (key == null) {
            out.write(("HTTP/1.1 400 Bad Request\r\nContent-Length: 46\r\nConnection: close\r\n\r\n"
                    + "this port speaks WebSocket, not HTTP; try ws://host:port/")
                    .getBytes(StandardCharsets.UTF_8));
            out.flush();
            return false;
        }
        out.write(("HTTP/1.1 101 Switching Protocols\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Accept: " + accept(key) + "\r\n\r\n")
                .getBytes(StandardCharsets.US_ASCII));
        out.flush();
        return true;
    }

    /**
     * The value a client checks: SHA-1 of its key and the magic string, base64 encoded.
     *
     * <p>Public because a client that wants to verify a server - a test, or a tool - should not have
     * to reimplement it, and because getting it wrong is the one handshake failure a browser reports
     * as "the server does not speak WebSocket".
     */
    public static String accept(String key) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha1.digest((key + GUID).getBytes(StandardCharsets.US_ASCII));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            // Every JVM has SHA-1; a missing one is not something this class can do anything about.
            throw new IllegalStateException("SHA-1 is unavailable", e);
        }
    }

    /** Reads up to the blank line that ends an HTTP header block, or null at end of stream. */
    private static String readHeaderBlock(InputStream in) throws IOException {
        StringBuilder header = new StringBuilder();
        int matched = 0;
        while (header.length() < 16384) {
            int b = in.read();
            if (b < 0) {
                return null;
            }
            header.append((char) b);
            // The blank line is CR LF CR LF, so the second CR continues the sequence rather than
            // starting a new one - the first version of this set it back to 1 and no handshake ever
            // completed.
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

    /** One upgraded connection: bytes in, bytes out, frames underneath. */
    public static final class Connection implements AutoCloseable {

        private final Socket socket;
        private final InputStream in;
        private final OutputStream out;
        private final InputStream payloads = new PayloadInputStream();
        private final OutputStream frames = new FrameOutputStream();
        private volatile boolean closed;

        Connection(Socket socket, InputStream in, OutputStream out) {
            this.socket = socket;
            this.in = in;
            this.out = out;
        }

        public String peer() {
            return String.valueOf(socket.getRemoteSocketAddress());
        }

        /** The bytes the client sent, with the framing taken off. */
        public InputStream in() {
            return payloads;
        }

        /** The bytes to send; a flush turns them into one binary frame. */
        public OutputStream out() {
            return frames;
        }

        /** A read timeout for the underlying socket, in milliseconds; 0 for none. */
        public void readTimeout(int millis) throws IOException {
            socket.setSoTimeout(millis);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            try {
                socket.close();
            } catch (IOException e) {
                JulLog.debug("closing a websocket", e);
            }
        }

        /** Reads whole frames and hands their payloads to the stream above, control frames aside. */
        private final class PayloadInputStream extends InputStream {

            private byte[] payload = new byte[0];
            private int at;

            @Override
            public int read() throws IOException {
                if (!more()) {
                    return -1;
                }
                return payload[at++] & 0xFF;
            }

            @Override
            public int read(byte[] target, int offset, int length) throws IOException {
                if (length == 0) {
                    return 0;
                }
                if (!more()) {
                    return -1;
                }
                int count = Math.min(length, payload.length - at);
                System.arraycopy(payload, at, target, offset, count);
                at += count;
                return count;
            }

            private boolean more() throws IOException {
                while (at >= payload.length) {
                    if (!nextFrame()) {
                        return false;
                    }
                }
                return true;
            }

            /**
             * The next data frame's payload, or false at the end of the stream.
             *
             * <p><b>A timeout is only meaningful before a frame starts.</b> The reader above sets one
             * to do its own idle work, and a timeout that arrives once a frame has begun would leave
             * the stream in the middle of it - and the reader above cannot tell that apart from "the
             * client is quiet", so the next read would be from the wrong offset. So the first byte of
             * a frame is read with the timeout the caller asked for, and everything after it is read
             * with the timeout retried: no data yet is not an error.
             */
            private boolean nextFrame() throws IOException {
                StringBuilder message = null;
                while (true) {
                    int first = in.read();
                    if (first < 0) {
                        return false;
                    }
                    int second = readInFrame();
                    if (second < 0) {
                        return false;
                    }
                    int opcode = first & 0x0F;
                    boolean last = (first & 0x80) != 0;
                    boolean masked = (second & 0x80) != 0;
                    long length = second & 0x7F;
                    if (length == 126) {
                        length = ((long) readInFrame() << 8) | readInFrame();
                    } else if (length == 127) {
                        length = 0;
                        for (int i = 0; i < 8; i++) {
                            length = (length << 8) | readInFrame();
                        }
                    }
                    if (length < 0 || length > MAX_MESSAGE_BYTES) {
                        throw new IOException("a websocket frame of " + length + " bytes is not one"
                                + " this server will read (at most " + MAX_MESSAGE_BYTES + ")");
                    }
                    if (!masked) {
                        // The protocol requires it of a client, and a server that accepted it would
                        // be open to a cache-poisoning attack that masking exists to prevent.
                        throw new IOException("an unmasked frame from a client");
                    }
                    byte[] key = new byte[4];
                    readFully(key);
                    byte[] data = new byte[(int) length];
                    readFully(data);
                    for (int i = 0; i < data.length; i++) {
                        data[i] ^= key[i & 3];
                    }
                    switch (opcode) {
                        case OP_PING -> pong(data);
                        case OP_PONG -> {
                            // Nothing to do: the server does not keep time on its clients.
                        }
                        case OP_CLOSE -> {
                            send(OP_CLOSE, new byte[0]);
                            return false;
                        }
                        case OP_BINARY, OP_TEXT -> message = new StringBuilder();
                        case OP_CONTINUATION -> {
                            if (message == null) {
                                throw new IOException("a continuation frame with nothing to continue");
                            }
                        }
                        default -> throw new IOException("unknown websocket opcode " + opcode);
                    }
                    if (opcode == OP_BINARY || opcode == OP_TEXT || opcode == OP_CONTINUATION) {
                        payload = data;
                        at = 0;
                        if (last) {
                            return payload.length > 0 || true;   // even an empty message is a message
                        }
                        // Fragmented: collect the pieces before handing anything up. RFB does not
                        // care where a message ends, but a reader waiting for bytes must not be given
                        // a half-message and then a stall.
                        byte[] rest = readRest();
                        byte[] whole = new byte[data.length + rest.length];
                        System.arraycopy(data, 0, whole, 0, data.length);
                        System.arraycopy(rest, 0, whole, data.length, rest.length);
                        payload = whole;
                        at = 0;
                        return true;
                    }
                }
            }

            /** The continuation frames that finish the message a first frame began. */
            private byte[] readRest() throws IOException {
                java.io.ByteArrayOutputStream collected = new java.io.ByteArrayOutputStream();
                while (true) {
                    int first = in.read();
                    int second = in.read();
                    if (first < 0 || second < 0) {
                        throw new EOFException("the client went away mid-message");
                    }
                    int opcode = first & 0x0F;
                    if (opcode != OP_CONTINUATION) {
                        throw new IOException("opcode " + opcode + " inside a fragmented message");
                    }
                    long length = second & 0x7F;
                    if (length == 126) {
                        length = ((long) readInFrame() << 8) | readInFrame();
                    } else if (length == 127) {
                        length = 0;
                        for (int i = 0; i < 8; i++) {
                            length = (length << 8) | readInFrame();
                        }
                    }
                    if (length < 0 || length > MAX_MESSAGE_BYTES) {
                        throw new IOException("a websocket frame of " + length + " bytes is too long");
                    }
                    byte[] key = new byte[4];
                    readFully(key);
                    byte[] data = new byte[(int) length];
                    readFully(data);
                    for (int i = 0; i < data.length; i++) {
                        data[i] ^= key[i & 3];
                    }
                    collected.write(data);
                    if ((first & 0x80) != 0) {
                        return collected.toByteArray();
                    }
                }
            }

            private void readFully(byte[] target) throws IOException {
                int read = 0;
                while (read < target.length) {
                    int count;
                    try {
                        count = in.read(target, read, target.length - read);
                    } catch (java.net.SocketTimeoutException late) {
                        continue;                        // no data yet is not an error mid-frame
                    }
                    if (count < 0) {
                        throw new EOFException("the client went away mid-frame");
                    }
                    read += count;
                }
            }

            /** One byte of a frame that has already started: a timeout is not an error here. */
            private int readInFrame() throws IOException {
                while (true) {
                    try {
                        return in.read();
                    } catch (java.net.SocketTimeoutException late) {
                        continue;
                    }
                }
            }

            private void pong(byte[] data) throws IOException {
                send(OP_PONG, data);
            }
        }

        /** Collects what the layer above writes and sends it as one binary frame per flush. */
        private final class FrameOutputStream extends OutputStream {

            private final java.io.ByteArrayOutputStream buffered = new java.io.ByteArrayOutputStream();

            @Override
            public void write(int b) {
                buffered.write(b);
            }

            @Override
            public void write(byte[] data, int offset, int length) {
                buffered.write(data, offset, length);
            }

            @Override
            public void flush() throws IOException {
                if (buffered.size() == 0) {
                    return;
                }
                byte[] message = buffered.toByteArray();
                buffered.reset();
                send(OP_BINARY, message);
            }
        }

        /** One frame, unmasked - a server must not mask - with the shortest length encoding. */
        private void send(int opcode, byte[] data) throws IOException {
            synchronized (out) {
                out.write(0x80 | opcode);
                if (data.length < 126) {
                    out.write(data.length);
                } else if (data.length <= 0xFFFF) {
                    out.write(126);
                    out.write((data.length >> 8) & 0xFF);
                    out.write(data.length & 0xFF);
                } else {
                    out.write(127);
                    for (int i = 7; i >= 0; i--) {
                        out.write((int) (((long) data.length >> (8 * i)) & 0xFF));
                    }
                }
                out.write(data);
                out.flush();
            }
        }
    }
}
