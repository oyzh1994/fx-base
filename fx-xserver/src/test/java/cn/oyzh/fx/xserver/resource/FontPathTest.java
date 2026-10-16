package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.resource.BitmapFont;
import cn.oyzh.fx.xserver.resource.FontPath;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The resolution a font path does: an alias to a full name, a full name to a file, and the file read.
 *
 * <p>`fonts.dir` is the standard's own table - a count, then a file name and the full name of the
 * font in it - and the thing this pins is that the *count line* is the only line to skip: a font
 * file's name usually begins with a digit (`8x13-ISO8859-1.pcf.gz` is one), so a reader that skips
 * "lines starting with a number" throws away the whole table and falls back to the file that happens
 * to share the short name. That is not hypothetical: it is what this did, and the property list of
 * the font it answered said so (ISO10646 where the alias means ISO8859-1).
 */
class FontPathTest {

    private static FontPath path() {
        return new FontPath(Path.of("src/test/resources/fonts").toAbsolutePath().toString());
    }

    /** A full name in the directory's table is found, and read. */
    @Test
    void aFullNameResolvesThroughTheDirectoryTable() {
        FontPath path = path();
        BitmapFont.Font font = path.open("-misc-fixed-medium-r-normal--13-120-75-75-c-80-iso8859-1");
        assertNotNull(font, "the font was not found");
        assertEquals(11, font.ascent(), "and it is the font the file holds");
        // The file's own spelling, which is capitalised - the same case-insensitive name it was
        // found by, and the one a reference server reports for this file.
        assertEquals("-misc-fixed-medium-r-normal--13-120-75-75-c-80-iso8859-1",
                font.stringPropertyValue("FONT").toLowerCase(java.util.Locale.ROOT),
                "the name it says it has is the one it was found by");
    }

    /** A name nothing on the path holds is a miss, and remembered as one. */
    @Test
    void aNameNobodyHasIsNotInvented() {
        FontPath path = path();
        assertNull(path.open("no-such-font-anywhere"), "a name nothing holds was answered anyway");
        assertNull(path.open("no-such-font-anywhere"), "and asked twice");
    }
}
