package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates {@code resource/BitmapFont.java} - real X bitmap fonts - by <b>reading them off a
 * reference server</b>.
 *
 * <p>This server used to answer every font with a Java font chosen by name, so a client that
 * measured text through {@code QueryTextExtents} or laid it out from {@code QueryFont}'s metrics got
 * a JVM's metrics and a JVM's glyph shapes. The fix (backlog D3) is to serve the font a client
 * actually asked for, and the fonts a client asks for are the ones an X server has: the
 * {@code misc-fixed} family that ships with X.
 *
 * <p><b>The reference is the source, and it is the right one here.</b> {@code QueryFont} answers with
 * every per-character metric the client will read - the left and right bearings, the character width,
 * the ascent and the descent - and a glyph's ink is captured by drawing it into a depth 1 pixmap and
 * reading that back with {@code GetImage}. Deriving the table from the font *file* would mean
 * reimplementing a PCF reader and hoping it agreed with the reference; taking it from the reference
 * cannot disagree with it. The cost is that this generator needs a running X server (the VM's
 * {@code Xvfb :98}, or any server with the font), which is why the table is generated once and
 * checked in - like {@code CursorFont}, and for the same reason.
 *
 * <pre>
 *   tools/kit.sh GenBitmapFont &lt;host&gt; &lt;port&gt; &lt;out.java&gt; [font-name ...]
 * </pre>
 *
 * <p>Each name is opened and its <em>own</em> answer is recorded, so an alias ("fixed") and the XLFD
 * it resolves to ("-misc-fixed-medium-r-semicondensed--13-120-75-75-c-60-iso10646-1") both end up
 * served, by the same table.
 */
public final class GenBitmapFont {

    /** The fonts a client is most likely to ask for by name, when none are given. */
    private static final String[] DEFAULT_FONTS = {
            "fixed", "6x13", "7x13", "9x15", "10x20", "12x24",
    };

    /**
     * {@code QueryFont}'s reply, field by field, read off a reference reply rather than derived: the
     * fixed part is <b>60</b> bytes because each {@code CHARINFO} is padded to sixteen, and the
     * properties and the per-character metrics follow it. Reading the metrics at the unpadded offsets
     * (12 bytes each) puts every field after the first bound four bytes early, which decodes as
     * plausible nonsense - a font with no characters and no properties.
     */
    /**
     * The font properties whose value is the atom of a string rather than a number.
     *
     * <p>X's own convention, and it is not derivable from the reply: {@code QueryFont} answers both
     * kinds as an atom and a CARD32, so a client - and this generator - has to know which names are
     * strings. The list is the one the font conventions define, and it is what {@code xlsfonts -ll}
     * prints as text rather than as a number.
     */
    private static final Set<String> STRING_PROPERTIES = Set.of(
            "FOUNDRY", "FAMILY_NAME", "WEIGHT_NAME", "SLANT", "SETWIDTH_NAME", "ADD_STYLE_NAME",
            "CHARSET_REGISTRY", "CHARSET_ENCODING", "COPYRIGHT", "NOTICE", "FONT",
            "FONTNAME_REGISTRY", "SPACING", "_GBDFED_INFO");

    private static final int QUERY_FONT_FIXED = 60;
    private static final int MIN_BOUNDS_AT = 8;
    private static final int MAX_BOUNDS_AT = 24;
    private static final int PROPERTY_COUNT_AT = 46;
    private static final int ASCENT_AT = 52;
    private static final int DESCENT_AT = 54;
    private static final int DEFAULT_CHAR_AT = 44;
    private static final int CHAR_COUNT_AT = 56;

