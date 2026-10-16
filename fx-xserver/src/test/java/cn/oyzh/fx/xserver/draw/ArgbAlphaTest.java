package cn.oyzh.fx.xserver.draw;

import cn.oyzh.fx.xserver.draw.ImageCodec;
import cn.oyzh.fx.xserver.draw.Rasterizer;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.resource.XGC;
import cn.oyzh.fx.xserver.resource.XPixmap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Alpha has to survive a trip through a depth 32 drawable.
 *
 * <p>Clients upload ARGB32 images - a cursor with its transparent surround, a RENDER source, a
 * translucent window - and expect the top byte to come back unchanged. Dropping it turns every
 * transparent pixel opaque black, which is what a cursor image looks like when it arrives as the
 * solid block a user sees under the pointer.
 */
public class ArgbAlphaTest {

    @Test
    void argb32VisualKeepsTheAlphaByte() {
        VisualInfo visual = VisualInfo.argb32(0x200);
        assertEquals(0x80112233, visual.toArgb(0x80112233));
        assertEquals(0x00FFFFFF, visual.toArgb(0x00FFFFFF) & 0x00FFFFFF);
        assertEquals(0, visual.toArgb(0) >>> 24, "a fully transparent pixel must stay transparent");
    }

    @Test
    void twentyFourBitVisualIsOpaque() {
        VisualInfo visual = VisualInfo.trueColor24(0x200);
        assertEquals(0xFF112233, visual.toArgb(0x112233));
    }

    @Test
    void putImageKeepsAlphaAtDepth32() {
        VisualInfo argb32 = VisualInfo.argb32(0x200);
        Screen screen = new Screen(0, 2, 1, List.of(argb32), 32, argb32.id());
        XPixmap pixmap = new XPixmap(1, null, screen, 2, 1, 32, argb32);
        XGC gc = new XGC(2, null, screen);
        // Two ARGB pixels in little endian byte order: half transparent red, then nothing at all.
        byte[] data = {(byte) 0x33, (byte) 0x22, (byte) 0x11, (byte) 0x80, 0, 0, 0, 0};

        ImageCodec.putImage(pixmap, new Rasterizer(pixmap, gc), 0, 0, 2, 1,
                ImageCodec.ZPIXMAP, 0, 32, data, 0, false);

        assertEquals(0x80112233, pixmap.pixel(0, 0));
        assertEquals(0x00000000, pixmap.pixel(1, 0));
    }
}
