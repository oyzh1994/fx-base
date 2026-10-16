package cn.oyzh.fx.xserver.draw;

import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.draw.Rasterizer;
import cn.oyzh.fx.xserver.resource.XPixmap;

/**
 * Converts the three image formats of the core protocol to and from the 0xAARRGGBB pixels the
 * server keeps in a {@link PixelStore}.
 *
 * <p>Everything here is byte order dependent. A client packs pixels and bits in the byte order of
 * the machine it runs on, so the caller passes the connection's {@code bigEndian} flag and this
 * class reads and writes the wire form in that order. Bits inside a scanline follow the same rule:
 * little endian clients send {@code LSBFirst} bitmaps, big endian clients {@code MSBFirst}.
 *
 * <p>Writes go through {@link Rasterizer} rather than straight to the store, so the GC function,
 * plane mask and clip of {@code PutImage} have exactly the semantics they have for every other
 * drawing request.
 *
 * <h2>Formats</h2>
 * <ul>
 *   <li>{@code ZPixmap} packs whole pixels, each scanline padded to 32 bits.</li>
 *   <li>{@code XYPixmap} sends one bitmap per plane, most significant plane first.</li>
 *   <li>{@code XYBitmap} sends a single plane, used for depth 1 drawables.</li>
 * </ul>
 *
 * <p>Depth 24 images use 32 bits per pixel on the wire, which is the format a 24 bit screen
 * advertises and what every modern client sends. A client that packs depth 24 into 3 bytes is
 * still understood by {@code putImage} when the request data is exactly that long, and
 * {@code getImage} emits the packed form when no visual is supplied to describe the drawable.
 */
public final class ImageCodec {

    /** Formats from the X protocol. */
    public static final int XY_BITMAP = 0;
    public static final int XYPIXMAP = 1;
    public static final int ZPIXMAP = 2;

    /** X11 pads every scanline of every image format to a 32 bit boundary. */
    private static final int SCANLINE_PAD = 32;

    /** Upper bound on the pixel count of a single image, to keep a bogus request harmless. */
    private static final long MAX_IMAGE_PIXELS = 64L * 1024 * 1024;

    /** Upper bound on the byte size of a decoded or encoded image. */
    private static final long MAX_IMAGE_BYTES = 256L * 1024 * 1024;

    private ImageCodec() {
    }

    // ---------------------------------------------------------------- PutImage

    /**
     * Decodes raw request bytes into pixels and writes them to the store with the given GX
     * function.
     *
     * @param format {@link #XY_BITMAP}, {@link #XYPIXMAP} or {@link #ZPIXMAP}
     * @param leftPad bits ignored at the start of each scanline of the XY formats
     * @param depth depth of the drawable the image is going to, which fixes the wire layout
     * @param offset first byte of the image inside {@code data}
     * @param bigEndian true when the client is a big endian machine
     */
    public static void putImage(PixelStore store, Rasterizer out, int x, int y, int width, int height,
            int format, int leftPad, int depth, byte[] data, int offset, boolean bigEndian) {
        if (store == null || out == null || data == null || width <= 0 || height <= 0 || depth <= 0
                || offset < 0 || offset > data.length) {
            return;
        }
        if ((long) width * height > MAX_IMAGE_PIXELS) {
            return;
        }
        VisualInfo visual = visualOf(store);
        // The *caller's* rasterizer, not a new one: it carries the clip (a put into a window that is
        // partly covered must not paint over what covers it) and it is where the drawn region is
        // recorded, which is what the display tick presents. Building one here - which this did -
        // lost both, and a put image was drawn over the covering window and marked as nothing.
        // Only the part of the image that lands on the drawable is worth decoding.
        int colStart = Math.max(0, -x);
        int colEnd = Math.min(width, store.width() - x);
        int rowStart = Math.max(0, -y);
        int rowEnd = Math.min(height, store.height() - y);
        if (colStart >= colEnd || rowStart >= rowEnd) {
            return;
        }
        switch (format) {
            case XY_BITMAP -> putBitmap(out, data, offset, width, height, leftPad, x, y, colStart,
                    colEnd, rowStart, rowEnd, depth, visual, bigEndian);
            case XYPIXMAP -> putXY(out, data, offset, width, height, leftPad, x, y, colStart, colEnd,
                    rowStart, rowEnd, depth, visual, bigEndian);
            default -> putZ(out, data, offset, width, height, x, y, colStart, colEnd, rowStart,
                    rowEnd, depth, visual, bigEndian);
        }
    }

