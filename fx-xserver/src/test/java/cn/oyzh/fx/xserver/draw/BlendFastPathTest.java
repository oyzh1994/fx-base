package cn.oyzh.fx.xserver.draw;

import cn.oyzh.fx.xserver.draw.Compositor;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The blend's fast paths answer exactly what the general path answers - for every input, not on
 * average.
 *
 * <p>Backlog E3: the opaque-source path and the masked path are pure integer arithmetic where the
 * general path is double arithmetic, and the item's own warning is that "a fast path that changes
 * rounding must not be added quietly". The way not to add it quietly is this: {@link
 * Compositor#blend} - the one the pixel loop calls, fast paths included - is compared against
 * {@link Compositor#blendGeneral} over the whole range that the fast paths claim to cover. If a
 * future change makes them disagree by one unit on one input, this fails rather than one pixel of
 * somebody's rendering changing.
 *
 * <p>The space is walked rather than sampled for the mask, because the mask is the fast path's whole
 * subject: all 256 alphas against both ends of the source and the destination (0 and 255), plus the
 * values just either side of a rounding boundary (1, 2, 127, 128, 253, 254), plus a few thousand
 * random triples. The two operators are the ones the paths are about: {@code Src} and {@code Over}.
 */
class BlendFastPathTest {

    /** Channel values that sit at the ends and at the rounding boundaries. */
    private static final int[] INTERESTING = {0, 1, 2, 63, 127, 128, 129, 253, 254, 255};

    private static final int SRC = 1;
    private static final int OVER = 3;

    @Test
    void aMaskedOpaqueSourceBlendsTheSameWayInIntegers() {
        for (int op : new int[]{SRC, OVER}) {
            for (int maskAlpha = 0; maskAlpha <= 255; maskAlpha++) {
                for (int srcChannel : INTERESTING) {
                    for (int dstChannel : INTERESTING) {
                        int src = 0xFF000000 | (srcChannel << 16) | (srcChannel << 8) | srcChannel;
                        int dst = 0xFF000000 | (dstChannel << 16) | (dstChannel << 8) | dstChannel;
                        int mask = maskAlpha << 24;         // an A8 mask: the alpha is the whole of it
                        assertEquals(Compositor.blendGeneral(op, src, dst, mask, maskAlpha, false),
                                Compositor.blend(op, src, dst, mask, maskAlpha, false),
                                "op " + op + " mask " + maskAlpha + " src " + srcChannel
                                        + " dst " + dstChannel);
                    }
                }
            }
        }
    }

    @Test
    void aMaskedOpaqueSourceOverATranslucentDestinationIsLeftToTheGeneralPath() {
        // The fast path's Over case needs an opaque destination, and it says so rather than assuming
        // it: a translucent destination is not one of the pixels this server composites text onto,
        // and the numbers there still come from the general path.
        int src = 0xFF804020;
        int dst = 0x40804020;
        for (int maskAlpha : new int[]{0, 1, 128, 254, 255}) {
            int mask = maskAlpha << 24;
            assertEquals(Compositor.blendGeneral(OVER, src, dst, mask, maskAlpha, false),
                    Compositor.blend(OVER, src, dst, mask, maskAlpha, false), "mask " + maskAlpha);
        }
    }

    @Test
    void aTranslucentSourceIsLeftToTheGeneralPath() {
        // An A8 mask and a *translucent* source is a different case, and the general path keeps it.
        int dst = 0xFF102030;
        for (int maskAlpha : new int[]{0, 17, 128, 200, 255}) {
            for (int srcAlpha : new int[]{0, 1, 128, 254, 255}) {
                int src = (srcAlpha << 24) | 0x00A0B0C0;
                int mask = maskAlpha << 24;
                for (int op : new int[]{SRC, OVER}) {
                    assertEquals(Compositor.blendGeneral(op, src, dst, mask, maskAlpha, false),
                            Compositor.blend(op, src, dst, mask, maskAlpha, false),
                            "op " + op + " source alpha " + srcAlpha + " mask " + maskAlpha);
                }
            }
        }
    }

    @Test
    void randomPixelsAgree() {
        Random random = new Random(20260925L);
        for (int i = 0; i < 20000; i++) {
            int src = random.nextInt();
            int dst = random.nextInt();
            int mask = random.nextInt();
            int maskAlpha = mask >>> 24;
            boolean componentAlpha = random.nextBoolean();
            for (int op : new int[]{SRC, OVER}) {
                assertEquals(Compositor.blendGeneral(op, src, dst, mask, maskAlpha, componentAlpha),
                        Compositor.blend(op, src, dst, mask, maskAlpha, componentAlpha),
                        "op " + op + " src " + Integer.toHexString(src) + " dst "
                                + Integer.toHexString(dst) + " mask " + Integer.toHexString(mask)
                                + " componentAlpha " + componentAlpha);
            }
        }
    }
}
