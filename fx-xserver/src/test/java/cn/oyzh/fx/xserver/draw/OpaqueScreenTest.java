package cn.oyzh.fx.xserver.draw;

import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.draw.Rasterizer;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.resource.XGC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The screen only ever holds opaque pixels.
 *
 * <p>These exist because a client that drew with a graphics context's default foreground produced
 * {@code 0x00000000} - correct as a colour, but transparent as a pixel. {@code GetImage} read it
 * back as black and every test passed, while the JavaFX window, which uploads premultiplied and
 * therefore treats alpha zero as a hole, showed nothing at all. A screen has no alpha, so the
 * framebuffer must not be able to store one.
 */
class OpaqueScreenTest {

    private static final VisualInfo TRUE_COLOR = VisualInfo.trueColor24(0x200);

    private static XGC gc() {
        XGC gc = new XGC(1, null, null);
        gc.setForegroundArgb(0xFF000000);
        gc.setBackgroundArgb(0xFFFFFFFF);
        return gc;
    }

    /** The default foreground is black, and black on a screen is opaque. */
    @Test
    void defaultForegroundIsOpaqueBlack() {
        XGC gc = new XGC(1, null, null);
        assertEquals(0xFF000000, gc.foreground(),
                "X11's default foreground is black, which must be opaque on a screen");
        assertEquals(0xFFFFFFFF, gc.background(), "X11's default background is white");
    }

    @Test
    void drawingWithTheDefaultForegroundIsOpaque() {
        Framebuffer framebuffer = new Framebuffer(32, 32, 0xFFFFFFFF);
        XGC gc = new XGC(1, null, null);
        Rasterizer rasterizer = new Rasterizer(framebuffer, gc);
        rasterizer.fillRect(4, 4, 8, 8);
        rasterizer.drawLine(0, 0, 31, 31);

        assertEquals(0xFF000000, framebuffer.pixel(6, 6));
        assertEquals(0xFF000000, framebuffer.pixel(16, 16));
        assertOpaque(framebuffer);
    }

    /**
     * Every raster op can produce a value with a zero high byte - {@code GXclear} yields zero,
     * {@code GXinvert} turns white into transparent black - so the framebuffer has to refuse them.
     */
    @Test
    void rasterFunctionsCannotStoreTransparency() {
        for (int function = XGC.GX_CLEAR; function <= XGC.GX_SET; function++) {
            Framebuffer framebuffer = new Framebuffer(16, 16, 0xFFFFFFFF);
            XGC gc = gc();
            gc.setFunction(function);
            Rasterizer rasterizer = new Rasterizer(framebuffer, gc);
            rasterizer.fillRect(0, 0, 16, 16);
            assertOpaque(framebuffer);
        }
    }

    @Test
    void invertedAndClearedPixelsStayOpaque() {
        Framebuffer framebuffer = new Framebuffer(8, 8, 0xFFFFFFFF);
        XGC gc = gc();
        gc.setFunction(XGC.GX_INVERT);
        new Rasterizer(framebuffer, gc).fillRect(0, 0, 8, 8);
        assertEquals(0xFF000000, framebuffer.pixel(4, 4), "inverted white is opaque black");

        gc.setFunction(XGC.GX_CLEAR);
        new Rasterizer(framebuffer, gc).fillRect(0, 0, 8, 8);
        assertEquals(0xFF000000, framebuffer.pixel(4, 4), "cleared pixels are opaque black");
        assertOpaque(framebuffer);
    }

    @Test
    void everyPixelWrittenByDrawingIsOpaque() {
        Framebuffer framebuffer = new Framebuffer(64, 64, 0xFF102030);
        XGC gc = gc();
        Rasterizer rasterizer = new Rasterizer(framebuffer, gc);
        rasterizer.fillRect(0, 0, 64, 64);
        rasterizer.drawRect(2, 2, 60, 60);
        rasterizer.fillArc(10, 10, 40, 40, 0, 360 * 64);
        rasterizer.fillPolygon(new int[] {5, 5, 30, 45, 45, 30}, true);
        assertOpaque(framebuffer);
    }

    private static void assertOpaque(Framebuffer framebuffer) {
        int[] pixels = framebuffer.pixels();
        for (int i = 0; i < pixels.length; i++) {
            assertTrue((pixels[i] >>> 24) == 0xFF,
                    "pixel " + i + " is 0x" + Integer.toHexString(pixels[i])
                            + ", which a display backend would draw as a hole");
        }
    }
}
