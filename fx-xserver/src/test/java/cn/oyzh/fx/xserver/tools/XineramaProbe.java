package cn.oyzh.fx.xserver.tools;

/**
 * Asks a server every question Xinerama has, and prints the answers.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1), and the harness for item B3. The
 * extension is a stub here that answers the three requests a toolkit's startup asks - the version,
 * whether the display is several monitors acting as one, and the list of heads - and the remaining
 * three ({@code GetState}, {@code GetScreenCount}, {@code GetScreenSize}) are for a client that
 * already knows it is on a Xinerama display.
 *
 * <pre>
 *   XineramaProbe &lt;host&gt; &lt;port&gt;
 * </pre>
 *
 * <p>Each answer is printed as the numbers the server sent, so two servers can be compared field by
 * field rather than by "it replied".
 */
public final class XineramaProbe {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: XineramaProbe <host> <port>");
            return;
        }
        try (Wire wire = Wire.connect(args[0], Integer.parseInt(args[1]))) {
            int opcode = queryExtension(wire, "XINERAMA");
            System.out.println("XINERAMA major opcode = " + opcode);
            if (opcode == 0) {
                return;
            }
            version(wire, opcode);
            isActive(wire, opcode);
            getState(wire, opcode);
            getScreenCount(wire, opcode);
            getScreenSize(wire, opcode);
            queryScreens(wire, opcode);
        }
    }

    /** {@code QueryVersion}: a request with a length of two units and the client's version in it. */
    private static void version(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = 0;
        Wire.writeShort(request, 2, 2);
        request[4] = 1;
        request[5] = 1;
        wire.send(request);
        byte[] reply = reply(wire, "QueryVersion");
        if (reply != null) {
            System.out.printf("  QueryVersion: %d.%d%n", Wire.readShort(reply, 8),
                    Wire.readShort(reply, 10));
        }
    }

    private static void isActive(Wire wire, int opcode) throws Exception {
        byte[] reply = simple(wire, opcode, 4, "IsActive");
        if (reply != null) {
            System.out.println("  IsActive: " + (skip(reply) ? "<skip>" : reply[8] == 0
                    ? "no - one screen" : "yes - several heads"));
        }
    }

    private static void getState(Wire wire, int opcode) throws Exception {
        byte[] reply = aboutRoot(wire, opcode, 1, "GetState");
        if (reply != null && !skip(reply)) {
            System.out.printf("  GetState: %d (%s)%n", reply[8] & 0xFF,
                    reply[8] == 0 ? "active" : "inactive");
        }
    }

    private static void getScreenCount(Wire wire, int opcode) throws Exception {
        byte[] reply = aboutRoot(wire, opcode, 2, "GetScreenCount");
        if (reply != null && !skip(reply)) {
            System.out.println("  GetScreenCount: " + (reply[8] & 0xFF));
        }
    }

    private static void getScreenSize(Wire wire, int opcode) throws Exception {
        byte[] reply = aboutRoot(wire, opcode, 3, "GetScreenSize");
        if (reply != null && !skip(reply)) {
            System.out.printf("  GetScreenSize: %dx%d%n", Wire.readInt(reply, 8),
                    Wire.readInt(reply, 12));
        }
    }

    private static void queryScreens(Wire wire, int opcode) throws Exception {
        byte[] reply = simple(wire, opcode, 5, "QueryScreens");
        if (reply == null || skip(reply)) {
            return;
        }
        int count = Wire.readInt(reply, 8);
        StringBuilder heads = new StringBuilder();
        for (int i = 0; i < Math.min(count, 8); i++) {
            heads.append(String.format(" [%d,%d %dx%d]", Wire.readShort(reply, 32 + i * 8),
                    Wire.readShort(reply, 34 + i * 8), Wire.readShort(reply, 36 + i * 8),
                    Wire.readShort(reply, 38 + i * 8)));
        }
        System.out.println("  QueryScreens: " + count + " head(s)" + heads);
    }

    /**
     * These three name the screen they are asking about - a window on it, in practice the root - so
     * their requests are eight bytes rather than four. Sending four gets {@code BadLength} from a
     * reference server, which is what the first version of this probe measured and read as "the
     * extension does not implement them".
     */
    private static byte[] aboutRoot(Wire wire, int opcode, int minor, String what) throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, wire.root());
        wire.send(request);
        return reply(wire, what);
    }

    /** A request with no arguments but a length in units, and its reply. */
    private static byte[] simple(Wire wire, int opcode, int minor, String what) throws Exception {
        byte[] request = new byte[4];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        Wire.writeShort(request, 2, 1);
        wire.send(request);
        return reply(wire, what);
    }

    /** True when {@code reply} is the marker a refused request leaves behind. */
    private static boolean skip(byte[] reply) {
        return reply.length == 0;
    }

    private static byte[] reply(Wire wire, String what) throws Exception {
        wire.readTimeoutMillis(1000);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                System.out.println("  " + what + ": no answer");
                wire.readTimeoutMillis(3000);
                return new byte[0];
            }
            if ((message[0] & 0x7F) == 1) {
                byte[] whole = wire.withExtraData(message);
                wire.readTimeoutMillis(3000);
                return whole;
            }
            System.out.printf("  %s: refused, error %d for opcode %d%n", what, message[1] & 0xFF,
                    message[10] & 0xFF);
            wire.readTimeoutMillis(3000);
            return new byte[0];
        }
    }

    private static int queryExtension(Wire wire, String name) throws Exception {
        byte[] request = new byte[(8 + name.length() + 3) & ~3];
        request[0] = 98;                            // QueryExtension
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeShort(request, 4, name.length());
        for (int i = 0; i < name.length(); i++) {
            request[8 + i] = (byte) name.charAt(i);
        }
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                return 0;
            }
            if ((message[0] & 0x7F) == 1) {
                return message[8] == 0 ? 0 : message[9] & 0xFF;
            }
        }
    }

    private XineramaProbe() {
    }
}
