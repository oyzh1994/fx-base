package cn.oyzh.fx.xserver.test;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.CursorSprite;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.Screen;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One client connection, one window: each forwarded application gets a screen of its own, a window
 * sized to what it draws, and no way to disturb another client's.
 *
 * <p>This is what makes a forwarded application look like a window rather than a corner of one big
 * X screen, and it is also what keeps two applications from drawing over each other: two clients on
 * two screens share no pixels at all, where two clients on one screen would fight over the same
 * framebuffer.
 */
class PerClientWindowTest {

    /** Display numbers nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 92;

    private static final int EXPOSURE_MASK = 1 << 15;
    private static final int RED = 0xFFCC2222;
    private static final int BLUE = 0xFF2233CC;

    @Test
    void eachConnectionGetsItsOwnScreenAndItsOwnWindow() throws Exception {
        Server server = new Server(DISPLAY);
        try (RawXClient first = RawXClient.connect(DISPLAY);
             RawXClient second = RawXClient.connect(DISPLAY)) {
            int firstWindow = first.allocateId();
            int secondWindow = second.allocateId();

            first.createWindow(firstWindow, first.root(), 0, 0, 200, 100);
            first.mapWindow(firstWindow);
            first.fillRectangle(firstWindow, first.createGC(first.allocateId(), first.root(), RED), 0, 0, 200, 100);
            first.flush();

            second.createWindow(secondWindow, second.root(), 0, 0, 400, 300);
            second.mapWindow(secondWindow);
            second.fillRectangle(secondWindow, second.createGC(second.allocateId(), second.root(), BLUE), 0, 0,
                    400, 300);
            second.flush();

            // Two roots, and each client's window is on its own.
            assertNotEquals(first.root(), second.root(), "each connection needs its own root window");
            assertEquals(first.root(), first.rootOf(firstWindow),
                    "the first client's window belongs to the first client's root");
            assertEquals(second.root(), second.rootOf(secondWindow),
                    "and the second's to the second's");
            // The display's own EWMH check window ({@code _NET_SUPPORTING_WM_CHECK}) is a child of the
            // root as well - chrome the display publishes on every screen, not a client's window - so
            // the root holds it and the first client's window, and none of the second's. It is made
            // before the client connects, which is why it comes first in the stacking order.
            assertEquals(List.of(wmCheckWindow(first), firstWindow), List.of(childrenOf(first)),
                    "the first client's root holds its own window and none of the other's");

            // Two screens means two framebuffers: the same coordinates hold each client's own pixels.
            assertEquals(RED, pixelAt(first, firstWindow, 10, 10),
                    "the first screen should hold the first client's window");
            assertEquals(BLUE, pixelAt(second, secondWindow, 10, 10),
                    "and the second screen the second's");

            waitUntil(() -> server.windows.size() == 2, "each connection should open a window");
            TestBackend firstBackend = server.windows.get(0);
            TestBackend secondBackend = server.windows.get(1);
            assertNotEquals(firstBackend.screen, secondBackend.screen, "one screen per window");
            assertEquals(new Rectangle(0, 0, 200, 100), firstBackend.viewport,
                    "the window is sized to the client's window, not to the whole screen");
            assertEquals(new Rectangle(0, 0, 400, 300), secondBackend.viewport);
        } finally {
            server.stop();
        }
    }

    /**
     * A second connection that maps anything gets a window of its own - which is the point of this
     * mode, and is also what an input method cannot live with.
     *
     * <p>An X input method (fcitx, ibus) is an X client like any other: it is found through the
     * {@code XIM_SERVERS} selection, it talks to the application with {@code ClientMessage}s and
     * window properties, and it draws its pre-edit text and its candidate list in a window of its
     * own, positioned over the application's window. Every one of those works across two screens -
     * selections, properties and sent events are all server-wide - except the last one: the input
     * method's window lands on the input method's screen, so it is shown in the input method's own
     * host window rather than over the application. The same is true of anything else that is
     * infrastructure rather than an application: a window manager, a clipboard manager, a drag-and-
     * drop source.
     */
    @Test
    void aClientThatIsNotAnApplicationGetsAWindowOfItsOwnToo() throws Exception {
        Server server = new Server(DISPLAY);
        try (RawXClient application = RawXClient.connect(DISPLAY);
             RawXClient inputMethod = RawXClient.connect(DISPLAY)) {
            int appWindow = application.allocateId();
            application.createWindow(appWindow, application.root(), 0, 0, 400, 300);
            application.mapWindow(appWindow);
            application.flush();
            waitUntil(() -> server.windows.size() == 1, "the application's window should open");

            // The input method's pre-edit window: override-redirect, put over the application's
            // window, which is exactly what an input method does with it.
            int preedit = inputMethod.allocateId();
            inputMethod.createWindow(preedit, inputMethod.root(), 100, 100, 200, 40);
            inputMethod.setOverrideRedirect(preedit);
            inputMethod.mapWindow(preedit);
            inputMethod.flush();
            waitUntil(() -> server.windows.size() == 2,
                    "the input method gets a host window of its own, which is where its pre-edit "
                            + "text ends up - not over the application");

            // And it is a window showing the input method's screen, so nothing of the application
            // is in it: the two clients cannot see each other's pixels at all.
            assertNotSame(server.windows.get(1).screen, server.windows.get(0).screen,
                    "two connections, two screens - so the pre-edit window cannot appear over the "
                            + "application it belongs to");
        } finally {
            server.stop();
        }
    }

