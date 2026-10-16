package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * Prints what a server answers about a font, and the pixels a fixed string draws in it, so two
 * servers can be compared line by line.
 *
 * <pre>
 *   FontProbe &lt;host&gt; &lt;port&gt; [font-name]
 * </pre>
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1) and the harness for D3, which is
 * about serving the font a client actually asked for rather than a Java approximation of it. Three
 * things are what a client can see, and all three are printed:
 *
 * <ul>
 *   <li>{@code QueryFont}'s own metrics - the bounds, the ascent and descent, the properties and
 *       every character's left and right bearing, width, ascent and descent. This is what a client
 *       that lays text out reads, so getting a field subtly wrong mis-lays every string it draws.
 *   <li>{@code QueryTextExtents}, which is the same question asked about a string.
 *   <li>The pixels: the string drawn with {@code ImageText8} into a mapped window and read back with
 *       {@code GetImage}, printed as text. That is the half the metrics cannot check - the shapes.
 * </ul>
 *
 * <p>The pixels are read from a window rather than from a one-bit pixmap, and the string is drawn
 * white on a black background: a depth 1 pixmap answers text with the font's <em>default character</em>
 * for every code on a reference server (measured while this probe was written - a solid block for a
 * space included, which reads as a font that agrees with itself and is wrong).
 */
public final class FontProbe {

    /** A string with ascenders, descenders, digits and punctuation in it. */
    private static final String DEFAULT_TEXT = "Hello, World 123!";
    /**
     * The string whose pixels are compared. An argument rather than a constant because a font's
     * glyphs are not all equally good: the first version of this drew one string, and the character
     * that was wrong in the table - 'B' - was not in it (found 2026-09-26 through the cursor probe,
     * of all things).
     */
    private static String text = DEFAULT_TEXT;

    private static final int WINDOW_WIDTH = 200;
    private static final int WINDOW_HEIGHT = 30;

    /**
     * The properties whose value is the atom of a string: X's own convention, and the same list the
     * generator uses - {@code QueryFont} answers both kinds as an atom and a CARD32, so which is
     * which is not in the reply.
     */
    private static final java.util.Set<String> STRING_PROPERTIES = java.util.Set.of(
            "FOUNDRY", "FAMILY_NAME", "WEIGHT_NAME", "SLANT", "SETWIDTH_NAME", "ADD_STYLE_NAME",
            "CHARSET_REGISTRY", "CHARSET_ENCODING", "COPYRIGHT", "NOTICE", "FONT",
            "FONTNAME_REGISTRY", "SPACING", "_GBDFED_INFO");

    /** One pixel of margin, so a glyph with a negative left bearing is not clipped away. */
    private static final int PEN_X = 4;
    private static final int BASELINE = 20;

