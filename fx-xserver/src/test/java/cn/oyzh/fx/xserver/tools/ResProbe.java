package cn.oyzh.fx.xserver.tools;

import com.github.moaxcp.x11.protocol.X11LittleEndianOutputStream;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.res.ClientIdMask;
import com.github.moaxcp.x11.protocol.res.ClientIdSpec;
import com.github.moaxcp.x11.protocol.res.QueryClientIds;
import com.github.moaxcp.x11.protocol.res.QueryClientPixmapBytes;
import com.github.moaxcp.x11.protocol.res.QueryClientResources;
import com.github.moaxcp.x11.protocol.res.QueryClients;
import com.github.moaxcp.x11.protocol.res.QueryResourceBytes;
import com.github.moaxcp.x11.protocol.res.QueryVersion;
import com.github.moaxcp.x11.protocol.res.ResourceIdSpec;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.eclipse.collections.impl.factory.Lists;

import java.io.ByteArrayOutputStream;

/**
 * X-Resource as a client uses it - the harness for F3, and the extension that answers "who is using
 * the memory", which E4's per-client framebuffer work left with no way to see.
 *
 * <p>What it does, in the order a client does it: the version handshake (twice, because whether the
 * answer is the server's version or the client's is a per-extension rule this kit measures rather
 * than carries over), the client list, the resource counts for this very client, its pixmap bytes,
 * its client ids, and finally the byte size of one named resource. Every field is printed whether or
 * not the extension answered, because "the extension is missing" and "the count is zero" are
 * different findings - and the resource-count reply's atom *names* are resolved with
 * {@code GetAtomName} rather than assumed, since the atom numbering is the server's own.
 *
 * <p>Requests are serialised with the generated protocol classes, for the reason
 * {@code PresentProbe} records: the field orders are given rather than written down twice, and a
 * reply reader that disagrees with a reference's reply is still caught. Replies are read by hand,
 * because the offsets are the same numbers the generated readers would use and printing a field
 * position makes a difference readable.
 *
 * <p>Usage: {@code ResProbe &lt;host&gt; &lt;port&gt;}.
 */
