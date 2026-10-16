package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.resource.BitmapFont;
import cn.oyzh.fx.xserver.resource.XPixmap;

/**
 * A cursor: a small ARGB image plus a hotspot.
 *
 * <p>The cursor is drawn straight into the framebuffer at the pointer position, which keeps the
 * sprite handling identical for every display backend.
 */
public final class XCursor extends XResource {

    /** Standard X cursor shape, used when a window has no cursor of its own. */
    private static final int[] DEFAULT_CURSOR = {
            0b1000000000000000,
            0b1100000000000000,
            0b1110000000000000,
            0b1111000000000000,
            0b1111100000000000,
            0b1111110000000000,
            0b1111111000000000,
            0b1111111100000000,
            0b1111111110000000,
            0b1111111111000000,
            0b1111111111100000,
            0b1111110000000000,
            0b1100110000000000,
            0b1000011000000000,
            0b0000011000000000,
            0b0000000000000000,
    };

    /**
     * Handed out in creation order, and what XFixes reports as a cursor's {@code cursorSerial}.
     *
     * <p>It identifies the cursor rather than the change: a client that watches the cursor is told
     * which one is displayed, so moving back to a cursor it has already seen reports the same number
     * again. Measured against a reference server, whose serials go back down on the way back.
     */
    private static final java.util.concurrent.atomic.AtomicInteger NEXT_SERIAL =
            new java.util.concurrent.atomic.AtomicInteger(1);

    private int serial = NEXT_SERIAL.getAndIncrement();

    // Not final: XFIXES' ChangeCursor rewrites the destination cursor in place, and the two fields
    // that move are the image and the serial (see adopt).
    private int width;
    private int height;
    private int hotspotX;
    private int hotspotY;
    private int[] pixels;
    /**
     * Bumped whenever the image changes, so a cache that holds this cursor's pixels knows to read
     * them again. The display tick's sprite cache is the one that matters.
     */
    private int imageVersion;

    private final boolean recolorable;

    private int foreground;
    private int background;

    /**
     * The name a client gave this cursor with XFIXES' {@code SetCursorName}, as an atom, or 0 for
     * none. Cursors are not born named - this server has no name for the shapes it creates - and the
     * name is the *client's* to give, which is what other clients read back to tell one cursor from
     * another (measured against a reference: a name given is a name read back).
     */
    private int name;

    public int name() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    /** This cursor's XFixes serial, stable for the cursor's lifetime - until it is rewritten. */
    public int serial() {
        return serial;
    }

    /** @see #imageVersion */
    public int imageVersion() {
        return imageVersion;
    }

    /**
     * Takes on another cursor's image, hotspot and serial - XFIXES' {@code ChangeCursor}, where after
     * the call the destination shows the source's shape and reports the source's serial.
     *
     * <p>Rewritten in place rather than by rebinding the id, because that is what a reference server's
     * answers show: a window that was **already** showing the destination sees the new shape, so what
     * it holds was changed rather than replaced. Measured with {@code tools/ChangeCursorProbe}, which
     * also measured that **no cursor notification is raised** for a rewrite - and this matches that
     * for free: the cursor the pointer is showing is the same cursor afterwards, so the display tick
     * sees nothing change.
     */
    public void adopt(XCursor other) {
        this.name = other.name;
        this.width = other.width;
        this.height = other.height;
        this.hotspotX = other.hotspotX;
        this.hotspotY = other.hotspotY;
        this.pixels = other.pixels;
        this.serial = other.serial;
        this.imageVersion++;
    }

    public XCursor(int id, ClientConnection owner, int width, int height, int hotspotX,
                   int hotspotY, int[] pixels, boolean recolorable) {
        super(id, CLASS_CURSOR, owner);
        this.width = width;
        this.height = height;
        this.hotspotX = hotspotX;
        this.hotspotY = hotspotY;
        this.pixels = pixels;
        this.recolorable = recolorable;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int hotspotX() {
        return hotspotX;
    }

    public int hotspotY() {
        return hotspotY;
    }

    public int pixel(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return 0;
        }
        return pixels[y * width + x];
    }

    public boolean isRecolorable() {
        return recolorable;
    }

    public void setColors(int foreground, int background) {
        this.foreground = foreground;
        this.background = background;
    }

