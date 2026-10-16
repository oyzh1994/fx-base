package cn.oyzh.fx.xserver.draw;

import cn.oyzh.fx.xserver.resource.BitmapFont;
import cn.oyzh.fx.xserver.resource.XFont;
import cn.oyzh.fx.xserver.resource.XGC;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Draws the X11 core protocol primitives into a {@link PixelStore}.
 *
 * <p>Core X is not a compositing model: every operation is a bitwise raster op applied to the
 * destination pixel, the source pixel and the plane mask. For every affected pixel the result is
 *
 * <pre>(old op src) &amp; planeMask | (old &amp; ~planeMask)</pre>
 *
 * where {@code op} comes from the GC's function ({@code GX*}): the source is the GC foreground, or
 * the tile/stipple pixel when the fill style says so, never a blend of colours. Anything that
 * blends would be wrong for the clients that still use XOR for rubber bands and cursor drawing.
 *
 * <p>A rasterizer is created for one request and is not thread safe: it snapshots the parts of the
 * GC it needs, resolves the clip once ({@link PixelStore} bounds, the optional caller clip, the GC
 * clip rectangle list or clip mask pixmap) and then draws.
 *
 * <h2>Approximations</h2>
 *
 * Xorg rasterizes wide lines, joins and arcs analytically. This implementation keeps the pixel
 * coverage rules but approximates the geometry:
 * <ul>
 *   <li>Lines are Bresenham center lines. {@code lineWidth} zero traces a single pixel per step;
 *       a wider line paints the pixels its rectangle covers at every step, which is exact for
 *       horizontal and vertical lines and a close approximation of the rotated rectangle for the
 *       rest. Ends follow the cap style and corners the join style, except that a miter is drawn
 *       as a bevel.</li>
 *   <li>A {@code JoinRound} line (or one with round caps) paints a disc instead, so its corners
 *       and ends are round.</li>
 *   <li>Dashes advance one unit per center-line pixel rather than per unit of true arc length, so
 *       diagonal dashes come out slightly longer than Xorg's, and the count carried across a
 *       polyline vertex differs by one, which can shift the rest of the pattern by a pixel. Double
 *       dash fills the gaps with the background and puts a dot in the middle of each one.</li>
 *   <li>Arcs are sampled at roughly one pixel of arc and stroked or filled as a polygon, with the
 *       ellipse centred in the bounding box and spanning it exactly, so an arc outline reaches the
 *       far side of the box while its fill stays one pixel inside, as it does on an X server. The
 *       fill is the region the ellipse really covers; Xorg's Bresenham ellipse is up to a pixel
 *       thinner at the flat top and bottom of an arc.</li>
 *   <li>Text is rasterized from the AWT font that backs the {@link XFont} rather than from X
 *       bitmap fonts, so glyph shapes differ from an Xorg server's built-in fonts.</li>
 * </ul>
 */
public final class Rasterizer {

    private final PixelStore store;
    private final XGC gc;

    // Snapshot of the GC state that affects a single request.
    private final int function;
    private final int planeMask;
    private final int foreground;
    private final int background;
    private final int fillStyle;
    private final int lineWidth;
    private final int lineStyle;
    private final int joinStyle;
    private final int capStyle;
    private final int arcMode;
    private final int fillRule;

    /** GX_COPY with a full plane mask: the destination value is irrelevant, {@code src} wins. */
    private final boolean directCopy;

    private final PixelStore tile;
    private final int tileWidth;
    private final int tileHeight;
    private final int tileOriginX;
    private final int tileOriginY;

    private final PixelStore stipple;
    private final int stippleWidth;
    private final int stippleHeight;
    private final int stippleOriginX;
    private final int stippleOriginY;

    // Clip: a bounding box, plus either a rectangle list or a mask pixmap (never both).
    private final int clipX0;
    private final int clipY0;
    private final int clipX1;
    private final int clipY1;
    /**
     * The writable region as x0,y0,x1,y1 groups, when the drawable is partly covered by other
     * windows: the visible parts of a window, which are the only places its pixels really show.
     * Null when the clip box is the whole story.
     */
    private final int[] regionRects;
    private final int[] clipRects;
    private final PixelStore clipMask;
    private final int clipOriginX;
    private final int clipOriginY;
    /** No mask and no rectangle list: the clip is the bounding box alone. */
    private final boolean simpleClip;

    /**
     * The region this rasterizer has actually written, in the drawable's own coordinates, or an
     * empty box when it wrote nothing.
     *
     * <p><b>Why the rasterizer keeps this.</b> The display tick presents what the framebuffer says
     * changed, and every drawing request used to mark the *whole screen* changed - which is correct
     * and useless: a keystroke in a terminal repainted 1300x800, and a VNC update (D2) that could
     * have been a few hundred bytes was four megabytes. What a request can write is exactly what it
     * is clipped to, but which of those pixels it *did* write is known here and nowhere else, so it
     * is recorded here - two comparisons per written pixel - and the handler marks that rectangle.
     *
     * <p>It is a superset of what changed (a fill that stopped early still widens the box), which is
     * the safe direction: a damage rectangle that is too big costs a repaint, one that is too small
     * leaves a stale pixel on the screen.
     */
    private int drawnX0 = Integer.MAX_VALUE;
    private int drawnY0 = Integer.MAX_VALUE;
    private int drawnX1 = Integer.MIN_VALUE;
    private int drawnY1 = Integer.MIN_VALUE;

    /**
     * True when a filled rectangle can be handed to {@link PixelStore#fillRect} as a block: the
     * function ignores the destination and the fill style is a solid colour, so no per pixel work
     * is needed. That path also keeps damage tracking of a {@link Framebuffer} intact.
     */
    private final boolean solidCopy;

    /**
     * {@link #solidCopy} plus "the clip box is the whole story", which is what allows a fill to go
     * through as one block without consulting the clip at all.
     */
    private final boolean fastFill;

    /**
     * True when the effective clip is exactly the clip box, even if that box arrived as one
     * rectangle list. A toolkit's clip is usually a single rectangle that is not the whole
     * drawable, which makes {@link #simpleClip} false for a reason that has no effect on a copy
     * inside the box - so this is the condition the copy and image fast paths ask instead.
     */
    private final boolean boxIsClip;

    public Rasterizer(PixelStore store, XGC gc) {
        this(store, gc, null);
    }

    /**
     * @param clip extra clip rectangle, intersected with the drawable bounds and the GC clip; the
     *     server passes the window and ancestor clip here.
     */
    public Rasterizer(PixelStore store, XGC gc, Rectangle clip) {
        this(store, gc, clip, null);
    }

