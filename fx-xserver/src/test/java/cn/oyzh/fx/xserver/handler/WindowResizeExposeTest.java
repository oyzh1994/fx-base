package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A resize owes the client the whole of the window, and a series counts itself down.
 *
 * <p>A resize is a whole window's worth of damage, not the strip the window gained. A server cannot
 * carry a resized window's pixels over, so every resized window is exposed again in full - a growth, a
 * shrink, and a change of one side only alike. Measured against a reference server (Xvfb) with a raw
 * client: a 600x500 window grown to 800x600 is sent *one* Expose of {@code 0,0 800x600 count 0}, and
 * so are a width-only growth, a height-only growth, and a shrink to 400x400.
 *
 * <p>That is what {@code xclock} needs and what this server did not give it. Sent only the strip it
 * had gained, it drew just that strip: xclock frees its backing pixmap on a resize, makes a new one at
 * the new size and paints into it exactly the area its Exposes name, so the rest of a stretched window
 * was left undefined - the black band down the side of a wide xclock, and the clock that came back
 * half drawn when a maximised window was restored.
 *
 * <p>The other half of the contract is the series: the {@code count} field of every Expose but the
 * last says how many more are on the way, which is what Xt's exposure compression reads before it
 * hands a widget one region.
 */
class WindowResizeExposeTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 98;

    private static final int EXPOSE = 12;

    /** ExposureMask, the event a toolkit draws itself from. */
    private static final int EXPOSURE_MASK = 1 << 15;

    /** SHAPE's bounding kind and its {@code Set} operation. */
    private static final int KIND_BOUNDING = 0;
    private static final int SET = 0;

    /** Where an event's {@code window} field sits, in bytes. */
    private static final int WINDOW = 4;

    /** Where an Expose event's fields sit, in bytes: x, y, width, height, and the count. */
    private static final int X = 8;
    private static final int Y = 10;
    private static final int WIDTH = 12;
    private static final int HEIGHT = 14;
    private static final int COUNT = 16;

    @Test
    void aResizeExposesTheWholeWindow() throws Exception {
        XServer server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            client.socketTimeout(1000);
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 600, 500);
            client.selectInput(window, EXPOSURE_MASK);
            client.mapWindow(window);
            client.flush();

            byte[] alone = client.readEvent(EXPOSE);
            assertNotNull(alone, "the map owes the window an Expose for the whole of itself");
            assertExpose(alone, 0, 0, 600, 500, 0);

            client.configureWindow(window, 0, 0, 800, 600);
            client.flush();

            byte[] resized = client.readEvent(EXPOSE);
            assertNotNull(resized, "a resize owes the client an Expose");
            assertExpose(resized, 0, 0, 800, 600, 0);
            assertNull(client.readEvent(EXPOSE),
                    "the whole window goes out as the one Expose, not the strip it gained");
        } finally {
            server.stop();
        }
    }

    /**
     * A shape gain exposes the window's children, not just the window.
     *
     * <p>A bounding shape clips a window's inferiors as much as the window itself: a child's visible
     * region is its own region intersected with every ancestor's shape, so when the shape gains area
     * the child gains it too. The window cannot stand in for the child - a child that covers the
     * window leaves the window's own visible region empty, so the Expose the window is sent names
     * nothing - and a toolkit that is never told (xeyes: the shell carries the eyes' mask, the eyes
     * widget covers the shell) never redraws. Maximised, its eyes stayed the size they had before
     * (2026-10-13).
     */
    @Test
    void aShapeThatGainsAreaExposesTheChildThatCoversTheWindow() throws Exception {
        XServer server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            client.socketTimeout(1000);
            int shell = client.allocateId();
            int widget = client.allocateId();
            client.createWindow(shell, client.root(), 0, 0, 200, 200);
            client.createWindow(widget, shell, 0, 0, 200, 200);
            client.selectInput(shell, EXPOSURE_MASK);
            client.selectInput(widget, EXPOSURE_MASK);
            client.mapWindow(shell);
            client.mapWindow(widget);
            client.flush();
            drainExposes(client);

            int shape = client.queryExtension("SHAPE");
            assertTrue(shape != 0, "the extension this test is about");

            // A shape smaller than the window, so that growing it is a gain the child has to hear
            // about. Taking area away is not: a child draws nothing where it is not shown.
            client.shapeRectangles(shape, shell, KIND_BOUNDING, SET, new int[][] {{40, 40, 20, 20}});
            client.flush();
            drainExposes(client);

            client.shapeRectangles(shape, shell, KIND_BOUNDING, SET, new int[][] {{0, 0, 200, 200}});
            client.flush();

            List<byte[]> series = readExposeSeries(client);
            for (byte[] expose : series) {
                assertEquals(widget, RawXClient.readInt(expose, WINDOW),
                        "the Expose is the covering child's: the shell's own region is empty");
            }
            assertTrue(covers(series, 0, 0) && covers(series, 199, 199),
                    "the series names the area the shape gave back, corners included");
        } finally {
            server.stop();
        }
    }

    /**
     * A gain divided into more pieces than a client can use is sent as one Expose, not a flood.
     *
     * <p>A shape mask leaves a rectangle per scanned row, and nothing merges them: xeyes' eyes at a
     * maximised size are about two thousand rows. A series that long is useless to the client - a
     * toolkit combines a series into one region anyway, whose bounding box is what it draws - and it
     * overflows the connection's own queue of a thousand messages, which drops the client.
     */
    @Test
    void aGainDividedIntoTooManyPiecesIsSentAsItsBoundingRectangle() throws Exception {
        XServer server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            client.socketTimeout(1000);
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 200, 200);
            client.selectInput(window, EXPOSURE_MASK);
            client.mapWindow(window);
            client.flush();
            drainExposes(client);

            int shape = client.queryExtension("SHAPE");
            client.shapeRectangles(shape, window, KIND_BOUNDING, SET,
                    new int[][] {{0, 0, 10, 10}});
            client.flush();
            drainExposes(client);

            // A hundred and one one-pixel-tall stripes: a region whose pieces number far more than
            // any client reads one at a time, and which only a bounding rectangle can describe.
            int[][] stripes = new int[101][4];
            for (int i = 0; i < 100; i++) {
                stripes[i] = new int[] {0, i * 2, 200, 1};
            }
            stripes[100] = new int[] {0, 199, 200, 1};
            client.shapeRectangles(shape, window, KIND_BOUNDING, SET, stripes);
            client.flush();

            List<byte[]> series = readExposeSeries(client);
            assertEquals(1, series.size(),
                    "the whole region goes out as one Expose, not a hundred");
            assertExpose(series.get(0), 0, 0, 200, 200, 0);
        } finally {
            server.stop();
        }
    }

    /** Reads a whole Expose series: the one whose count is zero is the last. */
    private static List<byte[]> readExposeSeries(RawXClient client) throws Exception {
        List<byte[]> series = new ArrayList<>();
        byte[] expose;
        do {
            expose = client.readEvent(EXPOSE);
            assertNotNull(expose, "the series ended while its last Expose still promised more");
            series.add(expose);
        } while (RawXClient.readShort(expose, COUNT) != 0);
        return series;
    }

    /** Reads and throws away the Exposes an earlier step owes, so the next read starts clean. */
    private static void drainExposes(RawXClient client) throws Exception {
        while (client.readEvent(EXPOSE) != null) {
            // Each of these is a series of its own, and the series this test cares about is the one
            // that follows, so they are only carried off the stream.
        }
    }

    /** True when some rectangle in the series holds the point. */
    private static boolean covers(List<byte[]> series, int x, int y) {
        for (byte[] expose : series) {
            int left = RawXClient.readShort(expose, X);
            int top = RawXClient.readShort(expose, Y);
            if (x >= left && x < left + RawXClient.readShort(expose, WIDTH)
                    && y >= top && y < top + RawXClient.readShort(expose, HEIGHT)) {
                return true;
            }
        }
        return false;
    }

    private static void assertExpose(byte[] expose, int x, int y, int width, int height, int count) {
        assertEquals(x, RawXClient.readShort(expose, X), "expose x");
        assertEquals(y, RawXClient.readShort(expose, Y), "expose y");
        assertEquals(width, RawXClient.readShort(expose, WIDTH), "expose width");
        assertEquals(height, RawXClient.readShort(expose, HEIGHT), "expose height");
        assertEquals(count, RawXClient.readShort(expose, COUNT),
                "the count field is how many Exposes are still to come");
    }
}
