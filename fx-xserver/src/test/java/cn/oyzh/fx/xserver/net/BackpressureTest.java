package cn.oyzh.fx.xserver.net;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A client that stops reading must not stop the server.
 *
 * <p>Every reply and event used to be written to the client's socket from the thread that held the
 * server lock - the display tick for an event, another client's request thread for a reply. A client
 * that stopped reading filled its socket buffer, the write blocked, and the lock was never released:
 * **one hung client froze every other client's screen**, which is the worst failure mode a display
 * server can have. It is what the README listed as a known limitation.
 *
 * <p>Now each connection has a bounded queue and a writer thread of its own, so the only thread that
 * ever waits for a client is that client's own; a queue that fills up means the client has stopped
 * reading altogether, and it is dropped instead. The two tests here are the item's own Verify: a
 * second client keeps drawing while the first is stalled, and the stalled one is disconnected.
 *
 * <p>The "stopped reading" client is a real one - a socket that handshakes, shrinks its receive
 * buffer, and then sends requests whose replies it never reads. Its replies fill the server's socket
 * buffer and then the queue behind it, which is what a hung X client does.
 */
class BackpressureTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 72;

    /**
     * How many small requests follow the one big reply: enough to overflow the queue behind the
     * socket buffers, capped so a run cannot send for ever. The flood runs on its own thread because
     * a socket write has no timeout to bound it.
     */
    private static final int FLOOD_REQUESTS = 20000;

    private XServer server;
    private RawXClient stalled;
    private Thread flood;

    /** How many requests the flood has sent, so a test can wait for the server to be under load. */
    private final java.util.concurrent.atomic.AtomicInteger sent =
            new java.util.concurrent.atomic.AtomicInteger();

    @BeforeEach
    void startServerAndStallAClient() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "400x300", "--backend", "headless", "--single-window", "--no-unix"}));
        server.start();
        stalled = RawXClient.connect(DISPLAY);
        // A window of its own with its crossings selected: the events a stalled client is sent by
        // the *server* - from the tick, or from another client's XTEST request, both of which run
        // under the server lock - are what freezes everybody else. Replies alone do not: those are
        // written on the stalling client's own request thread.
        int window = stalled.allocateId();
        stalled.createWindow(window, stalled.root(), 0, 0, 100, 100);
        stalled.selectInput(window, 0x10 | 0x20);       // EnterWindowMask | LeaveWindowMask
        stalled.mapWindow(window);
        stalled.flush();
        // A small receive buffer, so the client's window closes early and it is the server's own
        // buffers that the test then fills.
        stalled.smallReceiveBuffer(2048);
        // The flood runs on a thread of its own, and that is not tidiness: a socket write has no
        // timeout, so a server that has stopped reading this client blocks the *writer* - and a test
        // whose own thread blocks there hangs instead of failing, which is not a regression guard.
        // As a daemon thread it can be left behind without holding the suite up.
        flood = new Thread(this::floodWithUnreadRequests, "backpressure-test-flood");
        flood.setDaemon(true);
        flood.start();
    }

    /** Sends requests whose replies are never read, until the server drops this client. */
    private void floodWithUnreadRequests() {
        try {
            // Big replies first, and several of them: a socket buffer on this platform can absorb
            // a megabyte before a write blocks, so one screen-sized GetImage is not enough to make
            // the server notice anything. (It was measured: one of them and the server never
            // blocked, which read as "the unfixed server is fine" - the lie a test like this exists
            // to avoid telling.) Five megabytes is past any buffer.
            for (int i = 0; i < 10; i++) {
                stalled.sendRequest(getImageRequest(stalled.root(), 400, 300));
            }
            // And then traffic behind it, which is what fills the queue the writer is stuck on.
            for (int i = 0; i < FLOOD_REQUESTS; i++) {
                stalled.sendRequest(geometryRequest(stalled.root()));
                sent.incrementAndGet();
            }
            stalled.flush();
        } catch (IOException dropped) {
            // The server closed the connection under it, which is what the test is waiting for.
        }
    }

    /** {@code GetImage} of a whole window, built here so its reply is never read. */
    private static byte[] getImageRequest(int drawable, int width, int height) {
        byte[] request = new byte[20];
        request[0] = 73;                            // GetImage
        request[1] = 2;                             // ZPixmap
        RawXClient.writeShortAt(request, 2, 5);
        RawXClient.writeIntAt(request, 4, drawable);
        RawXClient.writeShortAt(request, 8, 0);     // x
        RawXClient.writeShortAt(request, 10, 0);    // y
        RawXClient.writeShortAt(request, 12, width);
        RawXClient.writeShortAt(request, 14, height);
        RawXClient.writeIntAt(request, 16, 0xFFFFFFFF);     // plane mask
        return request;
    }

    @AfterEach
    void stopServer() {
        if (stalled != null) {
            try {
                stalled.close();
            } catch (IOException ignored) {
                // The server has usually closed it already, which is the point.
            }
        }
        server.stop();
    }

    /**
     * A second client draws and reads its own pixels back while the first is stalled, which is the
     * half of the failure a user notices: every screen but the hung one kept working.
     */
    @Test
    void aSecondClientKeepsDrawingWhileAnotherIsStalled() throws Exception {
        // Wait until the stalled client has really loaded the server - or until the server has
        // dropped it, which is the other way this ends. Without that wait the round trip below can
        // happen before the flood has done anything, and the test passes against a server that would
        // freeze a moment later, which is the kind of pass that means nothing.
        long deadline = System.currentTimeMillis() + 5000;
        while (sent.get() < 3000 && flood.isAlive() && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        try (RawXClient working = RawXClient.connect(DISPLAY)) {
            working.readTimeoutMillis(5000);
            int window = working.allocateId();
            working.createWindow(window, working.root(), 200, 150, 100, 100, 0x0000FF);
            working.mapWindow(window);
            working.clearArea(window, 0, 0, 100, 100);
            working.flush();

            // Move the pointer in and out of the stalled client's window. Every move is a crossing
            // for that client, sent by the server - and if the server writes them inline, the thread
            // holding the lock is the one waiting for a client that is not reading.
            int xtest = working.queryExtension("XTEST");
            for (int i = 0; i < 200; i++) {
                working.fakeMotion(xtest, 50, 50);
                working.fakeMotion(xtest, 350, 250);
            }
            working.flush();

            // A round trip against a live server: if the stalled client were holding the lock, this
            // would not come back at all.
            int[] geometry = working.geometry(window);
            assertEquals(100, geometry[2], "the working client's window is still there");
            assertEquals(0xFF0000FF, working.pixelAt(window, 5, 5),
                    "and the pixels it drew are its own, drawn after the other client stalled");
        }
    }

    /**
     * The stalled client is dropped, and dropped by the server - its connection is closed under it
     * while it is still sending, which is the only thing it can notice, since it is not reading.
     */
    @Test
    void theClientThatStoppedReadingIsDisconnected() throws Exception {
        // Draining what it never read: the stream ends - an EOF from the server's side of the close -
        // when the client has been dropped, and goes quiet instead when it has not. Reading is the
        // only way to tell the two apart from this end, and reading is what this client never did
        // while it was sending.
        stalled.readTimeoutMillis(8000);
        try {
            while (stalled.readEvent() != null) {
                // Everything it was sent while it was not looking, and then the end.
            }
            fail("the stalled client was never dropped: its connection is still open after sending "
                    + FLOOD_REQUESTS + " requests whose replies were never read");
        } catch (IOException dropped) {
            // The end of the stream, or a reset: either way the server closed its side, which is the
            // answer. A reset rather than an EOF is what a close with unread data in the socket's own
            // receive buffer looks like - this client was still sending when it was dropped.
        }
        for (var client : server.clients()) {
            if (!client.isClosed()) {
                fail("the server still holds a live connection for " + client.peer()
                        + " after dropping the client that stopped reading");
            }
        }
    }

    /** {@code GetGeometry}, built here so the client can send one without waiting for its reply. */
    private static byte[] geometryRequest(int window) {
        byte[] request = new byte[8];
        request[0] = 14;                            // GetGeometry
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeIntAt(request, 4, window);
        return request;
    }
}
