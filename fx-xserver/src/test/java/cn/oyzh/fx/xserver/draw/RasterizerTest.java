package cn.oyzh.fx.xserver.draw;

import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.draw.ImageCodec;
import cn.oyzh.fx.xserver.draw.PixelStore;
import cn.oyzh.fx.xserver.draw.Rasterizer;
import cn.oyzh.fx.xserver.resource.XGC;
import cn.oyzh.fx.xserver.resource.XPixmap;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pixel level checks for the rasterizer and the image codec.
 *
 * <p>Every expectation here was taken from an Xorg server answering the same requests, so a
 * failure means this implementation has drifted from what a client sees on a real display. The
 * drawables are pixmaps with a null screen, which is why the tests set colours with the
 * {@code *Argb} setters: those are the only GC setters that consult the screen's visual.
 */
class RasterizerTest {

    private static XPixmap pixmap(int w, int h, int argb) {
        XPixmap pixmap = new XPixmap(1, null, null, w, h, 24, VisualInfo.trueColor24(1));
        pixmap.fillRect(0, 0, w, h, argb);
        return pixmap;
    }

    private static XGC gc() {
        XGC gc = new XGC(1, null, null);
        gc.setForegroundArgb(0xFFFFFFFF);
        return gc;
    }

    private static int lit(PixelStore store) {
        int count = 0;
        for (int y = 0; y < store.height(); y++) {
            for (int x = 0; x < store.width(); x++) {
                if (store.pixel(x, y) != 0xFF000000) {
                    count++;
                }
            }
        }
        return count;
    }

    // ------------------------------------------------------------ raster ops

    @Test
    void rasterOpsFollowTheProtocolTable() {
        // X names the functions after what happens to the source, so AndReverse inverts the
        // destination: src AND NOT dst. Verified against Xorg 21 for both operands.
        int old = 0x0F0F0F;
        int src = 0x00FF00;
        assertEquals(0x00000000, Rasterizer.applyFunction(old, src, XGC.GX_CLEAR));
        assertEquals(0x00000F00, Rasterizer.applyFunction(old, src, XGC.GX_AND));
        assertEquals(0x0000F000, Rasterizer.applyFunction(old, src, XGC.GX_AND_REVERSE));
        assertEquals(0x0000FF00, Rasterizer.applyFunction(old, src, XGC.GX_COPY));
        assertEquals(0x000F000F, Rasterizer.applyFunction(old, src, XGC.GX_AND_INVERTED));
        assertEquals(0x000F0F0F, Rasterizer.applyFunction(old, src, XGC.GX_NOOP));
        assertEquals(0x000FF00F, Rasterizer.applyFunction(old, src, XGC.GX_XOR));
        assertEquals(0x000FFF0F, Rasterizer.applyFunction(old, src, XGC.GX_OR));
        assertEquals(0xFFF000F0, Rasterizer.applyFunction(old, src, XGC.GX_NOR));
        assertEquals(0xFFF00FF0, Rasterizer.applyFunction(old, src, XGC.GX_EQUIV));
        assertEquals(0xFFF0F0F0, Rasterizer.applyFunction(old, src, XGC.GX_INVERT));
        assertEquals(0xFFF0FFF0, Rasterizer.applyFunction(old, src, XGC.GX_OR_REVERSE));
        assertEquals(0xFFFF00FF, Rasterizer.applyFunction(old, src, XGC.GX_COPY_INVERTED));
        assertEquals(0xFFFF0FFF, Rasterizer.applyFunction(old, src, XGC.GX_OR_INVERTED));
        assertEquals(0xFFFFF0FF, Rasterizer.applyFunction(old, src, XGC.GX_NAND));
        assertEquals(0xFFFFFFFF, Rasterizer.applyFunction(old, src, XGC.GX_SET));
    }

    @Test
    void planeMaskKeepsTheRestOfTheDestination() {
        XPixmap target = pixmap(4, 1, 0xFF112233);
        XGC gc = gc();
        gc.setPlaneMask(0x0000FF00);
        new Rasterizer(target, gc).fillRect(0, 0, 4, 1);
        assertEquals(0xFF11FF33, target.pixel(0, 0));
    }

