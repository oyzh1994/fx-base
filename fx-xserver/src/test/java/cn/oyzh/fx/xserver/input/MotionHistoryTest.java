package cn.oyzh.fx.xserver.input;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The pointer history a client can ask for.
 *
 * <p>{@code GetMotionEvents} used to answer nothing at all, on the argument that keeping a buffer
 * per window is waste for a request almost nobody makes. What it costs is a client that wants to
 * tell a drag from a click, or to draw a stroke with the shape it had, being told the pointer never
 * moved - and what it costs to keep is a bounded ring per screen, which is what Xorg does too
 * ({@code MOTION_HISTORY_SIZE}).
 *
 * <p>What the history holds is what the pointer <em>did</em>, filed under the window it was in at
 * the time, not what was delivered: a motion swallowed by a grab, or made over another client's
 * window, is still the pointer's history and still belongs to the window it happened in.
 */
class MotionHistoryTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 90;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void theHistoryHoldsTheMotionsThatHappenedInsideTheWindow() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int xtest = client.queryExtension("XTEST");
            int window = client.allocateId();
            client.createWindow(window, client.root(), 100, 100, 200, 200);
            client.mapWindow(window);

            long before = clock();
            client.fakeMotion(xtest, 120, 130);
            client.fakeMotion(xtest, 140, 150);
            client.fakeMotion(xtest, 160, 170);
            client.flush();
            long after = clock();

            List<long[]> events = client.getMotionEvents(window, before, after + 1000);
            assertEquals(3, events.size(), "the three motions inside the window are the answer");
            assertPosition(120, 130, events.get(0));
            assertPosition(140, 150, events.get(1));
            assertPosition(160, 170, events.get(2));
            // The times are the server's own clock - the same one the motion events carry - so a
            // client can compare them with what it has already seen. The server stamps a motion when
            // it reads the request, which is a moment after the client wrote it, so the bracket is
            // the one the query used rather than the instant `after` was read.
            assertTrue(events.get(0)[0] >= before && events.get(2)[0] <= after + 1000,
                    "the times are the ones the events carried");
            assertTrue(events.get(0)[0] <= events.get(1)[0] && events.get(1)[0] <= events.get(2)[0],
                    "and they are in the order the motions happened");

            // A motion outside the window is another window's history, not this one's.
            client.fakeMotion(xtest, 10, 10);
            assertEquals(3, client.getMotionEvents(window, before, after + 1000).size(),
                    "a motion outside the window does not join its history");

            // And the range is the client's: an empty one is the "how many are there" question, and
            // a range the motions are not in answers none of them. The second range starts a minute
            // past the motions rather than a millisecond: the server stamps a motion when it reads
            // the request, which can be after this thread read the clock - so a range that begins
            // just after `after` is one a real motion can still land in, and asserting otherwise is
            // a test that fails one run in four.
            assertEquals(0, client.getMotionEvents(window, before, before).size());
            assertEquals(0, client.getMotionEvents(window, after + 60_000, after + 61_000).size());
        }
    }

    @Test
    void theHistoryStopsAtTheWindowThePointerWasIn() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int xtest = client.queryExtension("XTEST");
            int left = client.allocateId();
            int right = client.allocateId();
            client.createWindow(left, client.root(), 400, 100, 100, 100);
            client.createWindow(right, client.root(), 600, 100, 100, 100);
            client.mapWindow(left);
            client.mapWindow(right);

            long before = clock();
            client.fakeMotion(xtest, 420, 120);
            client.fakeMotion(xtest, 620, 120);
            client.fakeMotion(xtest, 440, 140);
            long after = clock();

            List<long[]> inLeft = client.getMotionEvents(left, before, after + 1000);
            List<long[]> inRight = client.getMotionEvents(right, before, after + 1000);
            assertEquals(2, inLeft.size(), "the two motions over the left window");
            assertEquals(1, inRight.size(), "and the one over the right window");
            assertPosition(420, 120, inLeft.get(0));
            assertPosition(440, 140, inLeft.get(1));
            assertPosition(620, 120, inRight.get(0));
        }
    }

    private static void assertPosition(int x, int y, long[] event) {
        assertEquals(x, event[1], "x");
        assertEquals(y, event[2], "y");
    }

    private static long clock() {
        return System.currentTimeMillis() & 0xFFFFFFFFL;
    }
}