    public int foreground() {
        return foreground;
    }

    public int background() {
        return background;
    }

    /**
     * Builds a cursor from a source pixmap and a mask pixmap, as {@code CreateCursor} does.
     * Source bits are drawn black, mask-only bits white, matching the core protocol.
     */
    public static XCursor fromBitmaps(int id, ClientConnection owner, XPixmap source, XPixmap mask,
                                      int sourceX, int sourceY, int maskX, int maskY, int width,
                                      int height, int hotspotX, int hotspotY) {
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int src = source == null ? 0 : luminance(source.pixel(sourceX + x, sourceY + y));
                int msk = mask == null ? 0 : luminance(mask.pixel(maskX + x, maskY + y));
                pixels[y * width + x] = cursorPixel(src != 0, msk != 0);
            }
        }
        return new XCursor(id, owner, width, height, hotspotX, hotspotY, pixels, false);
    }

    /**
     * Builds a cursor from a font glyph, as {@code CreateGlyphCursor} does.
     */
    public static XCursor fromGlyph(int id, ClientConnection owner, XFont font, int sourceChar,
                                    int maskChar, int foreground, int background) {
        // A null font means the cursor font: that is what the callers with no font in hand pass,
        // and it is the font these shapes belong to.
        CursorFont.Glyph shape = font == null || isCursorFont(font)
                ? CursorFont.glyph(sourceChar) : null;
        if (shape != null) {
            // One of the cursor font's own shapes: the client asked for an arrow, an I-beam, a
            // resize handle. Its bitmap is the shape and the character after it is its mask, which
            // is what XCreateGlyphCursor composes: the source in the foreground over the mask in
            // the background. The font argument is not needed for this path at all - the glyphs are
            // in the table, because a client that asks for XC_left_ptr means the arrow that every X
            // server draws, not whatever this server's stand-in font has at that character.
            int[] pixels = new int[shape.width() * shape.height()];
            for (int y = 0; y < shape.height(); y++) {
                for (int x = 0; x < shape.width(); x++) {
                    boolean source = bit(shape.source(), shape.width(), x, y);
                    boolean mask = bit(shape.mask(), shape.width(), x, y);
                    pixels[y * shape.width() + x] = source && mask ? foreground
                            : mask ? background : 0;
                }
            }
            XCursor cursor = new XCursor(id, owner, shape.width(), shape.height(),
                    shape.hotspotX(), shape.hotspotY(), pixels, true);
            cursor.setColors(foreground, background);
            return cursor;
        }
        int cellWidth = Math.max(1, font.maxWidth());
        int cellHeight = Math.max(1, font.ascent() + font.descent());
        BitmapFont.Glyph ink = font.bitmapGlyph(sourceChar);
        // No mask font means the source glyph masks itself, which is what X does with mask_font None.
        BitmapFont.Glyph silhouette = maskChar == 0 ? ink : font.bitmapGlyph(maskChar);
        // The font's own table decides which path this is, not whether these two glyphs happen to
        // have ink: a space has none, and a cursor made of a space is an empty cursor rather than the
        // filled cell the approximation would draw (a reference server answers with nothing at all
        // there - measured).
        if (font != null && font.bitmap() != null) {
            // A font with real glyph bitmaps - the built-in tables, or a font read from the font
            // path - composes the cursor the way X does: **the mask glyph's ink is the silhouette**,
            // the source glyph's ink is drawn in the foreground inside it, and the rest of the
            // silhouette takes the background colour.
            //
            // Measured against a reference server 2026-09-26 with tools/GlyphCursorProbe: for
            // `fixed`'s 'A' the cell is the font's own 6x13, the hotspot is the glyph's origin
            // (0,11), and the drawn shape follows the *mask* character - 'A' with 'B' as its mask
            // came back as a B, and with a space came back empty. The one
            // thing the probe cannot check is the two colours: both servers answer the image as
            // opaque black whatever the request named, so they follow the spec and the cursor font's
            // own path above, where the shape is the source and the mask is what fills behind it.
            int[] pixels = new int[cellWidth * cellHeight];
            for (int y = 0; y < cellHeight; y++) {
                for (int x = 0; x < cellWidth; x++) {
                    boolean inMask = inkAt(silhouette, font, x, y);
                    boolean inSource = inkAt(ink, font, x, y);
                    pixels[y * cellWidth + x] = inMask && inSource ? foreground
                            : inMask ? background : 0;
                }
            }
            // The hotspot is the glyph's **origin**: the pen position in the cell, which is the
            // left edge and the font's ascent down - *not* the ink's left bearing. Measured across
            // seven glyphs, four of them with a bearing of their own ('!' 2, '"' 1, ',' 1, '(' 1):
            // a reference server answers 0,11 for every one of them, and the bearing would have put
            // the hotspot inside the glyph.
            XCursor cursor = new XCursor(id, owner, cellWidth, cellHeight, 0, font.ascent(),
                    pixels, true);
            cursor.setColors(foreground, background);
            return cursor;
        }
        // Nothing to compose: a font with no glyph table at all gets the old approximation, a filled
        // cell, which is what this path did for every font before the tables existed.
        int width = cellWidth;
        int height = cellHeight;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean inSource = x < font.charWidth(sourceChar);
                boolean inMask = maskChar == 0 || x < font.charWidth(maskChar);
                if (inSource && inMask) {
                    pixels[y * width + x] = foreground;
                } else if (inMask) {
                    pixels[y * width + x] = background;
                } else {
                    pixels[y * width + x] = 0;
                }
            }
        }
        XCursor cursor = new XCursor(id, owner, width, height, 0, 0, pixels, true);
        cursor.setColors(foreground, background);
        return cursor;
    }

    /**
     * Whether the font a glyph cursor is asked for is the cursor font itself - the one font whose
     * codes are a *shape table* rather than characters.
     *
     * <p>Measured against a reference server (2026-09-26, while A8's ChangeCursor was being done):
     * {@code CreateGlyphCursor} against {@code fixed} with character 66 answers **that font's** 'B'
     * there and one of the cursor font's shapes here, because this server consulted the embedded
     * table by code whatever font was named. The two agree for the cursor font's own codes and
     * nowhere else, so the table is used only for the font it belongs to.
     */
    private static boolean isCursorFont(XFont font) {
        String name = font == null ? null : font.fontName();
        return name != null && name.toLowerCase(java.util.Locale.ROOT).contains("cursor");
    }

    /**
     * Whether a glyph's ink covers a point of the font's cell.
     *
     * <p>A glyph's bits are one byte a pixel, positioned from the pen and the baseline (the record
     * says so, and the rasteriser reads them the same way), and the cursor's cell has the baseline
     * {@code ascent} of the way down - so the cell's row for a glyph row is
     * {@code originY + row + ascent}.
     */
    private static boolean inkAt(BitmapFont.Glyph glyph, XFont font, int x, int y) {
        if (glyph == null) {
            return false;
        }
        int mx = x - glyph.originX();
        int my = y - (glyph.originY() + font.ascent());
        if (mx < 0 || my < 0 || mx >= glyph.width() || my >= glyph.height()) {
            return false;
        }
        return glyph.bits()[my * glyph.width() + mx] != 0;
    }

    /** One bit of a packed cursor bitmap: a row is padded to whole bytes, most significant bit first. */
    private static boolean bit(byte[] rows, int width, int x, int y) {
        int stride = (width + 7) / 8;
        int index = y * stride + (x >> 3);
        if (index < 0 || index >= rows.length) {
            return false;
        }
        return ((rows[index] >> (7 - (x & 7))) & 1) != 0;
    }

    private static int cursorPixel(boolean source, boolean mask) {
        if (source && mask) {
            return 0xFF000000;
        }
        if (mask) {
            return 0xFFFFFFFF;
        }
        return 0;
    }

    private static int luminance(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (r * 299 + g * 587 + b * 114) / 1000;
    }

    /** The cursor every window and the root start with when nothing else is set. */
    public static XCursor defaultCursor(int id, ClientConnection owner) {
        int size = 16;
        int[] pixels = new int[size * size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean set = (DEFAULT_CURSOR[y] & (1 << (15 - x))) != 0;
                pixels[y * size + x] = set ? 0xFF000000 : 0;
            }
        }
        return new XCursor(id, owner, size, size, 0, 0, pixels, false);
    }

    @Override
    public String typeName() {
        return "cursor";
    }
}
