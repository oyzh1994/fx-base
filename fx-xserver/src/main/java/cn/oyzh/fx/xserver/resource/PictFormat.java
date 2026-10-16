package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.resource.XColormap;

/**
 * A RENDER picture format: the description of how the bits of a pixel divide into red, green, blue
 * and alpha.
 *
 * <p>A format is either <em>direct</em>, where each channel is a bit field, or <em>indexed</em>,
 * where the pixel names a colormap cell. Formats are not resources: like visuals they are created
 * by the server at startup, cannot be named by clients, and are shared by every connection. A
 * client quotes a format id in {@code CreatePicture} and gets the whole list back from
 * {@code QueryPictFormats}, which is the reply it makes every later decision on.
 *
 * <p>The set of formats is deliberately the one a real server publishes, because clients do not
 * negotiate: cairo and Xft ask for {@code PictStandardA8} and {@code PictStandardARGB32} by looking
 * for a format with an exact mask layout, and a server that reports slightly different masks is a
 * server they silently decide has no alpha support.
 *
 * <p>Pixels crossing this boundary are always non-premultiplied {@code 0xAARRGGBB}. The encoders
 * and decoders here exist so the rest of the server never has to know about channel widths: a
 * depth-16 picture and a depth-32 picture both arrive as the same ARGB value.
 */
public final class PictFormat {

    /** {@code PictType} value: the pixel is a colormap index. */
    public static final int INDEXED = 0;

    /** {@code PictType} value: the pixel is a set of bit fields. */
    public static final int DIRECT = 1;

    /** The {@code None} format, used wherever the protocol lets a format be absent. */
    public static final int NONE = 0;

    private final int id;
    private final int type;
    private final int depth;

    private final int redShift;
    private final int redMask;
    private final int greenShift;
    private final int greenMask;
    private final int blueShift;
    private final int blueMask;
    private final int alphaShift;
    private final int alphaMask;

    /** The visual this format was derived from, or null for the standalone alpha formats. */
    private final VisualInfo visual;

    /** The colormap indexed pixels resolve through, or null for direct formats. */
    private final XColormap colormap;

    private PictFormat(int id, int type, int depth, VisualInfo visual, XColormap colormap,
                       int redMask, int greenMask, int blueMask, int alphaMask) {
        this.id = id;
        this.type = type;
        this.depth = depth;
        this.visual = visual;
        this.colormap = colormap;
        this.redMask = redMask;
        this.greenMask = greenMask;
        this.blueMask = blueMask;
        this.alphaMask = alphaMask;
        this.redShift = shiftOf(redMask);
        this.greenShift = shiftOf(greenMask);
        this.blueShift = shiftOf(blueMask);
        this.alphaShift = shiftOf(alphaMask);
    }

    /**
     * A direct format matching a screen visual.
     *
     * @param alphaMask the alpha field, or 0 for a format whose pixels are fully opaque. A visual
     *                  carries no alpha of its own, so the caller decides: the depth 32 visual is
     *                  the one that must be given {@code 0xFF000000} or ARGB32 surfaces stop being
     *                  translucent.
     */
    public static PictFormat direct(int id, VisualInfo visual, int alphaMask) {
        return new PictFormat(id, DIRECT, visual.depth(), visual, null, visual.redMask(),
                visual.greenMask(), visual.blueMask(), alphaMask);
    }

    /**
     * A direct format with no colour channels, so every bit is alpha. This is what A1, A4 and A8
     * are, and what a glyph set or a mask picture is normally created with.
     *
     * @param depth     bits per pixel, and therefore bits of alpha
     * @param alphaMask the alpha field, which for these formats is the whole value
     */
    public static PictFormat alphaOnly(int id, int depth, int alphaMask) {
        return new PictFormat(id, DIRECT, depth, null, null, 0, 0, 0, alphaMask);
    }

    /**
     * An indexed format over a visual's colormap. Indexed formats have no alpha channel: a pixel
     * either names a colour or it does not.
     */
    public static PictFormat indexed(int id, VisualInfo visual, XColormap colormap) {
        return new PictFormat(id, INDEXED, visual.depth(), visual, colormap, 0, 0, 0, 0);
    }

    public int id() {
        return id;
    }

    /** {@link #INDEXED} or {@link #DIRECT}. */
    public int type() {
        return type;
    }

    public int depth() {
        return depth;
    }

    public boolean isDirect() {
        return type == DIRECT;
    }

    public boolean isIndexed() {
        return type == INDEXED;
    }

    /** The visual this format describes, or null for the formats that exist without one. */
    public VisualInfo visual() {
        return visual;
    }

    public XColormap colormap() {
        return colormap;
    }

    public int redShift() {
        return redShift;
    }

    public int redMask() {
        return redMask;
    }