    /**
     * @param region the parts of a window that are actually visible, in screen coordinates, when
     *     windows are stacked on it. Drawing outside them would paint over the window on top, so
     *     every write is confined to this region; {@code clip} is its bounding box (or the window's
     *     clip when there is no region).
     */
    public Rasterizer(PixelStore store, XGC gc, Rectangle clip, List<Rectangle> region) {
        this.store = store;
        this.gc = gc;
        this.function = gc.function();
        this.planeMask = gc.planeMask();
        this.foreground = gc.foreground();
        this.background = gc.background();
        this.fillStyle = gc.fillStyle();
        this.lineWidth = gc.lineWidth();
        this.lineStyle = gc.lineStyle();
        this.joinStyle = gc.joinStyle();
        this.capStyle = gc.capStyle();
        this.arcMode = gc.arcMode();
        this.fillRule = gc.fillRule();
        this.directCopy = function == XGC.GX_COPY && planeMask == 0xFFFFFFFF;

        this.tileOriginX = gc.tileStippleOriginX();
        this.tileOriginY = gc.tileStippleOriginY();
        PixelStore t = gc.tile() instanceof PixelStore ps ? ps : null;
        this.tile = t;
        this.tileWidth = t == null ? 0 : Math.max(1, t.width());
        this.tileHeight = t == null ? 0 : Math.max(1, t.height());

        this.stippleOriginX = gc.stippleOriginX();
        this.stippleOriginY = gc.stippleOriginY();
        PixelStore s = gc.stipple() instanceof PixelStore ps ? ps : null;
        this.stipple = s;
        this.stippleWidth = s == null ? 0 : Math.max(1, s.width());
        this.stippleHeight = s == null ? 0 : Math.max(1, s.height());

        this.clipOriginX = gc.clipOriginX();
        this.clipOriginY = gc.clipOriginY();

        int x0 = 0;
        int y0 = 0;
        int x1 = store.width();
        int y1 = store.height();
        if (clip != null) {
            x0 = Math.max(x0, clip.x);
            y0 = Math.max(y0, clip.y);
            x1 = Math.min(x1, clip.x + clip.width);
            y1 = Math.min(y1, clip.y + clip.height);
        }

        int[] pieces = null;
        if (region != null) {
            pieces = new int[region.size() * 4];
            int n = 0;
            int regionX0 = x0;
            int regionY0 = y0;
            int regionX1 = x1;
            int regionY1 = y1;
            if (region.isEmpty()) {
                // Nothing of the window is visible: the clip box collapses and no pixel is writable.
                regionX0 = regionY0 = 0;
                regionX1 = regionY1 = 0;
            }
            for (Rectangle piece : region) {
                pieces[n++] = piece.x;
                pieces[n++] = piece.y;
                pieces[n++] = piece.x + piece.width;
                pieces[n++] = piece.y + piece.height;
                regionX0 = Math.min(regionX0, piece.x);
                regionY0 = Math.min(regionY0, piece.y);
                regionX1 = Math.max(regionX1, piece.x + piece.width);
                regionY1 = Math.max(regionY1, piece.y + piece.height);
            }
            x0 = Math.max(x0, regionX0);
            y0 = Math.max(y0, regionY0);
            x1 = Math.min(x1, regionX1);
            y1 = Math.min(y1, regionY1);
        }

        PixelStore mask = null;
        int[] rects = null;
        int rectX0 = x0;
        int rectY0 = y0;
        int rectX1 = x1;
        int rectY1 = y1;
        if (gc.isClipByRectangles()) {
            List<XGC.Rectangle2> list = gc.clipRectangles();
            // Rectangle coordinates are relative to the clip origin (SetClipRectangles).
            rects = new int[list.size() * 4];
            rectX0 = Integer.MAX_VALUE;
            rectY0 = Integer.MAX_VALUE;
            rectX1 = Integer.MIN_VALUE;
            rectY1 = Integer.MIN_VALUE;
            int n = 0;
            for (XGC.Rectangle2 r : list) {
                int rx = r.x() + clipOriginX;
                int ry = r.y() + clipOriginY;
                rects[n++] = rx;
                rects[n++] = ry;
                rects[n++] = rx + r.width();
                rects[n++] = ry + r.height();
                rectX0 = Math.min(rectX0, rx);
                rectY0 = Math.min(rectY0, ry);
                rectX1 = Math.max(rectX1, rx + r.width());
                rectY1 = Math.max(rectY1, ry + r.height());
            }
            if (list.isEmpty()) {
                // An empty clip list means nothing can be drawn.
                rectX0 = rectY0 = 0;
                rectX1 = rectY1 = 0;
            }
            x0 = Math.max(x0, rectX0);
            y0 = Math.max(y0, rectY0);
            x1 = Math.min(x1, rectX1);
            y1 = Math.min(y1, rectY1);
        } else if (gc.clipMask() instanceof PixelStore cm) {
            // A set bit in the mask means the pixel is inside the clip region.
            mask = cm;
            int mx0 = clipOriginX;
            int my0 = clipOriginY;
            x0 = Math.max(x0, mx0);
            y0 = Math.max(y0, my0);
            x1 = Math.min(x1, mx0 + cm.width());
            y1 = Math.min(y1, my0 + cm.height());
        }
        this.clipX0 = x0;
        this.clipY0 = y0;
        this.clipX1 = Math.max(x0, x1);
        this.clipY1 = Math.max(y0, y1);
        this.regionRects = pieces == null || coversBox(pieces, clipX0, clipY0, clipX1, clipY1)
                ? null : pieces;
        this.clipRects = rects;
        this.clipMask = mask;
        this.simpleClip = regionRects == null && rects == null && mask == null;
        this.solidCopy = directCopy && fillStyle == XGC.FILL_SOLID;
        this.fastFill = solidCopy && simpleClip;
        this.boxIsClip = regionRects == null && mask == null
                && (rects == null || coversBox(rects, clipX0, clipY0, clipX1, clipY1));
    }

    /** True when a single rectangle of the region spans the whole clip box. */
    private static boolean coversBox(int[] pieces, int x0, int y0, int x1, int y1) {
        return pieces.length == 4 && pieces[0] <= x0 && pieces[1] <= y0 && pieces[2] >= x1
                && pieces[3] >= y1;
    }

    // ---------------------------------------------------------------- raster ops