    private static void putZ(Rasterizer out, byte[] data, int offset, int width, int height,
            int x, int y, int colStart, int colEnd, int rowStart, int rowEnd, int depth,
            VisualInfo visual, boolean bigEndian) {
        int bpp = bitsPerPixel(depth, visual);
        int packed24 = rasterBytes(width, height, 24);
        int packed32 = rasterBytes(width, height, bitsPerPixel(depth, visual));
        if (depth == 24 && packed24 != packed32 && data.length - offset == packed24) {
            // A client that packed depth 24 into three bytes, which happens on 24 bpp screens. The
            // two layouts only differ for some widths, so an exact length is what tells them apart.
            bpp = 24;
        }
        int bytesPerPixel = (bpp + 7) / 8;
        int scanline = scanlineBytes(width, bpp);
        long rowBytes = ((long) colEnd * bpp + 7) / 8;
        // **The common case - one pixel to four bytes, which is every 24/32 bit screen - is a row at
        // a time.** Building the row and handing it to the rasterizer's bulk write skips the
        // per-pixel clip test and the per-pixel drawn-region bookkeeping that made a forwarded
        // client's image upload the server's hottest frame; other formats keep the pixel loop, which
        // is still the definition of what a put means.
        if (bytesPerPixel == 4) {
            int span = colEnd - colStart;
            int[] row = new int[span];
            for (int r = rowStart; r < rowEnd; r++) {
                long base = (long) offset + (long) r * scanline;
                if (base + rowBytes > data.length) {
                    return;
                }
                int at = (int) base + colStart * 4;
                for (int i = 0; i < span; i++) {
                    row[i] = argb(readPixel(data, at + i * 4, 4, bigEndian), depth, visual);
                }
                out.writeRun(x + colStart, y + r, row, 0, span);
            }
            return;
        }
        for (int row = rowStart; row < rowEnd; row++) {
            long base = (long) offset + (long) row * scanline;
            if (base + rowBytes > data.length) {
                return;
            }
            int py = y + row;
            for (int col = colStart; col < colEnd; col++) {
                int value = bpp < 8
                        ? readBits(data, (int) base, col * bpp, bpp, bigEndian)
                        : readPixel(data, (int) (base + (long) col * bytesPerPixel), bytesPerPixel,
                                bigEndian);
                out.writePixel(x + col, py, argb(value, depth, visual));
            }
        }
    }

    private static void putXY(Rasterizer out, byte[] data, int offset, int width, int height,
            int leftPad, int x, int y, int colStart, int colEnd, int rowStart, int rowEnd,
            int depth, VisualInfo visual, boolean bigEndian) {
        int scanline = scanlineBytes(width, 1);
        int[] values = new int[width];
        for (int row = rowStart; row < rowEnd; row++) {
            long planeBase = (long) offset + (long) row * scanline;
            if (planeBase + (long) depth * scanline * height > data.length) {
                return;
            }
            java.util.Arrays.fill(values, colStart, colEnd, 0);
            for (int plane = 0; plane < depth; plane++) {
                // Planes arrive from the most significant plane down.
                int bit = 1 << (depth - 1 - plane);
                long base = planeBase + (long) plane * scanline * height;
                for (int col = colStart; col < colEnd; col++) {
                    if (readBit(data, (int) base, leftPad + col, bigEndian)) {
                        values[col] |= bit;
                    }
                }
            }
            int py = y + row;
            for (int col = colStart; col < colEnd; col++) {
                out.writePixel(x + col, py, argb(values[col], depth, visual));
            }
        }
    }

