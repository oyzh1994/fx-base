package cn.oyzh.fx.xserver;

import cn.oyzh.fx.xserver.HostWindowStore;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.CursorSprite;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A client's window opens where the user left it, the next time that client is forwarded.
 *
 * <p>Nothing in the X protocol carries this: the position belongs to the host window, which is the
 * server's own, so it is remembered between runs - keyed by the client's {@code WM_CLASS}, written
 * when the tick sees the window hold still, and handed back to the backend when the next session
 * opens a window for the same client.
 *
 * <p>The backend is a test one, so what "opening at a position" means is observable: a real one would
 * move a real window, which a test cannot watch.
 */
class HostWindowPositionTest {

    @Test
    void aWindowOpensWhereItWasLeftLastTime(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("host-windows.properties");
        HostWindowStore store = HostWindowStore.at(file);

        try (Server first = new Server(80, store)) {
            first.client().mapWindowWithClass("Termora", "termora");
            // awaitInput and not just awaitWindow: the backend is handed to the session before it is
            // started, so a test that takes the window the moment it appears can call `input()` on
            // one that has no input sink yet. It passed for a week and failed once under a full
            // suite's load, which is what a race that is a few microseconds wide does.
            TestWindow window = first.awaitWindow().awaitInput();
            assertNull(window.rememberedPosition(),
                    "a client this server has never seen opens where the platform puts it");

            // The platform says where it opened the window, which the server takes as where the
            // window already is; then the user drags it, which is a move.
            window.input().windowMoved(60, 40);
            Thread.sleep(600);
            window.input().windowMoved(260, 140);
            assertTrue(await("the position to be written out",
                    () -> store.position("Termora") != null), "the move was never remembered");
            assertEquals(new Point(260, 140), store.position("Termora"));
        }

        try (Server second = new Server(81, store)) {
            second.client().mapWindowWithClass("Termora", "termora");
            TestWindow window = second.awaitWindow();
            assertNotNull(window.rememberedPosition(),
                    "the same client has to open where it was left");
            assertEquals(new Point(260, 140), window.rememberedPosition());
        }
    }

    @Test
    void whereAWindowOpensIsNotAMoveTheUserMade(@TempDir Path directory) throws Exception {
        HostWindowStore store = HostWindowStore.at(directory.resolve("opened.properties"));
        try (Server server = new Server(84, store)) {
            server.client().mapWindowWithClass("Termora", "termora");
            TestWindow window = server.awaitWindow().awaitInput();

            // The platform puts the window somewhere as it opens, and reports it the same way a
            // drag is reported. It is where the window already is, not news about the user.
            window.input().windowMoved(100, 100);
            Thread.sleep(900);
            assertNull(store.position("Termora"),
                    "the spot a window opens at is not somewhere the user put it");

            // And a move after that is remembered, as the difference it is.
            window.input().windowMoved(150, 120);
            assertTrue(await("the move to be remembered",
                    () -> store.position("Termora") != null), "a real move was not remembered");
            assertEquals(new Point(150, 120), store.position("Termora"));
        }
    }

    @Test
    void aClientThatPublishesNoClassIsNotRemembered(@TempDir Path directory) throws Exception {
        HostWindowStore store = HostWindowStore.at(directory.resolve("no-class.properties"));
        try (Server server = new Server(82, store)) {
            // A window with a title but no WM_CLASS: there is no name for it that survives it
            // retitling itself, so there is nothing to remember it by.
            server.client().mapWindowWithClass(null, "termora");
            server.awaitWindow().input().windowMoved(300, 200);

            Thread.sleep(800);
            assertNull(store.position("termora"));
            assertNull(store.position(null));
        }
    }

    @Test
    void anotherClientsWindowIsNotMoved(@TempDir Path directory) throws Exception {
        HostWindowStore store = HostWindowStore.at(directory.resolve("two.properties"));
        store.rememberPosition("EasyShell", 40, 60);

        try (Server server = new Server(83, store)) {
            server.client().mapWindowWithClass("Termora", "termora");
            assertNull(server.awaitWindow().rememberedPosition(),
                    "what was remembered for one application is not where another one opens");
        }
    }

    // ------------------------------------------------------------------
    // Harness
    // ------------------------------------------------------------------

    private static boolean await(String what, java.util.function.BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + 4000;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return false;
    }

    /** A server whose windows are backends a test can inspect instead of real ones. */
    private static final class Server implements AutoCloseable {

        final XServer xserver;
        final List<TestWindow> windows = new CopyOnWriteArrayList<>();
        private final RawXClient client;

        Server(int display, HostWindowStore store) throws Exception {
            xserver = new XServer(ServerOptions.parse(new String[]{"--display", ":" + display,
                    "--backend", "headless", "--no-unix"}));
            xserver.setHostWindowStore(store);
            xserver.setBackendFactory(() -> {
                TestWindow window = new TestWindow();
                windows.add(window);
                return window;
            });
            xserver.start();
            xserver.startEventLoop();
            client = RawXClient.connect(display);
        }

        RawXClient client() {
            return client;
        }

        /** The window the server has opened for the client, once it has opened one. */
        TestWindow awaitWindow() {
            long deadline = System.currentTimeMillis() + 4000;
            while (System.currentTimeMillis() < deadline && windows.isEmpty()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            assertTrue(!windows.isEmpty(), "the server never opened a window");
            return windows.get(0);
        }

        @Override
        public void close() throws Exception {
            client.close();
            xserver.stop();
        }
    }

    /** A window that records what it is asked to show and where it was asked to go. */
    private static final class TestWindow implements DisplayBackend {
        private InputSink input;
        private Point rememberedPosition;

        Point rememberedPosition() {
            return rememberedPosition;
        }

        /** This window, once the server has started it and it has somewhere to report moves to. */
        TestWindow awaitInput() {
            long deadline = System.currentTimeMillis() + 4000;
            while (System.currentTimeMillis() < deadline && input == null) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            assertNotNull(input, "the window was never started");
            return this;
        }

        InputSink input() {
            return input;
        }

        @Override
        public String name() {
            return "test";
        }

        @Override
        public void start(Screen screen, int displayNumber, InputSink input) {
            this.input = input;
        }

        @Override
        public void setWindowPosition(int x, int y) {
            rememberedPosition = new Point(x, y);
        }

        @Override
        public void present(Screen screen, Rectangle damage, CursorSprite cursor) {
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    }
}
