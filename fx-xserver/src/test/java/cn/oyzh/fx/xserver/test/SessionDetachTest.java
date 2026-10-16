package cn.oyzh.fx.xserver.test;

import cn.oyzh.fx.xserver.ScreenSession;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.CursorSprite;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.Screen;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A closed window does not have to be the end of the session (backlog D1).
 *
 * <p>The painful failure mode this is about is the one a user meets when they forward a long-running
 * application: closing the frame it was in kills the application. With {@code --detach-on-close} the
 * window is only a window - the clients keep running with nobody watching - and the control port is
 * how a window comes back, showing the same screen it was showing before.
 *
 * <p>What makes this worth a test rather than a line of documentation is the state machine: the tick
 * opens a window whenever a session has none and its client has drawn something, which is exactly
 * wrong for a detached session, and the pixels a detached screen drew are no longer in any damage
 * rectangle by the time a window comes back. Both have a way of looking right in a quick manual run
 * and wrong the first time somebody leaves a session detached overnight.
 */
class SessionDetachTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 93;

    private static final int WHITE = 0xFFFFFF;
    private static final int RED = 0xFF0000;

    @Test
    void closingTheWindowWithTheFlagOnLeavesTheClientRunning() throws Exception {
        Server server = new Server(DISPLAY, true);
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = mappedWindow(client);
            waitUntil(() -> server.windows.size() == 1, "the window should have opened");
            ScreenSession session = sessionOf(server);

            server.windows.get(0).closed = true;
            waitUntil(session::detached, "the session should have detached");

            // The client is still connected and still being served: a pixel it draws comes back out
            // of the framebuffer, which is a round trip through the server and back - the only proof
            // of "the session stayed" that means anything.
            int gc = client.allocateId();
            client.createGC(gc, client.root(), RED);
            client.fillRectangle(window, gc, 10, 10, 20, 20);
            assertEquals(RED, client.pixelAt(window, 15, 15) & 0xFFFFFF, "and still drawing");

            // And no window was opened for it again: the tick opens one whenever a session has none
            // and its client has drawn something, which is the trap this flag has to switch off.
            Thread.sleep(300);
            assertEquals(1, server.windows.size(),
                    "a detached session must not have a window opened for it");
            // Still alive after all of that, and after the frames that went by in between.
            assertEquals(200, client.geometry(window)[2], "the client is still there");
        } finally {
            server.stop();
        }
    }

    @Test
    void attachingAgainOpensAWindowAndPresentsTheWholeViewport() throws Exception {
        Server server = new Server(DISPLAY, true);
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = mappedWindow(client);
            waitUntil(() -> server.windows.size() == 1, "the window should have opened");
            ScreenSession session = sessionOf(server);
            server.windows.get(0).closed = true;
            waitUntil(session::detached, "the session should have detached");

            // The client draws while nobody is looking, which is what a detached session is for.
            int gc = client.allocateId();
            client.createGC(gc, client.root(), RED);
            client.fillRectangle(window, gc, 0, 0, 40, 30);
            client.flush();
            Thread.sleep(200);

            assertEquals("opening a window for 1 session(s); 1 client(s) connected",
                    command(server, "attach"));
            waitUntil(() -> server.windows.size() == 2, "attaching should open a window again");
            assertFalse(session.detached(), "and the session is attached again");

            // The first frame is the whole viewport, not the damage: what the client drew while
            // detached was consumed by the frames that had no window to present it to, so a window
            // that waited for more damage would show the background until the client drew again.
            Backend backend = server.windows.get(1);
            waitUntil(() -> !backend.damage.isEmpty(), "the new window should be shown something");
            assertEquals(new Rectangle(session.viewport()), backend.damage.get(0),
                    "the frame that comes back is the whole viewport");

            // And the screen really does carry the pixels it drew: the same framebuffer, unseen.
            assertEquals(RED, session.screen().framebuffer().pixel(5, 5) & 0xFFFFFF,
                    "the pixels drawn while detached are still there");
        } finally {
            server.stop();
        }
    }

    @Test
    void theControlPortSaysWhatIsGoingOnAndCanDetachToo() throws Exception {
        Server server = new Server(DISPLAY, true);
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            mappedWindow(client);
            waitUntil(() -> server.windows.size() == 1, "the window should have opened");
            ScreenSession session = sessionOf(server);

            String status = command(server, "status");
            assertTrue(status.contains("windows=1"), "one window, and it says so: " + status);
            assertTrue(status.contains("detached=0"), status);
            assertTrue(status.contains("clients=1"), status);

            // Detaching on demand is the other half of the mechanism, and it works the same way: the
            // window goes, the client stays.
            assertTrue(command(server, "detach").startsWith("closing the window of 1 session"),
                    "detach should say what it is doing");
            waitUntil(session::detached, "the session should have detached");
            waitUntil(() -> session.backend() == null, "and its window should be closed");
            // The client is still being served while no window shows it.
            int gc = client.allocateId();
            client.createGC(gc, client.root(), RED);
            client.fillRectangle(session.screen().root().id(), gc, 0, 0, 5, 5);
            assertTrue(command(server, "status").contains("detached=1"),
                    "and the server says so: " + command(server, "status"));
            assertTrue(command(server, "attach").startsWith("opening a window for 1 session"),
                    "the window can be asked back");
            waitUntil(() -> server.windows.size() == 2, "and comes back");
        } finally {
            server.stop();
        }
    }

    @Test
    void aSessionThatIsNotDetachedIsNotAttachedTwice() throws Exception {
        Server server = new Server(DISPLAY, true);
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            mappedWindow(client);
            waitUntil(() -> server.windows.size() == 1, "the window should have opened");
            // Nothing is detached, so there is nothing to attach and one window to keep.
            assertEquals("nothing to attach: 1 client(s) connected", command(server, "attach"));
            Thread.sleep(200);
            assertEquals(1, server.windows.size(), "and no second window was opened");
            assertEquals("unknown command \"frobnicate\": attach, detach or status",
                    command(server, "frobnicate"));
        } finally {
            server.stop();
        }
    }

    // ------------------------------------------------------------------
    // The harness
    // ------------------------------------------------------------------

    /**
     * The session a connected client's own screen belongs to: the one with a client behind it.
     *
     * <p>Read from the server rather than from the test's client, which speaks the protocol and knows
     * nothing about sessions - the master screen is always there too, and it has no client.
     */
    private static ScreenSession sessionOf(Server server) {
        return server.xserver.sessions().stream()
                .filter(session -> session.client() != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no client session"));
    }

    /** A client window that has been mapped, which is what makes a session open a window at all. */
    private static int mappedWindow(RawXClient client) throws Exception {
        int window = client.allocateId();
        client.createWindow(window, client.root(), 0, 0, 200, 100, WHITE);
        client.mapWindow(window);
        client.flush();
        return window;
    }

    /** One control command over a real connection, as `nc` would send it. */
    private static String command(Server server, String line) throws Exception {
        try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), server.xserver.controlPort())) {
            OutputStream out = socket.getOutputStream();
            out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                    StandardCharsets.UTF_8));
            return in.readLine();
        }
    }

    private static void waitUntil(BooleanSupplier condition, String message) throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(10);
        }
        assertTrue(condition.getAsBoolean(), message);
    }

    private static final class Server {

        final XServer xserver;
        final List<Backend> windows = new CopyOnWriteArrayList<>();

        Server(int display, boolean detachOnClose) throws Exception {
            String[] args = detachOnClose
                    ? new String[]{"--display", ":" + display, "--backend", "headless", "--no-unix",
                            "--detach-on-close", "--control-port", "0"}
                    : new String[]{"--display", ":" + display, "--backend", "headless", "--no-unix"};
            this.xserver = new XServer(ServerOptions.parse(args));
            xserver.setBackendFactory(() -> {
                Backend backend = new Backend();
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

    /** A window that records what it is asked to present, and can be "closed" by a test. */
    private static final class Backend implements DisplayBackend {

        final AtomicBoolean stopped = new AtomicBoolean();
        final List<Rectangle> damage = new CopyOnWriteArrayList<>();
        volatile Screen screen;
        volatile InputSink input;
        volatile Rectangle viewport = new Rectangle();
        volatile boolean closed;

        @Override
        public String name() {
            return "test";
        }

        @Override
        public void start(Screen screen, int displayNumber, InputSink input) {
            this.screen = screen;
            this.input = input;
        }

        @Override
        public void setViewport(Rectangle viewport) {
            this.viewport = new Rectangle(viewport);
        }

        @Override
        public void present(Screen screen, Rectangle damage, CursorSprite cursor) {
            this.damage.add(new Rectangle(damage));
        }

        @Override
        public void stop() {
            stopped.set(true);
        }

        @Override
        public boolean isClosed() {
            return closed;
        }
    }
}
