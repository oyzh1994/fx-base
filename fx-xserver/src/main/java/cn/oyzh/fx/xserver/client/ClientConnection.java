package cn.oyzh.fx.xserver.client;

import com.github.moaxcp.x11.protocol.X11BigEndianInputStream;
import com.github.moaxcp.x11.protocol.X11Input;
import com.github.moaxcp.x11.protocol.X11LittleEndianInputStream;
import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.xproto.SetupRequest;
import cn.oyzh.fx.xserver.ScreenSession;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.net.ClientOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * One connected client: its socket, its byte order, its resource id range and its request loop.
 *
 * <p>Each connection runs on its own thread. Requests are decoded by the generated protocol
 * classes and executed while holding the server lock, so the resource model never needs finer
 * locking than that.
 */
public final class ClientConnection implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientConnection.class);

    /** Client index occupies the top bits of a resource id, giving 256 clients. */
    public static final int CLIENT_BITS = 8;
    public static final int CLIENT_SHIFT = 29 - CLIENT_BITS;
    public static final int MAX_CLIENTS = 1 << CLIENT_BITS;
    /** Bits available for a single client's resources. This is the mask reported in the setup. */
    public static final int RESOURCE_MASK = (1 << CLIENT_SHIFT) - 1;

    private static final int MAX_REQUEST_UNITS = 0xFFFF;
    private static final int MAX_BIG_REQUEST_UNITS = (1 << 22) - 1;

    private final XServer server;
    private final Socket socket;
    private final SocketChannel channel;
    private final String peer;
    private final int index;

    /** Inclusive range of resource ids this client may use. */
    private final int resourceIdBase;

    private X11Input in;
    /**
     * The byte stream the request bytes actually arrive on, kept so a request body can be read in
     * one call rather than a byte at a time.
     *
     * <p>The generated {@link X11Input} has no bulk read: its {@code readByte()} reads a single
     * byte from this stream, and the request loop used to copy a body through it one byte per call.
     * For a forwarded application that is fatal - a JavaFX or GTK client uploads every frame with
     * {@code PutImage}, so a 1300x800 frame meant four million {@code read()} calls (measured:
     * about 16 ms per frame just to read the bytes, against 0.5 ms in bulk). Nothing here buffers
     * ahead, so reading the body straight from this stream is the same bytes the decoder would see.
     */
    private InputStream rawIn;
    private OutputStream rawOut;
    private ClientOutput output;

    private boolean bigEndian;
    private boolean bigRequestsEnabled;
    private boolean closed;

    /**
     * Whether this connection is trusted, which every connection is unless it authenticated with an
     * authorization SECURITY generated as untrusted (F7).
     *
     * <p>Untrusted is a property of the *connection*, not of a request: what such a connection may do
     * is refused at the requests that would read or change somebody else's drawable, and the
     * extensions that are hidden from it are hidden everywhere ({@code ListExtensions},
     * {@code QueryExtension}, and their opcodes).
     */
    private volatile boolean trusted = true;

    /** The authorization this connection authenticated with, or zero; what a revoke acts on. */
    private volatile int authorizationId;

    private short sequenceNumber;
    private byte currentOpcode;
    private byte currentMinorOpcode;

    /** Next id handed out on the client's behalf for server-created resources. */
    private int nextServerId;

    private int closeDownMode;

    /** Set while a client connection is being torn down, so late events are dropped. */
    private volatile boolean shuttingDown;

    /** How many requests have been handled, and the last few, kept for the disconnect log. */
    private static final int RECENT_REQUEST_COUNT = 12;
    private int requestCount;
    private int recentRequestIndex;
    private Class<?> currentRequest;
    private boolean loggedDisconnect;

    public ClientConnection(XServer server, Socket socket, int index) throws IOException {
        this(server, socket, null, index);
    }

    public ClientConnection(XServer server, SocketChannel channel, int index) throws IOException {
        this(server, null, channel, index);
    }

    /** The screen and window this connection owns, or null when every client shares the master one. */
    private ScreenSession session;

    private ClientConnection(XServer server, Socket socket, SocketChannel channel, int index)
            throws IOException {
        this.server = server;
        this.socket = socket;
        this.channel = channel;
        this.index = index;
        this.resourceIdBase = index << CLIENT_SHIFT;
        this.nextServerId = resourceIdBase | RESOURCE_MASK;
        if (socket != null) {
            this.peer = String.valueOf(socket.getRemoteSocketAddress());
            socket.setTcpNoDelay(true);
            this.rawOut = new BufferedOutputStream(socket.getOutputStream(), 64 * 1024);
            this.in = null; // set during the handshake, once the byte order is known
        } else {
            this.peer = String.valueOf(channel.getRemoteAddress());
            this.rawOut = new BufferedOutputStream(
                    java.nio.channels.Channels.newOutputStream(channel), 64 * 1024);
            this.in = null;
        }
    }

    private InputStream inputStream() throws IOException {
        if (socket != null) {
            return socket.getInputStream();
        }
        return java.nio.channels.Channels.newInputStream(channel);
    }

    public ScreenSession session() {
        return session;
    }

    public void setSession(ScreenSession session) {
        this.session = session;
    }

    public int index() {
        return index;
    }

    public String peer() {
        return peer;
    }

    public int resourceIdBase() {
        return resourceIdBase;
    }

    public int resourceIdMask() {
        return RESOURCE_MASK;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public short sequenceNumber() {
        return sequenceNumber;
    }

    public ClientOutput output() {
        return output;
    }

    public byte currentOpcode() {
        return currentOpcode;
    }

    public byte currentMinorOpcode() {
        return currentMinorOpcode;
    }

    public boolean isShuttingDown() {
        return shuttingDown;
    }

    /** True unless this connection authenticated with an untrusted authorization. */
    public boolean isTrusted() {
        return trusted;
    }

    /**
     * Marks this connection as belonging to an authorization: {@code trusted} is the level the
     * authorization was created with, and the id is what {@code SecurityRevokeAuthorization} acts on.
     */
    public void authenticatedBy(int authorizationId, boolean trusted) {
        this.authorizationId = authorizationId;
        this.trusted = trusted;
    }

    /** The authorization this connection authenticated with, or zero. */
    public int authorizationId() {
        return authorizationId;
    }

    public boolean isClosed() {
        return closed;
    }

    public int closeDownMode() {
        return closeDownMode;
    }

    public void setCloseDownMode(int closeDownMode) {
        this.closeDownMode = closeDownMode;
    }

    /** True when {@code id} falls inside this client's resource id range. */
    public boolean ownsIdRange(int id) {
        return (id & ~RESOURCE_MASK) == resourceIdBase;
    }

    /**
     * Allocates an id on the client's behalf, for resources the server creates itself such as the
     * default graphics context or the root window.
     */
    public synchronized int allocateServerId() {
        int candidate = nextServerId;
        for (int attempts = 0; attempts <= RESOURCE_MASK; attempts++) {
            if (!server.resources().contains(candidate)) {
                nextServerId = candidate == resourceIdBase ? resourceIdBase | RESOURCE_MASK
                        : candidate - 1;
                return candidate;
            }
            candidate = candidate == resourceIdBase ? resourceIdBase | RESOURCE_MASK : candidate - 1;
        }
        throw ProtocolException.badAlloc();
    }

    @Override
    public void run() {
        String reason = "the connection ended";
        try {
            handshake();
            server.registerClient(this);
            requestLoop();
            reason = closed ? "closed by the server" : "the request loop ended";
        } catch (EOFException e) {
            reason = "the client closed the connection";
        } catch (java.net.SocketException e) {
            reason = "socket error: " + e.getMessage();
        } catch (IOException e) {
            reason = "read failed: " + e;
        } catch (RuntimeException e) {
            reason = "failed handling " + currentRequestName();
            log.warn("client {} failed during {}", peer, currentRequestName(), e);
        } catch (Error e) {
            // An Error would otherwise kill the thread and drop the connection with no explanation
            // at all, which is exactly the kind of silent disconnect that is impossible to debug
            // from the outside.
            reason = "fatal error handling " + currentRequestName();
            log.error("client {} hit {} during {}", peer, e.getClass().getSimpleName(),
                    currentRequestName(), e);
        } finally {
            logDisconnect(reason);
            shutdown();
        }
    }

    /**
     * Explains why a connection ended, and what it had been doing. A client that disconnects
     * abruptly is one of the hardest things to diagnose from the server side, so the last few
     * requests it sent are kept and reported here.
     */
    private void logDisconnect(String reason) {
        if (loggedDisconnect) {
            return;
        }
        loggedDisconnect = true;
        if (requestCount == 0 && reason.startsWith("the client closed")) {
            // A connection that never sent a request is noise, not a diagnostic.
            log.debug("client {} connected and left without a request", peer);
            return;
        }
        log.info("client {} disconnected after {} requests: {}; last requests: {}", peer,
                requestCount, reason, recentRequests());
    }

    private final Class<?>[] recentRequests = new Class<?>[RECENT_REQUEST_COUNT];

    /** Names of the last few requests, oldest first, for the disconnect log. */
    private String recentRequests() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < RECENT_REQUEST_COUNT; i++) {
            Class<?> type = recentRequests[(recentRequestIndex + i) % RECENT_REQUEST_COUNT];
            if (type == null) {
                continue;
            }
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(type.getSimpleName());
        }
        return text.length() == 0 ? "none" : text.toString();
    }

    private String currentRequestName() {
        Class<?> current = currentRequest;
        return current == null ? "the request loop" : current.getSimpleName();
    }

    /** Records the request now being handled, for the disconnect log. */
    public void recordRequest(Class<?> type) {
        currentRequest = type;
        recentRequests[recentRequestIndex] = type;
        recentRequestIndex = (recentRequestIndex + 1) % RECENT_REQUEST_COUNT;
        requestCount++;
    }

    /** How many requests this client has sent, used in the disconnect log. */
    public int requestCount() {
        return requestCount;
    }

    // ------------------------------------------------------------------
    // Connection setup
    // ------------------------------------------------------------------

    private void handshake() throws IOException {
        PushbackInputStream pushback = new PushbackInputStream(
                new BufferedInputStream(inputStream(), 64 * 1024), 1);
        int firstByte = pushback.read();
        if (firstByte < 0) {
            throw new EOFException("client closed before sending a setup request");
        }
        pushback.unread(firstByte);
        // 'B' selects big endian; anything else means little endian ('l' from Xlib).
        bigEndian = firstByte == 'B';
        rawIn = pushback;
        in = bigEndian ? new X11BigEndianInputStream(pushback)
                : new X11LittleEndianInputStream(pushback);
        // The RECORD listener is attached *after* the setup reply, in ConnectionSetup.sendSetup:
        // a reference records that reply as the client-started element and not as an ordinary
        // server message, so a recorder must not see it go past here (F2, measured).
        output = new ClientOutput(rawOut, bigEndian);

        SetupRequest request = SetupRequest.readSetupRequest(in);
        server.connectionSetup().accept(this, request);
    }

    // ------------------------------------------------------------------
    // Request loop
    // ------------------------------------------------------------------

    private void requestLoop() throws IOException {
        while (!closed && !shuttingDown) {
            int opcode = in.readByte() & 0xFF;
            int data = in.readByte() & 0xFF;
            int lengthUnits = in.readCard16() & 0xFFFF;

            int headerBytes = 4;
            if (lengthUnits == 0) {
                if (!bigRequestsEnabled) {
                    // A zero length is only meaningful once BIG-REQUESTS has been enabled.
                    sequenceNumber++;
                    sendError(ProtocolException.LENGTH, 0);
                    return;
                }
                // BIG-REQUESTS: the real length follows and counts the whole request including
                // both 4 byte header words.
                long bigLength = Integer.toUnsignedLong(in.readInt32());
                if (bigLength > MAX_BIG_REQUEST_UNITS) {
                    // **Too long, and the connection survives it.** A reference answers BadLength
                    // and then *discards* the whole request the client has already committed to
                    // sending - measured on a tap while XTS's `Xproto` set ran: a client sends a
                    // big request whose length is `bigreq_size + 1` and then all sixteen megabytes
                    // of it, gets one BadLength, and the next request on that connection is
                    // answered normally. Closing instead (which is what this did) leaves the client
                    // writing into a closed socket: XTS reported the case as `UNRESOLVED` with
                    // `SIGPIPE`, and every later test case on that connection failed with it. See
                    // A37 in docs/backlog.md.
                    sequenceNumber++;
                    sendError(ProtocolException.LENGTH, 0);
                    discard(bigLength * 4 - 8);
                    continue;
                }
                lengthUnits = (int) bigLength;
                headerBytes = 8;
            }

            int bodyBytes = lengthUnits * 4 - headerBytes;
            if (bodyBytes < 0) {
                sequenceNumber++;
                sendError(ProtocolException.LENGTH, 0);
                continue;
            }
            // Read the whole body in one call. Going through `in.readByte()` per byte is the same
            // bytes but millions of calls for an image upload - see `rawIn`.
            byte[] body = new byte[bodyBytes];
            readFully(body, 0, bodyBytes);

            sequenceNumber++;
            currentOpcode = (byte) opcode;
            currentMinorOpcode = (byte) data;

            // The request as it arrived, before anything decodes it: a recorder replays these bytes.
            server.recordClientRequest(this,
                    requestBytes(opcode, data, lengthUnits, headerBytes, body), sequenceNumber);

            dispatch(opcode, data, lengthUnits, body);

            // A reply still in the buffer is a client waiting for it, and a client waiting for a
            // reply sends nothing more. That alone would only delay things; what makes it fatal is
            // that the bytes still readable here need not be a whole request. Xlib flushes its
            // sixteen kilobyte buffer wherever it fills, so a small round trip followed by a large
            // PutImage reaches the server as the reply-worthy request and the head of the image.
            // Waiting for the rest of that image means waiting for a client that is itself waiting
            // for this reply - both sides stop, for good. Flushing while the loop can still make
            // progress is what keeps that from happening.
            if (output.hasPending()) {
                output.flush();
            }
        }
    }

    private void dispatch(int opcode, int data, int lengthUnits, byte[] body) {
        // Rebuild the request without its opcode byte, which is the shape the generated decoders
        // expect: they read the data byte, the length and then the payload.
        byte[] parserBytes = new byte[3 + body.length];
        parserBytes[0] = (byte) data;
        // The length field is sixteen bits wide, so a BIG-REQUESTS request - one larger than 64K -
        // cannot be described by it at all. What is written here is therefore a placeholder, and
        // the true length travels beside it: a decoder that sizes a trailing list from this field
        // alone would read a fraction of what the client sent.
        putCard16(parserBytes, 1, Math.min(lengthUnits, MAX_REQUEST_UNITS));
        System.arraycopy(body, 0, parserBytes, 3, body.length);

        ByteArrayInputStream bytes = new ByteArrayInputStream(parserBytes);
        X11Input requestIn = bigEndian ? new X11BigEndianInputStream(bytes)
                : new X11LittleEndianInputStream(bytes);

        // The size handed over is the request's own minus its opcode byte, because that is the shape
        // the decoder is given. A BIG-REQUESTS request is four bytes longer than that description
        // suggests - its header is eight bytes rather than four - and the difference has to be taken
        // out here, or a decoder sizing a trailing list from it overshoots by exactly those bytes.
        server.executeRequest(this, (byte) opcode, (byte) data, requestIn,
                parserBytes.length + 1, parserBytes);
    }

    /**
     * Reads exactly {@code length} bytes into {@code target}, the way the request loop needs them.
     *
     * <p>A socket read can return fewer bytes than asked for, so this loops; a read of {@code -1}
     * before the body is complete means the client went away mid-request, which is an
     * {@link EOFException} and not a request to decode.
     */
    private void readFully(byte[] target, int offset, int length) throws IOException {
        int read = 0;
        while (read < length) {
            int count = rawIn.read(target, offset + read, length - read);
            if (count < 0) {
                throw new EOFException("client closed with " + (length - read)
                        + " request byte(s) still to come");
            }
            read += count;
        }
    }

    /**
     * Reads and throws away {@code length} bytes of a request that was refused for its size.
     *
     * <p>A client that has sent a request header has usually already written the whole request, so
     * the bytes are on their way whether or not this server wants them; skipping them is what keeps
     * the <em>next</em> request's header where the client put it. Read in chunks: the request that
     * triggers this is, by definition, as large as a client can ask for - sixteen megabytes for the
     * biggest BIG-REQUESTS request - and this allocation is not worth making.
     */
    private void discard(long length) throws IOException {
        byte[] scratch = new byte[Math.min(1 << 16, (int) Math.max(1, Math.min(length, 1 << 16)))];
        for (long left = length; left > 0; ) {
            int count = rawIn.read(scratch, 0, (int) Math.min(left, scratch.length));
            if (count < 0) {
                throw new EOFException("client closed with " + left
                        + " request byte(s) still to discard");
            }
            left -= count;
        }
    }

    /** One request as it arrived, header and body: what a recorder is given. */
    private byte[] requestBytes(int opcode, int data, int lengthUnits, int headerBytes, byte[] body) {
        byte[] bytes = new byte[headerBytes + body.length];
        bytes[0] = (byte) opcode;
        bytes[1] = (byte) data;
        if (headerBytes == 4) {
            putCard16(bytes, 2, lengthUnits);
        } else {
            // A BIG-REQUESTS header: the sixteen bit length is zero and the real one follows it.
            putCard32(bytes, 4, lengthUnits);
        }
        System.arraycopy(body, 0, bytes, headerBytes, body.length);
        return bytes;
    }

    private void putCard16(byte[] bytes, int offset, int value) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 8);
            bytes[offset + 1] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
        }
    }

    private void putCard32(byte[] bytes, int offset, int value) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 24);
            bytes[offset + 1] = (byte) (value >> 16);
            bytes[offset + 2] = (byte) (value >> 8);
            bytes[offset + 3] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
            bytes[offset + 2] = (byte) (value >> 16);
            bytes[offset + 3] = (byte) (value >> 24);
        }
    }

    /** Decodes a request with a generated plugin, returning null if the opcode is not supported. */
    public com.github.moaxcp.x11.protocol.XRequest decode(XProtocolPlugin plugin, byte majorOpcode,
                                                          byte minorOpcode, X11Input in) {
        try {
            return plugin.readRequest(majorOpcode, minorOpcode, in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ------------------------------------------------------------------
    // Outbound protocol messages
    // ------------------------------------------------------------------

    /** Sends a reply to the request currently being processed. */
    public void sendReply(com.github.moaxcp.x11.protocol.XReply reply) {
        if (shuttingDown) {
            return;
        }
        try {
            output.writeReply(reply);
        } catch (IOException e) {
            log.debug("failed to send reply to {}: {}", peer, e.getMessage());
            close();
            throw new RuntimeException(e);
        }
    }

    public void sendError(byte errorCode, int badValue) {
        if (shuttingDown) {
            return;
        }
        try {
            output.writeError(errorCode, badValue, sequenceNumber, currentOpcode, currentMinorOpcode);
        } catch (IOException e) {
            log.debug("failed to send error to {}: {}", peer, e.getMessage());
            close();
        }
    }

    /**
     * Asked for by whoever has just written something and wants it out.
     *
     * <p>It no longer does anything: the connection's writer thread flushes after every message, so
     * there is nothing to push. Kept because a caller saying "this one matters" is worth leaving in
     * the code, and because it is where the flush used to be.
     */
    public void flush() {
        output.flush();
    }

    public void enableBigRequests() {
        this.bigRequestsEnabled = true;
    }

    public boolean isBigRequestsEnabled() {
        return bigRequestsEnabled;
    }

    /** Largest request the client may send, in 4 byte units. */
    public int maximumRequestUnits() {
        return bigRequestsEnabled ? MAX_BIG_REQUEST_UNITS : MAX_REQUEST_UNITS;
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        // The writer thread first: it is the only thing that waits for this client, and stopping it
        // before the socket is what keeps it from sitting in a write that will never finish.
        output.close();
        try {
            if (socket != null) {
                socket.close();
            } else if (channel != null) {
                channel.close();
            }
        } catch (IOException ignored) {
            // closing is best effort
        }
    }

    private void shutdown() {
        shuttingDown = true;
        server.removeClient(this);
        close();
    }

    @Override
    public String toString() {
        return "client[" + index + " " + peer + "]";
    }
}
