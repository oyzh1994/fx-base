package cn.oyzh.fx.xserver.display;

/**
 * A visual: the description of how pixel values map to colours for a given depth.
 *
 * <p>Server side visuals are values, not resources - they cannot be created or destroyed by
 * clients. Each one has an id that is stable for the lifetime of the server and that clients put
 * in {@code CreateWindow}, colormaps and RENDER pictures.
 */
public record VisualInfo(
        int id,
        int depth,
        byte visualClass,
        byte bitsPerRgbValue,
        short colormapEntries,
        int redMask,
        int greenMask,
        int blueMask) {

    // Visual classes from the X11 protocol.
    public static final byte STATIC_GRAY = 0;
    public static final byte GRAY_SCALE = 1;
    public static final byte STATIC_COLOR = 2;
    public static final byte PSEUDO_COLOR = 3;
    public static final byte TRUE_COLOR = 4;
    public static final byte DIRECT_COLOR = 5;

    /** Depth 24 TrueColor, the visual every modern toolkit expects to find. */
    public static VisualInfo trueColor24(int id) {
        return new VisualInfo(id, 24, TRUE_COLOR, (byte) 8, (short) 256, 0x00FF0000, 0x0000FF00, 0x000000FF);
    }

    /** Depth 30 TrueColor (10 bits per channel), used by clients that ask for deep colour. */
    public static VisualInfo trueColor30(int id) {
        return new VisualInfo(id, 30, TRUE_COLOR, (byte) 10, (short) 1024, 0x3FF00000, 0x000FFC00, 0x000003FF);
    }

    /** Depth 32 TrueColor with an alpha channel. RENDER and Composite build on this. */
    public static VisualInfo argb32(int id) {
        return new VisualInfo(id, 32, TRUE_COLOR, (byte) 8, (short) 256, 0x00FF0000, 0x0000FF00, 0x000000FF);
    }

    public static VisualInfo trueColor16(int id) {
        return new VisualInfo(id, 16, TRUE_COLOR, (byte) 5, (short) 32, 0xF800, 0x07E0, 0x001F);
    }

    public static VisualInfo trueColor8(int id) {
        return new VisualInfo(id, 8, TRUE_COLOR, (byte) 3, (short) 8, 0xE0, 0x1C, 0x03);
    }

    public static VisualInfo pseudoColor8(int id) {
        return new VisualInfo(id, 8, PSEUDO_COLOR, (byte) 8, (short) 256, 0, 0, 0);
    }

    public static VisualInfo staticGray1(int id) {
        return new VisualInfo(id, 1, STATIC_GRAY, (byte) 1, (short) 2, 0, 0, 0);
    }

    public static VisualInfo staticGray8(int id) {
        return new VisualInfo(id, 8, STATIC_GRAY, (byte) 8, (short) 256, 0, 0, 0);
    }

    /** True when pixel values are composed of separate red/green/blue bit fields. */
    public boolean isTrueColor() {
        return visualClass == TRUE_COLOR || visualClass == DIRECT_COLOR;
    }

    /** True when pixel values are colormap indices. */
    public boolean isIndexed() {
        return visualClass == PSEUDO_COLOR || visualClass == STATIC_COLOR
                || visualClass == GRAY_SCALE || visualClass == STATIC_GRAY;
    }

    /**
     * Converts a packed pixel value in this visual to 0xAARRGGBB.
     */
    public int toArgb(int pixel) {
        if (!isTrueColor()) {
            // Indexed visuals go through the colormap, which the caller must supply.
            return 0xFF000000 | pixel;
        }
        int r = extract(pixel, redMask, bitsPerRgbValue);
        int g = extract(pixel, greenMask, bitsPerRgbValue);
        int b = extract(pixel, blueMask, bitsPerRgbValue);
        return alphaOf(pixel) | (r << 16) | (g << 8) | b;
    }

    /**
     * Whether the bits this visual leaves unused are an alpha channel.
     *
     * <p>Only ARGB32 qualifies here: depth 24 has no spare byte at all, and the spare bit a 16 bit
     * TrueColor visual has is not an alpha channel.
     */
    public boolean hasAlpha() {
        return isTrueColor() && depth == 32;
    }

    /**
     * The alpha a wire pixel carries, or opaque for a visual that has no alpha channel.
     *
     * <p>A client that uploads into an ARGB32 drawable - a RENDER source, a cursor image, a
     * translucent window - sends 0xAARRGGBB and expects the top byte to be kept. Forcing it opaque
     * is what turns a cursor's transparent surround into a solid black block, and it would make
     * every translucent surface opaque.
     */
    private int alphaOf(int pixel) {
        return hasAlpha() ? (pixel & 0xFF000000) : 0xFF000000;
    }

    /**
     * Converts 0xAARRGGBB into a packed pixel value in this visual.
     */
    public int fromArgb(int argb) {
        if (!isTrueColor()) {
            return argb & 0xFF;
        }
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int bits = bitsPerRgbValue;
        return (scale(r, bits, redMask) | scale(g, bits, greenMask) | scale(b, bits, blueMask));
    }

    private static int extract(int pixel, int mask, int bits) {
        if (mask == 0) {
            return 0;
        }
        int shift = Integer.numberOfTrailingZeros(mask);
        int raw = (pixel & mask) >>> shift;
        int width = Integer.bitCount(mask);
        if (width == 8) {
            return raw;
        }
        // Scale an n-bit channel up to 8 bits with rounding so 5-bit 31 maps to 255.
        int max = (1 << width) - 1;
        return (raw * 255 + max / 2) / max;
    }

    private static int scale(int value8, int bits, int mask) {
        if (mask == 0) {
            return 0;
        }
        int shift = Integer.numberOfTrailingZeros(mask);
        int width = Integer.bitCount(mask);
        int max = (1 << width) - 1;
        int scaled = (value8 * max + 127) / 255;
        return scaled << shift;
    }

    /** Number of bits per pixel actually stored in an image of this visual. */
    public int bitsPerPixel() {
        return isTrueColor() ? bitsPerRgbValue * 3 : depth;
    }
}
