package cn.oyzh.fx.xserver.display;

import cn.oyzh.fx.xserver.display.CursorSprite;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.JavaFxBackend;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.draw.Framebuffer;
import javafx.application.Platform;
import javafx.scene.Cursor;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the framebuffer actually reaches the JavaFX image.
 *
 * <p>A window cannot be observed on a machine without a visible display, so the check is done by
 * reading the image back instead: if the pixels arrive here, the only thing left unverified is the
 * compositor, which is the platform's business.
 */
class JavaFxBackendTest {

    private static final int WIDTH = 64;
    private static final int HEIGHT = 48;

    @Test
    void presentsFramebufferToImage() throws Exception {
        skipWithoutJavaFx();

        Screen screen = new Screen(0, WIDTH, HEIGHT,
                List.of(VisualInfo.trueColor24(0x200)), 24, 0x200);
        Framebuffer framebuffer = screen.framebuffer();

        JavaFxBackend backend = new JavaFxBackend("test");
        AtomicReference<DisplayBackend.InputSink> sink = new AtomicReference<>();
        backend.start(screen, 5, new NoopInput());
        // Checked here rather than in a test of its own: stop() exits the toolkit, and a JVM gets
        // one JavaFX runtime, so only one window can be opened per test run.
        assertWindowLeavesThePointerAlone(backend);

        // Paint a recognisable pattern, then present the whole screen.
        framebuffer.fillRect(0, 0, WIDTH, HEIGHT, 0xFF102030);
        framebuffer.fillRect(10, 10, 8, 8, 0xFFEEDDCC);
        backend.present(screen, new Rectangle(0, 0, WIDTH, HEIGHT), null);

        int sampled = readPixel(backend, 12, 12);
        assertEquals(0xFFEEDDCC, sampled,
                "the pattern drawn into the framebuffer should be readable from the JavaFX image");
        assertEquals(0xFF102030, readPixel(backend, 0, 0),
                "the background should have reached the JavaFX image too");

        // **The window is not taken away in the same turn the pointer is handed back to it** (A55).
        // Releasing the pointer is a task the toolkit applies at a pulse, and the platform's own close
        // disposes the window in the same turn - so the release is posted, the window is gone, and the
        // posted task no-ops against a dead scene: the process-wide hide stays and the desktop has no
        // arrow ("点击关闭窗口…鼠标也跟随消失了"). Closing a pulse later is what gives the release a
        // window to land in, and that deferral is the whole of what can be checked from inside a test:
        // right after `stop()`, the window is still there. On a real display the arrow coming back is
        // what is checked - see docs/backlog.md, A55.
        assertTrue(onFxThread(() -> {
            backend.stop();
            return backend.windowStillOpen();
        }), "the window must outlive the turn that hands the pointer back to it");
        assertSame(Cursor.DEFAULT, onFxThread(backend::windowCursor),
                "and the pointer is handed back as the window goes, not hidden with it");
        // And the delay is a pulse, not a leak: the window is taken down once the release has landed.
        long deadline = System.currentTimeMillis() + 5000;
        while (onFxThread(backend::windowStillOpen) && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertFalse(onFxThread(backend::windowStillOpen),
                "the window is still taken down, a pulse after the release");
    }

    /**
     * The frame's damage always covers the sprite's own box, whatever the client repainted.
     *
     * <p>Without it a client whose damage misses the sprite leaves last frame's copy of the arrow in
     * the canvas, and the next frame composites that one again - a pointer that darkens and lightens
     * as the client repaints, which is A21 (EasyShell's SSH tab: the sprite's own pixels alternated
     * between 17 and 102 dark ones across captures, and 17 throughout with this). The app's damage
     * alternates between rectangles that reach the sprite and rectangles that do not, which is why a
     * client that repaints its whole window never showed it.
     */
    @Test
    void theFrameDamageCoversTheSprite() {
        CursorSprite cursor = new CursorSprite(10, 10, 24, 24, 8, 8, new int[24 * 24]);
        // A band of the screen the sprite is not in - the case that used to leave the sprite's copy
        // in the canvas, and so draw the arrow twice.
        Rectangle band = new Rectangle(0, 0, WIDTH, 10);
        Rectangle bounds = new Rectangle(0, 0, WIDTH, HEIGHT);

        Rectangle covered = JavaFxBackend.damageWithSprite(band, cursor, bounds);

        assertTrue(covered.contains(10, 10, 24, 24),
                "the sprite's box has to be repainted from the framebuffer: " + covered);
        assertTrue(covered.contains(0, 0, WIDTH, 10), "and the damage itself is still there");
        assertEquals(band, JavaFxBackend.damageWithSprite(band, null, bounds),
                "with no sprite there is nothing to cover");
    }

    /**
     * A pointer at the edge of the screen is clipped to the window, and the window still opens.
     *
     * <p>The pointer can sit on the very last pixel, so the sprite's box reaches past the screen -
     * and the union of that box with damage that is already inside the screen reaches past it too.
     * `drawImage` then asks Prism to update a destination region outside the canvas and JavaFX throws
     * `IllegalArgumentException: Destination region (x=1268, y=489, w=48, h=25) must fit within
     * texture content bounds (contentWidth=1300, contentHeight=800)`. The report was "sometimes, and
     * then the window never appears", which is the shape of an exception thrown by the paint the
     * window is opened with. The 48 is the union of the sprite's box with the position it came from.
     */
    @Test
    void aSpriteHangingOverTheEdgeIsClippedToWhatTheCanvasCovers() {
        CursorSprite cursor = new CursorSprite(WIDTH - 4, HEIGHT - 4, 24, 24, 8, 8, new int[24 * 24]);
        Rectangle damage = new Rectangle(WIDTH - 10, HEIGHT - 10, 10, 10);
        Rectangle bounds = new Rectangle(0, 0, WIDTH, HEIGHT);

        Rectangle covered = JavaFxBackend.damageWithSprite(damage, cursor, bounds);

        assertTrue(covered.x + covered.width <= WIDTH && covered.y + covered.height <= HEIGHT,
                "the repaint has to stay inside the canvas: " + covered);
        assertTrue(covered.contains(damage), "and it still covers the damage: " + covered);
        assertTrue(covered.contains(WIDTH - 4, HEIGHT - 4), "and the visible part of the sprite");

        // The sprite alone, for the cursor image itself: drawing the whole of it is the same
        // exception on the other `drawImage` this backend makes.
        Rectangle visible = JavaFxBackend.visibleSprite(cursor, bounds);
        assertEquals(4, visible.width, "only the part on the screen is drawn");
        assertEquals(4, visible.height);
        assertTrue(JavaFxBackend.visibleSprite(cursor, new Rectangle(0, 0, 10, 10)).isEmpty(),
                "a pointer on another window is not drawn here at all");
    }

    /**
     * Hiding the platform pointer is a loan, not something the window keeps.
     *
     * <p>On macOS {@link Cursor#NONE} is a process-wide {@code [NSCursor hide]}, undone only when the
     * window's cursor changes away from NONE - so a window that hides the pointer without a way back
     * takes it off the desktop and every other application with it. The pointer is only the server's
     * while it is over the window; anywhere else - outside it, or on a window it no longer shows -
     * the platform's arrow stays. Whether the window is the active one is deliberately not part of
     * it: a freshly opened window is usually not active, and leaving the arrow on top of the
     * client's cursor until the user clicks shows two pointers at once.
     *
     * <p>An image cursor would sidestep the global hide and does not work on this platform: an
     * opaque red 32x32 cursor was measured drawing the standard arrow over the window, so the choice
     * here is not a matter of taste.
     */
    @Test
    void hostCursorHidesThePlatformPointerWheneverItIsOverTheWindow() {
        skipWithoutJavaFx();

        assertSame(Cursor.NONE, JavaFxBackend.hostCursor(true, true),
                "the sprite the server draws is the only pointer over the window");
        assertSame(Cursor.DEFAULT, JavaFxBackend.hostCursor(false, true),
                "the pointer outside the window belongs to the platform");
        // And the platform will not let a background application hide the pointer at all: activating
        // another application puts the system cursor back (measured with a native probe, 2026-10-01),
        // and JavaFX does not even try again. The sprite is what gives way then - see
        // JavaFxBackend.applicationActive - so the pair is one pointer either way.
        assertSame(Cursor.DEFAULT, JavaFxBackend.hostCursor(true, false),
                "an application in the background cannot hide the pointer, and must not try");
    }

    /**
     * The key that leaves full screen is the platform's own convention, and Escape is not it: JavaFX
     * consumes its exit key before the scene sees it, so a forwarded application in a full screen
     * window would never be given its own Escape - which a terminal, an editor and an IDE all need.
     *
     * <p>Which key to use instead is not this server's decision, and offering only the macOS one is
     * what a reader of this project caught: on Windows and the X desktops it is F11, and a full screen
     * window with no key to leave it is a window with no way out.
     */
    @Test
    void theKeyThatLeavesFullScreenIsThePlatformsOwn() {
        assertEquals("Ctrl+Meta+F", JavaFxBackend.keyFor("Mac OS X"), "macOS uses Control-Command-F");
        assertEquals("Ctrl+Meta+F", JavaFxBackend.keyFor("macOS"), "whatever the name says, if it is mac");
        assertEquals("F11", JavaFxBackend.keyFor("Windows 11"), "and Windows uses F11");
        assertEquals("F11", JavaFxBackend.keyFor("Linux"), "as do the X desktops");
        assertEquals("F11", JavaFxBackend.keyFor(null), "a platform that will not say gets the common one");
    }

    /**
     * The window is the content plus whatever is above it, and two different things can be: the
     * client-area header bar of an extended window, which is inside the scene, or the platform's own
     * title bar, which is outside it.
     *
     * <p>The client is shown neither, and a window sized as if it were only the content is short by
     * the header - which is what the first report of this feature turned into: the window opened as a
     * strip thirty points tall (measured 2026-10-04).
     */
    @Test
    void theHeaderBarIsPartOfTheWindowAndNotOfTheContent() {
        assertEquals(374, JavaFxBackend.windowHeight(344, 30, 0),
                "the header bar is added above the content");
        assertEquals(372, JavaFxBackend.windowHeight(344, 0, 28),
                "and the platform's own title bar with it, when there is no header bar");
        assertEquals(344, JavaFxBackend.windowHeight(344, 0, 0),
                "a window with neither is exactly its content");
    }

    /**
     * The content is the scene, and the platform's frame is outside the scene - so measuring the
     * content off the scene must not take the frame off a second time.
     *
     * <p>It did until 2026-10-13, and no macOS run could show it: an extended window there has no
     * frame, so the subtraction was zero. On Windows the extended window keeps an invisible resize
     * border, and the content came out 15x7 small - measured with xclock, the client's window was
     * resized to that short number on every maximize and restore (and never came back to the size it
     * started at), and a band of the scene was left showing down the right and the bottom of the
     * picture, with the old window's box left behind in the framebuffer.
     */
    @Test
    void thePlatformsFrameIsOutsideTheSceneAndIsNotTakenOffTheContentTwice() {
        assertEquals(300, JavaFxBackend.contentExtent(300, 1),
                "a scene of 300 points is 300 framebuffer pixels at scale 1, whatever frame is around it");
        assertEquals(600, JavaFxBackend.contentExtent(300, 2),
                "a scaled display is the pixels per point the client is told about");
        assertEquals(0, JavaFxBackend.contentExtent(-4, 1),
                "a scene smaller than the header bar it carries has no content at all");
    }

    /**
     * A window bigger than the display is not shown by Windows - the part outside it is not drawn and
     * cannot be reached, which is the sides of a forwarded window going missing - so the content is
     * drawn smaller instead, and only when it has to be.
     *
     * <p>Measured 2026-10-13: an 1800x900 xclock, whose screen the server had grown to 3600x1200,
     * wanted a frame 1814x667 points on a display with 1707x912 of usable area, and the right 107
     * points of it were off the screen. At 94% the whole of it fits.
     */
    @Test
    void contentTooLargeForTheDisplayIsDrawnSmallerRatherThanClipped() {
        assertEquals(1.0, JavaFxBackend.fitScale(1300, 700, 1707, 912),
                "content that fits the display is not scaled at all");
        assertEquals(1.0, JavaFxBackend.fitScale(1300, 700, 1300, 700),
                "content that exactly fits is not scaled either");
        assertEquals(1707.0 / 1814.0, JavaFxBackend.fitScale(1814, 667, 1707, 912), 0.0001,
                "the width binds, and the factor is what makes the window as wide as the display");
        assertEquals(912.0 / 1000.0, JavaFxBackend.fitScale(300, 1000, 1707, 912), 0.0001,
                "the height binds when it is the taller of the two");
        assertEquals(1.0, JavaFxBackend.fitScale(0, 700, 1707, 912),
                "a window with no content, or a display with no room, is left alone");
        assertEquals(1.0, JavaFxBackend.fitScale(1300, 700, 0, 0),
                "a display the platform will not describe does not make the content smaller");
    }

    /**
     * Where the pointer really is decides whether the platform pointer is handed back, and a window
     * that moves or changes size under a pointer that does not move is what the enter and leave
     * events miss.
     *
     * <p>Measured 2026-10-04: leaving full screen shrank the window away from a stationary pointer, no
     * {@code MOUSE_EXITED} arrived, and the platform pointer stayed hidden - it then vanished over the
     * desktop and came back only once it was moved into the window again ("full screen is fine, after
     * leaving full screen the mouse disappears unless it is over the xserver window"). The backend
     * asks the platform where the pointer is instead of trusting the events alone, so the two
     * directions of that decision are worth pinning.
     */
    @Test
    void thePointerIsOverAWindowByWhereItIsNotByTheLastEvent() {
        assertTrue(JavaFxBackend.pointerInside(100, 200, 400, 300, 300, 350),
                "a point in the middle is inside");
        assertTrue(JavaFxBackend.pointerInside(100, 200, 400, 300, 100, 200),
                "the top left corner belongs to the window");
        assertTrue(JavaFxBackend.pointerInside(100, 200, 400, 300, 499, 499),
                "so does the last pixel before the right and bottom edge");
        assertFalse(JavaFxBackend.pointerInside(100, 200, 400, 300, 500, 350),
                "the right edge itself is outside");
        assertFalse(JavaFxBackend.pointerInside(100, 200, 400, 300, 300, 500),
                "and so is the bottom edge");
        assertFalse(JavaFxBackend.pointerInside(100, 200, 400, 300, 99, 350),
                "a point to the left of the window is outside");
        assertFalse(JavaFxBackend.pointerInside(100, 200, 400, 300, 300, 199),
                "and one above it");
        // The case that was reported: the window shrinks away from a pointer that has not moved, so
        // the same point that was inside a full screen window is outside the window it came back to.
        assertTrue(JavaFxBackend.pointerInside(0, 37, 1512, 945, 900, 500));
        assertFalse(JavaFxBackend.pointerInside(106, 81, 400, 328, 900, 500));
    }

    /**
     * A window the pointer is not over leaves the platform pointer alone, for the reason above.
     *
     * <p>What the window installs is what is read: the platform effect cannot be observed from a test,
     * since a cursor only reaches the platform when the window is under the pointer - which is now the
     * truth this asks about rather than "has a mouse event arrived". So the window is moved to a corner
     * the pointer is not in first, and what is checked is the rule rather than wherever this session's
     * mouse happens to be sitting (a window the pointer *is* over hides the arrow, which is the whole
     * point of it, and cannot be checked here because it also needs this application to be the active
     * one).
     */
    private static void assertWindowLeavesThePointerAlone(JavaFxBackend backend) throws Exception {
        javafx.geometry.Rectangle2D usable = javafx.stage.Screen.getPrimary().getVisualBounds();
        java.awt.Point pointer = pointerOnScreen();
        if (pointer != null) {
            boolean pointerOnTheLeft = pointer.x < usable.getMinX() + usable.getWidth() / 2;
            boolean pointerAtTheTop = pointer.y < usable.getMinY() + usable.getHeight() / 2;
            backend.setWindowPosition(
                    (int) (pointerOnTheLeft ? usable.getMaxX() - 1 : usable.getMinX()),
                    (int) (pointerAtTheTop ? usable.getMaxY() - 1 : usable.getMinY()));
        }
        assertSame(Cursor.DEFAULT, onFxThread(backend::windowCursor),
                "a window the pointer is not over must not hide the platform pointer");
    }

    /** Where the pointer is, or null where the platform will not say. */
    private static java.awt.Point pointerOnScreen() {
        try {
            java.awt.PointerInfo info = java.awt.MouseInfo.getPointerInfo();
            return info == null ? null : info.getLocation();
        } catch (RuntimeException | Error refused) {
            return null;
        }
    }

    /**
     * Skips on a machine that cannot show a window, which is two different machines.
     *
     * <p>One has no JavaFX on its classpath, and the server falls back to Swing there. The other has
     * JavaFX and no display - a CI runner, a container, a machine reached without a window server -
     * and that is the case this test used to walk into: JavaFX is a dependency, so the class is
     * always there, and the toolkit's own startup then fails with "Unable to open DISPLAY" and takes
     * the build with it. {@code GraphicsEnvironment.isHeadless()} is the question to ask, because it
     * is the platform's answer rather than a guess: true on a Linux runner with no {@code DISPLAY},
     * false on a desktop with one.
     */
    private static void skipWithoutJavaFx() {
        try {
            Class.forName("javafx.scene.image.WritableImage");
        } catch (Throwable e) {
            // No JavaFX on this machine: the server falls back to Swing, and so does this test.
            throw new TestAbortedException("JavaFX is not available", e);
        }
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            throw new TestAbortedException("no display: a window cannot be opened here");
        }
    }

