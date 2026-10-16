package cn.oyzh.fx.xserver.tools;

/**
 * Prints what each window says is under a pointer position, walking from the root down.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1), and the tool for a question that is
 * asked with {@code QueryPointer} and answered wrongly in a way nothing else shows: the protocol says
 * a reply's {@code child} field is the child of the <em>queried</em> window that is under the
 * pointer, not the deepest window there is. A server that answers with the grandchild hands a client
 * a window id it never asked about, and the client goes on to query that window instead - a
 * difference that is invisible until something walks the chain, which is what this does.
 *
 * <pre>
 *   PointProbe &lt;host&gt; &lt;port&gt; [x y]     # defaults to the pointer's current position
 * </pre>
 *
 * <p>Each line is one {@code QueryPointer} on the window the last line named, so the chain it prints
 * is exactly the chain a client would walk. A reference server and this one should print the same
 * windows, in the same order, at the same coordinates. Motion is not injected: pair it with
 * {@link Drive} ({@code M x y}) to put the pointer somewhere worth asking about, and read the dump in
 * between.
 */
public final class PointProbe {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: PointProbe <host> <port> [x y]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (Wire wire = Wire.connect(host, port)) {
            int window = wire.root();
            int depth = 0;
            while (window != 0 && depth < 16) {
                byte[] reply = queryPointer(wire, window);
                int sameScreen = reply[1] & 0xFF;
                int child = Wire.readInt(reply, 12);
                int rootX = Wire.readShort(reply, 16);
                int rootY = Wire.readShort(reply, 18);
                int winX = Wire.readShort(reply, 20);
                int winY = Wire.readShort(reply, 22);
                int mask = Wire.readShort(reply, 24);
                System.out.printf("%s: child=%s root=%d,%d win=%d,%d mask=0x%x sameScreen=%d%n",
                        window == wire.root() ? "root" : "0x" + Integer.toHexString(window),
                        child == 0 ? "None" : "0x" + Integer.toHexString(child),
                        (short) rootX, (short) rootY, (short) winX, (short) winY, mask, sameScreen);
                if (child == 0 || child == window) {
                    break;
                }
                window = child;
                depth++;
            }
        }
    }

    private static byte[] queryPointer(Wire wire, int window) throws Exception {
        byte[] request = new byte[8];
        request[0] = 38;                            // QueryPointer
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, window);
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                throw new IllegalStateException("no answer to QueryPointer");
            }
            if ((message[0] & 0x7F) == 1) {
                return message;
            }
        }
    }

    private PointProbe() {
    }
}
