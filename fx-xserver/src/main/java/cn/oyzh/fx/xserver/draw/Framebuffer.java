package cn.oyzh.fx.xserver.draw;

import java.awt.Rectangle;

/**
 * The pixel store behind one screen.
 *
 * <p>There is exactly one framebuffer per screen and every window draws straight into it through
 * its clip rectangle, which is how X keeps a single coherent image and how {@code Expose} events
 * stay meaningful: obscured content is simply not preserved.
 *
 * <p>Changes are tracked as a bounding box so the display backend can blit only what moved.
 */
public final class Framebuffer implements PixelStore {

    private int width;
    private int height;
    /**
     * The pixels, which change identity when the screen is resized - so a caller that keeps the
     * array between frames has to ask again after one (see {@link #resize}). Everything else about
     * this object, its identity included, survives a resize, which is what the backends and the
     * picture store depend on.
     *
     * <p><b>Null until something draws</b>, which is what keeps a connection that never draws from
     * costing a screen's worth of memory: in the default mode every connection gets a screen of its
     * own, and the ones that only ask questions - a probe, {@code xdpyinfo}, anything that connects
     * to look - used to allocate 4 MB each at the handshake (E4). {@link #pixels()} allocates on
     * demand, so a caller never sees null; {@link #allocated()} is what a test asks to tell the two
     * apart, and what a caller asks before deciding it holds the only reference.
     */
    private int[] pixels;

    /** What a late allocation is filled with: the screen's own background. */
    private final int fillArgb;

    private boolean dirty;
    private int dirtyX0;
    private int dirtyY0;
    private int dirtyX1;
    private int dirtyY1;

    /**
     * Whether the alpha channel is part of a pixel here.
     *
     * <p>A <em>screen</em> has no alpha and must not: X composites everything onto an opaque root,
     * and the raster operations can produce any 32 bit value - {@code GXclear} yields zero outright
     * and {@code GXinvert} turns white into transparent black - so a backend that honours alpha
     * would draw those as holes. A window's <em>own</em> store is the other case: a window whose
     * visual has an alpha channel is asking for its pixels to be kept with it, so that what it
     * draws can be composited over what is under it (B5).
     */
    private final boolean alpha;

    /** A screen's pixels: opaque, because a screen composites everything onto an opaque root. */
    public Framebuffer(int width, int height, int fillArgb) {
        this(width, height, fillArgb, false);
    }

    /**
     * @param alpha true for a window's own store, which keeps the alpha a client drew with, and
     *     false for a screen, which has none
     */
    public Framebuffer(int width, int height, int fillArgb, boolean alpha) {
        this.width = width;
        this.height = height;
        this.fillArgb = fillArgb;
        this.alpha = alpha;
        // Nothing is allocated here: a screen that is never drawn into is a size and a colour, and
        // the whole of it counts as damaged so that whatever allocates it later is presented.
        markDirty(0, 0, width, height);
    }

    /** Whether this store keeps the alpha channel, which only a window's own store does. */
    public boolean keepsAlpha() {
        return alpha;
    }

    /** Whether the pixels have been allocated, which is what the lazy allocation turns on. */
    public boolean allocated() {
        return pixels != null;
    }

    /** The pixels, allocated on the first ask and filled with the screen's background if new. */
    private int[] allocate() {
        if (pixels == null) {
            pixels = new int[width * height];
            java.util.Arrays.fill(pixels, opaque(fillArgb));
            markDirty(0, 0, width, height);
        }
        return pixels;
    }

