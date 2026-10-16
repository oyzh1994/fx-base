package cn.oyzh.fx.xserver.draw;

import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.draw.PixelStore;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code Framebuffer.copyFrom} copies a whole row at a time when it can - the composite that puts a
 * window's pixels on the screen is the server's hottest path with a forwarded client on it - so this
 * holds that fast path to the pixel-by-pixel answer it replaced: the same pixels across two surfaces,
 * a same-surface copy that overlaps in both axes (where the row order matters), and a request that
 * runs off both the destination and the source.
 */
class FramebufferCopyTest {

    private static final int W = 40;
    private static final int H = 30;

    /** The answer as the old loop defined it: clip to the destination, walk away from the overlap. */
    private static void reference(Framebuffer dst, PixelStore src, int srcX, int srcY, int dstX,
                                  int dstY, int w, int h) {
        int dx0 = Math.max(0, dstX);
        int dy0 = Math.max(0, dstY);
        int dx1 = Math.min(dst.width(), dstX + w);
        int dy1 = Math.min(dst.height(), dstY + h);
        if (dx0 >= dx1 || dy0 >= dy1) {
            return;
        }
        boolean same = src == dst;
        boolean backX = same && dstX > srcX;
        boolean backY = same && dstY > srcY;
        int rowStart = backY ? dy1 - 1 : dy0;
        int rowEnd = backY ? dy0 - 1 : dy1;
        int rowStep = backY ? -1 : 1;
        for (int dy = rowStart; dy != rowEnd; dy += rowStep) {
            int sy = srcY + (dy - dstY);
            if (sy < 0 || sy >= src.height()) {
                continue;
            }
            int colStart = backX ? dx1 - 1 : dx0;
            int colEnd = backX ? dx0 - 1 : dx1;
            int colStep = backX ? -1 : 1;
            for (int dx = colStart; dx != colEnd; dx += colStep) {
                int sx = srcX + (dx - dstX);
                if (sx < 0 || sx >= src.width()) {
                    continue;
                }
                dst.setPixel(dx, dy, src.pixel(sx, sy));
            }
        }
    }

    private static Framebuffer filled(int seed) {
        Framebuffer fb = new Framebuffer(W, H, 0xFF000000);
        int[] p = fb.pixels();
        for (int i = 0; i < p.length; i++) {
            p[i] = 0xFF000000 | (int) ((i * 2654435761L + seed * 40503L) & 0xFFFFFF);
        }
        return fb;
    }

    private static void samePixels(Framebuffer a, Framebuffer b, String what) {
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                assertEquals(b.pixel(x, y), a.pixel(x, y), what + " at " + x + "," + y);
            }
        }
    }

    @Test
    void betweenTwoSurfaces() {
        int[][] cases = {{0, 0, 0, 0, W, H}, {5, 4, 9, 7, 20, 15}, {-6, -3, 2, 1, 18, 12},
                {30, 20, 5, 8, 25, 25}, {0, 0, 39, 29, 5, 5}};
        for (int[] c : cases) {
            Framebuffer src = filled(1);
            Framebuffer fast = filled(2);
            Framebuffer ref = filled(2);
            reference(ref, src, c[0], c[1], c[2], c[3], c[4], c[5]);
            fast.copyFrom(src, c[0], c[1], c[2], c[3], c[4], c[5]);
            samePixels(fast, ref, "copyFrom " + java.util.Arrays.toString(c));
        }
    }

    @Test
    void onItselfInBothDirections() {
        int[][] cases = {{20, 15, 4, 3, 12, 10}, {4, 3, 20, 15, 12, 10}, {10, 10, 10, 2, 15, 9},
                {10, 2, 10, 10, 15, 9}, {5, 5, 6, 6, 20, 20}};
        for (int[] c : cases) {
            Framebuffer fast = filled(3);
            Framebuffer ref = filled(3);
            reference(ref, ref, c[0], c[1], c[2], c[3], c[4], c[5]);
            fast.copyFrom(fast, c[0], c[1], c[2], c[3], c[4], c[5]);
            samePixels(fast, ref, "self copy " + java.util.Arrays.toString(c));
        }
    }

    @Test
    void marksTheRegionItWrote() {
        Framebuffer src = filled(4);
        Framebuffer dst = filled(5);
        dst.pixels();
        dst.takeDamage();
        dst.copyFrom(src, 0, 0, 12, 9, 20, 15);
        Rectangle damage = dst.takeDamage();
        assertEquals(new Rectangle(12, 9, 20, 15), damage);
    }
}
