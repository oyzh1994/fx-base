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
 * XVideo (Xv): the extension a video player asks for, answered the way a server with no video hardware
 * answers it (F6).
 *
 * <p>What is pinned here is the measured shape of that answer: {@code QueryAdaptors} reports **zero**
 * adaptors - which is what a reference reports too, and what a player falls back from - while the
 * extension itself is present, answers its own version 2.2, and refuses every request that names a
 * port with its own {@code BadPort}. The one request that names a drawable rather than a port,
 * {@code SelectVideoNotify}, is accepted, and the order of the checks is the reference's: the drawable
 * and the GC are resolved before the port, so a GC of {@code None} is {@code BadGContext} rather than
 * {@code BadPort}. That last one is not a detail - the probe caught it, and a chapter of this server's
 * history is extensions that answered the right refusal for the wrong reason.
 */
class XvTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 84;

    private static final int QUERY_EXTENSION = 0;
    private static final int QUERY_ADAPTORS = 1;
    private static final int QUERY_ENCODINGS = 2;
    private static final int GRAB_PORT = 3;
    private static final int PUT_VIDEO = 5;
    private static final int STOP_VIDEO = 9;
    private static final int SELECT_VIDEO_NOTIFY = 10;
    private static final int QUERY_PORT_ATTRIBUTES = 15;
    private static final int PUT_IMAGE = 18;

    /** Core errors this test asserts on. */
    private static final int BAD_DRAWABLE = 9;

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

    /** The extension is there, with its own version, two events and three errors. */
    @Test
    void theExtensionAnswersItsOwnVersion() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("XVideo");
            assertEquals(1, info[0], "the extension is there");
            assertTrue(info[2] > 0, "it defines events: VideoNotify and PortNotify");
            assertTrue(info[3] > 0, "and errors: BadPort, BadEncoding and BadControl");

            byte[] request = new byte[4];
            request[0] = (byte) info[1];
            request[1] = QUERY_EXTENSION;
            RawXClient.writeShortAt(request, 2, 1);
            client.sendRequest(request);
            byte[] reply = client.readReply();
            assertEquals(2, RawXClient.readShort(reply, 8), "the extension's own version");
            assertEquals(2, RawXClient.readShort(reply, 10), "and revision, which is a reference's");
        }
    }

    /**
     * No adaptors: the honest answer for a server with no video hardware, and the one a reference
     * gives - a player that reads it takes its non-Xv path, which is what it does today.
     */
    @Test
    void thereAreNoAdaptors() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtensionInfo("XVideo")[1];
            client.sendRequest(request(opcode, QUERY_ADAPTORS, 2, client.root()));
            byte[] reply = client.readReply();
            assertEquals(0, RawXClient.readInt(reply, 8), "zero adaptors");
            assertEquals(0, RawXClient.readInt(reply, 4), "and the reply is empty behind that");

            // The drawable is still checked: an id that is not one is the core error.
            client.sendRequest(request(opcode, QUERY_ADAPTORS, 2, client.allocateId()));
            assertEquals(BAD_DRAWABLE, refusal(client), "a drawable that is not a drawable");
        }
    }

    /** Every request that names a port is refused with the extension's own error. */
    @Test
    void aPortThatCannotExistIsBadPort() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("XVideo");
            int opcode = info[1];
            int port = client.allocateId();
            // A real GC and a real drawable, so the *only* thing wrong with these requests is the
            // port: with a GC of None the answer is BadGContext instead, which the test below pins.
            int gc = client.allocateId();
            client.createGC(gc, client.root(), 0);
            client.flush();

            for (int minor : new int[]{QUERY_ENCODINGS, GRAB_PORT, QUERY_PORT_ATTRIBUTES,
                    STOP_VIDEO, PUT_IMAGE}) {
                client.sendRequest(minor == STOP_VIDEO
                        ? stopVideo(opcode, port, client.root())
                        : minor == PUT_IMAGE
                                ? putImage(opcode, port, client.root(), gc)
                                : request(opcode, minor, 2, port));
                assertEquals(info[3], refusal(client),
                        "the extension's own BadPort, not a core error (minor " + minor + ")");
            }

            // The drawable and the GC are checked *first* on the requests that carry them, which is
            // what a reference does: a GC of None is BadGContext rather than BadPort.
            client.sendRequest(putImage(opcode, port, client.root(), client.allocateId()));
            assertEquals(cn.oyzh.fx.xserver.client.ProtocolException.GC, refusal(client),
                    "a GC that does not exist is reported before the port is");

            client.sendRequest(putImage(opcode, port, client.root(), gc));
            assertEquals(info[3], refusal(client), "and with a real GC it is the port that is wrong");
        }
    }

    /**
     * {@code SelectVideoNotify} names a drawable, and a reference accepts it with no adaptors at all:
     * the mask is recorded and nothing ever fires, which is what a client sees here too.
     */
    @Test
    void videoNotifyIsAcceptedForADrawable() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtensionInfo("XVideo")[1];
            byte[] request = new byte[12];
            request[0] = (byte) opcode;
            request[1] = SELECT_VIDEO_NOTIFY;
            RawXClient.writeShortAt(request, 2, 3);
            RawXClient.writeIntAt(request, 4, client.root());
            request[8] = 1;                             // on
            client.sendRequest(request);
            client.flush();
            client.socketTimeout(400);
            // Accepted: the stream stays quiet, and the next request is answered normally.
            client.sendRequest(request(opcode, QUERY_ADAPTORS, 2, client.root()));
            byte[] reply = client.readReply();
            assertEquals(0, RawXClient.readInt(reply, 8), "the next answer is the adaptors reply");

            // A drawable that is not one is still refused.
            request[4] = 0;
            request[5] = 0;
            request[6] = 0;
            request[7] = 0;
            client.sendRequest(request);
            assertEquals(BAD_DRAWABLE, refusal(client), "and a drawable of None is not accepted");
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static byte[] request(int opcode, int minor, int units, int value) {
        byte[] request = new byte[units * 4];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        RawXClient.writeShortAt(request, 2, units);
        RawXClient.writeIntAt(request, 4, value);
        return request;
    }

    private static byte[] stopVideo(int opcode, int port, int drawable) {
        byte[] request = new byte[12];
        request[0] = (byte) opcode;
        request[1] = STOP_VIDEO;
        RawXClient.writeShortAt(request, 2, 3);
        RawXClient.writeIntAt(request, 4, port);
        RawXClient.writeIntAt(request, 8, drawable);
        return request;
    }

    private static byte[] putImage(int opcode, int port, int drawable, int gc) {
        byte[] request = new byte[40];
        request[0] = (byte) opcode;
        request[1] = PUT_IMAGE;
        RawXClient.writeShortAt(request, 2, 10);
        RawXClient.writeIntAt(request, 4, port);
        RawXClient.writeIntAt(request, 8, drawable);
        RawXClient.writeIntAt(request, 12, gc);
        RawXClient.writeIntAt(request, 16, 0x3);        // the image format
        RawXClient.writeShortAt(request, 32, 64);
        RawXClient.writeShortAt(request, 34, 48);
        return request;
    }

    private static int refusal(RawXClient client) throws IOException {
        byte[] error = client.readBytes(32);
        assertEquals(0, error[0] & 0xFF, "the next message is an error, not a reply or an event");
        return error[1] & 0xFF;
    }
}
