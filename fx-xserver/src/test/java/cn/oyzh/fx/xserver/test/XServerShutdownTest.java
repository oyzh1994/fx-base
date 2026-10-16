package cn.oyzh.fx.xserver.test;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.CursorSprite;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.Screen;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What happens at the two ends of a session: the host window closing, and the clients going away.
 *
 * <p>Both directions used to leave the other side hanging. Closing the window stopped the display
 * loop but told nobody, so the program on the far end of a forwarded display kept running with no
 * display to draw to and the ssh that started it never returned. And a client that quit on its own
 * left the server holding a window that went on being a live display for nobody.
 */
class XServerShutdownTest {

    private static final int CLIENT_MESSAGE = 33;
    private static final int EXPOSURE_MASK = 1 << 15;

    /** How long the tests give a client to close itself, well inside the five second socket timeout. */
    private static final int GRACE_MILLIS = 300;

    @Test
    void closingTheWindowAsksTheClientsToCloseAndThenDisconnectsThem() throws Exception {
        TestServer server = new TestServer(97);
        try (RawXClient client = RawXClient.connect(server.display)) {
            int protocols = client.internAtom("WM_PROTOCOLS");
            int deleteWindow = client.internAtom("WM_DELETE_WINDOW");
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 200, 200);
            // A top level window that asks to be told before it is closed, which is what a toolkit
            // sets up and what makes it quit when a window manager asks it to.
            client.setAtomListProperty(window, protocols, protocols, deleteWindow);
            client.mapWindow(window);
            client.flush();

            server.backend.closed.set(true);

            byte[] message = client.readEvent(CLIENT_MESSAGE);
            assertNotNull(message, "the client was never asked to close");
            assertEquals(32, message[1] & 0xFF, "WM_DELETE_WINDOW is a 32 bit message");
            assertEquals(window, RawXClient.readInt(message, 4), "the window being asked to close");
            assertEquals(protocols, RawXClient.readInt(message, 8), "the message type");
            assertEquals(deleteWindow, RawXClient.readInt(message, 12), "WM_DELETE_WINDOW");

            // A client that ignores the message is cut off once the grace period is up, which is the
            // only thing left to tell it that the display is gone.
            assertTrue(client.atEndOfStream(), "the client was left connected after the window closed");
            waitUntil(() -> server.backend.stopped.get(), "the server did not shut down after its window closed");
        } finally {
            server.stop();
        }
    }

    @Test
    void stoppingTheServerAsksTheClientsToCloseBeforeItCutsThemOff() throws Exception {
        // The stop path, not the window-close one: Ctrl-C, a signal, the control port - a server that
        // only cut the connections left an application whose toolkit never learned to quit running on
        // the far machine for ever (A27). And through the *clients*, not the sessions: in
        // single-window mode - which is what this test runs - every client's windows are on the
        // master screen and the master session has no client of its own, so a loop over
        // `session.client()` asks nobody at all.
        TestServer server = new TestServer(88);
        try (RawXClient client = RawXClient.connect(server.display)) {
            int protocols = client.internAtom("WM_PROTOCOLS");
            int deleteWindow = client.internAtom("WM_DELETE_WINDOW");
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 200, 200);
            client.setAtomListProperty(window, protocols, protocols, deleteWindow);
            client.mapWindow(window);
            client.flush();

            // The stop runs on its own thread, because the ask has to be *readable* while it is
            // happening: a stop that blocked the reader would have cut the connection first, and a
            // cut with unread input is a reset, which is exactly how a real application loses a
            // WM_DELETE_WINDOW it was never given the chance to read.
            Thread stopping = new Thread(server::stop, "test-stop");
            stopping.start();

            byte[] message = client.readEvent(CLIENT_MESSAGE);
            assertNotNull(message, "the client was never asked to close by the stop path");
            assertEquals(window, RawXClient.readInt(message, 4), "the window being asked to close");
            assertEquals(deleteWindow, RawXClient.readInt(message, 12), "WM_DELETE_WINDOW");

            // What a toolkit does with it: quit, which closes the connection. The stop then has
            // nothing left to wait for and returns at once.
            client.close();
            stopping.join(2000);
            assertFalse(stopping.isAlive(), "the stop waited for the client instead of cutting it off"
                    + " as soon as it was gone");
        } finally {
            server.stop();
        }
    }

    @Test
    void theDisplayLoopEndsWhenTheServerDoes() throws Exception {
        // `Main` waits for this thread and then ends the process - which is what the macOS Quit
        // needed: the JavaFX toolkit's keep-alive thread is not a daemon and outlives
        // `Platform.exit()`, so a process whose server had finished (the dock's Quit closed the
        // window and released every client) was still in the dock twelve seconds later with no
        // server thread left in it. A display loop that never ended would be a process that never
        // does.
        TestServer server = new TestServer(89);
        try {
            assertTrue(server.loop.isAlive(), "the loop runs while the server does");
            server.stop();
            server.loop.join(2000);
            assertFalse(server.loop.isAlive(), "the loop has to end when the server does");
        } finally {
            server.stop();
        }
    }

    @Test
    void aClientThatDoesNotWantToBeToldIsJustDisconnected() throws Exception {
        TestServer server = new TestServer(94);
        try (RawXClient client = RawXClient.connect(server.display)) {
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 200, 200);
            client.mapWindow(window);
            client.flush();

            server.backend.closed.set(true);

            // No WM_PROTOCOLS on the window, so there is no message to send and the connection going
            // away is the only notice it gets - which is what a real X server's death looks like.
            assertTrue(client.atEndOfStream(), "the client was left connected after the window closed");
        } finally {
            server.stop();
        }
    }

    @Test
    void noClientMeansNoFramesUntilOneConnectsAgain() throws Exception {
        TestServer server = new TestServer(93);
        try (RawXClient client = RawXClient.connect(server.display)) {
            waitUntil(() -> !server.backend.idle.get(), "a connected client should keep it live");

            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 200, 200);
            client.selectInput(window, EXPOSURE_MASK);
            client.mapWindow(window);
            client.flush();
            assertNotNull(client.readEvent(), "the window should have been exposed");

            client.close();

            // The last frame is painted out - the destroyed window is what it has to stop showing -
            // and from then on the server holds it instead of treating an empty screen as something
            // to keep drawing.
            waitUntil(() -> server.backend.idle.get(), "the server never noticed the last client went");
            int frames = server.backend.frames.get();
            Thread.sleep(GRACE_MILLIS);
            int painted = server.backend.frames.get() - frames;
            assertTrue(painted <= 1, "the server kept painting with no clients: " + painted + " frames");
        } finally {
            server.stop();
        }
    }

    // ------------------------------------------------------------------
    // The server under test
    // ------------------------------------------------------------------

    /** Runs a server on a backend a test can close, and records what the server asks of it. */
    private static final class TestServer {

        final int display;
        final XServer xserver;
        final TestBackend backend = new TestBackend();
        /** The display loop, which is what an entry point that quits the process waits for. */
        final Thread loop;

        TestServer(int display) throws Exception {
            this.display = display;
            // Single window mode: these tests are about the semantics of the one shared window -
            // closing it ends the session, and idleness stops the frames.
            this.xserver = new XServer(ServerOptions.parse(new String[]{"--display", ":" + display,
                    "--backend", "headless", "--no-unix", "--single-window",
                    "--close-grace-ms", String.valueOf(GRACE_MILLIS)}));
            xserver.setBackend(backend);
            xserver.start();
            this.loop = xserver.startEventLoop();
        }

        void stop() {
            xserver.stop();
        }
    }

    /** A window that reports itself closed when a test says so, and counts the frames it is sent. */
    private static final class TestBackend implements DisplayBackend {
        final AtomicBoolean closed = new AtomicBoolean();
        final AtomicBoolean idle = new AtomicBoolean();
        final AtomicBoolean stopped = new AtomicBoolean();
        final AtomicInteger frames = new AtomicInteger();

        @Override
        public String name() {
            return "test";
        }

        @Override
        public void start(Screen screen, int displayNumber, InputSink input) {
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
            return closed.get();
        }
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