    @Test
    void theWindowFollowsWhatTheClientDraws() throws Exception {
        Server server = new Server(DISPLAY);
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 200, 100);
            client.mapWindow(window);
            client.flush();
            waitUntil(() -> server.windows.size() == 1, "the window should have opened");
            TestBackend backend = server.windows.get(0);
            assertEquals(new Rectangle(0, 0, 200, 100), backend.viewport);

            // A resize moves the window with it, which is what keeps a resized application fully
            // visible instead of cropped to the size it first appeared at.
            client.configureWindow(window, 10, 20, 300, 150);
            client.flush();
            waitUntil(() -> new Rectangle(10, 20, 300, 150).equals(backend.viewport),
                    "the window should follow the client's resize, saw " + backend.viewport);

            // Unmapping everything leaves nothing to show: the window keeps its last frame.
            client.destroyWindow(window);
            client.flush();
            waitUntil(() -> backend.idle.get(), "a client with no windows has nothing to show");
        } finally {
            server.stop();
        }
    }

    @Test
    void movingThePointerPresentsAFrameEvenWhenTheClientDrawsNothing() throws Exception {
        Server server = new Server(DISPLAY);
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 200, 100);
            client.mapWindow(window);
            client.flush();
            waitUntil(() -> server.windows.size() == 1, "the window should have opened");
            TestBackend backend = server.windows.get(0);
            int xtest = client.queryExtension("XTEST");

            // Nothing is drawn from here on: the only thing that changes is where the pointer is,
            // and the frame that carries it has to be built for that alone. When the sprite's
            // bookkeeping sat behind the "is there any damage" test, a pointer moving over a window
            // whose client was not repainting produced no damage and so no frame, and the cursor
            // only caught up whenever the client happened to draw - a stutter.
            waitUntil(() -> backend.frames.get() > 0, "the first frame never arrived");
            Thread.sleep(200);
            int settled = backend.frames.get();
            Thread.sleep(200);
            assertEquals(settled, backend.frames.get(),
                    "frames kept coming while nothing at all was changing");

            // One pointer move and nothing else: the frame that carries it has to be built for that
            // alone, with the client drawing nothing.
            client.fakeMotion(xtest, 60, 60);
            waitUntil(() -> backend.frames.get() > settled,
                    "a pointer move over an idle client produced no frame");
        } finally {
            server.stop();
        }
    }

    @Test
    void oneConnectionEndingLeavesTheOthersAlone() throws Exception {
        Server server = new Server(DISPLAY);
        try (RawXClient first = RawXClient.connect(DISPLAY)) {
            int firstWindow = first.allocateId();
            first.createWindow(firstWindow, first.root(), 0, 0, 100, 100);
            first.mapWindow(firstWindow);
            first.flush();
            waitUntil(() -> server.windows.size() == 1, "the first window should have opened");

            RawXClient second = RawXClient.connect(DISPLAY);
            int secondWindow = second.allocateId();
            second.createWindow(secondWindow, second.root(), 0, 0, 100, 100);
            second.mapWindow(secondWindow);
            second.flush();
            waitUntil(() -> server.windows.size() == 2, "the second window should have opened");

            second.close();

            // Only the window of the client that went away is closed; the other keeps drawing.
            waitUntil(() -> server.windows.get(1).stopped.get(),
                    "the window of the client that disconnected was left open");
            TestBackend survivor = server.windows.get(0);
            assertTrue(!survivor.stopped.get(), "the surviving client's window was closed as well");
        } finally {
            server.stop();
        }
    }

    // ------------------------------------------------------------------
    // The server under test
    // ------------------------------------------------------------------

    /** Runs a server whose windows are backends a test can inspect instead of real windows. */
    private static final class Server {

        final XServer xserver;
        final List<TestBackend> windows = new CopyOnWriteArrayList<>();

        Server(int display) throws Exception {
            this.xserver = new XServer(ServerOptions.parse(new String[]{"--display", ":" + display,
                    "--backend", "headless", "--no-unix"}));
            xserver.setBackendFactory(() -> {
                TestBackend backend = new TestBackend();
                windows.add(backend);
                return backend;
            });
            xserver.start();
            xserver.startEventLoop();
        }

        void stop() {
            xserver.stop();
        }
    }

    /** A window that records what it is asked to show. */
    private static final class TestBackend implements DisplayBackend {
        final AtomicBoolean stopped = new AtomicBoolean();
        final AtomicBoolean idle = new AtomicBoolean();
        final AtomicInteger frames = new AtomicInteger();
        volatile Screen screen;
        volatile Rectangle viewport = new Rectangle();

        @Override
        public String name() {
            return "test";
        }

        @Override
        public void start(Screen screen, int displayNumber, InputSink input) {
            this.screen = screen;
        }

        @Override
        public void setViewport(Rectangle viewport) {
            this.viewport = new Rectangle(viewport);
        }

        @Override
        public void present(Screen screen, Rectangle damage, CursorSprite cursor) {
            frames.incrementAndGet();
        }

        @Override
        public void setIdle(boolean idle) {
            this.idle.set(idle);
        }

        @Override
        public void stop() {
            stopped.set(true);
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** The root's children as the client sees them, which is how a screen stays private. */
    private static Integer[] childrenOf(RawXClient client) throws Exception {
        int[] children = client.queryTree(client.root());
        Integer[] boxed = new Integer[children.length];
        for (int i = 0; i < children.length; i++) {
            boxed[i] = children[i];
        }
        return boxed;
    }

    /** The window the root points at as its window manager, which the display publishes itself. */
    private static int wmCheckWindow(RawXClient client) throws Exception {
        int atom = client.internAtom("_NET_SUPPORTING_WM_CHECK");
        return RawXClient.readInt(client.getProperty(client.root(), atom, 0, false), 32);
    }

    /** The colour of a pixel of a client's own screen, read back as ZPixmap bytes (BGRA). */
    private static int pixelAt(RawXClient client, int drawable, int x, int y) throws Exception {
        byte[] reply = client.getImage(drawable, x, y, 1, 1);
        int offset = 32;
        return 0xFF000000 | ((reply[offset + 2] & 0xFF) << 16) | ((reply[offset + 1] & 0xFF) << 8)
                | (reply[offset] & 0xFF);
    }

    private static void waitUntil(BooleanSupplier condition, String message) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError(message);
    }
}
