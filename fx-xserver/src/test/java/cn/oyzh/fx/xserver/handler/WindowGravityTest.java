package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Window gravity: where a child goes when its parent is resized.
 *
 * <p>{@code winGravity} is the half of window gravity the *server* applies - the other, {@code
 * bitGravity}, is about the window's own contents when it is resized - and this server stored it,
 * reported it, and never used it. A window's position is relative to its parent, so a parent that
 * grows leaves every child where it was unless something moves it, and a toolkit that pins a child to
 * the right-hand edge asks for exactly that by setting the gravity.
 *
 * <p>Every number here was measured against a reference server with {@code tools/GravityProbe}, one
 * gravity at a time, and the whole three-by-three grid is pinned rather than the interesting corners:
 * the two that are easy to get wrong are the halves - {@code North} moves a child by *half* the width
 * change and {@code South} by all of it - and a test that only checked {@code East} would pass with
 * the vertical half wrong.
 *
 * <p>The reference moves the child silently, with no {@code ConfigureNotify}; this server does too,
 * and hands back the area the child left, which is what the second test pins.
 */
class WindowGravityTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 80;

    /** Grow the parent by this much: a width and a height whose halves are whole numbers. */
    private static final int GROW_WIDTH = 200;
    private static final int GROW_HEIGHT = 100;

    /**
     * X's gravity numbers, and how far each moves the child when the parent grows by
     * {@link #GROW_WIDTH} x {@link #GROW_HEIGHT}: keeping the distance to an edge means moving by the
     * *whole* change, centring by half of it, and the near edges by none of it.
     */
    private static final int[][] GRAVITY_SHIFT = {
            {1, 0, 0},                      // NorthWest keeps the distance to both near edges
            {2, GROW_WIDTH / 2, 0},         // North centres horizontally
            {3, GROW_WIDTH, 0},             // NorthEast keeps the distance to the right edge
            {4, 0, GROW_HEIGHT / 2},        // West centres vertically
            {5, GROW_WIDTH / 2, GROW_HEIGHT / 2},   // Center
            {6, GROW_WIDTH, GROW_HEIGHT / 2},       // East
            {7, 0, GROW_HEIGHT},            // SouthWest
            {8, GROW_WIDTH / 2, GROW_HEIGHT},       // South centres horizontally
            {9, GROW_WIDTH, GROW_HEIGHT},   // SouthEast
    };

    /** The window whose own contents the bit-gravity test resizes, and the marker drawn into it. */
    private static final int WIDTH = 60;
    private static final int HEIGHT = 40;
    private static final int MARKER = 6;
    private static final int MARKER_COLOUR = 0x00FF00;

    /**
     * The same table for {@code bitGravity}: how far a window's <em>own</em> contents move when the
     * window is grown, which is the same three by three grid applied to pixels - plus {@code Forget}
     * (0), which is not a shift but the instruction to throw the contents away.
     */
    private static final int[][] BIT_GRAVITY_SHIFT = {
            {0, -1, -1},                    // Forget: nothing survives, so nothing to check a place for
            {1, 0, 0},
            {2, GROW_WIDTH / 2, 0},
            {3, GROW_WIDTH, 0},
            {4, 0, GROW_HEIGHT / 2},
            {5, GROW_WIDTH / 2, GROW_HEIGHT / 2},
            {6, GROW_WIDTH, GROW_HEIGHT / 2},
            {7, 0, GROW_HEIGHT},
            {8, GROW_WIDTH / 2, GROW_HEIGHT},
            {9, GROW_WIDTH, GROW_HEIGHT},
            {10, 0, 0},                     // Static: the contents are not moved
    };

    private XServer server;
    private RawXClient client;
    private int parent;

    @BeforeEach
    void startServerAndParent() throws Exception {
        // Single window, so the pixels the bit-gravity test reads are on the screen it reads them
        // from: in the default mode every connection gets a screen of its own and this one would be
        // empty however well the client drew.
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix", "--single-window"}));
        server.start();
        client = RawXClient.connect(DISPLAY);
        parent = client.allocateId();
        client.createWindow(parent, client.root(), 0, 0, 300, 300);
        client.mapWindow(parent);
        client.flush();
    }

    @AfterEach
    void stopServer() throws Exception {
        if (client != null) {
            client.close();
        }
        server.stop();
    }

    /**
     * The whole grid: each of the nine gravities moves the child by the share of the parent's change
     * its name promises, and the two that ask for no movement - {@code Forget} and {@code Static} -
     * move it not at all.
     */
    @Test
    void eachGravityMovesAChildByItsOwnShareOfTheParentsChange() throws Exception {
        for (int[] row : GRAVITY_SHIFT) {
            int gravity = row[0];
            int child = client.allocateId();
            client.createWindowWithGravity(child, parent, 150, 150, 100, 100, 1, gravity);
            client.mapWindow(child);
            client.flush();

            client.configureWindow(parent, 0, 0, 300 + GROW_WIDTH, 300 + GROW_HEIGHT);
            client.flush();
            client.geometry(parent);                    // a round trip, so the replies stay in step

            int[] geometry = client.geometry(child);
            assertEquals(150 + row[1], geometry[0],
                    "gravity " + gravity + " moved the child horizontally by the wrong share");
            assertEquals(150 + row[2], geometry[1],
                    "gravity " + gravity + " moved the child vertically by the wrong share");
            assertEquals(100, geometry[2], "a gravity move is a move, not a resize");

            client.destroyWindow(child);
            client.configureWindow(parent, 0, 0, 300, 300);
            client.flush();
            client.geometry(parent);
        }
    }

    /**
     * The other half of the item: a window's own contents follow its {@code bitGravity} when the
     * window is resized - the same grid as above, applied to pixels instead of to a child's position,
     * and {@code Forget} which throws them away rather than moving them.
     *
     * <p>Every case was measured against a reference server with {@code tools/BitGravityProbe} - a
     * marker near each corner of a window, the window grown, the window read back - and the two that
     * are easy to get wrong are the same halves: {@code North} moves the contents by half the width
     * change, and {@code Static} does not move them at all.
     */
    @Test
    void aResizeMovesAWindowsOwnContentsByItsBitGravity() throws Exception {
        for (int[] row : BIT_GRAVITY_SHIFT) {
            int gravity = row[0];
            int window = client.allocateId();
            int gc = client.allocateId();
            client.createWindowWithGravity(window, client.root(), 10, 10, WIDTH, HEIGHT, gravity, 1);
            client.createGC(gc, client.root(), MARKER_COLOUR);
            client.mapWindow(window);
            client.fillRectangle(window, gc, 1, 1, MARKER, MARKER);
            client.queryTree(window);                   // a round trip: the marker is drawn

            client.configureWindow(window, 10, 10, WIDTH + GROW_WIDTH, HEIGHT + GROW_HEIGHT);
            client.queryTree(window);

            if (gravity == 0) {
                // Forget: the measured behaviour is that the marker is simply gone, and the window is
                // its own background afterwards.
                for (int y = 0; y < HEIGHT + GROW_HEIGHT; y++) {
                    for (int x = 0; x < WIDTH + GROW_WIDTH; x++) {
                        assertNotEquals(MARKER_COLOUR,
                                server.screen().framebuffer().pixel(10 + x, 10 + y) & 0xFFFFFF,
                                "Forget threw the contents away, so no ink survives at " + x + "," + y);
                    }
                }
                client.destroyWindow(window);
                continue;
            }
            int x = 10 + 1 + row[1];
            int y = 10 + 1 + row[2];
            assertEquals(MARKER_COLOUR, server.screen().framebuffer().pixel(x, y) & 0xFFFFFF,
                    "bit gravity " + gravity + " moved the window's own contents to the wrong place");
            if (row[1] > 0 || row[2] > 0) {
                // What it moved away from is background: the area a shift leaves is painted and
                // exposed, exactly as a reveal leaves it.
                assertNotEquals(MARKER_COLOUR,
                        server.screen().framebuffer().pixel(10 + WIDTH - 2, 10 + HEIGHT - 2) & 0xFFFFFF,
                        "the pixel the marker left behind is still ink for bit gravity " + gravity);
            }
            client.destroyWindow(window);
        }
    }

    /**
     * The two gravities that mean "do not move": {@code Forget} (0) and {@code Static} (10), which are
     * outside the three by three grid. (A window nobody sets a gravity on has {@code NorthWest}, which
     * is where a window starts: measured against a reference server with {@code GetWindowAttributes},
     * which answers 1 for a freshly created window.)
     */
    @Test
    void forgetAndStaticLeaveTheChildWhereItIs() throws Exception {
        for (int gravity : new int[] {0, 10}) {
            int child = client.allocateId();
            client.createWindowWithGravity(child, parent, 150, 150, 100, 100, 1, gravity);
            client.mapWindow(child);
            client.flush();

            client.configureWindow(parent, 0, 0, 300 + GROW_WIDTH, 300 + GROW_HEIGHT);
            client.flush();
            client.geometry(parent);

            int[] geometry = client.geometry(child);
            assertEquals(150, geometry[0], "gravity " + gravity + " must not move the child");
            assertEquals(150, geometry[1], "gravity " + gravity + " must not move the child");

            client.destroyWindow(child);
            client.configureWindow(parent, 0, 0, 300, 300);
            client.flush();
            client.geometry(parent);
        }
    }

    /**
     * A move is not a resize, and only a resize moves the children: the gravity is about the parent's
     * *size*, so a parent that slides across the screen takes its children with it and does not
     * recompute their gravity for a change that did not happen.
     */
    @Test
    void movingTheParentDoesNotMoveTheChildByItsGravity() throws Exception {
        int child = client.allocateId();
        client.createWindowWithGravity(child, parent, 150, 150, 100, 100, 1, 9);   // SouthEast
        client.mapWindow(child);
        client.flush();

        client.configureWindow(parent, 40, 40, 300, 300);
        client.flush();
        client.geometry(parent);

        int[] geometry = client.geometry(child);
        assertEquals(150, geometry[0], "the parent moved without changing size");
        assertEquals(150, geometry[1], "the parent moved without changing size");
    }
}
