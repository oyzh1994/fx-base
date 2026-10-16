package cn.oyzh.fx.xserver.draw;

import java.awt.Rectangle;

/**
 * A rectangular array of pixels addressed as 0xAARRGGBB, shared by the screen framebuffer and by
 * pixmaps and windows. Internally everything is 32 bit ARGB; the depth of the owning drawable
 * decides how those values are packed when they cross the protocol boundary.
 */
public interface PixelStore {

    int width();

    int height();

    /** 0xAARRGGBB at the given coordinates, or 0 outside the surface. */
    int pixel(int x, int y);

    void setPixel(int x, int y, int argb);

    /**
     * Direct access to the backing array. Row {@code y} starts at {@code y * stride()}.
     * Callers must respect {@link #width()} and {@link #height()}.
     */
    int[] pixels();

    /** Pixels per row in {@link #pixels()}; may exceed {@link #width()} for screen rows. */
    default int stride() {
        return width();
    }

    default boolean contains(int x, int y) {
        return x >= 0 && y >= 0 && x < width() && y < height();
    }

    /**
     * Fills a rectangle with an ARGB value, clipped to the surface.
     */
    default void fillRect(int x, int y, int w, int h, int argb) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int x0 = Math.max(0, x);
        int y0 = Math.max(0, y);
        int x1 = Math.min(width(), x + w);
        int y1 = Math.min(height(), y + h);
        if (x0 >= x1 || y0 >= y1) {
            return;
        }
        int[] data = pixels();
        int stride = stride();
        for (int row = y0; row < y1; row++) {
            int base = row * stride;
            java.util.Arrays.fill(data, base + x0, base + x1, argb);
        }
    }

    /** Copies a rectangle from another surface, with overlap-safe handling. */
    default void copyFrom(PixelStore src, int srcX, int srcY, int dstX, int dstY, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        int step = (srcY < dstY && src == this) ? h - 1 : 0;
        int end = (srcY < dstY && src == this) ? -1 : h;
        int direction = (srcY < dstY && src == this) ? -1 : 1;
        for (int row = step; row != end; row += direction) {
            for (int col = 0; col < w; col++) {
                int sx = srcX + col;
                int sy = srcY + row;
                int dx = dstX + col;
                int dy = dstY + row;
                if (src.contains(sx, sy) && contains(dx, dy)) {
                    setPixel(dx, dy, src.pixel(sx, sy));
                }
            }
        }
    }

    /** Bounding box of the whole surface. */
    default Rectangle bounds() {
        return new Rectangle(0, 0, width(), height());
    }
}
