package cn.oyzh.fx.xserver;

import cn.oyzh.fx.xserver.ScreenSession;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E4: a screen's pixels are allocated when something draws, and not before.
 *
 * <p>In the default mode every connection gets a screen of its own, and a screen used to allocate
 * its framebuffer at the handshake - 4 MB at 1300x800, and 16 MB at {@code --scale 2}. Every
 * connection paid it, including the ones that only ask questions: a probe, {@code xdpyinfo},
 * {@code xdotool} (which opens a connection per invocation). A server that allows 256 clients was
 * therefore holding a gigabyte of framebuffers for connections that had drawn nothing.
 *
 * <p>What is measured here is the allocation and not the heap: {@code Runtime} numbers move with the
 * garbage collector and a test that asserted on them uncollected would fail on a busy machine. The
 * heap is reported as well - see the message on the last test - and the assertions are on the thing
 * that decides it: {@link #settledHeap()} collects first, so the last test weighs what the
 * connections <em>hold</em> rather than what they happened to have allocated.
 */
class IdleScreenMemoryTest {

    /** A display number nothing else in the suite uses. */
    private static final int DISPLAY = 78;

    private XServer server;
    private final List<RawXClient> clients = new ArrayList<>();

    @BeforeEach
    void startServer() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "1300x800", "--backend", "headless", "--no-unix"}));
        server.start();
    }

    @AfterEach
    void stopServer() throws Exception {
        for (RawXClient client : clients) {
            client.close();
        }
        clients.clear();
        server.stop();
    }

    @Test
    void aConnectionThatNeverDrawsAllocatesNoFramebuffer() throws Exception {
        for (int i = 0; i < 4; i++) {
            connect();
        }
        for (ScreenSession session : clientSessions()) {
            assertFalse(session.screen().framebuffer().allocated(),
                    "a client that has asked questions and drawn nothing is not holding a screen's"
                            + " worth of pixels");
        }
    }

    @Test
    void theClientThatDrawsAllocatesOnlyItsOwnScreen() throws Exception {
        RawXClient drawing = connect();
        connect();
        connect();

        int window = drawing.allocateId();
        drawing.createWindow(window, drawing.root(), 0, 0, 200, 200, 0x00FF00);
        drawing.mapWindow(window);
        drawing.flush();

        // The paint happens on the client's request thread, so the allocation is awaited rather
        // than assumed: the first version of this test read the flag before the server had read the
        // request and saw zero.
        long deadline = System.currentTimeMillis() + 4000;
        while (System.currentTimeMillis() < deadline && allocatedScreens() == 0) {
            Thread.sleep(20);
        }

        int allocated = allocatedScreens();
        assertEquals(1, allocated, "one client drew and one screen was allocated for it; the two"
                + " that only connected are still holding nothing");
    }

    @Test
    void andIdleConnectionsBarelyMoveTheHeap() throws Exception {
        connect();
        long before = settledHeap();
        for (int i = 0; i < 20; i++) {
            connect();
        }
        long after = settledHeap();
        long growth = after - before;
        // 20 screens at 1300x800x4 would be 84 MB before this change; what is left is the connection
        // machinery itself, which is a few tens of kilobytes each. The bound is loose on purpose -
        // the point is the order of magnitude, and the number is printed either way.
        assertTrue(growth < 20L * 1024 * 1024,
                "twenty idle connections grew the heap by " + (growth / 1024) + " KB, which is not"
                        + " twenty framebuffers");
        System.out.println("E4: 20 idle connections grew the used heap by " + (growth / 1024) + " KB"
                + " (20 framebuffers would be " + (20L * 1300 * 800 * 4 / 1024) + " KB)");
    }

    // ------------------------------------------------------------------

    private RawXClient connect() throws Exception {
        RawXClient client = RawXClient.connect(DISPLAY);
        clients.add(client);
        return client;
    }

    /** How many client screens have allocated their pixels. */
    private int allocatedScreens() {
        int allocated = 0;
        for (ScreenSession session : clientSessions()) {
            if (session.screen().framebuffer().allocated()) {
                allocated++;
            }
        }
        return allocated;
    }

    /** The sessions of the clients above - the master screen is not one of them. */
    private List<ScreenSession> clientSessions() {
        List<ScreenSession> result = new ArrayList<>();
        for (ScreenSession session : server.sessions()) {
            if (session.client() != null) {
                result.add(session);
            }
        }
        return result;
    }

    private static long usedHeap() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * The heap in use once the garbage has been collected, which is the number this test is about.
     *
     * <p>{@link #usedHeap()} on its own is the live set <em>plus</em> whatever the collector has not
     * got round to, and the whole suite runs in one JVM: measured that way the twenty connections
     * "grew" the heap by 15.9 MB on one run and 20.1 MB on the next - enough to fail the bound - with
     * nothing in either number but what earlier tests had left lying around. A framebuffer is
     * strongly reachable from a live connection, so it survives a collection; what does not survive
     * one was never this test's business.
     */
    private static long settledHeap() throws InterruptedException {
        System.gc();
        Thread.sleep(50);
        return usedHeap();
    }
}
