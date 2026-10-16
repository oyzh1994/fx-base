package cn.oyzh.fx.xserver.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * A PCF font file, read into the table {@code XFont} serves - which is the same
 * {@link BitmapFont.Font} the generated tables carry, so nothing downstream knows where a font came
 * from.
 *
 * <p>This is the other half of D3: the built-in tables cover the names a client is most likely to
 * ask for, and a <b>font file on a font path</b> covers every other name an X server would answer.
 * The format is read off {@code libXfont}'s {@code src/bitmap/pcfread.c} rather than guessed, and
 * every field below has a comment saying what it is because several of them are not where the shape
 * of the file suggests.
 *
 * <p><b>What the format actually looks like</b>, in the order it matters:
 *
 * <ul>
 *   <li>The table of contents is <b>little endian whatever the rest of the file is</b>: the version,
 *       a count, then (type, format, size, offset) per section. Each section's own <em>format</em>
 *       word says how to read that section's payload: bit 2 is the byte order, bit 3 the bit order,
 *       bits 0-1 the glyph padding (one, two, four or eight bytes a row) and bits 4-5 the scan unit.
 *   <li>The <b>font's own header is in the {@code ACCELERATORS} section</b>, not in the metrics one
 *       where it sounds like it should be: eight flag bytes, then the ascent, the descent and the
 *       maximum overlap, then two (or four) sets of bounds. It is the same layout read under the
 *       {@code BDF_ACCELERATORS} type, which a font may have instead.
 *   <li>The metrics section is a count and then that many metrics, and a "compressed" metrics
 *       section - which is the common one - stores each of the five numbers in <b>one byte, biased by
 *       0x80</b>, and its <b>count is sixteen bits rather than thirty-two</b>. Both are measured
 *       facts about the format, and either one read the other way gives plausible nonsense.
 *   <li>A font file may hold <b>far more glyphs than a character set has</b> - the {@code misc}
 *       fonts keep several charsets in one file, and 8x13 has 3703 glyphs for its 256 codes. The
 *       <b>encodings</b> section is what says which glyph a code means, and it is the only thing that
 *       can be used for that: the metrics are in glyph order.
 * </ul>
 */
public final class PcfFont {

    /**
     * Section types, as the format numbers them - and the two that are easy to mix up are the last
     * two: <b>the encodings are 32 and the accelerators 256</b> ({@code PCF_BDF_ENCODINGS} is
     * {@code 1 << 5} and {@code PCF_BDF_ACCELERATORS} is {@code 1 << 8}). Reading them the other way
     * round finds the font header where the character map should be, and every field decodes as
     * nonsense - which is how this was found, on a real file whose section 256 turned out to be
     * eight flag bytes, an ascent of eleven and a descent of two.
     */
    private static final int PROPERTIES = 1;
    private static final int ACCELERATORS = 2;
    private static final int METRICS = 4;
    private static final int BITMAPS = 8;
    private static final int INK_METRICS = 16;
    private static final int BDF_ENCODINGS = 32;
    private static final int BDF_ACCELERATORS = 256;

    /** {@code PCF_COMPRESSED_METRICS}: the five metrics in one byte each, biased by 0x80. */
    private static final int COMPRESSED_METRICS = 0x100;
    /** {@code PCF_ACCEL_W_INKBOUNDS}: the accelerators carry ink bounds as well. */
    private static final int ACCEL_W_INKBOUNDS = 0x200;

    /** The bias a compressed metric carries, and the reason the value is a byte: 0x80 is zero. */
    private static final int METRIC_BIAS = 0x80;

    /** What {@code XFont} and {@code BitmapFont} call a character no key has. */
    private static final int NO_SUCH_CODE = 0xFFFF;

    private final byte[] data;
    private final String name;

    private PcfFont(byte[] data, String name) {
        this.data = data;
        this.name = name;
    }

