package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * The Generic Event Extension as a client uses it - the harness for F4.
 *
 * <p>XGE is a handshake and a version number and nothing else: one request, {@code QueryVersion},
 * whose reply tells a client that generic events are a thing the server understands. XI2's events
 * already work here, so what was missing is not a mechanism but the way a client *checks* - Xlib's
 * cookie API and every toolkit that gates XI2 behind it look for this extension before subscribing.
 *
 * <p>The extension defines no events and no errors, so there is nothing else to ask. Two version
 * pairs are sent because the reply's rule - the server's version or the client's - is per-extension
 * and measured each time rather than carried over.
 *
 * <p>There is no generated protocol module for XGE in the x11 library, so the request is four
 * fields written by hand and the reply's two version bytes read at their fixed offsets: the request
 * is {@code opcode, 0, length=2} then two CARD16s, and the reply's body starts at offset 8.
 *
 * <p>Usage: {@code XgeProbe &lt;host&gt; &lt;port&gt;}.
 */
public final class XgeProbe {

    private static final int QUERY_VERSION = 0;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: XgeProbe <host> <port>");
            return;
        }
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int[] info = client.queryExtensionInfo("Generic Event Extension");
            System.out.println("Generic Event Extension present=" + info[0] + " major=" + info[1]
                    + " firstEvent=" + info[2] + " firstError=" + info[3]);
            if (info[0] == 0) {
                return;
            }
            int opcode = info[1];

            for (int[] asked : new int[][]{{1, 0}, {0, 0}}) {
                byte[] request = new byte[8];
                request[0] = (byte) opcode;
                request[1] = (byte) QUERY_VERSION;
                RawXClient.writeShortAt(request, 2, 2);              // two four-byte units
                RawXClient.writeShortAt(request, 4, asked[0]);       // client_major_version
                RawXClient.writeShortAt(request, 6, asked[1]);       // client_minor_version
                client.sendRequest(request);
                byte[] reply = client.readReply();
                System.out.println("  QueryVersion(" + asked[0] + "." + asked[1] + ") -> "
                        + RawXClient.readShort(reply, 8) + "." + RawXClient.readShort(reply, 10));
            }
        }
    }

    private XgeProbe() {
    }
}
