package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.resource.PictFormat;
import cn.oyzh.fx.xserver.resource.XWindow;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Where a picture on a window puts its pixels: the window's own coordinates, wherever it is.
 *
 * <p>It used to be the framebuffer's coordinates, the window's position added, read once when the
 * picture was created. That is wrong for every window placed after its picture exists - which is how
 * AWT opens a dialog: it creates the window at the origin of the screen and only then configures it
 * where it belongs. The stale position was used for the clip as well as the address, and since the two
 * then disagreed with the window's real rectangle the writes were not merely misplaced, they were
 * refused: the picture was asked to draw a dialog and the dialog kept the background it was cleared
 * to, with not one pixel of its contents. Measured against termora: the 消息 box of its keymap
 * settings is 305x130, created at (0,0) and configured to (497,335); the composite of its contents
 * reported 0 of 39650 pixels written until the origin was re-read per request.
 *
 * <p>Since a window keeps its own pixels (B5), there is no translation left to go stale: a picture
 * over a window addresses that window's store in the window's own coordinates, and the composite that
 * ends the request puts them on the screen where the window now is. The root is the only drawable
 * whose picture still carries a screen translation, and the root never moves.
 */
class PictureOriginTest {

    private static final int ROOT = 900;

    private static Screen screen() {
        VisualInfo visual = VisualInfo.trueColor24(0x200);
        return new Screen(0, ROOT, ROOT, List.of(visual), 24, visual.id());
    }

    private static XWindow window(Screen screen, XWindow parent, int id, int x, int y, int w,
                                  int h) {
        XWindow window = new XWindow(id, null, screen, parent, 24, XWindow.INPUT_OUTPUT,
                screen.rootVisual(), x, y, w, h, 0);
        window.setMapped(true);
        return window;
    }

    /**
     * A picture over a window, as {@code CreatePicture} builds one: on the window's own store, in the
     * window's own coordinates (B5).
     */
    private static Picture picture(Screen screen, XWindow window) {
        return new Picture(9, null, window,
                PictFormat.direct(1, screen.rootVisual(), 0), screen, window.ownPixels(),
                0, 0, window);
    }

    @Test
    void aPictureIsAddressedInTheWindowsOwnCoordinates() {
        Screen screen = screen();
        XWindow root = window(screen, null, 1, 0, 0, ROOT, ROOT);
        XWindow dialog = window(screen, root, 2, 0, 0, 305, 130);
        Picture contents = picture(screen, dialog);

        // The window is placed where the client actually wants it, after its picture exists.
        dialog.setPosition(497, 335);
        contents.refreshClip();

        contents.writePixel(0, 0, 0xFF112233);
        contents.writePixel(304, 129, 0xFF445566);

        assertEquals(0xFF112233, contents.pixelAt(0, 0),
                "the window's own pixel at its own origin, wherever the window was placed");
        assertEquals(0xFF445566, contents.pixelAt(304, 129), "and at its far corner");
    }

    @Test
    void aPictureOverAWindowNeverAddressesTheScreen() {
        Screen screen = screen();
        XWindow root = window(screen, null, 1, 0, 0, ROOT, ROOT);
        XWindow dialog = window(screen, root, 2, 0, 0, 305, 130);
        Picture contents = picture(screen, dialog);
        dialog.setPosition(497, 335);
        contents.refreshClip();

        contents.writePixel(0, 0, 0xFF112233);

        // The screen holds its own background, untouched: writing a window's pixels does not reach
        // it - the composite that ends the request is what puts them there (Compositor.finish).
        assertEquals(Screen.DEFAULT_BACKGROUND_ARGB, screen.framebuffer().pixel(497, 335),
                "writing a window's pixels does not touch the screen");
        assertEquals(Screen.DEFAULT_BACKGROUND_ARGB, screen.framebuffer().pixel(0, 0),
                "and nothing is left at the place the window was created at either");
    }
}
