package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.resource.ShapeState;
import cn.oyzh.fx.xserver.resource.XWindow;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A window's visible region is its rectangle minus everything stacked above it.
 *
 * <p>This is what drawing is clipped to. Without it a window paints over whatever is on top of it,
 * and the covered window - which is never told it was covered - does not draw again: a dialog
 * disappears the moment the window under it repaints.
 */
public class WindowVisibilityTest {

    private static final int ROOT = 100;

    private static Screen screen() {
        VisualInfo visual = VisualInfo.trueColor24(0x200);
        return new Screen(0, ROOT, ROOT, List.of(visual), 24, visual.id());
    }

    private static XWindow window(Screen screen, XWindow parent, int id, int x, int y, int w,
                                  int h) {
        return new XWindow(id, null, screen, parent, 24, XWindow.INPUT_OUTPUT,
                screen.rootVisual(), x, y, w, h, 0);
    }

    /** Total area of a region, which is what a clipped rectangle is worth asserting on. */
    private static int area(List<Rectangle> region) {
        int total = 0;
        for (Rectangle rectangle : region) {
            total += rectangle.width * rectangle.height;
        }
        return total;
    }

    @Test
    void aWindowOnItsOwnIsItsOwnRectangle() {
        Screen screen = screen();
        XWindow root = window(screen, null, 1, 0, 0, ROOT, ROOT);
        root.setMapped(true);
        XWindow lower = window(screen, root, 2, 10, 10, 60, 60);
        lower.setMapped(true);

        List<Rectangle> region = lower.visibleRegion();

        assertEquals(60 * 60, area(region));
        assertTrue(ShapeState.containsPoint(region, 10, 10));
        assertTrue(ShapeState.containsPoint(region, 69, 69));
        assertFalse(ShapeState.containsPoint(region, 70, 70), "outside its own rectangle");
    }

    @Test
    void aWindowOnTopTakesItsRectangleOutOfTheOneBelow() {
        Screen screen = screen();
        XWindow root = window(screen, null, 1, 0, 0, ROOT, ROOT);
        root.setMapped(true);
        XWindow lower = window(screen, root, 2, 10, 10, 60, 60);
        lower.setMapped(true);
        XWindow upper = window(screen, root, 3, 40, 40, 20, 20);
        upper.setMapped(true);

        List<Rectangle> region = lower.visibleRegion();

        assertEquals(60 * 60 - 20 * 20, area(region));
        assertFalse(ShapeState.containsPoint(region, 45, 45), "the covered square is not ours");
        assertTrue(ShapeState.containsPoint(region, 39, 45));
        assertTrue(ShapeState.containsPoint(region, 61, 45));
        // The window on top keeps all of itself.
        assertEquals(20 * 20, area(upper.visibleRegion()));
        assertTrue(lower.isAbove(upper) == false && upper.isAbove(lower));
    }

    @Test
    void stackingThemTheOtherWayRoundSwapsWhatIsVisible() {
        Screen screen = screen();
        XWindow root = window(screen, null, 1, 0, 0, ROOT, ROOT);
        root.setMapped(true);
        XWindow lower = window(screen, root, 2, 10, 10, 60, 60);
        lower.setMapped(true);
        XWindow upper = window(screen, root, 3, 40, 40, 20, 20);
        upper.setMapped(true);

        // Put the one that was on top at the bottom of the stack.
        root.children().remove(upper);
        root.children().add(0, upper);

        assertEquals(60 * 60, area(lower.visibleRegion()), "nothing covers it any more");
        assertEquals(20 * 20 - 20 * 20, area(upper.visibleRegion()), "it is covered completely");
        assertTrue(lower.isAbove(upper));
    }

    @Test
    void anUnmappedWindowCoversNothing() {
        Screen screen = screen();
        XWindow root = window(screen, null, 1, 0, 0, ROOT, ROOT);
        root.setMapped(true);
        XWindow lower = window(screen, root, 2, 10, 10, 60, 60);
        lower.setMapped(true);
        XWindow upper = window(screen, root, 3, 40, 40, 20, 20);

        assertEquals(60 * 60, area(lower.visibleRegion()));
    }

    @Test
    void anAncestorAndTheScreenStillClipTheRegion() {
        Screen screen = screen();
        XWindow root = window(screen, null, 1, 0, 0, ROOT, ROOT);
        root.setMapped(true);
        XWindow parent = window(screen, root, 2, 20, 20, 40, 40);
        parent.setMapped(true);
        // A child that sticks out of its parent by 30 pixels on both axes.
        XWindow child = window(screen, parent, 3, 30, 30, 40, 40);
        child.setMapped(true);

        List<Rectangle> region = child.visibleRegion();

        assertEquals(10 * 10, area(region));
        assertTrue(ShapeState.containsPoint(region, 55, 55));
        assertFalse(ShapeState.containsPoint(region, 61, 61), "outside the parent");
        assertTrue(child.isAbove(parent), "a child draws inside and above its parent");
        assertFalse(parent.isAbove(child));
    }
}
