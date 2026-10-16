package cn.oyzh.fx.xserver.tools;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * The bare protocol a probe needs, with messages kept as they arrive.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1). It differs from
 * {@link cn.oyzh.fx.xserver.test.RawXClient} in the one way that matters to a probe:
 * {@code RawXClient} finds a reply by reading until one turns up, which is right for a test that
 * wants an answer and wrong for one that wants to see the <em>events</em> - a probe that reads its
 * reply first throws away the focus or crossing events it was counting. Here nothing is consumed
 * behind the caller's back: {@link #readMessage} hands back every thirty two byte message in the
 * order the server sent it, reply and event alike, and the caller decides what it is.
 *
 * <p>Only the fixed part of a reply is read. Every request these probes make - {@code QueryPointer},
 * {@code GetInputFocus}, {@code GrabKeyboard}, {@code QueryTree} - answers in those thirty two bytes,
 * and a request whose reply carried extra data would leave the stream out of step and has no business
 * here.
 */
final class Wire implements AutoCloseable {

    private final Socket socket;
    private final DataInputStream in;
    private final OutputStream out;
    private final int root;
    private final int idBase;
    private int nextId = 1;

    private Wire(Socket socket, DataInputStream in, OutputStream out, int root, int idBase) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.root = root;
        this.idBase = idBase;
    }

    static Wire connect(String host, int port) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 5000);
        socket.setSoTimeout(3000);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        OutputStream out = socket.getOutputStream();

        byte[] setup = new byte[12];
        setup[0] = 'l';                             // little endian
        setup[2] = 11;                              // protocol major
        out.write(setup);
        out.flush();

        byte[] header = new byte[8];
        in.readFully(header);
        if ((header[0] & 0xFF) != 1) {
            throw new IOException("the connection setup was refused");
        }
        int length = (header[6] & 0xFF) | ((header[7] & 0xFF) << 8);
        byte[] rest = new byte[length * 4];
        in.readFully(rest);

        // The same offsets RawXClient reads: id base after the release, and the first screen after
        // the vendor string and the pixmap formats.
        int idBase = readInt(rest, 4);
        int vendor = readShort(rest, 16);
        int formats = rest[21] & 0xFF;
        int screensAt = 32 + ((vendor + 3) & ~3) + formats * 8;
        return new Wire(socket, in, out, readInt(rest, screensAt), idBase);
    }

    int root() {
        return root;
    }

    int allocateId() {
        return idBase + nextId++;
    }

    /** How long a read waits for a message before answering null. */
    void readTimeoutMillis(int millis) throws IOException {
        socket.setSoTimeout(millis);
    }

    void send(byte[] request) throws IOException {
        out.write(request);
        out.flush();
    }

    /**
     * One thirty two byte message, or null when the server was quiet for the read timeout.
     *
     * <p>An error is a message like any other here rather than an exception: a probe that diffs two
     * servers wants to see that one of them refused a request, not to die on it.
     */
    byte[] readMessage() throws IOException {
        byte[] message = new byte[32];
        try {
            in.readFully(message);
        } catch (java.net.SocketTimeoutException quiet) {
            return null;
        }
        return message;
    }

    /** A reply with a {@code length} field larger than its fixed part carries data after it. */
    static boolean hasExtraData(byte[] message) {
        return readInt(message, 4) != 0;
    }

    /**
     * Reads the data a reply's {@code length} field says follows it, and gives back the whole reply.
     *
     * <p>{@link #readMessage} takes thirty two bytes and no more, which is right for every request
     * whose answer fits in the fixed part - and wrong for the ones that carry a list, where the
     * unread data stays in the stream and every message after it is read from the wrong offset. A
     * caller that expects a list says so here.
     */
    byte[] withExtraData(byte[] message) throws IOException {
        int extra = readInt(message, 4) * 4;
        if (extra <= 0) {
            return message;
        }
        byte[] whole = new byte[32 + extra];
        System.arraycopy(message, 0, whole, 0, 32);
        in.readFully(whole, 32, extra);
        return whole;
    }

    int internAtom(String name) throws IOException {
        byte[] request = new byte[(8 + name.length() + 3) & ~3];
        request[0] = 16;                            // InternAtom
        writeShort(request, 2, request.length / 4);
        writeShort(request, 4, name.length());
        for (int i = 0; i < name.length(); i++) {
            request[8 + i] = (byte) name.charAt(i);
        }
        send(request);
        while (true) {
            byte[] message = readMessage();
            if (message == null) {
                throw new IOException("no answer to InternAtom " + name);
            }
            if ((message[0] & 0xFF) == 1) {
                return readInt(message, 8);
            }
        }
    }

    /** What an atom is called, or a hex id when the server does not know it. */
    String atomName(int atom) throws IOException {
        byte[] request = new byte[8];
        request[0] = 17;                            // GetAtomName
        writeShort(request, 2, 2);
        writeInt(request, 4, atom);
        send(request);
        while (true) {
            byte[] message = readMessage();
            if (message == null) {
                return "0x" + Integer.toHexString(atom);
            }
            if ((message[0] & 0x7F) == 1) {
                byte[] whole = withExtraData(message);
                int length = readShort(whole, 8);
                return length == 0 ? "0x" + Integer.toHexString(atom)
                        : new String(whole, 32, length, java.nio.charset.StandardCharsets.ISO_8859_1);
            }
        }
    }

    static int readInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8)
                | ((bytes[offset + 2] & 0xFF) << 16) | ((bytes[offset + 3] & 0xFF) << 24);
    }

    static int readShort(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);
    }

    static void writeInt(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) value;
        bytes[offset + 1] = (byte) (value >> 8);
        bytes[offset + 2] = (byte) (value >> 16);
        bytes[offset + 3] = (byte) (value >> 24);
    }

    static void writeShort(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) value;
        bytes[offset + 1] = (byte) (value >> 8);
    }

    /** A request's length in four byte units, which every hand written request has to carry. */
    static int units(int byteLength) {
        return byteLength / 4;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
