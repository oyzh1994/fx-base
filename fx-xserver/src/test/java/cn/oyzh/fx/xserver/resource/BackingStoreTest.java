package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Backing store: no window is asked to draw again what it never lost.
 *
 * <p>Before B5's general store this was the difference the {@code backing_store} attribute bought: a
 * window that asked for {@code WhenMapped} or {@code Always} had its pixels saved before something
 * covered it and got them back with no {@code Expose}, while one with the default attribute was
 * painted with its background and told to draw. Now <b>every</b> window keeps its own pixels, so no
 * window is asked to draw again on a reveal - which is what a reference server with backing store
 * always does, and what makes a frame per top level possible (C8).
 *
 * <p>The third test is the half that used to make the attribute safe: a window that <em>draws</em>
 * while it is covered. Its drawing used to be clipped to what showed, so the saved pixels stopped
 * being its picture and it had to be asked to draw again. Now the drawing goes into its own store
 * whether it shows or not, so what it drew while covered is what appears when it is uncovered - and
 * there is still nothing to expose.
 */
class BackingStoreTest {

    /** A display number nothing else in the suite uses; the server listens on TCP 6000 + it. */
    private static final int DISPLAY = 81;

    private static final int EXPOSURE_MASK = 1 << 15;

    /** What a window is created with: X's own 24 bit colour, which this server reads as opaque. */
    private static final int WHITE_RGB = 0xFFFFFF;
    private static final int BLACK_RGB = 0x000000;

    /** And the same colours the way {@code pixelAt} answers them, ARGB and opaque. */
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    /** {@code WhenMapped}, which is the capability a server advertises when it has one. */
    private static final int WHEN_MAPPED = 1;

    /** How long to wait before deciding that nothing more is coming. */
    private static final int QUIET_MILLIS = 200;

    private XServer server;
    private RawXClient client;

    @BeforeEach
    void startServer() throws Exception {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
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
    void aWindowThatAskedForOneIsNotAskedToDrawWhatItKept() throws Exception {
        int lower = lowerWindow(WHEN_MAPPED);
        quiet();
        int upper = coverAndUncover(lower);

        assertNull(client.readEvent(),
                "the window asked for a backing store and was asked to draw anyway");
        assertEquals(WHITE, client.pixelAt(lower, 50, 50),
                "and the pixels it kept are the ones that were under the window that covered it");
        assertNotNull(upper, "the covering window existed");
    }

    @Test
    void aWindowWithNoBackingStoreKeepsItsPixelsToo() throws Exception {
        int lower = lowerWindow(0);
        quiet();
        coverAndUncover(lower);

        assertNull(client.readEvent(),
                "every window keeps its own pixels now, so the attribute no longer decides whether"
                        + " the client is asked to draw again");
        assertEquals(WHITE, client.pixelAt(lower, 50, 50),
                "and its pixels are the ones that were under the window that covered it");
    }

    @Test
    void aWindowThatDrewWhileItWasCoveredKeepsWhatItDrew() throws Exception {
        int lower = lowerWindow(WHEN_MAPPED);
        int gc = client.allocateId();
        client.createGC(gc, client.root(), BLACK_RGB);
        client.flush();
        quiet();

        int upper = coverAndUncover(lower, gc);

        assertNull(client.readEvent(),
                "it drew into its own store rather than into what showed, so nothing was lost and"
                        + " there is nothing to expose");
        assertEquals(BLACK, client.pixelAt(lower, 50, 50),
                "what it drew while it was covered is what it shows once it is uncovered");
        assertNotNull(upper, "the covering window existed");
    }

    // ------------------------------------------------------------------

    /** A window that shows white, with the backing store the test is about, mapped and quiet. */
    private int lowerWindow(int backingStore) throws Exception {
        int lower = client.allocateId();
        byte[] request = new byte[40];
        request[0] = 1;                             // CreateWindow
        request[1] = 0;                             // depth: copy from the parent
        RawXClient.writeShortAt(request, 2, request.length / 4);
        RawXClient.writeIntAt(request, 4, lower);
        RawXClient.writeIntAt(request, 8, client.root());
        RawXClient.writeShortAt(request, 16, 200);  // width
        RawXClient.writeShortAt(request, 18, 200);  // height
        RawXClient.writeShortAt(request, 22, 1);    // InputOutput
        // CWBackPixel | CWBackingStore, whose values follow in the order the bits are numbered in.
        RawXClient.writeIntAt(request, 28, 0x2 | 0x40);
        RawXClient.writeIntAt(request, 32, WHITE_RGB);
        RawXClient.writeIntAt(request, 36, backingStore);
        client.sendRequest(request);
        client.selectInput(lower, EXPOSURE_MASK);
        client.mapWindow(lower);
        client.flush();
        quiet();
        return lower;
    }

    /**
     * Covers the middle of {@code lower} with a black window and takes it away again, leaving
     * whatever the server did about the reveal for the caller to read.
     */
    private int coverAndUncover(int lower) throws Exception {
        return coverAndUncover(lower, -1);
    }

    private int coverAndUncover(int lower, int gc) throws Exception {
        int upper = client.allocateId();
        client.createWindow(upper, client.root(), 50, 50, 100, 100, BLACK_RGB);
        client.mapWindow(upper);
        client.flush();
        quiet();
        // Read through the windows themselves: a window's own pixels are what GetImage answers for
        // it, and the root's answer stops where its children begin - which is the whole point of the
        // region this server clips every drawable by.
        assertEquals(BLACK, client.pixelAt(upper, 50, 50), "the covering window shows");

        if (gc >= 0) {
            // The window draws while it is covered: into its own store, the whole of it, whether or
            // not it shows (B5).
            client.fillRectangle(lower, gc, 0, 0, 200, 200);
            client.flush();
            quiet();
        }

        client.unmapWindow(upper);
        client.flush();
        return upper;
    }

    /** Reads and drops whatever is left, so a test only reads what its own step produced. */
    private void quiet() throws Exception {
        client.readTimeoutMillis(QUIET_MILLIS);
        while (client.readEvent() != null) {
            // Quiet is the end of it.
        }
        client.readTimeoutMillis(5000);
    }
}
