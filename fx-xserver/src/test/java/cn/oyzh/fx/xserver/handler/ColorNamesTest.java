package cn.oyzh.fx.xserver.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The colour database the server answers {@code LookupColor} from.
 *
 * <p>xterm builds its ANSI palette by looking up names, and the names it asks for are the numbered
 * variants of {@code rgb.txt}: {@code green3}, {@code cyan3}, {@code blue2} and so on. A table that
 * knew only {@code green} and {@code cyan} answered {@code BadName} for all of them, so those palette
 * entries stayed at their initial black - which is how htop's header and F-key bars, drawn in
 * {@code green3} and {@code cyan3}, came out as black bands on this server and coloured on a
 * reference one (measured 2026-11-09).
 *
 * <p>The numbers are the reference {@code rgb.txt}'s own, including {@code gray50}'s 127 rather than
 * 128: a generated ramp that rounds 50% to 128 is off by one from the database every client reads.
 */
class ColorNamesTest {

    @Test
    void theNumberedVariantsXtermsPaletteUsesResolve() {
        assertEquals(0x00CD00, ResourceHandlers.colorForName("green3"), "green3");
        assertEquals(0x00CDCD, ResourceHandlers.colorForName("cyan3"), "cyan3");
        assertEquals(0xCD0000, ResourceHandlers.colorForName("red3"), "red3");
        assertEquals(0x0000EE, ResourceHandlers.colorForName("blue2"), "blue2");
        assertEquals(0xCDCD00, ResourceHandlers.colorForName("yellow3"), "yellow3");
        assertEquals(0xCD00CD, ResourceHandlers.colorForName("magenta3"), "magenta3");
        assertEquals(0x7F7F7F, ResourceHandlers.colorForName("gray50"), "gray50 is 127, not 128");
        assertEquals(0x000000, ResourceHandlers.colorForName("black"), "black");
        assertEquals(0xFFFFFF, ResourceHandlers.colorForName("white"), "white");
    }

    /** X's lookup ignores case and spaces, and the database has both spellings of a name. */
    @Test
    void namesAreMatchedIgnoringCaseAndSpaces() {
        assertEquals(0xF8F8FF, ResourceHandlers.colorForName("ghost white"), "with a space");
        assertEquals(0xF8F8FF, ResourceHandlers.colorForName("GhostWhite"), "without one");
        assertEquals(0x00FF00, ResourceHandlers.colorForName("GREEN"), "case folded");
        assertEquals(0x00CD00, ResourceHandlers.colorForName("green 3"), "spaces ignored");
        assertEquals(0x00CD00, ResourceHandlers.colorForName("#00cd00"), "the hex form still works");
    }

    @Test
    void aNameOutsideTheDatabaseIsUnknown() {
        assertNull(ResourceHandlers.colorForName("not-a-colour"));
    }
}