public final class ResProbe {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: ResProbe <host> <port>");
            return;
        }
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int[] info = client.queryExtensionInfo("X-Resource");
            System.out.println("X-Resource present=" + info[0] + " major=" + info[1]
                    + " firstEvent=" + info[2] + " firstError=" + info[3]);
            if (info[0] == 0) {
                return;
            }
            int opcode = info[1];

            for (byte[] asked : new byte[][]{{1, 2}, {1, 0}, {2, 0}}) {
                send(client, opcode, QueryVersion.builder()
                        .clientMajor(asked[0]).clientMinor(asked[1]).build());
                byte[] reply = client.readReply();
                // The reply's two version fields are CARD16, not two bytes: reading them one byte
                // apart turns a 1.2 into "1.0" and reports the *high* half of the major as the
                // minor. Read off the generated reader's own field widths rather than counted.
                System.out.println("  QueryVersion(" + asked[0] + "." + asked[1] + ") -> "
                        + RawXClient.readShort(reply, 8) + "." + RawXClient.readShort(reply, 10));
            }

            // A window of this client's own, so the requests below have one id that is certainly a
            // resource of a known client - the XID argument names the client by naming any of its
            // resources.
            int window = client.allocateId();
            client.createWindow(window, client.root(), 10, 10, 40, 30);
            // One of each kind a client can make cheaply, so the resource-count reply names the
            // types instead of only the one - the atom numbers are the server's own and the *names*
            // are what a comparison can use.
            int pixmap = client.allocateId();
            client.createPixmap(pixmap, client.root(), 16, 16, 24);
            int gc = client.allocateId();
            client.createGC(gc, client.root(), 0xFFFFFF);
            int font = client.allocateId();
            client.openFont(font, "fixed");
            int cursorId = client.allocateId();
            client.createGlyphCursor(cursorId, font, font, 'x', 'x', 0, 0xFFFFFF);
            client.flush();

            send(client, opcode, QueryClients.builder().build());
            byte[] clients = client.readReply();
            int clientCount = RawXClient.readInt(clients, 8);
            System.out.println("  QueryClients -> " + clientCount + " client(s)");
            for (int i = 0; i < clientCount; i++) {
                int at = 32 + i * 8;
                System.out.println("    client[" + i + "] base=0x"
                        + Integer.toHexString(RawXClient.readInt(clients, at))
                        + " mask=0x" + Integer.toHexString(RawXClient.readInt(clients, at + 4)));
            }

            send(client, opcode, QueryClientResources.builder().xid(window).build());
            byte[] resources = client.readReply();
            int typeCount = RawXClient.readInt(resources, 8);
            System.out.println("  QueryClientResources(own window) -> " + typeCount + " type(s)");
            for (int i = 0; i < typeCount; i++) {
                int at = 32 + i * 8;
                int type = RawXClient.readInt(resources, at);
                int count = RawXClient.readInt(resources, at + 4);
                System.out.println("    type=" + type + " (" + atomName(client, type) + ") count="
                        + count);
            }

            send(client, opcode, QueryClientPixmapBytes.builder().xid(window).build());
            byte[] bytes = client.readReply();
            System.out.println("  QueryClientPixmapBytes -> bytes=" + RawXClient.readInt(bytes, 8)
                    + " overflow=" + RawXClient.readInt(bytes, 12));

            int base = clientCount > 0 ? RawXClient.readInt(clients, 32) : 0;

            // A spec that names one client rather than all of them, and one that names none - the
            // second is the only way to make a server resolve a client that is not there, and what
            // it answers is not something a probe can guess.
            for (int named : new int[]{base, 0x7FE00000}) {
                send(client, opcode, QueryClientIds.builder()
                        .specs(Lists.immutable.of(ClientIdSpec.builder().client(named).mask(1).build()))
                        .build());
                try {
                    byte[] answer = client.readReply();
                    System.out.println("  QueryClientIds(client=0x" + Integer.toHexString(named)
                            + ") -> " + RawXClient.readInt(answer, 8) + " id value(s)");
                } catch (java.io.IOException refused) {
                    System.out.println("  QueryClientIds(client=0x" + Integer.toHexString(named)
                            + ") -> refused:" + errorCode(refused.getMessage()));
                }
            }

            // Each mask the protocol defines, one at a time: 1 is CLIENT_X_I_D and 2 is
            // LOCAL_CLIENT_P_I_D, and what a server puts in `value` for each is its own decision -
            // measured rather than assumed, because the two are not symmetric.
            for (int mask : new int[]{ClientIdMask.CLIENT_X_I_D.getValue(),
                    ClientIdMask.LOCAL_CLIENT_P_I_D.getValue(),
                    ClientIdMask.CLIENT_X_I_D.getValue() | ClientIdMask.LOCAL_CLIENT_P_I_D.getValue()}) {
                send(client, opcode, QueryClientIds.builder()
                        .specs(Lists.immutable.of(ClientIdSpec.builder()
                                .client(0)                          // 0 = every client, per the spec
                                .mask(mask)
                                .build()))
                        .build());
                byte[] ids = client.readReply();
                int idCount = RawXClient.readInt(ids, 8);
                System.out.println("  QueryClientIds(mask=" + mask + ") -> " + idCount
                        + " id value(s), reply " + ids.length + " bytes");
                int at = 32;
                for (int i = 0; i < idCount; i++) {
                    int specClient = RawXClient.readInt(ids, at);
                    int specMask = RawXClient.readInt(ids, at + 4);
                    int valueBytes = RawXClient.readInt(ids, at + 8);
                    StringBuilder value = new StringBuilder();
                    for (int b = 0; b < valueBytes / 4; b++) {
                        value.append(RawXClient.readInt(ids, at + 12 + b * 4)).append(' ');
                    }
                    System.out.println("    spec(client=0x" + Integer.toHexString(specClient)
                            + " mask=0x" + Integer.toHexString(specMask) + ") value=["
                            + value.toString().trim() + "]");
                    at += 12 + valueBytes;
                }
            }

            // A named resource's size, and what a type of None means. The type is an atom; the
            // resource-count reply above is where its number came from, so asking with None is the
            // "any type" form a client that wants a total would use.
            // 1 is included because the spec calls `type` an atom and a first run of this probe
            // asked only with the measured WINDOW atom (33) and got nothing back from the
            // reference - so the field is compared against something other than the atom number,
            // and 1 is the other candidate an implementation might use.
            for (int type : new int[]{0, 1, windowType(resources)}) {
                for (int[] named : new int[][]{{window, 'w'}, {pixmap, 'p'}}) {
                    send(client, opcode, QueryResourceBytes.builder()
                            .client(base)
                            .specs(Lists.immutable.of(ResourceIdSpec.builder()
                                    .resource(named[0]).type(type).build()))
                            .build());
                    byte[] sizes = client.readReply();
                    int sizeCount = RawXClient.readInt(sizes, 8);
                    System.out.println("  QueryResourceBytes(" + (char) named[1] + ", type=" + type
                            + " (" + atomName(client, type) + ")) -> " + sizeCount + " size(s)");
                    int cursor = 32;
                    for (int i = 0; i < sizeCount; i++) {
                        System.out.println("    resource=0x"
                                + Integer.toHexString(RawXClient.readInt(sizes, cursor))
                                + " type=" + RawXClient.readInt(sizes, cursor + 4)
                                + " bytes=" + RawXClient.readInt(sizes, cursor + 8)
                                + " refCount=" + RawXClient.readInt(sizes, cursor + 12)
                                + " useCount=" + RawXClient.readInt(sizes, cursor + 16)
                                + " crossReferences=" + RawXClient.readInt(sizes, cursor + 20));
                        cursor += 24 + RawXClient.readInt(sizes, cursor + 20) * 20;
                    }
                }
            }

            // An id no client owns: what a server says when it cannot resolve the client at all.
            // `readReply` turns a refusal into an exception, which is printed rather than allowed to
            // end the run - the refusal *is* the measurement, and the error code is what a client
            // branches on.
            send(client, opcode, QueryClientResources.builder()
                    .xid(0xDEADBEEF).build());
            try {
                byte[] answer = client.readReply();
                System.out.println("  QueryClientResources(an id nobody owns) -> reply with "
                        + RawXClient.readInt(answer, 8) + " type(s)");
            } catch (java.io.IOException refused) {
                System.out.println("  QueryClientResources(an id nobody owns) -> refused:"
                        + errorCode(refused.getMessage()));
            }

            send(client, opcode, QueryClientPixmapBytes.builder().xid(0xDEADBEEF).build());
            try {
                byte[] answer = client.readReply();
                System.out.println("  QueryClientPixmapBytes(an id nobody owns) -> bytes="
                        + RawXClient.readInt(answer, 8));
            } catch (java.io.IOException refused) {
                System.out.println("  QueryClientPixmapBytes(an id nobody owns) -> refused:"
                        + errorCode(refused.getMessage()));
            }

            send(client, opcode, QueryResourceBytes.builder()
                    .client(0x7FE00000)
                    .specs(Lists.immutable.of(ResourceIdSpec.builder()
                            .resource(window).type(0).build()))
                    .build());
            try {
                byte[] answer = client.readReply();
                System.out.println("  QueryResourceBytes(an unknown client) -> "
                        + RawXClient.readInt(answer, 8) + " size(s)");
            } catch (java.io.IOException refused) {
                System.out.println("  QueryResourceBytes(an unknown client) -> refused:"
                        + errorCode(refused.getMessage()));
            }
        }
    }

    /**
     * The diagnostic part of a refusal's message, without the sequence numbers - which differ
     * between two servers by construction and would make every comparison a difference.
     */
    private static String errorCode(String message) {
        int from = message.indexOf("error ");
        int to = message.indexOf(" major ");
        if (from < 0) {
            return " " + message;
        }
        return " " + message.substring(from, to < 0 ? message.length() : to);
    }

    /** The atom a window's resource type was reported under, or None if the reply had no entries. */
    private static int windowType(byte[] resourcesReply) {
        return RawXClient.readInt(resourcesReply, 8) > 0
                ? RawXClient.readInt(resourcesReply, 32) : 0;
    }

    /** Resolves a resource-type atom so the comparison is by name, not by a server's own numbering. */
    private static String atomName(RawXClient client, int atom) {
        if (atom == 0) {
            return "None";
        }
        try {
            return client.atomName(atom);
        } catch (Exception e) {
            return "?";
        }
    }

    /** Serialises a generated request and sends it, so no field order is written down twice. */
    private static void send(RawXClient client, int opcode, XRequest request) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        X11LittleEndianOutputStream out = new X11LittleEndianOutputStream(bytes);
        request.write((byte) opcode, out);
        out.flush();
        client.sendRequest(bytes.toByteArray());
    }

    private ResProbe() {
    }
}
