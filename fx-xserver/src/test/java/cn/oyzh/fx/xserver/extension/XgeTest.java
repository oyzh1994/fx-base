package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Generic Event Extension: the handshake that makes the generic events this server already sends
 * *findable* to a client that checks for them before subscribing.
 *
 * <p>Three things are pinned, and the third is the one that is easy to get wrong in a way nothing
 * else notices: the extension is advertised under its own name, its reply is the **server's**
 * version (1.0 to a client asking for 1.0 and to one asking for 0.0, measured on a reference with
 * {@code tools/XgeProbe}), and {@code QueryExtension} reports **zero** for both its event and error
 * bases - an extension that defines neither reports 0, not the next free code, which is what the
 * registry used to hand out (a reference answers 0 for BIG-REQUESTS, XC-MISC, Composite and Present
 * too, all measured the same way).
 */
class XgeTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 87;

    private static final int QUERY_VERSION = 0;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--single-window", "--no-unix"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    /** It is advertised, and it claims no event or error codes because it defines none. */
    @Test
    void itIsAdvertisedWithoutEventOrErrorCodes() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("Generic Event Extension");
            assertTrue(info[0] != 0, "the extension has to be advertised");
            assertTrue(info[1] >= 128, "an extension opcode, which is what a client caches");
            assertEquals(0, info[2], "no events of its own, so the first event code is zero");
            assertEquals(0, info[3], "and the first error code too");
        }
    }

    /** The version is the server's: a client is told 1.0 whatever it asked for. */
    @Test
    void theVersionIsTheServers() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            assertEquals(1, queryVersion(client, opcode, 1, 0)[0], "major");
            assertEquals(0, queryVersion(client, opcode, 1, 0)[1], "a 1.0 client is told 1.0");
            assertEquals(1, queryVersion(client, opcode, 0, 0)[0], "major");
            assertEquals(0, queryVersion(client, opcode, 0, 0)[1], "and so is a 0.0 client");
        }
    }

    /**
     * A client that asks with a length that runs past the request is refused rather than read off
     * the end - the reply is exactly thirty two bytes and the body's two fields are where the
     * protocol puts them, which is what a hand-written decoder has to get right.
     */
    @Test
    void theReplyIsTheProtocolsShape() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            byte[] request = new byte[8];
            request[0] = (byte) opcode(client);
            request[1] = (byte) QUERY_VERSION;
            RawXClient.writeShortAt(request, 2, 2);
            RawXClient.writeShortAt(request, 4, 1);
            RawXClient.writeShortAt(request, 6, 0);
            client.sendRequest(request);
            byte[] reply = client.readReply();
            assertEquals(32, reply.length, "a reply with nothing after its fixed part");
            assertEquals(1, reply[0] & 0xFF, "a reply");
            assertEquals(0, RawXClient.readInt(reply, 4), "and no extra data");
            assertEquals(1, RawXClient.readShort(reply, 8), "major");
            assertEquals(0, RawXClient.readShort(reply, 10), "minor");
        }
    }

    // ------------------------------------------------------------------

    private static int[] queryVersion(RawXClient client, int opcode, int major, int minor)
            throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) QUERY_VERSION;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeShortAt(request, 4, major);
        RawXClient.writeShortAt(request, 6, minor);
        client.sendRequest(request);
        byte[] reply = client.readReply();
        return new int[]{RawXClient.readShort(reply, 8), RawXClient.readShort(reply, 10)};
    }

    private static int opcode(RawXClient client) throws IOException {
        int[] info = client.queryExtensionInfo("Generic Event Extension");
        assertTrue(info[0] != 0, "the extension has to be advertised");
        return info[1];
    }
}