    /**
     * Applies a graphics function. The operand order is the one X11 defines in {@code X.h}: the
     * named functions read as "src &lt;op&gt; dst", so {@code GXandReverse} is <em>src AND NOT
     * dst</em>. Getting this backwards only shows up in the four asymmetric functions, which is
     * exactly the kind of bug that survives a casual test with GXcopy.
     *
     * @param old the pixel already in the drawable, that is "dst"
     * @param src the value being drawn, that is "src"
     * @return {@code src op dst}; the plane mask is applied by {@link #writePixel}, not here
     */
    public static int applyFunction(int old, int src, int function) {
        return switch (function) {
            case XGC.GX_CLEAR -> 0;
            case XGC.GX_AND -> old & src;
            case XGC.GX_AND_REVERSE -> src & ~old;
            case XGC.GX_COPY -> src;
            case XGC.GX_AND_INVERTED -> ~src & old;
            case XGC.GX_NOOP -> old;
            case XGC.GX_XOR -> old ^ src;
            case XGC.GX_OR -> old | src;
            case XGC.GX_NOR -> ~(old | src);
            case XGC.GX_EQUIV -> ~(old ^ src);
            case XGC.GX_INVERT -> ~old;
            case XGC.GX_OR_REVERSE -> src | ~old;
            case XGC.GX_COPY_INVERTED -> ~src;
            case XGC.GX_OR_INVERTED -> ~src | old;
            case XGC.GX_NAND -> ~(old & src);
            case XGC.GX_SET -> ~0;
            default -> src;
        };
    }

    /**
     * Applies the GC function and plane mask to a single pixel of source value {@code src} and
     * stores the result, unless the pixel is clipped out.
     *
     * <p>This is the only place that writes to the target, so every primitive shares the same
     * function, plane mask and clip semantics.
     */
    /** The GC's foreground colour, used for the set bits of a bitmap put into a drawable. */
    public int foreground() {
        return foreground;
    }

    /** The GC's background colour, used for the clear bits of a bitmap. */
    public int background() {
        return background;
    }

    /** The region written so far, or null when nothing has been. */
    public Rectangle drawnBounds() {
        return drawnX0 > drawnX1 ? null
                : new Rectangle(drawnX0, drawnY0, drawnX1 - drawnX0 + 1, drawnY1 - drawnY0 + 1);
    }

    private void markDrawn(int x0, int y0, int x1, int y1) {
        if (x0 < drawnX0) {
            drawnX0 = x0;
        }
        if (y0 < drawnY0) {
            drawnY0 = y0;
        }
        if (x1 > drawnX1) {
            drawnX1 = x1;
        }
        if (y1 > drawnY1) {
            drawnY1 = y1;
        }
    }

    public void writePixel(int x, int y, int src) {
        if (!clipContains(x, y)) {
            return;
        }
        markDrawn(x, y, x, y);
        if (directCopy) {
            store.setPixel(x, y, src);
            return;
        }
        int old = store.pixel(x, y);
        int value = applyFunction(old, src, function);
        store.setPixel(x, y, (value & planeMask) | (old & ~planeMask));
    }

    /**
     * Writes a whole horizontal run of ARGB pixels in the drawable's storage form, one row at a
     * time.
     *
     * <p>This is the fast path a {@code PutImage} takes: a forwarded JavaFX or GTK client uploads
     * every frame this way and a per-pixel {@link #writePixel} - a clip test, the drawn-region
     * bookkeeping and a store write through an interface call for each of a window's pixels - was
     * the top of the server's profile with EasyShell's SSH tab open. When the GC is a plain copy,
     * the clip is just this box, and the destination is a framebuffer, a row can be written
     * straight into its array; anything else falls back to the pixel loop, which stays the
     * reference for what a run means.
     *
     * @param x    the drawable coordinate of the first pixel
     * @param y    the row
     * @param src  the run, as 0xAARRGGBB values, starting at {@code offset}
     * @param len  how many pixels
     */
    public void writeRun(int x, int y, int[] src, int offset, int len) {
        int x0 = Math.max(x, clipX0);
        int x1 = Math.min(x + len, clipX1);
        if (x1 <= x0 || y < clipY0 || y >= clipY1) {
            return;
        }
        if (directCopy && boxIsClip && store instanceof Framebuffer framebuffer) {
            int[] dst = framebuffer.pixels();
            int base = y * framebuffer.stride();
            boolean opaque = !framebuffer.keepsAlpha();
            int at = offset + (x0 - x);
            if (opaque) {
                for (int px = x0; px < x1; px++, at++) {
                    dst[base + px] = 0xFF000000 | (src[at] & 0x00FFFFFF);
                }
            } else {
                System.arraycopy(src, at, dst, base + x0, x1 - x0);
            }
            markDrawn(x0, y, x1 - 1, y);
            framebuffer.markDirty(x0, y, x1, y + 1);
            return;
        }
        for (int px = x0; px < x1; px++) {
            writePixel(px, y, src[offset + (px - x)]);
        }
    }

    /** True when the pixel survives the clip region: bounds, then rectangles, then mask. */
    public boolean clipContains(int x, int y) {
        if (x < clipX0 || x >= clipX1 || y < clipY0 || y >= clipY1) {
            return false;
        }
        if (regionRects != null) {
            boolean visible = false;
            for (int i = 0; i < regionRects.length; i += 4) {
                if (x >= regionRects[i] && x < regionRects[i + 2] && y >= regionRects[i + 1]
                        && y < regionRects[i + 3]) {
                    visible = true;
                    break;
                }
            }
            if (!visible) {
                // A window on top owns this pixel; writing it would paint over that window.
                return false;
            }
        }
        if (clipRects != null) {
            for (int i = 0; i < clipRects.length; i += 4) {
                if (x >= clipRects[i] && x < clipRects[i + 2] && y >= clipRects[i + 1]
                        && y < clipRects[i + 3]) {
                    return true;
                }
            }
            return false;
        }
        if (clipMask != null) {
            // The mask's own origin sits at the clip origin in drawable coordinates.
            return bitSet(clipMask.pixel(x - clipOriginX, y - clipOriginY));
        }
        return true;
    }

    /** The region actually drawable after clipping, as a rectangle. */
    public Rectangle clipBounds() {
        return new Rectangle(clipX0, clipY0, clipX1 - clipX0, clipY1 - clipY0);
    }

    // ---------------------------------------------------------------- rectangles

    /**
     * Fills the {@code w} by {@code h} rectangle whose top left pixel is {@code (x, y)}, using the
     * GC fill style and function.
     */
    public void fillRect(int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        long right = (long) x + w;
        long bottom = (long) y + h;
        if (regionRects != null && clipRects == null && clipMask == null) {
            // Partly covered by a window on top: fill the visible pieces of the rectangle one block
            // at a time, so a fill stays a block fill instead of a per pixel loop.
            for (int i = 0; i < regionRects.length; i += 4) {
                int px0 = Math.max(x, regionRects[i]);
                int py0 = Math.max(y, regionRects[i + 1]);
                long px1 = Math.min(right, regionRects[i + 2]);
                long py1 = Math.min(bottom, regionRects[i + 3]);
                if (px0 < px1 && py0 < py1) {
                    // The piece is already inside the region and there is no GC clip or mask here,
                    // so it can go through as a block.
                    fillRectClipped(px0, py0, px1, py1, solidCopy);
                }
            }
            return;
        }
        fillRectClipped(x, y, right, bottom, fastFill);
    }

