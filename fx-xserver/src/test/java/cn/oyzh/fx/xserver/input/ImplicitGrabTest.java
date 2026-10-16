package cn.oyzh.fx.xserver.input;

import cn.oyzh.fx.xserver.input.InputState;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.event.EventMasks;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * A button press pins the pointer to the window it landed on until every button is released.
 *
 * <p>This is X11's implicit grab, and dragging depends on it: the window being moved follows the
 * pointer, so the pointer is regularly outside it for a moment, and a client that stops being told
 * about motion mid-drag never sees the drag end - it stays in its moving state and ignores every
 * click afterwards.
 */
public class ImplicitGrabTest {

    /** {@code GrabModeSync} and {@code GrabModeAsync}, as {@code X.h} numbers them. */
    private static final int GRAB_SYNC = 0;
    private static final int GRAB_ASYNC = 1;

    private static XWindow window(Screen screen, XWindow parent, int id, int x, int y, int w,
                                  int h) {
        return new XWindow(id, null, screen, parent, 24, XWindow.INPUT_OUTPUT,
                screen.rootVisual(), x, y, w, h, 0);
    }

    private static XServer serverWithAWindow() {
        XServer server = new XServer(ServerOptions.parse(
                new String[]{"--display", "94", "--backend", "headless"}));
        XWindow window = window(server.screen(), server.rootWindow(), 0x400001, 10, 10, 60, 60);
        server.resources().add(window);
        window.setMapped(true);
        return server;
    }

    @Test
    void aPressHoldsThePointerUntilTheRelease() {
        XServer server = serverWithAWindow();
        InputState input = server.input();
        XWindow window = (XWindow) server.resources().get(0x400001);

        input.pointerMoved(20, 20);
        assertNull(input.implicitGrabWindow(), "nothing is grabbed before a button goes down");

        input.buttonChanged(1, true, 20, 20);
        assertEquals(window, input.implicitGrabWindow(), "the press pins the pointer to the window");

        // The pointer leaves the window: events still belong to it.
        input.pointerMoved(90, 90);
        assertEquals(window, input.implicitGrabWindow());

        input.buttonChanged(1, false, 90, 90);
        assertNull(input.implicitGrabWindow(), "the release lets the pointer go");
    }

    @Test
    void theGrabLastsUntilTheLastButtonIsUp() {
        XServer server = serverWithAWindow();
        InputState input = server.input();
        XWindow window = (XWindow) server.resources().get(0x400001);

        input.buttonChanged(1, true, 20, 20);
        input.buttonChanged(3, true, 20, 20);
        assertEquals(window, input.implicitGrabWindow());

        input.buttonChanged(1, false, 20, 20);
        assertEquals(window, input.implicitGrabWindow(), "a second button is still down");

        input.buttonChanged(3, false, 20, 20);
        assertNull(input.implicitGrabWindow());
    }

    /**
     * A client that grabs the pointer for a dialog and destroys the dialog instead of ungrabbing -
     * which is what JavaFX does for a modal dialog - must not leave the pointer stuck. A grab held on
     * a window that no longer exists reports every pointer event against nothing, so the pointer goes
     * dead while the keyboard keeps working, which is exactly what a stuck grab looks like to a user.
     */
    @Test
    void anExplicitGrabEndsWhenItsWindowIsDestroyed() {
        XServer server = serverWithAWindow();
        InputState input = server.input();
        XWindow window = (XWindow) server.resources().get(0x400001);
        input.setPointerGrab(new InputState.Grab(null, window, false, EventMasks.ALL, false,
                GRAB_ASYNC, GRAB_ASYNC, 0));

        input.forgetGrabWindow(window);

        assertNull(input.pointerGrab(), "the grab went with the window");
    }

    @Test
    void aWindowThatGoesAwayReleasesThePointer() {
        XServer server = serverWithAWindow();
        InputState input = server.input();
        XWindow window = (XWindow) server.resources().get(0x400001);

        input.buttonChanged(1, true, 20, 20);
        assertEquals(window, input.implicitGrabWindow());

        server.windows().unmapWindow(window, false);

        assertNull(input.implicitGrabWindow(), "an unmapped window cannot hold the pointer");
    }
}