    /**
     * Reallocates the pixels at a new size, keeping what was already drawn.
     *
     * <p>For the screen that a client asked to be bigger - the one case where the size of a screen
     * changes after clients have connected. The contents are copied into the new arrays rather than
     * the array being swapped for a new one everywhere: this object keeps its identity, which the
     * display backends and every picture over it hold on to, so the pixels are what moves.
     *
     * <p>Everything that caches {@link #pixels()} past the end of a frame has to take it again -
     * which is why the backends are told through {@code DisplayBackend.screenResized} rather than
     * being expected to notice.
     *
     * @param fillArgb what the area the old screen did not cover is painted with, which is the
     *     screen's own background: a client that had a window there is told about the change and
     *     draws into it, and until then it is the desktop
     */
    public void resize(int width, int height, int fillArgb) {
        if (width == this.width && height == this.height) {
            return;
        }
        int[] grown = new int[width * height];
        java.util.Arrays.fill(grown, opaque(fillArgb));
        int keepWidth = Math.min(width, this.width);
        int keepHeight = Math.min(height, this.height);
        if (pixels != null) {
            for (int row = 0; row < keepHeight; row++) {
                System.arraycopy(pixels, row * this.width, grown, row * width, keepWidth);
            }
        }
        this.pixels = grown;
        this.width = width;
        this.height = height;
        markAllDirty();
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int[] pixels() {
        return allocate();
    }

    @Override
    public int stride() {
        return width;
    }

    @Override
    public int pixel(int x, int y) {
        if (!contains(x, y)) {
            return 0;
        }
        // A read does not allocate: the answer is the background, because nothing has drawn here.
        return pixels == null ? opaque(fillArgb) : pixels[y * width + x];
    }

    @Override
    public void setPixel(int x, int y, int argb) {
        if (!contains(x, y)) {
            return;
        }
        allocate()[y * width + x] = opaque(argb);
        markDirty(x, y, x + 1, y + 1);
    }

    /**
     * Forces a pixel opaque.
     *
     * <p>A screen has no alpha: X composites everything onto an opaque root, and the raster
     * operations can produce any 32 bit value - {@code GXclear} yields zero outright, and
     * {@code GXinvert} turns white into transparent black. A display backend that honours alpha
     * (JavaFX uploads premultiplied) then draws those pixels as holes, so a client that paints
     * perfectly correct black on white sees nothing. The screen stores colour only.
     */
    private int opaque(int argb) {
        return alpha ? argb : 0xFF000000 | (argb & 0x00FFFFFF);
    }

    @Override
    public void fillRect(int x, int y, int w, int h, int argb) {
        int x0 = Math.max(0, x);
        int y0 = Math.max(0, y);
        int x1 = Math.min(width, x + w);
        int y1 = Math.min(height, y + h);
        if (x0 >= x1 || y0 >= y1) {
            return;
        }
        int colour = opaque(argb);
        int[] store = allocate();
        for (int row = y0; row < y1; row++) {
            java.util.Arrays.fill(store, row * width + x0, row * width + x1, colour);
        }
        markDirty(x0, y0, x1, y1);
    }

    @Override
    public void copyFrom(PixelStore src, int srcX, int srcY, int dstX, int dstY, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        // Work out the visible span once, then copy row by row with overlap handling.
        int dx0 = Math.max(0, dstX);
        int dy0 = Math.max(0, dstY);
        int dx1 = Math.min(width, dstX + w);
        int dy1 = Math.min(height, dstY + h);
        if (dx0 >= dx1 || dy0 >= dy1) {
            return;
        }
        boolean sameSurface = src == this;
        // A copy onto the same surface has to start at the far end of the overlap or it eats its own
        // source. Which end is "far" is per axis, and both axes have to be asked: this used to look
        // at the rows alone, so a purely *horizontal* self-copy (dstX > srcX, same rows) smeared -
        // every pixel written over the one the next read wanted, and the result a run of the first
        // pixel. Found by A12's bit gravity, which is the first caller to copy pixels sideways on the
        // same surface (backing-store saves copy between surfaces or downwards).
        boolean backwardsY = sameSurface && dstY > srcY;
        // **The common copies are one row at a time, and that is what makes a forwarded client
        // usable.** The per-pixel loop below is the general answer, but every drawing request that
        // lands in a window ends by compositing the window's own pixels onto the screen, and a
        // single-threaded pixel-at-a-time copy of a 1300x700 window costs more than everything the
        // request drew - it was the top of the server's profile while EasyShell's SSH tab was open.
        //
        // A row-wise {@code System.arraycopy} is the same pixels whenever the value needs no
        // conversion on the way in: this destination is a window store (it keeps alpha) or the
        // source is a non-alpha store (so every value it holds already has an opaque top byte, and
        // {@link #opaque} would leave it alone). Anything else - an alpha source onto a screen, a
        // source that has never been allocated - keeps the pixel loop, which is where the
        // conversion and the background read live.
        boolean verbatim = alpha
                || (src instanceof Framebuffer source && !source.keepsAlpha() && source.allocated());
        if (verbatim) {
            int[] dst = allocate();
            int[] from = src.pixels();
            int srcStride = src.stride();
            int dstStride = stride();
            int startRow = backwardsY ? dy1 - 1 : dy0;
            int endRow = backwardsY ? dy0 - 1 : dy1;
            int rowStep = backwardsY ? -1 : 1;
            for (int dy = startRow; dy != endRow; dy += rowStep) {
                int sy = srcY + (dy - dstY);
                if (sy < 0 || sy >= src.height()) {
                    continue;
                }
                // The horizontal span that is inside both the destination and the source.
                int dxStart = Math.max(dx0, dstX - srcX);
                int dxEnd = Math.min(dx1, dstX - srcX + src.width());
                if (dxStart >= dxEnd) {
                    continue;
                }
                System.arraycopy(from, sy * srcStride + (srcX + dxStart - dstX), dst,
                        dy * dstStride + dxStart, dxEnd - dxStart);
            }
            markDirty(dx0, dy0, dx1, dy1);
            return;
        }
        boolean backwardsX = sameSurface && dstX > srcX;
        int startRow = backwardsY ? dy1 - 1 : dy0;
        int endRow = backwardsY ? dy0 - 1 : dy1;
        int rowStep = backwardsY ? -1 : 1;
        for (int dy = startRow; dy != endRow; dy += rowStep) {
            int sy = srcY + (dy - dstY);
            if (sy < 0 || sy >= src.height()) {
                continue;
            }
            int startColumn = backwardsX ? dx1 - 1 : dx0;
            int endColumn = backwardsX ? dx0 - 1 : dx1;
            int columnStep = backwardsX ? -1 : 1;
            for (int dx = startColumn; dx != endColumn; dx += columnStep) {
                int sx = srcX + (dx - dstX);
                if (sx < 0 || sx >= src.width()) {
                    continue;
                }
                setPixel(dx, dy, src.pixel(sx, sy));
            }
        }
        markDirty(dx0, dy0, dx1, dy1);
    }

    /**
     * Called on whichever thread drew, the first time damage is marked after the last frame was
     * taken, so that a display can draw the frame at once instead of at the end of its tick. Null
     * for a store no display watches - a window's own store, and any framebuffer a test builds.
     *
     * <p>One call per batch of damage rather than one per mark: the damage between two frames is
     * already accumulated here, and what a display waits to hear about is the *arrival* of the
     * first of it.
     */
    private volatile Runnable damageListener;

    /** Sets the listener above; see it for what it is for. */
    public void setDamageListener(Runnable listener) {
        damageListener = listener;
    }

    /** Marks a rectangle as needing to reach the screen. */
    public void markDirty(int x0, int y0, int x1, int y1) {
        if (!dirty) {
            dirty = true;
            Runnable listener = damageListener;
            if (listener != null) {
                listener.run();
            }
            dirtyX0 = x0;
            dirtyY0 = y0;
            dirtyX1 = x1;
            dirtyY1 = y1;
            return;
        }
        dirtyX0 = Math.min(dirtyX0, x0);
        dirtyY0 = Math.min(dirtyY0, y0);
        dirtyX1 = Math.max(dirtyX1, x1);
        dirtyY1 = Math.max(dirtyY1, y1);
    }

    public void markDirty(Rectangle rect) {
        markDirty(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
    }

    public void markAllDirty() {
        markDirty(0, 0, width, height);
    }

    /**
     * Returns the accumulated damage and resets it, or {@code null} when nothing changed since the
     * last call.
     */
    public Rectangle takeDamage() {
        if (!dirty) {
            return null;
        }
        dirty = false;
        int x0 = Math.max(0, dirtyX0);
        int y0 = Math.max(0, dirtyY0);
        int x1 = Math.min(width, dirtyX1);
        int y1 = Math.min(height, dirtyY1);
        if (x0 >= x1 || y0 >= y1) {
            return null;
        }
        return new Rectangle(x0, y0, x1 - x0, y1 - y0);
    }
}
