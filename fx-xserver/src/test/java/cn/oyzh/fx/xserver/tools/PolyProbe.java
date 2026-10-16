package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * Draws a fixed table of filled polygons and prints the pixels they produced, so two servers' scan
 * conversion can be compared line by line.
 *
 * <pre>
 *   PolyProbe &lt;host&gt; &lt;port&gt;
 * </pre>
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1). It exists because of the 0.8%
 * difference the E3 work recorded between this server's {@code xlogo} and Xvfb's: "the deltas are
 * full-scale, so it is geometry rather than blend rounding", and the geometry of a filled shape is
 * exactly what a server's polygon filler decides. A request log settles which primitive is involved
 * - {@code xlogo} sends five {@code FillPoly} and no arcs or lines at all - but not *where* the two
 * fills disagree, and a drawn shape is the only place a scan-conversion rule is visible.
 *
 * <p>The polygons are chosen to put the crossing points at every kind of fraction, because that is
 * where candidate rules differ: an intersection whose fractional part is below a half is the only
 * place {@code ceil(x)} and "the pixel whose centre is inside" give different first pixels. The
 * slopes run from 1 (a control, where every rule agrees) through 2, 44/21, 44/17, 44/45 and a
 * quadrilateral with no axis-aligned edge, and the last cell is a sixteen-gon - what a filled arc
 * and every toolkit's rounded shape is.
 *
 * <p>The pixels are read back with {@code GetImage} rather than captured with {@code xwd}, so the
 * output is text: run it against both servers and diff.
 */
public final class PolyProbe {

    /** One cell per polygon; the window is these many cells across and down. */
    private static final int CELL = 64;
    private static final int COLUMNS = 4;
    private static final int ROWS = 3;

    private static final int WHITE = 0xFFFFFF;

    /**
     * The five polygons {@code xlogo} fills, in the coordinates it sends them - read off this
     * server's own request log rather than reconstructed, because the whole point is to reproduce
     * the one shape both servers are known to disagree about. The window is the 220x220 one xlogo
     * makes at +60+60, so these are screen coordinates with the window's origin added.
     */
    private static final int[][] XLOGO = {
        // The four arms of the X, then the middle where they cross.
        {281, 61, 236, 61, 61, 281, 106, 281},
        {83, 281, 171, 171, 194, 171, 106, 281},
        {259, 61, 171, 171, 148, 171, 236, 61},
        {61, 61, 116, 61, 281, 281, 226, 281},
        {261, 61, 256, 61, 81, 281, 86, 281},
    };

