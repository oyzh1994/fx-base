package cn.oyzh.fx.xserver.draw;

import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.draw.ImageCodec;
import cn.oyzh.fx.xserver.draw.PixelStore;
import cn.oyzh.fx.xserver.draw.Rasterizer;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.resource.XFont;
import cn.oyzh.fx.xserver.resource.XGC;
import cn.oyzh.fx.xserver.resource.XPixmap;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The damage a request marks covers every pixel that request changed - and no more of the screen than
 * it has to.
 *
 * <p><b>Why this is a test and not a comment.</b> The display tick presents the rectangle the
 * framebuffer says changed, and every drawing request used to mark the *whole screen*: correct, and
 * useless for the two things that read it. A host window re-uploaded a full frame for a keystroke,
 * and the VNC backend (D2) sent four megabytes for one - which is what that item said would have to
 * be fixed before a viewer was usable. The rasterizer now records what it actually wrote
 * ({@code drawnBounds}), so a pixel that changed outside that rectangle is a pixel left stale on
 * somebody else's screen: a bug with no error, no log line, and no way to notice except by looking.
 *
 * <p>So every primitive is drawn into a fresh surface, the damage is taken, and the pixels are
 * compared against a pristine copy of the same surface: <b>every changed pixel must be inside the
 * damage</b>, and the damage must be smaller than the surface (otherwise the precision is nominal).
 * A primitive that writes without going through {@code writePixel} or a block fill fails here rather
 * than in somebody's viewer.
 */
class DamageTrackingTest {

    private static final int SIZE = 64;
    private static final int BACKGROUND = 0xFF102030;

    @Test
    void everyPrimitiveMarksWhatItChanged() {
        check("a filled rectangle, which takes the block path", gc -> {
        }, rasterizer -> rasterizer.fillRect(10, 12, 20, 8));
        check("a stippled fill, which writes pixel by pixel", gc -> {
            XPixmap stipple = new XPixmap(2, null, null, 2, 2, 1, VisualInfo.staticGray1(1));
            stipple.setPixel(0, 0, 0xFFFFFFFF);
            gc.setFillStyle(XGC.FILL_STIPPLED);
            gc.setStipple(stipple);
        }, rasterizer -> rasterizer.fillRect(10, 12, 20, 8));
        check("a thin line", gc -> {
        }, rasterizer -> rasterizer.drawLine(5, 5, 40, 22));
        check("a wide line", gc -> gc.setLineWidth(7),
                rasterizer -> rasterizer.drawLine(5, 5, 40, 22));
        check("a polyline with round joins", gc -> {
            gc.setLineWidth(5);
            gc.setJoinStyle(XGC.JOIN_ROUND);
        }, rasterizer -> rasterizer.drawPolyline(new int[] {4, 4, 30, 50, 55, 6}));
        check("a polygon", gc -> {
        }, rasterizer -> rasterizer.fillPolygon(new int[] {8, 8, 50, 20, 30, 55}, false));
        check("a filled arc", gc -> {
        }, rasterizer -> rasterizer.fillArc(10, 10, 40, 40, 0, 90 * 64));
        check("an arc outline", gc -> {
        }, rasterizer -> rasterizer.drawArc(10, 10, 40, 40, 0, 360 * 64));
        check("points", gc -> {
        }, rasterizer -> rasterizer.drawPoints(new int[] {3, 3, 20, 40, 60, 60}));
        check("a rectangle outline", gc -> {
        }, rasterizer -> rasterizer.drawRect(6, 6, 30, 20));
        check("text", gc -> {
        }, rasterizer -> {
            XFont font = new XFont(1, null, "fixed");
            rasterizer.drawText8(font, 4, 30, "Hg".getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                    0, 2);
        });
        check("a copy", gc -> {
        }, rasterizer -> {
            XPixmap source = new XPixmap(2, null, null, 8, 8, 24, VisualInfo.trueColor24(1));
            source.fillRect(0, 0, 8, 8, 0xFFFFFFFF);
            rasterizer.copyArea(source, 0, 0, 30, 30, 8, 8);
        });
    }

