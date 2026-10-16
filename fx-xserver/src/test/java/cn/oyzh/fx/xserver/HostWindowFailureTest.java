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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A server that cannot show a window stops, rather than serving clients into the void.
 *
 * <p>Found while doing E2, which is about running the suite on a machine with no display: there the
 * JavaFX toolkit's startup parks for ever - measured, with a thread dump - so the tick thread never
 * came back. The server went on answering clients, logging nothing, its only explanation a stack
 * trace from a render thread, and no frame was ever shown again. A display server that cannot
 * display has one honest thing to say.
 *
 * <p>What is tested here is the *server's* half: a backend that refuses to open a window makes the
 * server stop instead of carrying on. The other half - the toolkit startup being bounded at all -
 * needs a machine that cannot open a window, and is the live check in the backlog's entry.
 */
class HostWindowFailureTest {

    /** A display number nothing else in the suite uses. */
    private static final int DISPLAY = 77;

    private XServer server;
    private RawXClient client;

    @BeforeEach
    void startServer() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "400x300", "--no-unix", "--backend", "headless"}));
        // One window per client, so the backend is opened from the tick rather than from start().
        server.setBackendFactory(RefusingWindow::new);
        server.start();
        server.startEventLoop();
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
    void aBackendThatCannotOpenAWindowStopsTheServer() throws Exception {
        // The client draws, which is what asks the server for a window of the client's own.
        int window = client.allocateId();
        client.createWindow(window, client.root(), 0, 0, 200, 200, 0x00FF00);
        client.mapWindow(window);
        client.flush();

        long deadline = System.currentTimeMillis() + 4000;
        boolean stopped = false;
        while (System.currentTimeMillis() < deadline && !stopped) {
            stopped = !acceptsConnections();
            Thread.sleep(50);
        }
        assertTrue(stopped, "the server kept serving with nowhere to show anything");

        // And the client that was there is let go rather than left writing into nothing.
        assertThrows(Exception.class, () -> {
            for (int i = 0; i < 100; i++) {
                client.getImage(client.root(), 0, 0, 1, 1);
                client.flush();
                Thread.sleep(10);
            }
        });
    }

    @Test
    void andAServerThatNeverNeedsAWindowIsUntouched() throws Exception {
        // No window is mapped, so no backend is ever asked for: the server is not stopped by the
        // refusal it never meets. This is what an idle connection looks like.
        Thread.sleep(600);
        assertTrue(acceptsConnections(), "a server nobody has mapped anything on is still listening");
    }

    /** Whether a new connection is still accepted, which is the thing stopping takes away. */
    private boolean acceptsConnections() {
        try (RawXClient attempt = RawXClient.connect(DISPLAY)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** A window that cannot be opened, standing in for a machine with no display. */
    private static final class RefusingWindow implements DisplayBackend {

        @Override
        public String name() {
            return "refusing";
        }

        @Override
        public void start(Screen screen, int displayNumber, InputSink input) {
            throw new IllegalStateException("no display: a window cannot be opened here");
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
