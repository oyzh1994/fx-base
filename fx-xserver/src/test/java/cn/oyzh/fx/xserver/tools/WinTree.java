package cn.oyzh.fx.xserver.tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Bare-protocol client that prints the server's window tree: geometry, attributes and titles.
 *
 * <p>Usage: {@code WinTree host port}. Part of the verification kit ({@code docs/backlog.md} §1) and
 * the only way to see a client's windows from outside: in the default one-window-per-client mode a
 * fresh connection gets a screen of its own, so {@code xwininfo} and {@code xwd} see an empty root -
 * run the server with {@code --single-window} when a probe has to see the client's windows.
 *
 * <p>What it answers that the request log cannot: whether a window is still mapped after a click,
 * where it is, whether it is override-redirect, and what it calls itself. The {@code transient-for}
 * column is how a dialog is told from a toplevel.
 */
public class WinTree {

    static DataInputStream in;
    static DataOutputStream out;

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket(args[0], Integer.parseInt(args[1]))) {
            socket.setSoTimeout(5000);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            int root = handshake();
            int wmName = internAtom("WM_NAME");
            int netWmName = internAtom("_NET_WM_NAME");
            int wmClass = internAtom("WM_CLASS");
            int wmTransient = internAtom("WM_TRANSIENT_FOR");
            System.out.println("root 0x" + Integer.toHexString(root)
                    + " atoms WM_NAME=0x" + Integer.toHexString(wmName)
                    + " _NET_WM_NAME=0x" + Integer.toHexString(netWmName)
                    + " WM_CLASS=0x" + Integer.toHexString(wmClass));
            walk(root, 0, wmName, netWmName, wmClass, wmTransient);
        }
    }

    static void walk(int window, int depth, int wmName, int netWmName, int wmClass,
                     int wmTransient) throws Exception {
        int[] geometry = geometry(window);
        int[] attributes = attributes(window);
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            line.append("  ");
        }
        line.append(String.format("0x%08x %dx%d%+d%+d depth=%d class=%d map=%d override=%d border=%d",
                window, geometry[2], geometry[3], geometry[0], geometry[1], geometry[4],
                attributes[0], attributes[1], attributes[2], geometry[5]));
        String title = text(window, netWmName);
        if (title == null) {
            title = text(window, wmName);
        }
        if (title != null) {
            line.append(" name=\"").append(title).append('"');
        }
        String klass = text(window, wmClass);
        if (klass != null) {
            line.append(" class=").append(klass.replace('\n', '|'));
        }
        Integer transientFor = card32(window, wmTransient);
        if (transientFor != null) {
            line.append(" transient-for=0x").append(Integer.toHexString(transientFor));
        }
        System.out.println(line);
        for (int child : children(window)) {
            walk(child, depth + 1, wmName, netWmName, wmClass, wmTransient);
        }
    }

    static int handshake() throws Exception {
        byte[] setup = new byte[12];
        setup[0] = 'l';
        setup[2] = 11;
        out.write(setup);
        out.flush();
        byte[] header = new byte[8];
        in.readFully(header);
        if ((header[0] & 0xFF) != 1) {
            throw new IllegalStateException("setup failed");
        }
        int length = (header[6] & 0xFF) | ((header[7] & 0xFF) << 8);
        byte[] data = new byte[length * 4];
        in.readFully(data);
        int vendorLength = shortAt(data, 16);
        int formats = data[21] & 0xFF;
        // The screen section starts after the vendor string and the format list; the root window is
        // its first field. Off by four here reads garbage (or zero) - the kind of thing that looks
        // like a server bug for an afternoon.
        int at = 32 + pad4(vendorLength) + formats * 8;
        return intAt(data, at);
    }

    static int pad4(int value) {
        return (value + 3) & ~3;
    }

    static int internAtom(String name) throws Exception {
        byte[] request = new byte[8 + pad4(name.length())];
        request[0] = 16;
        request[1] = 1;
        request[2] = (byte) (request.length / 4);
        putShort(request, 4, (short) name.length());
        for (int i = 0; i < name.length(); i++) {
            request[8 + i] = (byte) name.charAt(i);
        }
        out.write(request);
        out.flush();
        byte[] reply = new byte[32];
        in.readFully(reply);
        return intAt(reply, 8);
    }

    static int[] children(int window) throws Exception {
        // QueryTree is an eight byte request: sending twelve with length 2 desynchronises the stream
        // and every reply after it is read from the wrong offset.
        byte[] request = new byte[8];
        request[0] = 15;
        request[2] = 2;
        putInt(request, 4, window);
        out.write(request);
        out.flush();
        byte[] header = new byte[32];
        in.readFully(header);
        int count = shortAt(header, 16);
        int[] children = new int[count];
        for (int i = 0; i < count; i++) {
            byte[] child = new byte[4];
            in.readFully(child);
            children[i] = intAt(child, 0);
        }
        return children;
    }

    static int[] geometry(int drawable) throws Exception {
        byte[] request = new byte[8];
        request[0] = 14;
        request[2] = 2;
        putInt(request, 4, drawable);
        out.write(request);
        out.flush();
        byte[] reply = new byte[32];
        in.readFully(reply);
        return new int[]{shortAt(reply, 12), shortAt(reply, 14), shortAt(reply, 16),
                shortAt(reply, 18), reply[1] & 0xFF, shortAt(reply, 20)};
    }

    /** {@code {visual, mapState, overrideRedirect}} - where GetWindowAttributes puts those fields. */
    static int[] attributes(int window) throws Exception {
        byte[] request = new byte[8];
        request[0] = 3;
        request[2] = 2;
        putInt(request, 4, window);
        out.write(request);
        out.flush();
        byte[] reply = new byte[44];
        in.readFully(reply);
        return new int[]{reply[12], reply[26], reply[27]};
    }

    /** The property value as text, or null when it is not set. */
    static String text(int window, int property) throws Exception {
        byte[] data = property(window, property);
        return data == null ? null : new String(data, java.nio.charset.StandardCharsets.UTF_8);
    }

    static Integer card32(int window, int property) throws Exception {
        byte[] data = property(window, property);
        if (data == null || data.length < 4) {
            return null;
        }
        return intAt(data, 0);
    }

    static byte[] property(int window, int property) throws Exception {
        byte[] request = new byte[24];
        request[0] = 20;                            // GetProperty
        request[2] = 6;
        putInt(request, 4, window);
        putInt(request, 8, property);
        putInt(request, 16, 0);                     // long offset, in 4 byte units
        putInt(request, 20, 1024);                  // long length, also in 4 byte units
        // The two fields are adjacent and both count four byte units, and putting the length in the
        // offset's place is a mistake that reads as a working tool: every read is answered
        // `BadValue` (an offset 4096 bytes into a short property), the tool prints no title, and
        // nothing says why. It is what this one did until a server log was read rather than the
        // output.
        out.write(request);
        out.flush();
        byte[] header = new byte[32];
        in.readFully(header);
        int format = header[1] & 0xFF;
        int count = intAt(header, 16);
        int bytes = count * format / 8;
        byte[] data = new byte[pad4(bytes)];
        in.readFully(data);
        return bytes == 0 ? null : java.util.Arrays.copyOf(data, bytes);
    }

    static int intAt(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16) | ((data[offset + 3] & 0xFF) << 24);
    }

    static int shortAt(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    static void putShort(byte[] data, int offset, short value) {
        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >> 8);
    }

    static void putInt(byte[] data, int offset, int value) {
        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >> 8);
        data[offset + 2] = (byte) (value >> 16);
        data[offset + 3] = (byte) (value >> 24);
    }
}