    @Test
    void functionAppliesToTheDestinationValue() {
        XPixmap target = pixmap(4, 1, 0xFF123456);
        XGC gc = gc();
        gc.setFunction(XGC.GX_XOR);
        new Rasterizer(target, gc).fillRect(0, 0, 4, 1);
        assertEquals(0xFF123456 ^ 0xFFFFFFFF, target.pixel(0, 0));
        // Drawing the same thing twice returns the pixel to where it started.
        new Rasterizer(target, gc).fillRect(0, 0, 4, 1);
        assertEquals(0xFF123456, target.pixel(0, 0));
    }

    // ------------------------------------------------------------ clipping

    @Test
    void callerClipIntersectsTheDrawable() {
        XPixmap target = pixmap(8, 4, 0xFF000000);
        new Rasterizer(target, gc(), new Rectangle(2, 1, 3, 2)).fillRect(0, 0, 8, 4);
        assertEquals(0xFFFFFFFF, target.pixel(2, 1));
        assertEquals(0xFFFFFFFF, target.pixel(4, 2));
        assertEquals(0xFF000000, target.pixel(5, 2));
        assertEquals(0xFF000000, target.pixel(2, 3));
    }

    /**
     * A window that is partly covered is drawn only where it shows: the pixels under the window on
     * top belong to that window, and painting over them is what makes a dialog disappear when the
     * window underneath it repaints.
     */
    @Test
    void aCoveredRegionKeepsDrawingOutOfTheCoveredPart() {
        XPixmap target = pixmap(10, 10, 0xFF000000);
        // Visible: the left column and the right two columns of a window that spans the pixmap.
        List<Rectangle> region = List.of(new Rectangle(0, 0, 4, 10), new Rectangle(8, 0, 2, 10));

        new Rasterizer(target, gc(), new Rectangle(0, 0, 10, 10), region).fillRect(0, 0, 10, 10);

        assertEquals(0xFFFFFFFF, target.pixel(3, 5), "a visible piece is painted");
        assertEquals(0xFFFFFFFF, target.pixel(9, 5));
        assertEquals(0xFF000000, target.pixel(5, 5), "the covered piece is left alone");
        assertEquals(0xFF000000, target.pixel(7, 5));
        assertEquals(6 * 10, lit(target));
    }

    @Test
    void clipRectanglesAreRelativeToTheClipOrigin() {
        XPixmap target = pixmap(12, 12, 0xFF000000);
        XGC gc = gc();
        gc.setClipRectangles(List.of(new XGC.Rectangle2(1, 1, 2, 2)), 0);
        gc.setClipOriginX(5);
        gc.setClipOriginY(5);
        new Rasterizer(target, gc).fillRect(0, 0, 12, 12);
        assertEquals(0xFFFFFFFF, target.pixel(6, 6));
        assertEquals(0xFFFFFFFF, target.pixel(7, 7));
        assertEquals(0xFF000000, target.pixel(5, 5));
        assertEquals(4, lit(target));
    }

    @Test
    void clipMaskPixelsAreOffsetByTheClipOrigin() {
        XPixmap mask = new XPixmap(2, null, null, 4, 4, 1, VisualInfo.staticGray1(1));
        // A set bit is white in the pixel values this server stores.
        mask.setPixel(1, 1, 0xFFFFFFFF);
        mask.setPixel(2, 2, 0xFFFFFFFF);
        XPixmap target = pixmap(12, 12, 0xFF000000);
        XGC gc = gc();
        gc.setClipMask(mask);
        gc.setClipOriginX(5);
        gc.setClipOriginY(5);
        new Rasterizer(target, gc).fillRect(0, 0, 12, 12);
        assertEquals(0xFFFFFFFF, target.pixel(6, 6));
        assertEquals(0xFFFFFFFF, target.pixel(7, 7));
        assertEquals(0xFF000000, target.pixel(5, 5));
        assertEquals(0xFF000000, target.pixel(6, 7));
        assertEquals(2, lit(target));
    }

    // ------------------------------------------------------------ fills

    @Test
    void fillRectangleCoversExactlyItsPixels() {
        XPixmap target = pixmap(8, 6, 0xFF000000);
        new Rasterizer(target, gc()).fillRect(1, 1, 6, 4);
        assertEquals(24, lit(target));
        assertEquals(0xFFFFFFFF, target.pixel(1, 1));
        assertEquals(0xFF000000, target.pixel(7, 1));
    }