    /** Reads a pixel, on the FX thread, which is the only thread allowed to touch the image. */
    private static int readPixel(JavaFxBackend backend, int x, int y) throws Exception {
        return onFxThread(() -> backend.image().getPixelReader().getArgb(x, y));
    }

    /** Runs a query on the FX thread and hands back what it returned, or what it threw. */
    private static <T> T onFxThread(Callable<T> query) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(query.call());
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "the FX thread did not answer in time");
        Throwable thrown = failure.get();
        if (thrown instanceof Exception e) {
            throw e;
        }
        if (thrown != null) {
            throw new AssertionError(thrown);
        }
        return result.get();
    }

    /**
     * The window shows a sub-rectangle of the screen, so a point in the window is not a point on the
     * screen: the difference is the corner the rectangle starts at. Ignoring it reports every click
     * as far above and left of where it happened as that corner is from the origin - twenty pixels
     * for a client whose window sits at y=20 - and a menu whose items are thirty pixels tall then
     * closes because the position it is told about has left the menu while the pointer has not.
     *
     * <p>No window is needed for this: the conversion is arithmetic, and it is the arithmetic the
     * Swing backend has always done and this one did not.
     */
    @Test
    void aPointInTheWindowIsOffsetByTheCornerOfTheRectangleItShows() {
        assertEquals(120, JavaFxBackend.screenCoordinate(100, 20, 1, 800),
                "a point is measured from where the rectangle starts");
        assertEquals(100, JavaFxBackend.screenCoordinate(100, 0, 1, 800),
                "a rectangle at the origin changes nothing");
        assertEquals(799, JavaFxBackend.screenCoordinate(900, 0, 1, 800),
                "a drag can leave the screen, and is clamped back onto it");
        assertEquals(799, JavaFxBackend.screenCoordinate(790, 20, 1, 800),
                "the offset is applied before the clamp, as the Swing backend has always done");
    }

    /**
     * On a screen of twice the pixels the window is laid out at the size it looks, so a point in it
     * is twice as far into the framebuffer. This is what keeps the pointer where the user put it on
     * a HiDPI display - and what makes the screen worth having, since a client drawing at that
     * resolution is drawn into pixels that reach the display one for one.
     */
    @Test
    void aPointOnAScaledScreenCountsTwiceThePixels() {
        assertEquals(220, JavaFxBackend.screenCoordinate(100, 20, 2, 1600),
                "a logical point is scale framebuffer pixels from the viewport's corner");
        assertEquals(0, JavaFxBackend.screenCoordinate(0, 0, 2, 1600));
        assertEquals(1599, JavaFxBackend.screenCoordinate(900, 0, 2, 1600),
                "and it is still clamped to the framebuffer it lands in");
    }

    /**
     * Dragging the window's edge is the only size change of the content area that is the user's
     * doing; the backend's own resizes must not come back as requests to resize the client.
     *
     * <p>Nothing else distinguishes them: the content area changes size for both, and a request that
     * echoed one of the backend's own would be answered by resizing the client to the size it already
     * has, which never settles.
     */
    @Test
    void onlyASizeTheClientHasNotBeenToldIsAUserResize() {
        Rectangle viewport = new Rectangle(0, 0, 800, 600);
        assertTrue(JavaFxBackend.isUserResize(false, 900, 600, viewport),
                "a wider content area than the client's window is a drag of the edge");
        assertTrue(JavaFxBackend.isUserResize(false, 800, 500, viewport),
                "shrinking is a resize too, and the one a StackPane's centring used to break");
        assertTrue(!JavaFxBackend.isUserResize(true, 900, 600, viewport),
                "the backend sizing the window to the client is not the user resizing it");
        assertTrue(!JavaFxBackend.isUserResize(false, 800, 600, viewport),
                "the size the client has already been given is not a new one");
        assertTrue(!JavaFxBackend.isUserResize(false, 0, 600, viewport),
                "a window with no area yet is not a resize either");
    }

    /**
     * The frame belongs to the platform while the window is full screen or maximized, while a drag of
     * its edge is in flight, and while a gesture of the platform's own (a zoom, or a restore) is
     * still settling; only then may this backend size it.
     *
     * <p>A size set on a window the platform has maximized *un-maximizes* it on Windows (measured
     * 2026-10-13), and the size it is left at - the content plus the frame, which is not the work area
     * - becomes the one the platform then restores to. The client is never told anything, because the
     * frame it is drawn in did not change size, so a maximized window the user restored stayed as
     * large as the maximized one was: the window that "suddenly gets bigger" after a few maximize and
     * restore cycles and never comes back.
     *
     * <p>The settling case is the second half of the same thing, and the one a restore by the header
     * bar's own control reaches (measured the same day): the {@code maximized} flag flips back to
     * false *before* the platform has put the window back, and the frame that runs in between sizes
     * the window to a viewport that is still the maximized one - 1721x919 against the work area's
     * 1721x927 - which is the size it then keeps. The platform's gesture has to be allowed to finish.
     *
     * <p>The last case is the mirror of it, the zoom-in gap: Windows moves and resizes the window to
     * the work area *before* it reports {@code maximized=true}, and for that moment the flags say the
     * window is an ordinary one - so the frame the backend wrote in it was the one the platform ever
     * after restored to, at the position it was written at (measured 2026-10-13: a stretched window
     * came back to {@code -7,-7 499x421}).
     */
    @Test
    void theFrameIsThePlatformsWhileItIsMaximizedOrFullScreen() {
        assertTrue(JavaFxBackend.platformOwnsFrame(true, false, false, false, false),
                "a full screen window's frame is the platform's Space");
        assertTrue(JavaFxBackend.platformOwnsFrame(false, true, false, false, false),
                "and a maximized window's is the work area: a size set on it takes the maximize away");
        assertTrue(JavaFxBackend.platformOwnsFrame(false, false, true, false, false),
                "so is the frame under a drag of its edge, sized from the pointer");
        assertTrue(JavaFxBackend.platformOwnsFrame(false, false, false, true, false),
                "and so is the frame while the platform is still applying its own gesture");
        assertTrue(JavaFxBackend.platformOwnsFrame(false, false, false, false, true),
                "and the frame a zoom has already made but not yet reported");
        assertFalse(JavaFxBackend.platformOwnsFrame(false, false, false, false, false),
                "every other frame is this backend's to size");
    }

//    @Test
//    void prefersEs2OnMacUnlessTheCallerChoseAPipeline() {
//        assertEquals("es2,sw", JavaFxBackend.preferredPrismOrder("Mac OS X", null),
//                "macOS must not default to the Metal pipeline that crashes during live resize");
//        assertEquals("es2,sw", JavaFxBackend.preferredPrismOrder("Mac OS X", "  "),
//                "a blank prism.order is the same as no choice at all");
//        assertNull(JavaFxBackend.preferredPrismOrder("Mac OS X", "sw"),
//                "an explicit pipeline choice must not be overridden");
//        assertNull(JavaFxBackend.preferredPrismOrder("Linux", null),
//                "other platforms keep their normal pipeline selection");
//    }

    /** The server only calls back into this when the pointer moves, which the test never does. */
    private static final class NoopInput implements DisplayBackend.InputSink {
        @Override
        public void pointerMoved(int x, int y) {
        }

        @Override
        public void buttonChanged(int button, boolean pressed, int x, int y) {
        }

        @Override
        public void keyChanged(int keycode, boolean pressed, int x, int y) {
        }

        @Override
        public void focusChanged(boolean focused) {
        }
    }
}