    public int greenShift() {
        return greenShift;
    }

    public int greenMask() {
        return greenMask;
    }

    public int blueShift() {
        return blueShift;
    }

    public int blueMask() {
        return blueMask;
    }

    public int alphaShift() {
        return alphaShift;
    }

    public int alphaMask() {
        return alphaMask;
    }

    /** True when pixels carry an alpha channel rather than being opaque by definition. */
    public boolean hasAlpha() {
        return alphaMask != 0;
    }

    /** True when the format has alpha and no colour at all, as A1/A4/A8 do. */
    public boolean isAlphaOnly() {
        return isDirect() && alphaMask != 0 && redMask == 0 && greenMask == 0 && blueMask == 0;
    }

    /** True when the format has colour channels. Such a glyph set is component-alpha. */
    public boolean hasColor() {
        return redMask != 0 || greenMask != 0 || blueMask != 0;
    }

    /**
     * Whether this format is the one a visual would be given, which is what {@code CreatePicture}
     * checks when the drawable is a window. Alpha is ignored: a picture over a window may or may
     * not use the visual's alpha channel.
     */
    public boolean matchesVisual(VisualInfo other) {
        if (other == null) {
            return false;
        }
        if (isIndexed()) {
            return visual != null && visual.id() == other.id();
        }
        return depth == other.depth() && redMask == other.redMask()
                && greenMask == other.greenMask() && blueMask == other.blueMask();
    }

    /** Decodes a stored pixel into non-premultiplied {@code 0xAARRGGBB}. */
    public int toArgb(int pixel) {
        if (isIndexed()) {
            // Indexed pixels have no alpha, and the colour lives in the colormap.
            int argb = colormap == null ? (0xFF000000 | (pixel & 0xFF)) : colormap.colorAt(pixel);
            return 0xFF000000 | (argb & 0x00FFFFFF);
        }
        int alpha = alphaMask == 0 ? 0xFF : extract(pixel, alphaMask);
        int red = extract(pixel, redMask);
        int green = extract(pixel, greenMask);
        int blue = extract(pixel, blueMask);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /** Encodes non-premultiplied {@code 0xAARRGGBB} as a pixel of this format. */
    public int fromArgb(int argb) {
        if (isIndexed()) {
            if (colormap == null) {
                return argb & 0xFF;
            }
            int index = colormap.findColor((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF);
            // A colour the map does not hold has no index; the low byte is the closest thing to a
            // defined answer, and it keeps a fill from writing an out of range index.
            return index >= 0 ? index : (argb & 0xFF);
        }
        int pixel = pack((argb >> 16) & 0xFF, redMask, redShift)
                | pack((argb >> 8) & 0xFF, greenMask, greenShift)
                | pack(argb & 0xFF, blueMask, blueShift);
        if (alphaMask != 0) {
            pixel |= pack((argb >>> 24) & 0xFF, alphaMask, alphaShift);
        }
        return pixel;
    }

    /** The alpha of a stored pixel as 0..255, or 255 when the format has no alpha channel. */
    public int alphaOf(int pixel) {
        if (isIndexed() || alphaMask == 0) {
            return 0xFF;
        }
        return extract(pixel, alphaMask);
    }

    private static int shiftOf(int mask) {
        return mask == 0 ? 0 : Integer.numberOfTrailingZeros(mask);
    }

    /**
     * Scales one channel of a pixel up to 8 bits. The widening is rounded rather than truncated so
     * that the largest value of a narrow channel - 31 in a 5 bit green - maps to exactly 255, which
     * is what lets a 16 bit picture round trip through ARGB without drifting brighter.
     */
    private static int extract(int pixel, int mask) {
        if (mask == 0) {
            return 0;
        }
        int raw = (pixel & mask) >>> Integer.numberOfTrailingZeros(mask);
        int bits = Integer.bitCount(mask);
        if (bits == 8) {
            return raw;
        }
        int max = (1 << bits) - 1;
        return (raw * 255 + max / 2) / max;
    }

    private static int pack(int value8, int mask, int shift) {
        if (mask == 0) {
            return 0;
        }
        int bits = Integer.bitCount(mask);
        int max = (1 << bits) - 1;
        int scaled = (value8 * max + 127) / 255;
        return (scaled << shift) & mask;
    }

    @Override
    public String toString() {
        if (isIndexed()) {
            return "PictFormat(0x" + Integer.toHexString(id) + ", indexed depth " + depth + ")";
        }
        return "PictFormat(0x" + Integer.toHexString(id) + ", direct depth " + depth
                + ", rgb " + Integer.toHexString(redMask) + "/" + Integer.toHexString(greenMask)
                + "/" + Integer.toHexString(blueMask) + ", alpha "
                + Integer.toHexString(alphaMask) + ")";
    }
}
