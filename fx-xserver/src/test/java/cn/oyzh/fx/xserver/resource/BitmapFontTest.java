package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The fonts this server serves, and the ones it can only approximate - backlog D3.
 *
 * <p>A font a client asks for by name is now served from a table captured off a reference server, so
 * the metrics it reads through {@code QueryFont} and {@code QueryTextExtents} and the glyphs it draws
 * are that server's. The numbers below are the reference's own, read off it with
 * {@code tools/kit.sh FontProbe 192.168.22.152 6098 fixed}, and they are what fails if the table is
 * ignored: a Java font chosen by the name answers with entirely different ones (its own ascent, its
 * own bearings, and a glyph it rasterizes itself), which is what this class used to do and what the
 * README still calls an approximation for names that have no table.
 *
 * <p>The pixels are checked as well as the metrics, because the two halves fail differently: metrics
 * that are subtly wrong mis-lay every string, and glyphs that are subtly wrong draw a picture no
 * measurement would notice.
 */
class BitmapFontTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 69;

    private static final int WINDOW = 60;
    private static final int HEIGHT = 20;
    private static final int BASELINE = 15;
    private static final int WHITE = 0xFFFFFF;
    private static final int BLACK = 0x000000;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix", "--single-window"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    /** {@code QueryFont} of a font with a table: the reference's bounds, ascent and metrics. */
    @Test
    void aFontWithATableAnswersWithTheReferencesMetrics() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int font = client.allocateId();
            client.openFont(font, "fixed");
            byte[] reply = client.queryFont(font);

            assertEquals(11, (short) RawXClient.readShort(reply, 52), "font ascent");
            assertEquals(2, (short) RawXClient.readShort(reply, 54), "font descent");
            assertEquals(0, RawXClient.readShort(reply, 40), "min-char-or-byte2");
            assertEquals(255, RawXClient.readShort(reply, 42), "max-char-or-byte2");
            assertEquals(0, RawXClient.readShort(reply, 44), "default-char");
            assertEquals(23, RawXClient.readShort(reply, 46), "the reference reports 23 properties");
            assertEquals(256, RawXClient.readInt(reply, 56), "one metric per code 0..255");

            // min-bounds and max-bounds, which are the font's own and not the table's extremes: a
            // fixed pitch font's minimum and maximum width are the same number.
            assertEquals("lb=0 rb=0 width=6 asc=-1 desc=-10", metric(reply, 8), "min-bounds");
            assertEquals("lb=2 rb=6 width=6 asc=11 desc=2", metric(reply, 24), "max-bounds");

            // 'A': five pixels of ink from the origin, nine above the baseline.
            int charInfoAt = 60 + 23 * 8 + 'A' * 12;
            assertEquals("lb=0 rb=5 width=6 asc=9 desc=0", metric(reply, charInfoAt), "'A'");
            // The space has no ink at all, which is what its metrics say.
            int spaceAt = 60 + 23 * 8 + ' ' * 12;
            assertEquals("lb=0 rb=0 width=6 asc=0 desc=0", metric(reply, spaceAt), "' '");
        }
    }

    /** {@code QueryTextExtents}: the string's own ascent and descent, and its edges. */
    @Test
    void aStringIsMeasuredTheWayTheReferenceMeasuresIt() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int font = client.allocateId();
            client.openFont(font, "fixed");
            byte[] chars = "Hello, World 123!".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
            int units = (8 + chars.length * 2 + 2) / 4;
            byte[] request = new byte[units * 4];
            request[0] = 48;                            // QueryTextExtents
            request[1] = 1;                             // an odd number of Char2b, so a pad
            RawXClient.writeShortAt(request, 2, units);
            RawXClient.writeIntAt(request, 4, font);
            for (int i = 0; i < chars.length; i++) {
                request[9 + i * 2] = chars[i];
            }
            client.sendRequest(request);
            byte[] reply = client.readReply();

            assertEquals(11, (short) RawXClient.readShort(reply, 8), "the font's ascent");
            assertEquals(2, (short) RawXClient.readShort(reply, 10), "the font's descent");
            // The *string's* own extremes, which are not the font's: nothing in "Hello, World 123!"
            // is eleven pixels tall, and this is the number a client reserves a line for.
            assertEquals(9, (short) RawXClient.readShort(reply, 12), "overall ascent");
            assertEquals(1, (short) RawXClient.readShort(reply, 14), "overall descent");
            assertEquals(17 * 6, RawXClient.readInt(reply, 16), "overall width");
            assertEquals(0, RawXClient.readInt(reply, 20), "overall left");
            assertEquals(99, RawXClient.readInt(reply, 24), "overall right: '!' ends at 96 + 3");
        }
    }

    /** The drawn glyphs are the reference's shapes, not a Java font's. */
    @Test
    void theGlyphsDrawnAreTheFontsOwn() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int font = client.allocateId();
            client.openFont(font, "fixed");
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, WINDOW, HEIGHT, BLACK);
            client.mapWindow(window);
            client.queryInputFocus();
            int gc = client.allocateId();
            client.createGC(gc, window, WHITE);
            client.changeGC(gc, RawXClient.GCFONT, font);
            byte[] text = "Hi".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
            client.imageText8(window, gc, 0, BASELINE, text, 0, text.length);
            client.flush();
            byte[] image = client.getImage(window, 0, 0, WINDOW, HEIGHT, WHITE);

            // 'H' in the reference's 6x13: two stems, a crossbar, nine pixels above the baseline.
            String h = glyph(image, 0, BASELINE - 9, 6, 9);
            assertEquals("""

                    #...#.
                    #...#.
                    #...#.
                    #...#.
                    #####.
                    #...#.
                    #...#.
                    #...#.
                    #...#.
                    """.strip(), h, "'H' as the reference draws it");
            // The 'i' after it, one cell along: its top row is the dot, a single pixel, and its
            // stem is below - which is what tells the reference's glyph from a Java font's, whose
            // 'i' has a serif and a wider dot.
            String dot = glyph(image, 6, BASELINE - 9, 6, 2);
            assertEquals(1, dot.chars().filter(c -> c == '#').count(), "the dot is one pixel");
            assertTrue(glyph(image, 6, BASELINE - 4, 6, 4).contains("#"), "and the stem is below it");
        }
    }

    /**
     * A name with no table still opens: the approximation is the fallback, and a client that asks
     * for a font nobody here has gets something it can draw with rather than a refusal.
     */
    @Test
    void aNameWithNoTableStillOpensAndDraws() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int font = client.allocateId();
            client.openFont(font, "-adobe-helvetica-medium-r-normal--12-120-75-75-p-67-iso8859-1");
            byte[] reply = client.queryFont(font);
            assertEquals(256, RawXClient.readInt(reply, 56), "the approximation describes every code");
            assertTrue((short) RawXClient.readShort(reply, 52) > 0, "and has an ascent");
        }
    }

    /**
     * A font reached through an alias is listed under the alias and <em>reported</em> under its own
     * name - and that second half is what Xlib's font sets are built on.
     *
     * <p>{@code XCreateFontSet} reads a font's charset out of its own XLFD name (the last two
     * fields), so a server that reports {@code fixed} where the font is
     * {@code -misc-fixed-...-iso8859-1} makes every font unclassifiable: every charset comes back
     * missing and the call fails. Measured against Xvfb 2026-09-26: it answers {@code ListFonts}
     * with {@code fixed} and {@code ListFontsWithInfo} with the XLFD names, and against this server
     * the same probe failed - which is why no X input method worked here (backlog C10).
     *
     * <p>Both halves are asserted, because either one alone would pass against a server that
     * answered the same name to both requests.
     */
    @Test
    void anAliasedFontIsReportedUnderItsOwnName() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            assertEquals(java.util.List.of("fixed"), client.listFonts("fixed", 64),
                    "ListFonts answers the name the pattern matched");
            assertEquals(java.util.List.of(
                            "-Misc-Fixed-Medium-R-SemiCondensed--13-120-75-75-C-60-ISO8859-1"),
                    client.listFontsWithInfo("fixed", 64),
                    "ListFontsWithInfo answers the font's own XLFD name, which is where its "
                            + "charset is");
        }
    }

    /**
     * A font is loadable by the name the server itself reports for it, and by a pattern - the two
     * halves of the same client gesture, and the reason no X font set could be built here.
     *
     * <p>A client that lists fonts gets the font's <em>own</em> XLFD name back (see
     * {@link #anAliasedFontIsReportedUnderItsOwnName}) and then loads it by that name; Xlib's
     * font-set machinery also builds a <em>pattern</em> from the base name and the charset it wants
     * and loads that. Both names used to miss the table and land on the Java approximation, whose
     * font has no {@code CHARSET_REGISTRY}/{@code CHARSET_ENCODING} properties at all - so every
     * charset came back missing and {@code XCreateFontSet} failed. Measured against Xvfb: it answers
     * {@code ISO8859}/{@code 1} for both names where this server answered neither (backlog C10).
     *
     * <p>Each assertion is the table's own answer (ascent 11, descent 2, 23 properties), which is
     * what the approximation cannot produce: without the two lookups the name is unknown, the font
     * is a JVM font, and its numbers are its own.
     */
    @Test
    void aFontIsLoadableByItsOwnNameAndByAPattern() throws Exception {
        String xlfd = "-Misc-Fixed-Medium-R-SemiCondensed--13-120-75-75-C-60-ISO8859-1";
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            assertEquals(java.util.List.of(xlfd), client.listFonts(xlfd, 64),
                    "the name a ListFontsWithInfo reply carries has to be in the font list too, or "
                            + "the client is told a name the server then cannot find");
            for (String name : new String[] {xlfd, "-misc-fixed-*-iso8859-1", "fixed"}) {
                int font = client.allocateId();
                client.openFont(font, name);
                byte[] reply = client.queryFont(font);
                assertEquals(11, (short) RawXClient.readShort(reply, 52), "ascent for " + name);
                assertEquals(2, (short) RawXClient.readShort(reply, 54), "descent for " + name);
                assertEquals(23, RawXClient.readShort(reply, 46),
                        "the real font's properties for " + name);
            }
        }
    }

    private static String metric(byte[] reply, int at) {
        return String.format("lb=%d rb=%d width=%d asc=%d desc=%d",
                (short) RawXClient.readShort(reply, at), (short) RawXClient.readShort(reply, at + 2),
                (short) RawXClient.readShort(reply, at + 4),
                (short) RawXClient.readShort(reply, at + 6),
                (short) RawXClient.readShort(reply, at + 8));
    }

    /** A rectangle of the image as text: {@code #} where the glyph drew, {@code .} where it did not. */
    private static String glyph(byte[] image, int x, int y, int width, int height) {
        int stride = WINDOW * 4;
        StringBuilder out = new StringBuilder();
        for (int row = 0; row < height; row++) {
            if (row > 0) {
                out.append('\n');
            }
            for (int column = 0; column < width; column++) {
                int at = 32 + (y + row) * stride + (x + column) * 4;
                boolean white = at + 2 < image.length
                        && (image[at] & 0xFF) == 0xFF && (image[at + 1] & 0xFF) == 0xFF
                        && (image[at + 2] & 0xFF) == 0xFF;
                out.append(white ? '#' : '.');
            }
        }
        return out.toString();
    }
}