    /**
     * A request whose drawing is clipped away entirely wakes nobody.
     *
     * <p>Not the same promise as "an idempotent write marks nothing": drawing a colour that is
     * already there does mark, because the rasterizer wrote the pixel and does not read it back to
     * ask whether the value changed. That costs a repaint of the rectangle and is right; what this
     * pins is that a request with nothing on screen at all - the common case for a client that draws
     * outside its own window - does not make the display tick present a frame.
     */
    /**
     * {@code PutImage}, which has its own way of getting this wrong.
     *
     * <p>The image codec used to build a rasterizer of its own from the store and the GC - so a put
     * image was drawn with **no clip** (over whatever covered the window it went into) and the
     * region it wrote was recorded nowhere, which after this change would have meant a put image
     * that never appeared on the screen at all. It now draws through the caller's rasterizer, and
     * this is the test that says so.
     */
    @Test
    void anImagePutIntoASurfaceMarksThePixelsItWrote() {
        Framebuffer target = new Framebuffer(SIZE, SIZE, BACKGROUND);
        target.pixels();
        target.takeDamage();
        XPixmap pristine = pixmap();

        // Four by four opaque red pixels, ZPixmap, little endian: B, G, R, unused.
        byte[] data = new byte[4 * 4 * 4];
        for (int i = 0; i < 16; i++) {
            data[i * 4 + 2] = (byte) 0xFF;
        }
        ImageCodec.putImage(target, new Rasterizer(target, gc()), 20, 20, 4, 4,
                ImageCodec.ZPIXMAP, 0, 24, data, 0, false);

        Rectangle damage = target.takeDamage();
        assertNotNull(damage, "a put image changed pixels and marked nothing");
        for (int[] pixel : changedPixels(pristine, target)) {
            assertTrue(damage.contains(pixel[0], pixel[1]),
                    "pixel " + pixel[0] + "," + pixel[1] + " changed outside " + damage);
        }
        assertTrue(damage.width <= 16 && damage.height <= 16, "the damage should be the image: "
                + damage);
    }

    @Test
    void aRequestWhoseDrawingIsClippedAwayMarksNothing() {
        Framebuffer target = new Framebuffer(SIZE, SIZE, BACKGROUND);
        target.pixels();
        target.takeDamage();
        new Rasterizer(target, gc()).fillRect(200, 200, 20, 20);
        Rectangle damage = target.takeDamage();
        assertTrue(damage == null || damage.isEmpty(),
                "a request that drew nothing on this surface must not mark it, saw " + damage);
    }

    /**
     * A batch of damage tells the display once, not once per mark.
     *
     * <p>{@link Framebuffer#setDamageListener} is what wakes the display loop when a client draws,
     * and it is called on the thread that drew - so a client marking damage a hundred times a second
     * must not cost a hundred wakes for the frame that will pick all of them up. The listener is told
     * on the first mark of a batch, and again only once that batch has been taken.
     */
    @Test
    void aBatchOfDamageWakesTheDisplayOnce() {
        Framebuffer target = new Framebuffer(SIZE, SIZE, BACKGROUND);
        target.pixels();
        target.takeDamage();
        int[] wakes = {0};
        target.setDamageListener(() -> wakes[0]++);

        target.markDirty(1, 1, 5, 5);
        target.markDirty(6, 6, 9, 9);
        target.markDirty(0, 0, SIZE, SIZE);
        assertEquals(1, wakes[0], "three marks of one batch are one wake, not three");

        target.takeDamage();
        target.markDirty(2, 2, 3, 3);
        assertEquals(2, wakes[0], "damage after the batch was taken is a wake of its own");
    }

    /**
     * Draws one primitive into a fresh surface, then holds the damage to the same standard the
     * rasterizer is: coverage first, precision second.
     */
    private static void check(String what, Consumer<XGC> setup, Consumer<Rasterizer> primitive) {
        // A Framebuffer, not a pixmap: damage tracking is the framebuffer's, and it is the surface
        // the display tick presents from.
        Framebuffer target = new Framebuffer(SIZE, SIZE, BACKGROUND);
        XPixmap pristine = pixmap();
        // Allocate first: the first write allocates the pixels, and allocating counts the whole
        // surface as damaged - which is the framebuffer's own rule (E4) and not this request's.
        target.pixels();
        target.takeDamage();

        XGC gc = gc();
        setup.accept(gc);
        primitive.accept(new Rasterizer(target, gc));

        Rectangle damage = target.takeDamage();
        List<int[]> changed = changedPixels(pristine, target);
        assertTrue(!changed.isEmpty(), what + ": the primitive should have changed something");
        assertNotNull(damage, what + ": it changed pixels and marked no damage at all");
        for (int[] pixel : changed) {
            assertTrue(damage.contains(pixel[0], pixel[1]),
                    what + ": pixel " + pixel[0] + "," + pixel[1] + " changed outside the damage "
                            + damage + ", so it would stay stale on any screen shown this");
        }
        assertTrue((long) damage.width * damage.height < (long) SIZE * SIZE,
                what + ": the damage should be the shape, not the surface: " + damage);
    }

    private static List<int[]> changedPixels(PixelStore before, PixelStore after) {
        List<int[]> changed = new ArrayList<>();
        for (int y = 0; y < before.height(); y++) {
            for (int x = 0; x < before.width(); x++) {
                if (before.pixel(x, y) != after.pixel(x, y)) {
                    changed.add(new int[] {x, y});
                }
            }
        }
        return changed;
    }

    private static XPixmap pixmap() {
        XPixmap pixmap = new XPixmap(1, null, null, SIZE, SIZE, 24, VisualInfo.trueColor24(1));
        pixmap.fillRect(0, 0, SIZE, SIZE, BACKGROUND);
        return pixmap;
    }

    private static XGC gc() {
        XGC gc = new XGC(1, null, null);
        gc.setForegroundArgb(0xFF00FF00);
        return gc;
    }
}
