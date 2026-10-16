package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.resource.BitmapFont;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A font resource.
 *
 * <p>A font a client asks for by name is served from {@link BitmapFont} when that table has it -
 * the real X font, captured from a reference server, so a client that measures text with
 * {@code QueryTextExtents} or lays it out from {@code QueryFont}'s metrics gets the same numbers and
 * the same glyph shapes it would there (backlog D3).
 *
 * <p><b>Every other name falls back to a Java font chosen by the name</b>, which is what this class
 * always did. That is an approximation and the README says so: the shapes and the metrics are the
 * JVM's, and a font file on a font path is not read. The fallback is kept rather than refused
 * because a client that opens a font nobody here has should get something it can draw with - which
 * is also what an X server with a font path does, up to the point where it has no font at all.
 */
public final class XFont extends XResource {

    /** Glyphs are described for byte values 0..255, the range core text requests can address. */
    public static final int MIN_CHAR = 0;
    public static final int MAX_CHAR = 255;

    private final String name;
    /** The real font behind this name, or null when the name only has the Java approximation. */
    private final BitmapFont.Font bitmap;
    private final Font awtFont;
    private final FontMetrics metrics;
    private final int[] charWidths = new int[MAX_CHAR + 1];

    private int fontAscent;
    private int fontDescent;
    private int minLeftBearing;
    private int maxRightBearing;
    private int minWidth = Integer.MAX_VALUE;
    private int maxWidth;
    private boolean fixedPitch;

    public XFont(int id, ClientConnection owner, String name) {
        this(id, owner, name, null);
    }

    /**
     * The same, with a font already read from somewhere else - a file on the font path, which is what
     * the built-in tables cannot answer (D3). Null means "look the name up in the tables", which is
     * what the one argument form above does.
     */
    public XFont(int id, ClientConnection owner, String name, BitmapFont.Font font) {
        super(id, CLASS_FONT, owner);
        this.name = name;
        this.bitmap = font != null ? font : BitmapFont.forName(name);
        if (bitmap != null) {
            // Nothing of the approximation is needed, and not building it is not only thrift: the
            // measurements above are the font's own and a Java font's would be a second answer.
            this.awtFont = null;
            this.metrics = null;
            this.fontAscent = bitmap.ascent();
            this.fontDescent = bitmap.descent();
            this.minLeftBearing = bitmap.minBounds().leftBearing();
            this.maxRightBearing = bitmap.maxBounds().rightBearing();
            this.minWidth = bitmap.minBounds().width();
            this.maxWidth = bitmap.maxBounds().width();
            this.fixedPitch = minWidth == maxWidth;
            // A font's codes do not always start at zero - 12x24's begin at one - and a character
            // the font does not have draws as its default character, which is what its width is.
            int defaultWidth = bitmap.metric(bitmap.defaultChar()) == null
                    ? 0 : bitmap.metric(bitmap.defaultChar()).width();
            for (int ch = MIN_CHAR; ch <= MAX_CHAR; ch++) {
                BitmapFont.Metric metric = bitmap.metric(ch);
                charWidths[ch] = metric == null ? defaultWidth : metric.width();
            }
            return;
        }
        this.awtFont = resolveFont(name);
        this.metrics = new Canvas().getFontMetrics(awtFont);
        computeMetrics();
    }

    /** The character a code the font does not have draws as - {@code QueryFont}'s default-char. */
    public int defaultChar() {
        return bitmap != null ? bitmap.defaultChar() : '?';
    }

    /** The real font behind this name, or null when only the Java approximation has it. */
    public BitmapFont.Font bitmap() {
        return bitmap;
    }