    private static void putBitmap(Rasterizer out, byte[] data, int offset, int width, int height,
            int leftPad, int x, int y, int colStart, int colEnd, int rowStart, int rowEnd,
            int depth, VisualInfo visual, boolean bigEndian) {
        int scanline = scanlineBytes(width, 1);
        for (int row = rowStart; row < rowEnd; row++) {
            long base = (long) offset + (long) row * scanline;
            if (base + scanline > data.length) {
                return;
            }
            int py = y + row;
            for (int col = colStart; col < colEnd; col++) {
                boolean set = readBit(data, (int) base, leftPad + col, bigEndian);
                // A one bit drawable stores the bit itself, which is what its two entry colormap
                // means. Into anything deeper the bits become a stipple painted in the GC's
                // foreground and background, which is how clients draw monochrome icons and
                // cursor masks into colour windows.
                if (depth == 1) {
                    out.writePixel(x + col, py, argb(set ? 1 : 0, depth, visual));
                } else {
                    out.writePixel(x + col, py, set ? out.foreground() : out.background());
                }
            }
        }
    }

    // ---------------------------------------------------------------- GetImage

    /**
     * Encodes an image for {@code GetImage}.
     *
     * @param planeMask plane selection; a plane whose bit is clear is returned as zeros but still
     *     occupies its place in the reply, exactly as the protocol requires
     * @param depth depth of the drawable, which fixes the wire layout
     * @param visual visual of the drawable, or null to assume a true colour default for the depth
     * @param bigEndian true when the client is a big endian machine
     */
    public static byte[] getImage(PixelStore store, int x, int y, int width, int height,
            int format, int planeMask, int depth, VisualInfo visual, boolean bigEndian) {
        if (store == null || width <= 0 || height <= 0 || depth <= 0
                || (long) width * height > MAX_IMAGE_PIXELS) {
            return new byte[0];
        }
        return switch (format) {
            case XY_BITMAP -> getBitmap(store, x, y, width, height, planeMask, depth, visual,
                    bigEndian);
            case XYPIXMAP -> getXY(store, x, y, width, height, planeMask, depth, visual, bigEndian);
            default -> getZ(store, x, y, width, height, planeMask, depth, visual, bigEndian);
        };
    }

    private static byte[] getZ(PixelStore store, int x, int y, int width, int height, int planeMask,
            int depth, VisualInfo visual, boolean bigEndian) {
        int bpp = depth == 24 && visual == null ? 24 : bitsPerPixel(depth, visual);
        int bytesPerPixel = (bpp + 7) / 8;
        int scanline = scanlineBytes(width, bpp);
        byte[] out = allocate((long) scanline * height);
        if (out == null) {
            return new byte[0];
        }
        for (int row = 0; row < height; row++) {
            int base = row * scanline;
            // The width of one row in bytes, used to stay inside the buffer even if the caller
            // asks for a rectangle the drawable cannot supply.
            int rowBytes = bpp < 8 ? (width * bpp + 7) / 8 : width * bytesPerPixel;
            if (base + rowBytes > out.length) {
                break;
            }
            for (int col = 0; col < width; col++) {
                int value = packed(store.pixel(x + col, y + row), depth, visual) & planeMask;
                if (bpp < 8) {
                    // A depth one pixel is a single bit, not a byte. Writing one byte per pixel
                    // overruns the buffer, which the row length above would otherwise allow.
                    writeBits(out, base, col * bpp, bpp, value, bigEndian);
                } else {
                    writePixel(out, base + col * bytesPerPixel, value, bytesPerPixel, bigEndian);
                }
            }
        }
        return out;
    }

    private static byte[] getXY(PixelStore store, int x, int y, int width, int height, int planeMask,
            int depth, VisualInfo visual, boolean bigEndian) {
        int scanline = scanlineBytes(width, 1);
        long planes = (long) scanline * depth * height;
        byte[] out = allocate(planes);
        if (out == null) {
            return new byte[0];
        }
        for (int row = 0; row < height; row++) {
            int planeBase = row * scanline;
            for (int col = 0; col < width; col++) {
                int value = packed(store.pixel(x + col, y + row), depth, visual);
                for (int plane = 0; plane < depth; plane++) {
                    // The first plane of the reply is the most significant bit of the pixel.
                    int bit = depth - 1 - plane;
                    if (((planeMask >>> bit) & 1) != 0 && ((value >>> bit) & 1) != 0) {
                        setBit(out, (int) (planeBase + (long) plane * scanline * height), col,
                                bigEndian);
                    }
                }
            }
        }
        return out;
    }