    /**
     * Reads a font file - plain or gzipped, which is how the X fonts on a Linux machine are kept -
     * into the table a font resource serves. Never null: a file that cannot be read is an exception
     * the caller turns into a refusal, because a server that answers {@code OpenFont} with a font it
     * invented is worse than one that says the name is unknown.
     */
    public static BitmapFont.Font read(InputStream stream, String name) throws IOException {
        byte[] raw = stream.readAllBytes();
        byte[] data = raw.length > 2 && raw[0] == (byte) 0x1f && raw[1] == (byte) 0x8b
                ? new GZIPInputStream(new ByteArrayInputStream(raw)).readAllBytes()
                : raw;
        return new PcfFont(data, name).parse();
    }

    // ------------------------------------------------------------------
    // The table of contents
    // ------------------------------------------------------------------

    /** One section: what it is, how its payload is laid out, and where it starts. */
    private record Section(int type, int format, int size, int offset) {
    }

    private List<Section> sections() {
        if (data.length < 8 || ascii(0, 4).equals("fcp") == false) {
            throw new IllegalArgumentException("not a PCF file");
        }
        int count = int32Little(4);
        if (count < 1 || count > 64 || 8 + count * 16 > data.length) {
            throw new IllegalArgumentException("a PCF file with " + count + " sections");
        }
        List<Section> sections = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int at = 8 + i * 16;
            sections.add(new Section(int32Little(at), int32Little(at + 4), int32Little(at + 8),
                    int32Little(at + 12)));
        }
        return sections;
    }

    private Section section(List<Section> sections, int... types) {
        for (int type : types) {
            for (Section section : sections) {
                if (section.type() == type) {
                    return section;
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // The font
    // ------------------------------------------------------------------

    private BitmapFont.Font parse() {
        List<Section> sections = sections();
        Header header = header(sections);
        // What {@code QueryFont} reports per character is the *ink* metrics where the file has
        // them, and this is the difference between a client laying text out right and not: the
        // plain metrics table of `8x13-ISO8859-1` says every glyph is the whole cell, and its ink
        // table says what each one actually covers - which is what a reference server answers
        // (measured: code 0x41 is lb=1 rb=7 asc=9 desc=0 from the ink table, and 0,8,8,11,2 from the
        // plain one).
        Metrics metrics = metrics(sections, INK_METRICS);
        if (metrics == null) {
            metrics = metrics(sections, METRICS);
        }
        int[][][] ink = bitmaps(sections, metrics);
        Encoding encoding = encoding(sections);
        Properties properties = properties(sections, header);

        // The code range this file answers for, and one entry per code in it - which is the shape
        // XFont serves and QueryFont reports.
        int minChar = Math.min(encoding.firstCol(), 0xFF);
        int maxChar = Math.min(encoding.lastCol(), 0xFF);
        BitmapFont.Metric[] chars = new BitmapFont.Metric[maxChar - minChar + 1];
        byte[][] packed = new byte[chars.length][];
        for (int code = minChar; code <= maxChar; code++) {
            int glyph = encoding.glyphFor(code);
            int index = code - minChar;
            if (glyph < 0) {
                // A code the font does not have answers with *nothing* - not the default character's
                // metrics, which was the guess here and is measurably wrong: a reference server
                // reports lb=0 rb=0 width=0 asc=0 desc=0 for 0x7f and every code above it in a font
                // whose encoding table says they are not there (8x13-ISO8859-1).
                chars[index] = new BitmapFont.Metric(0, 0, 0, 0, 0, 0);
                continue;
            }
            BitmapFont.Metric metric = metrics.metrics()[glyph];
            chars[index] = metric;
            packed[index] = ink[glyph] == null ? null : pack(ink[glyph]);
        }
        return new BitmapFont.Font(name, properties.xlfd() == null ? name : properties.xlfd(),
                header.ascent(), header.descent(), encoding.defaultChar(), minChar, maxChar,
                minBounds(metrics, header), maxBounds(metrics, header), chars, packed,
                properties.names(),
                properties.values(), properties.strings());
    }

    /**
     * The bounds {@code QueryFont} reports, computed the way a reference server computes them:
     * <b>each field is the extreme of that field over the file's glyphs</b> - all of them, not only
     * the ones the encoding table maps - and the header's own bounds are only the starting point.
     * Both halves are measured: 8x13's header says its widest left bearing is zero where the glyphs'
     * is three, and a font whose codes above 0x7f are unmapped still counts their glyphs here.
     */
    private static BitmapFont.Metric minBounds(Metrics metrics, Header header) {
        BitmapFont.Metric bounds = header.minBounds();
        for (BitmapFont.Metric metric : metrics.metrics()) {
            if (metric == null) {
                continue;
            }
            bounds = new BitmapFont.Metric(Math.min(bounds.leftBearing(), metric.leftBearing()),
                    Math.min(bounds.rightBearing(), metric.rightBearing()),
                    Math.min(bounds.width(), metric.width()),
                    Math.min(bounds.ascent(), metric.ascent()),
                    Math.min(bounds.descent(), metric.descent()), 0);
        }
        return bounds;
    }

    private static BitmapFont.Metric maxBounds(Metrics metrics, Header header) {
        BitmapFont.Metric bounds = header.maxBounds();
        for (BitmapFont.Metric metric : metrics.metrics()) {
            if (metric == null) {
                continue;
            }
            bounds = new BitmapFont.Metric(Math.max(bounds.leftBearing(), metric.leftBearing()),
                    Math.max(bounds.rightBearing(), metric.rightBearing()),
                    Math.max(bounds.width(), metric.width()),
                    Math.max(bounds.ascent(), metric.ascent()),
                    Math.max(bounds.descent(), metric.descent()), 0);
        }
        return bounds;
    }

    /** The font's header, which lives in the accelerators section. */
    private record Header(int ascent, int descent, BitmapFont.Metric minBounds,
                          BitmapFont.Metric maxBounds) {
    }

    private record Metrics(BitmapFont.Metric[] metrics) {
    }

    private Header header(List<Section> sections) {
        Section section = section(sections, ACCELERATORS, BDF_ACCELERATORS);
        if (section == null) {
            throw new IllegalArgumentException("a PCF file with no accelerators section");
        }
        int at = section.offset();
        int format = int32Little(at);
        boolean msb = (format & 0x4) != 0;
        at += 4 + 8;                                // the eight flag bytes
        int ascent = int32(at, msb);
        int descent = int32(at + 4, msb);
        at += 12;                                   // ascent, descent and the maximum overlap
        BitmapFont.Metric min = metric(at, msb);
        BitmapFont.Metric max = metric(at + 12, msb);
        return new Header(ascent, descent, min, max);
    }

    private Metrics metrics(List<Section> sections, int type) {
        Section section = section(sections, type);
        if (section == null) {
            return null;
        }
        int at = section.offset();
        int format = int32Little(at);
        boolean msb = (format & 0x4) != 0;
        boolean compressed = (format & COMPRESSED_METRICS) != 0;
        at += 4;
        // The count's width is part of the format, and this is the trap the format sets: a
        // compressed metric is a byte, so its count is sixteen bits, and reading it as thirty-two
        // reads the first metric's bytes as part of the count.
        int count = compressed ? uint16(at, msb) : int32(at, msb);
        at += compressed ? 2 : 4;
        BitmapFont.Metric[] metrics = new BitmapFont.Metric[count];
        for (int i = 0; i < count; i++) {
            if (compressed) {
                int base = at + i * 5;
                metrics[i] = new BitmapFont.Metric(byteAt(base) - METRIC_BIAS,
                        byteAt(base + 1) - METRIC_BIAS, byteAt(base + 2) - METRIC_BIAS,
                        byteAt(base + 3) - METRIC_BIAS, byteAt(base + 4) - METRIC_BIAS, 0);
            } else {
                metrics[i] = metric(at + i * 12, msb);
            }
        }
        return new Metrics(metrics);
    }

    /**
     * Every glyph's ink, one array a glyph, in the file's own order and with the file's own bit and
     * byte order undone: the rows are packed left to right, most significant bit first, which is the
     * form {@link BitmapFont.Font#glyph} expands.
     */
    private int[][][] bitmaps(List<Section> sections, Metrics metrics) {
        Section section = section(sections, BITMAPS);
        if (section == null) {
            throw new IllegalArgumentException("a PCF file with no bitmaps section");
        }
        int at = section.offset();
        int format = int32Little(at);
        boolean msb = (format & 0x4) != 0;
        boolean msbBits = (format & 0x8) != 0;
        int glyphPad = 1 << (format & 0x3);
        at += 4;
        int count = int32(at, msb);
        at += 4;
        int[] offsets = new int[count];
        for (int i = 0; i < count; i++) {
            offsets[i] = int32(at + i * 4, msb);
        }
        at += count * 4;
        // Four sizes follow, one per glyph padding; the section's payload is the one the format
        // names, so the others are only worth skipping.
        int size = int32(at + (log2(glyphPad)) * 4, msb);
        int dataAt = at + 16;

        int[][][] ink = new int[count][][];
        for (int glyph = 0; glyph < count; glyph++) {
            BitmapFont.Metric metric = metrics.metrics()[glyph];
            int width = metric.rightBearing() - metric.leftBearing();
            int height = metric.ascent() + metric.descent();
            if (width <= 0 || height <= 0) {
                continue;                           // a space, or a glyph with no ink
            }
            int rowBytes = ((width * 1 + glyphPad * 8 - 1) / (glyphPad * 8)) * glyphPad;
            // The offsets are relative to the bitmap data, and one may be past the end of a font
            // that lies about its sizes - the reference reader checks the same way.
            int start = offsets[glyph];
            if (start < 0 || start + rowBytes * height > size) {
                continue;
            }
            int[][] rows = new int[height][width];
            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    int byteAt = dataAt + start + row * rowBytes + (column >> 3);
                    int bit = msbBits ? 7 - (column & 7) : (column & 7);
                    rows[row][column] = (data[byteAt] >> bit) & 1;
                }
            }
            ink[glyph] = rows;
        }
        return ink;
    }

    private static int log2(int value) {
        int index = 0;
        while ((1 << index) < value) {
            index++;
        }
        return index;
    }

    /** Which glyph a character code means, which only the encodings section knows. */
    private record Encoding(int firstCol, int lastCol, int firstRow, int lastRow, int defaultChar,
                            int[] glyphs) {

        int glyphFor(int code) {
            int row = (code >> 8) & 0xFF;
            int column = code & 0xFF;
            if (row < firstRow || row > lastRow || column < firstCol || column > lastCol) {
                return -1;
            }
            int value = glyphs[(row - firstRow) * (lastCol - firstCol + 1) + (column - firstCol)];
            return value == NO_SUCH_CODE ? -1 : value;
        }
    }

    private Encoding encoding(List<Section> sections) {
        Section section = section(sections, BDF_ENCODINGS);
        if (section == null) {
            throw new IllegalArgumentException("a PCF file with no encodings section");
        }
        int at = section.offset();
        int format = int32Little(at);
        boolean msb = (format & 0x4) != 0;
        at += 4;
        int firstCol = uint16(at, msb);
        int lastCol = uint16(at + 2, msb);
        int firstRow = uint16(at + 4, msb);
        int lastRow = uint16(at + 6, msb);
        int defaultChar = uint16(at + 8, msb);
        at += 10;
        if (firstCol > lastCol || firstRow > lastRow || lastCol - firstCol > 255) {
            throw new IllegalArgumentException("a PCF file with impossible encodings");
        }
        int count = (lastCol - firstCol + 1) * (lastRow - firstRow + 1);
        int[] glyphs = new int[count];
        for (int i = 0; i < count; i++) {
            glyphs[i] = uint16(at + i * 2, msb);
        }
        return new Encoding(firstCol, lastCol, firstRow, lastRow, defaultChar, glyphs);
    }

    /** The font's properties, in the file's own order and with the string ones resolved. */
    private record Properties(String[] names, int[] values, String[] strings, String xlfd) {
    }

    private Properties properties(List<Section> sections, Header header) {
        Section section = section(sections, PROPERTIES);
        if (section == null) {
            return new Properties(new String[0], new int[0], new String[0], null);
        }
        int at = section.offset();
        int format = int32Little(at);
        boolean msb = (format & 0x4) != 0;
        at += 4;
        int count = int32(at, msb);
        at += 4;
        int[] nameOffsets = new int[count];
        boolean[] isString = new boolean[count];
        int[] valueOffsets = new int[count];
        for (int i = 0; i < count; i++) {
            nameOffsets[i] = int32(at + i * 9, msb);
            isString[i] = byteAt(at + i * 9 + 4) != 0;
            valueOffsets[i] = int32(at + i * 9 + 5, msb);
        }
        at += count * 9 + (count % 4 == 0 ? 0 : 4 - (count % 4));
        int stringsSize = int32(at, msb);
        int stringsAt = at + 4;
        String[] names = new String[count];
        int[] values = new int[count];
        String[] strings = new String[count];
        String xlfd = null;
        for (int i = 0; i < count; i++) {
            names[i] = string(stringsAt, nameOffsets[i], stringsSize);
            if (isString[i]) {
                strings[i] = string(stringsAt, valueOffsets[i], stringsSize);
                if ("FONT".equals(names[i])) {
                    xlfd = strings[i];
                }
            } else {
                values[i] = valueOffsets[i];
            }
        }
        return new Properties(names, values, strings, xlfd);
    }

    /** Packs one glyph's rows the way the generated tables hold them: eight pixels to a byte. */
    private static byte[] pack(int[][] rows) {
        int height = rows.length;
        int width = rows[0].length;
        int stride = (width + 7) / 8;
        byte[] packed = new byte[stride * height];
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                if (rows[row][column] != 0) {
                    packed[row * stride + (column >> 3)] |= (byte) (1 << (column & 7));
                }
            }
        }
        return packed;
    }

    // ------------------------------------------------------------------
    // Bytes
    // ------------------------------------------------------------------

    private BitmapFont.Metric metric(int at, boolean msb) {
        return new BitmapFont.Metric((short) uint16(at, msb), (short) uint16(at + 2, msb),
                (short) uint16(at + 4, msb), (short) uint16(at + 6, msb), (short) uint16(at + 8, msb),
                uint16(at + 10, msb));
    }

    private String string(int base, int offset, int size) {
        if (offset < 0 || offset >= size) {
            return "";
        }
        int end = base + offset;
        while (end < base + size && data[end] != 0) {
            end++;
        }
        return new String(data, base + offset, end - base - offset,
                java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    /** The table of contents is little endian whatever the file's own byte order is. */
    private int int32Little(int at) {
        return (byteAt(at)) | (byteAt(at + 1) << 8) | (byteAt(at + 2) << 16) | (byteAt(at + 3) << 24);
    }

    private int int32(int at, boolean msb) {
        return msb ? (byteAt(at) << 24) | (byteAt(at + 1) << 16) | (byteAt(at + 2) << 8)
                | byteAt(at + 3)
                : int32Little(at);
    }

    private int uint16(int at, boolean msb) {
        return msb ? (byteAt(at) << 8) | byteAt(at + 1) : byteAt(at) | (byteAt(at + 1) << 8);
    }

    private int byteAt(int at) {
        return data[at] & 0xFF;
    }

    private String ascii(int at, int length) {
        return new String(data, at, length, java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    /** The whole file, for a caller that has already read it - {@link #read} takes a stream. */
    public static BitmapFont.Font read(byte[] bytes, String name) throws IOException {
        return read(new ByteArrayInputStream(bytes), name);
    }
}
