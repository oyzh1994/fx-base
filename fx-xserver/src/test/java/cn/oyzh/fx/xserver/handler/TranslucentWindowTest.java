package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A window whose visual has an alpha channel keeps its own pixels and is composited (B5).
 *
 * <p>This server draws every window straight into the screen's framebuffer, which has <b>no alpha</b>
 * - deliberately, because the raster operations can produce any 32 bit value and a backend that
 * honoured alpha would draw {@code GXclear}'s zeros as holes. So a client that asked for the 32 bit
 * ARGB visual and drew something translucent got it painted opaquely: the visual was advertised and
 * meant nothing.
 *
 * <p>What is pinned here is the fix and its extent. A window with an alpha visual draws into a store
 * of its own - in its own coordinates, the whole of it rather than only the part that shows - and what
 * reaches the screen is that store composited over what is under it, through the same blend every
 * other composite in this server uses.
 *
 * <p>The store is now <b>every</b> window's, not only an alpha window's (B5's general half): an
 * ordinary window keeps its pixels too, and is composited from them with a straight copy rather than a
 * blend, because its store is opaque like the screen. The tests below cover both, and the ordinary
 * window's case is the one C8 needs - a frame per top level can only show a window's own pixels if
 * there are some.
 */
class TranslucentWindowTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 89;

    // Opaque, because that is what the screen holds: a screen has no alpha and every pixel read
    // from it carries a full one.
    private static final int RED = 0xFFFF0000;
    private static final int BLUE = 0xFF0000FF;
    private static final int GREEN = 0xFF00FF00;

    /**
     * The same blue as a <em>visual</em> pixel rather than as ARGB, which is what {@code
     * CreateWindow}'s background attribute carries: a 24 bit visual has no alpha, so its pixels are
     * {@code 0xRRGGBB}. Passing the ARGB form here would read as a negative int and the client
     * helper would send no background at all - which is how this test first measured a grey window.
     */
    private static final int BLUE_RGB = 0x0000FF;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        // --single-window so the client's windows are on the screen these tests read pixels from.
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
    void aTranslucentFillsAlphaReachesTheScreen() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            // An alpha window with a *transparent* background: nothing of it is painted, so the
            // screen keeps whatever was there - which is the screen's own colour, this being the
            // master screen of single-window mode.
            int window = client.allocateId();
            client.createWindowWithVisual(window, client.root(), 0, 0, 100, 100, 32, alphaVisual(),
                    0x00000000);
            client.mapWindow(window);
            sync(client, window);
            assertEquals(SCREEN_BACKGROUND, colorAt(10, 10),
                    "a window with a transparent background paints nothing at all");

            // Half of it half-transparent blue: over the screen's grey that is neither of the two,
            // and every channel is the weighted average of them.
            // The GC is created on the *window*, which is what a real client does and the only way
            // the alpha survives: a context converts its colours in the visual of the drawable it was
            // created on, and the root's visual has no alpha to convert with.
            int halfBlue = client.createGC(client.allocateId(), window, 0x800000FF);
            client.fillRectangle(window, halfBlue, 0, 0, 50, 100);
            sync(client, window);
            int blended = colorAt(10, 10);
            assertTrue(blended != SCREEN_BACKGROUND && blended != BLUE,
                    "a half transparent fill is neither what was under it nor its own colour: 0x"
                            + Integer.toHexString(blended));
            // Half of the destination's red plus half of the source's none: the source-over
            // arithmetic, on the screen's own colour. Without the store this came out as the
            // source's colour alone - the alpha was dropped before anything composited it.
            assertEquals((SCREEN_BACKGROUND >> 16 & 0xFF) / 2, red(blended), 1,
                    "and the red it let through is half of the grey that was there - the alpha"
                            + " reached the screen, which it could not before this store existed");

            // And an opaque fill in the same window is simply its colour.
            int green = client.createGC(client.allocateId(), window, 0x00000000 | 0xFF00FF00);
            client.fillRectangle(window, green, 50, 0, 50, 100);
            sync(client, window);
            assertEquals(GREEN, colorAt(60, 10), "an opaque fill in an alpha window is its colour");
        }
    }

    @Test
    void aCoveredWindowWithItsOwnPixelsKeepsThem() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int below = client.allocateId();
            client.createWindow(below, client.root(), 300, 0, 200, 200);
            client.mapWindow(below);
            int red2 = client.createGC(client.allocateId(), client.root(), RED);
            client.fillRectangle(below, red2, 0, 0, 200, 200);
            int alpha = client.allocateId();
            client.createWindowWithVisual(alpha, client.root(), 300, 0, 100, 100, 32, alphaVisual(),
                    0x00000000);
            client.mapWindow(alpha);
            sync(client, alpha);
            int green = client.createGC(client.allocateId(), alpha, 0xFF000000 | GREEN);
            client.fillRectangle(alpha, green, 0, 0, 100, 100);
            sync(client, alpha);
            waitForColor(310, 10, GREEN, "the window's own pixels should be composited");

            // Something opaque over it, and then gone again.
            int cover = client.allocateId();
            client.createWindow(cover, client.root(), 300, 0, 200, 200);
            client.mapWindow(cover);
            int blueCover = client.createGC(client.allocateId(), client.root(), BLUE);
            client.fillRectangle(cover, blueCover, 0, 0, 200, 200);
            sync(client, cover);
            waitForColor(310, 10, BLUE, "the covering window should be painted over it");

            client.destroyWindow(cover);
            sync(client, below);
            waitForColor(310, 10, GREEN, "and uncovering it puts its own pixels back, with no expose"
                    + " for a client that never lost them");
        }
    }

    @Test
    void anOrdinaryWindowKeepsItsPixelsToo() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 600, 0, 50, 50);
            client.mapWindow(window);
            int green2 = client.createGC(client.allocateId(), client.root(), GREEN);
            client.fillRectangle(window, green2, 0, 0, 50, 50);
            sync(client, window);
            waitForColor(610, 10, GREEN, "an ordinary window is painted as it always was");

            // The store every window now has (B5's general half). Its pixels are its own, so a frame
            // per top level can show them (C8) - but the store is opaque, like the screen, because an
            // ordinary visual has no alpha to keep and the raster operations can produce any 32 bit
            // value.
            cn.oyzh.fx.xserver.resource.XWindow resource =
                    (cn.oyzh.fx.xserver.resource.XWindow) server.resources()
                            .get(window);
            assertTrue(resource.keepsOwnPixels(), "every window but the root keeps its pixels");
            assertFalse(resource.ownPixels().keepsAlpha(),
                    "an ordinary visual has no alpha, so the store is opaque like the screen");

            // And what it draws while something covers it is still there when it is uncovered.
            int cover = client.allocateId();
            client.createWindow(cover, client.root(), 600, 0, 50, 50, BLUE_RGB);
            client.mapWindow(cover);
            sync(client, cover);
            assertEquals(BLUE, client.pixelAt(cover, 10, 10),
                    "the covering window's own store holds the background it was created with");
            waitForColor(610, 10, BLUE, "the covering window is over it");

            int red3 = client.createGC(client.allocateId(), window, RED);
            client.fillRectangle(window, red3, 0, 0, 50, 50);
            sync(client, window);

            client.destroyWindow(cover);
            sync(client, window);
            waitForColor(610, 10, RED,
                    "what it drew while it was covered is in its store and shows when it is exposed");
        }
    }

    @Test
    void theStoreIsAWindowAndNotAScreen() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindowWithVisual(window, client.root(), 900, 0, 40, 30, 32, alphaVisual(),
                    0x00000000);
            client.mapWindow(window);
            sync(client, window);
            int blue = client.createGC(client.allocateId(), window, 0xFF000000 | BLUE);
            client.fillRectangle(window, blue, 0, 0, 40, 30);
            sync(client, window);
            waitForColor(910, 10, BLUE, "painted");

            cn.oyzh.fx.xserver.resource.XWindow resource =
                    (cn.oyzh.fx.xserver.resource.XWindow) server.resources().get(window);
            assertTrue(resource.keepsOwnPixels(), "an alpha visual keeps its pixels");
            assertEquals(40, resource.ownPixels().width(),
                    "the store is the window's own rectangle, not the screen's");
            assertEquals(30, resource.ownPixels().height());
            assertTrue(resource.ownPixels().keepsAlpha(), "and it is the store that keeps the alpha");
        }
    }

    /** The screen's own colour, which is what shows where nothing has been painted. */
    private static final int SCREEN_BACKGROUND = Screen.DEFAULT_BACKGROUND_ARGB;

    /** One channel of a colour, 0..255. */
    private static int red(int argb) {
        return (argb >> 16) & 0xFF;
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

    /** This server's 32 bit ARGB visual, from its own list rather than from a hard-coded id. */
    private static int alphaVisual() {
        for (VisualInfo visual : server.screen().visuals()) {
            if (visual.hasAlpha()) {
                return visual.id();
            }
        }
        throw new AssertionError("this server advertises a 32 bit ARGB visual");
    }
}