    private static byte[] getBitmap(PixelStore store, int x, int y, int width, int height,
            int planeMask, int depth, VisualInfo visual, boolean bigEndian) {
        int scanline = scanlineBytes(width, 1);
        byte[] out = allocate((long) scanline * height);
        if (out == null) {
            return new byte[0];
        }
        for (int row = 0; row < height; row++) {
            int base = row * scanline;
            for (int col = 0; col < width; col++) {
                int value = packed(store.pixel(x + col, y + row), depth, visual) & planeMask;
                if ((value & 1) != 0) {
                    setBit(out, base, col, bigEndian);
                }
            }
        }
        return out;
    }

    // ---------------------------------------------------------------- helpers

    /** Bytes one scanline of {@code width} pixels occupies, padded to 32 bits. */
    private static int scanlineBytes(int width, int bitsPerPixel) {
        long bits = (long) width * bitsPerPixel;
        long bytes = (bits + SCANLINE_PAD - 1) / SCANLINE_PAD * (SCANLINE_PAD / 8);
        return (int) Math.max(0, bytes);
    }

    private static int rasterBytes(int width, int height, int bitsPerPixel) {
        return scanlineBytes(width, bitsPerPixel) * height;
    }

    /** Allocates the reply buffer, or null when the request asks for something unreasonable. */
    private static byte[] allocate(long size) {
        if (size <= 0 || size > MAX_IMAGE_BYTES) {
            return null;
        }
        return new byte[(int) size];
    }

    /**
     * Bits per pixel used on the wire for a drawable of this depth. Depth 24 is stored as 32 bits
     * because that is the format its screen advertises; the visual only refines which bits carry
     * which channel.
     */
    private static int bitsPerPixel(int depth, VisualInfo visual) {
        return switch (depth) {
            case 1 -> 1;
            case 8 -> 8;
            case 16 -> 16;
            case 24, 32 -> 32;
            default -> visual != null && visual.bitsPerPixel() > depth ? visual.bitsPerPixel()
                    : (depth + 7) / 8 * 8;
        };
    }

    /** Reads one packed pixel in the client's byte order. */
    private static int readPixel(byte[] data, int at, int bytes, boolean bigEndian) {
        int value = 0;
        if (bigEndian) {
            for (int i = 0; i < bytes; i++) {
                value = (value << 8) | (data[at + i] & 0xFF);
            }
        } else {
            for (int i = bytes - 1; i >= 0; i--) {
                value = (value << 8) | (data[at + i] & 0xFF);
            }
        }
        return value;
    }

    private static void writePixel(byte[] out, int at, int value, int bytes, boolean bigEndian) {
        if (bigEndian) {
            for (int i = 0; i < bytes; i++) {
                out[at + i] = (byte) (value >>> (8 * (bytes - 1 - i)));
            }
        } else {
            for (int i = 0; i < bytes; i++) {
                out[at + i] = (byte) (value >>> (8 * i));
            }
        }
    }

    /**
     * Reads {@code count} bits starting at {@code bit}, the layout {@code ZPixmap} uses when a
     * pixel is narrower than a byte, as it is for a depth one drawable.
     */
    private static int readBits(byte[] data, int base, int bit, int count, boolean bigEndian) {
        int value = 0;
        for (int i = 0; i < count; i++) {
            int set = readBit(data, base, bit + i, bigEndian) ? 1 : 0;
            if (bigEndian) {
                // MSBFirst sends the most significant bit of the pixel first.
                value = (value << 1) | set;
            } else {
                value |= set << i;
            }
        }
        return value;
    }

    private static boolean readBit(byte[] data, int base, int bit, boolean bigEndian) {
        int at = base + (bit >> 3);
        if (at < 0 || at >= data.length) {
            return false;
        }
        int mask = bigEndian ? 0x80 >>> (bit & 7) : 1 << (bit & 7);
        return (data[at] & mask) != 0;
    }