    @Test
    void tileFillRepeatsFromTheTileOrigin() {
        XPixmap tile = pixmap(2, 2, 0xFF000000);
        tile.setPixel(0, 0, 0xFF111111);
        XPixmap target = pixmap(4, 4, 0xFF000000);
        XGC gc = gc();
        gc.setFillStyle(XGC.FILL_TILED);
        gc.setTile(tile);
        new Rasterizer(target, gc).fillRect(0, 0, 4, 4);
        assertEquals(0xFF111111, target.pixel(0, 0));
        assertEquals(0xFF111111, target.pixel(2, 2));
        assertEquals(0xFF000000, target.pixel(1, 0));
    }

    @Test
    void stippleOnlyPaintsWhereTheStippleIsSet() {
        XPixmap stipple = new XPixmap(2, null, null, 4, 1, 1, VisualInfo.staticGray1(1));
        stipple.setPixel(0, 0, 0xFFFFFFFF);
        stipple.setPixel(2, 0, 0xFFFFFFFF);
        XPixmap target = pixmap(8, 1, 0xFF000000);
        XGC gc = gc();
        gc.setFillStyle(XGC.FILL_STIPPLED);
        gc.setStipple(stipple);
        // The stipple origin shifts the pattern: index = x - origin.
        gc.setStippleOriginX(1);
        new Rasterizer(target, gc).fillRect(0, 0, 8, 1);
        assertEquals(0xFFFFFFFF, target.pixel(1, 0));
        assertEquals(0x00000000 | 0xFF000000, target.pixel(0, 0));
        assertEquals(4, lit(target));
    }

    // ------------------------------------------------------------ lines

    @Test
    void thinLineRunsThroughBothEndPoints() {
        XPixmap target = pixmap(16, 4, 0xFF000000);
        new Rasterizer(target, gc()).drawLine(0, 1, 10, 1);
        assertEquals(11, lit(target));
    }

    @Test
    void wideLinesUseTheRectangleXorgDraws() {
        // Width 3 from 0 to 20: rows -1..1 around the line and the far end point excluded.
        XPixmap target = pixmap(24, 6, 0xFF000000);
        XGC gc = gc();
        gc.setLineWidth(3);
        new Rasterizer(target, gc).drawLine(0, 2, 20, 2);
        assertEquals(3, rowsWithInk(target));
        assertEquals(20, lit(target) / 3);
        assertEquals(0xFFFFFFFF, target.pixel(0, 1));
        assertEquals(0xFF000000, target.pixel(20, 2));
        // An even width sits one row higher on the screen, as it does on an X server.
        XPixmap even = pixmap(24, 6, 0xFF000000);
        gc.setLineWidth(2);
        new Rasterizer(even, gc).drawLine(0, 2, 20, 2);
        assertEquals(0xFFFFFFFF, even.pixel(0, 1));
        assertEquals(0xFFFFFFFF, even.pixel(0, 2));
        assertEquals(0xFF000000, even.pixel(0, 3));
    }

    private static int rowsWithInk(PixelStore store) {
        int rows = 0;
        for (int y = 0; y < store.height(); y++) {
            for (int x = 0; x < store.width(); x++) {
                if (store.pixel(x, y) != 0xFF000000) {
                    rows++;
                    break;
                }
            }
        }
        return rows;
    }

    @Test
    void buttCapsStopOnTheEndPoints() {
        XPixmap target = pixmap(24, 8, 0xFF000000);
        XGC gc = gc();
        gc.setLineWidth(3);
        new Rasterizer(target, gc).drawLine(5, 4, 12, 4);
        // The rectangle runs from 5 up to but not including 12.
        assertEquals(0xFFFFFFFF, target.pixel(5, 4));
        assertEquals(0xFFFFFFFF, target.pixel(9, 4));
        assertEquals(0xFF000000, target.pixel(12, 4));
    }

    @Test
    void dashesAlternateFromTheDashOffset() {
        XPixmap target = pixmap(32, 2, 0xFF000000);
        XGC gc = gc();
        gc.setLineWidth(1);
        gc.setLineStyle(XGC.LINE_ON_OFF_DASH);
        gc.setDashes(new byte[]{4, 4});
        new Rasterizer(target, gc).drawLine(0, 0, 31, 0);
        assertEquals("####....####....####....####....", row(target, 0));
        // An offset of two starts two pixels into the first dash.
        gc.setDashOffset(2);
        XPixmap shifted = pixmap(32, 2, 0xFF000000);
        new Rasterizer(shifted, gc).drawLine(0, 0, 31, 0);
        assertEquals("##....####....####....####....#.", row(shifted, 0));
    }