    /**
     * The name the font <em>really</em> has, which is what {@code ListFontsWithInfo} answers with.
     *
     * <p>A font reached through an alias is reported under its own name and not the alias, because
     * that is what a font path does: the name in {@code fonts.dir}/{@code fonts.alias} is what
     * {@code ListFonts} matches and reports, and the XLFD the font carries is what the info replies
     * carry. Measured against a reference server (backlog C10): {@code ListFonts("fixed")} answered
     * {@code fixed} there and {@code ListFontsWithInfo("fixed")} answered
     * {@code -misc-fixed-medium-r-semicondensed--13-120-75-75-c-60-iso8859-1}.
     *
     * <p>It is not cosmetic. Xlib's font-set machinery ({@code XCreateFontSet}) classifies a font by
     * the charset in its own name, so an info reply that names an alias makes the font
     * unclassifiable, every charset comes back missing, and the call fails - which is how this was
     * found: no X input method could be used against this server, and the failing font set was the
     * one {@code xterm} builds for its XIM pre-edit text.
     */
    public String realName() {
        return bitmap != null ? bitmap.xlfd() : name;
    }

    /**
     * The glyph the font itself has for a character, or null when it has none - a space, a control
     * code, or a name that fell back to the Java approximation.
     */
    public BitmapFont.Glyph bitmapGlyph(int ch) {
        return bitmap == null ? null : bitmap.glyph(ch);
    }

    /** The font's own bounds, which {@code QueryFont} reports as min-bounds and max-bounds. */
    public BitmapFont.Metric bitmapBounds(boolean minBounds) {
        if (bitmap == null) {
            return null;
        }
        return minBounds ? bitmap.minBounds() : bitmap.maxBounds();
    }

    /** The character's own metrics, as {@code QueryFont} reports them, or null for no table. */
    public BitmapFont.Metric charMetric(int ch) {
        return bitmap == null ? null : bitmap.metric(ch);
    }

    /** The font's own first and last character codes, which {@code QueryFont} reports. */
    public int minChar() {
        return bitmap == null ? MIN_CHAR : bitmap.minChar();
    }

    public int maxChar() {
        return bitmap == null ? MAX_CHAR : bitmap.maxChar();
    }

    /**
     * Maps an X font name onto a Java font. Both short aliases ("fixed", "9x15") and long XLFD
     * names ("-misc-fixed-medium-r-normal--13-120-75-75-c-70-iso10646-1") are understood.
     */
    public static Font resolveFont(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        int size = parsePixelSize(lower);
        String family = Font.SANS_SERIF;
        if (lower.equals("fixed") || lower.contains("fixed") || lower.contains("courier")
                || lower.contains("mono") || lower.contains("terminal") || lower.contains("console")) {
            family = Font.MONOSPACED;
        } else if (lower.contains("times") || (lower.contains("serif") && !lower.contains("sans"))) {
            family = Font.SERIF;
        }
        if (size <= 0 && lower.matches("\\d+x\\d+")) {
            // Names like 8x13 or 6x13 encode the pixel size in the second number.
            size = Integer.parseInt(lower.substring(lower.indexOf('x') + 1));
        }
        if (size <= 0) {
            size = 12;
        }
        int style = Font.PLAIN;
        if (lower.contains("bold")) {
            style |= Font.BOLD;
        }
        if (lower.contains("-i-") || lower.contains("italic") || lower.contains("oblique")) {
            style |= Font.ITALIC;
        }
        return new Font(family, style, size);
    }

    private static int parsePixelSize(String lower) {
        if (!lower.startsWith("-")) {
            return -1;
        }
        // XLFD: -foundry-family-weight-slant-setwidth-addstyle-pixelsize-...
        String[] parts = lower.split("-");
        if (parts.length > 7) {
            try {
                int value = Integer.parseInt(parts[7]);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // not a sized XLFD, fall through
            }
        }
        return -1;
    }

    private void computeMetrics() {
        fontAscent = metrics.getAscent();
        fontDescent = metrics.getDescent();
        int minW = Integer.MAX_VALUE;
        int maxW = 0;
        for (int ch = MIN_CHAR; ch <= MAX_CHAR; ch++) {
            int width = metrics.charWidth((char) ch);
            if (width <= 0 && Character.isISOControl(ch)) {
                // Control characters have no glyph; X still reports the space width for them.
                width = metrics.charWidth(' ');
            }
            charWidths[ch] = width;
            minW = Math.min(minW, width);
            maxW = Math.max(maxW, width);
        }
        if (minW == Integer.MAX_VALUE) {
            minW = 0;
        }
        minWidth = minW;
        maxWidth = maxW;
        fixedPitch = minW == maxW;
        minLeftBearing = 0;
        maxRightBearing = maxW;
    }

