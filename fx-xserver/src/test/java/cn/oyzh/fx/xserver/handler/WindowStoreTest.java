package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every window keeps its pixels (B5's general store), and that is what a client can read back.
 *
 * <p>Before the store a window drew straight into the screen, clipped to the part of itself that was
 * visible. Two things followed, and both are wrong for a compositing window manager: what a window
 * drew while something covered it was thrown away, and asking a covered window for its pixels
 * answered with whatever the window on top had painted there. A frame per top level (C8) cannot be
 * built on that - it has to show a window's own pixels, not a rectangle of the shared screen.
 *
 * <p>What is pinned here: a window's pixels are its own wherever it is and whatever is over it, and a
 * move carries them along instead of leaving them behind. The reveal path is covered by
 * {@code BackingStoreTest} and the alpha case by {@code TranslucentWindowTest}.
 */
class WindowStoreTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 63;

    private static final int RED = 0xFFFF0000;
    private static final int BLUE = 0xFF0000FF;
    private static final int GREEN = 0xFF00FF00;

    private static final int SCREEN_BACKGROUND = Screen.DEFAULT_BACKGROUND_ARGB;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix", "--single-window"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void aCoveredWindowAnswersWithItsOwnPixels() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int lower = client.allocateId();
            client.createWindow(lower, client.root(), 0, 0, 200, 200);
            client.mapWindow(lower);
            int red = client.createGC(client.allocateId(), client.root(), RED);
            client.fillRectangle(lower, red, 0, 0, 200, 200);

            int upper = client.allocateId();
            client.createWindow(upper, client.root(), 0, 0, 200, 200);
            client.mapWindow(upper);
            int blue = client.createGC(client.allocateId(), client.root(), BLUE);
            client.fillRectangle(upper, blue, 0, 0, 200, 200);
            sync(client, upper);
            waitForColor(10, 10, BLUE, "the window on top is what the screen shows");

            // The read is GetImage on the window that is covered: its own pixels, not the ones the
            // window over it put on the screen at that rectangle.
            assertEquals(RED, client.pixelAt(lower, 10, 10),
                    "a window answers with its own pixels, which it kept while it was covered");
            assertEquals(BLUE, client.pixelAt(upper, 10, 10),
                    "and the window on top answers with its own");
        }
    }

    @Test
    void aMovedWindowKeepsItsPixels() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 300, 0, 100, 100);
            client.mapWindow(window);
            int green = client.createGC(client.allocateId(), client.root(), GREEN);
            client.fillRectangle(window, green, 0, 0, 100, 100);
            sync(client, window);
            waitForColor(310, 10, GREEN, "painted where it was created");

            // The window carries its pixels with it: only the screen's copy is left behind.
            client.configureWindow(window, 500, 300, 100, 100);
            sync(client, window);

            waitForColor(550, 350, GREEN, "and still painted where it moved to");
            waitForColor(310, 10, SCREEN_BACKGROUND,
                    "while the place it left is the desktop again");
        }
    }

    /**
     * A window takes a copy *from a pixmap* into its own pixels, and the screen shows it from there.
     *
     * <p>This is the shape a caret blink draws with, and it is the one drawing path the rest of the
     * suite never exercises: {@code RawXClient} had no {@code CopyArea} at all until this test, so
     * nothing sent one. It has to be pinned separately because copying a pixmap into a window is
     * where the store's coordinate rules meet a source that has no origin on the screen - the
     * destination's origin is 0 (its pixels *are* its coordinates) while a screen-drawing window's
     * would be its absolute position, and getting that wrong leaves the copy in the store but not on
     * the screen, or the other way round.
     */
    @Test
    void aWindowTakesACopyFromAPixmapIntoItsOwnPixels() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 600, 0, 100, 100);
            client.mapWindow(window);
            sync(client, window);

            int pixmap = client.allocateId();
            client.createPixmap(pixmap, client.root(), 100, 100, 24);
            int gc = client.createGC(client.allocateId(), client.root(), GREEN);
            client.fillRectangle(pixmap, gc, 0, 0, 100, 100);
            client.copyArea(pixmap, window, gc, 0, 0, 0, 0, 100, 100);
            sync(client, window);

            waitForColor(650, 50, GREEN, "the copy from the pixmap reached the screen");
            assertEquals(GREEN, client.pixelAt(window, 50, 50),
                    "and it is in the window's own pixels, so a read answers with it");
        }
    }

    // ------------------------------------------------------------------

    /**
     * Waits for the server to have processed everything sent so far, which a round trip does: the
     * reply to this request cannot come before the requests in front of it have run.
     */
    private static void sync(RawXClient client, int window) throws IOException {
        client.geometry(window);
    }

    /** The screen's pixel, which is where the composited result is read from. */
    private static int colorAt(int x, int y) {
        return server.screen().framebuffer().pixel(x, y) | 0xFF000000;
    }

    private static void waitForColor(int x, int y, int expected, String what) {
        long deadline = System.currentTimeMillis() + 4000;
        int last = 0;
        while (System.currentTimeMillis() < deadline) {
            last = colorAt(x, y);
            if (last == expected) {
                return;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        assertEquals(expected, last, what + " (at " + x + "," + y + ")");
    }
}
