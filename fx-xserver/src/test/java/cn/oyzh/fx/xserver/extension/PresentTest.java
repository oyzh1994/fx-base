package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Present: a client hands the server a finished frame, and hears that it has been shown.
 *
 * <p>Two things are pinned, and both were measured against a reference
 * ({@code tools/PresentProbe}) rather than taken from the specification: the version the extension
 * answers **echoes the client's own** - the opposite of Composite's and SYNC's, which answer the
 * server's - and the completion notification is a **forty byte** generic event. The second one is not
 * cosmetic: a generic event's length field is the only thing that says it is longer than the thirty
 * two bytes a client assumes, and with it left at zero the client reads the fixed part and leaves the
 * rest in the stream, so every message after it is read from the wrong offset.
 */
class PresentTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 86;

    private static final int QUERY_VERSION = 0;
    private static final int PIXMAP = 1;
    private static final int SELECT_INPUT = 3;

    /** The fixed part of a {@code PresentPixmap}: everything before its trailing notifies list. */
    private static final int PRESENT_FIXED_BYTES = 72;

    /** The {@code SelectInput} mask bit for completion notifications - a *mask*, not the event's
     * own number, and the two are one apart in a way that reads as a server bug when it is confused. */
    private static final int COMPLETE_NOTIFY_MASK = 1 << 1;

    /** The {@code eventType} a completion notification carries, which is the {@code Event} enum's
     * value and not the mask bit above. */
    private static final int COMPLETE_NOTIFY_EVENT = 1;

    private static final int WINDOW_COLOR = 0x00AA22;
    private static final int PIXMAP_COLOR = 0x0000FF;

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

    /**
     * The whole of the extension: a presented pixmap arrives in the window, and the completion
     * notification says so - with the client's serial, so a client with several presents in flight
     * can tell which one this is.
     */
    @Test
    void aPresentedPixmapArrivesInTheWindowAndIsReported() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = opcode(client);
            int window = client.allocateId();
            client.createWindow(window, client.root(), 20, 20, 100, 80);
            client.mapWindow(window);
            int windowGc = client.createGC(client.allocateId(), client.root(), WINDOW_COLOR);
            client.fillRectangle(window, windowGc, 0, 0, 100, 80);

            int pixmap = client.allocateId();
            client.createPixmap(pixmap, client.root(), 100, 80, 24);
            int pixmapGc = client.createGC(client.allocateId(), client.root(), PIXMAP_COLOR);
            client.fillRectangle(pixmap, pixmapGc, 0, 0, 100, 80);

            selectInput(client, opcode, window);
            present(client, opcode, window, pixmap, 7);

            byte[] event = client.readEvent();
            assertNotNull(event, "the completion notification has to arrive");
            assertEquals(40, event.length,
                    "a generic event is longer than the thirty two bytes a reader assumes, and its "
                            + "length field is the only thing that says so");
            assertEquals(COMPLETE_NOTIFY_EVENT, event[8] & 0xFF, "eventType");
            assertEquals(0, event[10] & 0xFF, "kind: the pixmap was presented");
            assertEquals(0, event[11] & 0xFF, "mode: it was a copy");
            assertEquals(window, RawXClient.readInt(event, 16), "for the window that was presented to");
            assertEquals(7, RawXClient.readInt(event, 20), "and it carries the client's own serial");

            assertEquals(0xFF000000 | PIXMAP_COLOR, client.pixelAt(window, 50, 40),
                    "the presented pixmap is what the window shows");
        }
    }

    /** The version answers the client's own, measured: 1.0 to a client asking for 1.0. */
    @Test
    void theVersionIsTheClientsOwn() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            byte[] request = new byte[12];
            request[0] = (byte) opcode(client);
            request[1] = (byte) QUERY_VERSION;
            RawXClient.writeShortAt(request, 2, 3);
            RawXClient.writeIntAt(request, 4, 1);
            RawXClient.writeIntAt(request, 8, 0);
            client.sendRequest(request);
            byte[] reply = client.readReply();
            assertEquals(1, RawXClient.readInt(reply, 8), "major");
            assertEquals(0, RawXClient.readInt(reply, 12), "a client asking for 1.0 is told 1.0");
        }
    }

    // ------------------------------------------------------------------

    private static void selectInput(RawXClient client, int opcode, int window) throws IOException {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = (byte) SELECT_INPUT;
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeIntAt(request, 4, client.allocateId());     // the event context
        RawXClient.writeIntAt(request, 8, window);
        RawXClient.writeIntAt(request, 12, COMPLETE_NOTIFY_MASK);
        client.sendRequest(request);
    }

    /**
     * {@code PresentPixmap} with the whole pixmap valid, the whole window to update, and no extra
     * notifications. The offsets are the ones the reference accepted and the ones the probe's
     * generated writer produces - `valid` and `update` are region ids, not flags.
     */
    private static void present(RawXClient client, int opcode, int window, int pixmap, int serial)
            throws IOException {
        byte[] request = new byte[PRESENT_FIXED_BYTES];
        request[0] = (byte) opcode;
        request[1] = (byte) PIXMAP;
        RawXClient.writeShortAt(request, 2, PRESENT_FIXED_BYTES / 4);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, pixmap);
        RawXClient.writeIntAt(request, 12, serial);
        RawXClient.writeIntAt(request, 16, 0);                      // valid: None, the whole pixmap
        RawXClient.writeIntAt(request, 20, 0);                      // update: None, the whole window
        RawXClient.writeShortAt(request, 24, 0);                    // x-off
        RawXClient.writeShortAt(request, 26, 0);                    // y-off
        RawXClient.writeIntAt(request, 28, 0);                      // target-crtc
        RawXClient.writeIntAt(request, 32, 0);                      // wait-fence
        RawXClient.writeIntAt(request, 36, 0);                      // idle-fence
        RawXClient.writeIntAt(request, 40, 0);                      // options
        RawXClient.writeIntAt(request, 48, 0);                      // target-msc, low word
        RawXClient.writeIntAt(request, 52, 0);
        RawXClient.writeIntAt(request, 56, 0);                      // divisor
        RawXClient.writeIntAt(request, 60, 0);
        RawXClient.writeIntAt(request, 64, 0);                      // remainder
        RawXClient.writeIntAt(request, 68, 0);
        client.sendRequest(request);
    }

    private static int opcode(RawXClient client) throws IOException {
        int[] info = client.queryExtensionInfo("Present");
        assertTrue(info[0] != 0, "the extension has to be advertised");
        return info[1];
    }
}
