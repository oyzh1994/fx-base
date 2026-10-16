package cn.oyzh.fx.xserver;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.CursorSprite;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * C2: the host window comes to the front when the client's first window appears - and only then.
 *
 * <p>The user's report was "the window opens behind the IDE": the host window is made once and was
 * never brought forward, so an application started while something else was frontmost stayed behind
 * it, grey and unfocused. The fix is a raise on the client's *first* top level window, and the two
 * things that make it safe are what these tests pin: it happens **once** however many windows the
 * client goes on to map (a client that maps a popup per keystroke is not asking to be pulled out from
 * under the user's hands), and it happens only **after** a frame has been shown, because raising onto
 * an empty frame tells the user their application arrived and shows them nothing.
 */
class HostWindowRaiseTest {

    /** A display number nothing else in the suite uses. */
    private static final int DISPLAY = 79;

    private static final long SETTLE_MILLIS = 600;

    private Server server;
    private RawXClient client;

    @BeforeEach
    void startServer() throws Exception {
        server = Server.start();
        client = RawXClient.connect(DISPLAY);
    }

    @AfterEach
    void stopServer() throws Exception {
        if (client != null) {
            client.close();
        }
        server.stop();
    }

    @Test
    void aClientsFirstWindowBringsTheHostWindowForward() throws Exception {
        TestWindow frame = server.awaitWindow().awaitInput();
        mapAWindow(500, 400);
        awaitRaise(frame);

        List<String> calls = frame.calls;
        int firstRaise = calls.indexOf("raise");
        assertTrue(calls.subList(0, firstRaise).contains("present"),
                "the window is brought forward only once it has something to show: an empty frame"
                        + " raised over the user's other windows says the application arrived and"
                        + " shows them nothing. Calls were " + calls);
    }

    @Test
    void andOnlyOnceHoweverManyWindowsItMaps() throws Exception {
        TestWindow frame = server.awaitWindow().awaitInput();
        mapAWindow(500, 400);
        awaitRaise(frame);

        // A second, larger window - which is what a splash screen followed by the real one looks
        // like, and the largest mapped window is what the host window is named after.
        mapAWindow(600, 500);
        Thread.sleep(SETTLE_MILLIS);
        assertEquals(1, frame.raises(), "a client's later windows are its own business, not a reason"
                + " to take the front away from whatever the user is doing");
    }

    @Test
    void aPopupIsNotAReasonToComeForward() throws Exception {
        TestWindow frame = server.awaitWindow().awaitInput();
        int popup = client.allocateId();
        client.createWindow(popup, client.root(), 100, 100, 400, 300, 0x00FF00);
        client.setOverrideRedirect(popup);
        client.mapWindow(popup);
        client.flush();

        Thread.sleep(SETTLE_MILLIS);
        assertEquals(0, frame.raises(), "a menu is mapped and unmapped as the user works through it,"
                + " and every one of them breaking in would make the window something the user"
                + " cannot get away from");
    }

    // ------------------------------------------------------------------

    /** Maps a plain top level window of the given size, which is what a client's application is. */
    private void mapAWindow(int width, int height) throws Exception {
        int window = client.allocateId();
        client.createWindow(window, client.root(), 0, 0, width, height, 0x00FF00);
        client.mapWindow(window);
        client.flush();
    }

    private static void awaitRaise(TestWindow frame) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 4000;
        while (System.currentTimeMillis() < deadline && frame.raises() == 0) {
            Thread.sleep(20);
        }
        assertTrue(frame.raises() > 0, "the host window was never brought forward; calls were " + frame.calls);
    }

    /** A server whose frame records what it was asked to do, and in which order. */
    private record Server(XServer xserver, List<TestWindow> windows) {

        static Server start() throws Exception {
            XServer xserver = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                    "--geometry", "400x300", "--backend", "headless", "--single-window", "--no-unix"}));
            List<TestWindow> windows = new CopyOnWriteArrayList<>();
            xserver.setBackendFactory(() -> {
                TestWindow window = new TestWindow();
                windows.add(window);
                return window;
            });
            xserver.start();
            // The server's own start does not run the display loop - the platform's event loop needs
            // the main thread - so a test that wants frames and raises ticks for itself.
            xserver.startEventLoop();
            return new Server(xserver, windows);
        }

        TestWindow awaitWindow() throws InterruptedException {
            long deadline = System.currentTimeMillis() + 4000;
            while (System.currentTimeMillis() < deadline && windows.isEmpty()) {
                Thread.sleep(20);
            }
            assertTrue(!windows.isEmpty(), "the server never opened a window");
            return windows.get(0);
        }

        void stop() {
            xserver.stop();
        }
    }

    private static final class TestWindow implements DisplayBackend {

        private DisplayBackend.InputSink input;
        /** Every {@code present} and {@code raise} in the order they were asked for. */
        final List<String> calls = new CopyOnWriteArrayList<>();

        @Override
        public String name() {
            return "test";
        }

        @Override
        public void start(Screen screen, int displayNumber, DisplayBackend.InputSink input) {
            this.input = input;
        }

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
            return this;
        }

        int raises() {
            return (int) calls.stream().filter("raise"::equals).count();
        }

        @Override
        public void present(Screen screen, Rectangle damage, CursorSprite cursor) {
            calls.add("present");
        }

        @Override
        public void raise() {
            calls.add("raise");
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