    /** The clipping itself, once the caller has settled which pieces are writable. */
    private void fillRectClipped(int x, int y, long right, long bottom, boolean asBlock) {
        int x0 = Math.max(clipX0, x);
        int y0 = Math.max(clipY0, y);
        int x1 = (int) Math.min(clipX1, right);
        int y1 = (int) Math.min(clipY1, bottom);
        if (x0 >= x1 || y0 >= y1) {
            return;
        }
        if (asBlock) {
            markDrawn(x0, y0, x1 - 1, y1 - 1);
            store.fillRect(x0, y0, x1 - x0, y1 - y0, foreground);
            return;
        }
        for (int py = y0; py < y1; py++) {
            for (int px = x0; px < x1; px++) {
                fillPixel(px, py);
            }
        }
    }

    /** Fills the inclusive span {@code x0..x1} of row {@code y}. */
    private void fillSpan(int y, int x0, int x1) {
        if (x0 > x1 || y < clipY0 || y >= clipY1) {
            return;
        }
        if (regionRects != null && clipRects == null && clipMask == null) {
            for (int i = 0; i < regionRects.length; i += 4) {
                if (y < regionRects[i + 1] || y >= regionRects[i + 3]) {
                    continue;
                }
                int px0 = Math.max(x0, regionRects[i]);
                int px1 = Math.min(x1, regionRects[i + 2] - 1);
                if (px0 <= px1) {
                    fillSpanClipped(y, px0, px1, solidCopy);
                }
            }
            return;
        }
        fillSpanClipped(y, x0, x1, fastFill);
    }

    private void fillSpanClipped(int y, int x0, int x1, boolean asBlock) {
        int px0 = Math.max(clipX0, x0);
        int px1 = Math.min(clipX1 - 1, x1);
        if (px0 > px1) {
            return;
        }
        if (asBlock) {
            markDrawn(px0, y, px1, y);
            store.fillRect(px0, y, px1 - px0 + 1, 1, foreground);
            return;
        }
        for (int x = px0; x <= px1; x++) {
            fillPixel(x, y);
        }
    }

    /** Paints one pixel of a filled area with the source the GC fill style selects. */
    private void fillPixel(int x, int y) {
        switch (fillStyle) {
            case XGC.FILL_TILED -> writePixel(x, y, tile == null ? foreground : tilePixel(x, y));
            case XGC.FILL_STIPPLED -> {
                if (stippleSet(x, y)) {
                    writePixel(x, y, foreground);
                }
            }
            case XGC.FILL_OPAQUE_STIPPLED ->
                    writePixel(x, y, stippleSet(x, y) ? foreground : background);
            default -> writePixel(x, y, foreground);
        }
    }

    /**
     * True when a monochrome mask pixel stands for a set bit.
     *
     * <p>A depth one surface keeps two pixel values, and this server names them the way the rest of
     * it does, as ARGB: black is {@code 0xFF000000} (bit clear), white is {@code 0xFFFFFFFF} (bit
     * set). Decoded images use the same pair, so the colour bits decide.
     */
    private static boolean bitSet(int argb) {
        return (argb & 0x00FFFFFF) != 0;
    }

    /** Tile pixel, with the tile origin subtracted, or the foreground when no tile is set. */
    private int tilePixel(int x, int y) {
        int tx = Math.floorMod(x - tileOriginX, tileWidth);
        int ty = Math.floorMod(y - tileOriginY, tileHeight);
        return tile.pixel(tx, ty);
    }

    /** True when the stipple is set at the pixel, or when there is no stipple at all. */
    private boolean stippleSet(int x, int y) {
        if (stipple == null) {
            return true;
        }
        int sx = Math.floorMod(x - stippleOriginX, stippleWidth);
        int sy = Math.floorMod(y - stippleOriginY, stippleHeight);
        return bitSet(stipple.pixel(sx, sy));
    }

    // ---------------------------------------------------------------- points

    public void drawPoint(int x, int y) {
        // PolyPoint honours the fill style in the same way a one pixel rectangle does.
        fillPixel(x, y);
    }

    /** Draws a list of {@code x, y} pairs. */
    public void drawPoints(int[] points) {
        for (int i = 0; i + 1 < points.length; i += 2) {
            drawPoint(points[i], points[i + 1]);
        }
    }

    // ---------------------------------------------------------------- lines

    public void drawLine(int x1, int y1, int x2, int y2) {
        strokePolyline(new int[]{x1, y1, x2, y2}, 2, false);
    }

    /** Draws each {@code x1, y1, x2, y2} quadruple as an independent segment. */
    public void drawSegments(int[] points) {
        for (int i = 0; i + 3 < points.length; i += 4) {
            // Each segment is its own figure, so the dash pattern restarts for every one of them.
            strokeSegment(points[i], points[i + 1], points[i + 2], points[i + 3], newDasher(), true,
                    true);
        }
    }

    /**
     * Outline of the rectangle at {@code (x, y)} of {@code w} by {@code h} pixels.
     *
     * <p>{@code PolyRectangle} is not the outline of the filled rectangle: its corners are
     * {@code (x, y)} and {@code (x + w, y + h)}, so a 6 by 4 rectangle draws a 7 by 5 pixel ring
     * and a zero width draws a line one pixel longer than the height.
     */
    public void drawRect(int x, int y, int w, int h) {
        if (w < 0 || h < 0) {
            return;
        }
        strokePolyline(new int[]{x, y, x + w, y, x + w, y + h, x, y + h}, 4, true);
    }

    /** Draws the polyline through the points, without closing the loop. */
    public void drawPolyline(int[] points) {
        int count = points.length / 2;
        if (count < 2) {
            if (count == 1) {
                drawPoint(points[0], points[1]);
            }
            return;
        }
        strokePolyline(points, count, false);
    }

    /** Outline of the polygon, closing the loop back to the first point. */
    public void drawPolygon(int[] points) {
        int count = points.length / 2;
        if (count < 2) {
            if (count == 1) {
                drawPoint(points[0], points[1]);
            }
            return;
        }
        strokePolyline(points, count, true);
    }

    private Dasher newDasher() {
        if (lineStyle == XGC.LINE_SOLID) {
            return null;
        }
        byte[] pattern = gc.dashes();
        return pattern.length >= 2 ? new Dasher(pattern) : null;
    }