    private static String row(PixelStore store, int y) {
        StringBuilder text = new StringBuilder();
        for (int x = 0; x < store.width(); x++) {
            text.append(store.pixel(x, y) == 0xFF000000 ? '.' : '#');
        }
        return text.toString();
    }

    // ------------------------------------------------------------ shapes

    @Test
    void rectangleOutlineIsOnePixelLargerThanTheFilledRectangle() {
        // PolyRectangle corners are (x,y) and (x+w,y+h): a 6 by 4 rectangle draws a 7 by 5 ring.
        XPixmap target = pixmap(12, 8, 0xFF000000);
        new Rasterizer(target, gc()).drawRect(1, 1, 6, 4);
        assertEquals(20, lit(target));
        assertEquals(0xFFFFFFFF, target.pixel(7, 5));
        assertEquals(0xFF000000, target.pixel(2, 2));
    }

    @Test
    void polygonFillUsesTheEvenOddOrWindingRule() {
        XPixmap target = pixmap(16, 12, 0xFF000000);
        int[] square = {1, 1, 10, 1, 10, 10, 1, 10};
        new Rasterizer(target, gc()).fillPolygon(square, false);
        assertEquals(81, lit(target));
        int[] triangle = {0, 0, 10, 0, 0, 10};
        XPixmap wedged = pixmap(16, 12, 0xFF000000);
        new Rasterizer(wedged, gc()).fillPolygon(triangle, false);
        assertEquals(55, lit(wedged));
        // A five pointed star covers more with the winding rule than with even odd.
        int[] star = {6, 0, 9, 11, 0, 4, 12, 4, 3, 11};
        XPixmap evenOdd = pixmap(16, 12, 0xFF000000);
        new Rasterizer(evenOdd, gc()).fillPolygon(star, false);
        XPixmap winding = pixmap(16, 12, 0xFF000000);
        new Rasterizer(winding, gc()).fillPolygon(star, true);
        // **Xorg's own numbers, and this is the assertion that caught the half-pixel scanline**: the
        // filler here used to take each scanline's crossings at the pixel centre (y + 0.5) where the
        // reference takes them at y, which on a concave shape whose edges cross at every kind of
        // fraction changes the *area* by six pixels. The old test proved it: it asserted 28 and 41 -
        // this server's own answers - while its comment already recorded that the reference answers
        // 34 and 47. Both numbers are now measured against Xvfb over the whole star (tools/PolyProbe,
        // whose eighth cell is this shape and whose output is identical on the two servers).
        assertEquals(34, lit(evenOdd));
        assertEquals(47, lit(winding));
        assertTrue(lit(winding) > lit(evenOdd));
    }

    /**
     * The scanline's crossings are taken at {@code y}, not at the pixel's centre {@code y + 0.5}.
     *
     * <p>That half pixel is worth up to half a pixel of slope in x, and the two cases below are the
     * two ways it shows. Both numbers are Xvfb's, read off {@code tools/PolyProbe} - whose second
     * cell is this triangle - and the shape is the one the 0.8% {@code xlogo} difference came down
     * to: a triangle whose hypotenuse has slope 2, so its crossings land on an integer at every
     * scanline (the first case) and on a fraction just above one (the second).
     */
    @Test
    void polygonCrossingsAreSampledAtTheScanlineAndNotAtItsCentre() {
        // (2,2) (46,2) (2,23): the hypotenuse runs from (46,2) to (2,23), 44 across and 21 down.
        int[] triangle = {2, 2, 46, 2, 2, 23};
        XPixmap target = pixmap(48, 24, 0xFF000000);
        new Rasterizer(target, gc()).fillPolygon(triangle, false);

        // Row 2: the crossing is x = 46 exactly, and the reference fills through x 45. A crossing
        // taken half a scanline lower is at 43.9, whose span stops at 43 - two pixels short.
        assertEquals(0xFFFFFFFF, target.pixel(45, 2), "the reference's last pixel on row 2");
        assertEquals(0xFF000000, target.pixel(46, 2), "and nothing past it");
        // Row 12: the crossing is x = 24 exactly, and the reference fills through x 25.
        assertEquals(0xFFFFFFFF, target.pixel(25, 12), "the reference's last pixel on row 12");
        assertEquals(0xFF000000, target.pixel(26, 12), "and nothing past it");
        // The left edge is vertical, so both conventions agree there - which is what makes the two
        // assertions above about the hypotenuse rather than about a shifted shape.
        assertEquals(0xFFFFFFFF, target.pixel(2, 12));
        assertEquals(0xFF000000, target.pixel(1, 12));
    }

