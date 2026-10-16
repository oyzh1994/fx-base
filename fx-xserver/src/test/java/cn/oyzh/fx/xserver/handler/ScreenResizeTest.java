package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The screen growing to fit a window dragged past it.
 *
 * <p>A client can only be told a window is as large as the framebuffer behind it, so when the user
 * drags a forwarded application's frame past the screen, the screen is what has to move first. The
 * size of a screen is therefore not fixed after all, which the rest of the server has to survive:
 * the pixels move into a larger array, the root window's geometry changes under the clients' feet,
 * and every rectangle computed from the screen - damage, the pointer clamp, a window's clip - has to
 * follow.
 *
 * <p>A drag is the only thing that grows a screen, so the tests drive one the way the host window
 * does: {@code InputSink.windowResized}, which the display tick picks up.
 */
class ScreenResizeTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 79;

    /** StructureNotifyMask: the mask a client watches the root's own geometry with. */
    private static final int STRUCTURE_NOTIFY = 0x20000;
    private static final int CONFIGURE_NOTIFY = 22;

    /**
     * A screen of its own per test, because a screen only ever grows: what one test drags it to
     * would be the floor the next one starts from, and a test that watched for "the screen got
     * bigger" would be watching something that had already happened.
     */
    private Server server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.xserver().stop();
        }
    }

    private void startServer() throws IOException {
        // A small screen and a low ceiling, so a test reaches the cap without allocating a desktop's
        // worth of pixels.
        server = new Server(new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "400x300", "--max-screen", "800x600", "--backend", "headless",
                "--single-window", "--no-unix"})));
        server.xserver.start();
        server.xserver.startEventLoop();
    }

    /** The server under test, so a test can reach the input a drag arrives on. */
    private record Server(XServer xserver) {
    }

    @Test
    void theScreenGrowsWithAWindowDraggedPastIt() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            mapWindow(client, 0, 0, 300, 200);
            assertEquals(400, width(client), "the screen starts at --geometry");

            drag(600, 500);
            eventually("the screen to grow", () -> width(client) == 600);

            eventually("both dimensions to follow the drag",
                    () -> width(client) == 600 && height(client) == 500);
            assertEquals(600, width(client));
            assertEquals(500, height(client));
        }
    }

    @Test
    void whatWasDrawnIsStillThereAfterTheScreenGrows() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = mapWindow(client, 0, 0, 100, 100);
            client.clearArea(window, 0, 0, 100, 100);
            client.flush();
            int drawn = client.pixelAt(client.root(), 50, 50);

            drag(500, 400);
            eventually("the screen to grow", () -> width(client) == 500);

            assertEquals(drawn, client.pixelAt(client.root(), 50, 50),
                    "the pixels moved into a larger array: growing a screen is not a new screen");
            // And the area it grew into is really there. The window was dragged to the new size, so
            // its own background reaches this corner - but only if the pixels behind the screen are
            // 500x400 as well: a screen that claimed that size over a 400x300 array would read this
            // out of the end of the array and answer with nothing at all.
            assertEquals(0xFF00FF00, client.pixelAt(client.root(), 480, 380),
                    "the screen grew into pixels it did not have");
        }
    }

    @Test
    void theScreenStopsAtMaxScreen() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            mapWindow(client, 0, 0, 200, 200);

            drag(4000, 4000);
            eventually("the screen to reach the ceiling", () -> height(client) == 600);

            assertEquals(800, width(client), "--max-screen is a ceiling, not a suggestion");
            assertEquals(600, height(client), "and the setup reply could not describe more anyway");
        }
    }

    @Test
    void theClientIsToldTheRootChangedSize() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = mapWindow(client, 0, 0, 200, 200);
            client.selectInput(client.root(), STRUCTURE_NOTIFY);
            client.flush();

            drag(700, 500);

            // Watched on the root, which is how a toolkit notices the display changed under it: there
            // is no RandR here, so this event is the only thing that says so.
            byte[] event = null;
            long deadline = System.currentTimeMillis() + 4000;
            client.readTimeoutMillis(500);
            try {
                while (event == null && System.currentTimeMillis() < deadline) {
                    byte[] next = client.readEvent();
                    if (next != null && (next[0] & 0x7F) == CONFIGURE_NOTIFY
                            && RawXClient.readInt(next, 4) == client.root()) {
                        event = next;
                    }
                }
            } finally {
                client.readTimeoutMillis(5000);
            }
            assertNotNull(event, "the root's geometry changed and nobody was told");
            // ConfigureNotify: the event window at 4, the window at 8, the above-sibling at 12, the
            // position at 16 and the size at 20 and 22.
            assertEquals(client.root(), RawXClient.readInt(event, 8));
            assertEquals(700, RawXClient.readShort(event, 20),
                    "the event carries the size the root has now");
            assertEquals(500, RawXClient.readShort(event, 22));
            assertNotNull(window);
        }
    }

    /**
     * A window the client mapped where the screen cannot show all of it, measured on the real
     * application: EasyShell centres a 1300x700 window on the 1280x800 screen it was told about, so
     * it lands at {@code -10+33} and ten pixels of it are off each side. Nothing can show a pixel at
     * a negative position, so the server puts the window on the screen - and then the screen is
     * widened to the window's far edge, which is the same rule a drag follows.
     */
    @Test
    void aWindowMappedOffTheLeftOfTheScreenIsPutOnItAndTheScreenFitsIt() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            // Wider than the 400x300 screen and starting off its left edge, which is the shape a
            // toolkit that centres a window larger than the display produces.
            int window = mapWindow(client, -50, 0, 500, 200);

            eventually("the window to be put back on the screen",
                    () -> client.geometry(window)[0] == 0);
            eventually("the screen to be widened to the window's far edge",
                    () -> width(client) == 500);
            assertEquals(0, client.geometry(window)[0], "ten pixels of it were off the left");
            assertEquals(300, height(client), "the window fits vertically: nothing to grow there");
            assertEquals(500, client.geometry(window)[2], "the client's own size is left alone");
        }
    }

    /** A window taller than the screen grows it downwards, the other half of the same rule. */
    @Test
    void aWindowTallerThanTheScreenGrowsItToo() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            mapWindow(client, 0, -20, 200, 400);

            eventually("the screen to be made tall enough", () -> height(client) == 400);
            assertEquals(400, width(client), "and no wider than it has to be");
        }
    }

    /** Both halves stop where every other growth does, at {@code --max-screen}. */
    @Test
    void aWindowBiggerThanMaxScreenIsStillPutOnTheScreen() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = mapWindow(client, -100, -100, 4000, 4000);

            eventually("the window to be put back on the screen",
                    () -> client.geometry(window)[0] == 0 && client.geometry(window)[1] == 0);
            eventually("the screen to reach the ceiling", () -> height(client) == 600);
            assertEquals(800, width(client), "--max-screen is a ceiling, not a suggestion");
            assertEquals(600, height(client));
        }
    }

    /**
     * The client is told, like any other configure: the application sees a ConfigureNotify carrying
     * the position the server chose, which is what a window manager's own placement sends.
     */
    @Test
    void theClientIsToldItsWindowWasMoved() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), -60, 0, 200, 200, 0x00FF00);
            client.selectInput(window, STRUCTURE_NOTIFY);
            client.mapWindow(window);
            client.flush();

            byte[] event = null;
            long deadline = System.currentTimeMillis() + 4000;
            client.readTimeoutMillis(500);
            try {
                while (event == null && System.currentTimeMillis() < deadline) {
                    byte[] next = client.readEvent();
                    if (next != null && (next[0] & 0x7F) == CONFIGURE_NOTIFY
                            && RawXClient.readInt(next, 8) == window) {
                        event = next;
                    }
                }
            } finally {
                client.readTimeoutMillis(5000);
            }
            assertNotNull(event, "the window was moved and nobody was told");
            assertEquals(0, RawXClient.readShort(event, 16), "the event carries the new position");
            assertEquals(0, RawXClient.readShort(event, 18));
        }
    }

    // ------------------------------------------------------------------
    // 辅助
    // ------------------------------------------------------------------

    /** The screen's size as a client sees it, which is the root window's geometry. */
    private static int width(RawXClient client) throws IOException {
        return client.geometry(client.root())[2];
    }

    private static int height(RawXClient client) throws IOException {
        return client.geometry(client.root())[3];
    }

    private static int mapWindow(RawXClient client, int x, int y, int w, int h) throws IOException {
        int window = client.allocateId();
        client.createWindow(window, client.root(), x, y, w, h, 0x00FF00);
        client.mapWindow(window);
        client.flush();
        return window;
    }

    /**
     * The user dragging the host window's edge, which is what the backend reports. In single-window
     * mode every client shares the master screen, so that screen's input is the one the drag lands
     * on.
     */
    private void drag(int width, int height) {
        server.xserver.inputFor(server.xserver.screen()).windowResized(width, height);
    }

    private static void eventually(String what, Check condition) {
        long deadline = System.currentTimeMillis() + 4000;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (condition.ok()) {
                    return;
                }
            } catch (IOException e) {
                throw new AssertionError(what, e);
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new AssertionError("timed out waiting for " + what);
    }

    private interface Check {
        boolean ok() throws IOException;
    }
}