    private GenBitmapFont() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("usage: GenBitmapFont <host> <port> <out.java> [font-name ...]");
            return;
        }
        List<String> names = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            names.add(args[i]);
        }
        if (names.isEmpty()) {
            names.addAll(List.of(DEFAULT_FONTS));
        }

        StringBuilder tables = new StringBuilder();
        List<String> generated = new ArrayList<>();
        // What the aliases of an already emitted font are called: "fixed" and the XLFD it resolves
        // to are one table with two names, and emitting the table twice would double the file.
        Map<String, String> byXlfd = new LinkedHashMap<>();
        Map<String, String> names2 = new LinkedHashMap<>();
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int screen = client.root();
            for (String name : names) {
                Font font = read(client, screen, name);
                if (font == null) {
                    System.err.println("skipped " + name + ": the reference has no such font");
                    continue;
                }
                String constant = byXlfd.get(font.xlfd());
                if (constant == null) {
                    constant = constantName(name);
                    byXlfd.put(font.xlfd(), constant);
                    tables.append(emit(font, constant));
                }
                names2.put(font.name(), constant);
                generated.add(name + " -> " + font.xlfd() + " (" + constant + ")");
            }
        }
        StringBuilder puts = new StringBuilder();
        names2.forEach((alias, constant) ->
                puts.append("        BY_NAME.put(\"").append(alias).append("\", ").append(constant)
                        .append(");\n"));
        Files.writeString(Path.of(args[2]),
                header() + tables + "\n    static {\n" + puts + "    }\n\n" + footer(),
                StandardCharsets.UTF_8);
        System.out.println("wrote " + args[2] + ": " + generated.size() + " font(s)");
        generated.forEach(line -> System.out.println("  " + line));
    }

    // ------------------------------------------------------------------
    // Reading a font off the reference
    // ------------------------------------------------------------------

    /** One font, as the reference answered for it. */
    private record Font(String name, String xlfd, int ascent, int descent, int defaultChar,
                        int minChar, int maxChar, Metric minBounds, Metric maxBounds,
                        Metric[] metrics, byte[][] ink, String[] propertyNames, int[] propertyValues,
                        String[] propertyStrings) {
    }

    /** One character's metrics, exactly as {@code QueryFont} carries them. */
    private record Metric(int leftBearing, int rightBearing, int width, int ascent, int descent,
                          int attributes) {
    }

    private static Font read(RawXClient client, int screen, String name) throws IOException {
        int font = client.allocateId();
        client.openFont(font, name);
        byte[] reply = client.queryFont(font);
        if ((reply[0] & 0xFF) != 1) {
            return null;
        }
        Metric minBounds = metric(reply, MIN_BOUNDS_AT);
        Metric maxBounds = metric(reply, MAX_BOUNDS_AT);
        int propertyCount = RawXClient.readShort(reply, PROPERTY_COUNT_AT);
        int ascent = (short) RawXClient.readShort(reply, ASCENT_AT);
        int descent = (short) RawXClient.readShort(reply, DESCENT_AT);
        int minChar = RawXClient.readShort(reply, 40) & 0xFFFF;
        int maxChar = RawXClient.readShort(reply, 42) & 0xFFFF;
        int defaultChar = RawXClient.readShort(reply, DEFAULT_CHAR_AT) & 0xFFFF;
        int charCount = RawXClient.readInt(reply, CHAR_COUNT_AT);

        // The properties come first, then one 12 byte metric per character.
        int at = QUERY_FONT_FIXED;
        int[] propertyAtoms = new int[propertyCount];
        int[] propertyValues = new int[propertyCount];
        for (int i = 0; i < propertyCount; i++) {
            propertyAtoms[i] = RawXClient.readInt(reply, at);
            propertyValues[i] = RawXClient.readInt(reply, at + 4);
            at += 8;
        }
        Metric[] metrics = new Metric[charCount];
        for (int i = 0; i < charCount; i++) {
            metrics[i] = metric(reply, at);
            at += 12;
        }

        // The XLFD is what the FONT property holds; a server with no such property (or a short
        // alias) gets the name it was asked for.
        String xlfd = name;
        String[] propertyNames = new String[propertyCount];
        String[] propertyStrings = new String[propertyCount];
        for (int i = 0; i < propertyCount; i++) {
            propertyNames[i] = client.atomName(propertyAtoms[i]);
            if (STRING_PROPERTIES.contains(propertyNames[i])) {
                // The value is an atom of the string, and the string is what a client reads.
                String value = client.atomName(propertyValues[i]);
                propertyStrings[i] = value.startsWith("<") ? "" : value;
            }
            if (propertyNames[i].equals("FONT")) {
                xlfd = propertyStrings[i];
            }
        }

        byte[][] ink = drawGlyphs(client, screen, font, metrics, ascent, descent, minChar);
        return new Font(name, xlfd, ascent, descent, defaultChar, minChar, maxChar, minBounds,
                maxBounds, metrics, ink, propertyNames, propertyValues, propertyStrings);
    }

    /**
     * One {@code CHARINFO}, with the five signed fields sign extended: a descender's descent is
     * negative for a character with no ink below the baseline, and reading it as an unsigned short
     * turns "-10" into 65526 - which, as a box height, is a hundred kilobyte glyph literal.
     */
    private static Metric metric(byte[] reply, int at) {
        return new Metric((short) RawXClient.readShort(reply, at),
                (short) RawXClient.readShort(reply, at + 2),
                (short) RawXClient.readShort(reply, at + 4),
                (short) RawXClient.readShort(reply, at + 6),
                (short) RawXClient.readShort(reply, at + 8),
                RawXClient.readShort(reply, at + 10) & 0xFFFF);
    }

    /**
     * Captures every character's ink by drawing the whole table as one long strip into a depth 1
     * pixmap and reading it back.
     *
     * <p>One strip rather than a pixmap per character: the strip is cleared once, nothing overlaps
     * (each character is laid out on its own advance plus a pixel of gap), and one {@code GetImage}
     * answers for all of them. The ink of a character is the box its metrics describe - from its left
     * bearing to its right bearing, and from its ascent above the baseline to its descent below it -
     * which is exactly the shape the rasterizer draws from.
     */
    /**
     * Captures every character's ink by drawing the whole table as one long strip and reading it
     * back.
     *
     * <p><b>Drawn into the root window rather than into a one-bit pixmap</b>, which is not the
     * obvious choice and was measured: text drawn into a depth 1 pixmap came back as the font's
     * <em>default character</em> for every code - a solid block, space included - on a reference
     * server whose own {@code xlsfonts -ll} says the font has all 256 codes in a linear range. The
     * window path draws real glyphs, and reading a 32 bit image back removes the bit order question
     * at the same time: a pixel is ink when any of its bytes is not zero.
     *
     * <p>One strip rather than a pixmap per character: the strip is painted black once, nothing
     * overlaps (each character is laid out on its own advance plus a pixel of gap), and one
     * {@code GetImage} answers for all of them.
     */
    /**
     * Captures every character's ink, a batch at a time, by drawing the characters as a strip and
     * reading it back.
     *
     * <p><b>Drawn into the root window rather than into a one-bit pixmap</b>, which is not the
     * obvious choice and was measured: text drawn into a depth 1 pixmap came back as the font's
     * <em>default character</em> for every code - a solid block, space included - on a reference
     * server whose own {@code xlsfonts -ll} says the font has all 256 codes in a linear range. The
     * window path draws real glyphs, and reading a 32 bit image back removes the bit order question
     * at the same time: a pixel is ink when any of its bytes is not zero.
     *
     * <p>A batch is as many characters as fit across the screen, because {@code GetImage} of a strip
     * wider than the window is a {@code BadMatch} and not a clipped answer - measured, after a strip
     * of 1852 pixels on a 1300 pixel screen was refused.
     */
    private static byte[][] drawGlyphs(RawXClient client, int screen, int font, Metric[] metrics,
                                       int ascent, int descent, int minChar) throws IOException {
        int count = metrics.length;
        int baseline = ascent + 1;                  // one pixel of margin, so nothing is clipped
        int height = ascent + descent + 2;
        int black = client.allocateId();
        client.createGC(black, screen, 0);
        int white = client.allocateId();
        client.createGC(white, screen, 0xFFFFFF);
        client.changeGC(white, RawXClient.GCFONT, font);
        byte[][] ink = new byte[count][];

        // A batch is filled up to a strip a thousand pixels wide rather than a fixed number of
        // characters: 12x24 is twice as wide as 6x13, and a strip that leaves the screen is a
        // BadMatch and not a clipped answer - measured.
        int from = 0;
        while (from < count) {
            int to = from;
            int[] pen = new int[count];
            int x = 1;
            while (to < count && x + Math.max(metrics[to].width(), 0) + 2 <= 1000) {
                pen[to] = x;
                x += Math.max(metrics[to].width(), 0) + 2;
                to++;
            }
            int width = x + 1;
            client.fillRectangle(screen, black, 0, 0, width, height);
            byte[] text = new byte[1];
            for (int ch = from; ch < to; ch++) {
                // The *code* is the table index plus the font's first character, which is not always
                // zero: a font whose codes begin at one drew every glyph one character early - which
                // looks like a working capture of a font that is simply wrong.
                text[0] = (byte) (minChar + ch);
                client.imageText8(screen, white, pen[ch], baseline, text, 0, 1);
            }
            client.flush();
            // Plane mask within the window's depth: a reference server refuses one with bits outside
            // it, and refuses a rectangle that leaves the window at all.
            byte[] image = client.getImage(screen, 0, 0, width, height, 0xFFFFFF);
            // A 32 bit ZPixmap: four bytes a pixel, in the server's own byte order - so the test for
            // ink is "any byte of the pixel is set", which does not depend on it.
            int stride = width * 4;
            if (System.getenv("DUMP_FONT") != null && from == 0) {
                System.err.printf("batch %d..%d: %dx%d, reply %d bytes, pens 1..%d%n", from, to,
                        width, height, image.length, pen[to - 1]);
                StringBuilder hex = new StringBuilder();
                for (int i = 32; i < 72 && i < image.length; i++) {
                    hex.append(String.format("%02x ", image[i] & 0xFF));
                }
                System.err.println("  data[0..40]: " + hex);
                // and a few pixels of the 'A' cell
                int penA = pen[65];
                for (int row : new int[] {baseline - 9, baseline - 5, baseline - 1}) {
                    StringBuilder raw = new StringBuilder();
                    for (int col = 0; col < 6; col++) {
                        int at = 32 + row * stride + (penA + col) * 4;
                        for (int k = 0; k < 4 && at + k < image.length; k++) {
                            raw.append(String.format("%02x ", image[at + k] & 0xFF));
                        }
                        raw.append("| ");
                    }
                    System.err.println("  'A' row " + row + " bytes: " + raw);
                }
                System.err.println("  stride=" + stride + " penA=" + penA + " baseline=" + baseline);
            }

            for (int ch = from; ch < to; ch++) {
                Metric metric = metrics[ch];
                int left = metric.leftBearing();
                // X measures both bearings from the origin - the right one names the ink's right
                // edge, not the slack before the advance - so the ink is [left, right) wide. Reading
                // it as "width minus both bearings" makes every glyph one or two pixels wide.
                int boxWidth = metric.rightBearing() - metric.leftBearing();
                int boxHeight = metric.ascent() + metric.descent();
                if (boxWidth <= 0 || boxHeight <= 0) {
                    continue;                       // a space, or a character with no ink at all
                }
                byte[] bits = new byte[boxWidth * boxHeight];
                boolean any = false;
                for (int row = 0; row < boxHeight; row++) {
                    int imageY = baseline - metric.ascent() + row;
                    for (int col = 0; col < boxWidth; col++) {
                        int imageX = pen[ch] + left + col;
                        int pixelAt = 32 + imageY * stride + imageX * 4;
                        if (pixelAt + 3 >= image.length) {
                            continue;
                        }
                        // "Is this pixel the white the text was drawn in", not "is it non-zero":
                        // measured, and it cost a round of solid-block glyphs - the black the strip is
                        // painted with comes back as #000001, so every background pixel is non-zero
                        // in its first byte.
                        if ((image[pixelAt] & 0xFF) == 0xFF && (image[pixelAt + 1] & 0xFF) == 0xFF
                                && (image[pixelAt + 2] & 0xFF) == 0xFF) {
                            bits[row * boxWidth + col] = 1;
                            any = true;
                        }
                    }
                }
                if (any) {
                    ink[ch] = bits;
                }
            }
            from = to;
        }
        return ink;
    }

    // ------------------------------------------------------------------
    // Emitting the table
    // ------------------------------------------------------------------

    private static String constantName(String name) {
        String cleaned = name.replace("-", "_").replace(".", "_").toUpperCase(java.util.Locale.ROOT);
        if (cleaned.isEmpty() || !Character.isJavaIdentifierStart(cleaned.charAt(0))) {
            cleaned = "FONT_" + cleaned;
        }
        return cleaned;
    }

    private static String emit(Font font, String constant) {
        StringBuilder out = new StringBuilder();
        // One method per font, and the field calls it: a single initializer holding six tables of
        // 256 metrics and 256 glyphs is more bytecode than one method may hold - the compiler says
        // "code too large" and it is right.
        String factory = constant.toLowerCase(java.util.Locale.ROOT) + "Font";
        out.append("\n    /** ").append(font.name()).append(" - ").append(font.xlfd())
                .append(", captured from a reference server. */\n");
        out.append("    private static final Font ").append(constant).append(" = ").append(factory)
                .append("();\n");
        out.append("\n    private static Font ").append(factory).append("() {\n");
        out.append("        return new Font(\n");
        out.append("            \"").append(font.name()).append("\",\n");
        out.append("            \"").append(font.xlfd()).append("\",\n");
        out.append("            ").append(font.ascent()).append(", ").append(font.descent())
                .append(", ").append(font.defaultChar()).append(", ").append(font.minChar())
                .append(", ").append(font.maxChar()).append(",\n");
        out.append("            ").append(metricLiteral(font.minBounds())).append(",\n");
        out.append("            ").append(metricLiteral(font.maxBounds())).append(",\n");
        out.append("            new Metric[] {\n");
        for (int ch = 0; ch < font.metrics().length; ch++) {
            out.append("                    ").append(metricLiteral(font.metrics()[ch])).append(",");
            if (ch % 4 == 3) {
                out.append("\n");
            }
        }
        if (font.metrics().length % 4 != 0) {
            out.append("\n");
        }
        out.append("            },\n");
        out.append("            new byte[][] {\n");
        for (int ch = 0; ch < font.ink().length; ch++) {
            byte[] bits = font.ink()[ch];
            if (bits == null) {
                out.append("                    null, // ").append(describe(ch)).append("\n");
                continue;
            }
            Metric metric = font.metrics()[ch];
            byte[] packed = pack(bits, metric.rightBearing() - metric.leftBearing(),
                    metric.ascent() + metric.descent());
            out.append("                    bytes(");
            for (int i = 0; i < packed.length; i++) {
                if (i > 0) {
                    out.append(", ");
                }
                out.append(packed[i] & 0xFF);
                if (i % 24 == 23) {
                    out.append("\n                            ");
                }
            }
            out.append("), // ").append(describe(ch)).append("\n");
        }
        out.append("            },\n");
        out.append("            new String[] {");
        for (int i = 0; i < font.propertyNames().length; i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append("\"").append(font.propertyNames()[i]).append("\"");
        }
        out.append("},\n");
        out.append("            new int[] {");
        for (int i = 0; i < font.propertyValues().length; i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append(font.propertyValues()[i]);
        }
        out.append("},\n");
        out.append("            new String[] {");
        for (int i = 0; i < font.propertyStrings().length; i++) {
            if (i > 0) {
                out.append(", ");
            }
            String value = font.propertyStrings()[i];
            out.append(value == null ? "null" : "\"" + value.replace("\\", "\\\\")
                    .replace("\"", "\\\"") + "\"");
        }
        out.append("});\n");
        out.append("    }\n");
        return out.toString();
    }

    /**
     * Packs the rows of a glyph, one byte per eight pixels, least significant bit first - the same
     * shape {@code GetImage} answered in. A font is then a few kilobytes of table rather than tens:
     * the class expands each row back when a glyph is asked for, which happens once per glyph
     * because the rasterizer caches what it draws.
     */
    private static byte[] pack(byte[] bits, int width, int height) {
        int stride = (width + 7) / 8;
        byte[] packed = new byte[stride * height];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (bits[row * width + col] != 0) {
                    packed[row * stride + (col >> 3)] |= (byte) (1 << (col & 7));
                }
            }
        }
        return packed;
    }

    private static String describe(int ch) {
        if (ch >= 0x21 && ch < 0x7F) {
            return "'" + (char) ch + "'";
        }
        return String.format("0x%02x", ch);
    }

    private static String metricLiteral(Metric metric) {
        return "new Metric(" + metric.leftBearing() + ", " + metric.rightBearing() + ", "
                + metric.width() + ", " + metric.ascent() + ", " + metric.descent() + ", "
                + metric.attributes() + ")";
    }

    private static String header() {
        return """
                package cn.oyzh.fx.xserver.resource;

                import java.util.HashMap;
                import java.util.Map;

                /**
                 * The X bitmap fonts this server serves, generated from a reference server by
                 * {@code tools/kit.sh GenBitmapFont} - see that tool for why the reference and not the
                 * font file.
                 *
                 * <p>Each entry carries everything {@code QueryFont} reports for the font: its bounds,
                 * its ascent and descent, every character's own metrics and every glyph's ink, the
                 * properties a client reads. They are the reference's answers, so a client that
                 * measures text here and there gets the same numbers.
                 *
                 * <p>A name with no entry is not an error: {@code XFont} falls back to the Java font
                 * approximation and the README's limitations say so.
                 */
                public final class BitmapFont {

                    /** One glyph's ink, positioned relative to the pen and the baseline. */
                    public record Glyph(int originX, int originY, int width, int height, byte[] bits) {
                    }

                    /** One character's metrics, in {@code QueryFont}'s own order and units. */
                    public record Metric(int leftBearing, int rightBearing, int width, int ascent, int descent,
                                  int attributes) {
                    }

                    /** A font, as the reference server answered for it. */
                    public record Font(String name, String xlfd, int ascent, int descent, int defaultChar,
                                int minChar, int maxChar, Metric minBounds, Metric maxBounds,
                                Metric[] chars, byte[][] ink, String[] propertyNames,
                                int[] propertyValues, String[] propertyStrings) {

                        /** The properties whose value is a number, in the reference's own order. */
                        java.util.List<String> numericPropertyNames() {
                            java.util.List<String> out = new java.util.ArrayList<>();
                            for (int i = 0; i < propertyNames.length; i++) {
                                if (propertyStrings[i] == null) {
                                    out.add(propertyNames[i]);
                                }
                            }
                            return out;
                        }

                        /** The properties whose value is a string, in the reference's own order. */
                        java.util.List<String> stringPropertyNames() {
                            java.util.List<String> out = new java.util.ArrayList<>();
                            for (int i = 0; i < propertyNames.length; i++) {
                                if (propertyStrings[i] != null) {
                                    out.add(propertyNames[i]);
                                }
                            }
                            return out;
                        }

                        int numericPropertyValue(String name) {
                            for (int i = 0; i < propertyNames.length; i++) {
                                if (propertyStrings[i] == null && propertyNames[i].equals(name)) {
                                    return propertyValues[i];
                                }
                            }
                            return 0;
                        }

                        String stringPropertyValue(String name) {
                            for (int i = 0; i < propertyNames.length; i++) {
                                if (propertyStrings[i] != null && propertyNames[i].equals(name)) {
                                    return propertyStrings[i];
                                }
                            }
                            return null;
                        }

                        /**
                         * The glyph for a character, or null when it has no ink (a space).
                         *
                         * <p>The table holds each row packed eight pixels to a byte - the shape
                         * {@code GetImage} answered in - and this expands it into the one byte per
                         * pixel the rasterizer draws from. Once per glyph: the rasterizer caches
                         * what it has drawn, and a request repeats the same few dozen characters.
                         */
                        /** A character's metrics, or null when the font has no such character. */
                        Metric metric(int ch) {
                            int index = ch - minChar;
                            return index < 0 || index >= chars.length ? null : chars[index];
                        }

                        Glyph glyph(int ch) {
                            // The table starts at the font's own first character, which is not
                            // always zero: a font whose codes begin at one reports one as its
                            // min-char-or-byte2, and a client indexes from there too.
                            int index = ch - minChar;
                            if (index < 0 || index >= chars.length || ink[index] == null) {
                                return null;
                            }
                            Metric metric = chars[index];
                            int width = metric.rightBearing() - metric.leftBearing();
                            int height = metric.ascent() + metric.descent();
                            byte[] packed = ink[index];
                            int stride = (width + 7) / 8;
                            byte[] bits = new byte[width * height];
                            for (int row = 0; row < height; row++) {
                                for (int col = 0; col < width; col++) {
                                    int at = row * stride + (col >> 3);
                                    if (at < packed.length
                                            && ((packed[at] >> (col & 7)) & 1) != 0) {
                                        bits[row * width + col] = 1;
                                    }
                                }
                            }
                            return new Glyph(metric.leftBearing(), -metric.ascent(), width, height,
                                    bits);
                        }
                    }

                    private static final Map<String, Font> BY_NAME = new HashMap<>();

                    /** A glyph's rows, packed, from the literal a table writes. */
                    private static byte[] bytes(int... values) {
                        byte[] out = new byte[values.length];
                        for (int i = 0; i < values.length; i++) {
                            out[i] = (byte) values[i];
                        }
                        return out;
                    }

                """;
    }

    private static String footer() {
        return """
                    /** The font a client asked for by name, or null when it is not one of these. */
                    static Font forName(String name) {
                        return name == null ? null : BY_NAME.get(name.trim().toLowerCase(java.util.Locale.ROOT));
                    }

                    private BitmapFont() {
                    }
                }
                """;
    }

    private static byte[] bits(int... values) {
        byte[] out = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = (byte) values[i];
        }
        return out;
    }
}