    @Test
    void filledArcStaysInsideItsBoundingBox() {
        XPixmap target = pixmap(16, 16, 0xFF000000);
        new Rasterizer(target, gc()).fillArc(0, 0, 10, 10, 0, 360 * 64);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (target.pixel(x, y) != 0xFF000000) {
                    assertTrue(x <= 9 && y <= 9, "fill escaped the bounding box at " + x + "," + y);
                }
            }
        }
        // A full disc of diameter ten covers roughly 75 pixels, the area of the circle.
        assertTrue(lit(target) > 60 && lit(target) < 90, "area " + lit(target));
        // An arc of a quarter sweep has the chord of that quarter.
        XPixmap chord = pixmap(16, 16, 0xFF000000);
        new Rasterizer(chord, gc()).fillArc(1, 1, 20, 20, 0, 90 * 64);
        assertTrue(lit(chord) < lit(target), "chord fill should be smaller than a disc");
    }

    // ------------------------------------------------------------ copies

    @Test
    void copyAreaSurvivesOverlap() {
        XPixmap target = pixmap(8, 2, 0xFF000000);
        for (int x = 0; x < 4; x++) {
            target.setPixel(x, 0, 0xFF100000 + x);
        }
        new Rasterizer(target, gc()).copyArea(target, 0, 0, 2, 0, 4, 1);
        assertEquals(0xFF100000, target.pixel(2, 0));
        assertEquals(0xFF100001, target.pixel(3, 0));
        assertEquals(0xFF100002, target.pixel(4, 0));
        assertEquals(0xFF100003, target.pixel(5, 0));
    }

    @Test
    void copyPlaneExpandsOneBitToAllBits() {
        XPixmap source = pixmap(4, 1, 0xFF000000);
        source.setPixel(1, 0, 0xFFFFFFFF);
        XPixmap target = pixmap(4, 1, 0xFF000000);
        new Rasterizer(target, gc()).copyPlane(source, 0, 0, 0, 0, 4, 1, 0);
        assertEquals(0xFFFFFFFF, target.pixel(1, 0));
        assertEquals(0x00000000, target.pixel(0, 0));
    }

    // ------------------------------------------------------------ image codec

    @Test
    void zPixmapRoundTripsInBothByteOrders() {
        XPixmap target = pixmap(2, 1, 0xFF000000);
        // Little endian client: the bytes are blue, green, red, unused.
        byte[] data = {(byte) 0x33, (byte) 0x22, (byte) 0x11, 0, (byte) 0xAA, (byte) 0xBB,
                (byte) 0xCC, 0};
        ImageCodec.putImage(target, new Rasterizer(target, gc()), 0, 0, 2, 1, ImageCodec.ZPIXMAP, 0, 24, data, 0, false);
        assertEquals(0xFF112233, target.pixel(0, 0));
        assertEquals(0xFFCCBBAA, target.pixel(1, 0));
        byte[] out = ImageCodec.getImage(target, 0, 0, 2, 1, ImageCodec.ZPIXMAP, 0xFFFFFFFF, 24,
                VisualInfo.trueColor24(1), false);
        assertArrayEquals(data, out);

        XPixmap other = pixmap(1, 1, 0xFF000000);
        ImageCodec.putImage(other, new Rasterizer(other, gc()), 0, 0, 1, 1, ImageCodec.ZPIXMAP, 0, 24,
                new byte[]{0x00, 0x11, 0x22, 0x33}, 0, true);
        assertEquals(0xFF112233, other.pixel(0, 0));
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], "byte " + i);
        }
    }

    @Test
    void putImageGoesThroughTheGcFunction() {
        XPixmap target = pixmap(1, 1, 0xFFF0F0F0);
        XGC gc = gc();
        gc.setFunction(XGC.GX_XOR);
        byte[] data = {(byte) 0xE4, (byte) 0x0C, (byte) 0xF0, 0};
        ImageCodec.putImage(target, new Rasterizer(target, gc), 0, 0, 1, 1, ImageCodec.ZPIXMAP, 0, 24, data, 0, false);
        assertEquals(0xFFF0F0F0 ^ 0xFFF00CE4, target.pixel(0, 0));
    }

    @Test
    void putImageHonoursThePlaneMask() {
        XPixmap target = pixmap(1, 1, 0xFF00FF00);
        XGC gc = gc();
        gc.setPlaneMask(0x00FF0000);
        byte[] data = {(byte) 0xE4, (byte) 0x0C, (byte) 0xF0, 0};
        ImageCodec.putImage(target, new Rasterizer(target, gc), 0, 0, 1, 1, ImageCodec.ZPIXMAP, 0, 24, data, 0, false);
        assertEquals(0xFFF0FF00, target.pixel(0, 0));
    }

    @Test
    void xyBitmapPacksBitsLeastSignificantFirst() {
        XPixmap target = pixmap(8, 2, 0xFF000000);
        byte[] data = {(byte) 0xAA, 0, 0, 0, (byte) 0xC0, 0, 0, 0};
        ImageCodec.putImage(target, new Rasterizer(target, gc()), 0, 0, 8, 2, ImageCodec.XY_BITMAP, 0, 1, data, 0, false);
        assertEquals(".#.#.#.#", row(target, 0));
        assertEquals("......##", row(target, 1));
        byte[] back = ImageCodec.getImage(target, 0, 0, 8, 2, ImageCodec.XY_BITMAP, 1, 1,
                VisualInfo.staticGray1(1), false);
        assertArrayEquals(data, back);
    }

    @Test
    void xyPixmapSendsTheMostSignificantPlaneFirst() {
        XPixmap target = pixmap(8, 1, 0xFF000000);
        byte[] data = {(byte) 0xAA, 0, 0, 0, (byte) 0xF0, 0, 0, 0};
        ImageCodec.putImage(target, new Rasterizer(target, gc()), 0, 0, 8, 1, ImageCodec.XYPIXMAP, 0, 2, data, 0, false);
        // Plane zero is the high bit of the pixel, plane one the low bit.
        assertEquals("02021313", values(target));
        byte[] back = ImageCodec.getImage(target, 0, 0, 8, 1, ImageCodec.XYPIXMAP, 0xFFFFFFFF, 2,
                VisualInfo.pseudoColor8(1), false);
        assertArrayEquals(data, back);
        // A plane the mask leaves out still occupies its place in the reply, as zeros.
        byte[] masked = ImageCodec.getImage(target, 0, 0, 8, 1, ImageCodec.XYPIXMAP, 0x2, 2,
                VisualInfo.pseudoColor8(1), false);
        assertEquals((byte) 0xAA, masked[0]);
        assertEquals((byte) 0x00, masked[4]);
    }

    private static String values(PixelStore store) {
        StringBuilder text = new StringBuilder();
        for (int x = 0; x < store.width(); x++) {
            text.append(Integer.toHexString(store.pixel(x, 0) & 0x0F));
        }
        return text.toString();
    }

    @Test
    void depthOneZPixmapUsesOneBitPerPixel() {
        XPixmap target = new XPixmap(1, null, null, 8, 1, 1, VisualInfo.staticGray1(1));
        target.fillRect(0, 0, 8, 1, 0xFF000000);
        ImageCodec.putImage(target, new Rasterizer(target, gc()), 0, 0, 8, 1, ImageCodec.ZPIXMAP, 0, 1,
                new byte[]{(byte) 0xAA, 0, 0, 0}, 0, false);
        assertEquals(0xFF000000, target.pixel(0, 0));
        assertEquals(0xFFFFFFFF, target.pixel(1, 0));
        assertEquals(8, target.width());
        assertNotEquals(0, lit(target));
    }

    @Test
    void getImageMasksPixelsWithThePlaneMask() {
        XPixmap target = pixmap(1, 1, 0xFF112233);
        byte[] out = ImageCodec.getImage(target, 0, 0, 1, 1, ImageCodec.ZPIXMAP, 0x00FF00, 24,
                VisualInfo.trueColor24(1), false);
        assertEquals(4, out.length);
        assertEquals(0x00, out[0] & 0xFF);
        assertEquals(0x22, out[1] & 0xFF);
        assertEquals(0x00, out[2] & 0xFF);
    }
}