    /** Writes {@code count} bits of {@code value}, the inverse of {@link #readBits}. */
    private static void writeBits(byte[] out, int base, int bit, int count, int value,
            boolean bigEndian) {
        for (int i = 0; i < count; i++) {
            int set = bigEndian ? (value >>> (count - 1 - i)) & 1 : (value >>> i) & 1;
            if (set != 0) {
                setBit(out, base, bit + i, bigEndian);
            }
        }
    }

    private static void setBit(byte[] out, int base, int bit, boolean bigEndian) {
        int at = base + (bit >> 3);
        if (at < 0 || at >= out.length) {
            return;
        }
        out[at] |= (byte) (bigEndian ? 0x80 >>> (bit & 7) : 1 << (bit & 7));
    }

    /**
     * Converts a wire pixel value to the 0xAARRGGBB the stores use.
     *
     * <p>A depth 8 drawable is kept as the byte the client sent, in the low eight bits, whether or
     * not it has a visual. Eight bit pixels are mostly not colours at all - a toolkit rasterises its
     * antialiased shapes and glyphs into a depth 8 pixmap whose bytes <em>are</em> coverage - and
     * reading that byte as a colour through the visual destroys six of its eight bits: this server
     * exposes a depth 8 visual of the usual 3-3-2 layout, so each byte came back as the blue channel
     * of its own expansion, which is coverage modulo four. Measured against termora, whose icons then
     * arrived quantised to four levels - the toolbar drew as flat, bold shapes where a real server
     * shows smooth edges - and the same loss hit every antialiased glyph drawn beside it. A drawable
     * with a colour visual still shows the right colours, because the picture's format carries the
     * same masks and decodes the byte on the way out.
     */
    private static int argb(int value, int depth, VisualInfo visual) {
        if (depth == 1) {
            // A one bit drawable has two pixel values, which X11 names black and white.
            return (value & 1) != 0 ? 0xFFFFFFFF : 0xFF000000;
        }
        if (depth == 8) {
            return 0xFF000000 | (value & 0xFF);
        }
        if (visual != null) {
            return visual.toArgb(value);
        }
        return switch (depth) {
            case 16 -> expand565(value);
            // A depth 32 drawable with no visual is ARGB32 by convention here, and the top byte of
            // the wire pixel is its alpha channel; keeping it is what makes an uploaded cursor image
            // translucent instead of a solid block.
            case 32 -> value;
            default -> 0xFF000000 | (value & 0xFFFFFF);
        };
    }

    /** Converts 0xAARRGGBB to a wire pixel value for a drawable of this depth. */
    private static int packed(int argb, int depth, VisualInfo visual) {
        if (depth == 1) {
            return (argb & 0x00FFFFFF) != 0 ? 1 : 0;
        }
        if (depth == 8) {
            // Mirrors the read path: what the client put in comes back out, so an antialiased mask
            // survives a GetImage of the pixmap it was rasterised into.
            return argb & 0xFF;
        }
        if (visual != null) {
            int value = visual.fromArgb(argb);
            return depth == 32 ? value | (argb & 0xFF000000) : value;
        }
        return switch (depth) {
            case 16 -> ((argb >> 8) & 0xF800) | ((argb >> 5) & 0x07E0) | ((argb >> 3) & 0x001F);
            // Mirrors the read path: a depth 32 drawable keeps its alpha, so a client can read a
            // cursor image or a translucent surface back the way it wrote it.
            case 32 -> argb;
            default -> argb & 0xFFFFFF;
        };
    }

    private static int expand565(int value) {
        int r = (value >> 11) & 0x1F;
        int g = (value >> 5) & 0x3F;
        int b = value & 0x1F;
        int r8 = (r * 255 + 15) / 31;
        int g8 = (g * 255 + 31) / 63;
        int b8 = (b * 255 + 15) / 31;
        return 0xFF000000 | (r8 << 16) | (g8 << 8) | b8;
    }

    /** The visual of the drawable, when the store knows one. */
    private static VisualInfo visualOf(PixelStore store) {
        return store instanceof XPixmap pixmap ? pixmap.visual() : null;
    }
}