    /**
     * Strokes a polyline: a center line from Bresenham, widened and dashed as the GC asks.
     *
     * @param closed when true the last point joins back to the first and no caps are drawn
     */
    private void strokePolyline(int[] points, int count, boolean closed) {
        if (count < 2) {
            if (count == 1) {
                fillPixel(points[0], points[1]);
            }
            return;
        }
        Dasher dasher = newDasher();
        int segments = closed ? count : count - 1;
        for (int i = 0; i < segments; i++) {
            int a = i;
            int b = (i + 1) % count;
            boolean capStart = !closed && i == 0;
            boolean capEnd = !closed && i == segments - 1;
            strokeSegment(points[2 * a], points[2 * a + 1], points[2 * b], points[2 * b + 1],
                    dasher, capStart, capEnd);
        }
    }

    private void strokeSegment(int x0, int y0, int x1, int y1, Dasher dasher, boolean capStart,
            boolean capEnd) {
        if (lineWidth == 0) {
            // lineWidth zero asks for the thinnest possible line, which X11 draws with a plain
            // single pixel tracer that includes both end points.
            strokeThin(x0, y0, x1, y1, dasher);
            return;
        }
        // Xorg normalises wide horizontal and vertical lines so that the far end is the larger
        // coordinate; that is what makes the final pixel of such a line fall outside the figure.
        if (y0 == y1 ? x0 > x1 : (x0 == x1 && y0 > y1)) {
            int swap = x0;
            x0 = x1;
            x1 = swap;
            swap = y0;
            y0 = y1;
            y1 = swap;
        }
        double dx = x1 - x0;
        double dy = y1 - y0;
        double len = Math.hypot(dx, dy);
        double ux = len == 0 ? 0 : dx / len;
        double uy = len == 0 ? 0 : dy / len;
        double half = lineWidth / 2.0;
        // The band of pixels a wide line owns runs from -(lineWidth >> 1) to that plus the width:
        // Xorg centres even widths half a pixel towards the start of the line.
        int lo = -(lineWidth >> 1);
        int hi = lo + lineWidth - 1;
        boolean round = joinStyle == XGC.JOIN_ROUND || capStyle == XGC.CAP_ROUND;
        double mid = lo + (lineWidth - 1) / 2.0;
        // Along the line, a butt cap stops on the end point, a projecting cap half a width past it
        // and an interior join always overlaps the next segment.
        boolean buttStart = capStart && !round && capStyle != XGC.CAP_PROJECTING;
        boolean buttEnd = capEnd && !round && capStyle != XGC.CAP_PROJECTING;
        double capMin = buttStart ? 0 : -half;
        double capMax = buttEnd ? len : len + half;
        // A dash is a figure of its own: it starts and stops square, so the pattern bounds the
        // stamp just as the end points of a solid line do.
        int totalSteps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        double perStep = totalSteps == 0 ? 1 : len / totalSteps;

        int adx = Math.abs(x1 - x0);
        int ady = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = adx + ady;
        int cx = x0;
        int cy = y0;
        while (true) {
            if (dasher == null || dasher.next()) {
                double tMin = capMin;
                double tMax = capMax;
                if (dasher != null) {
                    tMin = Math.max(tMin, dasher.runStart() * perStep);
                    tMax = Math.min(tMax, dasher.runEnd() * perStep);
                }
                for (int oy = lo; oy <= hi; oy++) {
                    for (int ox = lo; ox <= hi; ox++) {
                        int px = cx + ox;
                        int py = cy + oy;
                        if (round) {
                            double dcx = ox - mid;
                            double dcy = oy - mid;
                            if (dcx * dcx + dcy * dcy > half * half) {
                                continue;
                            }
                        } else {
                            double rx = px - x0;
                            double ry = py - y0;
                            double across = ry * ux - rx * uy;
                            if (across < -half || across >= half) {
                                continue;
                            }
                            double along = rx * ux + ry * uy;
                            if (along < tMin || along >= tMax) {
                                continue;
                            }
                        }
                        fillPixel(px, py);
                    }
                }
            }
            if (cx == x1 && cy == y1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 >= ady) {
                err += ady;
                cx += sx;
            }
            if (e2 <= adx) {
                err += adx;
                cy += sy;
            }
        }
    }

    /** Bresenham tracer for {@code lineWidth} zero, the one pixel line both ends included. */
    private void strokeThin(int x0, int y0, int x1, int y1, Dasher dasher) {
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        int cx = x0;
        int cy = y0;
        while (true) {
            if (dasher == null || dasher.next()) {
                fillPixel(cx, cy);
            }
            if (cx == x1 && cy == y1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                cx += sx;
            }
            if (e2 <= dx) {
                err += dx;
                cy += sy;
            }
        }
    }

    /**
     * Walks the GC dash pattern one centre-line pixel at a time.
     *
     * <p>Even pattern positions are dashes, odd positions are gaps, matching the protocol's
     * description of {@code LineDoubleDash}; for that style a single pixel is also drawn in the
     * middle of every gap.
     */
    private final class Dasher {

        private final byte[] pattern;
        private final boolean doubleDash;
        private int index;
        private int remaining;
        private boolean on;
        private int gapPos;
        private int gapDot;
        private int step;
        private int runStart;
        private int runEnd;

        Dasher(byte[] pattern) {
            this.pattern = pattern;
            this.doubleDash = lineStyle == XGC.LINE_DOUBLE_DASH;
            int total = 0;
            for (byte b : pattern) {
                total += b & 0xFF;
            }
            this.index = 0;
            this.on = true;
            this.remaining = Math.max(1, pattern[0] & 0xFF);
            this.gapDot = -1;
            int skip = total > 0 ? Math.floorMod(gc.dashOffset(), total) : 0;
            while (skip > 0) {
                if (skip >= remaining) {
                    skip -= remaining;
                    advance();
                } else {
                    remaining -= skip;
                    skip = 0;
                }
            }
            this.runEnd = remaining;
        }

        private void advance() {
            index = (index + 1) % pattern.length;
            on = (index & 1) == 0;
            remaining = Math.max(1, pattern[index] & 0xFF);
            runStart = step;
            runEnd = step + remaining;
            if (!on) {
                gapPos = 0;
                gapDot = doubleDash ? remaining / 2 : -1;
            }
        }

        /** First pixel step of the dash the last {@link #next()} call landed in. */
        int runStart() {
            return runStart;
        }

        /** One past the last pixel step of that dash. */
        int runEnd() {
            return runEnd;
        }

        /** True when the pixel at the current position along the line is painted. */
        boolean next() {
            if (remaining <= 0) {
                advance();
            }
            remaining--;
            step++;
            if (on) {
                return true;
            }
            boolean dot = doubleDash && gapPos == gapDot;
            gapPos++;
            return dot;
        }
    }

    // ---------------------------------------------------------------- arcs

