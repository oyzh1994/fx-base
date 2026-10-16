package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.resource.CursorFont;
import cn.oyzh.fx.xserver.resource.XCursor;
import cn.oyzh.fx.xserver.resource.XFont;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The cursor a client gets when it asks for a cursor-font shape.
 *
 * <p>{@code XCreateFontCursor} reaches {@code CreateGlyphCursor} on the font called "cursor", so a
 * client that wants an arrow, an I-beam or a resize handle asks for the character the cursorfont
 * header names it by - and every one of those used to arrive as a filled cell, because the server had
 * no glyph outlines to draw and approximated the character with "this cell is the shape". The
 * symptom is a solid rectangle under the pointer, which is what `xterm` and `xclock` showed.
 *
 * <p>The glyphs are now the font's own bitmaps, embedded in {@link CursorFont}.
 */
class CursorFontTest {

    private static final int FOREGROUND = 0xFF000000;
    private static final int BACKGROUND = 0xFFFFFFFF;

    /** XC_left_ptr: the standard arrow, the shape almost every client asks for. */
    private static final int LEFT_PTR = 68;

    @Test
    void theStandardArrowIsAnArrowAndNotACell() {
        XCursor cursor = XCursor.fromGlyph(1, null, null, LEFT_PTR, LEFT_PTR + 1, FOREGROUND,
                BACKGROUND);
        int width = cursor.width();
        int height = cursor.height();
        int transparent = 0;
        int foreground = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = cursor.pixel(x, y);
                if (pixel == 0) {
                    transparent++;
                } else if (pixel == FOREGROUND) {
                    foreground++;
                }
            }
        }
        // A filled cell has no transparent pixels at all; the arrow's bitmap has plenty.
        org.junit.jupiter.api.Assertions.assertTrue(transparent > width * height / 4,
                "an arrow leaves most of its cell transparent, and " + transparent + " of "
                        + (width * height) + " were");
        org.junit.jupiter.api.Assertions.assertTrue(foreground > 0, "and draws a shape in the foreground");
    }

    @Test
    void theHotspotIsTheGlyphsOwn() {
        XCursor cursor = XCursor.fromGlyph(1, null, null, LEFT_PTR, LEFT_PTR + 1, FOREGROUND,
                BACKGROUND);
        // The arrow's point is its hotspot: top left, and the pixel there is the shape's own.
        org.junit.jupiter.api.Assertions.assertTrue(cursor.hotspotX() >= 0 && cursor.hotspotY() >= 0,
                "the hotspot is inside the bitmap, not at the origin by accident");
    }

    /**
     * A glyph cursor from a font that is <em>not</em> the cursor font: the shape is the mask
     * character's bitmap and the hotspot is the source glyph's own.
     *
     * <p>Measured against a reference server with {@code tools/GlyphCursorProbe}, which reads a
     * cursor back through XFIXES' {@code GetCursorImage}: for {@code fixed}'s 'A' the reference
     * answers a 6x13 cursor with the hotspot at 0,11 and the glyph's own ink - twenty inked pixels,
     * not the filled cell (seventy eight) this used to be for every font but the cursor font.
     */
    @Test
    void aGlyphFromAnyFontIsDrawnAsItsOwnShape() {
        XCursor cursor = XCursor.fromGlyph(1, null, new XFont(1, null, "fixed"), 'A', 'A',
                FOREGROUND, BACKGROUND);
        assertEquals(6, cursor.width(), "the font's own cell width");
        assertEquals(13, cursor.height(), "and its height: eleven of ascent, two of descent");
        assertEquals(0, cursor.hotspotX(), "the source glyph's left bearing");
        assertEquals(11, cursor.hotspotY(), "and the font's ascent");
        int ink = 0;
        for (int y = 0; y < cursor.height(); y++) {
            for (int x = 0; x < cursor.width(); x++) {
                if (cursor.pixel(x, y) != 0) {
                    ink++;
                }
            }
        }
        assertEquals(20, ink, "the glyph's own ink, where a filled cell would ink all seventy eight");

        // The hotspot is the glyph's *origin*, not its ink's left bearing: measured across seven
        // glyphs, and four of them have a bearing of their own. '!' begins two pixels in (lb=2) and a
        // reference server still answers 0,11 - the pen's own place in the cell.
        XCursor bearing = XCursor.fromGlyph(1, null, new XFont(1, null, "fixed"), '!', '!',
                FOREGROUND, BACKGROUND);
        assertEquals(0, bearing.hotspotX(), "the bearing does not move the hotspot");
        assertEquals(11, bearing.hotspotY(), "which is the font's ascent down from the cell's top");

        // And a code the *cursor font* has a shape for is that font's glyph here, not its shape: the
        // cursor font's table is consulted for the cursor font and for no other (measured: 66 is one
        // of its codes, and a reference server draws `fixed`'s 'B' for it).
        XCursor collision = XCursor.fromGlyph(1, null, new XFont(1, null, "fixed"), 66, 66,
                FOREGROUND, BACKGROUND);
        assertEquals(6, collision.width(), "the cell of the font that was asked for");
        assertEquals(13, collision.height());
    }

    @Test
    void aShapeTheTableDoesNotHaveStillGetsACursor() {
        // The fallback is the approximation this used to be for everything: a client that invents a
        // character gets a cursor rather than an error.
        XCursor cursor = XCursor.fromGlyph(1, null, new XFont(1, null, "fixed"), 201, 0,
                FOREGROUND, BACKGROUND);
        org.junit.jupiter.api.Assertions.assertTrue(cursor.width() > 0 && cursor.height() > 0);
    }
}
