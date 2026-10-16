package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.resource.PictFormat;
import cn.oyzh.fx.xserver.resource.Picture;
import cn.oyzh.fx.xserver.resource.XWindow;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code SubwindowMode}: whether a picture over a window sees the window's children.
 *
 * <p>The default, {@code ClipByChildren}, is what a toolkit wants when it draws a window: the window's
 * own pixels stop where a child begins, so a frame whose content window covers it has no visible
 * region of its own at all. {@code IncludeInferiors} is the other question a client asks - "give me
 * what this window *shows*, children and all" - which is what copying the screen through a parent
 * window, a screenshot tool or a "grab my parent's pixels" trick amounts to. It was stored and never
 * consulted, so every picture behaved as {@code ClipByChildren} and such a copy came back empty.
 *
 * <p>The window rectangle still bounds it and what is stacked above the window is still excluded:
 * {@code IncludeInferiors} is about the window's own children, not about the screen.
 */
class IncludeInferiorsTest {

    private static final int ROOT = 200;

    /** The parent's own colour, and the child's: two values that cannot be confused. */
    private static final int PARENT_COLOUR = 0xFF00FF00;
    private static final int CHILD_COLOUR = 0xFF0000FF;

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
     * window's own coordinates (B5). The store is where the window's own pixels live, which is what
     * makes {@code IncludeInferiors} a question about the children rather than about the screen.
     */
    private static Picture picture(Screen screen, XWindow window) {
        return new Picture(9, null, window,
                PictFormat.direct(1, screen.rootVisual(), 0), screen, window.ownPixels(),
                0, 0, window);
    }

    private Screen screen;
    private XWindow root;
    private XWindow parent;
    private XWindow child;

    /** A parent with a child inside it, each painted a colour of its own. */
    private void build() {
        screen = screen();
        root = window(screen, null, 1, 0, 0, ROOT, ROOT);
        parent = window(screen, root, 2, 0, 0, 100, 100);
        child = window(screen, parent, 3, 10, 10, 50, 50);
        picture(screen, parent).writePixel(2, 2, PARENT_COLOUR);
        picture(screen, child).writePixel(2, 2, CHILD_COLOUR);
    }

    private Picture over(int subwindowMode) {
        Picture picture = picture(screen, parent);
        picture.setSubwindowMode(subwindowMode);
        picture.refreshClip();
        return picture;
    }

    /** {@code picture.sample} takes a point, so a pixel is sampled at its centre. */
    private static int sample(Picture picture, int x, int y) {
        return picture.sample(x + 0.5, y + 0.5);
    }

    @Test
    void theDefaultDoesNotSeeTheChild() {
        build();
        Picture picture = over(Picture.CLIP_BY_CHILDREN);

        assertEquals(PARENT_COLOUR, sample(picture, 2, 2), "its own pixels are still its own");
        assertEquals(0, sample(picture, 12, 12),
                "ClipByChildren stops where the child begins: that pixel is the child's, and the "
                        + "window has nothing of its own to answer with");
    }

    @Test
    void includingInferiorsSeesTheChild() {
        build();
        Picture picture = over(Picture.INCLUDE_INFERIORS);

        assertEquals(PARENT_COLOUR, sample(picture, 2, 2), "and its own pixels are still its own");
        assertEquals(CHILD_COLOUR, sample(picture, 12, 12),
                "IncludeInferiors asks for what the window shows, and the child is part of that");
    }

    @Test
    void includingInferiorsStillStopsAtWhatIsCovered() {
        build();
        Picture picture = over(Picture.INCLUDE_INFERIORS);
        assertEquals(PARENT_COLOUR, sample(picture, 2, 2), "nothing is above it yet");

        // A window over the parent, which is what "covered" means: its pixels are not the parent's to
        // hand out, whatever the subwindow mode says.
        window(screen, root, 4, 0, 0, 100, 100);
        picture.refreshClip();

        assertEquals(0, sample(picture, 2, 2),
                "an obscured window has no pixels of its own to read, children or not");
    }

    @Test
    void includingInferiorsStillStopsAtTheWindowEdges() {
        build();
        Picture picture = over(Picture.INCLUDE_INFERIORS);

        assertEquals(0, sample(picture, 150, 150),
                "outside the window there is nothing: IncludeInferiors widens a window to its "
                        + "children, not to the screen");
        assertEquals(0, sample(picture, -1, -1), "and the corner before it is outside too");
    }
}