    /**
     * Strokes an arc of the ellipse inscribed in the bounding box. Angles are in 1/64 degrees,
     * counter-clockwise, zero at three o'clock; screen y grows downwards, so the sign of the sine
     * is flipped.
     */
    public void drawArc(int x, int y, int w, int h, int angle1, int angle2) {
        int[] points = arcPoints(x, y, w, h, angle1, angle2);
        strokePolyline(points, points.length / 2, false);
    }

    /** Fills the arc, closed by its chord ({@code arcMode} 0) or through the centre (mode 1). */
    public void fillArc(int x, int y, int w, int h, int angle1, int angle2) {
        int[] arc = arcPoints(x, y, w, h, angle1, angle2);
        int count = arc.length / 2;
        if (count < 2) {
            return;
        }
        int[] polygon;
        if (arcMode == 1) {
            // ArcPieSlice: close the loop through the centre of the ellipse.
            polygon = new int[arc.length + 2];
            System.arraycopy(arc, 0, polygon, 0, arc.length);
            polygon[arc.length] = (int) Math.round(x + Math.max(0, w - 1) / 2.0);
            polygon[arc.length + 1] = (int) Math.round(y + Math.max(0, h - 1) / 2.0);
        } else {
            // ArcChord: the polygon filler closes the loop with the chord.
            polygon = arc;
        }
        fillPolygon(polygon, fillRule == XGC.FILL_WINDING);
    }

    /**
     * Samples the arc into a point list.
     *
     * <p>The ellipse is centred half way across the bounding box with radii of half its width and
     * height, which is how Xorg parameterises an arc: the outline of {@code (0,0,10,10)} therefore
     * reaches x 0 to 10, while the fill stays at 0 to 9 because the polygon filler only paints
     * pixels whose centre is inside.
     */
    private static int[] arcPoints(int x, int y, int w, int h, int angle1, int angle2) {
        double rx = Math.max(0, w) / 2.0;
        double ry = Math.max(0, h) / 2.0;
        double cx = x + rx;
        double cy = y + ry;
        double start = Math.toRadians(angle1 / 64.0);
        double sweep = Math.toRadians(angle2 / 64.0);
        double full = 2 * Math.PI;
        sweep = Math.max(-full, Math.min(full, sweep));
        // One sample per pixel of arc, and never fewer than about sixteen around a full circle, so
        // that a small arc still comes out as a round shape rather than a triangle.
        double step = Math.min(0.4, 1.0 / Math.max(1.0, Math.max(rx, ry)));
        int steps = (int) Math.ceil(Math.abs(sweep) / step);
        steps = Math.max(1, Math.min(8192, steps));
        int[] points = new int[(steps + 1) * 2];
        for (int i = 0; i <= steps; i++) {
            double a = start + sweep * i / steps;
            points[2 * i] = (int) Math.round(cx + rx * Math.cos(a));
            points[2 * i + 1] = (int) Math.round(cy - ry * Math.sin(a));
        }
        return points;
    }

    // ---------------------------------------------------------------- polygons

