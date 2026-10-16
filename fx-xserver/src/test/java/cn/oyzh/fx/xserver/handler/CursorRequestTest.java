package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The cursors a toolkit installs, including the one that killed it.
 *
 * <p>A toolkit does not use {@code CreateCursor}'s bitmap form for its own cursors: it builds the
 * image with RENDER ({@code CreateCursor} on a picture) and installs it on a window with
 * {@code XFixesChangeCursor}. What it still sends the core request for is a **placeholder** - a one
 * pixel pixmap with the hotspot at (1,1), which is *outside* it - and this server answered that with
 * {@code BadMatch}. The refusal reached the toolkit as an X error, and GTK's default handler ends the
 * process, so a forwarded application lost its input fields' right-click menus (复制/剪切/粘贴 dead
 * but clickable) and nothing else looked wrong: measured on EasyShell, 2026-09-30, with a GTK probe
 * on the same display.
 *
 * <p>Measured against Xvfb: the same request is **accepted** - a 1x1 source and hotspot (1,1) create a
 * cursor - so this is a difference from a reference and not a client's mistake. The hotspot is only an
 * offset into the image, so a hotspot outside it is a cursor whose image hangs off its own origin,
 * which is drawable.
 */
class CursorRequestTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 62;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "400x300", "--backend", "headless", "--no-unix"}));
        server.start();
        server.startEventLoop();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void aCursorWithTheHotspotOutsideItsPixmapIsAccepted() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int pixmap = client.allocateId();
            int cursor = client.allocateId();
            byte[] createPixmap = new byte[16];
            createPixmap[0] = 53;                           // CreatePixmap
            RawXClient.writeShortAt(createPixmap, 2, 4);
            RawXClient.writeIntAt(createPixmap, 4, pixmap);
            RawXClient.writeIntAt(createPixmap, 8, client.root());
            RawXClient.writeShortAt(createPixmap, 12, 1);   // 1x1
            RawXClient.writeShortAt(createPixmap, 14, 1);
            createPixmap[1] = 1;                            // depth 1: a bitmap, as a cursor wants
            client.sendRequest(createPixmap);

            byte[] createCursor = new byte[32];
            createCursor[0] = 93;                           // CreateCursor
            RawXClient.writeShortAt(createCursor, 2, 8);
            RawXClient.writeIntAt(createCursor, 4, cursor);
            RawXClient.writeIntAt(createCursor, 8, pixmap); // source
            RawXClient.writeIntAt(createCursor, 12, pixmap); // mask: the same 1x1 pixmap
            // The hotspot sits at 28 and 30 - after the two colours (six CARD16 each) - and that
            // offset is the whole point: written at 24 and 26 it is not read at all, and the request
            // becomes the ordinary one this test is not about.
            RawXClient.writeShortAt(createCursor, 28, 1);   // hotspot x: outside a 1x1 pixmap
            RawXClient.writeShortAt(createCursor, 30, 1);   // hotspot y
            client.sendRequest(createCursor);
            client.flush();

            // A refusal is an error event, which the client reads like any other message: the round
            // trip that follows is what proves the stream is still in step.
            assertFalse(refusedCursorError(client, 93),
                    "a hotspot outside the source has to be accepted, as a reference does");
            assertNotEquals(0, client.queryExtension("XTEST"),
                    "and the stream is still in step afterwards");

            // **The check above is only worth something if it can fail**: a request this server
            // *must* refuse is sent with it, and the same reader has to see that error. A cursor
            // with neither a source nor a mask is the documented BadMatch.
            byte[] badCursor = new byte[32];
            badCursor[0] = 93;
            RawXClient.writeShortAt(badCursor, 2, 8);
            RawXClient.writeIntAt(badCursor, 4, client.allocateId());
            client.sendRequest(badCursor);
            client.flush();
            assertTrue(refusedCursorError(client, 93),
                    "the reader has to see a refusal it is looking for, or the check above proves"
                            + " nothing");
        }
    }

    /**
     * Whether the server refused a {@code CreateCursor} (opcode 93) since the last read.
     *
     * <p>An error is a message like any other: reading until the stream goes quiet is what collects
     * it, and the caller's next request is what shows the stream is still in step.
     */
    private static boolean refusedCursorError(RawXClient client, int opcode) throws IOException {
        boolean refused = false;
        for (byte[] message = client.readEvent(); message != null; message = client.readEvent()) {
            // An error's fields: the error code at 1, the bad resource at 4, and the opcode at 10.
            // **The opcode is the major one, at 10** - offset 11 is its minor half, which for a core
            // request is nought. Reading the wrong one is how this check first reported "not refused"
            // for a request the server had refused.
            if ((message[0] & 0x7F) == 0 && message[10] == (byte) opcode) {
                refused = true;
            }
        }
        return refused;
    }
}