    public String fontName() {
        return name;
    }

    public Font awtFont() {
        return awtFont;
    }

    public FontMetrics fontMetrics() {
        return metrics;
    }

    public int charWidth(int ch) {
        if (ch < MIN_CHAR || ch > MAX_CHAR) {
            return metrics.charWidth((char) ch);
        }
        return charWidths[ch];
    }

    /** Width of a Latin-1 byte string, which is what {@code PolyText8} measures. */
    public int stringWidth8(byte[] chars, int offset, int length) {
        int total = 0;
        for (int i = 0; i < length; i++) {
            total += charWidth(chars[offset + i] & 0xFF);
        }
        return total;
    }

    /** Width of a 16-bit character string, which is what {@code PolyText16} measures. */
    public int stringWidth16(char[] chars, int offset, int length) {
        int total = 0;
        for (int i = 0; i < length; i++) {
            total += charWidth(chars[offset + i]);
        }
        return total;
    }

    public int ascent() {
        return fontAscent;
    }

    public int descent() {
        return fontDescent;
    }

    public int minWidth() {
        return minWidth;
    }

    public int maxWidth() {
        return maxWidth;
    }

    public int minLeftBearing() {
        return minLeftBearing;
    }

    public int maxRightBearing() {
        return maxRightBearing;
    }

    public boolean isFixedPitch() {
        return fixedPitch;
    }

    /**
     * The numeric property set reported by {@code QueryFont}. String valued properties such as
     * FOUNDRY and FAMILY_NAME are reported separately, because their value is the atom of the
     * string rather than a number, and this class has no access to the atom table.
     */
    public List<FontProperty> properties() {
        List<FontProperty> properties = new ArrayList<>();
        if (bitmap != null) {
            // The font's own properties, verbatim: a client that picks a family or a spacing reads
            // these, and inventing them was half of what "the metrics are the JVM's" meant.
            for (String name : bitmap.numericPropertyNames()) {
                properties.add(new FontProperty(name, bitmap.numericPropertyValue(name)));
            }
            return properties;
        }
        properties.add(new FontProperty("FONT_ASCENT", fontAscent));
        properties.add(new FontProperty("FONT_DESCENT", fontDescent));
        properties.add(new FontProperty("DEFAULT_CHAR", '?'));
        properties.add(new FontProperty("PIXEL_SIZE", awtFont.getSize()));
        properties.add(new FontProperty("POINT_SIZE", awtFont.getSize() * 10));
        properties.add(new FontProperty("RESOLUTION_X", 100));
        properties.add(new FontProperty("RESOLUTION_Y", 100));
        properties.add(new FontProperty("SPACING", fixedPitch ? 2 : 0));
        properties.add(new FontProperty("AVERAGE_WIDTH", (minWidth + maxWidth) / 2 * 10));
        return properties;
    }

    /** Properties whose value is a string; the caller interns each one as an atom. */
    public List<TextProperty> textProperties() {
        List<TextProperty> properties = new ArrayList<>();
        if (bitmap != null) {
            // The properties whose value is a string, including FONT - the XLFD this name resolves
            // to, which is how a client that opened an alias learns what it actually got.
            for (String name : bitmap.stringPropertyNames()) {
                properties.add(new TextProperty(name, bitmap.stringPropertyValue(name)));
            }
            return properties;
        }
        properties.add(new TextProperty("FAMILY_NAME", familyName()));
        properties.add(new TextProperty("FOUNDRY", "cn/oyzh/fx/xserver"));
        return properties;
    }

    private String familyName() {
        String family = awtFont.getFamily();
        // X family names are lower case.
        return family == null ? "unknown" : family.toLowerCase(Locale.ROOT);
    }

    /** A font property as reported by {@code QueryFont}: an atom name and a numeric value. */
    public record FontProperty(String name, int value) {
    }

    /** A font property whose value is a string, carried as the atom of that string. */
    public record TextProperty(String name, String value) {
    }

    @Override
    public String typeName() {
        return "font";
    }
}