    /** Where xlogo's window sits, so the same shape lands on the same pixels. */
    private static final int XLOGO_X = 60;
    private static final int XLOGO_Y = 60;
    private static final int XLOGO_SIZE = 220;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: PolyProbe <host> <port> [xlogo]  (xlogo: its window, its border, its polygons)");
            return;
        }
        boolean xlogo = args.length > 2 && args[2].equals("xlogo");
        int width = xlogo ? XLOGO_SIZE : CELL * COLUMNS;
        int height = xlogo ? XLOGO_SIZE : CELL * ROWS;
        int atX = xlogo ? XLOGO_X : 0;
        int atY = xlogo ? XLOGO_Y : 0;
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int window = client.allocateId();
            // The window is the whole probe: white background, one pixel of border, exactly what
            // xlogo makes - so a difference cannot come from the window's own frame.
            client.createWindow(window, client.root(), atX, atY, width, height, WHITE,
                    xlogo ? 1 : 0);
            int gc = client.createGC(client.allocateId(), client.root(), 0x000000);
            // Map *before* drawing, which is what every client does and what the first version of
            // this probe got wrong: a window's background is painted when it becomes viewable, so
            // anything drawn before the map is painted over - and a probe whose drawing never landed
            // reads as "the two servers agree", with no ink in the output at all to say otherwise.
            client.mapWindow(window);
            Thread.sleep(400);
            if (xlogo) {
                for (int[] shape : XLOGO) {
                    client.fillPolygon(window, gc, shape);
                }
            } else {
                for (int cell = 0; cell < COLUMNS * ROWS; cell++) {
                    int[] shape = polygon(cell, (cell % COLUMNS) * CELL, (cell / COLUMNS) * CELL);
                    if (shape != null) {
                        client.fillPolygon(window, gc, shape);
                    }
                }
            }
            Thread.sleep(400);

            byte[] reply = client.getImage(window, 0, 0, width, height);
            if (reply.length < 32 + width * height * 4) {
                System.out.println("GetImage answered " + reply.length + " bytes, expected at least "
                        + (32 + width * height * 4));
                return;
            }
            System.out.printf("window %dx%d at %d,%d, black = filled%n",
                    width, height, atX, atY);
            if (xlogo) {
                printRows(reply, width, 0, width, 0, height);
            } else {
                for (int cell = 0; cell < COLUMNS * ROWS; cell++) {
                    System.out.println("cell " + cell + ":");
                    printRows(reply, width, (cell % COLUMNS) * CELL, CELL,
                            (cell / COLUMNS) * CELL, CELL);
                }
            }
        }
    }

    /** Prints a rectangle of the read-back image: '#' where the fill landed, '.' where it did not. */
    private static void printRows(byte[] reply, int width, int x0, int w, int y0, int h) {
        for (int y = y0; y < y0 + h; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = x0; x < x0 + w; x++) {
                int at = 32 + (y * width + x) * 4;
                int red = reply[at + 2] & 0xFF;
                line.append(red < 128 ? '#' : '.');
            }
            System.out.println("  " + line);
        }
    }

    /**
     * The polygon drawn in each cell, in cell-local coordinates offset by the cell's origin.
     *
     * <p>Every one of them starts two pixels in and stops two pixels short of the cell's edge, so no
     * shape touches its neighbour and the clipping is never what a difference is about.
     */
    private static int[] polygon(int cell, int ox, int oy) {
        return switch (cell) {
            // A control: 45 degrees, where every candidate rule gives the same pixels.
            case 0 -> points(ox, oy, 2, 2, 46, 2, 2, 46);
            // Slope 2, then slopes just off it - each puts the row crossings at other fractions.
            case 1 -> points(ox, oy, 2, 2, 46, 2, 2, 24);
            case 2 -> points(ox, oy, 2, 2, 46, 2, 2, 23);
            case 3 -> points(ox, oy, 2, 2, 46, 2, 2, 19);
            // The same shape the other way up, so a rule that is right on one side has to be right
            // on the other: this is the case a "one end rounds up, the other down" mistake shows in.
            case 4 -> points(ox, oy, 2, 46, 46, 46, 2, 2);
            // Almost 45 degrees: the crossings move by about one every forty five rows.
            case 5 -> points(ox, oy, 2, 2, 46, 2, 2, 47);
            // No axis-aligned edge at all, and no two edges parallel.
            case 6 -> points(ox, oy, 2, 2, 46, 10, 40, 46, 8, 30);
            // A sixteen-gon: what every arc and every rounded toolkit shape is drawn as.
            case 7 -> circle(ox + 32, oy + 32, 22, 16);
            // The concave star the in-process test uses, which is where a half-pixel scanline
            // changes the *area* of a shape rather than one edge: Xorg fills 34 of its pixels with
            // the even-odd rule where the old filler here filled 28.
            case 8 -> points(ox, oy, 6, 0, 9, 11, 0, 4, 12, 4, 3, 11);
            // A concave arrow, for the same reason with a different set of fractional crossings.
            case 9 -> points(ox, oy, 2, 20, 30, 2, 58, 20, 42, 20, 58, 40, 30, 22, 2, 40);
            default -> null;
        };
    }

    private static int[] points(int ox, int oy, int... coordinates) {
        int[] shape = coordinates.clone();
        for (int i = 0; i < shape.length; i += 2) {
            shape[i] += ox;
            shape[i + 1] += oy;
        }
        return shape;
    }

    /** A regular polygon of {@code sides} sides, which is how a filled circle is drawn. */
    private static int[] circle(int cx, int cy, int radius, int sides) {
        int[] shape = new int[sides * 2];
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            shape[2 * i] = (int) Math.round(cx + radius * Math.cos(angle));
            shape[2 * i + 1] = (int) Math.round(cy + radius * Math.sin(angle));
        }
        return shape;
    }
}
