package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.resource.PictFormat;
import cn.oyzh.fx.xserver.resource.XWindow;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * What a picture remembers about the window it drew into, which is what the composite needs.
 *
 * <p>RENDER draws into a window's own store (B5). Nothing puts it on the screen until the request
 * ends, and {@code Compositor.finish} is that moment: it asks the picture what it wrote and
 * composites exactly that. So the picture has to remember the region as it writes it - per pixel, or
 * the composite would have to copy the window's whole rectangle for every request, which is what it
 * deliberately does not do. The root is the exception and the guard against over-reach: its picture
 * draws straight into the screen, so it remembers nothing and {@code finish} marks the screen the way
 * it always did.
 */
class PictureWindowDrawnTest {

    private static final int ROOT = 400;

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

    private static Picture picture(Screen screen, XWindow window) {
        return new Picture(9, null, window,
                PictFormat.direct(1, screen.rootVisual(), 0), screen, window.ownPixels(),
                0, 0, window);
    }

    @Test
    void theRegionWrittenIsRememberedAndThenForgotten() {
        Screen screen = screen();
        XWindow root = window(screen, null, 1, 0, 0, ROOT, ROOT);
        XWindow target = window(screen, root, 2, 30, 40, 100, 100);
        Picture contents = picture(screen, target);
        contents.refreshClip();

        assertNull(contents.takeWindowDrawn(), "nothing has been written yet");

        contents.writePixel(5, 7, 0xFF112233);
        contents.writePixel(9, 3, 0xFF445566);

        // The bounding box of what was written, in the window's own coordinates - the composite is
        // clipped to the window afterwards, so a box that is too large is only wasted work, while one
        // that is too small loses pixels.
        assertEquals(new Rectangle(5, 3, 5, 5), contents.takeWindowDrawn());
        assertNull(contents.takeWindowDrawn(), "and asking for it clears it, so the next request"
                + " composites only what it drew itself");
    }

    @Test
    void aReadRemembersNothing() {
        Screen screen = screen();
        XWindow root = window(screen, null, 1, 0, 0, ROOT, ROOT);
        XWindow target = window(screen, root, 2, 30, 40, 100, 100);
        Picture contents = picture(screen, target);
        contents.refreshClip();
        contents.writePixel(5, 7, 0xFF112233);
        contents.takeWindowDrawn();

        contents.pixelAt(5, 7);

        assertNull(contents.takeWindowDrawn(),
                "reading a picture is not drawing, and must not schedule a composite");
    }

    @Test
    void aPictureOnTheRootRemembersNothing() {
        Screen screen = screen();
        XWindow root = window(screen, null, 1, 0, 0, ROOT, ROOT);
        // The root is the screen itself: its picture still addresses the framebuffer, so there is
        // nothing to composite afterwards and nothing to remember.
        Picture contents = new Picture(9, null, root,
                PictFormat.direct(1, screen.rootVisual(), 0), screen, screen.framebuffer(),
                0, 0, root);
        contents.refreshClip();

        contents.writePixel(5, 7, 0xFF112233);

        assertNull(contents.takeWindowDrawn(),
                "the root draws straight into the screen, so no composite is owed");
        assertEquals(0xFF112233, screen.framebuffer().pixel(5, 7), "and the pixels are already there");
    }
}
