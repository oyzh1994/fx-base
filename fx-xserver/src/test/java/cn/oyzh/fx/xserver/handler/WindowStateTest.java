package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.CursorSprite;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a client can ask the frame to do.
 *
 * <p>The host window is the frame around a forwarded application, so the two things an application
 * asks its window manager for - "fill the display" and "the user should look at me" - have to be
 * answered here or they cannot be had at all: the application cannot reach the platform's window
 * itself.
 *
 * <p>The awkward half is the protocol: a toolkit does not set the state, it *asks* for it with a
 * {@code ClientMessage} to the root and waits for the window manager to put the state into the
 * property. A client that is ignored asks again, for ever, which is what makes writing it back part
 * of the feature rather than a detail of it.
 */
class WindowStateTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 74;

    /** {@code _NET_WM_STATE}'s actions. */
    private static final int REMOVE = 0;
    private static final int ADD = 1;
    private static final int TOGGLE = 2;

    private Server server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.xserver().stop();
        }
    }

    @Test
    void aClientThatAsksToFillTheDisplayIsGivenTheWholeScreen() throws Exception {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = mapWindow(client);
            int fullScreen = client.internAtom("_NET_WM_STATE_FULLSCREEN");

            client.sendWindowState(window, ADD, fullScreen, 0);
            client.flush();

            TestWindow frame = server.awaitWindow().awaitInput();
            assertTrue(await(() -> Boolean.TRUE.equals(frame.fullScreen)),
                    "a client that asks to fill the display has to be shown the display");
            assertEquals(List.of(fullScreen), List.of(boxed(client.windowState(window))),
                    "and the state has to be written back, or the client asks again for ever");
        }
    }

    @Test
    void leavingFullScreenPutsTheWindowBack() throws Exception {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = mapWindow(client);
            int fullScreen = client.internAtom("_NET_WM_STATE_FULLSCREEN");
            client.sendWindowState(window, ADD, fullScreen, 0);
            client.flush();
            TestWindow frame = server.awaitWindow().awaitInput();
            assertTrue(await(() -> Boolean.TRUE.equals(frame.fullScreen)));

            client.sendWindowState(window, REMOVE, fullScreen, 0);
            client.flush();
            assertTrue(await(() -> Boolean.FALSE.equals(frame.fullScreen)),
                    "a client that leaves full screen gets its frame back");
            assertEquals(0, client.windowState(window).length, "and the state is gone from the list");
        }
    }

    @Test
    void togglingAddsThenRemoves() throws Exception {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = mapWindow(client);
            int fullScreen = client.internAtom("_NET_WM_STATE_FULLSCREEN");

            client.sendWindowState(window, TOGGLE, fullScreen, 0);
            client.flush();
            assertTrue(await(() -> client.windowState(window).length == 1),
                    "toggle adds a state the window does not have");

            client.sendWindowState(window, TOGGLE, fullScreen, 0);
            client.flush();
            assertTrue(await(() -> client.windowState(window).length == 0),
                    "and removes the one it does");
        }
    }

    @Test
    void bothStatesOfARequestAreApplied() throws Exception {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = mapWindow(client);
            int horizontal = client.internAtom("_NET_WM_STATE_MAXIMIZED_HORZ");
            int vertical = client.internAtom("_NET_WM_STATE_MAXIMIZED_VERT");

            // A maximised request names two properties, and a window that comes back half maximised
            // is a window the user has to fix by hand.
            client.sendWindowState(window, ADD, horizontal, vertical);
            client.flush();

            assertTrue(await(() -> client.windowState(window).length == 2),
                    "both atoms of a request belong in the state");
        }
    }

    @Test
    void aStateWrittenStraightToThePropertyIsReadToo() throws Exception {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = mapWindow(client);
            int state = client.internAtom("_NET_WM_STATE");
            int fullScreen = client.internAtom("_NET_WM_STATE_FULLSCREEN");

            // Some clients skip the message and write the property themselves; the state is then
            // already the window manager's answer, and it has to be honoured the same way.
            client.setCardinalProperty(window, state, client.internAtom("ATOM"), fullScreen);
            client.flush();

            TestWindow frame = server.awaitWindow().awaitInput();
            assertTrue(await(() -> Boolean.TRUE.equals(frame.fullScreen)));
        }
    }

    @Test
    void theUrgencyHintRaisesTheFrameOnce() throws Exception {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = mapWindow(client);
            TestWindow frame = server.awaitWindow().awaitInput();
            // The client's first window is itself a raise (C2 - the frame comes forward when an
            // application arrives), so the hint is measured as a *change* rather than from zero.
            Thread.sleep(500);
            int before = frame.raises.get();

            client.setUrgency(window, true);
            client.flush();
            assertTrue(await(() -> frame.raises.get() == before + 1),
                    "a client asking for attention gets the window brought forward");

            // The same hint, still set, a second later: the frame is already there, and a window
            // that raises itself every frame is a window the user cannot get away from.
            Thread.sleep(500);
            assertEquals(before + 1, frame.raises.get(), "the hint is acted on once, not once a frame");

            // Cleared and set again is a second request, and is answered.
            client.setUrgency(window, false);
            client.flush();
            Thread.sleep(200);
            client.setUrgency(window, true);
            client.flush();
            assertTrue(await(() -> frame.raises.get() == before + 2), "a new hint is a new request");
            assertFalse(client.windowState(window).length > 0, "and it is not a window state");
        }
    }

    // ------------------------------------------------------------------
    // 辅助
    // ------------------------------------------------------------------

    private void startServer() throws Exception {
        server = new Server(new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "400x300", "--backend", "headless", "--single-window", "--no-unix"})));
        server.xserver().setBackendFactory(() -> {
            TestWindow window = new TestWindow();
            server.windows().add(window);
            return window;
        });
        server.xserver().start();
        server.xserver().startEventLoop();
    }

    private static int mapWindow(RawXClient client) throws Exception {
        int window = client.allocateId();
        client.createWindow(window, client.root(), 0, 0, 300, 200);
        client.mapWindow(window);
        client.flush();
        return window;
    }

    private static Integer[] boxed(int[] atoms) {
        Integer[] boxed = new Integer[atoms.length];
        for (int i = 0; i < atoms.length; i++) {
            boxed[i] = atoms[i];
        }
        return boxed;
    }

    /** A condition that may have to talk to the server, which most of these do. */
    private interface Check {
        boolean ok() throws Exception;
    }

    private static boolean await(Check condition) {
        long deadline = System.currentTimeMillis() + 4000;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (condition.ok()) {
                    return true;
                }
            } catch (Exception e) {
                throw new AssertionError(e);
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

    private record Server(XServer xserver, List<TestWindow> windows) {
        Server(XServer xserver) {
            this(xserver, new CopyOnWriteArrayList<>());
        }

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
            assertFalse(windows.isEmpty(), "the server never opened a window");
            return windows.get(0);
        }
    }

    /** A frame that records what it was asked to do about the client's state. */
    private static final class TestWindow implements DisplayBackend {

        private DisplayBackend.InputSink input;
        /** Null until the tick has decided, so a test can tell "not yet" from "no". */
        volatile Boolean fullScreen;
        final java.util.concurrent.atomic.AtomicInteger raises =
                new java.util.concurrent.atomic.AtomicInteger();

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

        @Override
        public void setFullScreen(boolean fullScreen) {
            this.fullScreen = fullScreen;
        }

        @Override
        public void raise() {
            raises.incrementAndGet();
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
