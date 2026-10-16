package cn.oyzh.fx.xserver.draw;

import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.resource.GlyphSet;
import cn.oyzh.fx.xserver.resource.Picture;
import cn.oyzh.common.log.JulLog;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * RENDER's drawing engine: the compositing operators, the polygon rasterizer and the glyph
 * renderer.
 *
 * <p>Where the core protocol paints a pixel by applying a bitwise raster operation, RENDER blends
 * colours. Every request reduces to the same shape - take a source, attenuate it by a mask, and
 * combine it with the destination through a {@code PictOp} - so everything here funnels through
 * {@link #draw}, and the requests differ only in where the source and the mask come from.
 *
 * <h2>Operators</h2>
 *
 * The operator table from the specification is evaluated exactly as written:
 *
 * <pre>C = Ca * Fa + Cb * Fb</pre>
 *
 * with {@code Fa} and {@code Fb} functions of the two alpha values. That table is defined over
 * <em>premultiplied</em> channels, so each pixel is converted on the way in, evaluated, and
 * converted back on the way out; the values this class reads and writes are always the server's
 * non-premultiplied {@code 0xAARRGGBB}. {@code Src} and {@code Over} - the two that matter to cairo,
 * GTK and Xft - are exact, and so are the other twelve Porter-Duff operators and the disjoint and
 * conjoint families. Of the blend modes added in RENDER 0.11, the eleven separable ones (multiply
 * through exclusion) are evaluated as the standard blend followed by source-over, which is what
 * they are; only the four non-separable HSL modes are approximated, as source-over.
 *
 * <p>The destination is the screen framebuffer or a pixmap, both of which store ARGB, and the
 * framebuffer forces every stored pixel opaque. Compositing onto the screen therefore always ends
 * opaque, which is what makes a {@code Src} of a translucent ARGB32 surface show up as its blended
 * colour rather than as a hole.
 *
 * <h2>Antialiasing</h2>
 *
 * Polygon and glyph coverage is antialiased. Triangles, strips, fans and trapezoids are rasterized
 * through Java2D into a coverage image, which gives fractional edges for smooth polygons and exact
 * one-bit coverage when the picture asks for {@code PolyEdgeSharp}; glyph images carry their own
 * coverage in their alpha channel and are used directly.
 */
public final class Compositor {


    /**
     * How many pixels the last rectangle drawn put down, and how many the destination's clip refused.
     * A diagnostic: nothing in the server's behaviour depends on either.
     */
    private int written;
    /** Pixels the last request wrote, for a caller that reports what a request did. */
    public int written() {
        return written;
    }

    /** Pixels the last request left alone because the destination would not take them. */
    public int blocked() {
        return blocked;
    }

    private int blocked;

    private static final int TRANSPARENT = 0;
    private static final int WHITE = 0xFFFFFFFF;

    /** The {@code PictOpAdd} operator, which {@code AddTraps} is defined in terms of. */
    private static final int ADD = 12;

    /** First operator of the blend-mode range; everything at or above it is composed over. */
    private static final int MULTIPLY = 48;

    private final XServer server;

    public Compositor(XServer server) {
        this.server = server;
    }

    // ------------------------------------------------------------------
    // Samplers
    // ------------------------------------------------------------------

    /**
     * A pixel source addressed in the coordinates of the region being drawn, so the region's
     * position in the destination never leaks into the sampling arithmetic.
     */
    @FunctionalInterface
    public interface Sampler {

        /** Non-premultiplied ARGB at a region-relative coordinate. */
        int argbAt(int x, int y);
    }

    /**
     * A picture read as a source or a mask.
     *
     * <p>The offset is what registers the picture against the destination: the protocol gives
     * separate source and destination rectangles, and the difference between them is a constant
     * that applies to every pixel of the request. Keeping it here means the per-pixel loop never
     * sees a request-level concept.
     */
    private static final class PictureSampler implements Sampler {

        private final Picture picture;
        private final double offsetX;
        private final double offsetY;
        private final int baseX;
        private final int baseY;
        private final Picture.Transform transform;
        private final Picture alphaMap;
        private final int alphaX;
        private final int alphaY;

        PictureSampler(Picture picture, int registeredX, int registeredY, int baseX, int baseY) {
            this.picture = picture;
            this.offsetX = registeredX;
            this.offsetY = registeredY;
            this.baseX = baseX;
            this.baseY = baseY;
            Picture.Transform t = picture.transform();
            this.transform = t == null || t.isIdentity() ? null : t;
            this.alphaMap = picture.alphaMap();
            this.alphaX = picture.alphaXOrigin();
            this.alphaY = picture.alphaYOrigin();
        }

        @Override
        public int argbAt(int x, int y) {
            // The registration comes first and the transform second: the protocol aligns the
            // source location with the top left of the destination rectangle, and the transform
            // then maps that already shifted point back into the source. Applying the transform
            // to the unshifted point and adding the registration afterwards puts the pattern one
            // registration's worth of source pixels out of place under every scale and rotation.
            //
            // The half pixel is the sample point: a pixel's centre is at (x + 0.5, y + 0.5), which
            // is where the protocol says a composite reads, and what Picture.sample expects. Handing
            // it the pixel's index instead lands halfway between two pixels, and a bilinear filter
            // then averages them - a blur of half a pixel over everything drawn through a mask,
            // which is most of a toolkit's output. At 1:1 the centre samples the source pixel
            // exactly, which is the whole point: a copy has to be a copy.
            double dx = baseX + x + offsetX + 0.5;
            double dy = baseY + y + offsetY + 0.5;
            double sx = dx;
            double sy = dy;
            if (transform != null) {
                sx = transform.mapX(dx, dy);
                sy = transform.mapY(dx, dy);
                if (Double.isNaN(sx) || Double.isNaN(sy)) {
                    return TRANSPARENT;
                }
            }
            int argb = picture.sample(sx, sy);
            if (!picture.format().hasAlpha()) {
                // A format with no alpha channel declares its pixels fully opaque.
                argb |= 0xFF000000;
            }
            if (alphaMap != null) {
                int mapX = (int) Math.floor(sx) + alphaX;
                int mapY = (int) Math.floor(sy) + alphaY;
                argb = (alphaMap.pixelAt(mapX, mapY) & 0xFF000000) | (argb & 0x00FFFFFF);
            }
            return argb;
        }
    }

    /** A constant source, as {@code FillRectangles} and {@code CreateSolidFill} need. */
    private record SolidSampler(int argb) implements Sampler {
        @Override
        public int argbAt(int x, int y) {
            return argb;
        }
    }

    /** A rasterized coverage image, addressed relative to its own top left corner. */
    private record CoverageSampler(int[] coverage, int width, int height) implements Sampler {
        @Override
        public int argbAt(int x, int y) {
            if (x < 0 || y < 0 || x >= width || y >= height) {
                return TRANSPARENT;
            }
            return coverage[y * width + x];
        }
    }

    /**
     * A single glyph positioned in destination picture coordinates. The glyph's own metric offsets
     * have already been applied by the caller, so this is the top left corner of the image.
     */
    public record PlacedGlyph(GlyphSet.Glyph glyph, int x, int y) {
    }

    // ------------------------------------------------------------------
    // Composite
    // ------------------------------------------------------------------

    /**
     * {@code Composite}: combine a source with a destination through an operator, attenuated by an
     * optional mask.
     */
    public void composite(int op, Picture src, Picture mask, Picture dst, int srcX, int srcY,
                          int maskX, int maskY, int dstX, int dstY, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        prepare(src, mask, dst);
        Sampler source = new PictureSampler(src, srcX - dstX, srcY - dstY, dstX, dstY);
        Sampler maskSampler = mask == null
                ? null
                : new PictureSampler(mask, maskX - dstX, maskY - dstY, dstX, dstY);
        boolean componentAlpha = mask != null && mask.componentAlpha();
        written = 0;
        blocked = 0;
        draw(op, source, maskSampler, componentAlpha, dst, dstX, dstY, width, height);
        report(dst, dstX, dstY, width, height);
        finish(dst);
    }

    /**
     * Reports a composite onto a window, with the two numbers that say whether the client's drawing
     * was seen at all: how many pixels it put down and how many the window's clip refused.
     *
     * <p>A composite onto a window that is refused entirely is a client drawing something that never
     * appears - a dialog with no contents, a menu that stays empty - and there is nothing else in the
     * log to say so: the request is well formed, the pixels are in the source, and the reason is
     * always outside the request. The window's clip and the picture's origin are printed with it
     * because those are the two things that decide it, and the region is printed whenever anything
     * was refused, which is the case worth reading.
     */
    private void report(Picture dst, int x, int y, int width, int height) {
        if (dst.window() == null || !JulLog.isDebugEnabled()) {
            return;
        }
        JulLog.debug("composite onto window 0x{} at ({},{}) {}x{}: {} pixels written, {} refused; "
                        + "picture origin ({},{}), window at ({},{}), visible {}",
                Integer.toHexString(dst.window().id()), x, y, width, height, written, blocked,
                dst.originX(), dst.originY(), dst.window().absoluteX(), dst.window().absoluteY(),
                blocked == 0 ? "all" : dst.window().visibleRegion());
    }

    // ------------------------------------------------------------------
    // FillRectangles
    // ------------------------------------------------------------------

    /**
     * {@code FillRectangles}: fill rectangles with a constant colour. The colour travels in the
     * request rather than in the picture, so the same picture can be filled a different way each
     * time without a round trip through {@code ChangePicture}.
     */
    public void fillRectangles(int op, Picture dst, int argb, List<Rectangle> rectangles) {
        prepare(dst);
        // An opaque colour under Src or Over is the colour everywhere, so the whole rectangle can be
        // filled a row at a time instead of compositing pixel by pixel with a mask the answer does
        // not depend on. That is what a toolkit's panels and text backgrounds are, and it was the
        // server's hottest frame with EasyShell's SSH tab open.
        // Clear is the colour zero whatever the request's colour; an opaque colour under Src or Over
        // is that colour. Both are a rectangle written a row at a time, which is what a toolkit's
        // panel paint and its alpha clears are, and it was the server's hottest frame with
        // EasyShell's SSH tab open.
        boolean clear = op == 0;
        boolean plain = clear || ((op == SRC || op == OVER) && (argb >>> 24) == 255);
        boolean drew = false;
        for (Rectangle rectangle : rectangles) {
            if (rectangle.width <= 0 || rectangle.height <= 0) {
                continue;
            }
            if (plain) {
                dst.fillArgb(rectangle.x, rectangle.y, rectangle.width, rectangle.height,
                        clear ? 0 : argb);
            } else {
                draw(op, new SolidSampler(argb), null, false, dst, rectangle.x, rectangle.y,
                        rectangle.width, rectangle.height);
            }
            drew = true;
        }
        if (drew) {
            finish(dst);
        }
    }

    // ------------------------------------------------------------------
    // Polygons
    // ------------------------------------------------------------------

    /**
     * Trapezoids, triangles, strips and fans.
     *
     * <p>The specification gives two renderings, and which one applies is decided by whether the
     * request named a mask format. With one, every polygon is added into a single coverage image
     * and the source is composited through it once, so overlapping polygons do not accumulate
     * against each other. Without one, each polygon is composited in turn, which is how a caller
     * that wants its overlaps doubled asks for them.
     *
     * @param shapes        polygons in destination picture coordinates, already tessellated
     * @param registrationX the destination point the pattern is pinned to, which for trapezoids
     *                      and triangles alike is the floor of the first polygon's top left corner
     * @param accumulate    true when every polygon should first be merged into one coverage image
     * @param sharp         true when the edges should be one bit of coverage rather than fractional
     */
    public void polygons(int op, Picture src, Picture dst, List<Shape> shapes, int srcX, int srcY,
                         int registrationX, int registrationY, boolean accumulate, boolean sharp) {
        if (shapes.isEmpty()) {
            return;
        }
        prepare(src, dst);
        // One offset for the whole request: the pattern stays globally aligned in the destination
        // rather than restarting at each polygon.
        int offsetX = srcX - registrationX;
        int offsetY = srcY - registrationY;
        if (!accumulate) {
            boolean drew = false;
            for (Shape shape : shapes) {
                Rectangle region = clip(shape.getBounds2D(), dst);
                if (region.isEmpty()) {
                    continue;
                }
                int[] coverage = rasterize(shape, region, sharp);
                draw(op, new PictureSampler(src, offsetX, offsetY, region.x, region.y),
                        new CoverageSampler(coverage, region.width, region.height),
                        false, dst, region.x, region.y, region.width, region.height);
                drew = true;
            }
            if (drew) {
                finish(dst);
            }
            return;
        }
        Rectangle region = bounds(shapes, dst);
        if (region.isEmpty()) {
            return;
        }
        int[] coverage = new int[region.width * region.height];
        for (Shape shape : shapes) {
            Rectangle patch = clip(shape.getBounds2D(), dst);
            if (patch.isEmpty()) {
                continue;
            }
            addCoverage(coverage, region, rasterize(shape, patch, sharp), patch);
        }
        draw(op, new PictureSampler(src, offsetX, offsetY, region.x, region.y),
                new CoverageSampler(coverage, region.width, region.height), false, dst,
                region.x, region.y, region.width, region.height);
        finish(dst);
    }

    /**
     * {@code AddTraps}: accumulate trap coverage into an alpha-only picture.
     *
     * <p>The specification defines the request as a {@code PictOpAdd} of a white source through
     * each trap in turn, so overlapping traps build up rather than replacing one another. That is
     * what makes it usable for composing a mask a piece at a time, which is its whole purpose.
     *
     * @param picture the destination, which the caller has already checked is alpha-only
     */
    public void addTraps(Picture picture, List<Shape> shapes, boolean sharp) {
        prepare(picture);
        boolean drew = false;
        for (Shape shape : shapes) {
            Rectangle region = clip(shape.getBounds2D(), picture);
            if (region.isEmpty()) {
                continue;
            }
            int[] coverage = rasterize(shape, region, sharp);
            draw(ADD, new SolidSampler(WHITE),
                    new CoverageSampler(coverage, region.width, region.height), false, picture,
                    region.x, region.y, region.width, region.height);
            drew = true;
        }
        if (drew) {
            finish(picture);
        }
    }

    // ------------------------------------------------------------------
    // Glyphs
    // ------------------------------------------------------------------

    /**
     * {@code CompositeGlyphs}: draw positioned glyph images as an alpha mask over the source
     * colour, which is what makes Xft text appear at all.
     *
     * <p>The placements are in destination picture coordinates, with each glyph's metric offsets
     * already applied. {@code CompositeGlyphs} carries no destination field - the caller encodes
     * the starting position in the first glyph element's delta - so the pattern is registered
     * against the request's starting position, which {@code offsetX}/{@code offsetY} must already
     * account for: they are the source coordinate that corresponds to destination {@code (0, 0)}.
     *
     * <p>When {@code accumulate} is set the glyphs are first merged into one coverage image and
     * composited once, matching the protocol's mask-format path; otherwise each glyph is composited
     * on its own, which is the path the protocol takes when the request has no mask format and is
     * what lets overlapping glyphs build up under {@code PictOpAdd}.
     */
    public void glyphs(int op, Picture src, Picture dst, int offsetX, int offsetY,
                       List<PlacedGlyph> glyphs, boolean accumulate, boolean componentAlpha) {
        if (glyphs.isEmpty()) {
            return;
        }
        prepare(src, dst);
        if (!accumulate) {
            boolean drew = false;
            for (PlacedGlyph placed : glyphs) {
                Rectangle region = clip(placed.glyph(), dst, placed.x(), placed.y());
                if (region.isEmpty()) {
                    continue;
                }
                int[] coverage = glyphCoverage(placed, region);
                draw(op, new PictureSampler(src, offsetX, offsetY, region.x, region.y),
                        new CoverageSampler(coverage, region.width, region.height),
                        componentAlpha, dst, region.x, region.y, region.width, region.height);
                drew = true;
            }
            if (drew) {
                finish(dst);
            }
            return;
        }
        Rectangle region = glyphBounds(glyphs, dst);
        if (region.isEmpty()) {
            return;
        }
        int[] coverage = new int[region.width * region.height];
        for (PlacedGlyph placed : glyphs) {
            Rectangle glyphRegion = clip(placed.glyph(), dst, placed.x(), placed.y());
            if (glyphRegion.isEmpty()) {
                continue;
            }
            addCoverage(coverage, region, glyphCoverage(placed, glyphRegion), glyphRegion);
        }
        draw(op, new PictureSampler(src, offsetX, offsetY, region.x, region.y),
                new CoverageSampler(coverage, region.width, region.height), componentAlpha, dst,
                region.x, region.y, region.width, region.height);
        finish(dst);
    }

    /** Extracts a glyph image as a coverage patch clipped to {@code region}. */
    private static int[] glyphCoverage(PlacedGlyph placed, Rectangle region) {
        int[] coverage = new int[region.width * region.height];
        GlyphSet.Glyph glyph = placed.glyph();
        for (int y = 0; y < region.height; y++) {
            int gy = region.y + y - placed.y();
            for (int x = 0; x < region.width; x++) {
                int gx = region.x + x - placed.x();
                coverage[y * region.width + x] = glyph.argbAt(gx, gy);
            }
        }
        return coverage;
    }

    private static Rectangle glyphBounds(List<PlacedGlyph> glyphs, Picture dst) {
        Rectangle total = null;
        for (PlacedGlyph placed : glyphs) {
            Rectangle region = clip(placed.glyph(), dst, placed.x(), placed.y());
            if (region.isEmpty()) {
                continue;
            }
            total = total == null ? region : total.union(region);
        }
        return total == null ? new Rectangle() : total;
    }

    private static Rectangle clip(GlyphSet.Glyph glyph, Picture dst, int x, int y) {
        return clip(new Rectangle2D.Double(x, y, glyph.width(), glyph.height()), dst);
    }

    // ------------------------------------------------------------------
    // The pixel loop
    // ------------------------------------------------------------------

    /**
     * The one place pixels are blended. Every request above reduces to a source, an optional mask
     * and a rectangle.
     */
    private void draw(int op, Sampler src, Sampler mask, boolean componentAlpha, Picture dst,
                      int regionX, int regionY, int width, int height) {
        int written = 0;
        int blocked = 0;
        for (int y = 0; y < height; y++) {
            int dy = regionY + y;
            for (int x = 0; x < width; x++) {
                int dx = regionX + x;
                if (!dst.isWritable(dx, dy)) {
                    blocked++;
                    continue;
                }
                int maskArgb = mask == null ? WHITE : mask.argbAt(x, y);
                int maskAlpha = maskArgb >>> 24;
                if (maskAlpha == 0) {
                    continue;
                }
                int srcArgb = src.argbAt(x, y);
                int dstArgb = dst.pixelAt(dx, dy);
                int result = blend(op, srcArgb, dstArgb, maskArgb, maskAlpha, componentAlpha);
                dst.writePixel(dx, dy, result);
                written++;
            }
        }
        this.written = written;
        this.blocked = blocked;
    }

    /** {@code Src} and {@code Over}, the two operators every fast path below is about. */
    private static final int SRC = 1;
    private static final int OVER = 3;

    /**
     * Applies one operator to one pixel.
     *
     * <p>The two {@code return} statements before the general path are the same answer computed
     * without the arithmetic, and each is defined by a measurement rather than by an argument: they
     * cover the cases a desktop actually draws, and {@code draw/BlendFastPathTest} checks every one
     * of them against {@link #blendGeneral} over the whole range of mask alphas and both ends of
     * every channel, so "the fast path does not change the answer" is a property the suite holds
     * rather than a claim in a comment. See backlog E3.
     *
     * @param maskArgb the mask's colour, whose channels are separate alpha values when
     *                 {@code componentAlpha} is set
     * @param maskAlpha the mask's alpha, already extracted because every path needs it
     */
    /**
     * A source composited over an opaque destination, with the source's own alpha - what a window's
     * own pixels are painted onto the screen with (B5).
     *
     * <p>It is {@link #blend} with no mask and the source-over operator, so a window's translucent
     * pixels are composited by exactly the arithmetic everything else in this server composites with
     * - including the fast paths, whose identity with the general form is what {@code
     * BlendFastPathTest} holds.
     */
    public static int compositeOver(int srcArgb, int dstArgb) {
        return blend(OVER, srcArgb, dstArgb, 0, 255, false);
    }

    static int blend(int op, int srcArgb, int dstArgb, int maskArgb, int maskAlpha,
                     boolean componentAlpha) {
        // The two cases that are a copy, and they are most of what a desktop draws: an opaque source
        // with a full mask, under Src (the answer is the source) or under Over (an opaque source
        // hides the destination completely, which is the same thing). The general path below returns
        // exactly this for both - fa = 1 and fb = 0 - and its arithmetic on a value that came from
        // eight bit channels is exact, so this is not an approximation of the answer, it is the
        // answer without the arithmetic.
        if (maskAlpha == 255 && !componentAlpha && (srcArgb >>> 24) == 255
                && (op == SRC || op == OVER)) {
            return srcArgb;
        }
        // **A mask with a partly transparent pixel, which is what a glyph or an icon is**: an opaque
        // source through an alpha mask. Under Src the answer is the source's colour carrying the
        // mask's alpha, whatever is underneath; under Over with an *opaque* destination - which the
        // framebuffer always is - the answer is the source and the destination weighted by the mask,
        // with the result still opaque.
        //
        // Both are integer arithmetic here, and they are not approximations of the general path: its
        // answer is the same rational number, and the only way the two could disagree is a rounding
        // tie, which cannot happen - the unrounded value is `(src*m + dst*(255-m)) / 255` with
        // integers throughout, and a tie there would need `2A = 255 * odd`, which has no solution.
        // The test is what says so.
        if (maskAlpha != 0 && !componentAlpha && (srcArgb >>> 24) == 255
                && (op == SRC || (op == OVER && (dstArgb >>> 24) == 255))) {
            if (op == SRC) {
                return (maskAlpha << 24) | (srcArgb & 0xFFFFFF);
            }
            int inverse = 255 - maskAlpha;
            int red = (((srcArgb >> 16) & 0xFF) * maskAlpha + ((dstArgb >> 16) & 0xFF) * inverse
                    + 127) / 255;
            int green = (((srcArgb >> 8) & 0xFF) * maskAlpha + ((dstArgb >> 8) & 0xFF) * inverse
                    + 127) / 255;
            int blue = ((srcArgb & 0xFF) * maskAlpha + (dstArgb & 0xFF) * inverse + 127) / 255;
            return 0xFF000000 | (red << 16) | (green << 8) | blue;
        }
        return blendGeneral(op, srcArgb, dstArgb, maskArgb, maskAlpha, componentAlpha);
    }

    /** The blend, in the general form the fast paths above are checked against. */
    static int blendGeneral(int op, int srcArgb, int dstArgb, int maskArgb, int maskAlpha,
                            boolean componentAlpha) {
        double sr = ((srcArgb >> 16) & 0xFF) / 255.0;
        double sg = ((srcArgb >> 8) & 0xFF) / 255.0;
        double sb = (srcArgb & 0xFF) / 255.0;
        double sa = ((srcArgb >>> 24) & 0xFF) / 255.0 * (maskAlpha / 255.0);
        if (componentAlpha) {
            // Each mask channel is its own alpha, which is what subpixel rendering and colour
            // glyphs need: red, green and blue coverage may differ across a single pixel.
            sr *= ((maskArgb >> 16) & 0xFF) / 255.0;
            sg *= ((maskArgb >> 8) & 0xFF) / 255.0;
            sb *= (maskArgb & 0xFF) / 255.0;
        }

        double dr = ((dstArgb >> 16) & 0xFF) / 255.0;
        double dg = ((dstArgb >> 8) & 0xFF) / 255.0;
        double db = (dstArgb & 0xFF) / 255.0;
        double da = ((dstArgb >>> 24) & 0xFF) / 255.0;

        if (op >= MULTIPLY) {
            // A blend mode changes the source colour first; the composition that follows is plain
            // source-over. Guarding on the opcode keeps the hot path free of the array the three
            // blended channels are returned in.
            double[] blended = blendMode(op, sr, sg, sb, dr, dg, db);
            sr = blended[0];
            sg = blended[1];
            sb = blended[2];
        }

        double fa;
        double fb;
        switch (op) {
            case 0 -> {                     // Clear
                fa = 0;
                fb = 0;
            }
            case 1 -> {                     // Src
                fa = 1;
                fb = 0;
            }
            case 2 -> {                     // Dst
                fa = 0;
                fb = 1;
            }
            case 3 -> {                     // Over
                fa = 1;
                fb = 1 - sa;
            }
            case 4 -> {                     // OverReverse
                fa = 1 - da;
                fb = 1;
            }
            case 5 -> {                     // In
                fa = da;
                fb = 0;
            }
            case 6 -> {                     // InReverse
                fa = 0;
                fb = sa;
            }
            case 7 -> {                     // Out
                fa = 1 - da;
                fb = 0;
            }
            case 8 -> {                     // OutReverse
                fa = 0;
                fb = 1 - sa;
            }
            case 9 -> {                     // Atop
                fa = da;
                fb = 1 - sa;
            }
            case 10 -> {                    // AtopReverse
                fa = 1 - da;
                fb = sa;
            }
            case 11 -> {                    // Xor
                fa = 1 - da;
                fb = 1 - sa;
            }
            case 12 -> {                    // Add
                fa = 1;
                fb = 1;
            }
            case 13 -> {                    // Saturate
                fa = Math.min(1, divide(1 - da, sa));
                fb = 1;
            }
            case 16, 17, 18 -> {            // DisjointClear, DisjointSrc, DisjointDst
                fa = op == 17 ? 1 : 0;
                fb = op == 18 ? 1 : 0;
            }
            case 19 -> {                    // DisjointOver
                fa = 1;
                fb = Math.min(1, divide(1 - sa, da));
            }
            case 20 -> {                    // DisjointOverReverse
                fa = Math.min(1, divide(1 - da, sa));
                fb = 1;
            }
            case 21 -> {                    // DisjointIn
                fa = Math.max(1 - divide(1 - da, sa), 0);
                fb = 0;
            }
            case 22 -> {                    // DisjointInReverse
                fa = 0;
                fb = Math.max(1 - divide(1 - sa, da), 0);
            }
            case 23 -> {                    // DisjointOut
                fa = Math.min(1, divide(1 - da, sa));
                fb = 0;
            }
            case 24 -> {                    // DisjointOutReverse
                fa = 0;
                fb = Math.min(1, divide(1 - sa, da));
            }
            case 25 -> {                    // DisjointAtop
                fa = Math.max(1 - divide(1 - da, sa), 0);
                fb = Math.min(1, divide(1 - sa, da));
            }
            case 26 -> {                    // DisjointAtopReverse
                fa = Math.min(1, divide(1 - da, sa));
                fb = Math.max(1 - divide(1 - sa, da), 0);
            }
            case 27 -> {                    // DisjointXor
                fa = Math.min(1, divide(1 - da, sa));
                fb = Math.min(1, divide(1 - sa, da));
            }
            case 32, 33, 34 -> {            // ConjointClear, ConjointSrc, ConjointDst
                fa = op == 33 ? 1 : 0;
                fb = op == 34 ? 1 : 0;
            }
            case 35 -> {                    // ConjointOver
                fa = 1;
                fb = Math.max(1 - divide(sa, da), 0);
            }
            case 36 -> {                    // ConjointOverReverse
                fa = Math.max(1 - divide(da, sa), 0);
                fb = 1;
            }
            case 37 -> {                    // ConjointIn
                fa = Math.min(1, divide(da, sa));
                fb = 0;
            }
            case 38 -> {                    // ConjointInReverse
                fa = 0;
                fb = Math.min(divide(sa, da), 1);
            }
            case 39 -> {                    // ConjointOut
                fa = Math.max(1 - divide(da, sa), 0);
                fb = 0;
            }
            case 40 -> {                    // ConjointOutReverse
                fa = 0;
                fb = Math.max(1 - divide(sa, da), 0);
            }
            case 41 -> {                    // ConjointAtop
                fa = Math.min(1, divide(da, sa));
                fb = Math.max(1 - divide(sa, da), 0);
            }
            case 42 -> {                    // ConjointAtopReverse
                fa = Math.max(1 - divide(da, sa), 0);
                fb = Math.min(1, divide(sa, da));
            }
            case 43 -> {                    // ConjointXor
                fa = Math.max(1 - divide(da, sa), 0);
                fb = Math.max(1 - divide(sa, da), 0);
            }
            default -> {                    // The blend modes, all composed as source-over
                fa = 1;
                fb = 1 - sa;
            }
        }

        // The table is defined over premultiplied channels.
        double ra = limit(sa * fa + da * fb);
        double rr = limit(sr * sa * fa + dr * da * fb);
        double rg = limit(sg * sa * fa + dg * da * fb);
        double rb = limit(sb * sa * fa + db * da * fb);
        if (ra <= 0) {
            return TRANSPARENT;
        }
        // Back to the non-premultiplied form the pixel stores hold.
        int alpha = (int) Math.round(ra * 255);
        int red = (int) Math.round(limit(rr / ra) * 255);
        int green = (int) Math.round(limit(rg / ra) * 255);
        int blue = (int) Math.round(limit(rb / ra) * 255);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * The separable blend modes of RENDER 0.11. They modify the source colour before the usual
     * over-composition, so the caller applies them and then treats the operator as source-over.
     * The four HSL modes are not separable and are left alone, which makes them source-over.
     */
    private static double[] blendMode(int op, double sr, double sg, double sb,
                                      double dr, double dg, double db) {
        switch (op) {
            case 48 -> {                    // Multiply
                return new double[]{sr * dr, sg * dg, sb * db};
            }
            case 49 -> {                    // Screen
                return new double[]{screen(sr, dr), screen(sg, dg), screen(sb, db)};
            }
            case 50 -> {                    // Overlay
                return new double[]{overlay(sr, dr), overlay(sg, dg), overlay(sb, db)};
            }
            case 51 -> {                    // Darken
                return new double[]{Math.min(sr, dr), Math.min(sg, dg), Math.min(sb, db)};
            }
            case 52 -> {                    // Lighten
                return new double[]{Math.max(sr, dr), Math.max(sg, dg), Math.max(sb, db)};
            }
            case 53 -> {                    // ColorDodge
                return new double[]{colorDodge(sr, dr), colorDodge(sg, dg), colorDodge(sb, db)};
            }
            case 54 -> {                    // ColorBurn
                return new double[]{colorBurn(sr, dr), colorBurn(sg, dg), colorBurn(sb, db)};
            }
            case 55 -> {                    // HardLight
                return new double[]{hardLight(sr, dr), hardLight(sg, dg), hardLight(sb, db)};
            }
            case 56 -> {                    // SoftLight
                return new double[]{softLight(sr, dr), softLight(sg, dg), softLight(sb, db)};
            }
            case 57 -> {                    // Difference
                return new double[]{Math.abs(sr - dr), Math.abs(sg - dg), Math.abs(sb - db)};
            }
            case 58 -> {                    // Exclusion
                return new double[]{sr + dr - 2 * sr * dr, sg + dg - 2 * sg * dg,
                        sb + db - 2 * sb * db};
            }
            default -> {
                return new double[]{sr, sg, sb};
            }
        }
    }

    private static double screen(double s, double d) {
        return s + d - s * d;
    }

    private static double overlay(double s, double d) {
        return d <= 0.5 ? 2 * s * d : 1 - 2 * (1 - s) * (1 - d);
    }

    private static double colorDodge(double s, double d) {
        if (d <= 0) {
            return 0;
        }
        if (s >= 1) {
            return 1;
        }
        return Math.min(1, d / (1 - s));
    }

    private static double colorBurn(double s, double d) {
        if (d >= 1) {
            return 1;
        }
        if (s <= 0) {
            return 0;
        }
        return 1 - Math.min(1, (1 - d) / s);
    }

    private static double hardLight(double s, double d) {
        return overlay(d, s);
    }

    private static double softLight(double s, double d) {
        if (s <= 0.5) {
            return d - (1 - 2 * s) * d * (1 - d);
        }
        double factor = d <= 0.25 ? ((16 * d - 12) * d + 4) * d : Math.sqrt(d);
        return d + (2 * s - 1) * (factor - d);
    }

    /** The specification defines division by zero as positive infinity, not as an error. */
    private static double divide(double numerator, double denominator) {
        return denominator == 0 ? Double.POSITIVE_INFINITY : numerator / denominator;
    }

    private static double limit(double value) {
        if (value < 0) {
            return 0;
        }
        return value > 1 ? 1 : value;
    }

    // ------------------------------------------------------------------
    // Coverage rasterization
    // ------------------------------------------------------------------

    /**
     * Rasterizes a polygon into a coverage patch.
     *
     * <p>The result is a mask in the server's ARGB form: white with the coverage in the alpha
     * channel, so it can be handed to the same blending code a picture mask uses. Java2D does the
     * scan conversion, which is why a path tessellated into thousands of triangles by cairo comes
     * out with the same antialiasing the rest of the desktop has.
     *
     * @param region  where the patch sits in destination coordinates, also its size
     * @param sharp   true for one-bit coverage, as {@code PolyEdgeSharp} and depth-1 masks ask for
     */
    static int[] rasterize(Shape shape, Rectangle region, boolean sharp) {
        int[] pixels = new int[region.width * region.height];
        BufferedImage image = new BufferedImage(region.width, region.height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.Src);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    sharp ? RenderingHints.VALUE_ANTIALIAS_OFF
                            : RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
            graphics.translate(-region.x, -region.y);
            graphics.setColor(Color.WHITE);
            graphics.fill(shape);
        } finally {
            graphics.dispose();
        }
        image.getRGB(0, 0, region.width, region.height, pixels, 0, region.width);
        for (int i = 0; i < pixels.length; i++) {
            int alpha = pixels[i] >>> 24;
            if (sharp) {
                alpha = alpha >= 128 ? 255 : 0;
            }
            pixels[i] = (alpha << 24) | 0x00FFFFFF;
        }
        return pixels;
    }

    /**
     * Adds one coverage patch at its place in a larger accumulating patch, saturating at full
     * coverage. This is the protocol's {@code Combine (Add, tmp, triangle, None)}: without the
     * saturation two triangles meeting along an edge would leave a visible seam.
     */
    private static void addCoverage(int[] target, Rectangle targetRegion, int[] patch,
                                    Rectangle patchRegion) {
        int offsetX = patchRegion.x - targetRegion.x;
        int offsetY = patchRegion.y - targetRegion.y;
        for (int y = 0; y < patchRegion.height; y++) {
            int ty = y + offsetY;
            if (ty < 0 || ty >= targetRegion.height) {
                continue;
            }
            for (int x = 0; x < patchRegion.width; x++) {
                int tx = x + offsetX;
                if (tx < 0 || tx >= targetRegion.width) {
                    continue;
                }
                int index = ty * targetRegion.width + tx;
                int added = (target[index] >>> 24) + (patch[y * patchRegion.width + x] >>> 24);
                target[index] = (Math.min(255, added) << 24) | 0x00FFFFFF;
            }
        }
    }

    /** Bounding box of a set of polygons, clipped to whatever the destination can reach. */
    private static Rectangle bounds(List<Shape> shapes, Picture dst) {
        Rectangle total = null;
        for (Shape shape : shapes) {
            Rectangle region = clip(shape.getBounds2D(), dst);
            if (region.isEmpty()) {
                continue;
            }
            total = total == null ? region : total.union(region);
        }
        return total == null ? new Rectangle() : total;
    }

    /**
     * Clips a shape's bounding box to the destination picture.
     *
     * <p>Fixed point polygon coordinates can be enormous - a trapezoid clipped by a window edge is
     * routinely specified with edges far outside it - so the box has to be trimmed before anything
     * is allocated for it, or a client could ask for a gigabyte of coverage image.
     */
    private static Rectangle clip(Rectangle2D box, Picture dst) {
        double x0 = Math.max(0, Math.floor(box.getMinX()));
        double y0 = Math.max(0, Math.floor(box.getMinY()));
        double x1 = Math.min(dst.width(), Math.ceil(box.getMaxX()));
        double y1 = Math.min(dst.height(), Math.ceil(box.getMaxY()));
        if (x0 >= x1 || y0 >= y1) {
            return new Rectangle();
        }
        return new Rectangle((int) x0, (int) y0, (int) (x1 - x0), (int) (y1 - y0));
    }

    /**
     * Reports that a drawing request finished on a window. Writing pixels already tells the
     * framebuffer which rows changed; this additionally marks the whole screen, which is what the
     * display backends latch on to before the next repaint.
     */
    private void finish(Picture dst) {
        if (dst.window() == null || server == null) {
            return;
        }
        cn.oyzh.fx.xserver.resource.XWindow window = dst.window();
        if (window.keepsOwnPixels()) {
            // RENDER drew into the window's own store, so what it wrote has to reach the screen from
            // there (B5). Only the region actually written is composited and marked: a window that
            // shows nothing composites nothing, and a small draw does not repaint a screen.
            Rectangle drawn = dst.takeWindowDrawn();
            if (drawn != null) {
                server.compositeOwnPixels(window, drawn);
            }
        } else {
            server.markScreenDirty(window);
        }
    }

    /**
     * Renews every picture's cached visible rectangle before the pixel loop starts. A window's clip
     * is consulted once per pixel, so it is worth computing once per request - and the request is
     * the right scope, because the server holds its lock for the whole of one and the window tree
     * cannot move underneath it.
     */
    private static void prepare(Picture... pictures) {
        for (Picture picture : pictures) {
            if (picture != null) {
                picture.refreshClip();
            }
        }
    }
}
