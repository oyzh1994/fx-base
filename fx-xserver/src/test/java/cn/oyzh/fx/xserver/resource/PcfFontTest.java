package cn.oyzh.fx.xserver.resource;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A font file read from a font path - the half of D3 that the generated tables cannot cover.
 *
 * <p>The font is `8x13-ISO8859-1` as it ships with X, copied into the test resources because it is
 * what a real font path holds and because its own COPYRIGHT property says "Public domain font.
 * Share and enjoy". The numbers below are the ones {@code tools/FontProbe} read off the reference
 * server for the same file, so what this pins is agreement with a reference rather than with this
 * reader's own idea of the format.
 */
class PcfFontTest {

    private static BitmapFont.Font read() throws Exception {
        try (InputStream in = PcfFontTest.class.getResourceAsStream("/fonts/8x13-ISO8859-1.pcf.gz")) {
            assertNotNull(in, "the test font is missing from the resources");
            return PcfFont.read(in, "8x13");
        }
    }

    /** The header, the bounds and the character range, as the reference reports them. */
    @Test
    void theFontHeaderIsTheFiles() throws Exception {
        BitmapFont.Font font = read();
        assertEquals(11, font.ascent(), "the font's ascent");
        assertEquals(2, font.descent(), "and its descent");
        assertEquals(0, font.minChar(), "its first character code");
        assertEquals(255, font.maxChar(), "and its last");
        assertEquals(0, font.defaultChar(), "and the code it draws for a character it has not got");
        // The bounds are the extremes *over the glyphs*, not the header's own numbers: 8x13's header
        // says its widest left bearing is zero where its glyphs' is three, and its minimum width is
        // eight where a code it does not have reports zero.
        assertEquals(new BitmapFont.Metric(0, 0, 8, -1, -10, 0), font.minBounds(), "min-bounds");
        assertEquals(new BitmapFont.Metric(3, 8, 8, 11, 2, 0), font.maxBounds(), "max-bounds");
    }

    /**
     * Every character's metrics come from the file's <em>ink</em> metrics, which is the difference
     * between a client laying text out right and not: the plain metrics table of this font says every
     * glyph is the whole cell, and the ink table says what each one covers.
     */
    @Test
    void eachCharacterHasItsOwnMetrics() throws Exception {
        BitmapFont.Font font = read();
        assertEquals(new BitmapFont.Metric(1, 7, 8, 9, 0, 0), font.chars()['A'], "'A'");
        assertEquals(new BitmapFont.Metric(1, 7, 8, 9, 0, 0), font.chars()['H'], "'H'");
        assertEquals(new BitmapFont.Metric(0, 0, 8, 0, 0, 0), font.chars()[' '], "the space");
        // A code the font's encoding table does not map answers with nothing at all, which is what a
        // reference server reports for 0x7f and everything above it in this font.
        assertEquals(new BitmapFont.Metric(0, 0, 0, 0, 0, 0), font.chars()[0x7F], "0x7f");
    }

    /**
     * And the glyphs: the ink of 'H' is the shape the reference draws, five pixels wide and nine
     * tall, not a filled cell.
     */
    @Test
    void theGlyphsComeFromTheFile() throws Exception {
        BitmapFont.Font font = read();
        BitmapFont.Glyph glyph = font.glyph('H');
        assertNotNull(glyph, "'H' has no glyph");
        assertEquals(6, glyph.width(), "the ink's width");
        assertEquals(9, glyph.height(), "and its height");
        assertEquals(1, glyph.originX(), "positioned from the origin by its left bearing");
        assertEquals(-9, glyph.originY(), "and above the baseline by its ascent");
        // The shape is the file's, so the test is that it is a *shape*: a filled cell - which is
        // what a font with no outlines used to draw - has no transparent pixel in it at all, and the
        // reference's 'H' is mostly transparent.
        int ink = 0;
        for (byte bit : glyph.bits()) {
            if (bit != 0) {
                ink++;
            }
        }
        assertTrue(ink > 0, "'H' has no ink");
        assertTrue(ink < glyph.width() * glyph.height(), "'H' is a filled cell");
        assertTrue(font.glyph(' ') == null, "and a space has no ink at all");
    }

    /** A file that is not a PCF file is refused rather than answered with an invented font. */
    @Test
    void aFileThatIsNotAFontIsRefused() {
        byte[] notAFont = "this is not a font file".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> PcfFont.read(notAFont, "junk"));
    }
}
