package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.resource.XWindow;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.extension.ShapeExtension;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A bounding shape takes pixels away from a window, so drawing into it stays inside the shape.
 *
 * <p>Only the input shape used to be honoured, which made SHAPE look implemented: a client could set
 * a shape, hit-test through it, ask for its extents - and the window's corners were still painted.
 * That is the whole point of the extension on a server with no compositor: a rounded popup, a shaped
 * splash, anything that is not a rectangle, reaches the screen as its shape and nothing else.
 *
 * <p>A bounding shape <em>removes</em> pixels rather than clipping on top of them, and it applies to
 * the window's children too: a child that falls outside its parent's shape is not on screen either.
 * Both halves are what these tests pin.
 */
class ShapeClippingTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 87;

    /** The colour the client fills with, and the background a window shows where the fill cannot land. */
    private static final int FILL = 0x00FF00;
    private static final int BACKGROUND = Screen.DEFAULT_BACKGROUND_ARGB & 0xFFFFFF;

    private static final int KIND_BOUNDING = 0;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        // --single-window so the client's windows are on the screen this test reads pixels from: in
        // the default mode every connection gets a screen and framebuffer of its own, and the
        // master's framebuffer would be empty however well the client drew.
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix", "--single-window"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Sets a window's bounding shape through the extension, in process.
     *
     * <p>What the shape <em>does</em> is what most of these tests are for, and setting it in process
     * keeps them about that. The wire encoding has its own test now
     * ({@link #aShapeSetWithOneRectangleOverTheWireTakesEffect}): {@code ShapeRectangles} used to be
     * refused as a bad request, which was a bug in the generated reader rather than in this server's
     * shape handling, and that test is the one that would catch it coming back.
     */
    private static void shape(int windowId, Rectangle rectangle) {
        XWindow window = (XWindow) server.resources().get(windowId);
        new ShapeExtension().setShape(server, window, KIND_BOUNDING, List.of(rectangle), 0, 0);
    }

    /**
     * A shape set the way a client really sets one: {@code ShapeRectangles} over the wire.
     *
     * <p>This request was refused as a bad request for as long as the extension has existed, and the
     * cause was arithmetic in the generated reader rather than anything here: it sizes the rectangle
     * list with {@code withInitialCapacity(length - javaStart)}, where the length is in <em>four byte
     * units</em> and the offset is in bytes, so a request with fewer than six rectangles computes a
     * negative capacity and never reaches the handler. Measured against a live server: one rectangle
     * is refused ({@code initial capacity cannot be less than 0} in the log) and ten are accepted -
     * which is why it read as "sometimes works" for so long, and why the kit's hand-written request
     * (its README recorded the refusal as unexplained) was written off as the tool's fault.
     *
     * <p><b>One rectangle</b> is what this test sends, deliberately: that is the size that failed,
     * and a client shaping a window with a handful of rectangles is the ordinary case.
     */
    @Test
    void aShapeSetWithOneRectangleOverTheWireTakesEffect() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            int gc = client.allocateId();
            client.createWindow(window, client.root(), 500, 100, 40, 40);
            client.mapWindow(window);
            client.createGC(gc, client.root(), FILL);
            sync(client, window);

            int shape = client.queryExtension("SHAPE");
            assertTrue(shape != 0, "the extension this test is about");
            client.shapeRectangles(shape, window, KIND_BOUNDING, 0, new int[][] {{10, 10, 10, 10}});
            sync(client, window);

            client.fillRectangle(window, gc, 0, 0, 40, 40);
            sync(client, window);

            Screen screen = server.screen();
            assertEquals(FILL, screen.framebuffer().pixel(515, 115) & 0xFFFFFF,
                    "a point inside the one rectangle shape is painted");
            assertEquals(BACKGROUND, screen.framebuffer().pixel(502, 102) & 0xFFFFFF,
                    "and a corner the shape took away is not");
        }
    }

    /**
     * The same shape, built the other way a client builds one: an XFIXES region of one rectangle,
     * handed to the window with {@code SetWindowShapeRegion}.
     *
     * <p>{@code XFixesCreateRegion} carries the same broken arithmetic with a smaller fixed part -
     * its list starts eight bytes in, so zero, one or two rectangles were refused - and this is the
     * path GTK and Qt take when they shape a window themselves. The region is not tested on its own
     * because a region that is never applied draws nothing: what a client sees is the shape, so that
     * is what is asserted.
     */
    @Test
    void anXfixesRegionOfOneRectangleShapesTheWindow() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            int region = client.allocateId();
            int gc = client.allocateId();
            client.createWindow(window, client.root(), 700, 100, 40, 40);
            client.mapWindow(window);
            client.createGC(gc, client.root(), FILL);
            sync(client, window);

            int xfixes = client.queryExtension("XFIXES");
            assertTrue(xfixes != 0, "the extension this test is about");
            client.createRegion(xfixes, region, new int[][] {{10, 10, 10, 10}});
            client.setWindowShapeRegion(xfixes, window, KIND_BOUNDING, region);
            sync(client, window);

            client.fillRectangle(window, gc, 0, 0, 40, 40);
            sync(client, window);

            Screen screen = server.screen();
            assertEquals(FILL, screen.framebuffer().pixel(715, 115) & 0xFFFFFF,
                    "a point inside the region's one rectangle is painted");
            assertEquals(BACKGROUND, screen.framebuffer().pixel(702, 102) & 0xFFFFFF,
                    "and a corner the region took away is not");
        }
    }

    /**
     * A region that is <em>changed</em> rather than created: {@code XFixesSetRegion} with one
     * rectangle, on a region that was made with three.
     *
     * <p>The same broken arithmetic is in this request's generated reader, so one and two rectangles
     * were refused while three were accepted (measured against a live server: two refused with
     * {@code initial capacity cannot be less than 0}, three decoded). A client that keeps a region
     * and recalculates it - which is what a toolkit does with a shape it re-derives on every resize
     * - uses exactly this call, and the shape on screen is where the result shows.
     */
    @Test
    void aRegionChangedToOneRectangleShapesTheWindow() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            int region = client.allocateId();
            int gc = client.allocateId();
            client.createWindow(window, client.root(), 900, 100, 40, 40);
            client.mapWindow(window);
            client.createGC(gc, client.root(), FILL);
            sync(client, window);

            int xfixes = client.queryExtension("XFIXES");
            client.createRegion(xfixes, region, new int[][] {{0, 0, 20, 20}, {20, 0, 20, 20},
                    {0, 20, 40, 20}});
            client.setWindowShapeRegion(xfixes, window, KIND_BOUNDING, region);
            sync(client, window);
            client.fillRectangle(window, gc, 0, 0, 40, 40);
            sync(client, window);

            Screen screen = server.screen();
            assertEquals(FILL, screen.framebuffer().pixel(910, 130) & 0xFFFFFF,
                    "the three rectangles cover the window's bottom half too");

            // Now the same region, redefined by one small rectangle near the top left. Changing a
            // region does not move a window's shape by itself - a shape is set from a region, not
            // bound to it - so the client applies it again, which is what a toolkit does.
            client.setRegion(xfixes, region, new int[][] {{10, 10, 10, 10}});
            client.setWindowShapeRegion(xfixes, window, KIND_BOUNDING, region);
            sync(client, window);

            assertEquals(FILL, screen.framebuffer().pixel(915, 115) & 0xFFFFFF,
                    "the point inside the new rectangle keeps the client's paint");
            assertEquals(BACKGROUND, screen.framebuffer().pixel(910, 130) & 0xFFFFFF,
                    "and what the old rectangles covered is handed back");
        }
    }

    /**
     * Nothing is synchronous between this thread and the server's: a request that is answered proves
     * everything sent before it has been processed, which is what a framebuffer assertion needs.
     */
    private static void sync(RawXClient client, int window) throws IOException {
        client.queryTree(window);
    }

    @Test
    void drawingStaysInsideTheShape() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            int gc = client.allocateId();
            client.createWindow(window, client.root(), 100, 100, 40, 40);
            sync(client, window);                       // the server must have made it first
            shape(window, new Rectangle(10, 10, 10, 10));
            client.mapWindow(window);
            client.createGC(gc, client.root(), FILL);

            client.fillRectangle(window, gc, 0, 0, 40, 40);
            sync(client, window);

            Screen screen = server.screen();
            assertEquals(FILL, screen.framebuffer().pixel(115, 115) & 0xFFFFFF,
                    "a point inside the shape is painted");
            assertEquals(BACKGROUND, screen.framebuffer().pixel(102, 102) & 0xFFFFFF,
                    "and a corner the shape took away is not");
            assertEquals(BACKGROUND, screen.framebuffer().pixel(138, 138) & 0xFFFFFF,
                    "nor the far corner");
        }
    }

    @Test
    void aShapeSetAfterMappingHandsTheAreaBack() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            int gc = client.allocateId();
            client.createWindow(window, client.root(), 300, 100, 40, 40);
            client.mapWindow(window);
            sync(client, window);
            client.createGC(gc, client.root(), FILL);

            // Painted unshaped, so the corners are the client's paint to begin with.
            client.fillRectangle(window, gc, 0, 0, 40, 40);
            sync(client, window);
            Screen screen = server.screen();
            assertEquals(FILL, screen.framebuffer().pixel(302, 102) & 0xFFFFFF,
                    "an unshaped window takes the paint everywhere");

            // Shaping it afterwards has to take those pixels away again, not leave them painted:
            // the area belongs to whatever is underneath once the window no longer covers it.
            shape(window, new Rectangle(10, 10, 10, 10));
            sync(client, window);

            assertEquals(BACKGROUND, screen.framebuffer().pixel(302, 102) & 0xFFFFFF,
                    "the corner the shape took away goes back to what is underneath");
            assertEquals(FILL, screen.framebuffer().pixel(315, 115) & 0xFFFFFF,
                    "and what is still inside the shape keeps the client's paint");
        }
    }

    @Test
    void aChildOutsideItsParentsShapeIsNotOnScreen() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int parent = client.allocateId();
            int child = client.allocateId();
            int gc = client.allocateId();
            client.createWindow(parent, client.root(), 200, 300, 40, 40);
            sync(client, parent);
            shape(parent, new Rectangle(0, 0, 20, 40));
            client.mapWindow(parent);
            client.createWindow(child, parent, 25, 0, 10, 10);
            client.mapWindow(child);

            client.createGC(gc, client.root(), FILL);
            client.fillRectangle(child, gc, 0, 0, 10, 10);
            sync(client, child);

            Screen screen = server.screen();
            assertEquals(BACKGROUND, screen.framebuffer().pixel(230, 305) & 0xFFFFFF,
                    "the child is outside the parent's shape, so its paint does not land");
        }
    }

    /**
     * A shape set the way a toolkit sets one when it has no rectangle list to hand: a one bit mask
     * pixmap, which is what {@code XShapeCombineMask} sends and what xeyes shapes its window with.
     *
     * <p>The mask is built exactly as xeyes builds its own - a depth 1 pixmap, a GC whose foreground
     * is 0 to clear it and 1 to set the shape - because the failure this test is for is a mask whose
     * set bits never reach the window: the corners stay painted and the window is a rectangle again.
     */
    @Test
    void aBoundingShapeSetFromAOneBitMaskClipsDrawing() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int window = client.allocateId();
            int mask = client.allocateId();
            int maskGc = client.allocateId();
            int gc = client.allocateId();
            client.createWindow(window, client.root(), 1100, 100, 40, 40);
            client.mapWindow(window);
            client.createPixmap(mask, client.root(), 40, 40, 1);
            client.createGC(maskGc, client.root(), 0);
            client.changeGC(maskGc, RawXClient.GCFOREGROUND, 0);
            client.fillRectangle(mask, maskGc, 0, 0, 40, 40);
            client.changeGC(maskGc, RawXClient.GCFOREGROUND, 1);
            client.fillRectangle(mask, maskGc, 10, 10, 20, 20);
            sync(client, window);

            int shape = client.queryExtension("SHAPE");
            assertTrue(shape != 0, "the extension this test is about");
            client.shapeMask(shape, window, KIND_BOUNDING, 0, 0, 0, mask);
            sync(client, window);

            client.createGC(gc, client.root(), FILL);
            client.fillRectangle(window, gc, 0, 0, 40, 40);
            sync(client, window);

            Screen screen = server.screen();
            assertEquals(FILL, screen.framebuffer().pixel(1115, 115) & 0xFFFFFF,
                    "a point whose mask bit is set is painted");
            assertEquals(BACKGROUND, screen.framebuffer().pixel(1102, 102) & 0xFFFFFF,
                    "and a corner the mask cleared is not");
        }
    }
}
