package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every window in the tree is a resource a client can look up.
 *
 * <p>That is the invariant a client walks the tree on: {@code QueryTree} lists a window's children,
 * and then the client asks about each one - {@code GetGeometry}, {@code QueryTree} again,
 * {@code GetWindowAttributes}. The server's own windows are in that tree too: the bridges that carry
 * the clipboard, the tray and a host drag each create one as a child of the root, and those were made
 * in a bridge's *constructor* but registered in its {@code start()} - so a bridge that never started
 * left windows behind that belonged to nothing.
 *
 * <p>Measured against a real client, which is how this was found (2026-09-28): {@code xwininfo -root
 * -tree} printed {@code X Error: 3: Bad Window} for each of them and could not finish its walk, and
 * {@code xdotool search} would have had the same problem. The walk itself is what this test does, over
 * the wire, because that is the client's own path - an assertion on the resource table alone would not
 * have caught it, since the table is exactly what was missing.
 */
class WindowTreeTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 83;

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
     * A bridge that has been made but not started still has its window in the tree - and that window
     * is a resource from the moment it exists.
     *
     * <p>This is the bug, reproduced where it happened: each bridge made its window in its
     * constructor and registered it in {@code start()}, so a bridge that never started (a headless
     * server, {@code --no-clipboard}, {@code --no-tray}) left a window in the tree that the server
     * could not resolve. Both bridges are made here and deliberately **not** started, which is what
     * such a server looks like.
     */
    @Test
    void aWindowTooLargeToKeepPixelsKeepsNone() {
        // A store is the window's own size, and it was the one allocation in this server with no
        // budget: a pixmap is capped at 16M pixels and an image decode at 64M, but a client that
        // created a window and drew into it could ask for 65535x65535x4 bytes - 17 GB. Measured
        // 2026-10-01: a 12000x12000 window reached OutOfMemoryError inside Framebuffer.allocate
        // while it was being mapped, which killed that connection. The window now draws straight
        // onto the screen instead - the root's own path - so nothing about it is refused, and no
        // window that fits on the display is affected.
        assertTrue(XWindow.keepsStore(1300, 800), "an ordinary window");
        assertTrue(XWindow.keepsStore(8192, 8192), "the largest screen this server can be asked for:"
                + " a window that fits on the display always keeps its pixels");
        assertTrue(XWindow.keepsStore(65000, 1024), "a wide strip costs what it holds, not what it is");
        assertFalse(XWindow.keepsStore(8193, 8192), "one pixel over the budget is over");
        assertFalse(XWindow.keepsStore(12000, 12000), "the size that was measured to blow up");
        assertFalse(XWindow.keepsStore(65535, 65535), "and the largest a window can be asked for");
    }

    @Test
    void aBridgeThatNeverStartsStillRegistersItsWindow() throws Exception {
        cn.oyzh.fx.xserver.tray.TrayBridge tray =
                new cn.oyzh.fx.xserver.tray.TrayBridge(server);
        cn.oyzh.fx.xserver.drop.DropBridge drop =
                new cn.oyzh.fx.xserver.drop.DropBridge(server);
        int trayWindow = 0;
        int dropWindow = 0;
        for (int child : childrenOfTheRoot()) {
            if (tray.ownsWindow(child)) {
                trayWindow = child;
            } else {
                dropWindow = child;
            }
            assertNotNull(server.resources().get(child), "window 0x"
                    + Integer.toHexString(child) + " exists, so it is a resource");
        }
        assertNotEquals(0, trayWindow, "the tray's window is in the tree");
        assertNotEquals(0, dropWindow, "and so is the host drag's");
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            client.geometry(trayWindow);            // BadDrawable before the fix
            client.queryTree(trayWindow);           // BadWindow before it
            client.geometry(dropWindow);
            client.queryTree(dropWindow);
        }
    }

    @Test
    void everyChildOfTheRootCanBeQueried() throws Exception {
        new cn.oyzh.fx.xserver.tray.TrayBridge(server);
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] children = client.queryTree(client.root());
            assertTrue(children.length > 0,
                    "the bridges' windows are children of the root: this test is about walking them");
            for (int child : children) {
                assertNotNull(server.resources().get(child),
                        "window 0x" + Integer.toHexString(child) + " is in the tree, so it is a"
                                + " resource");
                // What a client actually does with it: geometry and its own children. Both used to
                // answer BadDrawable/BadWindow for the bridges' windows.
                assertTrue(client.geometry(child)[2] > 0,
                        "the geometry of child 0x" + Integer.toHexString(child) + " is readable");
                client.queryTree(child);
            }
        }
    }

    /** The walk is depth-first for a client, so the children of a child have to be walkable too. */
    @Test
    void theWholeTreeIsWalkable() throws Exception {
        new cn.oyzh.fx.xserver.drop.DropBridge(server);
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            walk(client, client.root(), 0);
        }
    }

    private int walk(RawXClient client, int window, int depth) throws IOException {
        assertTrue(depth < 8, "a tree this deep means something is reparenting in a loop");
        int seen = 1;
        for (int child : client.queryTree(window)) {
            client.geometry(child);
            seen += walk(client, child, depth + 1);
        }
        return seen;
    }

    private int[] childrenOfTheRoot() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            return client.queryTree(client.root());
        }
    }

    /** And the client can see the windows at all: they are the bridges', on the shared screen. */
    @Test
    void theBridgesWindowsAreTheOnesInTheTree() throws Exception {
        new cn.oyzh.fx.xserver.tray.TrayBridge(server);
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] children = client.queryTree(client.root());
            int owned = 0;
            for (int child : children) {
                XResource resource = server.resources().get(child);
                assertEquals(null, resource.owner(),
                        "a bridge's window belongs to the server, not to a client");
                owned++;
            }
            assertEquals(children.length, owned, "every child is one of the server's own windows");
        }
    }
}
