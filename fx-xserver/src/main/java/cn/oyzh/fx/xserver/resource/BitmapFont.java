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


    /** fixed - -Misc-Fixed-Medium-R-SemiCondensed--13-120-75-75-C-60-ISO8859-1, captured from a reference server. */
    private static final Font FIXED = fixedFont();

    private static Font fixedFont() {
        return new Font(
            "fixed",
            "-Misc-Fixed-Medium-R-SemiCondensed--13-120-75-75-C-60-ISO8859-1",
            11, 2, 0, 0, 255,
            new Metric(0, 0, 6, -1, -10, 0),
            new Metric(2, 6, 6, 11, 2, 0),
            new Metric[] {
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 7, -2, 0),                    new Metric(0, 6, 6, 11, 2, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(1, 5, 6, 9, -5, 0),
                    new Metric(0, 5, 6, 8, -1, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 3, 6, 11, -4, 0),
                    new Metric(0, 3, 6, 5, 2, 0),                    new Metric(2, 6, 6, 5, 2, 0),                    new Metric(2, 6, 6, 11, -4, 0),                    new Metric(0, 6, 6, 11, 2, 0),
                    new Metric(0, 6, 6, 11, -10, 0),                    new Metric(0, 6, 6, 8, -7, 0),                    new Metric(0, 6, 6, 5, -4, 0),                    new Metric(0, 6, 6, 2, -1, 0),
                    new Metric(0, 6, 6, -1, 2, 0),                    new Metric(2, 6, 6, 11, 2, 0),                    new Metric(0, 3, 6, 11, 2, 0),                    new Metric(0, 6, 6, 11, -4, 0),
                    new Metric(0, 6, 6, 5, 2, 0),                    new Metric(2, 3, 6, 11, 2, 0),                    new Metric(0, 5, 6, 8, -1, 0),                    new Metric(0, 5, 6, 8, -1, 0),
                    new Metric(0, 5, 6, 6, 0, 0),                    new Metric(0, 5, 6, 6, -1, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(2, 4, 6, 5, -4, 0),
                    new Metric(0, 0, 6, 0, 0, 0),                    new Metric(2, 3, 6, 9, 0, 0),                    new Metric(1, 4, 6, 9, -6, 0),                    new Metric(0, 5, 6, 8, -1, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 8, 0, 0),                    new Metric(2, 3, 6, 9, -6, 0),
                    new Metric(1, 4, 6, 10, 1, 0),                    new Metric(1, 4, 6, 10, 1, 0),                    new Metric(0, 5, 6, 9, -4, 0),                    new Metric(0, 5, 6, 7, -2, 0),
                    new Metric(1, 4, 6, 2, 1, 0),                    new Metric(0, 5, 6, 5, -4, 0),                    new Metric(1, 4, 6, 2, 1, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(1, 4, 6, 7, 1, 0),                    new Metric(1, 4, 6, 7, 1, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 6, -2, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(1, 4, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 1, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(1, 4, 6, 10, 1, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(1, 4, 6, 10, 1, 0),                    new Metric(0, 5, 6, 9, -6, 0),                    new Metric(0, 5, 6, 0, 1, 0),
                    new Metric(2, 4, 6, 10, -8, 0),                    new Metric(0, 5, 6, 6, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 6, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 6, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 6, 2, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(1, 4, 6, 8, 0, 0),                    new Metric(0, 4, 6, 8, 2, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(1, 4, 6, 9, 0, 0),                    new Metric(0, 5, 6, 6, 0, 0),                    new Metric(0, 5, 6, 6, 0, 0),                    new Metric(0, 5, 6, 6, 0, 0),
                    new Metric(0, 5, 6, 6, 2, 0),                    new Metric(0, 5, 6, 6, 2, 0),                    new Metric(0, 5, 6, 6, 0, 0),                    new Metric(0, 5, 6, 6, 0, 0),
                    new Metric(0, 5, 6, 8, 0, 0),                    new Metric(0, 5, 6, 6, 0, 0),                    new Metric(0, 5, 6, 6, 0, 0),                    new Metric(0, 5, 6, 6, 0, 0),
                    new Metric(0, 5, 6, 6, 0, 0),                    new Metric(0, 5, 6, 6, 2, 0),                    new Metric(0, 5, 6, 6, 0, 0),                    new Metric(0, 5, 6, 10, 1, 0),
                    new Metric(2, 3, 6, 9, 0, 0),                    new Metric(0, 5, 6, 10, 1, 0),                    new Metric(0, 5, 6, 9, -6, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 6, 0, 0, 0),                    new Metric(2, 3, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, -1, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 7, -1, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(2, 3, 6, 9, 0, 0),                    new Metric(1, 5, 6, 10, 0, 0),
                    new Metric(1, 4, 6, 10, -8, 0),                    new Metric(0, 5, 6, 10, -1, 0),                    new Metric(0, 5, 6, 9, -2, 0),                    new Metric(0, 5, 6, 7, -1, 0),
                    new Metric(0, 5, 6, 5, -2, 0),                    new Metric(1, 4, 6, 5, -4, 0),                    new Metric(0, 5, 6, 10, -1, 0),                    new Metric(0, 5, 6, 9, -8, 0),
                    new Metric(1, 5, 6, 9, -5, 0),                    new Metric(0, 5, 6, 8, -1, 0),                    new Metric(0, 3, 6, 10, -5, 0),                    new Metric(0, 3, 6, 10, -5, 0),
                    new Metric(2, 4, 6, 10, -8, 0),                    new Metric(0, 5, 6, 6, 2, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(2, 4, 6, 5, -4, 0),
                    new Metric(2, 4, 6, 0, 2, 0),                    new Metric(0, 3, 6, 10, -5, 0),                    new Metric(0, 5, 6, 9, -2, 0),                    new Metric(0, 5, 6, 7, -1, 0),
                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),
                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 2, 0),
                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),
                    new Metric(1, 4, 6, 10, 0, 0),                    new Metric(1, 4, 6, 10, 0, 0),                    new Metric(1, 5, 6, 10, 0, 0),                    new Metric(1, 4, 6, 10, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),
                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 6, -1, 0),
                    new Metric(0, 5, 6, 10, 1, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),
                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 6, 0, 0),                    new Metric(0, 5, 6, 6, 2, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(1, 4, 6, 9, 0, 0),                    new Metric(1, 4, 6, 9, 0, 0),                    new Metric(1, 5, 6, 9, 0, 0),                    new Metric(1, 4, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 10, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 8, -1, 0),
                    new Metric(0, 5, 6, 7, 1, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 0, 0),
                    new Metric(0, 5, 6, 9, 0, 0),                    new Metric(0, 5, 6, 9, 2, 0),                    new Metric(0, 5, 6, 8, 2, 0),                    new Metric(0, 5, 6, 9, 2, 0),
            },
            new byte[][] {
                    bytes(21, 0, 17, 0, 17, 0, 17, 0, 21), // 0x00
                    bytes(4, 14, 31, 14, 4), // 0x01
                    bytes(21, 42, 21, 42, 21, 42, 21, 42, 21, 42, 21, 42, 21), // 0x02
                    bytes(5, 5, 7, 5, 5, 28, 8, 8, 8), // 0x03
                    bytes(7, 1, 3, 1, 29, 4, 12, 4, 4), // 0x04
                    bytes(6, 1, 1, 6, 12, 20, 12, 20, 20), // 0x05
                    bytes(1, 1, 1, 7, 28, 4, 12, 4, 4), // 0x06
                    bytes(6, 9, 9, 6), // 0x07
                    bytes(4, 4, 31, 4, 4, 0, 31), // 0x08
                    bytes(9, 11, 13, 9, 4, 4, 4, 4, 28), // 0x09
                    bytes(5, 5, 5, 2, 2, 28, 8, 8, 8), // 0x0a
                    bytes(4, 4, 4, 4, 4, 4, 7), // 0x0b
                    bytes(7, 4, 4, 4, 4, 4, 4), // 0x0c
                    bytes(15, 1, 1, 1, 1, 1, 1), // 0x0d
                    bytes(1, 1, 1, 1, 1, 1, 15), // 0x0e
                    bytes(4, 4, 4, 4, 4, 4, 63, 4, 4, 4, 4, 4, 4), // 0x0f
                    bytes(63), // 0x10
                    bytes(63), // 0x11
                    bytes(63), // 0x12
                    bytes(63), // 0x13
                    bytes(63), // 0x14
                    bytes(1, 1, 1, 1, 1, 1, 15, 1, 1, 1, 1, 1, 1), // 0x15
                    bytes(4, 4, 4, 4, 4, 4, 7, 4, 4, 4, 4, 4, 4), // 0x16
                    bytes(4, 4, 4, 4, 4, 4, 63), // 0x17
                    bytes(63, 4, 4, 4, 4, 4, 4), // 0x18
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // 0x19
                    bytes(24, 6, 1, 6, 24, 0, 31), // 0x1a
                    bytes(3, 12, 16, 12, 3, 0, 31), // 0x1b
                    bytes(31, 10, 10, 10, 10, 10), // 0x1c
                    bytes(16, 31, 4, 31, 1), // 0x1d
                    bytes(12, 18, 2, 2, 7, 2, 2, 18, 13), // 0x1e
                    bytes(3), // 0x1f
                    null, // 0x20
                    bytes(1, 1, 1, 1, 1, 1, 1, 0, 1), // '!'
                    bytes(5, 5, 5), // '"'
                    bytes(10, 10, 31, 10, 31, 10, 10), // '#'
                    bytes(4, 30, 5, 5, 14, 20, 20, 15, 4), // '$'
                    bytes(18, 21, 10, 8, 4, 2, 10, 21, 9), // '%'
                    bytes(2, 5, 5, 2, 5, 25, 9, 22), // '&'
                    bytes(1, 1, 1), // '''
                    bytes(4, 2, 2, 1, 1, 1, 1, 1, 2, 2, 4), // '('
                    bytes(1, 2, 2, 4, 4, 4, 4, 4, 2, 2, 1), // ')'
                    bytes(4, 21, 14, 21, 4), // '*'
                    bytes(4, 4, 31, 4, 4), // '+'
                    bytes(6, 2, 1), // ','
                    bytes(31), // '-'
                    bytes(2, 7, 2), // '.'
                    bytes(16, 16, 8, 8, 4, 2, 2, 1, 1), // '/'
                    bytes(4, 10, 17, 17, 17, 17, 17, 10, 4), // '0'
                    bytes(4, 6, 5, 4, 4, 4, 4, 4, 31), // '1'
                    bytes(14, 17, 17, 16, 8, 4, 2, 1, 31), // '2'
                    bytes(31, 16, 8, 4, 14, 16, 16, 17, 14), // '3'
                    bytes(8, 8, 12, 10, 10, 9, 31, 8, 8), // '4'
                    bytes(31, 1, 1, 13, 19, 16, 16, 17, 14), // '5'
                    bytes(14, 17, 1, 1, 15, 17, 17, 17, 14), // '6'
                    bytes(31, 16, 8, 8, 4, 4, 2, 2, 2), // '7'
                    bytes(14, 17, 17, 17, 14, 17, 17, 17, 14), // '8'
                    bytes(14, 17, 17, 17, 30, 16, 16, 17, 14), // '9'
                    bytes(2, 7, 2, 0, 0, 2, 7, 2), // ':'
                    bytes(2, 7, 2, 0, 0, 6, 2, 1), // ';'
                    bytes(16, 8, 4, 2, 1, 2, 4, 8, 16), // '<'
                    bytes(31, 0, 0, 31), // '='
                    bytes(1, 2, 4, 8, 16, 8, 4, 2, 1), // '>'
                    bytes(14, 17, 17, 16, 8, 4, 4, 0, 4), // '?'
                    bytes(14, 17, 17, 25, 21, 21, 13, 1, 30), // '@'
                    bytes(4, 10, 17, 17, 17, 31, 17, 17, 17), // 'A'
                    bytes(15, 18, 18, 18, 14, 18, 18, 18, 15), // 'B'
                    bytes(14, 17, 1, 1, 1, 1, 1, 17, 14), // 'C'
                    bytes(15, 18, 18, 18, 18, 18, 18, 18, 15), // 'D'
                    bytes(31, 1, 1, 1, 15, 1, 1, 1, 31), // 'E'
                    bytes(31, 1, 1, 1, 15, 1, 1, 1, 1), // 'F'
                    bytes(14, 17, 1, 1, 1, 25, 17, 17, 14), // 'G'
                    bytes(17, 17, 17, 17, 31, 17, 17, 17, 17), // 'H'
                    bytes(7, 2, 2, 2, 2, 2, 2, 2, 7), // 'I'
                    bytes(28, 8, 8, 8, 8, 8, 8, 9, 6), // 'J'
                    bytes(17, 17, 9, 5, 3, 5, 9, 17, 17), // 'K'
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 31), // 'L'
                    bytes(17, 17, 27, 21, 21, 17, 17, 17, 17), // 'M'
                    bytes(17, 19, 19, 21, 21, 25, 25, 17, 17), // 'N'
                    bytes(14, 17, 17, 17, 17, 17, 17, 17, 14), // 'O'
                    bytes(15, 17, 17, 17, 15, 1, 1, 1, 1), // 'P'
                    bytes(14, 17, 17, 17, 17, 17, 17, 21, 14, 16), // 'Q'
                    bytes(15, 17, 17, 17, 15, 5, 9, 17, 17), // 'R'
                    bytes(14, 17, 1, 1, 14, 16, 16, 17, 14), // 'S'
                    bytes(31, 4, 4, 4, 4, 4, 4, 4, 4), // 'T'
                    bytes(17, 17, 17, 17, 17, 17, 17, 17, 14), // 'U'
                    bytes(17, 17, 17, 17, 10, 10, 10, 4, 4), // 'V'
                    bytes(17, 17, 17, 17, 21, 21, 21, 21, 10), // 'W'
                    bytes(17, 17, 10, 10, 4, 10, 10, 17, 17), // 'X'
                    bytes(17, 17, 10, 10, 4, 4, 4, 4, 4), // 'Y'
                    bytes(31, 16, 8, 8, 4, 2, 2, 1, 31), // 'Z'
                    bytes(7, 1, 1, 1, 1, 1, 1, 1, 1, 1, 7), // '['
                    bytes(1, 1, 2, 2, 4, 8, 8, 16, 16), // '\'
                    bytes(7, 4, 4, 4, 4, 4, 4, 4, 4, 4, 7), // ']'
                    bytes(4, 10, 17), // '^'
                    bytes(31), // '_'
                    bytes(1, 2), // '`'
                    bytes(14, 16, 30, 17, 25, 22), // 'a'
                    bytes(1, 1, 1, 15, 17, 17, 17, 17, 15), // 'b'
                    bytes(14, 17, 1, 1, 17, 14), // 'c'
                    bytes(16, 16, 16, 30, 17, 17, 17, 17, 30), // 'd'
                    bytes(14, 17, 31, 1, 17, 14), // 'e'
                    bytes(12, 18, 2, 2, 15, 2, 2, 2, 2), // 'f'
                    bytes(14, 17, 17, 17, 30, 16, 17, 14), // 'g'
                    bytes(1, 1, 1, 13, 19, 17, 17, 17, 17), // 'h'
                    bytes(2, 0, 3, 2, 2, 2, 2, 7), // 'i'
                    bytes(8, 0, 12, 8, 8, 8, 8, 9, 9, 6), // 'j'
                    bytes(1, 1, 1, 9, 5, 3, 5, 9, 17), // 'k'
                    bytes(3, 2, 2, 2, 2, 2, 2, 2, 7), // 'l'
                    bytes(11, 21, 21, 21, 21, 17), // 'm'
                    bytes(13, 19, 17, 17, 17, 17), // 'n'
                    bytes(14, 17, 17, 17, 17, 14), // 'o'
                    bytes(15, 17, 17, 17, 15, 1, 1, 1), // 'p'
                    bytes(30, 17, 17, 17, 30, 16, 16, 16), // 'q'
                    bytes(13, 19, 1, 1, 1, 1), // 'r'
                    bytes(14, 17, 6, 8, 17, 14), // 's'
                    bytes(2, 2, 15, 2, 2, 2, 18, 12), // 't'
                    bytes(17, 17, 17, 17, 25, 22), // 'u'
                    bytes(17, 17, 17, 10, 10, 4), // 'v'
                    bytes(17, 17, 21, 21, 21, 10), // 'w'
                    bytes(17, 10, 4, 4, 10, 17), // 'x'
                    bytes(17, 17, 17, 25, 22, 16, 17, 14), // 'y'
                    bytes(31, 8, 4, 2, 1, 31), // 'z'
                    bytes(24, 4, 4, 4, 4, 3, 4, 4, 4, 4, 24), // '{'
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1), // '|'
                    bytes(3, 4, 4, 4, 4, 24, 4, 4, 4, 4, 3), // '}'
                    bytes(18, 21, 9), // '~'
                    null, // 0x7f
                    null, // 0x80
                    null, // 0x81
                    null, // 0x82
                    null, // 0x83
                    null, // 0x84
                    null, // 0x85
                    null, // 0x86
                    null, // 0x87
                    null, // 0x88
                    null, // 0x89
                    null, // 0x8a
                    null, // 0x8b
                    null, // 0x8c
                    null, // 0x8d
                    null, // 0x8e
                    null, // 0x8f
                    null, // 0x90
                    null, // 0x91
                    null, // 0x92
                    null, // 0x93
                    null, // 0x94
                    null, // 0x95
                    null, // 0x96
                    null, // 0x97
                    null, // 0x98
                    null, // 0x99
                    null, // 0x9a
                    null, // 0x9b
                    null, // 0x9c
                    null, // 0x9d
                    null, // 0x9e
                    null, // 0x9f
                    null, // 0xa0
                    bytes(1, 0, 1, 1, 1, 1, 1, 1, 1), // 0xa1
                    bytes(4, 14, 21, 5, 5, 21, 14, 4), // 0xa2
                    bytes(12, 18, 2, 2, 7, 2, 2, 18, 13), // 0xa3
                    bytes(17, 14, 10, 10, 14, 17), // 0xa4
                    bytes(17, 17, 10, 10, 31, 4, 31, 4, 4), // 0xa5
                    bytes(1, 1, 1, 1, 0, 1, 1, 1, 1), // 0xa6
                    bytes(6, 9, 1, 6, 9, 9, 6, 8, 9, 6), // 0xa7
                    bytes(5, 5), // 0xa8
                    bytes(14, 17, 21, 27, 19, 27, 21, 17, 14), // 0xa9
                    bytes(14, 16, 30, 17, 30, 0, 31), // 0xaa
                    bytes(20, 10, 5, 5, 10, 20), // 0xab
                    bytes(31, 16, 16), // 0xac
                    bytes(7), // 0xad
                    bytes(14, 17, 23, 27, 27, 23, 27, 17, 14), // 0xae
                    bytes(31), // 0xaf
                    bytes(6, 9, 9, 6), // 0xb0
                    bytes(4, 4, 31, 4, 4, 0, 31), // 0xb1
                    bytes(2, 5, 4, 2, 7), // 0xb2
                    bytes(2, 5, 2, 4, 3), // 0xb3
                    bytes(2, 1), // 0xb4
                    bytes(17, 17, 17, 17, 25, 23, 1, 1), // 0xb5
                    bytes(30, 23, 23, 23, 23, 22, 20, 20, 20), // 0xb6
                    bytes(3), // 0xb7
                    bytes(2, 1), // 0xb8
                    bytes(2, 3, 2, 2, 7), // 0xb9
                    bytes(14, 17, 17, 17, 14, 0, 31), // 0xba
                    bytes(5, 10, 20, 20, 10, 5), // 0xbb
                    bytes(2, 3, 2, 2, 7, 16, 24, 20, 28, 16), // 0xbc
                    bytes(2, 3, 2, 2, 7, 8, 20, 16, 8, 28), // 0xbd
                    bytes(2, 5, 2, 4, 5, 18, 24, 20, 28, 16), // 0xbe
                    bytes(4, 0, 4, 4, 2, 1, 17, 17, 14), // 0xbf
                    bytes(2, 4, 0, 4, 10, 17, 17, 31, 17, 17), // 0xc0
                    bytes(8, 4, 0, 4, 10, 17, 17, 31, 17, 17), // 0xc1
                    bytes(12, 18, 0, 4, 10, 17, 17, 31, 17, 17), // 0xc2
                    bytes(20, 10, 0, 4, 10, 17, 17, 31, 17, 17), // 0xc3
                    bytes(10, 10, 0, 4, 10, 17, 17, 31, 17, 17), // 0xc4
                    bytes(4, 10, 4, 4, 10, 17, 17, 31, 17, 17), // 0xc5
                    bytes(26, 5, 5, 5, 13, 7, 5, 5, 29), // 0xc6
                    bytes(14, 17, 1, 1, 1, 1, 1, 17, 14, 4, 2), // 0xc7
                    bytes(2, 4, 0, 31, 1, 1, 15, 1, 1, 31), // 0xc8
                    bytes(8, 4, 0, 31, 1, 1, 15, 1, 1, 31), // 0xc9
                    bytes(12, 18, 0, 31, 1, 1, 15, 1, 1, 31), // 0xca
                    bytes(10, 10, 0, 31, 1, 1, 15, 1, 1, 31), // 0xcb
                    bytes(1, 2, 0, 7, 2, 2, 2, 2, 2, 7), // 0xcc
                    bytes(4, 2, 0, 7, 2, 2, 2, 2, 2, 7), // 0xcd
                    bytes(6, 9, 0, 7, 2, 2, 2, 2, 2, 7), // 0xce
                    bytes(5, 5, 0, 7, 2, 2, 2, 2, 2, 7), // 0xcf
                    bytes(15, 18, 18, 18, 23, 18, 18, 18, 15), // 0xd0
                    bytes(20, 10, 0, 17, 17, 19, 21, 25, 17, 17), // 0xd1
                    bytes(2, 4, 0, 14, 17, 17, 17, 17, 17, 14), // 0xd2
                    bytes(8, 4, 0, 14, 17, 17, 17, 17, 17, 14), // 0xd3
                    bytes(12, 18, 0, 14, 17, 17, 17, 17, 17, 14), // 0xd4
                    bytes(20, 10, 0, 14, 17, 17, 17, 17, 17, 14), // 0xd5
                    bytes(10, 10, 0, 14, 17, 17, 17, 17, 17, 14), // 0xd6
                    bytes(17, 10, 4, 10, 17), // 0xd7
                    bytes(16, 14, 25, 25, 21, 21, 21, 19, 19, 14, 1), // 0xd8
                    bytes(2, 4, 0, 17, 17, 17, 17, 17, 17, 14), // 0xd9
                    bytes(8, 4, 0, 17, 17, 17, 17, 17, 17, 14), // 0xda
                    bytes(12, 18, 0, 17, 17, 17, 17, 17, 17, 14), // 0xdb
                    bytes(10, 10, 0, 17, 17, 17, 17, 17, 17, 14), // 0xdc
                    bytes(8, 4, 0, 17, 17, 10, 4, 4, 4, 4), // 0xdd
                    bytes(1, 15, 17, 17, 17, 15, 1, 1, 1), // 0xde
                    bytes(6, 9, 9, 5, 5, 9, 17, 17, 13), // 0xdf
                    bytes(2, 4, 0, 14, 16, 30, 17, 25, 22), // 0xe0
                    bytes(8, 4, 0, 14, 16, 30, 17, 25, 22), // 0xe1
                    bytes(12, 18, 0, 14, 16, 30, 17, 25, 22), // 0xe2
                    bytes(20, 10, 0, 14, 16, 30, 17, 25, 22), // 0xe3
                    bytes(10, 10, 0, 14, 16, 30, 17, 25, 22), // 0xe4
                    bytes(12, 18, 12, 0, 14, 16, 30, 17, 25, 22), // 0xe5
                    bytes(14, 20, 14, 5, 21, 10), // 0xe6
                    bytes(14, 17, 1, 1, 17, 14, 4, 2), // 0xe7
                    bytes(2, 4, 0, 14, 17, 31, 1, 17, 14), // 0xe8
                    bytes(8, 4, 0, 14, 17, 31, 1, 17, 14), // 0xe9
                    bytes(12, 18, 0, 14, 17, 31, 1, 17, 14), // 0xea
                    bytes(10, 10, 0, 14, 17, 31, 1, 17, 14), // 0xeb
                    bytes(1, 2, 0, 3, 2, 2, 2, 2, 7), // 0xec
                    bytes(4, 2, 0, 3, 2, 2, 2, 2, 7), // 0xed
                    bytes(6, 9, 0, 3, 2, 2, 2, 2, 7), // 0xee
                    bytes(5, 5, 0, 3, 2, 2, 2, 2, 7), // 0xef
                    bytes(10, 4, 6, 8, 14, 17, 17, 17, 17, 14), // 0xf0
                    bytes(20, 10, 0, 13, 19, 17, 17, 17, 17), // 0xf1
                    bytes(2, 4, 0, 14, 17, 17, 17, 17, 14), // 0xf2
                    bytes(8, 4, 0, 14, 17, 17, 17, 17, 14), // 0xf3
                    bytes(12, 18, 0, 14, 17, 17, 17, 17, 14), // 0xf4
                    bytes(20, 10, 0, 14, 17, 17, 17, 17, 14), // 0xf5
                    bytes(10, 10, 0, 14, 17, 17, 17, 17, 14), // 0xf6
                    bytes(4, 4, 0, 31, 0, 4, 4), // 0xf7
                    bytes(16, 14, 25, 21, 21, 19, 14, 1), // 0xf8
                    bytes(2, 4, 0, 17, 17, 17, 17, 25, 22), // 0xf9
                    bytes(8, 4, 0, 17, 17, 17, 17, 25, 22), // 0xfa
                    bytes(12, 18, 0, 17, 17, 17, 17, 25, 22), // 0xfb
                    bytes(10, 10, 0, 17, 17, 17, 17, 25, 22), // 0xfc
                    bytes(8, 4, 0, 17, 17, 17, 25, 22, 16, 17, 14), // 0xfd
                    bytes(1, 1, 13, 19, 17, 17, 19, 13, 1, 1), // 0xfe
                    bytes(10, 10, 0, 17, 17, 17, 25, 22, 16, 17, 14), // 0xff
            },
            new String[] {"FONTNAME_REGISTRY", "FOUNDRY", "FAMILY_NAME", "WEIGHT_NAME", "SLANT", "SETWIDTH_NAME", "ADD_STYLE_NAME", "PIXEL_SIZE", "POINT_SIZE", "RESOLUTION_X", "RESOLUTION_Y", "SPACING", "AVERAGE_WIDTH", "CHARSET_REGISTRY", "CHARSET_ENCODING", "COPYRIGHT", "CAP_HEIGHT", "X_HEIGHT", "_GBDFED_INFO", "FONT", "WEIGHT", "RESOLUTION", "QUAD_WIDTH"},
            new int[] {87, 89, 90, 92, 94, 96, 87, 13, 120, 75, 75, 102, 60, 105, 107, 108, 9, 6, 110, 111, 10, 103, 6},
            new String[] {"", "Misc", "Fixed", "Medium", "R", "SemiCondensed", "", null, null, null, null, "C", null, "ISO8859", "1", "Public domain font.  Share and enjoy.", null, null, "Edited with gbdfed 1.3.", "-Misc-Fixed-Medium-R-SemiCondensed--13-120-75-75-C-60-ISO8859-1", null, null, null});
    }

    /** 7x13 - -Misc-Fixed-Medium-R-Normal--13-120-75-75-C-70-ISO8859-1, captured from a reference server. */
    private static final Font FONT_7X13 = font_7x13Font();

    private static Font font_7x13Font() {
        return new Font(
            "7x13",
            "-Misc-Fixed-Medium-R-Normal--13-120-75-75-C-70-ISO8859-1",
            11, 2, 0, 0, 255,
            new Metric(0, 0, 7, -1, -10, 0),
            new Metric(3, 7, 7, 11, 2, 0),
            new Metric[] {
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 7, 7, 7, 0, 0),                    new Metric(0, 7, 7, 11, 2, 0),                    new Metric(0, 7, 7, 9, 0, 0),
                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(0, 7, 7, 9, 0, 0),                    new Metric(0, 7, 7, 9, 0, 0),                    new Metric(1, 5, 7, 9, -5, 0),
                    new Metric(1, 6, 7, 8, -1, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 7, 7, 9, 0, 0),                    new Metric(0, 4, 7, 11, -4, 0),
                    new Metric(0, 4, 7, 5, 2, 0),                    new Metric(3, 7, 7, 5, 2, 0),                    new Metric(3, 7, 7, 11, -4, 0),                    new Metric(0, 7, 7, 11, 2, 0),
                    new Metric(0, 7, 7, 11, -10, 0),                    new Metric(0, 7, 7, 8, -7, 0),                    new Metric(0, 7, 7, 5, -4, 0),                    new Metric(0, 7, 7, 2, -1, 0),
                    new Metric(0, 7, 7, -1, 2, 0),                    new Metric(3, 7, 7, 11, 2, 0),                    new Metric(0, 4, 7, 11, 2, 0),                    new Metric(0, 7, 7, 11, -4, 0),
                    new Metric(0, 7, 7, 5, 2, 0),                    new Metric(3, 4, 7, 11, 2, 0),                    new Metric(0, 6, 7, 7, 0, 0),                    new Metric(0, 6, 7, 7, 0, 0),
                    new Metric(0, 6, 7, 6, 0, 0),                    new Metric(0, 6, 7, 8, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(2, 4, 7, 5, -4, 0),
                    new Metric(0, 0, 7, 0, 0, 0),                    new Metric(3, 4, 7, 9, 0, 0),                    new Metric(2, 5, 7, 9, -6, 0),                    new Metric(1, 6, 7, 8, -1, 0),
                    new Metric(1, 6, 7, 8, -1, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 7, 0, 0),                    new Metric(3, 4, 7, 9, -6, 0),
                    new Metric(2, 5, 7, 9, 0, 0),                    new Metric(2, 5, 7, 9, 0, 0),                    new Metric(0, 6, 7, 7, -2, 0),                    new Metric(1, 6, 7, 7, -2, 0),
                    new Metric(1, 5, 7, 2, 1, 0),                    new Metric(1, 6, 7, 5, -4, 0),                    new Metric(2, 5, 7, 2, 1, 0),                    new Metric(1, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(2, 5, 7, 7, 1, 0),                    new Metric(1, 5, 7, 7, 1, 0),
                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 6, -2, 0),                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 1, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(1, 5, 7, 10, 1, 0),
                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(1, 5, 7, 10, 1, 0),                    new Metric(1, 6, 7, 9, -6, 0),                    new Metric(0, 6, 7, 0, 1, 0),
                    new Metric(2, 4, 7, 10, -8, 0),                    new Metric(0, 6, 7, 6, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 6, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 6, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 6, 2, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 8, 0, 0),                    new Metric(1, 6, 7, 8, 2, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 6, 0, 0),                    new Metric(0, 6, 7, 6, 0, 0),                    new Metric(0, 6, 7, 6, 0, 0),
                    new Metric(0, 6, 7, 6, 2, 0),                    new Metric(0, 6, 7, 6, 2, 0),                    new Metric(0, 6, 7, 6, 0, 0),                    new Metric(0, 6, 7, 6, 0, 0),
                    new Metric(0, 6, 7, 8, 0, 0),                    new Metric(0, 6, 7, 6, 0, 0),                    new Metric(1, 6, 7, 6, 0, 0),                    new Metric(1, 6, 7, 6, 0, 0),
                    new Metric(0, 6, 7, 6, 0, 0),                    new Metric(0, 6, 7, 6, 2, 0),                    new Metric(0, 6, 7, 6, 0, 0),                    new Metric(1, 6, 7, 10, 1, 0),
                    new Metric(3, 4, 7, 9, 0, 0),                    new Metric(1, 6, 7, 10, 1, 0),                    new Metric(1, 6, 7, 9, -6, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 7, 0, 0, 0),                    new Metric(3, 4, 7, 9, 0, 0),                    new Metric(1, 6, 7, 9, -1, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 7, -1, 0),                    new Metric(0, 5, 7, 9, 0, 0),                    new Metric(3, 4, 7, 9, 0, 0),                    new Metric(1, 5, 7, 10, 0, 0),
                    new Metric(1, 5, 7, 9, -7, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 9, -2, 0),                    new Metric(0, 6, 7, 8, -1, 0),
                    new Metric(1, 6, 7, 5, -2, 0),                    new Metric(1, 5, 7, 5, -4, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 9, -8, 0),
                    new Metric(1, 5, 7, 9, -5, 0),                    new Metric(1, 6, 7, 8, -1, 0),                    new Metric(1, 4, 7, 10, -4, 0),                    new Metric(1, 4, 7, 10, -4, 0),
                    new Metric(2, 4, 7, 10, -8, 0),                    new Metric(0, 6, 7, 6, 1, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(2, 4, 7, 5, -4, 0),
                    new Metric(2, 4, 7, 0, 2, 0),                    new Metric(1, 4, 7, 10, -4, 0),                    new Metric(1, 5, 7, 9, -3, 0),                    new Metric(0, 6, 7, 8, -1, 0),
                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),
                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 2, 0),
                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),
                    new Metric(1, 6, 7, 10, 0, 0),                    new Metric(1, 6, 7, 10, 0, 0),                    new Metric(1, 6, 7, 10, 0, 0),                    new Metric(1, 6, 7, 10, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),
                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 7, -1, 0),
                    new Metric(0, 6, 7, 10, 1, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),
                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(1, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 6, 0, 0),                    new Metric(0, 6, 7, 6, 2, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 10, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(1, 6, 7, 8, -1, 0),
                    new Metric(0, 6, 7, 7, 1, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 0, 0),
                    new Metric(0, 6, 7, 9, 0, 0),                    new Metric(0, 6, 7, 9, 2, 0),                    new Metric(0, 6, 7, 8, 2, 0),                    new Metric(0, 6, 7, 9, 2, 0),
            },
            new byte[][] {
                    bytes(53, 0, 33, 0, 33, 0, 33, 0, 43), // 0x00
                    bytes(8, 28, 62, 127, 62, 28, 8), // 0x01
                    bytes(85, 42, 85, 42, 85, 42, 85, 42, 85, 42, 85, 42, 85), // 0x02
                    bytes(5, 5, 7, 5, 117, 32, 32, 32, 32), // 0x03
                    bytes(7, 1, 3, 1, 29, 4, 12, 4, 4), // 0x04
                    bytes(6, 1, 1, 1, 54, 80, 48, 80, 80), // 0x05
                    bytes(1, 1, 1, 1, 119, 16, 48, 16, 16), // 0x06
                    bytes(6, 9, 9, 6), // 0x07
                    bytes(4, 4, 31, 4, 4, 0, 31), // 0x08
                    bytes(3, 5, 5, 5, 13, 8, 8, 8, 56), // 0x09
                    bytes(17, 17, 10, 10, 116, 32, 32, 32, 32), // 0x0a
                    bytes(8, 8, 8, 8, 8, 8, 15), // 0x0b
                    bytes(15, 8, 8, 8, 8, 8, 8), // 0x0c
                    bytes(15, 1, 1, 1, 1, 1, 1), // 0x0d
                    bytes(1, 1, 1, 1, 1, 1, 15), // 0x0e
                    bytes(8, 8, 8, 8, 8, 8, 127, 8, 8, 8, 8, 8, 8), // 0x0f
                    bytes(127), // 0x10
                    bytes(127), // 0x11
                    bytes(127), // 0x12
                    bytes(127), // 0x13
                    bytes(127), // 0x14
                    bytes(1, 1, 1, 1, 1, 1, 15, 1, 1, 1, 1, 1, 1), // 0x15
                    bytes(8, 8, 8, 8, 8, 8, 15, 8, 8, 8, 8, 8, 8), // 0x16
                    bytes(8, 8, 8, 8, 8, 8, 127), // 0x17
                    bytes(127, 8, 8, 8, 8, 8, 8), // 0x18
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // 0x19
                    bytes(48, 12, 3, 12, 48, 0, 63), // 0x1a
                    bytes(3, 12, 48, 12, 3, 0, 63), // 0x1b
                    bytes(63, 18, 18, 18, 18, 18), // 0x1c
                    bytes(32, 16, 63, 8, 4, 63, 2, 1), // 0x1d
                    bytes(28, 34, 2, 2, 7, 2, 2, 34, 29), // 0x1e
                    bytes(3), // 0x1f
                    null, // 0x20
                    bytes(1, 1, 1, 1, 1, 1, 1, 0, 1), // '!'
                    bytes(5, 5, 5), // '"'
                    bytes(10, 10, 31, 10, 31, 10, 10), // '#'
                    bytes(4, 30, 5, 14, 20, 15, 4), // '$'
                    bytes(34, 37, 18, 8, 8, 4, 18, 41, 17), // '%'
                    bytes(6, 9, 9, 6, 41, 17, 46), // '&'
                    bytes(1, 1, 1), // '''
                    bytes(4, 2, 2, 1, 1, 1, 2, 2, 4), // '('
                    bytes(1, 2, 2, 4, 4, 4, 2, 2, 1), // ')'
                    bytes(18, 12, 63, 12, 18), // '*'
                    bytes(4, 4, 31, 4, 4), // '+'
                    bytes(14, 6, 1), // ','
                    bytes(31), // '-'
                    bytes(2, 7, 2), // '.'
                    bytes(16, 16, 8, 8, 4, 2, 2, 1, 1), // '/'
                    bytes(12, 18, 33, 33, 33, 33, 33, 18, 12), // '0'
                    bytes(4, 6, 5, 4, 4, 4, 4, 4, 31), // '1'
                    bytes(30, 33, 33, 32, 16, 12, 2, 1, 63), // '2'
                    bytes(63, 32, 16, 8, 28, 32, 32, 33, 30), // '3'
                    bytes(16, 24, 20, 18, 17, 17, 63, 16, 16), // '4'
                    bytes(63, 1, 1, 29, 35, 32, 32, 33, 30), // '5'
                    bytes(28, 2, 1, 1, 29, 35, 33, 33, 30), // '6'
                    bytes(63, 32, 16, 8, 8, 4, 4, 2, 2), // '7'
                    bytes(30, 33, 33, 33, 30, 33, 33, 33, 30), // '8'
                    bytes(30, 33, 33, 49, 46, 32, 32, 16, 14), // '9'
                    bytes(2, 7, 2, 0, 0, 2, 7, 2), // ':'
                    bytes(4, 14, 4, 0, 0, 14, 6, 1), // ';'
                    bytes(16, 8, 4, 2, 1, 2, 4, 8, 16), // '<'
                    bytes(63, 0, 0, 63), // '='
                    bytes(1, 2, 4, 8, 16, 8, 4, 2, 1), // '>'
                    bytes(30, 33, 33, 32, 16, 8, 8, 0, 8), // '?'
                    bytes(30, 33, 33, 57, 37, 53, 41, 1, 30), // '@'
                    bytes(12, 18, 33, 33, 33, 63, 33, 33, 33), // 'A'
                    bytes(31, 34, 34, 34, 30, 34, 34, 34, 31), // 'B'
                    bytes(30, 33, 1, 1, 1, 1, 1, 33, 30), // 'C'
                    bytes(31, 34, 34, 34, 34, 34, 34, 34, 31), // 'D'
                    bytes(63, 1, 1, 1, 15, 1, 1, 1, 63), // 'E'
                    bytes(63, 1, 1, 1, 15, 1, 1, 1, 1), // 'F'
                    bytes(30, 33, 1, 1, 1, 57, 33, 49, 46), // 'G'
                    bytes(33, 33, 33, 33, 63, 33, 33, 33, 33), // 'H'
                    bytes(31, 4, 4, 4, 4, 4, 4, 4, 31), // 'I'
                    bytes(56, 16, 16, 16, 16, 16, 16, 17, 14), // 'J'
                    bytes(33, 17, 9, 5, 3, 5, 9, 17, 33), // 'K'
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 63), // 'L'
                    bytes(33, 51, 51, 45, 45, 33, 33, 33, 33), // 'M'
                    bytes(33, 33, 35, 37, 41, 49, 33, 33, 33), // 'N'
                    bytes(30, 33, 33, 33, 33, 33, 33, 33, 30), // 'O'
                    bytes(31, 33, 33, 33, 31, 1, 1, 1, 1), // 'P'
                    bytes(30, 33, 33, 33, 33, 33, 37, 41, 30, 32), // 'Q'
                    bytes(31, 33, 33, 33, 31, 5, 9, 17, 33), // 'R'
                    bytes(30, 33, 1, 1, 30, 32, 32, 33, 30), // 'S'
                    bytes(31, 4, 4, 4, 4, 4, 4, 4, 4), // 'T'
                    bytes(33, 33, 33, 33, 33, 33, 33, 33, 30), // 'U'
                    bytes(33, 33, 33, 18, 18, 18, 12, 12, 12), // 'V'
                    bytes(33, 33, 33, 33, 45, 45, 51, 51, 33), // 'W'
                    bytes(33, 33, 18, 18, 12, 18, 18, 33, 33), // 'X'
                    bytes(17, 17, 10, 10, 4, 4, 4, 4, 4), // 'Y'
                    bytes(63, 32, 16, 8, 12, 4, 2, 1, 63), // 'Z'
                    bytes(15, 1, 1, 1, 1, 1, 1, 1, 1, 1, 15), // '['
                    bytes(1, 1, 2, 2, 4, 8, 8, 16, 16), // '\'
                    bytes(15, 8, 8, 8, 8, 8, 8, 8, 8, 8, 15), // ']'
                    bytes(4, 10, 17), // '^'
                    bytes(63), // '_'
                    bytes(1, 2), // '`'
                    bytes(30, 32, 62, 33, 49, 46), // 'a'
                    bytes(1, 1, 1, 29, 35, 33, 33, 35, 29), // 'b'
                    bytes(30, 33, 1, 1, 33, 30), // 'c'
                    bytes(32, 32, 32, 46, 49, 33, 33, 49, 46), // 'd'
                    bytes(30, 33, 63, 1, 33, 30), // 'e'
                    bytes(28, 34, 2, 2, 15, 2, 2, 2, 2), // 'f'
                    bytes(46, 17, 17, 14, 1, 30, 33, 30), // 'g'
                    bytes(1, 1, 1, 29, 35, 33, 33, 33, 33), // 'h'
                    bytes(4, 0, 6, 4, 4, 4, 4, 31), // 'i'
                    bytes(16, 0, 24, 16, 16, 16, 16, 17, 17, 14), // 'j'
                    bytes(1, 1, 1, 17, 9, 7, 9, 17, 33), // 'k'
                    bytes(6, 4, 4, 4, 4, 4, 4, 4, 31), // 'l'
                    bytes(11, 21, 21, 21, 21, 17), // 'm'
                    bytes(29, 35, 33, 33, 33, 33), // 'n'
                    bytes(30, 33, 33, 33, 33, 30), // 'o'
                    bytes(29, 35, 33, 35, 29, 1, 1, 1), // 'p'
                    bytes(46, 49, 33, 49, 46, 32, 32, 32), // 'q'
                    bytes(29, 34, 2, 2, 2, 2), // 'r'
                    bytes(30, 33, 6, 24, 33, 30), // 's'
                    bytes(2, 2, 15, 2, 2, 2, 34, 28), // 't'
                    bytes(33, 33, 33, 33, 49, 46), // 'u'
                    bytes(17, 17, 17, 10, 10, 4), // 'v'
                    bytes(17, 17, 21, 21, 21, 10), // 'w'
                    bytes(33, 18, 12, 12, 18, 33), // 'x'
                    bytes(33, 33, 33, 49, 46, 32, 33, 30), // 'y'
                    bytes(63, 16, 8, 4, 2, 63), // 'z'
                    bytes(28, 2, 2, 2, 4, 3, 4, 2, 2, 2, 28), // '{'
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1), // '|'
                    bytes(7, 8, 8, 8, 4, 24, 4, 8, 8, 8, 7), // '}'
                    bytes(18, 21, 9), // '~'
                    null, // 0x7f
                    null, // 0x80
                    null, // 0x81
                    null, // 0x82
                    null, // 0x83
                    null, // 0x84
                    null, // 0x85
                    null, // 0x86
                    null, // 0x87
                    null, // 0x88
                    null, // 0x89
                    null, // 0x8a
                    null, // 0x8b
                    null, // 0x8c
                    null, // 0x8d
                    null, // 0x8e
                    null, // 0x8f
                    null, // 0x90
                    null, // 0x91
                    null, // 0x92
                    null, // 0x93
                    null, // 0x94
                    null, // 0x95
                    null, // 0x96
                    null, // 0x97
                    null, // 0x98
                    null, // 0x99
                    null, // 0x9a
                    null, // 0x9b
                    null, // 0x9c
                    null, // 0x9d
                    null, // 0x9e
                    null, // 0x9f
                    null, // 0xa0
                    bytes(1, 0, 1, 1, 1, 1, 1, 1, 1), // 0xa1
                    bytes(4, 14, 21, 5, 5, 21, 14, 4), // 0xa2
                    bytes(28, 34, 2, 2, 7, 2, 2, 34, 29), // 0xa3
                    bytes(33, 30, 18, 18, 30, 33), // 0xa4
                    bytes(17, 17, 10, 10, 31, 4, 31, 4, 4), // 0xa5
                    bytes(1, 1, 1, 1, 0, 1, 1, 1, 1), // 0xa6
                    bytes(6, 9, 1, 6, 9, 9, 6, 8, 9, 6), // 0xa7
                    bytes(9, 9), // 0xa8
                    bytes(30, 33, 45, 37, 37, 37, 45, 33, 30), // 0xa9
                    bytes(14, 16, 30, 17, 30, 0, 31), // 0xaa
                    bytes(40, 20, 10, 5, 10, 20, 40), // 0xab
                    bytes(31, 16, 16), // 0xac
                    bytes(15), // 0xad
                    bytes(30, 33, 45, 53, 53, 45, 53, 33, 30), // 0xae
                    bytes(31), // 0xaf
                    bytes(6, 9, 9, 6), // 0xb0
                    bytes(4, 4, 31, 4, 4, 0, 31), // 0xb1
                    bytes(2, 5, 4, 2, 1, 7), // 0xb2
                    bytes(7, 4, 2, 4, 5, 2), // 0xb3
                    bytes(2, 1), // 0xb4
                    bytes(33, 33, 33, 33, 51, 45, 1), // 0xb5
                    bytes(62, 23, 23, 23, 22, 20, 20, 20, 20), // 0xb6
                    bytes(3), // 0xb7
                    bytes(2, 1), // 0xb8
                    bytes(2, 3, 2, 2, 2, 7), // 0xb9
                    bytes(6, 9, 9, 6, 0, 15), // 0xba
                    bytes(5, 10, 20, 40, 20, 10, 5), // 0xbb
                    bytes(2, 3, 2, 2, 34, 55, 40, 40, 56, 32), // 0xbc
                    bytes(2, 3, 2, 2, 18, 47, 32, 16, 8, 56), // 0xbd
                    bytes(7, 4, 2, 4, 37, 50, 40, 40, 56, 32), // 0xbe
                    bytes(4, 0, 4, 4, 2, 1, 33, 33, 30), // 0xbf
                    bytes(4, 8, 0, 12, 18, 33, 33, 63, 33, 33), // 0xc0
                    bytes(8, 4, 0, 12, 18, 33, 33, 63, 33, 33), // 0xc1
                    bytes(12, 18, 0, 12, 18, 33, 33, 63, 33, 33), // 0xc2
                    bytes(38, 25, 0, 12, 18, 33, 33, 63, 33, 33), // 0xc3
                    bytes(18, 18, 0, 12, 18, 33, 33, 63, 33, 33), // 0xc4
                    bytes(12, 18, 12, 12, 18, 33, 33, 63, 33, 33), // 0xc5
                    bytes(58, 5, 5, 5, 29, 7, 5, 5, 61), // 0xc6
                    bytes(30, 33, 1, 1, 1, 1, 1, 33, 30, 8, 4), // 0xc7
                    bytes(4, 8, 0, 63, 1, 1, 15, 1, 1, 63), // 0xc8
                    bytes(8, 4, 0, 63, 1, 1, 15, 1, 1, 63), // 0xc9
                    bytes(12, 18, 0, 63, 1, 1, 15, 1, 1, 63), // 0xca
                    bytes(18, 18, 0, 63, 1, 1, 15, 1, 1, 63), // 0xcb
                    bytes(2, 4, 0, 31, 4, 4, 4, 4, 4, 31), // 0xcc
                    bytes(4, 2, 0, 31, 4, 4, 4, 4, 4, 31), // 0xcd
                    bytes(4, 10, 0, 31, 4, 4, 4, 4, 4, 31), // 0xce
                    bytes(17, 17, 0, 31, 4, 4, 4, 4, 4, 31), // 0xcf
                    bytes(31, 34, 34, 34, 39, 34, 34, 34, 31), // 0xd0
                    bytes(38, 25, 0, 33, 35, 37, 37, 41, 49, 33), // 0xd1
                    bytes(4, 8, 0, 30, 33, 33, 33, 33, 33, 30), // 0xd2
                    bytes(8, 4, 0, 30, 33, 33, 33, 33, 33, 30), // 0xd3
                    bytes(12, 18, 0, 30, 33, 33, 33, 33, 33, 30), // 0xd4
                    bytes(38, 25, 0, 30, 33, 33, 33, 33, 33, 30), // 0xd5
                    bytes(18, 18, 0, 30, 33, 33, 33, 33, 33, 30), // 0xd6
                    bytes(33, 18, 12, 12, 18, 33), // 0xd7
                    bytes(32, 30, 49, 41, 41, 37, 37, 37, 35, 30, 1), // 0xd8
                    bytes(4, 8, 0, 33, 33, 33, 33, 33, 33, 30), // 0xd9
                    bytes(8, 4, 0, 33, 33, 33, 33, 33, 33, 30), // 0xda
                    bytes(12, 18, 0, 33, 33, 33, 33, 33, 33, 30), // 0xdb
                    bytes(18, 18, 0, 33, 33, 33, 33, 33, 33, 30), // 0xdc
                    bytes(8, 4, 0, 17, 17, 10, 4, 4, 4, 4), // 0xdd
                    bytes(1, 31, 33, 33, 33, 31, 1, 1, 1), // 0xde
                    bytes(6, 9, 9, 5, 5, 9, 17, 17, 13), // 0xdf
                    bytes(4, 8, 0, 30, 32, 62, 33, 49, 46), // 0xe0
                    bytes(8, 4, 0, 30, 32, 62, 33, 49, 46), // 0xe1
                    bytes(12, 18, 0, 30, 32, 62, 33, 49, 46), // 0xe2
                    bytes(38, 25, 0, 30, 32, 62, 33, 49, 46), // 0xe3
                    bytes(18, 18, 0, 30, 32, 62, 33, 49, 46), // 0xe4
                    bytes(12, 18, 12, 0, 30, 32, 62, 33, 49, 46), // 0xe5
                    bytes(22, 40, 62, 9, 41, 22), // 0xe6
                    bytes(30, 33, 1, 1, 33, 30, 8, 4), // 0xe7
                    bytes(4, 8, 0, 30, 33, 63, 1, 33, 30), // 0xe8
                    bytes(8, 4, 0, 30, 33, 63, 1, 33, 30), // 0xe9
                    bytes(12, 18, 0, 30, 33, 63, 1, 33, 30), // 0xea
                    bytes(18, 18, 0, 30, 33, 63, 1, 33, 30), // 0xeb
                    bytes(2, 4, 0, 6, 4, 4, 4, 4, 31), // 0xec
                    bytes(4, 2, 0, 6, 4, 4, 4, 4, 31), // 0xed
                    bytes(6, 9, 0, 6, 4, 4, 4, 4, 31), // 0xee
                    bytes(9, 9, 0, 6, 4, 4, 4, 4, 31), // 0xef
                    bytes(18, 12, 10, 16, 30, 33, 33, 33, 33, 30), // 0xf0
                    bytes(38, 25, 0, 29, 35, 33, 33, 33, 33), // 0xf1
                    bytes(4, 8, 0, 30, 33, 33, 33, 33, 30), // 0xf2
                    bytes(8, 4, 0, 30, 33, 33, 33, 33, 30), // 0xf3
                    bytes(12, 18, 0, 30, 33, 33, 33, 33, 30), // 0xf4
                    bytes(38, 25, 0, 30, 33, 33, 33, 33, 30), // 0xf5
                    bytes(18, 18, 0, 30, 33, 33, 33, 33, 30), // 0xf6
                    bytes(4, 4, 0, 31, 0, 4, 4), // 0xf7
                    bytes(32, 30, 49, 41, 37, 35, 30, 1), // 0xf8
                    bytes(4, 8, 0, 33, 33, 33, 33, 49, 46), // 0xf9
                    bytes(8, 4, 0, 33, 33, 33, 33, 49, 46), // 0xfa
                    bytes(12, 18, 0, 33, 33, 33, 33, 49, 46), // 0xfb
                    bytes(18, 18, 0, 33, 33, 33, 33, 49, 46), // 0xfc
                    bytes(8, 4, 0, 33, 33, 33, 49, 46, 32, 33, 30), // 0xfd
                    bytes(1, 1, 29, 35, 33, 33, 35, 29, 1, 1), // 0xfe
                    bytes(18, 18, 0, 33, 33, 33, 49, 46, 32, 33, 30), // 0xff
            },
            new String[] {"FONTNAME_REGISTRY", "FOUNDRY", "FAMILY_NAME", "WEIGHT_NAME", "SLANT", "SETWIDTH_NAME", "ADD_STYLE_NAME", "PIXEL_SIZE", "POINT_SIZE", "RESOLUTION_X", "RESOLUTION_Y", "SPACING", "AVERAGE_WIDTH", "CHARSET_REGISTRY", "CHARSET_ENCODING", "COPYRIGHT", "DESTINATION", "CAP_HEIGHT", "X_HEIGHT", "_GBDFED_INFO", "FONT", "WEIGHT", "RESOLUTION", "QUAD_WIDTH"},
            new int[] {87, 89, 90, 92, 94, 235, 87, 13, 120, 75, 75, 102, 70, 105, 107, 108, 1, 9, 6, 110, 237, 10, 107, 7},
            new String[] {"", "Misc", "Fixed", "Medium", "R", "Normal", "", null, null, null, null, "C", null, "ISO8859", "1", "Public domain font.  Share and enjoy.", null, null, null, "Edited with gbdfed 1.3.", "-Misc-Fixed-Medium-R-Normal--13-120-75-75-C-70-ISO8859-1", null, null, null});
    }

    /** 9x15 - -Misc-Fixed-Medium-R-Normal--15-140-75-75-C-90-ISO8859-1, captured from a reference server. */
    private static final Font FONT_9X15 = font_9x15Font();

    private static Font font_9x15Font() {
        return new Font(
            "9x15",
            "-Misc-Fixed-Medium-R-Normal--15-140-75-75-C-90-ISO8859-1",
            12, 3, 0, 0, 255,
            new Metric(0, 0, 9, -2, -11, 0),
            new Metric(4, 9, 9, 12, 3, 0),
            new Metric[] {
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(0, 9, 9, 9, 0, 0),                    new Metric(0, 9, 9, 12, 3, 0),                    new Metric(1, 8, 9, 11, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(3, 7, 9, 10, -6, 0),
                    new Metric(1, 8, 9, 10, -1, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(0, 5, 9, 12, -4, 0),
                    new Metric(0, 5, 9, 5, 3, 0),                    new Metric(4, 9, 9, 5, 3, 0),                    new Metric(4, 9, 9, 12, -4, 0),                    new Metric(0, 9, 9, 12, 3, 0),
                    new Metric(0, 9, 9, 12, -11, 0),                    new Metric(0, 9, 9, 9, -8, 0),                    new Metric(0, 9, 9, 5, -4, 0),                    new Metric(0, 9, 9, 1, 0, 0),
                    new Metric(0, 9, 9, -2, 3, 0),                    new Metric(4, 9, 9, 12, 3, 0),                    new Metric(0, 5, 9, 12, 3, 0),                    new Metric(0, 9, 9, 12, -4, 0),
                    new Metric(0, 9, 9, 5, 3, 0),                    new Metric(4, 5, 9, 12, 3, 0),                    new Metric(1, 8, 9, 8, -1, 0),                    new Metric(1, 8, 9, 8, -1, 0),
                    new Metric(1, 8, 9, 7, 0, 0),                    new Metric(1, 8, 9, 8, -1, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(4, 6, 9, 6, -4, 0),
                    new Metric(0, 0, 9, 0, 0, 0),                    new Metric(4, 5, 9, 11, 0, 0),                    new Metric(3, 7, 9, 10, -7, 0),                    new Metric(1, 7, 9, 9, -1, 0),
                    new Metric(1, 8, 9, 10, 1, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(4, 5, 9, 10, -7, 0),
                    new Metric(3, 6, 9, 11, 1, 0),                    new Metric(3, 6, 9, 11, 1, 0),                    new Metric(1, 8, 9, 10, -3, 0),                    new Metric(1, 8, 9, 8, -1, 0),
                    new Metric(4, 6, 9, 2, 3, 0),                    new Metric(1, 8, 9, 5, -4, 0),                    new Metric(4, 6, 9, 2, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(4, 6, 9, 7, 0, 0),                    new Metric(4, 6, 9, 7, 3, 0),
                    new Metric(2, 7, 9, 10, 0, 0),                    new Metric(1, 8, 9, 6, -2, 0),                    new Metric(2, 7, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(2, 7, 9, 10, 0, 0),                    new Metric(1, 9, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 2, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(3, 7, 9, 11, 1, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(2, 6, 9, 11, 1, 0),                    new Metric(1, 8, 9, 10, -6, 0),                    new Metric(0, 8, 9, 0, 1, 0),
                    new Metric(3, 6, 9, 11, -8, 0),                    new Metric(1, 8, 9, 7, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 7, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 7, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 7, 3, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(2, 7, 9, 10, 0, 0),                    new Metric(1, 7, 9, 10, 3, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(2, 7, 9, 10, 0, 0),                    new Metric(1, 8, 9, 7, 0, 0),                    new Metric(1, 8, 9, 7, 0, 0),                    new Metric(1, 8, 9, 7, 0, 0),
                    new Metric(1, 8, 9, 7, 3, 0),                    new Metric(1, 8, 9, 7, 3, 0),                    new Metric(1, 8, 9, 7, 0, 0),                    new Metric(1, 8, 9, 7, 0, 0),
                    new Metric(1, 8, 9, 9, 0, 0),                    new Metric(1, 8, 9, 7, 0, 0),                    new Metric(1, 8, 9, 7, 0, 0),                    new Metric(1, 8, 9, 7, 0, 0),
                    new Metric(1, 8, 9, 7, 0, 0),                    new Metric(1, 7, 9, 7, 3, 0),                    new Metric(1, 8, 9, 7, 0, 0),                    new Metric(3, 8, 9, 11, 1, 0),
                    new Metric(4, 5, 9, 11, 1, 0),                    new Metric(1, 6, 9, 11, 1, 0),                    new Metric(1, 8, 9, 10, -7, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 9, 0, 0, 0),                    new Metric(4, 5, 9, 11, 0, 0),                    new Metric(1, 7, 9, 8, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 9, -3, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(4, 5, 9, 10, 1, 0),                    new Metric(2, 6, 9, 10, 1, 0),
                    new Metric(2, 7, 9, 11, -9, 0),                    new Metric(0, 8, 9, 10, -1, 0),                    new Metric(1, 6, 9, 10, -3, 0),                    new Metric(1, 8, 9, 9, -1, 0),
                    new Metric(1, 7, 9, 6, -2, 0),                    new Metric(2, 7, 9, 5, -4, 0),                    new Metric(0, 8, 9, 10, -1, 0),                    new Metric(1, 7, 9, 10, -9, 0),
                    new Metric(3, 7, 9, 10, -6, 0),                    new Metric(1, 8, 9, 10, -1, 0),                    new Metric(1, 5, 9, 10, -4, 0),                    new Metric(1, 5, 9, 10, -4, 0),
                    new Metric(3, 6, 9, 11, -8, 0),                    new Metric(1, 8, 9, 7, 2, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(4, 6, 9, 6, -4, 0),
                    new Metric(2, 6, 9, 0, 3, 0),                    new Metric(1, 4, 9, 10, -4, 0),                    new Metric(1, 6, 9, 10, -4, 0),                    new Metric(1, 8, 9, 9, -1, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 12, 0, 0),                    new Metric(1, 8, 9, 12, 0, 0),                    new Metric(1, 8, 9, 12, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),
                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 3, 0),
                    new Metric(1, 8, 9, 12, 0, 0),                    new Metric(1, 8, 9, 12, 0, 0),                    new Metric(1, 8, 9, 12, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),
                    new Metric(2, 7, 9, 12, 0, 0),                    new Metric(2, 7, 9, 12, 0, 0),                    new Metric(2, 7, 9, 12, 0, 0),                    new Metric(2, 7, 9, 11, 0, 0),
                    new Metric(0, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 12, 0, 0),                    new Metric(1, 8, 9, 12, 0, 0),
                    new Metric(1, 8, 9, 12, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 8, -1, 0),
                    new Metric(1, 8, 9, 11, 1, 0),                    new Metric(1, 8, 9, 12, 0, 0),                    new Metric(1, 8, 9, 12, 0, 0),                    new Metric(1, 8, 9, 12, 0, 0),
                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 12, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 7, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 7, 0, 0),                    new Metric(1, 8, 9, 7, 3, 0),
                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),
                    new Metric(2, 7, 9, 11, 0, 0),                    new Metric(2, 7, 9, 11, 0, 0),                    new Metric(1, 7, 9, 11, 0, 0),                    new Metric(2, 7, 9, 10, 0, 0),
                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),
                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 8, 9, 9, 0, 0),
                    new Metric(1, 8, 9, 8, 1, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),                    new Metric(1, 8, 9, 11, 0, 0),
                    new Metric(1, 8, 9, 10, 0, 0),                    new Metric(1, 7, 9, 11, 3, 0),                    new Metric(1, 8, 9, 9, 3, 0),                    new Metric(1, 7, 9, 10, 3, 0),
            },
            new byte[][] {
                    bytes(91, 64, 1, 65, 64, 1, 65, 64, 1, 109), // 0x00
                    bytes(16, 0, 56, 0, 124, 0, 254, 0, 255, 1, 254, 0, 124, 0, 56, 0, 16, 0), // 0x01
                    bytes(85, 1, 170, 0, 85, 1, 170, 0, 85, 1, 170, 0, 85, 1, 170, 0, 85, 1, 170, 0, 85, 1, 170, 0
                            , 85, 1, 170, 0, 85, 1), // 0x02
                    bytes(9, 9, 15, 9, 9, 0, 124, 16, 16, 16, 16), // 0x03
                    bytes(15, 1, 7, 1, 121, 9, 56, 8, 8, 8), // 0x04
                    bytes(14, 1, 1, 1, 14, 0, 56, 72, 56, 72, 72), // 0x05
                    bytes(1, 1, 1, 1, 15, 0, 120, 8, 56, 8, 8), // 0x06
                    bytes(6, 9, 9, 6), // 0x07
                    bytes(8, 8, 8, 127, 8, 8, 8, 0, 127), // 0x08
                    bytes(17, 19, 21, 25, 17, 0, 8, 8, 8, 8, 120), // 0x09
                    bytes(17, 17, 10, 10, 4, 0, 124, 16, 16, 16, 16), // 0x0a
                    bytes(16, 16, 16, 16, 16, 16, 16, 31), // 0x0b
                    bytes(31, 16, 16, 16, 16, 16, 16, 16), // 0x0c
                    bytes(31, 1, 1, 1, 1, 1, 1, 1), // 0x0d
                    bytes(1, 1, 1, 1, 1, 1, 1, 31), // 0x0e
                    bytes(16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 255, 1, 16, 0, 16, 0, 16, 0, 16, 0
                            , 16, 0, 16, 0, 16, 0), // 0x0f
                    bytes(255, 1), // 0x10
                    bytes(255, 1), // 0x11
                    bytes(255, 1), // 0x12
                    bytes(255, 1), // 0x13
                    bytes(255, 1), // 0x14
                    bytes(1, 1, 1, 1, 1, 1, 1, 31, 1, 1, 1, 1, 1, 1, 1), // 0x15
                    bytes(16, 16, 16, 16, 16, 16, 16, 31, 16, 16, 16, 16, 16, 16, 16), // 0x16
                    bytes(16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 255, 1), // 0x17
                    bytes(255, 1, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0), // 0x18
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // 0x19
                    bytes(96, 28, 3, 28, 96, 0, 127), // 0x1a
                    bytes(3, 28, 96, 28, 3, 0, 127), // 0x1b
                    bytes(127, 34, 34, 34, 34, 34, 34), // 0x1c
                    bytes(16, 16, 127, 8, 127, 4, 4), // 0x1d
                    bytes(56, 68, 4, 4, 31, 4, 4, 6, 69, 58), // 0x1e
                    bytes(3, 3), // 0x1f
                    null, // 0x20
                    bytes(1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1), // '!'
                    bytes(9, 9, 9), // '"'
                    bytes(18, 18, 63, 18, 18, 63, 18, 18), // '#'
                    bytes(8, 62, 73, 9, 10, 28, 40, 72, 73, 62, 8), // '$'
                    bytes(66, 37, 37, 18, 8, 8, 36, 82, 82, 33), // '%'
                    bytes(6, 9, 9, 9, 6, 70, 41, 17, 41, 70), // '&'
                    bytes(1, 1, 1), // '''
                    bytes(4, 2, 2, 1, 1, 1, 1, 1, 1, 2, 2, 4), // '('
                    bytes(1, 2, 2, 4, 4, 4, 4, 4, 4, 2, 2, 1), // ')'
                    bytes(8, 73, 42, 28, 42, 73, 8), // '*'
                    bytes(8, 8, 8, 127, 8, 8, 8), // '+'
                    bytes(3, 3, 2, 2, 1), // ','
                    bytes(127), // '-'
                    bytes(3, 3), // '.'
                    bytes(64, 32, 32, 16, 8, 8, 4, 2, 2, 1), // '/'
                    bytes(28, 34, 65, 65, 65, 65, 65, 65, 34, 28), // '0'
                    bytes(8, 12, 10, 9, 8, 8, 8, 8, 8, 127), // '1'
                    bytes(62, 65, 65, 32, 16, 8, 4, 2, 1, 127), // '2'
                    bytes(127, 64, 32, 16, 56, 64, 64, 64, 65, 62), // '3'
                    bytes(32, 48, 40, 36, 34, 33, 127, 32, 32, 32), // '4'
                    bytes(127, 1, 1, 61, 67, 64, 64, 64, 65, 62), // '5'
                    bytes(60, 2, 1, 1, 61, 67, 65, 65, 65, 62), // '6'
                    bytes(127, 64, 32, 32, 16, 16, 8, 8, 4, 4), // '7'
                    bytes(28, 34, 65, 34, 28, 34, 65, 65, 34, 28), // '8'
                    bytes(62, 65, 65, 65, 97, 94, 64, 64, 32, 30), // '9'
                    bytes(3, 3, 0, 0, 0, 3, 3), // ':'
                    bytes(3, 3, 0, 0, 0, 3, 3, 2, 2, 1), // ';'
                    bytes(16, 8, 4, 2, 1, 1, 2, 4, 8, 16), // '<'
                    bytes(127, 0, 0, 127), // '='
                    bytes(1, 2, 4, 8, 16, 16, 8, 4, 2, 1), // '>'
                    bytes(62, 65, 65, 64, 32, 16, 8, 8, 0, 8), // '?'
                    bytes(62, 65, 65, 121, 69, 101, 89, 1, 1, 62), // '@'
                    bytes(8, 20, 34, 65, 65, 65, 127, 65, 65, 65), // 'A'
                    bytes(31, 33, 65, 33, 31, 33, 65, 65, 33, 31), // 'B'
                    bytes(62, 65, 1, 1, 1, 1, 1, 1, 65, 62), // 'C'
                    bytes(31, 33, 65, 65, 65, 65, 65, 65, 33, 31), // 'D'
                    bytes(127, 1, 1, 1, 31, 1, 1, 1, 1, 127), // 'E'
                    bytes(127, 1, 1, 1, 31, 1, 1, 1, 1, 1), // 'F'
                    bytes(62, 65, 1, 1, 1, 113, 65, 65, 65, 62), // 'G'
                    bytes(65, 65, 65, 65, 127, 65, 65, 65, 65, 65), // 'H'
                    bytes(31, 4, 4, 4, 4, 4, 4, 4, 4, 31), // 'I'
                    bytes(248, 32, 32, 32, 32, 32, 32, 32, 33, 30), // 'J'
                    bytes(65, 33, 17, 9, 7, 5, 9, 17, 33, 65), // 'K'
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1, 127), // 'L'
                    bytes(65, 65, 99, 85, 85, 73, 73, 65, 65, 65), // 'M'
                    bytes(65, 65, 67, 69, 73, 81, 97, 65, 65, 65), // 'N'
                    bytes(62, 65, 65, 65, 65, 65, 65, 65, 65, 62), // 'O'
                    bytes(63, 65, 65, 65, 63, 1, 1, 1, 1, 1), // 'P'
                    bytes(62, 65, 65, 65, 65, 65, 65, 69, 73, 62, 16, 96), // 'Q'
                    bytes(63, 65, 65, 65, 63, 9, 17, 33, 65, 65), // 'R'
                    bytes(62, 65, 65, 1, 14, 48, 64, 65, 65, 62), // 'S'
                    bytes(127, 8, 8, 8, 8, 8, 8, 8, 8, 8), // 'T'
                    bytes(65, 65, 65, 65, 65, 65, 65, 65, 65, 62), // 'U'
                    bytes(65, 65, 65, 34, 34, 34, 20, 20, 20, 8), // 'V'
                    bytes(65, 65, 65, 65, 73, 73, 73, 73, 85, 34), // 'W'
                    bytes(65, 65, 34, 20, 8, 8, 20, 34, 65, 65), // 'X'
                    bytes(65, 65, 34, 20, 8, 8, 8, 8, 8, 8), // 'Y'
                    bytes(127, 64, 32, 16, 8, 4, 2, 1, 1, 127), // 'Z'
                    bytes(15, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 15), // '['
                    bytes(1, 2, 2, 4, 8, 8, 16, 32, 32, 64), // '\'
                    bytes(15, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 15), // ']'
                    bytes(8, 20, 34, 65), // '^'
                    bytes(255), // '_'
                    bytes(1, 2, 4), // '`'
                    bytes(62, 64, 64, 126, 65, 97, 94), // 'a'
                    bytes(1, 1, 1, 61, 67, 65, 65, 65, 67, 61), // 'b'
                    bytes(62, 65, 1, 1, 1, 65, 62), // 'c'
                    bytes(64, 64, 64, 94, 97, 65, 65, 65, 97, 94), // 'd'
                    bytes(62, 65, 65, 127, 1, 1, 62), // 'e'
                    bytes(56, 68, 68, 4, 4, 31, 4, 4, 4, 4), // 'f'
                    bytes(94, 33, 33, 33, 30, 1, 62, 65, 65, 62), // 'g'
                    bytes(1, 1, 1, 61, 67, 65, 65, 65, 65, 65), // 'h'
                    bytes(6, 0, 0, 7, 4, 4, 4, 4, 4, 31), // 'i'
                    bytes(48, 0, 0, 56, 32, 32, 32, 32, 32, 33, 33, 33, 30), // 'j'
                    bytes(1, 1, 1, 65, 49, 13, 3, 13, 49, 65), // 'k'
                    bytes(7, 4, 4, 4, 4, 4, 4, 4, 4, 31), // 'l'
                    bytes(55, 73, 73, 73, 73, 73, 65), // 'm'
                    bytes(61, 67, 65, 65, 65, 65, 65), // 'n'
                    bytes(62, 65, 65, 65, 65, 65, 62), // 'o'
                    bytes(61, 67, 65, 65, 65, 67, 61, 1, 1, 1), // 'p'
                    bytes(94, 97, 65, 65, 65, 97, 94, 64, 64, 64), // 'q'
                    bytes(57, 70, 66, 2, 2, 2, 2), // 'r'
                    bytes(62, 65, 1, 62, 64, 65, 62), // 's'
                    bytes(4, 4, 63, 4, 4, 4, 4, 68, 56), // 't'
                    bytes(33, 33, 33, 33, 33, 33, 94), // 'u'
                    bytes(65, 65, 34, 34, 20, 20, 8), // 'v'
                    bytes(65, 65, 73, 73, 73, 85, 34), // 'w'
                    bytes(65, 34, 20, 8, 20, 34, 65), // 'x'
                    bytes(33, 33, 33, 33, 33, 49, 46, 32, 33, 30), // 'y'
                    bytes(127, 32, 16, 8, 4, 2, 127), // 'z'
                    bytes(28, 2, 2, 2, 4, 3, 3, 4, 2, 2, 2, 28), // '{'
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // '|'
                    bytes(7, 8, 8, 8, 4, 24, 24, 4, 8, 8, 8, 7), // '}'
                    bytes(70, 73, 49), // '~'
                    null, // 0x7f
                    null, // 0x80
                    null, // 0x81
                    null, // 0x82
                    null, // 0x83
                    null, // 0x84
                    null, // 0x85
                    null, // 0x86
                    null, // 0x87
                    null, // 0x88
                    null, // 0x89
                    null, // 0x8a
                    null, // 0x8b
                    null, // 0x8c
                    null, // 0x8d
                    null, // 0x8e
                    null, // 0x8f
                    null, // 0x90
                    null, // 0x91
                    null, // 0x92
                    null, // 0x93
                    null, // 0x94
                    null, // 0x95
                    null, // 0x96
                    null, // 0x97
                    null, // 0x98
                    null, // 0x99
                    null, // 0x9a
                    null, // 0x9b
                    null, // 0x9c
                    null, // 0x9d
                    null, // 0x9e
                    null, // 0x9f
                    null, // 0xa0
                    bytes(1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1), // 0xa1
                    bytes(16, 30, 41, 9, 5, 37, 30, 2), // 0xa2
                    bytes(56, 68, 4, 4, 31, 4, 4, 6, 69, 58), // 0xa3
                    bytes(65, 62, 34, 34, 62, 65), // 0xa4
                    bytes(65, 65, 34, 20, 62, 8, 62, 8, 8, 8), // 0xa5
                    bytes(1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1), // 0xa6
                    bytes(6, 9, 1, 6, 9, 9, 9, 6, 8, 9, 6), // 0xa7
                    bytes(17, 17), // 0xa8
                    bytes(60, 66, 153, 165, 133, 165, 153, 66, 60), // 0xa9
                    bytes(6, 9, 14, 9, 30, 0, 31), // 0xaa
                    bytes(72, 36, 18, 9, 9, 18, 36, 72), // 0xab
                    bytes(63, 32, 32, 32), // 0xac
                    bytes(31), // 0xad
                    bytes(60, 66, 157, 165, 157, 149, 165, 66, 60), // 0xae
                    bytes(63), // 0xaf
                    bytes(6, 9, 9, 6), // 0xb0
                    bytes(8, 8, 8, 127, 8, 8, 8, 0, 127), // 0xb1
                    bytes(6, 9, 8, 6, 1, 15), // 0xb2
                    bytes(6, 9, 4, 8, 9, 6), // 0xb3
                    bytes(4, 2, 1), // 0xb4
                    bytes(65, 65, 65, 65, 65, 99, 93, 1, 1), // 0xb5
                    bytes(126, 81, 81, 81, 94, 80, 80, 80, 80, 80), // 0xb6
                    bytes(3, 3), // 0xb7
                    bytes(12, 9, 6), // 0xb8
                    bytes(2, 3, 2, 2, 2, 7), // 0xb9
                    bytes(14, 17, 17, 14, 0, 31), // 0xba
                    bytes(9, 18, 36, 72, 72, 36, 18, 9), // 0xbb
                    bytes(2, 3, 2, 2, 66, 103, 80, 72, 88, 96), // 0xbc
                    bytes(2, 3, 2, 2, 50, 79, 64, 48, 8, 120), // 0xbd
                    bytes(6, 9, 4, 8, 73, 102, 80, 72, 88, 96), // 0xbe
                    bytes(8, 0, 8, 8, 4, 2, 1, 65, 65, 62), // 0xbf
                    bytes(2, 4, 8, 0, 28, 34, 65, 65, 127, 65, 65, 65), // 0xc0
                    bytes(32, 16, 8, 0, 28, 34, 65, 65, 127, 65, 65, 65), // 0xc1
                    bytes(8, 20, 34, 0, 28, 34, 65, 65, 127, 65, 65, 65), // 0xc2
                    bytes(70, 57, 0, 28, 34, 65, 65, 127, 65, 65, 65), // 0xc3
                    bytes(34, 34, 0, 28, 34, 65, 65, 127, 65, 65, 65), // 0xc4
                    bytes(28, 34, 28, 20, 34, 65, 65, 127, 65, 65, 65), // 0xc5
                    bytes(118, 9, 9, 9, 9, 63, 9, 9, 9, 121), // 0xc6
                    bytes(62, 65, 1, 1, 1, 1, 1, 1, 65, 62, 24, 18, 12), // 0xc7
                    bytes(2, 4, 8, 0, 127, 2, 2, 30, 2, 2, 2, 127), // 0xc8
                    bytes(32, 16, 8, 0, 127, 2, 2, 30, 2, 2, 2, 127), // 0xc9
                    bytes(8, 20, 34, 0, 127, 2, 2, 30, 2, 2, 2, 127), // 0xca
                    bytes(34, 34, 0, 127, 2, 2, 30, 2, 2, 2, 127), // 0xcb
                    bytes(1, 2, 4, 0, 31, 4, 4, 4, 4, 4, 4, 31), // 0xcc
                    bytes(16, 8, 4, 0, 31, 4, 4, 4, 4, 4, 4, 31), // 0xcd
                    bytes(4, 10, 17, 0, 31, 4, 4, 4, 4, 4, 4, 31), // 0xce
                    bytes(17, 17, 0, 31, 4, 4, 4, 4, 4, 4, 31), // 0xcf
                    bytes(62, 66, 130, 130, 135, 130, 130, 130, 66, 62), // 0xd0
                    bytes(70, 57, 0, 65, 67, 69, 73, 73, 81, 97, 65), // 0xd1
                    bytes(2, 4, 8, 0, 62, 65, 65, 65, 65, 65, 65, 62), // 0xd2
                    bytes(32, 16, 8, 0, 62, 65, 65, 65, 65, 65, 65, 62), // 0xd3
                    bytes(8, 20, 34, 0, 62, 65, 65, 65, 65, 65, 65, 62), // 0xd4
                    bytes(70, 57, 0, 62, 65, 65, 65, 65, 65, 65, 62), // 0xd5
                    bytes(34, 34, 0, 62, 65, 65, 65, 65, 65, 65, 62), // 0xd6
                    bytes(65, 34, 20, 8, 20, 34, 65), // 0xd7
                    bytes(64, 62, 97, 81, 81, 73, 73, 69, 69, 67, 62, 1), // 0xd8
                    bytes(2, 4, 8, 0, 65, 65, 65, 65, 65, 65, 65, 62), // 0xd9
                    bytes(32, 16, 8, 0, 65, 65, 65, 65, 65, 65, 65, 62), // 0xda
                    bytes(8, 20, 34, 0, 65, 65, 65, 65, 65, 65, 65, 62), // 0xdb
                    bytes(34, 34, 0, 65, 65, 65, 65, 65, 65, 65, 62), // 0xdc
                    bytes(32, 16, 8, 0, 65, 65, 34, 20, 8, 8, 8, 8), // 0xdd
                    bytes(1, 1, 63, 65, 65, 65, 63, 1, 1, 1), // 0xde
                    bytes(28, 34, 34, 18, 11, 18, 34, 34, 34, 26), // 0xdf
                    bytes(4, 8, 16, 0, 62, 64, 64, 126, 65, 97, 94), // 0xe0
                    bytes(32, 16, 8, 0, 62, 64, 64, 126, 65, 97, 94), // 0xe1
                    bytes(8, 20, 34, 0, 62, 64, 64, 126, 65, 97, 94), // 0xe2
                    bytes(76, 50, 0, 62, 64, 64, 126, 65, 97, 94), // 0xe3
                    bytes(34, 34, 0, 62, 64, 64, 126, 65, 97, 94), // 0xe4
                    bytes(24, 36, 24, 0, 62, 64, 64, 126, 65, 97, 94), // 0xe5
                    bytes(54, 73, 72, 62, 9, 73, 118), // 0xe6
                    bytes(62, 65, 1, 1, 1, 65, 62, 24, 18, 12), // 0xe7
                    bytes(2, 4, 8, 0, 62, 65, 65, 127, 1, 1, 62), // 0xe8
                    bytes(32, 16, 8, 0, 62, 65, 65, 127, 1, 1, 62), // 0xe9
                    bytes(8, 20, 34, 0, 62, 65, 65, 127, 1, 1, 62), // 0xea
                    bytes(34, 34, 0, 62, 65, 65, 127, 1, 1, 62), // 0xeb
                    bytes(1, 2, 4, 0, 7, 4, 4, 4, 4, 4, 31), // 0xec
                    bytes(8, 4, 2, 0, 7, 4, 4, 4, 4, 4, 31), // 0xed
                    bytes(4, 10, 17, 0, 14, 8, 8, 8, 8, 8, 62), // 0xee
                    bytes(9, 9, 0, 7, 4, 4, 4, 4, 4, 31), // 0xef
                    bytes(18, 12, 10, 16, 62, 65, 65, 65, 65, 65, 62), // 0xf0
                    bytes(70, 57, 0, 61, 67, 65, 65, 65, 65, 65), // 0xf1
                    bytes(2, 4, 8, 0, 62, 65, 65, 65, 65, 65, 62), // 0xf2
                    bytes(32, 16, 8, 0, 62, 65, 65, 65, 65, 65, 62), // 0xf3
                    bytes(8, 20, 34, 0, 62, 65, 65, 65, 65, 65, 62), // 0xf4
                    bytes(70, 57, 0, 62, 65, 65, 65, 65, 65, 62), // 0xf5
                    bytes(34, 34, 0, 62, 65, 65, 65, 65, 65, 62), // 0xf6
                    bytes(8, 28, 8, 0, 127, 0, 8, 28, 8), // 0xf7
                    bytes(64, 62, 81, 81, 73, 69, 69, 62, 1), // 0xf8
                    bytes(2, 4, 8, 0, 33, 33, 33, 33, 33, 33, 94), // 0xf9
                    bytes(32, 16, 8, 0, 33, 33, 33, 33, 33, 33, 94), // 0xfa
                    bytes(8, 20, 34, 0, 33, 33, 33, 33, 33, 33, 94), // 0xfb
                    bytes(18, 18, 0, 33, 33, 33, 33, 33, 33, 94), // 0xfc
                    bytes(16, 8, 4, 0, 33, 33, 33, 33, 33, 49, 46, 32, 33, 30), // 0xfd
                    bytes(1, 1, 1, 61, 67, 65, 65, 67, 61, 1, 1, 1), // 0xfe
                    bytes(18, 18, 0, 33, 33, 33, 33, 33, 49, 46, 32, 33, 30), // 0xff
            },
            new String[] {"FONTNAME_REGISTRY", "FOUNDRY", "FAMILY_NAME", "WEIGHT_NAME", "SLANT", "SETWIDTH_NAME", "ADD_STYLE_NAME", "PIXEL_SIZE", "POINT_SIZE", "RESOLUTION_X", "RESOLUTION_Y", "SPACING", "AVERAGE_WIDTH", "CHARSET_REGISTRY", "CHARSET_ENCODING", "COPYRIGHT", "CAP_HEIGHT", "X_HEIGHT", "_GBDFED_INFO", "FONT", "WEIGHT", "RESOLUTION", "QUAD_WIDTH"},
            new int[] {87, 89, 90, 92, 94, 235, 87, 15, 140, 75, 75, 102, 90, 105, 107, 108, 10, 7, 110, 238, 10, 103, 9},
            new String[] {"", "Misc", "Fixed", "Medium", "R", "Normal", "", null, null, null, null, "C", null, "ISO8859", "1", "Public domain font.  Share and enjoy.", null, null, "Edited with gbdfed 1.3.", "-Misc-Fixed-Medium-R-Normal--15-140-75-75-C-90-ISO8859-1", null, null, null});
    }

    /** 10x20 - -Misc-Fixed-Medium-R-Normal--20-200-75-75-C-100-ISO8859-1, captured from a reference server. */
    private static final Font FONT_10X20 = font_10x20Font();

    private static Font font_10x20Font() {
        return new Font(
            "10x20",
            "-Misc-Fixed-Medium-R-Normal--20-200-75-75-C-100-ISO8859-1",
            16, 4, 0, 0, 255,
            new Metric(0, 0, 10, -3, -15, 0),
            new Metric(4, 10, 10, 16, 4, 0),
            new Metric[] {
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 10, -2, 0),                    new Metric(0, 10, 10, 16, 4, 0),                    new Metric(1, 9, 10, 14, 0, 0),
                    new Metric(1, 9, 10, 14, 0, 0),                    new Metric(1, 9, 10, 14, 0, 0),                    new Metric(2, 9, 10, 14, 0, 0),                    new Metric(2, 8, 10, 13, -7, 0),
                    new Metric(1, 9, 10, 9, -2, 0),                    new Metric(1, 9, 10, 14, 0, 0),                    new Metric(1, 9, 10, 14, 0, 0),                    new Metric(0, 5, 10, 16, -6, 0),
                    new Metric(0, 5, 10, 7, 4, 0),                    new Metric(4, 10, 10, 7, 4, 0),                    new Metric(4, 10, 10, 16, -6, 0),                    new Metric(0, 10, 10, 16, 4, 0),
                    new Metric(0, 10, 10, 16, -15, 0),                    new Metric(0, 10, 10, 11, -10, 0),                    new Metric(0, 10, 10, 7, -6, 0),                    new Metric(0, 10, 10, 2, -1, 0),
                    new Metric(0, 10, 10, -3, 4, 0),                    new Metric(4, 10, 10, 16, 4, 0),                    new Metric(0, 5, 10, 16, 4, 0),                    new Metric(0, 10, 10, 16, -6, 0),
                    new Metric(0, 10, 10, 7, 4, 0),                    new Metric(4, 5, 10, 16, 4, 0),                    new Metric(0, 9, 10, 10, 0, 0),                    new Metric(0, 9, 10, 10, 0, 0),
                    new Metric(1, 9, 10, 8, 0, 0),                    new Metric(1, 9, 10, 10, 0, 0),                    new Metric(1, 10, 10, 12, 0, 0),                    new Metric(4, 7, 10, 8, -5, 0),
                    new Metric(0, 0, 10, 0, 0, 0),                    new Metric(4, 6, 10, 13, 0, 0),                    new Metric(2, 8, 10, 13, -9, 0),                    new Metric(1, 10, 10, 12, -1, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 10, 10, 12, 0, 0),                    new Metric(1, 10, 10, 13, 0, 0),                    new Metric(4, 6, 10, 13, -8, 0),
                    new Metric(3, 8, 10, 13, 0, 0),                    new Metric(2, 7, 10, 13, 0, 0),                    new Metric(1, 9, 10, 10, -3, 0),                    new Metric(1, 9, 10, 10, -3, 0),
                    new Metric(3, 7, 10, 2, 1, 0),                    new Metric(1, 9, 10, 7, -6, 0),                    new Metric(4, 7, 10, 3, 0, 0),                    new Metric(2, 9, 10, 12, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(4, 7, 10, 8, 0, 0),                    new Metric(3, 7, 10, 8, 1, 0),
                    new Metric(1, 8, 10, 13, 0, 0),                    new Metric(1, 9, 10, 9, -3, 0),                    new Metric(2, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 10, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 1, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(2, 8, 10, 13, 0, 0),
                    new Metric(2, 9, 10, 12, 0, 0),                    new Metric(2, 8, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, -9, 0),                    new Metric(1, 10, 10, 0, 1, 0),
                    new Metric(3, 7, 10, 13, -10, 0),                    new Metric(1, 9, 10, 8, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 8, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 8, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 8, 4, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 11, 0, 0),                    new Metric(2, 9, 10, 11, 4, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 8, 0, 0),                    new Metric(1, 9, 10, 8, 0, 0),                    new Metric(1, 9, 10, 8, 0, 0),
                    new Metric(1, 9, 10, 8, 4, 0),                    new Metric(1, 9, 10, 8, 4, 0),                    new Metric(1, 9, 10, 8, 0, 0),                    new Metric(1, 9, 10, 8, 0, 0),
                    new Metric(1, 9, 10, 11, 0, 0),                    new Metric(1, 9, 10, 8, 0, 0),                    new Metric(1, 9, 10, 8, 0, 0),                    new Metric(1, 9, 10, 8, 0, 0),
                    new Metric(1, 9, 10, 8, 0, 0),                    new Metric(1, 9, 10, 8, 4, 0),                    new Metric(2, 9, 10, 8, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(4, 6, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, -10, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 10, 0, 0, 0),                    new Metric(4, 6, 10, 13, 0, 0),                    new Metric(1, 8, 10, 12, 0, 0),                    new Metric(1, 10, 10, 12, 0, 0),
                    new Metric(0, 9, 10, 10, -1, 0),                    new Metric(1, 9, 10, 11, 0, 0),                    new Metric(4, 6, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(2, 8, 10, 13, -11, 0),                    new Metric(1, 9, 10, 11, 0, 0),                    new Metric(1, 9, 10, 13, -4, 0),                    new Metric(0, 9, 10, 11, 0, 0),
                    new Metric(1, 9, 10, 8, -4, 0),                    new Metric(2, 8, 10, 7, -6, 0),                    new Metric(1, 9, 10, 11, 0, 0),                    new Metric(1, 9, 10, 14, -13, 0),
                    new Metric(2, 8, 10, 13, -7, 0),                    new Metric(1, 9, 10, 9, -2, 0),                    new Metric(2, 7, 10, 13, -6, 0),                    new Metric(2, 7, 10, 13, -6, 0),
                    new Metric(3, 7, 10, 13, -10, 0),                    new Metric(1, 8, 10, 7, 3, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(4, 7, 10, 8, -5, 0),
                    new Metric(2, 7, 10, 0, 4, 0),                    new Metric(2, 6, 10, 13, -6, 0),                    new Metric(1, 8, 10, 13, -4, 0),                    new Metric(1, 10, 10, 11, 0, 0),
                    new Metric(1, 9, 10, 13, -1, 0),                    new Metric(1, 9, 10, 13, -1, 0),                    new Metric(1, 9, 10, 13, -1, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),
                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 16, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 4, 0),
                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),
                    new Metric(2, 8, 10, 15, 0, 0),                    new Metric(2, 8, 10, 15, 0, 0),                    new Metric(2, 8, 10, 15, 0, 0),                    new Metric(2, 8, 10, 15, 0, 0),
                    new Metric(0, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),
                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 8, 10, 8, 0, 0),
                    new Metric(1, 9, 10, 14, 1, 0),                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),
                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(1, 9, 10, 15, 0, 0),                    new Metric(2, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),
                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),
                    new Metric(1, 9, 10, 11, 0, 0),                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 8, 0, 0),                    new Metric(1, 9, 10, 8, 4, 0),
                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 11, 0, 0),
                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 11, 0, 0),
                    new Metric(1, 9, 10, 13, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),
                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 11, 0, 0),                    new Metric(1, 9, 10, 11, -1, 0),
                    new Metric(1, 9, 10, 9, 1, 0),                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),                    new Metric(1, 9, 10, 12, 0, 0),
                    new Metric(1, 9, 10, 11, 0, 0),                    new Metric(1, 9, 10, 12, 4, 0),                    new Metric(2, 9, 10, 13, 4, 0),                    new Metric(1, 9, 10, 11, 4, 0),
            },
            new byte[][] {
                    bytes(231, 129, 129, 0, 0, 129, 129, 129, 0, 0, 129, 129, 231), // 0x00
                    bytes(24, 60, 126, 255, 255, 126, 60, 24), // 0x01
                    bytes(170, 2, 85, 1, 170, 2, 85, 1, 170, 2, 85, 1, 170, 2, 85, 1, 170, 2, 85, 1, 170, 2, 85, 1
                            , 170, 2, 85, 1, 170, 2, 85, 1, 170, 2, 85, 1, 170, 2, 85, 1), // 0x02
                    bytes(51, 51, 51, 63, 51, 51, 51, 0, 252, 48, 48, 48, 48, 48), // 0x03
                    bytes(31, 3, 3, 15, 3, 3, 3, 248, 24, 24, 120, 24, 24, 24), // 0x04
                    bytes(30, 51, 3, 3, 51, 30, 0, 124, 204, 204, 124, 60, 108, 204), // 0x05
                    bytes(3, 3, 3, 3, 3, 31, 0, 124, 12, 12, 60, 12, 12, 12), // 0x06
                    bytes(12, 30, 51, 51, 30, 12), // 0x07
                    bytes(24, 24, 255, 24, 24, 0, 255), // 0x08
                    bytes(51, 55, 63, 63, 59, 51, 0, 24, 24, 24, 24, 24, 24, 248), // 0x09
                    bytes(51, 51, 51, 30, 30, 12, 12, 0, 252, 48, 48, 48, 48, 48), // 0x0a
                    bytes(16, 16, 16, 16, 16, 16, 16, 16, 16, 31), // 0x0b
                    bytes(31, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16), // 0x0c
                    bytes(63, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // 0x0d
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1, 63), // 0x0e
                    bytes(16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 255, 3, 16, 0, 16, 0
                            , 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0), // 0x0f
                    bytes(255, 3), // 0x10
                    bytes(255, 3), // 0x11
                    bytes(255, 3), // 0x12
                    bytes(255, 3), // 0x13
                    bytes(255, 3), // 0x14
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1, 63, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // 0x15
                    bytes(16, 16, 16, 16, 16, 16, 16, 16, 16, 31, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16), // 0x16
                    bytes(16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 255, 3), // 0x17
                    bytes(255, 3, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0, 16, 0), // 0x18
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // 0x19
                    bytes(192, 1, 112, 0, 28, 0, 7, 0, 28, 0, 112, 0, 192, 1, 0, 0, 0, 0, 255, 1), // 0x1a
                    bytes(7, 0, 28, 0, 112, 0, 192, 1, 112, 0, 28, 0, 7, 0, 0, 0, 0, 0, 255, 1), // 0x1b
                    bytes(255, 102, 102, 102, 102, 102, 102, 102), // 0x1c
                    bytes(96, 96, 48, 255, 24, 24, 255, 12, 6, 6), // 0x1d
                    bytes(120, 0, 204, 0, 204, 0, 12, 0, 12, 0, 63, 0, 12, 0, 12, 0, 12, 0, 31, 0, 181, 1, 231, 0
                            ), // 0x1e
                    bytes(7, 7, 7), // 0x1f
                    null, // 0x20
                    bytes(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 3, 3), // '!'
                    bytes(51, 51, 51, 18), // '"'
                    bytes(216, 0, 216, 0, 216, 0, 254, 1, 108, 0, 108, 0, 108, 0, 255, 0, 54, 0, 54, 0, 54, 0), // '#'
                    bytes(24, 126, 219, 27, 27, 27, 126, 216, 216, 216, 219, 126, 24), // '$'
                    bytes(206, 0, 219, 0, 123, 0, 110, 0, 48, 0, 48, 0, 24, 0, 24, 0, 236, 0, 188, 1, 182, 1, 230, 0
                            ), // '%'
                    bytes(28, 0, 54, 0, 54, 0, 54, 0, 30, 0, 12, 0, 14, 0, 27, 0, 179, 1, 227, 0, 99, 0, 247, 0
                            , 158, 1), // '&'
                    bytes(3, 3, 3, 3, 1), // '''
                    bytes(24, 12, 6, 6, 3, 3, 3, 3, 3, 6, 6, 12, 24), // '('
                    bytes(3, 6, 12, 12, 24, 24, 24, 24, 24, 12, 12, 6, 3), // ')'
                    bytes(102, 102, 60, 255, 60, 102, 102), // '*'
                    bytes(24, 24, 24, 255, 24, 24, 24), // '+'
                    bytes(14, 14, 7), // ','
                    bytes(255), // '-'
                    bytes(7, 7, 7), // '.'
                    bytes(96, 96, 48, 48, 24, 24, 12, 12, 6, 6, 3, 3), // '/'
                    bytes(24, 60, 102, 102, 195, 195, 195, 195, 195, 102, 102, 60, 24), // '0'
                    bytes(24, 28, 30, 27, 24, 24, 24, 24, 24, 24, 24, 24, 255), // '1'
                    bytes(60, 102, 195, 195, 192, 192, 96, 56, 12, 6, 3, 3, 255), // '2'
                    bytes(60, 102, 195, 195, 192, 96, 56, 96, 192, 195, 195, 102, 60), // '3'
                    bytes(64, 96, 112, 120, 108, 102, 99, 99, 255, 96, 96, 96, 96), // '4'
                    bytes(255, 3, 3, 3, 3, 59, 103, 192, 192, 192, 195, 102, 60), // '5'
                    bytes(60, 102, 67, 3, 3, 59, 103, 195, 195, 195, 195, 102, 60), // '6'
                    bytes(255, 192, 192, 96, 96, 48, 48, 24, 24, 12, 12, 6, 6), // '7'
                    bytes(60, 102, 195, 195, 195, 102, 60, 102, 195, 195, 195, 102, 60), // '8'
                    bytes(60, 102, 195, 195, 195, 195, 230, 220, 192, 192, 194, 102, 60), // '9'
                    bytes(7, 7, 0, 0, 0, 0, 7, 7), // ':'
                    bytes(14, 14, 0, 0, 0, 0, 14, 14, 7), // ';'
                    bytes(64, 96, 48, 24, 12, 6, 3, 6, 12, 24, 48, 96, 64), // '<'
                    bytes(255, 0, 0, 0, 0, 255), // '='
                    bytes(1, 3, 6, 12, 24, 48, 96, 48, 24, 12, 6, 3, 1), // '>'
                    bytes(60, 102, 195, 195, 195, 96, 48, 24, 24, 24, 0, 24, 24), // '?'
                    bytes(60, 102, 195, 243, 251, 219, 219, 219, 123, 51, 3, 198, 124), // '@'
                    bytes(24, 60, 102, 102, 195, 195, 195, 255, 195, 195, 195, 195, 195), // 'A'
                    bytes(31, 51, 99, 99, 99, 51, 63, 99, 195, 195, 195, 99, 63), // 'B'
                    bytes(60, 102, 195, 3, 3, 3, 3, 3, 3, 3, 195, 102, 60), // 'C'
                    bytes(63, 99, 195, 195, 195, 195, 195, 195, 195, 195, 195, 99, 63), // 'D'
                    bytes(255, 3, 3, 3, 3, 3, 63, 3, 3, 3, 3, 3, 255), // 'E'
                    bytes(255, 3, 3, 3, 3, 3, 63, 3, 3, 3, 3, 3, 3), // 'F'
                    bytes(60, 102, 195, 3, 3, 3, 243, 195, 195, 195, 195, 230, 188), // 'G'
                    bytes(195, 195, 195, 195, 195, 195, 255, 195, 195, 195, 195, 195, 195), // 'H'
                    bytes(255, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 255), // 'I'
                    bytes(248, 1, 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 99, 0, 99, 0, 54, 0
                            , 28, 0), // 'J'
                    bytes(195, 195, 99, 99, 51, 51, 31, 51, 51, 99, 99, 195, 195), // 'K'
                    bytes(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 255), // 'L'
                    bytes(195, 195, 231, 231, 255, 219, 219, 219, 219, 195, 195, 195, 195), // 'M'
                    bytes(195, 199, 199, 207, 207, 219, 219, 243, 243, 227, 227, 195, 195), // 'N'
                    bytes(60, 102, 195, 195, 195, 195, 195, 195, 195, 195, 195, 102, 60), // 'O'
                    bytes(63, 99, 195, 195, 195, 195, 99, 63, 3, 3, 3, 3, 3), // 'P'
                    bytes(60, 102, 195, 195, 195, 195, 195, 195, 195, 219, 243, 102, 124, 192), // 'Q'
                    bytes(63, 99, 195, 195, 195, 195, 99, 63, 51, 99, 99, 195, 195), // 'R'
                    bytes(60, 102, 195, 3, 3, 6, 60, 96, 192, 192, 195, 102, 60), // 'S'
                    bytes(255, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24), // 'T'
                    bytes(195, 195, 195, 195, 195, 195, 195, 195, 195, 195, 195, 102, 60), // 'U'
                    bytes(195, 195, 195, 195, 102, 102, 102, 60, 60, 60, 24, 24, 24), // 'V'
                    bytes(195, 195, 195, 195, 195, 219, 219, 219, 219, 231, 231, 195, 195), // 'W'
                    bytes(195, 195, 102, 102, 60, 60, 24, 60, 60, 102, 102, 195, 195), // 'X'
                    bytes(195, 195, 102, 102, 60, 60, 24, 24, 24, 24, 24, 24, 24), // 'Y'
                    bytes(255, 192, 192, 96, 48, 48, 24, 12, 12, 6, 3, 3, 255), // 'Z'
                    bytes(63, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 63), // '['
                    bytes(3, 3, 6, 6, 12, 12, 24, 24, 48, 48, 96, 96), // '\'
                    bytes(63, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 63), // ']'
                    bytes(24, 60, 102, 195), // '^'
                    bytes(255, 1), // '_'
                    bytes(3, 6, 12), // '`'
                    bytes(124, 198, 192, 254, 195, 195, 195, 190), // 'a'
                    bytes(3, 3, 3, 3, 3, 59, 103, 195, 195, 195, 195, 103, 59), // 'b'
                    bytes(124, 198, 3, 3, 3, 3, 198, 124), // 'c'
                    bytes(192, 192, 192, 192, 192, 220, 230, 195, 195, 195, 195, 230, 220), // 'd'
                    bytes(60, 102, 195, 255, 3, 3, 198, 124), // 'e'
                    bytes(120, 204, 204, 12, 12, 63, 12, 12, 12, 12, 12, 12, 12), // 'f'
                    bytes(190, 227, 99, 99, 99, 62, 3, 126, 195, 195, 195, 126), // 'g'
                    bytes(3, 3, 3, 3, 3, 59, 103, 195, 195, 195, 195, 195, 195), // 'h'
                    bytes(24, 24, 0, 30, 24, 24, 24, 24, 24, 24, 255), // 'i'
                    bytes(96, 96, 0, 120, 96, 96, 96, 96, 96, 96, 96, 99, 99, 99, 62), // 'j'
                    bytes(3, 3, 3, 3, 3, 99, 51, 27, 15, 31, 51, 99, 195), // 'k'
                    bytes(30, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 255), // 'l'
                    bytes(109, 255, 219, 219, 219, 219, 219, 219), // 'm'
                    bytes(59, 103, 195, 195, 195, 195, 195, 195), // 'n'
                    bytes(60, 102, 195, 195, 195, 195, 102, 60), // 'o'
                    bytes(59, 103, 195, 195, 195, 195, 103, 59, 3, 3, 3, 3), // 'p'
                    bytes(220, 230, 195, 195, 195, 195, 230, 220, 192, 192, 192, 192), // 'q'
                    bytes(123, 206, 6, 6, 6, 6, 6, 6), // 'r'
                    bytes(126, 195, 3, 126, 192, 192, 195, 126), // 's'
                    bytes(12, 12, 12, 63, 12, 12, 12, 12, 12, 204, 120), // 't'
                    bytes(195, 195, 195, 195, 195, 195, 230, 220), // 'u'
                    bytes(195, 195, 102, 102, 60, 60, 24, 24), // 'v'
                    bytes(195, 195, 195, 219, 219, 219, 255, 102), // 'w'
                    bytes(195, 102, 60, 24, 24, 60, 102, 195), // 'x'
                    bytes(195, 195, 195, 195, 195, 195, 230, 220, 192, 195, 102, 60), // 'y'
                    bytes(127, 96, 48, 24, 12, 6, 3, 127), // 'z'
                    bytes(240, 24, 24, 24, 24, 24, 15, 24, 24, 24, 24, 24, 240), // '{'
                    bytes(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3), // '|'
                    bytes(15, 24, 24, 24, 24, 24, 240, 24, 24, 24, 24, 24, 15), // '}'
                    bytes(206, 219, 115), // '~'
                    null, // 0x7f
                    null, // 0x80
                    null, // 0x81
                    null, // 0x82
                    null, // 0x83
                    null, // 0x84
                    null, // 0x85
                    null, // 0x86
                    null, // 0x87
                    null, // 0x88
                    null, // 0x89
                    null, // 0x8a
                    null, // 0x8b
                    null, // 0x8c
                    null, // 0x8d
                    null, // 0x8e
                    null, // 0x8f
                    null, // 0x90
                    null, // 0x91
                    null, // 0x92
                    null, // 0x93
                    null, // 0x94
                    null, // 0x95
                    null, // 0x96
                    null, // 0x97
                    null, // 0x98
                    null, // 0x99
                    null, // 0x9a
                    null, // 0x9b
                    null, // 0x9c
                    null, // 0x9d
                    null, // 0x9e
                    null, // 0x9f
                    null, // 0xa0
                    bytes(3, 3, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3), // 0xa1
                    bytes(24, 24, 60, 102, 67, 3, 3, 67, 102, 60, 24, 24), // 0xa2
                    bytes(120, 0, 204, 0, 204, 0, 12, 0, 12, 0, 63, 0, 12, 0, 12, 0, 12, 0, 31, 0, 181, 1, 231, 0
                            ), // 0xa3
                    bytes(1, 1, 187, 1, 254, 0, 198, 0, 198, 0, 198, 0, 254, 0, 187, 1, 1, 1), // 0xa4
                    bytes(129, 195, 102, 60, 126, 24, 126, 24, 24, 24, 24), // 0xa5
                    bytes(3, 3, 3, 3, 3, 0, 0, 0, 3, 3, 3, 3, 3), // 0xa6
                    bytes(60, 102, 99, 6, 30, 51, 102, 204, 120, 96, 198, 102, 60), // 0xa7
                    bytes(51, 51), // 0xa8
                    bytes(60, 102, 195, 189, 165, 133, 165, 189, 195, 102, 60), // 0xa9
                    bytes(124, 194, 192, 254, 195, 195, 190, 0, 255), // 0xaa
                    bytes(32, 1, 176, 1, 216, 0, 108, 0, 54, 0, 27, 0, 54, 0, 108, 0, 216, 0, 176, 1, 32, 1), // 0xab
                    bytes(255, 255, 192, 192), // 0xac
                    bytes(63), // 0xad
                    bytes(60, 102, 195, 189, 165, 189, 149, 181, 195, 102, 60), // 0xae
                    bytes(255), // 0xaf
                    bytes(12, 30, 51, 51, 30, 12), // 0xb0
                    bytes(24, 24, 255, 24, 24, 0, 255), // 0xb1
                    bytes(14, 27, 24, 12, 6, 3, 31), // 0xb2
                    bytes(14, 27, 24, 12, 24, 27, 14), // 0xb3
                    bytes(12, 6, 3), // 0xb4
                    bytes(99, 99, 99, 99, 99, 119, 95, 3, 3, 3), // 0xb5
                    bytes(254, 255, 223, 223, 223, 222, 216, 216, 216, 216, 216, 216, 216), // 0xb6
                    bytes(7, 7, 7), // 0xb7
                    bytes(12, 24, 27, 14), // 0xb8
                    bytes(6, 7, 6, 6, 6, 6, 15), // 0xb9
                    bytes(28, 54, 99, 99, 99, 54, 28, 0, 127), // 0xba
                    bytes(9, 0, 27, 0, 54, 0, 108, 0, 216, 0, 176, 1, 216, 0, 108, 0, 54, 0, 27, 0, 9, 0), // 0xbb
                    bytes(2, 3, 130, 66, 39, 16, 72, 100, 82, 121, 64, 64), // 0xbc
                    bytes(2, 3, 130, 66, 39, 16, 104, 148, 130, 65, 32, 240), // 0xbd
                    bytes(7, 8, 134, 72, 39, 16, 72, 100, 82, 249, 64, 64), // 0xbe
                    bytes(24, 24, 0, 24, 24, 24, 12, 6, 195, 195, 195, 102, 60), // 0xbf
                    bytes(6, 12, 24, 0, 24, 60, 102, 195, 195, 195, 255, 195, 195, 195, 195), // 0xc0
                    bytes(96, 48, 24, 0, 24, 60, 102, 195, 195, 195, 255, 195, 195, 195, 195), // 0xc1
                    bytes(24, 60, 102, 0, 24, 60, 102, 195, 195, 195, 255, 195, 195, 195, 195), // 0xc2
                    bytes(76, 126, 50, 0, 24, 60, 102, 195, 195, 195, 255, 195, 195, 195, 195), // 0xc3
                    bytes(102, 102, 0, 24, 60, 102, 102, 195, 195, 195, 255, 195, 195, 195, 195), // 0xc4
                    bytes(60, 102, 102, 60, 0, 24, 60, 102, 195, 195, 195, 255, 195, 195, 195, 195), // 0xc5
                    bytes(248, 60, 54, 54, 51, 51, 255, 51, 51, 51, 51, 51, 243), // 0xc6
                    bytes(60, 102, 195, 3, 3, 3, 3, 3, 3, 3, 195, 102, 60, 24, 48, 54, 28), // 0xc7
                    bytes(6, 12, 24, 0, 255, 3, 3, 3, 3, 63, 3, 3, 3, 3, 255), // 0xc8
                    bytes(48, 24, 12, 0, 255, 3, 3, 3, 3, 63, 3, 3, 3, 3, 255), // 0xc9
                    bytes(24, 60, 102, 0, 255, 3, 3, 3, 3, 63, 3, 3, 3, 3, 255), // 0xca
                    bytes(102, 102, 0, 0, 255, 3, 3, 3, 3, 63, 3, 3, 3, 3, 255), // 0xcb
                    bytes(6, 12, 24, 0, 63, 12, 12, 12, 12, 12, 12, 12, 12, 12, 63), // 0xcc
                    bytes(24, 12, 6, 0, 63, 12, 12, 12, 12, 12, 12, 12, 12, 12, 63), // 0xcd
                    bytes(12, 30, 51, 0, 63, 12, 12, 12, 12, 12, 12, 12, 12, 12, 63), // 0xce
                    bytes(51, 51, 0, 63, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 63), // 0xcf
                    bytes(126, 0, 198, 0, 134, 1, 134, 1, 134, 1, 134, 1, 159, 1, 134, 1, 134, 1, 134, 1, 134, 1, 198, 0
                            , 126, 0), // 0xd0
                    bytes(76, 126, 50, 0, 195, 199, 207, 207, 219, 219, 243, 243, 227, 195, 195), // 0xd1
                    bytes(12, 24, 48, 0, 60, 102, 195, 195, 195, 195, 195, 195, 195, 102, 60), // 0xd2
                    bytes(48, 24, 12, 0, 60, 102, 195, 195, 195, 195, 195, 195, 195, 102, 60), // 0xd3
                    bytes(24, 60, 102, 0, 60, 102, 195, 195, 195, 195, 195, 195, 195, 102, 60), // 0xd4
                    bytes(76, 126, 50, 0, 60, 102, 195, 195, 195, 195, 195, 195, 195, 102, 60), // 0xd5
                    bytes(102, 102, 0, 60, 102, 195, 195, 195, 195, 195, 195, 195, 195, 102, 60), // 0xd6
                    bytes(65, 99, 54, 28, 28, 54, 99, 65), // 0xd7
                    bytes(128, 124, 102, 227, 227, 211, 211, 211, 203, 203, 203, 199, 102, 62, 1), // 0xd8
                    bytes(12, 24, 48, 0, 195, 195, 195, 195, 195, 195, 195, 195, 195, 102, 60), // 0xd9
                    bytes(48, 24, 12, 0, 195, 195, 195, 195, 195, 195, 195, 195, 195, 102, 60), // 0xda
                    bytes(24, 60, 102, 0, 195, 195, 195, 195, 195, 195, 195, 195, 195, 102, 60), // 0xdb
                    bytes(102, 102, 0, 195, 195, 195, 195, 195, 195, 195, 195, 195, 195, 102, 60), // 0xdc
                    bytes(48, 24, 12, 0, 195, 195, 102, 102, 60, 60, 24, 24, 24, 24, 24), // 0xdd
                    bytes(3, 3, 3, 63, 99, 99, 99, 99, 99, 63, 3, 3, 3), // 0xde
                    bytes(56, 108, 198, 198, 102, 55, 54, 102, 198, 198, 198, 102, 54), // 0xdf
                    bytes(12, 24, 48, 0, 126, 195, 192, 254, 195, 195, 195, 190), // 0xe0
                    bytes(48, 24, 12, 0, 126, 195, 192, 254, 195, 195, 195, 190), // 0xe1
                    bytes(24, 60, 102, 0, 126, 195, 192, 254, 195, 195, 195, 190), // 0xe2
                    bytes(76, 126, 50, 0, 126, 195, 192, 254, 195, 195, 195, 190), // 0xe3
                    bytes(102, 102, 0, 126, 195, 192, 254, 195, 195, 195, 190), // 0xe4
                    bytes(60, 102, 102, 60, 0, 126, 195, 192, 254, 195, 195, 195, 190), // 0xe5
                    bytes(110, 217, 216, 120, 30, 27, 155, 118), // 0xe6
                    bytes(124, 198, 3, 3, 3, 3, 198, 124, 24, 48, 54, 28), // 0xe7
                    bytes(6, 12, 24, 0, 60, 102, 195, 255, 3, 3, 198, 124), // 0xe8
                    bytes(96, 48, 24, 0, 60, 102, 195, 255, 3, 3, 198, 124), // 0xe9
                    bytes(24, 60, 102, 0, 60, 102, 195, 255, 3, 3, 198, 124), // 0xea
                    bytes(102, 102, 0, 60, 102, 195, 255, 3, 3, 198, 124), // 0xeb
                    bytes(6, 12, 24, 0, 30, 24, 24, 24, 24, 24, 24, 255), // 0xec
                    bytes(48, 24, 12, 0, 30, 24, 24, 24, 24, 24, 24, 255), // 0xed
                    bytes(24, 60, 102, 0, 30, 24, 24, 24, 24, 24, 24, 255), // 0xee
                    bytes(102, 102, 0, 30, 24, 24, 24, 24, 24, 24, 255), // 0xef
                    bytes(17, 27, 14, 14, 27, 49, 124, 230, 195, 195, 195, 102, 60), // 0xf0
                    bytes(76, 126, 50, 0, 59, 103, 195, 195, 195, 195, 195, 195), // 0xf1
                    bytes(6, 12, 24, 0, 60, 102, 195, 195, 195, 195, 102, 60), // 0xf2
                    bytes(96, 48, 24, 0, 60, 102, 195, 195, 195, 195, 102, 60), // 0xf3
                    bytes(24, 60, 102, 0, 60, 102, 195, 195, 195, 195, 102, 60), // 0xf4
                    bytes(76, 126, 50, 0, 60, 102, 195, 195, 195, 195, 102, 60), // 0xf5
                    bytes(102, 102, 0, 60, 102, 195, 195, 195, 195, 102, 60), // 0xf6
                    bytes(24, 24, 0, 0, 255, 255, 0, 0, 24, 24), // 0xf7
                    bytes(128, 124, 102, 211, 211, 203, 203, 102, 62, 1), // 0xf8
                    bytes(12, 24, 48, 0, 195, 195, 195, 195, 195, 195, 230, 220), // 0xf9
                    bytes(96, 48, 24, 0, 195, 195, 195, 195, 195, 195, 230, 220), // 0xfa
                    bytes(24, 60, 102, 0, 195, 195, 195, 195, 195, 195, 230, 220), // 0xfb
                    bytes(102, 102, 0, 195, 195, 195, 195, 195, 195, 230, 220), // 0xfc
                    bytes(48, 24, 12, 0, 195, 195, 195, 195, 195, 195, 230, 220, 192, 195, 102, 60), // 0xfd
                    bytes(3, 3, 3, 3, 3, 3, 31, 51, 99, 99, 99, 51, 31, 3, 3, 3, 3), // 0xfe
                    bytes(102, 102, 0, 195, 195, 195, 195, 195, 195, 230, 220, 192, 195, 102, 60), // 0xff
            },
            new String[] {"FONTNAME_REGISTRY", "FOUNDRY", "FAMILY_NAME", "WEIGHT_NAME", "SLANT", "SETWIDTH_NAME", "ADD_STYLE_NAME", "PIXEL_SIZE", "POINT_SIZE", "RESOLUTION_X", "RESOLUTION_Y", "SPACING", "AVERAGE_WIDTH", "CHARSET_REGISTRY", "CHARSET_ENCODING", "X_HEIGHT", "CAP_HEIGHT", "COPYRIGHT", "_GBDFED_INFO", "FONT", "WEIGHT", "RESOLUTION", "QUAD_WIDTH"},
            new int[] {87, 89, 90, 92, 94, 235, 87, 20, 200, 75, 75, 102, 100, 105, 107, 8, 13, 108, 110, 239, 10, 103, 10},
            new String[] {"", "Misc", "Fixed", "Medium", "R", "Normal", "", null, null, null, null, "C", null, "ISO8859", "1", null, null, "Public domain font.  Share and enjoy.", "Edited with gbdfed 1.3.", "-Misc-Fixed-Medium-R-Normal--20-200-75-75-C-100-ISO8859-1", null, null, null});
    }

    /** 12x24 - -Sony-Fixed-Medium-R-Normal--24-170-100-100-C-120-ISO8859-1, captured from a reference server. */
    private static final Font FONT_12X24 = font_12x24Font();

    private static Font font_12x24Font() {
        return new Font(
            "12x24",
            "-Sony-Fixed-Medium-R-Normal--24-170-100-100-C-120-ISO8859-1",
            22, 2, 32, 1, 255,
            new Metric(0, 0, 12, 0, -19, 0),
            new Metric(5, 12, 12, 22, 2, 0),
            new Metric[] {
                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 12, 12, 22, -1, 0),                    new Metric(0, 10, 12, 20, -1, 0),                    new Metric(0, 10, 12, 20, -1, 0),
                    new Metric(0, 10, 12, 20, -1, 0),                    new Metric(0, 10, 12, 20, -1, 0),                    new Metric(2, 9, 12, 18, -12, 0),                    new Metric(0, 10, 12, 19, -2, 0),
                    new Metric(0, 10, 12, 20, -1, 0),                    new Metric(0, 10, 12, 20, -1, 0),                    new Metric(0, 6, 12, 22, -9, 0),                    new Metric(0, 6, 12, 10, 2, 0),
                    new Metric(5, 12, 12, 10, 2, 0),                    new Metric(5, 12, 12, 22, -9, 0),                    new Metric(0, 12, 12, 22, 2, 0),                    new Metric(0, 12, 12, 20, -19, 0),
                    new Metric(0, 12, 12, 15, -14, 0),                    new Metric(0, 12, 12, 10, -9, 0),                    new Metric(0, 12, 12, 5, -4, 0),                    new Metric(0, 12, 12, 0, 1, 0),
                    new Metric(5, 12, 12, 22, 2, 0),                    new Metric(0, 6, 12, 22, 2, 0),                    new Metric(0, 12, 12, 22, -9, 0),                    new Metric(0, 12, 12, 10, 2, 0),
                    new Metric(5, 6, 12, 22, 2, 0),                    new Metric(0, 10, 12, 15, -1, 0),                    new Metric(0, 10, 12, 15, -1, 0),                    new Metric(0, 11, 12, 11, -1, 0),
                    new Metric(0, 10, 12, 15, -2, 0),                    new Metric(1, 11, 12, 15, 0, 0),                    new Metric(4, 6, 12, 10, -8, 0),                    new Metric(0, 0, 12, 0, 0, 0),
                    new Metric(4, 7, 12, 20, -1, 0),                    new Metric(1, 9, 12, 22, -16, 0),                    new Metric(0, 11, 12, 21, 1, 0),                    new Metric(0, 11, 12, 21, 1, 0),
                    new Metric(0, 11, 12, 21, 1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(3, 6, 12, 22, -16, 0),                    new Metric(4, 10, 12, 22, 2, 0),
                    new Metric(1, 7, 12, 22, 2, 0),                    new Metric(0, 10, 12, 16, -4, 0),                    new Metric(1, 11, 12, 17, -3, 0),                    new Metric(3, 6, 12, 4, 2, 0),
                    new Metric(0, 11, 12, 11, -9, 0),                    new Metric(3, 6, 12, 4, -1, 0),                    new Metric(0, 11, 12, 21, 1, 0),                    new Metric(0, 11, 12, 20, 0, 0),
                    new Metric(1, 10, 12, 20, 0, 0),                    new Metric(0, 11, 12, 20, 0, 0),                    new Metric(0, 11, 12, 20, 0, 0),                    new Metric(0, 11, 12, 20, 0, 0),
                    new Metric(1, 11, 12, 20, 0, 0),                    new Metric(0, 11, 12, 20, 0, 0),                    new Metric(0, 11, 12, 20, 0, 0),                    new Metric(0, 11, 12, 20, 0, 0),
                    new Metric(0, 11, 12, 20, 0, 0),                    new Metric(4, 7, 12, 14, -1, 0),                    new Metric(4, 7, 12, 14, 2, 0),                    new Metric(1, 11, 12, 20, 0, 0),
                    new Metric(0, 11, 12, 14, -6, 0),                    new Metric(1, 11, 12, 20, 0, 0),                    new Metric(1, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, 0, 0),
                    new Metric(0, 12, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(1, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(2, 8, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(0, 12, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(0, 11, 12, 20, 2, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 10, 12, 20, -1, 0),
                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(4, 10, 12, 22, 2, 0),                    new Metric(0, 11, 12, 21, 1, 0),
                    new Metric(1, 7, 12, 22, 2, 0),                    new Metric(0, 11, 12, 22, -16, 0),                    new Metric(0, 11, 12, 0, 2, 0),                    new Metric(2, 9, 12, 22, -17, 0),
                    new Metric(0, 11, 12, 14, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 10, 12, 14, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(0, 11, 12, 14, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 15, 2, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(2, 8, 12, 20, -1, 0),                    new Metric(0, 8, 12, 20, 2, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(2, 8, 12, 20, -1, 0),
                    new Metric(0, 12, 12, 14, -1, 0),                    new Metric(0, 11, 12, 14, -1, 0),                    new Metric(0, 11, 12, 14, -1, 0),                    new Metric(0, 11, 12, 14, 2, 0),
                    new Metric(0, 11, 12, 14, 2, 0),                    new Metric(0, 11, 12, 14, -1, 0),                    new Metric(0, 11, 12, 14, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(0, 11, 12, 14, -1, 0),                    new Metric(0, 11, 12, 14, -1, 0),                    new Metric(0, 11, 12, 14, -1, 0),                    new Metric(0, 11, 12, 14, -1, 0),
                    new Metric(0, 11, 12, 14, 2, 0),                    new Metric(0, 11, 12, 14, -1, 0),                    new Metric(5, 10, 12, 22, 2, 0),                    new Metric(5, 7, 12, 22, 2, 0),
                    new Metric(1, 6, 12, 22, 2, 0),                    new Metric(0, 11, 12, 22, -18, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),                    new Metric(0, 0, 0, 0, 0, 0),
                    new Metric(4, 7, 12, 19, 0, 0),                    new Metric(0, 11, 12, 20, 0, 0),                    new Metric(1, 11, 12, 15, 0, 0),                    new Metric(0, 11, 12, 16, -5, 0),
                    new Metric(1, 11, 12, 19, -1, 0),                    new Metric(5, 7, 12, 20, -1, 0),                    new Metric(1, 11, 12, 20, -1, 0),                    new Metric(3, 9, 12, 20, -17, 0),
                    new Metric(0, 12, 12, 20, -1, 0),                    new Metric(3, 10, 12, 20, -9, 0),                    new Metric(1, 11, 12, 20, 0, 0),                    new Metric(0, 10, 12, 12, -7, 0),
                    new Metric(0, 10, 12, 12, -10, 0),                    new Metric(0, 12, 12, 20, -1, 0),                    new Metric(0, 11, 12, 19, -17, 0),                    new Metric(2, 9, 12, 20, -14, 0),
                    new Metric(1, 11, 12, 15, -2, 0),                    new Metric(3, 9, 12, 20, -11, 0),                    new Metric(3, 9, 12, 20, -11, 0),                    new Metric(3, 9, 12, 20, -16, 0),
                    new Metric(0, 11, 12, 10, 2, 0),                    new Metric(0, 12, 12, 20, -1, 0),                    new Metric(4, 7, 12, 12, -9, 0),                    new Metric(3, 9, 12, 3, 2, 0),
                    new Metric(3, 9, 12, 20, -11, 0),                    new Metric(3, 10, 12, 20, -9, 0),                    new Metric(1, 11, 12, 20, 0, 0),                    new Metric(0, 12, 12, 20, -1, 0),
                    new Metric(0, 12, 12, 20, -1, 0),                    new Metric(0, 12, 12, 20, -1, 0),                    new Metric(1, 11, 12, 20, -1, 0),                    new Metric(0, 12, 12, 22, -1, 0),
                    new Metric(0, 12, 12, 22, -1, 0),                    new Metric(0, 12, 12, 22, -1, 0),                    new Metric(0, 12, 12, 22, -1, 0),                    new Metric(0, 12, 12, 22, -1, 0),
                    new Metric(0, 12, 12, 22, -1, 0),                    new Metric(0, 12, 12, 20, -1, 0),                    new Metric(1, 11, 12, 20, 2, 0),                    new Metric(0, 11, 12, 22, -1, 0),
                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(2, 8, 12, 22, -1, 0),
                    new Metric(2, 8, 12, 22, -1, 0),                    new Metric(2, 8, 12, 22, -1, 0),                    new Metric(2, 8, 12, 22, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 11, 12, 22, -1, 0),
                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 11, 12, 15, -5, 0),                    new Metric(0, 11, 12, 21, 0, 0),
                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 11, 12, 22, -1, 0),
                    new Metric(0, 11, 12, 22, -1, 0),                    new Metric(0, 11, 12, 20, 0, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 19, -1, 0),                    new Metric(0, 11, 12, 18, -1, 0),
                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 14, -1, 0),                    new Metric(1, 11, 12, 15, 2, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 18, -1, 0),                    new Metric(0, 10, 12, 20, -1, 0),
                    new Metric(0, 10, 12, 20, -1, 0),                    new Metric(0, 10, 12, 20, -1, 0),                    new Metric(0, 10, 12, 18, -1, 0),                    new Metric(1, 11, 12, 19, -1, 0),
                    new Metric(0, 11, 12, 19, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),
                    new Metric(0, 11, 12, 18, -1, 0),                    new Metric(0, 11, 12, 18, -1, 0),                    new Metric(0, 10, 12, 15, -5, 0),                    new Metric(0, 11, 12, 14, -1, 0),
                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 20, -1, 0),                    new Metric(0, 11, 12, 18, -1, 0),
                    new Metric(0, 11, 12, 20, 2, 0),                    new Metric(0, 11, 12, 19, 2, 0),                    new Metric(0, 11, 12, 18, 2, 0),
            },
            new byte[][] {
                    bytes(32, 0, 32, 0, 112, 0, 112, 0, 248, 0, 248, 0, 252, 1, 252, 1, 254, 3, 254, 3, 255, 7, 254, 3
                            , 254, 3, 252, 1, 252, 1, 248, 0, 248, 0, 112, 0, 112, 0, 32, 0, 32, 0), // 0x00
                    bytes(99, 12, 99, 12, 99, 12, 12, 3, 12, 3, 12, 3, 99, 12, 99, 12, 99, 12, 12, 3, 12, 3, 12, 3
                            , 99, 12, 99, 12, 99, 12, 12, 3, 12, 3, 12, 3, 99, 12, 99, 12, 99, 12), // 0x01
                    bytes(99, 0, 99, 0, 99, 0, 99, 0, 127, 0, 99, 0, 99, 0, 99, 0, 99, 0, 0, 0, 0, 0, 252, 3
                            , 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 96, 0), // 0x02
                    bytes(127, 0, 3, 0, 3, 0, 3, 0, 63, 0, 3, 0, 3, 0, 3, 0, 3, 0, 0, 0, 248, 3, 24, 0
                            , 24, 0, 24, 0, 248, 1, 24, 0, 24, 0, 24, 0, 24, 0), // 0x03
                    bytes(60, 0, 102, 0, 99, 0, 3, 0, 3, 0, 3, 0, 3, 0, 102, 0, 60, 0, 0, 0, 248, 1, 24, 3
                            , 24, 3, 24, 3, 248, 1, 152, 0, 152, 1, 24, 3, 24, 3), // 0x04
                    bytes(3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 127, 0, 0, 0, 248, 3, 24, 0
                            , 24, 0, 24, 0, 248, 1, 24, 0, 24, 0, 24, 0, 24, 0), // 0x05
                    bytes(28, 54, 99, 99, 54, 28), // 0x06
                    bytes(48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 255, 3, 255, 3, 48, 0, 48, 0, 48, 0, 48, 0
                            , 48, 0, 48, 0, 0, 0, 255, 3, 255, 3), // 0x07
                    bytes(199, 0, 199, 0, 203, 0, 203, 0, 211, 0, 211, 0, 227, 0, 227, 0, 195, 0, 0, 0, 24, 0, 24, 0
                            , 24, 0, 24, 0, 24, 0, 24, 0, 24, 0, 24, 0, 248, 3), // 0x08
                    bytes(131, 1, 131, 1, 198, 0, 198, 0, 108, 0, 108, 0, 56, 0, 56, 0, 16, 0, 0, 0, 0, 0, 252, 3
                            , 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 96, 0), // 0x09
                    bytes(32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 63), // 0x0a
                    bytes(63, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32), // 0x0b
                    bytes(127, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), // 0x0c
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 127), // 0x0d
                    bytes(32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0
                            , 255, 15, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0
                            ), // 0x0e
                    bytes(255, 15), // 0x0f
                    bytes(255, 15), // 0x10
                    bytes(255, 15), // 0x11
                    bytes(255, 15), // 0x12
                    bytes(255, 15), // 0x13
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 127, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
                            ), // 0x14
                    bytes(32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 63, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32
                            ), // 0x15
                    bytes(32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0
                            , 255, 15), // 0x16
                    bytes(255, 15, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0, 32, 0
                            ), // 0x17
                    bytes(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
                            ), // 0x18
                    bytes(0, 2, 0, 3, 192, 1, 112, 0, 28, 0, 7, 0, 28, 0, 112, 0, 192, 1, 0, 3, 0, 2, 255, 3
                            , 0, 0, 255, 3), // 0x19
                    bytes(1, 0, 3, 0, 14, 0, 56, 0, 224, 0, 128, 3, 224, 0, 56, 0, 14, 0, 3, 0, 1, 0, 255, 3
                            , 0, 0, 255, 3), // 0x1a
                    bytes(255, 7, 140, 1, 140, 1, 140, 1, 140, 1, 140, 1, 142, 1, 134, 1, 131, 1, 131, 1), // 0x1b
                    bytes(0, 3, 128, 1, 128, 0, 64, 0, 255, 3, 32, 0, 48, 0, 16, 0, 255, 3, 8, 0, 4, 0, 6, 0
                            , 3, 0), // 0x1c
                    bytes(224, 0, 16, 1, 24, 3, 24, 3, 24, 0, 24, 0, 25, 0, 255, 1, 24, 0, 24, 0, 24, 0, 254, 0
                            , 155, 3, 155, 3, 14, 0), // 0x1d
                    bytes(3, 3), // 0x1e
                    null, // 0x1f
                    bytes(7, 7, 7, 7, 7, 7, 7, 7, 7, 2, 2, 2, 2, 2, 0, 0, 7, 7, 7), // 0x20
                    bytes(231, 231, 198, 198, 66, 33), // '!'
                    bytes(16, 1, 16, 1, 16, 1, 16, 1, 16, 1, 16, 1, 254, 7, 254, 7, 136, 0, 136, 0, 136, 0, 136, 0
                            , 136, 0, 136, 0, 255, 3, 255, 3, 68, 0, 68, 0, 68, 0, 68, 0, 68, 0, 68, 0), // '"'
                    bytes(32, 0, 32, 0, 248, 0, 38, 3, 35, 6, 35, 6, 35, 0, 35, 0, 39, 0, 62, 0, 248, 0, 224, 3
                            , 32, 7, 32, 6, 32, 6, 32, 6, 35, 6, 35, 6, 38, 3, 248, 0, 32, 0, 32, 0), // '#'
                    bytes(30, 4, 51, 4, 51, 2, 51, 2, 51, 1, 51, 1, 158, 0, 128, 0, 64, 0, 64, 0, 32, 0, 32, 0
                            , 16, 0, 16, 0, 8, 0, 200, 3, 100, 6, 100, 6, 98, 6, 98, 6, 97, 6, 193, 3), // '$'
                    bytes(120, 0, 132, 0, 134, 1, 134, 1, 134, 1, 198, 0, 102, 0, 44, 0, 24, 0, 24, 0, 52, 0, 34, 0
                            , 99, 6, 195, 4, 131, 6, 131, 3, 3, 1, 134, 3, 252, 6), // '%'
                    bytes(7, 7, 6, 6, 2, 1), // '&'
                    bytes(32, 16, 24, 8, 12, 4, 6, 6, 2, 3, 3, 3, 3, 3, 3, 2, 6, 6, 4, 12, 8, 24, 16, 32
                            ), // '''
                    bytes(1, 2, 6, 4, 12, 8, 24, 24, 16, 48, 48, 48, 48, 48, 48, 16, 24, 24, 8, 12, 4, 6, 2, 1
                            ), // '('
                    bytes(48, 0, 48, 0, 51, 3, 183, 3, 252, 0, 48, 0, 48, 0, 252, 0, 183, 3, 51, 3, 48, 0, 48, 0
                            ), // ')'
                    bytes(48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 255, 3, 255, 3, 48, 0, 48, 0, 48, 0, 48, 0
                            , 48, 0, 48, 0), // '*'
                    bytes(7, 7, 6, 6, 2, 1), // '+'
                    bytes(255, 7, 255, 7), // ','
                    bytes(7, 7, 7), // '-'
                    bytes(0, 4, 0, 6, 0, 2, 0, 3, 0, 1, 128, 1, 128, 0, 192, 0, 64, 0, 96, 0, 32, 0, 48, 0
                            , 16, 0, 24, 0, 8, 0, 12, 0, 4, 0, 6, 0, 2, 0, 3, 0, 1, 0, 1, 0), // '.'
                    bytes(112, 0, 216, 0, 140, 1, 4, 1, 6, 3, 2, 2, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6
                            , 3, 6, 3, 6, 2, 2, 6, 3, 4, 1, 140, 1, 216, 0, 112, 0), // '/'
                    bytes(48, 0, 56, 0, 60, 0, 63, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0
                            , 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 254, 1), // '0'
                    bytes(112, 0, 140, 1, 6, 3, 2, 6, 1, 6, 0, 6, 0, 6, 0, 3, 0, 3, 128, 1, 192, 0, 64, 0
                            , 32, 0, 16, 0, 8, 0, 4, 4, 2, 4, 2, 6, 255, 7, 255, 7), // '1'
                    bytes(124, 0, 198, 1, 129, 1, 0, 3, 0, 3, 0, 3, 0, 1, 128, 0, 120, 0, 252, 1, 128, 3, 0, 7
                            , 0, 6, 0, 6, 0, 6, 0, 6, 0, 3, 128, 1, 224, 0, 31, 0), // '2'
                    bytes(128, 1, 128, 1, 192, 1, 192, 1, 160, 1, 160, 1, 144, 1, 144, 1, 136, 1, 136, 1, 132, 1, 132, 1
                            , 130, 1, 130, 1, 255, 7, 128, 1, 128, 1, 128, 1, 128, 1, 224, 7), // '3'
                    bytes(255, 3, 255, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 125, 0, 199, 0, 131, 1, 1, 3, 0, 3
                            , 0, 3, 0, 3, 0, 3, 0, 3, 128, 1, 128, 1, 224, 0, 31, 0), // '4'
                    bytes(192, 1, 48, 0, 8, 0, 12, 0, 6, 0, 6, 0, 3, 0, 3, 0, 243, 0, 15, 3, 3, 6, 3, 6
                            , 3, 6, 3, 6, 3, 6, 3, 6, 6, 3, 6, 3, 140, 1, 112, 0), // '5'
                    bytes(255, 7, 255, 7, 3, 4, 1, 2, 1, 1, 0, 1, 128, 0, 128, 0, 64, 0, 64, 0, 96, 0, 96, 0
                            , 32, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0), // '6'
                    bytes(112, 0, 140, 1, 4, 1, 6, 3, 6, 3, 6, 3, 6, 3, 12, 1, 156, 0, 120, 0, 248, 0, 196, 3
                            , 2, 3, 3, 6, 3, 6, 3, 6, 3, 6, 2, 2, 6, 3, 248, 0), // '7'
                    bytes(112, 0, 140, 1, 6, 3, 6, 3, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 2, 6, 134, 7, 120, 6
                            , 0, 6, 0, 6, 0, 3, 0, 3, 128, 1, 128, 0, 96, 0, 28, 0), // '8'
                    bytes(7, 7, 7, 0, 0, 0, 0, 0, 0, 0, 7, 7, 7), // '9'
                    bytes(7, 7, 7, 0, 0, 0, 0, 0, 0, 0, 7, 7, 6, 6, 2, 1), // ':'
                    bytes(0, 2, 0, 3, 128, 1, 192, 0, 96, 0, 48, 0, 24, 0, 12, 0, 6, 0, 3, 0, 3, 0, 6, 0
                            , 12, 0, 24, 0, 48, 0, 96, 0, 192, 0, 128, 1, 0, 3, 0, 2), // ';'
                    bytes(255, 7, 255, 7, 0, 0, 0, 0, 0, 0, 0, 0, 255, 7, 255, 7), // '<'
                    bytes(1, 0, 3, 0, 6, 0, 12, 0, 24, 0, 48, 0, 96, 0, 192, 0, 128, 1, 0, 3, 0, 3, 128, 1
                            , 192, 0, 96, 0, 48, 0, 24, 0, 12, 0, 6, 0, 3, 0, 1, 0), // '='
                    bytes(120, 0, 134, 1, 3, 3, 3, 3, 7, 3, 6, 3, 128, 3, 128, 1, 192, 0, 96, 0, 32, 0, 16, 0
                            , 16, 0, 16, 0, 0, 0, 0, 0, 56, 0, 56, 0, 56, 0), // '>'
                    bytes(248, 0, 4, 1, 6, 3, 2, 2, 3, 6, 177, 4, 233, 4, 205, 4, 205, 4, 205, 4, 205, 4, 205, 4
                            , 205, 4, 237, 6, 185, 3, 3, 0, 2, 0, 6, 4, 4, 3, 248, 0), // '?'
                    bytes(96, 0, 96, 0, 96, 0, 208, 0, 208, 0, 208, 0, 144, 1, 136, 1, 136, 1, 136, 1, 4, 3, 4, 3
                            , 252, 3, 4, 3, 2, 6, 2, 6, 2, 6, 2, 6, 7, 15), // '@'
                    bytes(127, 0, 134, 1, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 1, 134, 0, 126, 0, 134, 1, 6, 3
                            , 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 134, 1, 127, 0), // 'A'
                    bytes(112, 2, 140, 3, 6, 3, 6, 2, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0
                            , 3, 0, 3, 0, 3, 0, 6, 2, 6, 2, 140, 1, 112, 0), // 'B'
                    bytes(63, 0, 198, 0, 134, 1, 6, 3, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6
                            , 6, 6, 6, 6, 6, 3, 6, 3, 134, 1, 198, 0, 63, 0), // 'C'
                    bytes(255, 3, 6, 3, 6, 2, 6, 4, 6, 0, 6, 0, 134, 0, 134, 0, 198, 0, 254, 0, 198, 0, 134, 0
                            , 134, 0, 6, 0, 6, 0, 6, 4, 6, 2, 6, 3, 255, 3), // 'D'
                    bytes(255, 3, 6, 3, 6, 2, 6, 4, 6, 0, 6, 0, 134, 0, 134, 0, 198, 0, 254, 0, 198, 0, 134, 0
                            , 134, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 15, 0), // 'E'
                    bytes(112, 2, 140, 3, 6, 3, 6, 2, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 195, 7, 3, 3
                            , 3, 3, 3, 3, 3, 3, 6, 3, 6, 3, 140, 3, 112, 2), // 'F'
                    bytes(143, 7, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 254, 3, 6, 3, 6, 3
                            , 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 143, 7), // 'G'
                    bytes(63, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 63), // 'H'
                    bytes(224, 7, 128, 1, 128, 1, 128, 1, 128, 1, 128, 1, 128, 1, 128, 1, 128, 1, 128, 1, 128, 1, 128, 1
                            , 128, 1, 134, 1, 135, 1, 131, 1, 131, 1, 198, 0, 56, 0), // 'I'
                    bytes(143, 7, 6, 2, 6, 1, 6, 1, 134, 0, 70, 0, 70, 0, 38, 0, 22, 0, 62, 0, 62, 0, 102, 0
                            , 102, 0, 198, 0, 198, 0, 134, 1, 134, 1, 6, 3, 143, 7), // 'J'
                    bytes(15, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0
                            , 6, 0, 6, 0, 6, 0, 6, 4, 6, 2, 6, 3, 255, 3), // 'K'
                    bytes(7, 14, 6, 6, 6, 7, 14, 7, 14, 7, 142, 6, 154, 6, 154, 6, 218, 6, 114, 6, 114, 6, 114, 6
                            , 34, 6, 34, 6, 2, 6, 2, 6, 2, 6, 2, 6, 7, 15), // 'L'
                    bytes(7, 7, 6, 2, 14, 2, 14, 2, 14, 2, 26, 2, 26, 2, 50, 2, 50, 2, 98, 2, 98, 2, 194, 2
                            , 194, 2, 194, 2, 130, 3, 130, 3, 2, 3, 2, 3, 7, 2), // 'M'
                    bytes(248, 0, 6, 3, 2, 2, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6
                            , 3, 6, 3, 6, 3, 6, 3, 6, 2, 2, 6, 3, 248, 0), // 'N'
                    bytes(127, 0, 134, 1, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 134, 1, 126, 0, 6, 0
                            , 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 15, 0), // 'O'
                    bytes(120, 0, 134, 1, 2, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3
                            , 3, 3, 3, 3, 51, 3, 75, 3, 70, 1, 198, 1, 120, 0, 192, 4, 128, 7, 0, 3), // 'P'
                    bytes(127, 0, 134, 1, 6, 1, 6, 3, 6, 3, 6, 3, 6, 3, 6, 1, 134, 1, 126, 0, 70, 0, 134, 0
                            , 134, 1, 134, 1, 134, 1, 134, 1, 134, 5, 134, 5, 15, 3), // 'Q'
                    bytes(120, 2, 134, 3, 3, 3, 3, 2, 3, 0, 3, 0, 7, 0, 14, 0, 124, 0, 240, 1, 128, 3, 0, 7
                            , 0, 6, 0, 6, 0, 6, 1, 6, 3, 3, 135, 1, 121, 0), // 'R'
                    bytes(255, 3, 51, 3, 49, 2, 49, 2, 49, 2, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0
                            , 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 120, 0), // 'S'
                    bytes(15, 7, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2
                            , 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 12, 1, 248, 0), // 'T'
                    bytes(15, 7, 6, 2, 6, 2, 6, 2, 6, 2, 12, 1, 12, 1, 12, 1, 12, 1, 152, 0, 152, 0, 152, 0
                            , 216, 0, 80, 0, 112, 0, 112, 0, 32, 0, 32, 0, 32, 0), // 'U'
                    bytes(7, 6, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4, 99, 4, 99, 4, 99, 4, 99, 4, 99, 4, 243, 4
                            , 211, 4, 214, 2, 214, 2, 142, 2, 142, 3, 140, 1, 140, 1), // 'V'
                    bytes(15, 7, 6, 2, 6, 2, 12, 1, 12, 1, 152, 0, 152, 0, 80, 0, 112, 0, 32, 0, 112, 0, 80, 0
                            , 200, 0, 200, 0, 132, 1, 132, 1, 2, 3, 2, 3, 135, 7), // 'W'
                    bytes(15, 7, 6, 2, 6, 2, 12, 1, 12, 1, 12, 1, 152, 0, 152, 0, 240, 0, 112, 0, 96, 0, 96, 0
                            , 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 248, 1), // 'X'
                    bytes(254, 7, 2, 3, 2, 3, 129, 1, 128, 1, 192, 0, 192, 0, 96, 0, 96, 0, 96, 0, 48, 0, 48, 0
                            , 24, 0, 24, 0, 12, 0, 12, 4, 6, 2, 6, 3, 255, 3), // 'Y'
                    bytes(48, 28, 7, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 7, 28, 48
                            ), // 'Z'
                    bytes(1, 0, 3, 0, 2, 0, 6, 0, 4, 0, 12, 0, 8, 0, 24, 0, 16, 0, 48, 0, 32, 0, 96, 0
                            , 64, 0, 192, 0, 128, 0, 128, 1, 0, 1, 0, 3, 0, 2, 0, 6, 0, 4, 0, 4), // '['
                    bytes(3, 15, 60, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 60, 15, 3
                            ), // '\'
                    bytes(32, 0, 112, 0, 248, 0, 220, 1, 6, 3, 3, 6), // ']'
                    bytes(255, 7, 255, 7), // '^'
                    bytes(6, 15, 30, 56, 96), // '_'
                    bytes(56, 0, 206, 0, 130, 1, 128, 1, 128, 1, 248, 1, 134, 1, 131, 1, 131, 1, 131, 1, 131, 5, 198, 5
                            , 60, 3), // '`'
                    bytes(6, 0, 7, 0, 6, 0, 6, 0, 6, 0, 6, 0, 118, 0, 142, 1, 6, 3, 6, 3, 6, 6, 6, 6
                            , 6, 6, 6, 6, 6, 6, 6, 3, 6, 3, 142, 1, 114, 0), // 'a'
                    bytes(112, 0, 140, 1, 6, 3, 6, 3, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 6, 0, 6, 2, 140, 1
                            , 112, 0), // 'b'
                    bytes(0, 3, 192, 3, 0, 3, 0, 3, 0, 3, 0, 3, 112, 3, 140, 3, 6, 3, 6, 3, 3, 3, 3, 3
                            , 3, 3, 3, 3, 3, 3, 6, 3, 6, 3, 140, 3, 112, 7), // 'c'
                    bytes(112, 0, 140, 1, 6, 3, 6, 7, 3, 6, 255, 7, 3, 0, 3, 0, 3, 0, 6, 0, 6, 4, 12, 3
                            , 240, 0), // 'd'
                    bytes(128, 3, 96, 6, 32, 6, 48, 0, 48, 0, 48, 0, 255, 3, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0
                            , 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 252, 0), // 'e'
                    bytes(0, 6, 112, 7, 140, 1, 6, 3, 6, 3, 6, 3, 140, 1, 112, 0, 8, 0, 12, 0, 248, 0, 228, 3
                            , 2, 7, 3, 6, 3, 6, 6, 3, 248, 0), // 'f'
                    bytes(6, 0, 7, 0, 6, 0, 6, 0, 6, 0, 6, 0, 246, 0, 142, 1, 6, 3, 6, 3, 6, 3, 6, 3
                            , 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 143, 7), // 'g'
                    bytes(14, 14, 14, 0, 0, 0, 15, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 63), // 'h'
                    bytes(224, 224, 224, 0, 0, 0, 240, 192, 192, 192, 192, 192, 192, 192, 192, 192, 192, 192, 192, 195, 99, 62), // 'i'
                    bytes(6, 0, 7, 0, 6, 0, 6, 0, 6, 0, 6, 0, 134, 7, 6, 1, 134, 0, 70, 0, 38, 0, 22, 0
                            , 30, 0, 54, 0, 102, 0, 198, 0, 134, 1, 6, 3, 143, 7), // 'j'
                    bytes(12, 15, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 63), // 'k'
                    bytes(182, 3, 111, 6, 102, 6, 102, 6, 102, 6, 102, 6, 102, 6, 102, 6, 102, 6, 102, 6, 102, 6, 102, 6
                            , 247, 14), // 'l'
                    bytes(102, 0, 159, 1, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3
                            , 143, 7), // 'm'
                    bytes(112, 0, 140, 1, 6, 3, 2, 2, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 2, 2, 6, 3, 140, 1
                            , 112, 0), // 'n'
                    bytes(118, 0, 143, 1, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3, 142, 1, 118, 0, 6, 0
                            , 6, 0, 6, 0, 6, 0, 15, 0), // 'o'
                    bytes(112, 2, 140, 3, 6, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 6, 3, 140, 3, 112, 3, 0, 3
                            , 0, 3, 0, 3, 0, 3, 128, 7), // 'p'
                    bytes(140, 3, 207, 7, 108, 6, 28, 0, 12, 0, 12, 0, 12, 0, 12, 0, 12, 0, 12, 0, 12, 0, 12, 0
                            , 63, 0), // 'q'
                    bytes(120, 2, 134, 3, 3, 3, 3, 2, 7, 0, 62, 0, 248, 1, 192, 3, 0, 7, 1, 6, 3, 6, 7, 3
                            , 249, 0), // 'r'
                    bytes(24, 0, 24, 0, 24, 0, 24, 0, 24, 0, 24, 0, 255, 1, 24, 0, 24, 0, 24, 0, 24, 0, 24, 0
                            , 24, 0, 24, 0, 24, 0, 24, 4, 24, 4, 48, 2, 224, 1), // 's'
                    bytes(6, 3, 135, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 204, 3
                            , 56, 7), // 't'
                    bytes(15, 7, 6, 2, 6, 2, 12, 1, 12, 1, 12, 1, 152, 0, 152, 0, 88, 0, 112, 0, 112, 0, 32, 0
                            , 32, 0), // 'u'
                    bytes(7, 6, 3, 4, 3, 4, 99, 4, 99, 4, 99, 4, 115, 4, 211, 4, 211, 4, 138, 6, 142, 3, 6, 3
                            , 4, 1), // 'v'
                    bytes(15, 7, 6, 2, 12, 1, 152, 0, 152, 0, 112, 0, 32, 0, 112, 0, 200, 0, 200, 0, 132, 1, 2, 3
                            , 135, 7), // 'w'
                    bytes(15, 7, 6, 2, 6, 2, 12, 1, 12, 1, 24, 1, 152, 0, 176, 0, 112, 0, 96, 0, 64, 0, 32, 0
                            , 32, 0, 19, 0, 19, 0, 14, 0), // 'x'
                    bytes(254, 7, 6, 3, 130, 1, 129, 1, 192, 0, 96, 0, 32, 0, 48, 0, 24, 0, 12, 4, 12, 2, 6, 2
                            , 255, 3), // 'y'
                    bytes(16, 8, 8, 12, 12, 12, 12, 12, 12, 4, 2, 1, 2, 4, 12, 12, 12, 12, 12, 12, 12, 8, 8, 16
                            ), // 'z'
                    bytes(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3
                            ), // '{'
                    bytes(1, 2, 2, 6, 6, 6, 6, 6, 6, 4, 8, 16, 8, 4, 6, 6, 6, 6, 6, 6, 6, 2, 2, 1
                            ), // '|'
                    bytes(28, 4, 62, 6, 227, 3, 193, 1), // '}'
                    null, // '~'
                    null, // 0x7f
                    null, // 0x80
                    null, // 0x81
                    null, // 0x82
                    null, // 0x83
                    null, // 0x84
                    null, // 0x85
                    null, // 0x86
                    null, // 0x87
                    null, // 0x88
                    null, // 0x89
                    null, // 0x8a
                    null, // 0x8b
                    null, // 0x8c
                    null, // 0x8d
                    null, // 0x8e
                    null, // 0x8f
                    null, // 0x90
                    null, // 0x91
                    null, // 0x92
                    null, // 0x93
                    null, // 0x94
                    null, // 0x95
                    null, // 0x96
                    null, // 0x97
                    null, // 0x98
                    null, // 0x99
                    null, // 0x9a
                    null, // 0x9b
                    null, // 0x9c
                    null, // 0x9d
                    null, // 0x9e
                    null, // 0x9f
                    bytes(7, 7, 7, 0, 0, 2, 2, 2, 2, 2, 7, 7, 7, 7, 7, 7, 7, 7, 7), // 0xa0
                    bytes(32, 0, 32, 0, 248, 0, 36, 1, 38, 2, 35, 6, 35, 6, 35, 6, 35, 0, 35, 0, 35, 0, 35, 0
                            , 35, 0, 35, 6, 35, 6, 38, 2, 36, 1, 248, 0, 32, 0, 32, 0), // 0xa1
                    bytes(224, 0, 16, 1, 24, 3, 24, 3, 24, 0, 24, 0, 25, 0, 255, 1, 24, 0, 24, 0, 24, 0, 254, 0
                            , 155, 3, 155, 3, 14, 0), // 0xa2
                    bytes(1, 4, 115, 6, 254, 3, 140, 1, 6, 3, 6, 3, 6, 3, 140, 1, 254, 3, 115, 6, 1, 4), // 0xa3
                    bytes(207, 3, 134, 1, 134, 0, 140, 0, 204, 0, 76, 0, 88, 0, 223, 3, 120, 0, 48, 0, 48, 0, 255, 3
                            , 48, 0, 48, 0, 48, 0, 48, 0, 120, 0, 254, 1), // 0xa4
                    bytes(3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3), // 0xa5
                    bytes(120, 0, 134, 1, 3, 3, 3, 3, 3, 0, 6, 0, 124, 0, 134, 1, 3, 3, 3, 3, 3, 3, 134, 1
                            , 248, 0, 128, 1, 0, 3, 3, 3, 3, 3, 134, 1, 120, 0), // 0xa6
                    bytes(51, 51, 51), // 0xa7
                    bytes(240, 0, 12, 3, 6, 6, 2, 4, 243, 12, 25, 11, 13, 14, 7, 14, 7, 8, 7, 8, 7, 8, 7, 8
                            , 13, 12, 25, 11, 243, 12, 2, 4, 6, 6, 12, 3, 240, 0), // 0xa8
                    bytes(28, 35, 99, 96, 124, 99, 99, 99, 92, 0, 127), // 0xa9
                    bytes(32, 2, 48, 3, 16, 1, 152, 1, 136, 0, 204, 0, 68, 0, 102, 0, 34, 0, 51, 0, 51, 0, 34, 0
                            , 102, 0, 68, 0, 204, 0, 136, 0, 152, 1, 16, 1, 48, 3, 32, 2), // 0xaa
                    bytes(255, 3, 255, 3, 0, 3, 0, 3, 0, 3), // 0xab
                    bytes(255, 3, 255, 3), // 0xac
                    bytes(240, 0, 12, 3, 6, 6, 2, 4, 251, 12, 13, 9, 13, 11, 13, 11, 13, 11, 253, 9, 141, 8, 13, 9
                            , 13, 11, 13, 11, 15, 15, 2, 4, 6, 6, 12, 3, 240, 0), // 0xad
                    bytes(255, 7, 255, 7), // 0xae
                    bytes(28, 54, 99, 99, 54, 28), // 0xaf
                    bytes(48, 0, 48, 0, 48, 0, 48, 0, 255, 3, 255, 3, 48, 0, 48, 0, 48, 0, 48, 0, 0, 0, 255, 3
                            , 255, 3), // 0xb0
                    bytes(30, 51, 51, 48, 24, 4, 34, 51, 63), // 0xb1
                    bytes(30, 51, 51, 48, 28, 48, 51, 51, 30), // 0xb2
                    bytes(56, 28, 6, 3), // 0xb3
                    bytes(48, 6, 48, 6, 16, 2, 16, 3, 8, 1, 8, 1, 140, 1, 116, 5, 6, 7, 3, 0, 3, 0, 3, 0
                            ), // 0xb4
                    bytes(248, 15, 252, 2, 254, 2, 254, 2, 255, 2, 255, 2, 255, 2, 254, 2, 252, 2, 240, 2, 128, 2, 128, 2
                            , 128, 2, 128, 2, 128, 2, 128, 2, 128, 2, 128, 2, 224, 14), // 0xb5
                    bytes(7, 7, 7), // 0xb6
                    bytes(56, 24, 12, 6, 1), // 0xb7
                    bytes(12, 14, 15, 12, 12, 12, 12, 12, 63), // 0xb8
                    bytes(28, 34, 99, 99, 99, 99, 99, 34, 28, 0, 127), // 0xb9
                    bytes(17, 0, 51, 0, 34, 0, 102, 0, 68, 0, 204, 0, 136, 0, 152, 1, 16, 1, 48, 3, 48, 3, 16, 1
                            , 152, 1, 136, 0, 204, 0, 68, 0, 102, 0, 34, 0, 51, 0, 17, 0), // 0xba
                    bytes(12, 0, 14, 0, 13, 0, 12, 0, 12, 8, 12, 12, 12, 6, 140, 3, 192, 0, 112, 0, 24, 0, 14, 3
                            , 131, 3, 65, 3, 32, 3, 48, 3, 240, 15, 0, 3, 0, 3), // 0xbb
                    bytes(12, 0, 14, 0, 13, 0, 12, 0, 12, 8, 12, 12, 12, 6, 140, 3, 192, 0, 112, 0, 24, 0, 142, 7
                            , 195, 12, 193, 12, 0, 12, 0, 6, 128, 1, 192, 8, 192, 15), // 0xbc
                    bytes(30, 0, 51, 0, 49, 0, 48, 0, 12, 8, 48, 12, 51, 6, 179, 3, 206, 0, 112, 0, 24, 0, 14, 3
                            , 131, 3, 65, 3, 32, 3, 48, 3, 240, 15, 0, 3, 0, 3), // 0xbd
                    bytes(112, 0, 112, 0, 112, 0, 0, 0, 0, 0, 32, 0, 32, 0, 32, 0, 16, 0, 24, 0, 12, 0, 6, 0
                            , 7, 0, 131, 1, 131, 3, 3, 3, 3, 3, 134, 1, 120, 0), // 0xbe
                    bytes(28, 0, 48, 0, 64, 0, 0, 0, 96, 0, 96, 0, 208, 0, 208, 0, 208, 0, 144, 1, 136, 1, 136, 1
                            , 136, 1, 4, 3, 4, 3, 252, 3, 4, 3, 2, 6, 2, 6, 2, 6, 7, 15), // 0xbf
                    bytes(128, 3, 192, 0, 32, 0, 0, 0, 96, 0, 96, 0, 208, 0, 208, 0, 208, 0, 144, 1, 136, 1, 136, 1
                            , 136, 1, 4, 3, 4, 3, 252, 3, 4, 3, 2, 6, 2, 6, 2, 6, 7, 15), // 0xc0
                    bytes(96, 0, 152, 1, 4, 2, 0, 0, 96, 0, 96, 0, 208, 0, 208, 0, 208, 0, 144, 1, 136, 1, 136, 1
                            , 136, 1, 4, 3, 4, 3, 252, 3, 4, 3, 2, 6, 2, 6, 2, 6, 7, 15), // 0xc1
                    bytes(56, 3, 230, 0, 0, 0, 0, 0, 96, 0, 96, 0, 208, 0, 208, 0, 208, 0, 144, 1, 136, 1, 136, 1
                            , 136, 1, 4, 3, 4, 3, 252, 3, 4, 3, 2, 6, 2, 6, 2, 6, 7, 15), // 0xc2
                    bytes(152, 1, 152, 1, 0, 0, 0, 0, 96, 0, 96, 0, 208, 0, 208, 0, 208, 0, 144, 1, 136, 1, 136, 1
                            , 136, 1, 4, 3, 4, 3, 252, 3, 4, 3, 2, 6, 2, 6, 2, 6, 7, 15), // 0xc3
                    bytes(240, 0, 152, 1, 152, 1, 240, 0, 96, 0, 96, 0, 208, 0, 208, 0, 208, 0, 144, 1, 136, 1, 136, 1
                            , 136, 1, 4, 3, 4, 3, 252, 3, 4, 3, 2, 6, 2, 6, 2, 6, 7, 15), // 0xc4
                    bytes(224, 15, 224, 12, 224, 8, 208, 0, 208, 0, 208, 0, 208, 4, 200, 4, 200, 6, 200, 7, 196, 6, 196, 4
                            , 252, 4, 196, 0, 194, 0, 194, 0, 194, 8, 194, 12, 231, 15), // 0xc5
                    bytes(112, 2, 140, 3, 6, 3, 6, 2, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0
                            , 3, 0, 3, 0, 6, 2, 6, 2, 140, 1, 112, 0, 32, 0, 64, 0, 64, 0, 48, 0), // 0xc6
                    bytes(28, 0, 48, 0, 64, 0, 0, 0, 255, 3, 6, 3, 6, 2, 6, 4, 6, 0, 134, 0, 134, 0, 198, 0
                            , 254, 0, 198, 0, 134, 0, 134, 0, 6, 0, 6, 4, 6, 2, 6, 3, 255, 3), // 0xc7
                    bytes(192, 1, 96, 0, 16, 0, 0, 0, 255, 3, 6, 3, 6, 2, 6, 4, 6, 0, 134, 0, 134, 0, 198, 0
                            , 254, 0, 198, 0, 134, 0, 134, 0, 6, 0, 6, 4, 6, 2, 6, 3, 255, 3), // 0xc8
                    bytes(32, 0, 216, 0, 6, 3, 0, 0, 255, 3, 6, 3, 6, 2, 6, 4, 6, 0, 134, 0, 134, 0, 198, 0
                            , 254, 0, 198, 0, 134, 0, 134, 0, 6, 0, 6, 4, 6, 2, 6, 3, 255, 3), // 0xc9
                    bytes(204, 0, 204, 0, 0, 0, 0, 0, 255, 3, 6, 3, 6, 2, 6, 4, 6, 0, 134, 0, 134, 0, 198, 0
                            , 254, 0, 198, 0, 134, 0, 134, 0, 6, 0, 6, 4, 6, 2, 6, 3, 255, 3), // 0xca
                    bytes(7, 12, 16, 0, 63, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 63), // 0xcb
                    bytes(56, 12, 2, 0, 63, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 63), // 0xcc
                    bytes(12, 18, 33, 0, 63, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 63), // 0xcd
                    bytes(51, 51, 0, 0, 63, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 63), // 0xce
                    bytes(63, 0, 198, 0, 134, 1, 6, 3, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6, 63, 6, 6, 6, 6, 6
                            , 6, 6, 6, 6, 6, 3, 6, 3, 134, 1, 198, 0, 63, 0), // 0xcf
                    bytes(56, 3, 230, 0, 0, 0, 0, 0, 7, 7, 6, 2, 14, 2, 14, 2, 26, 2, 26, 2, 26, 2, 50, 2
                            , 50, 2, 98, 2, 98, 2, 194, 2, 194, 2, 130, 3, 130, 3, 2, 3, 7, 3), // 0xd0
                    bytes(28, 0, 48, 0, 64, 0, 0, 0, 248, 0, 6, 3, 2, 2, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6
                            , 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 2, 2, 6, 3, 248, 0), // 0xd1
                    bytes(192, 1, 96, 0, 16, 0, 0, 0, 248, 0, 6, 3, 2, 2, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6
                            , 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 2, 2, 6, 3, 248, 0), // 0xd2
                    bytes(32, 0, 216, 0, 6, 3, 0, 0, 248, 0, 6, 3, 2, 2, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6
                            , 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 2, 2, 6, 3, 248, 0), // 0xd3
                    bytes(56, 3, 230, 0, 0, 0, 0, 0, 248, 0, 6, 3, 2, 2, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6
                            , 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 2, 2, 6, 3, 248, 0), // 0xd4
                    bytes(140, 1, 140, 1, 0, 0, 0, 0, 248, 0, 6, 3, 2, 2, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6
                            , 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 2, 2, 6, 3, 248, 0), // 0xd5
                    bytes(3, 6, 6, 3, 140, 1, 216, 0, 112, 0, 112, 0, 216, 0, 140, 1, 6, 3, 3, 6), // 0xd6
                    bytes(0, 2, 248, 2, 6, 3, 2, 3, 131, 6, 131, 6, 67, 6, 67, 6, 67, 6, 35, 6, 35, 6, 19, 6
                            , 19, 6, 19, 6, 11, 6, 11, 6, 7, 6, 6, 2, 6, 3, 250, 0, 2, 0), // 0xd7
                    bytes(56, 0, 96, 0, 128, 0, 0, 0, 15, 7, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2
                            , 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 12, 1, 248, 0), // 0xd8
                    bytes(192, 1, 96, 0, 16, 0, 0, 0, 15, 7, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2
                            , 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 12, 1, 248, 0), // 0xd9
                    bytes(32, 0, 216, 0, 6, 3, 0, 0, 15, 7, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2
                            , 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 12, 1, 248, 0), // 0xda
                    bytes(140, 1, 140, 1, 0, 0, 0, 0, 15, 7, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2
                            , 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 6, 2, 12, 1, 248, 0), // 0xdb
                    bytes(192, 1, 96, 0, 16, 0, 0, 0, 15, 7, 6, 2, 6, 2, 12, 1, 12, 1, 12, 1, 152, 0, 152, 0
                            , 240, 0, 112, 0, 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 96, 0, 248, 1), // 0xdc
                    bytes(15, 0, 6, 0, 6, 0, 126, 0, 134, 1, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 3
                            , 134, 1, 126, 0, 6, 0, 6, 0, 6, 0, 6, 0, 6, 0, 15, 0), // 0xdd
                    bytes(124, 0, 130, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 131, 0, 115, 0, 131, 1, 3, 3
                            , 3, 6, 3, 6, 3, 6, 3, 6, 3, 3, 131, 1, 115, 0), // 0xde
                    bytes(28, 0, 48, 0, 64, 0, 0, 0, 0, 0, 0, 0, 56, 0, 206, 0, 130, 1, 128, 1, 128, 1, 248, 1
                            , 134, 1, 131, 1, 131, 1, 131, 1, 131, 5, 198, 5, 60, 3), // 0xdf
                    bytes(224, 0, 48, 0, 8, 0, 0, 0, 0, 0, 0, 0, 56, 0, 206, 0, 130, 1, 128, 1, 128, 1, 248, 1
                            , 134, 1, 131, 1, 131, 1, 131, 1, 131, 5, 198, 5, 60, 3), // 0xe0
                    bytes(48, 0, 72, 0, 204, 0, 134, 1, 0, 0, 0, 0, 56, 0, 206, 0, 130, 1, 128, 1, 128, 1, 248, 1
                            , 142, 1, 131, 1, 131, 1, 131, 1, 131, 5, 198, 5, 60, 3), // 0xe1
                    bytes(156, 1, 230, 0, 0, 0, 0, 0, 0, 0, 56, 0, 206, 0, 130, 1, 128, 1, 128, 1, 248, 1, 134, 1
                            , 131, 1, 131, 1, 131, 1, 131, 5, 198, 5, 60, 3), // 0xe2
                    bytes(204, 0, 204, 0, 0, 0, 0, 0, 56, 0, 206, 0, 130, 1, 128, 1, 128, 1, 248, 1, 134, 1, 131, 1
                            , 131, 1, 131, 1, 131, 5, 198, 5, 60, 3), // 0xe3
                    bytes(120, 0, 204, 0, 204, 0, 120, 0, 0, 0, 0, 0, 56, 0, 206, 0, 130, 1, 128, 1, 128, 1, 248, 1
                            , 134, 1, 131, 1, 131, 1, 131, 1, 131, 5, 198, 5, 60, 3), // 0xe4
                    bytes(140, 1, 82, 2, 35, 6, 35, 6, 32, 6, 248, 7, 36, 0, 34, 0, 35, 0, 35, 6, 35, 6, 83, 6
                            , 142, 3), // 0xe5
                    bytes(112, 2, 140, 3, 6, 3, 3, 2, 3, 0, 3, 0, 3, 0, 3, 0, 3, 0, 3, 2, 6, 2, 140, 1
                            , 112, 0, 32, 0, 64, 0, 64, 0, 48, 0), // 0xe6
                    bytes(28, 0, 48, 0, 64, 0, 0, 0, 0, 0, 0, 0, 112, 0, 140, 1, 6, 3, 6, 7, 3, 6, 255, 7
                            , 3, 0, 3, 0, 3, 0, 6, 0, 6, 4, 12, 3, 240, 0), // 0xe7
                    bytes(224, 0, 48, 0, 8, 0, 0, 0, 0, 0, 0, 0, 112, 0, 140, 1, 6, 3, 6, 7, 3, 6, 255, 7
                            , 3, 0, 3, 0, 3, 0, 6, 0, 6, 4, 28, 3, 240, 0), // 0xe8
                    bytes(112, 0, 136, 0, 140, 1, 6, 3, 0, 0, 0, 0, 112, 0, 140, 1, 6, 3, 6, 7, 3, 6, 255, 7
                            , 3, 0, 3, 0, 3, 0, 6, 0, 6, 4, 12, 3, 240, 0), // 0xe9
                    bytes(140, 1, 140, 1, 0, 0, 0, 0, 112, 0, 140, 1, 6, 3, 6, 7, 3, 6, 255, 7, 3, 0, 3, 0
                            , 3, 0, 6, 0, 6, 4, 12, 3, 240, 0), // 0xea
                    bytes(28, 0, 48, 0, 64, 0, 0, 0, 0, 0, 0, 0, 60, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0
                            , 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 255, 3), // 0xeb
                    bytes(224, 0, 48, 0, 8, 0, 0, 0, 0, 0, 0, 0, 60, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0
                            , 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 255, 3), // 0xec
                    bytes(48, 0, 72, 0, 204, 0, 134, 1, 0, 0, 0, 0, 60, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0
                            , 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 255, 3), // 0xed
                    bytes(204, 0, 204, 0, 0, 0, 0, 0, 60, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0, 48, 0
                            , 48, 0, 48, 0, 48, 0, 48, 0, 255, 3), // 0xee
                    bytes(198, 0, 222, 0, 112, 0, 112, 0, 216, 0, 134, 0, 128, 1, 120, 1, 204, 1, 6, 3, 3, 3, 3, 3
                            , 3, 3, 3, 3, 3, 3, 3, 3, 134, 1, 124, 0), // 0xef
                    bytes(156, 1, 230, 0, 0, 0, 0, 0, 0, 0, 102, 0, 159, 1, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3
                            , 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 143, 7), // 0xf0
                    bytes(28, 0, 48, 0, 64, 0, 0, 0, 0, 0, 0, 0, 112, 0, 140, 1, 6, 3, 2, 2, 3, 6, 3, 6
                            , 3, 6, 3, 6, 3, 6, 2, 2, 6, 3, 140, 1, 112, 0), // 0xf1
                    bytes(224, 0, 48, 0, 8, 0, 0, 0, 0, 0, 0, 0, 112, 0, 140, 1, 6, 3, 2, 2, 3, 6, 3, 6
                            , 3, 6, 3, 6, 3, 6, 2, 2, 6, 3, 140, 1, 112, 0), // 0xf2
                    bytes(32, 0, 80, 0, 216, 0, 140, 1, 0, 0, 0, 0, 112, 0, 140, 1, 6, 3, 2, 2, 3, 6, 3, 6
                            , 3, 6, 3, 6, 3, 6, 2, 2, 6, 3, 140, 1, 112, 0), // 0xf3
                    bytes(56, 3, 204, 1, 0, 0, 0, 0, 112, 0, 140, 1, 6, 3, 2, 2, 3, 6, 3, 6, 3, 6, 3, 6
                            , 3, 6, 2, 2, 6, 3, 140, 1, 112, 0), // 0xf4
                    bytes(140, 1, 140, 1, 0, 0, 0, 0, 112, 0, 140, 1, 6, 3, 2, 2, 3, 6, 3, 6, 3, 6, 3, 6
                            , 3, 6, 2, 2, 6, 3, 140, 1, 112, 0), // 0xf5
                    bytes(48, 0, 48, 0, 0, 0, 0, 0, 255, 3, 255, 3, 0, 0, 0, 0, 48, 0, 48, 0), // 0xf6
                    bytes(112, 6, 140, 3, 6, 1, 130, 3, 195, 6, 99, 6, 35, 6, 51, 6, 27, 6, 14, 2, 4, 3, 142, 1
                            , 115, 0), // 0xf7
                    bytes(28, 0, 48, 0, 64, 0, 0, 0, 0, 0, 0, 0, 6, 3, 135, 3, 6, 3, 6, 3, 6, 3, 6, 3
                            , 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 204, 3, 56, 7), // 0xf8
                    bytes(224, 0, 48, 0, 8, 0, 0, 0, 0, 0, 0, 0, 6, 3, 135, 3, 6, 3, 6, 3, 6, 3, 6, 3
                            , 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 204, 3, 56, 7), // 0xf9
                    bytes(32, 0, 80, 0, 216, 0, 140, 1, 0, 0, 0, 0, 6, 3, 135, 3, 6, 3, 6, 3, 6, 3, 6, 3
                            , 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 204, 3, 56, 7), // 0xfa
                    bytes(204, 0, 204, 0, 0, 0, 0, 0, 6, 3, 135, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3, 6, 3
                            , 6, 3, 6, 3, 6, 3, 204, 3, 56, 7), // 0xfb
                    bytes(224, 0, 48, 0, 8, 0, 0, 0, 0, 0, 0, 0, 15, 7, 6, 2, 6, 2, 12, 1, 12, 1, 24, 1
                            , 152, 0, 176, 0, 112, 0, 96, 0, 64, 0, 32, 0, 32, 0, 19, 0, 19, 0, 14, 0), // 0xfc
                    bytes(7, 0, 6, 0, 6, 0, 6, 0, 6, 0, 118, 0, 142, 1, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6
                            , 6, 6, 6, 3, 142, 1, 118, 0, 6, 0, 6, 0, 6, 0, 6, 0, 15, 0), // 0xfd
                    bytes(152, 1, 152, 1, 0, 0, 0, 0, 15, 7, 6, 2, 6, 2, 12, 1, 12, 1, 24, 1, 152, 0, 176, 0
                            , 112, 0, 96, 0, 64, 0, 32, 0, 32, 0, 19, 0, 19, 0, 14, 0), // 0xfe
            },
            new String[] {"FONTNAME_REGISTRY", "FOUNDRY", "FAMILY_NAME", "WEIGHT_NAME", "SLANT", "SETWIDTH_NAME", "ADD_STYLE_NAME", "PIXEL_SIZE", "POINT_SIZE", "RESOLUTION_X", "RESOLUTION_Y", "SPACING", "AVERAGE_WIDTH", "CHARSET_REGISTRY", "CHARSET_ENCODING", "COPYRIGHT", "FONT", "WEIGHT", "RESOLUTION", "X_HEIGHT", "QUAD_WIDTH"},
            new int[] {87, 240, 90, 92, 94, 235, 87, 24, 170, 100, 100, 102, 120, 105, 107, 241, 242, 10, 138, 22, 12},
            new String[] {"", "Sony", "Fixed", "Medium", "R", "Normal", "", null, null, null, null, "C", null, "ISO8859", "1", "Copyright (c) 1987, 1988 Sony Corp.", "-Sony-Fixed-Medium-R-Normal--24-170-100-100-C-120-ISO8859-1", null, null, null, null});
    }

    static {
        BY_NAME.put("fixed", FIXED);
        BY_NAME.put("6x13", FIXED);
        BY_NAME.put("7x13", FONT_7X13);
        BY_NAME.put("9x15", FONT_9X15);
        BY_NAME.put("10x20", FONT_10X20);
        BY_NAME.put("12x24", FONT_12X24);
        // Each font is also reachable by its own XLFD name, and that is the name a client ends up
        // loading it by: it is what ListFontsWithInfo reports, so a client that lists fonts and then
        // opens one - which is exactly what Xlib's font-set machinery does - asks for
        // "-misc-fixed-medium-r-semicondensed--13-120-75-75-c-60-iso8859-1" and not for "fixed".
        //
        // Without this the lookup fell through to the Java approximation, which has **no
        // CHARSET_REGISTRY/CHARSET_ENCODING properties at all** (measured against a reference
        // server, 2026-09-26: the reference answered those two for the XLFD name, this server
        // answered neither). Xlib's `XCreateFontSet` reads a font's charset from those properties,
        // so every charset came back missing and no font set could be built - which is why no X
        // input method worked here (backlog C10). The listing was right and the loading was not.
        for (Font font : java.util.List.of(FIXED, FONT_7X13, FONT_9X15, FONT_10X20, FONT_12X24)) {
            BY_NAME.putIfAbsent(font.xlfd().toLowerCase(java.util.Locale.ROOT), font);
        }
    }

    /**
     * The font a client asked for by name, or null when it is not one of these.
     *
     * <p>A name with a wildcard in it is a <em>pattern</em>, and resolving one is not a nicety: it
     * is how a font-set client asks. Xlib's {@code XCreateFontSet} builds a pattern out of the base
     * name and the charset it is looking for and opens *that*, so a server that treats a pattern as
     * a name it has never heard of hands back the Java approximation - whose font has no
     * {@code CHARSET_REGISTRY}/{@code CHARSET_ENCODING} properties at all - and every charset comes
     * back missing. Measured against a reference server on 2026-09-26: for
     * {@code -misc-fixed-*-iso8859-1} the reference answered {@code ISO8859}/{@code 1} and this
     * server answered neither, which is the second half of why no font set could be built here
     * (backlog C10; the first half was the alias name in {@code ListFontsWithInfo}).
     */
    static Font forName(String name) {
        if (name == null) {
            return null;
        }
        String key = name.trim().toLowerCase(java.util.Locale.ROOT);
        Font exact = BY_NAME.get(key);
        if (exact != null) {
            return exact;
        }
        if (key.indexOf('*') < 0 && key.indexOf('?') < 0) {
            return null;
        }
        // The tables own five fonts, so the first of them a pattern matches is the answer - in a
        // fixed order, so the same request resolves the same way twice.
        for (Font font : java.util.List.of(FIXED, FONT_7X13, FONT_9X15, FONT_10X20, FONT_12X24)) {
            if (matches(key, font.name()) || matches(key, font.xlfd())) {
                return font;
            }
        }
        return null;
    }

    /** Whether a font name matches an X font pattern: {@code *} is any run, {@code ?} exactly one. */
    public static boolean matches(String pattern, String name) {
        return matches(pattern.toLowerCase(java.util.Locale.ROOT), 0,
                name.toLowerCase(java.util.Locale.ROOT), 0);
    }

    private static boolean matches(String pattern, int p, String name, int n) {
        while (p < pattern.length()) {
            char c = pattern.charAt(p);
            if (c == '*') {
                while (p < pattern.length() && pattern.charAt(p) == '*') {
                    p++;
                }
                if (p == pattern.length()) {
                    return true;
                }
                for (int i = n; i <= name.length(); i++) {
                    if (matches(pattern, p, name, i)) {
                        return true;
                    }
                }
                return false;
            }
            if (n >= name.length()) {
                return false;
            }
            if (c != '?' && c != name.charAt(n)) {
                return false;
            }
            p++;
            n++;
        }
        return n == name.length();
    }

    /**
     * The names the table's fonts answer to, each font's own XLFD - what a server lists them under
     * besides the short aliases.
     *
     * <p>{@code ResourceHandlers.SERVER_FONTS} adds these, and it has to: a {@code ListFontsWithInfo}
     * reply names a font by its XLFD, so a client that lists fonts and then asks about the name it
     * was handed - which is what Xlib's font-set machinery does, one request after the other - has
     * to be able to find it. Naming a font that this server cannot then list is what made
     * {@code XCreateFontSet} fail with every charset missing (backlog C10, measured 2026-09-26).
     */
    public static java.util.List<String> names() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Font font : java.util.List.of(FIXED, FONT_7X13, FONT_9X15, FONT_10X20, FONT_12X24)) {
            names.add(font.xlfd());
        }
        return names;
    }

    private BitmapFont() {
    }
}