    private FontProbe() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: FontProbe <host> <port> [font-name] [text]");
            return;
        }
        String name = args.length > 2 ? args[2] : "fixed";
        text = args.length > 3 ? args[3] : DEFAULT_TEXT;
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            System.out.println("font: " + name);
            int font = client.allocateId();
            client.openFont(font, name);
            printQueryFont(client, font);
            printTextExtents(client, font);

            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT,
                    0x000000);
            client.selectInput(window, 0x8000);         // no events wanted, but a round trip follows
            client.mapWindow(window);
            client.queryInputFocus();                   // the map has been processed after this
            int gc = client.allocateId();
            client.createGC(gc, window, 0xFFFFFF);      // white ink
            client.changeGC(gc, RawXClient.GCFONT, font);
            byte[] ink = text.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
            client.imageText8(window, gc, PEN_X, BASELINE, ink, 0, ink.length);
            client.flush();
            printPixels(client.getImage(window, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, 0xFFFFFF));
        }
    }

    /** {@code QueryFont}, field by field: the bounds, the metrics and the properties. */
    private static void printQueryFont(RawXClient client, int font) throws Exception {
        byte[] reply = client.queryFont(font);
        System.out.printf("  min-bounds: %s%n", metric(reply, 8));
        System.out.printf("  max-bounds: %s%n", metric(reply, 24));
        System.out.printf("  min-char-or-byte2=%d max-char-or-byte2=%d default-char=%d%n",
                RawXClient.readShort(reply, 40), RawXClient.readShort(reply, 42),
                RawXClient.readShort(reply, 44));
        System.out.printf("  draw-direction=%d all-chars-exist=%d%n", reply[48] & 0xFF,
                reply[51] & 0xFF);
        System.out.printf("  font-ascent=%d font-descent=%d%n", (short) RawXClient.readShort(reply, 52),
                (short) RawXClient.readShort(reply, 54));
        int properties = RawXClient.readShort(reply, 46);
        int chars = RawXClient.readInt(reply, 56);
        System.out.printf("  properties=%d char-infos=%d%n", properties, chars);
        int at = 60;
        for (int i = 0; i < properties; i++) {
            int atom = RawXClient.readInt(reply, at);
            int value = RawXClient.readInt(reply, at + 4);
            String property = client.atomName(atom);
            if (STRING_PROPERTIES.contains(property)) {
                // The value is the atom of a string: print the string, because the atom's *number*
                // is the server's to assign and comparing two servers' numbers compares nothing.
                System.out.printf("    prop %s=\"%s\"%n", property, client.atomName(value));
            } else {
                System.out.printf("    prop %s=%d%n", property, value);
            }
            at += 8;
        }
        for (int ch = 0; ch < chars; ch++) {
            System.out.printf("    char 0x%02x %s%n", ch, metric(reply, at));
            at += 12;
        }
    }

    private static String metric(byte[] reply, int at) {
        return String.format("lb=%d rb=%d width=%d asc=%d desc=%d attr=0x%x",
                (short) RawXClient.readShort(reply, at), (short) RawXClient.readShort(reply, at + 2),
                (short) RawXClient.readShort(reply, at + 4),
                (short) RawXClient.readShort(reply, at + 6),
                (short) RawXClient.readShort(reply, at + 8),
                RawXClient.readShort(reply, at + 10) & 0xFFFF);
    }

    /** {@code QueryTextExtents} of {@link #text}, as {@code (0, ch)} pairs the way Xlib sends them. */
    private static void printTextExtents(RawXClient client, int font) throws Exception {
        byte[] chars = text.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        // The request's length *is* the string: everything after the eight byte header is read as
        // Char2b pairs, and the odd-length flag says the last two bytes are padding. Sizing it as
        // "two units plus the characters" makes a server read the zeros after the string as text -
        // measured: a 17 character string came back 210 pixels wide, which is 35 characters.
        int units = (8 + chars.length * 2 + (chars.length % 2 == 1 ? 2 : 0)) / 4;
        byte[] request = new byte[units * 4];
        request[0] = 48;                            // QueryTextExtents
        request[1] = (byte) (chars.length % 2 == 1 ? 1 : 0);
        RawXClient.writeShortAt(request, 2, units);
        RawXClient.writeIntAt(request, 4, font);
        for (int i = 0; i < chars.length; i++) {
            request[8 + i * 2] = 0;                 // byte1: Xlib puts the character in the low byte
            request[9 + i * 2] = chars[i];
        }
        client.sendRequest(request);
        byte[] reply = client.readReply();
        System.out.printf("  extents(%s): ascent=%d descent=%d overall-ascent=%d overall-descent=%d"
                        + " width=%d left=%d right=%d%n", text,
                (short) RawXClient.readShort(reply, 8), (short) RawXClient.readShort(reply, 10),
                (short) RawXClient.readShort(reply, 12), (short) RawXClient.readShort(reply, 14),
                RawXClient.readInt(reply, 16), RawXClient.readInt(reply, 20),
                RawXClient.readInt(reply, 24));
    }

    /** The drawn pixels, as text: one character a pixel, so two servers are diffed as text. */
    private static void printPixels(byte[] reply) {
        int width = WINDOW_WIDTH;
        int height = WINDOW_HEIGHT;
        int stride = width * 4;
        for (int row = 0; row < height; row++) {
            StringBuilder line = new StringBuilder();
            for (int column = 0; column < width; column++) {
                int at = 32 + row * stride + column * 4;
                boolean white = at + 2 < reply.length
                        && (reply[at] & 0xFF) == 0xFF && (reply[at + 1] & 0xFF) == 0xFF
                        && (reply[at + 2] & 0xFF) == 0xFF;
                line.append(white ? '#' : '.');
            }
            String rendered = line.toString();
            if (rendered.contains("#")) {
                System.out.printf("  row %02d %s%n", row, rendered.replaceAll("\\.+$", ""));
            }
        }
    }
}