    /**
     * Scan converts a polygon with the even-odd or the non-zero winding rule.
     *
     * <p><b>The scanline's crossings are taken at {@code y}, not at the pixel's centre {@code y +
     * 0.5}}</b>, and that half pixel is not a detail: it is worth up to half a pixel of slope in x,
     * which is a whole pixel on any edge steeper than forty five degrees. Measured against Xvfb
     * over four triangles whose hypotenuses have slopes of 2, 44/21, 44/17 and 44/45 - every row of
     * each, with no exceptions: the reference's last filled pixel is {@code ceil(xr)} taking
     * {@code xr} at {@code y}, and taking it at {@code y + 0.5} reproduces this server's old output
     * exactly. It was found chasing the 0.8% difference between the two servers' {@code xlogo},
     * whose five polygons all have steep edges.
     *
     * <p>The horizontal rule matches the reference as it stands: a span starts at {@code ceil(xl)}
     * and ends at {@code ceil(xr) - 1}. So an edge that crosses a scanline exactly on an integer
     * lands one pixel differently from the centre rule, which is the case the probe's first triangle
     * (slope 2, which puts every crossing on an integer at these scanlines) is there to pin.
     *
     * @param winding true for {@code FillWinding}, false for {@code FillEvenOdd}
     */
    public void fillPolygon(int[] points, boolean winding) {
        int n = points.length / 2;
        if (n < 3) {
            return;
        }
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            int py = points[2 * i + 1];
            minY = Math.min(minY, py);
            maxY = Math.max(maxY, py);
        }
        int firstRow = Math.max(clipY0, minY);
        int lastRow = Math.min(clipY1 - 1, maxY);
        if (firstRow > lastRow) {
            return;
        }
        double[] xs = new double[n];
        int[] dirs = new int[n];
        for (int y = firstRow; y <= lastRow; y++) {
            double sy = y;
            int count = 0;
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                double ay = points[2 * i + 1];
                double by = points[2 * j + 1];
                if ((ay <= sy) == (by <= sy)) {
                    continue;
                }
                double ax = points[2 * i];
                double bx = points[2 * j];
                xs[count] = ax + (sy - ay) * (bx - ax) / (by - ay);
                dirs[count] = by > ay ? 1 : -1;
                count++;
            }
            if (count < 2) {
                continue;
            }
            sortCrossings(xs, dirs, count);
            if (winding) {
                int wind = 0;
                for (int i = 0; i < count - 1; i++) {
                    wind += dirs[i];
                    if (wind != 0) {
                        fillSpan(y, firstPixel(xs[i]), pastLastPixel(xs[i + 1]) - 1);
                    }
                }
            } else {
                for (int i = 0; i + 1 < count; i += 2) {
                    fillSpan(y, firstPixel(xs[i]), pastLastPixel(xs[i + 1]) - 1);
                }
            }
        }
    }

    /**
     * First pixel of a span starting at {@code x}. A scan line runs from the first whole column at
     * or after the start of the span, so a span from 1 to 10 covers the pixels 1 to 9.
     */
    private static int firstPixel(double x) {
        return (int) Math.ceil(x);
    }

    /** One past the last pixel covered by a span ending at {@code x}. */
    private static int pastLastPixel(double x) {
        return (int) Math.ceil(x);
    }

    /** First pixel whose centre is at or right of {@code x}, the outline sampling rule. */
    private static int pixelCentre(double x) {
        return (int) Math.ceil(x - 0.5);
    }

    private static void sortCrossings(double[] xs, int[] dirs, int count) {
        for (int i = 1; i < count; i++) {
            double x = xs[i];
            int d = dirs[i];
            int j = i - 1;
            while (j >= 0 && xs[j] > x) {
                xs[j + 1] = xs[j];
                dirs[j + 1] = dirs[j];
                j--;
            }
            xs[j + 1] = x;
            dirs[j + 1] = d;
        }
    }

    // ---------------------------------------------------------------- text

    /**
     * Draws {@code length} bytes of text with the pen starting at {@code (x, y)}, where y is the
     * baseline. Bytes are Latin-1, the range a core font can address.
     */
    public void drawText8(XFont font, int x, int y, byte[] chars, int offset, int length) {
        if (font == null) {
            return;
        }
        int pen = x;
        for (int i = 0; i < length; i++) {
            int ch = chars[offset + i] & 0xFF;
            drawGlyph(font, ch, pen, y);
            pen += font.charWidth(ch);
        }
    }

    /** Draws {@code length} 16 bit characters, the {@code PolyText16} mapping. */
    public void drawText16(XFont font, int x, int y, char[] chars, int offset, int length) {
        if (font == null) {
            return;
        }
        int pen = x;
        for (int i = 0; i < length; i++) {
            int ch = chars[offset + i];
            drawGlyph(font, ch, pen, y);
            pen += font.charWidth(ch);
        }
    }

    private void drawGlyph(XFont font, int ch, int pen, int baseline) {
        Glyph glyph = glyphFor(font, ch);
        if (fillStyle != XGC.FILL_OPAQUE_STIPPLED) {
            if (glyph == null) {
                return;
            }
            for (int my = 0; my < glyph.height; my++) {
                int y = baseline + glyph.originY + my;
                if (y < clipY0 || y >= clipY1) {
                    continue;
                }
                int rowBase = my * glyph.width;
                for (int mx = 0; mx < glyph.width; mx++) {
                    if (glyph.bits[rowBase + mx] != 0) {
                        fillPixel(pen + glyph.originX + mx, y);
                    }
                }
            }
            return;
        }
        // FillOpaqueStippled overwrites the whole character cell: the stipple and the glyph ink
        // together select the foreground, everything else in the cell gets the background.
        int cellWidth = Math.max(0, font.charWidth(ch));
        int top = baseline - font.ascent();
        int bottom = baseline + font.descent();
        for (int y = Math.max(top, clipY0); y < bottom && y < clipY1; y++) {
            for (int x = Math.max(pen, clipX0); x < pen + cellWidth && x < clipX1; x++) {
                boolean ink = glyph != null && glyph.ink(x - pen, y - baseline);
                writePixel(x, y, ink && stippleSet(x, y) ? foreground : background);
            }
        }
    }

    /** A rasterized glyph: a 1 bit per pixel ink mask positioned relative to the pen and baseline. */
    private record Glyph(int originX, int originY, int width, int height, byte[] bits) {

        boolean ink(int relX, int relY) {
            int mx = relX - originX;
            int my = relY - originY;
            if (mx < 0 || my < 0 || mx >= width || my >= height) {
                return false;
            }
            return bits[my * width + mx] != 0;
        }
    }

    private record GlyphKey(Font font, int codePoint) {
    }

    /**
     * Glyphs are rasterized from the AWT font behind the X font and cached globally: the same font
     * is shared by every client, and a request typically repeats the same few dozen characters.
     */
    private static final Map<GlyphKey, Glyph> GLYPH_CACHE = new ConcurrentHashMap<>();

    private static final int GLYPH_CACHE_LIMIT = 8192;

    private static Glyph glyphFor(XFont font, int ch) {
        // A font with a real table draws its own glyph - the shape a reference server draws, which
        // is the whole of D3's other half. When the table has no ink for the code (a space, a
        // control character, a code the font does not have) there is nothing to draw, and nothing to
        // approximate either: a Java font's glyph for it is not what the client asked for.
        BitmapFont.Glyph real = font.bitmapGlyph(ch);
        if (real != null) {
            return new Glyph(real.originX(), real.originY(), real.width(), real.height(),
                    real.bits());
        }
        if (font.bitmap() != null) {
            return null;
        }
        if (ch < 0x20 || ch == 0x7F) {
            // Control characters have no glyph; X fonts draw nothing for them.
            return null;
        }
        Font awt = font.awtFont();
        if (awt == null) {
            return null;
        }
        GlyphKey key = new GlyphKey(awt, ch);
        Glyph cached = GLYPH_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        if (GLYPH_CACHE.size() > GLYPH_CACHE_LIMIT) {
            GLYPH_CACHE.clear();
        }
        return GLYPH_CACHE.computeIfAbsent(key, k -> rasterize(awt, k.codePoint()));
    }

    /** Scan converts a glyph outline into a 1 bit ink mask. */
    private static Glyph rasterize(Font font, int ch) {
        FontRenderContext frc = new FontRenderContext(null, false, false);
        GlyphVector vector = font.createGlyphVector(frc, new char[]{(char) ch});
        Shape outline = vector.getGlyphOutline(0);
        if (outline == null) {
            return null;
        }
        Rectangle2D bounds = outline.getBounds2D();
        if (bounds.isEmpty()) {
            return null;
        }
        int x0 = (int) Math.floor(bounds.getMinX());
        int y0 = (int) Math.floor(bounds.getMinY());
        int width = (int) Math.ceil(bounds.getMaxX()) - x0;
        int height = (int) Math.ceil(bounds.getMaxY()) - y0;
        if (width <= 0 || height <= 0 || width > 4096 || height > 4096) {
            return null;
        }
        double[] edges = new double[64];
        int edgeCount = 0;
        PathIterator it = outline.getPathIterator(null, 0.2);
        double[] segment = new double[6];
        double startX = 0;
        double startY = 0;
        double curX = 0;
        double curY = 0;
        while (!it.isDone()) {
            switch (it.currentSegment(segment)) {
                case PathIterator.SEG_MOVETO -> {
                    startX = curX = segment[0];
                    startY = curY = segment[1];
                }
                case PathIterator.SEG_LINETO -> {
                    if (edgeCount + 4 > edges.length) {
                        edges = Arrays.copyOf(edges, edges.length * 2);
                    }
                    edges[edgeCount++] = curX;
                    edges[edgeCount++] = curY;
                    edges[edgeCount++] = segment[0];
                    edges[edgeCount++] = segment[1];
                    curX = segment[0];
                    curY = segment[1];
                }
                case PathIterator.SEG_CLOSE -> {
                    if (curX != startX || curY != startY) {
                        if (edgeCount + 4 > edges.length) {
                            edges = Arrays.copyOf(edges, edges.length * 2);
                        }
                        edges[edgeCount++] = curX;
                        edges[edgeCount++] = curY;
                        edges[edgeCount++] = startX;
                        edges[edgeCount++] = startY;
                    }
                    curX = startX;
                    curY = startY;
                }
                default -> {
                    // The iterator was flattened, so curves never appear.
                }
            }
            it.next();
        }
        byte[] bits = new byte[width * height];
        double[] xs = new double[Math.max(4, edgeCount / 4)];
        int[] dirs = new int[xs.length];
        for (int y = 0; y < height; y++) {
            double sy = y0 + y + 0.5;
            int count = 0;
            for (int e = 0; e + 3 < edgeCount; e += 4) {
                double ay = edges[e + 1];
                double by = edges[e + 3];
                if ((ay <= sy) == (by <= sy)) {
                    continue;
                }
                double ax = edges[e];
                double bx = edges[e + 2];
                xs[count] = ax + (sy - ay) * (bx - ax) / (by - ay);
                dirs[count] = by > ay ? 1 : -1;
                count++;
            }
            if (count < 2) {
                continue;
            }
            sortCrossings(xs, dirs, count);
            int wind = 0;
            for (int i = 0; i < count - 1; i++) {
                wind += dirs[i];
                if (wind == 0) {
                    continue;
                }
                // Outlines are sampled at pixel centres, which keeps a glyph from growing a pixel
                // fatter on every stem than the shape the font actually describes.
                int px = pixelCentre(xs[i]) - x0;
                int px1 = pixelCentre(xs[i + 1]) - 1 - x0;
                if (px < 0) {
                    px = 0;
                }
                if (px1 > width - 1) {
                    px1 = width - 1;
                }
                for (int x = px; x <= px1; x++) {
                    bits[y * width + x] = 1;
                }
            }
        }
        return new Glyph(x0, y0, width, height, bits);
    }

    // ---------------------------------------------------------------- copies

    /**
     * Copies a rectangle of pixels, applying the GC function and plane mask to every pixel as X11
     * does. Source pixels outside the source surface read as 0.
     *
     * <p>A copy on to the same surface walks rows and columns away from the source, so overlapping
     * areas are safe without first copying the rectangle aside.
     */
    public void copyArea(PixelStore src, int srcX, int srcY, int dstX, int dstY, int w, int h) {
        copyRect(src, srcX, srcY, dstX, dstY, w, h, -1);
    }

    /**
     * Copies one plane of a source rectangle: a set bit becomes all ones (the foreground colour in
     * the destination), a clear bit becomes all zeros, and the GC function then combines it.
     */
    public void copyPlane(PixelStore src, int srcX, int srcY, int dstX, int dstY, int w, int h,
            int plane) {
        copyRect(src, srcX, srcY, dstX, dstY, w, h, plane & 31);
    }

    private void copyRect(PixelStore src, int srcX, int srcY, int dstX, int dstY, int w, int h,
            int plane) {
        if (src == null || w <= 0 || h <= 0) {
            return;
        }
        // Restrict the loop to the part of the destination that can survive the clip.
        int colStart = Math.max(0, clipX0 - dstX);
        int colEnd = Math.min(w, clipX1 - dstX);
        int rowStart = Math.max(0, clipY0 - dstY);
        int rowEnd = Math.min(h, clipY1 - dstY);
        if (colStart >= colEnd || rowStart >= rowEnd) {
            return;
        }
        boolean sameSurface = src == store;
        boolean down = sameSurface && dstY > srcY;
        boolean right = sameSurface && dstX > srcX;
        // **A plain copy is one row at a time.** This is the terminal's scroll: a client copies a
        // window's own pixels over themselves, and the per-pixel loop below pays a clip test and a
        // method call for every one of ~900k pixels - it was one of the top frames in the server's
        // profile with EasyShell's SSH tab open. When the GC is a plain copy with no planes masked,
        // the clip is exactly this box (so nothing is clipped away), and the whole source rectangle
        // is inside the source, a row-wise {@code System.arraycopy} is the same pixels; the row
        // order still follows the overlap, and the copy itself handles a horizontal overlap.
        Framebuffer fastDst = store instanceof Framebuffer f ? f : null;
        // The source may be a pixmap (an off-screen buffer a toolkit blits from) as well as another
        // framebuffer, so ask it for its own storage rather than requiring a Framebuffer.
        boolean srcReady = src != null && (!(src instanceof Framebuffer f) || f.allocated());
        boolean srcOpaque = src instanceof Framebuffer f ? !f.keepsAlpha()
                : src instanceof cn.oyzh.fx.xserver.resource.XPixmap px && px.depth() < 32;
        boolean dstOpaque = fastDst != null && !fastDst.keepsAlpha();
        if (plane < 0 && directCopy && boxIsClip && fastDst != null && srcReady
                && (!dstOpaque || srcOpaque)
                && srcX + colStart >= 0 && srcX + colEnd <= src.width()
                && srcY + rowStart >= 0 && srcY + rowEnd <= src.height()) {
            int[] dst = fastDst.pixels();
            int[] from = src.pixels();
            int dstStride = fastDst.stride();
            int srcStride = src.stride();
            int span = colEnd - colStart;
            int startRow = down ? rowEnd - 1 : rowStart;
            int endRow = down ? rowStart - 1 : rowEnd;
            int rowStep = down ? -1 : 1;
            for (int row = startRow; row != endRow; row += rowStep) {
                int srcBase = (srcY + row) * srcStride + srcX + colStart;
                int dstBase = (dstY + row) * dstStride + dstX + colStart;
                if (dstOpaque) {
                    // The destination is a screen, which has no alpha: the write forces the top byte,
                    // exactly as PixelStore.setPixel does. A tight loop, not arraycopy, but still no
                    // clip test or drawn-region update per pixel.
                    for (int i = 0; i < span; i++) {
                        dst[dstBase + i] = 0xFF000000 | (from[srcBase + i] & 0x00FFFFFF);
                    }
                } else {
                    System.arraycopy(from, srcBase, dst, dstBase, span);
                }
            }
            markDrawn(dstX + colStart, dstY + rowStart, dstX + colEnd - 1, dstY + rowEnd - 1);
            // And the store's own damage, which the pixel loop would have set through setPixel;
            // without it a copied region is written but never presented (DamageTrackingTest).
            fastDst.markDirty(dstX + colStart, dstY + rowStart, dstX + colEnd, dstY + rowEnd);
            return;
        }
        int rowStep = down ? -1 : 1;
        int colStep = right ? -1 : 1;
        int row = down ? rowEnd - 1 : rowStart;
        int lastRow = down ? rowStart - 1 : rowEnd;
        for (; row != lastRow; row += rowStep) {
            int sy = srcY + row;
            int dy = dstY + row;
            for (int col = right ? colEnd - 1 : colStart; right ? col >= colStart : col < colEnd;
                    col += colStep) {
                int value = src.pixel(srcX + col, sy);
                if (plane >= 0) {
                    value = ((value >>> plane) & 1) == 0 ? 0 : 0xFFFFFFFF;
                }
                writePixel(dstX + col, dy, value);
            }
        }
    }
}
