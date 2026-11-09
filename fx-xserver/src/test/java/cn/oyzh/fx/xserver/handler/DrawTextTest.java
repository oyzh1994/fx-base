package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Text requests whose string is counted by a <b>CARD8</b>: {@code ImageText8} and {@code ImageText16}.
 *
 * <p>That length is the one field in the request that is a single byte, and the generated reader takes
 * it with a *signed* read - so every string of 128 characters or more arrives as a negative length
 * and the request was refused with {@code BadRequest}. Measured against Xvfb (2026-09-28), which
 * accepts every length up to 254; and found by a real client: xterm, redrawing after a window resize,
 * sent a 130 character run and died with {@code X Error: BadRequest ... X_ImageText8}, leaving its
 * host window gone. Both requests are decoded in {@code VariableLengthDecoders} now, and this pins the
 * boundary - 127 characters, 128, and a long one.
 */
class DrawTextTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 82;

    /** The xterm that died was running at about this size: past the signed byte, not near a limit. */
    private static final int LONG = 130;

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
     * The requests are accepted and the text lands: for every length, a fence request afterwards is
     * answered normally, which it could not be if the server had raised an error for the text.
     */
    @Test
    void aLongStringIsAcceptedAndDrawn() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 600, 60, 0xFFFFFF);
            client.mapWindow(window);
            int gc = client.allocateId();
            client.createGC(gc, window, 0x000000);
            client.flush();
            Thread.sleep(150);

            for (int length : new int[]{1, 127, 128, LONG, 254}) {
                byte[] text = new byte[length];
                java.util.Arrays.fill(text, (byte) 'M');
                client.imageText8(window, gc, 2, 30, text, 0, length);
                client.flush();
                // A fence: a reply to this one can only arrive after the text request was handled,
                // and `readReply` raises if an error for it is on the stream instead.
                int[] geometry = client.geometry(window);
                assertEquals(600, geometry[2], "the fence is answered after a "
                        + length + " character ImageText8");
            }
            assertEquals(0x000000, client.pixelAt(window, 6, 25) & 0xFFFFFF,
                    "and the text was drawn: the pixels are no longer the window's white");
        }
    }

    /**
     * {@code ImageText8} fills the characters' cells with the GC's background; {@code PolyText8}
     * draws only the ink.
     *
     * <p>That fill is a request's whole difference and a terminal depends on it: xterm erases the
     * cursor and repaints a cell by drawing the character that belongs there with
     * {@code XDrawImageString}, so without the background the old cursor block is never cleared.
     * Measured with xterm against a reference: every cursor position it visited stayed behind as a
     * black square.
     */
    @Test
    void imageTextPaintsTheCharacterCellsWithTheBackground() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 60, 40, 0x00FF00);
            client.mapWindow(window);
            int gc = client.allocateId();
            client.createGC(gc, window, 0x000000);
            client.changeGC(gc, RawXClient.GCBACKGROUND, 0xFF0000);
            client.flush();
            Thread.sleep(150);

            // A space has no ink, so the whole of its cell is background: the fill is the only
            // thing that can put the red there.
            client.imageText8(window, gc, 10, 20, new byte[]{' '}, 0, 1);
            client.flush();
            client.geometry(window);                    // a fence

            assertEquals(0xFF0000, client.pixelAt(window, 12, 15) & 0xFFFFFF,
                    "the character's cell is the GC background colour");
        }
    }

    /** The same boundary for {@code ImageText16}, whose string is two bytes per character. */
    @Test
    void aLongWideStringIsAccepted() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 600, 60, 0xFFFFFF);
            client.mapWindow(window);
            int gc = client.allocateId();
            client.createGC(gc, window, 0x000000);
            client.flush();
            Thread.sleep(150);

            client.sendRequest(imageText16(window, gc, LONG));
            client.flush();
            assertEquals(600, client.geometry(window)[2],
                    "a " + LONG + " character ImageText16 is accepted");
        }
    }

    /** {@code ImageText16(window, gc, x, y, string)} with {@code count} characters of 'M'. */
    private static byte[] imageText16(int window, int gc, int count) {
        int units = (16 + count * 2 + 3) / 4;
        byte[] request = new byte[units * 4];
        request[0] = 77;                                // ImageText16
        request[1] = (byte) count;
        RawXClient.writeShortAt(request, 2, units);
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, gc);
        RawXClient.writeShortAt(request, 12, 2);
        RawXClient.writeShortAt(request, 14, 30);
        for (int i = 0; i < count; i++) {
            request[16 + i * 2] = 0;                    // 'M' as a two byte character
            request[17 + i * 2] = (byte) 'M';
        }
        return request;
    }
}
