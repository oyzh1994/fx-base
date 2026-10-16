package cn.oyzh.fx.xserver.input;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.CursorSprite;
import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The pointer sprite belongs to the display backend, not to the framebuffer.
 *
 * <p>Painting it into the framebuffer and saving the pixels underneath is what left a trail of
 * patches wherever the pointer had been: a client that drew over the sprite made the saved copy
 * stale, and the next pointer move put that stale copy back. Keeping it out also means a client's
 * own pixels are never covered by it, and {@code GetImage} never reads it back.
 */
public class CursorSpriteTest {

    @Test
    void spriteIsTransparentAndLeavesTheFramebufferAlone() {
        // No backend is started, so this runs headless wherever the tests do.
        XServer server = new XServer(ServerOptions.parse(
                new String[]{"--display", "98", "--backend", "headless"}));
        Framebuffer framebuffer = server.screen().framebuffer();
        int before = framebuffer.pixel(40, 40);

        // Until something says where the pointer is there is no sprite to draw: a window that opens
        // under a pointer which never moves would otherwise show one in its corner, next to the
        // platform's own arrow.
        assertNull(server.input().cursorSprite(), "no position means no sprite");

        server.input().pointerMoved(40, 40);
        CursorSprite sprite = server.input().cursorSprite();

        assertNotNull(sprite, "the root cursor is always set");
        assertEquals(40, sprite.x(), "the default cursor's hotspot is its top left corner");
        assertEquals(40, sprite.y());

        // The default cursor is an arrow: everything but the arrow itself is transparent, and
        // forcing those pixels opaque is what makes it arrive as a black square.
        int transparent = 0;
        for (int pixel : sprite.pixels()) {
            if ((pixel >>> 24) == 0) {
                transparent++;
            }
        }
        assertTrue(transparent > sprite.pixels().length / 2,
                "most of a cursor is transparent, but only " + transparent + " of "
                        + sprite.pixels().length + " pixels are");

        assertEquals(before, framebuffer.pixel(40, 40),
                "the sprite must not be written into the framebuffer");
    }

    /**
     * {@code XFixesHideCursor}/{@code ShowCursor}: the sprite is not drawn while the pointer is in
     * the window that asked, and is drawn again when the hides are given back.
     *
     * <p>These two used to only count - {@code InputState} drew the sprite unconditionally, so a
     * client that hid the pointer (a video player, a viewer that wants its content unobstructed) got
     * the pointer anyway. The rules asserted here are the ones the extension defines and the reason
     * the count is per window: hiding the pointer while it is over <em>this</em> window must not
     * hide it over the rest of the screen, and "in this window" includes everything inside it -
     * an application hides the pointer on its toplevel while the pointer is over a child, and one
     * that hides it on a single child still shows it over the parent.
     */
    @Test
    void aHiddenCursorIsNotDrawnInTheWindowThatHidIt() throws Exception {
        XServer server = new XServer(ServerOptions.parse(new String[]{"--display", "63",
                "--backend", "headless", "--no-unix", "--single-window"}));
        server.start();
        try (RawXClient client = RawXClient.connect(63)) {
            int parent = client.allocateId();
            client.createWindow(parent, client.root(), 100, 100, 300, 300, 0xFFFFFF);
            client.mapWindow(parent);
            int child = client.allocateId();
            client.createWindow(child, parent, 20, 20, 100, 100, 0xFFFFFF);
            client.mapWindow(child);
            client.queryInputFocus();               // a round trip: the windows are up

            int xfixes = client.queryExtension("XFIXES");
            assertTrue(xfixes != 0, "the extension this test is about");

            // In the child, hiding on the child: hidden there and only there.
            client.warpPointer(child, 5, 5);
            client.queryInputFocus();
            assertNotNull(server.input().cursorSprite(), "a warp puts the sprite in the child");
            client.hideCursor(xfixes, child);
            client.queryInputFocus();
            assertNull(server.input().cursorSprite(), "the window that hid it is where it is hidden");

            // In the parent, with the hide still on the child: shown, because the hide is the
            // child's and the pointer is not in the child any more.
            client.warpPointer(parent, 5, 5);
            client.queryInputFocus();
            assertNotNull(server.input().cursorSprite(),
                    "a hide on one window must not hide the pointer over another");

            // A hide on the parent, with the pointer in the child: hidden, because "in this window"
            // covers everything inside it.
            client.hideCursor(xfixes, parent);
            client.warpPointer(child, 5, 5);
            client.queryInputFocus();
            assertNull(server.input().cursorSprite(), "the pointer is inside the parent");

            // The hides are counted: the child's is still outstanding, so one show of the parent
            // leaves the pointer hidden in the child.
            client.showCursor(xfixes, parent);
            client.queryInputFocus();
            assertNull(server.input().cursorSprite(),
                    "two hides were made and only one given back");

            client.showCursor(xfixes, child);
            client.queryInputFocus();
            assertNotNull(server.input().cursorSprite(), "every hide was given back");
        } finally {
            server.stop();
        }
    }

    /** {@code ShowCursor} on a window that never hid anything is not a credit to spend. */
    @Test
    void showingMoreThanWasHiddenIsNotRemembered() throws Exception {
        XServer server = new XServer(ServerOptions.parse(new String[]{"--display", "73",
                "--backend", "headless", "--no-unix", "--single-window"}));
        server.start();
        try (RawXClient client = RawXClient.connect(73)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 50, 50, 200, 200, 0xFFFFFF);
            client.mapWindow(window);
            client.warpPointer(window, 5, 5);
            client.queryInputFocus();

            int xfixes = client.queryExtension("XFIXES");
            client.showCursor(xfixes, window);      // never hidden
            client.showCursor(xfixes, window);
            client.hideCursor(xfixes, window);
            client.queryInputFocus();
            assertNull(server.input().cursorSprite(),
                    "a stream of shows is not a balance to spend against a later hide");

            client.showCursor(xfixes, window);
            client.queryInputFocus();
            assertNotNull(server.input().cursorSprite(), "one hide, one show");
        } finally {
            server.stop();
        }
    }
}
