package cn.oyzh.fx.xserver.handler;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * What a window paints stays inside the part of it that is visible.
 *
 * <p>Windows draw straight into one framebuffer, so a window that ignores the window on top of it
 * erases it - and since the covered window is never told to draw again, it stays erased. That is
 * what makes a dialog vanish when the window underneath repaints, with no client involved.
 */
public class WindowObscuringTest {

    private static final int LOWER = 0xFF203040;
    private static final int UPPER = 0xFF807060;
    /** A frame's content window, which is what actually shows in the frame's area. */
    private static final int CONTENT = 0xFF1020AA;
    /** Where nothing is drawn the screen's own default shows: what a revealed area falls back to. */
    private static final int ROOT_COLOUR = Screen.DEFAULT_BACKGROUND_ARGB;

    private static XWindow window(Screen screen, XWindow parent, int id, int x, int y, int w,
                                  int h, int colour) {
        XWindow window = new XWindow(id, null, screen, parent, 24, XWindow.INPUT_OUTPUT,
                screen.rootVisual(), x, y, w, h, 0);
        window.setBackgroundArgb(colour);
        window.setBorderArgb(colour);
        return window;
    }

    @Test
    void aWindowThatNamesNoColoursOfItsOwnGetsTheDefaultOnes() {
        XServer server = new XServer(ServerOptions.parse(
                new String[]{"--display", "95", "--backend", "headless"}));
        Screen screen = server.screen();
        Framebuffer framebuffer = screen.framebuffer();

        // A window that names neither a background nor a border colour, and a bordered one: this is
        // what a client that paints nothing of its own - xeyes is one - ends up looking like.
        XWindow plain = new XWindow(0x400001, null, screen, server.rootWindow(), 24,
                XWindow.INPUT_OUTPUT, screen.rootVisual(), 10, 10, 40, 30, 0);
        server.resources().add(plain);
        server.windows().mapWindow(plain);
        XWindow bordered = new XWindow(0x400002, null, screen, server.rootWindow(), 24,
                XWindow.INPUT_OUTPUT, screen.rootVisual(), 60, 10, 40, 30, 2);
        server.resources().add(bordered);
        server.windows().mapWindow(bordered);

        assertEquals(Screen.DEFAULT_BACKGROUND_ARGB, framebuffer.pixel(20, 20),
                "a window with no background of its own shows the screen's default");
        // The x,y a CreateWindow names is the inside of the border, so the window at (60,10) with a
        // 2 pixel border keeps that border in the ring (60,10)..(61,11).
        assertEquals(Screen.DEFAULT_BORDER_ARGB, framebuffer.pixel(60, 10),
                "and its border the colour that gives it an edge");
    }

    @Test
    void aWindowUnderneathDoesNotPaintOverTheOneOnTop() {
        XServer server = new XServer(ServerOptions.parse(
                new String[]{"--display", "97", "--backend", "headless"}));
        Screen screen = server.screen();
        Framebuffer framebuffer = screen.framebuffer();
        XWindow lower = window(screen, server.rootWindow(), 0x400001, 10, 10, 60, 60, LOWER);
        server.resources().add(lower);
        server.windows().mapWindow(lower);
        XWindow upper = window(screen, server.rootWindow(), 0x400002, 40, 40, 20, 20, UPPER);
        server.resources().add(upper);
        server.windows().mapWindow(upper);

        // The window on top has its pixels ...
        assertEquals(UPPER, framebuffer.pixel(45, 45));
        // ... and the one underneath repainting the whole of itself leaves them alone.
        server.windows().paintBackground(lower, lower.absoluteBounds());

        assertEquals(UPPER, framebuffer.pixel(45, 45), "the covered square belongs to the window "
                + "on top and must survive the one below repainting");
        assertEquals(LOWER, framebuffer.pixel(15, 15), "the visible part is the lower window's");
    }

    @Test
    void unmappingAWindowGivesItsAreaBackToTheWindowUnderneath() {
        XServer server = new XServer(ServerOptions.parse(
                new String[]{"--display", "96", "--backend", "headless"}));
        Screen screen = server.screen();
        Framebuffer framebuffer = screen.framebuffer();
        XWindow lower = window(screen, server.rootWindow(), 0x400001, 10, 10, 60, 60, LOWER);
        server.resources().add(lower);
        server.windows().mapWindow(lower);
        XWindow upper = window(screen, server.rootWindow(), 0x400002, 40, 40, 20, 20, UPPER);
        server.resources().add(upper);
        server.windows().mapWindow(upper);
        assertEquals(UPPER, framebuffer.pixel(45, 45));

        server.windows().unmapWindow(upper, true);

        assertEquals(LOWER, framebuffer.pixel(45, 45),
                "the uncovered square is repainted with the lower window's background");
        assertEquals(ROOT_COLOUR, framebuffer.pixel(70, 70), "the root shows where nothing is left");
        assertNotEquals(UPPER, framebuffer.pixel(45, 45));
    }

    /**
     * The shape every toolkit's toplevel and dialog has: a frame whose content window covers it.
     *
     * <p>The frame's own visible region is then empty - its child takes all of it - so giving back
     * "what the window showed" computed from that region gives back nothing, and the dialog's
     * picture stays on the screen after its window has gone. Measured with termora: the close
     * button of a settings dialog appeared to need two clicks, because the first one closed the
     * window and what the user went on aiming at was the picture it left behind.
     */
    @Test
    void unmappingAFrameGivesBackTheAreaItsContentWindowCovered() {
        XServer server = new XServer(ServerOptions.parse(
                new String[]{"--display", "97", "--backend", "headless"}));
        Screen screen = server.screen();
        Framebuffer framebuffer = screen.framebuffer();
        XWindow lower = window(screen, server.rootWindow(), 0x400001, 10, 10, 60, 60, LOWER);
        server.resources().add(lower);
        server.windows().mapWindow(lower);
        XWindow frame = window(screen, server.rootWindow(), 0x400002, 30, 30, 20, 20, UPPER);
        server.resources().add(frame);
        server.windows().mapWindow(frame);
        XWindow content = window(screen, frame, 0x400003, 0, 0, 20, 20, CONTENT);
        server.resources().add(content);
        server.windows().mapWindow(content);
        assertEquals(CONTENT, framebuffer.pixel(35, 35), "the content window is what shows");
        assertEquals(List.of(), frame.visibleRegion(), "and the frame owns not one pixel of it");

        server.windows().unmapWindow(frame, true);

        assertEquals(LOWER, framebuffer.pixel(35, 35),
                "the area the content window covered goes back to the window underneath");
    }

    @Test
    void aNewWindowOnTopKeepsWhatItWasDrawnWith() {
        XServer server = new XServer(ServerOptions.parse(
                new String[]{"--display", "95", "--backend", "headless"}));
        Screen screen = server.screen();
        Framebuffer framebuffer = screen.framebuffer();
        XWindow lower = window(screen, server.rootWindow(), 0x400001, 10, 10, 60, 60, LOWER);
        server.resources().add(lower);
        server.windows().mapWindow(lower);

        // The root paints its own background over everything, as an expose would, and the window on
        // top is not touched by it.
        XWindow upper = window(screen, server.rootWindow(), 0x400002, 40, 40, 20, 20, UPPER);
        server.resources().add(upper);
        server.windows().mapWindow(upper);
        server.windows().paintBackground(server.rootWindow(), server.rootWindow().absoluteBounds());

        assertEquals(UPPER, framebuffer.pixel(45, 45), "the root must not paint over its children");
        assertEquals(LOWER, framebuffer.pixel(15, 15));
    }
}
