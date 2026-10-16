package cn.oyzh.fx.xserver.display;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;

/**
 * The ZRLE encoding (RFB 16): the framebuffer cut into 64x64 tiles, each tile written the cheapest
 * way it fits, and the whole lot run through one zlib stream.
 *
 * <p>Backlog D2's bandwidth half. Raw frames are what this server sends by default and they are four
 * megabytes for a full 1300x800 screen - fine on a loopback or a LAN, painful on a phone. ZRLE is
 * the encoding every VNC client understands after raw, and for the thing this feature is for - a
 * terminal, a dialog, a desktop of flat colours - it turns that four megabytes into tens of
 * kilobytes.
 *
 * <p><b>Three things about the wire format are easy to get wrong, and each is written down here
 * because a mistake in any of them produces a picture that is subtly wrong rather than an error.</b>
 *
 * <ul>
 *   <li><b>A CPIXEL is not a pixel.</b> For a 32 bit format whose three channels live in the low
 *       three bytes - which is every client that asks for 32bpp little endian - the fourth byte is
 *       dropped and each pixel travels in **three** bytes. A client that expects three and gets four
 *       reads a garbled screen.
 *   <li><b>The zlib stream is the connection's, not the update's.</b> One {@link Deflater} per
 *       session, flushed (not finished) after every tile set, so successive updates share its
 *       dictionary.
 *   <li><b>A run never crosses a tile.</b> Each tile is encoded independently, and a run of the same
 *       colour is truncated at the tile's edge rather than continued into the next tile.
 * </ul>
 *
 * <p>What it writes: solid, packed palette (2..16 colours), plain RLE and raw - the four subencodings
 * a screen actually needs. Palette RLE (129..255) is not emitted; a client only has to decode what
 * it is sent.
 */
final class ZrleEncoder {

    /** ZRLE's tile, from the spec: 64 pixels square, and smaller at the right and bottom edges. */
    private static final int TILE = 64;

    /**
     * The most colours a packed-palette tile can carry, from the spec's subencoding range.
     *
     * <p>The palette search stops here, and that is the whole point of the number: past sixteen
     * colours the tile can only be plain RLE or raw, and neither of those looks at the palette - so
     * looking each following pixel up in it is pure work. Measured on full 1300x800 frames (min of 36
     * runs each, 2026-10-01): a photo-like frame, where nearly every tile has more than sixteen
     * colours, went from **79-100 ms to 57 ms**; a two-colour frame (10 ms) and a text-like one
     * (13.5 ms) did not move, which is the half of the measurement that matters - the common case must
     * not pay for the rare one. The numbers and the harness are in `docs/review-2026-10-01.md`.
     */
    private static final int PALETTE_LIMIT = 16;

    private ZrleEncoder() {
    }

    /**
     * One update's worth of ZRLE data, compressed and ready to send.
     *
     * @param argb    the region's pixels, row by row
     * @param width   the region's width
     * @param height  the region's height
     * @param packer  turns an ARGB pixel into what the client's format wants - the same conversion
     *                the raw path uses, so the two cannot disagree about a colour
     * @param cpxel   how many bytes the client's pixels take in this encoding (3 for the common
     *                32bpp format, see the class comment)
     * @param deflate the session's own zlib stream
     */
    static byte[] encode(int[] argb, int width, int height, java.util.function.IntUnaryOperator
            packer, int cpxel, Deflater deflate) {
        return encode(argb, width, height, packer, cpxel, deflate, new int[TILE * TILE]);
    }

    /**
     * The same, with the tile buffer owned by the caller.
     *
     * <p>{@code scratch} holds one tile's packed pixels and must be at least {@code TILE * TILE}
     * long; a session keeps one and passes it here, because a tile allocates its own otherwise and a
     * full 1300x800 frame is 273 tiles - a few megabytes of garbage for every frame an application
     * redraws, which a terminal scrolling at 60 Hz produces for as long as the user scrolls.
     */
    static byte[] encode(int[] argb, int width, int height, java.util.function.IntUnaryOperator
            packer, int cpxel, Deflater deflate, int[] scratch) {
        ByteArrayOutputStream raw = new ByteArrayOutputStream(Math.max(64, width * height / 2));
        for (int tileY = 0; tileY < height; tileY += TILE) {
            int tileHeight = Math.min(TILE, height - tileY);
            for (int tileX = 0; tileX < width; tileX += TILE) {
                int tileWidth = Math.min(TILE, width - tileX);
                encodeTile(raw, argb, width, tileX, tileY, tileWidth, tileHeight, packer, cpxel,
                        scratch);
            }
        }

        deflate.setInput(raw.toByteArray());
        ByteArrayOutputStream compressed = new ByteArrayOutputStream(raw.size() / 4 + 16);
        byte[] buffer = new byte[16384];
        // Z_SYNC_FLUSH and not Z_FINISH: the stream has to stay usable for the next update. The loop
        // runs until the input is consumed **and** the output buffer came back not-full: a deflate
        // call can return zero bytes with input still pending, and treating that as "done" truncates
        // the stream - measured, on a real 1300x800 desktop, as exactly four full buffers followed by
        // a client that gave up. A small frame never fills one buffer, which is why the tile-sized
        // tests passed while the live check failed.
        int count;
        do {
            count = deflate.deflate(buffer, 0, buffer.length, Deflater.SYNC_FLUSH);
            if (count > 0) {
                compressed.write(buffer, 0, count);
            }
        } while (!deflate.needsInput() || count == buffer.length);
        return compressed.toByteArray();
    }

    /** One tile: the cheapest of the four subencodings, then its pixels. */
    private static void encodeTile(ByteArrayOutputStream out, int[] argb, int stride, int tileX,
                                   int tileY, int width, int height,
                                   java.util.function.IntUnaryOperator packer, int cpxel,
                                   int[] tile) {
        // The palette first, up to the seventeen it takes to know the tile is not a palette tile:
        // `colours == 1` is the solid case, up to PALETTE_LIMIT settles the packed-palette case, and
        // one colour past the limit is what tells this code to stop looking pixels up in it.
        int[] palette = new int[PALETTE_LIMIT + 1];
        int colours = 0;
        int pixels = width * height;
        int at = 0;
        for (int y = 0; y < height; y++) {
            int row = (tileY + y) * stride + tileX;
            for (int x = 0; x < width; x++) {
                int packed = packer.applyAsInt(argb[row + x]);
                tile[at++] = packed;
                if (colours > PALETTE_LIMIT) {
                    continue;                       // a palette tile is out of the question now
                }
                int found = -1;
                for (int i = 0; i < colours; i++) {
                    if (palette[i] == packed) {
                        found = i;
                        break;
                    }
                }
                if (found < 0) {
                    palette[colours++] = packed;
                }
            }
        }

        if (colours == 1) {
            out.write(1);                                   // solid
            writePixels(out, palette[0], 1, cpxel);
            return;
        }

        // Runs: the length of the longest run of one colour, which decides between plain RLE and a
        // packed palette (a palette wastes bits per pixel; RLE wastes a byte per change).
        int longestRun = 1;
        int run = 1;
        for (int i = 1; i < pixels; i++) {
            run = tile[i] == tile[i - 1] ? run + 1 : 1;
            longestRun = Math.max(longestRun, run);
        }
        int bits = colours == 2 ? 1 : colours <= 4 ? 2 : 4;
        int packedBytes = (pixels * bits + 7) / 8;
        int rleBytes = pixels / Math.max(1, longestRun) * (cpxel + 1) + (pixels % longestRun > 0
                ? cpxel + 1 : 0);

        if (colours <= 16 && packedBytes <= rleBytes) {
            out.write(colours);                             // packed palette, 2..16
            for (int i = 0; i < colours; i++) {
                writePixels(out, palette[i], 1, cpxel);
            }
            int accumulator = 0;
            int bitsUsed = 0;
            for (int i = 0; i < pixels; i++) {
                int index = indexOf(palette, colours, tile[i]);
                accumulator = (accumulator << bits) | index;
                bitsUsed += bits;
                if (bitsUsed == 8) {
                    out.write(accumulator);
                    accumulator = 0;
                    bitsUsed = 0;
                }
            }
            if (bitsUsed > 0) {
                // The last byte is padded with zeros at the low end, which is what the spec's
                // "the remaining bits are unused" means for a reader that shifts from the top.
                out.write(accumulator << (8 - bitsUsed));
            }
            return;
        }

        if (longestRun >= 2) {
            out.write(128);                                 // plain RLE
            int i = 0;
            while (i < pixels) {
                int colour = tile[i];
                int length = 1;
                while (i + length < pixels && tile[i + length] == colour) {
                    length++;
                }
                writePixels(out, colour, 1, cpxel);
                int remaining = length - 1;
                while (remaining >= 255) {
                    out.write(255);
                    remaining -= 255;
                }
                out.write(remaining);
                i += length;
            }
            return;
        }

        out.write(0);                                       // raw
        for (int i = 0; i < pixels; i++) {
            writePixels(out, tile[i], 1, cpxel);
        }
    }

    private static int indexOf(int[] palette, int colours, int value) {
        for (int i = 0; i < colours; i++) {
            if (palette[i] == value) {
                return i;
            }
        }
        return 0;
    }

    /** One pixel of the client's format, in as many bytes as a CPIXEL takes. */
    private static void writePixels(ByteArrayOutputStream out, int value, int count, int cpxel) {
        for (int i = 0; i < count; i++) {
            for (int b = 0; b < cpxel; b++) {
                out.write((value >> (8 * b)) & 0xFF);
            }
        }
    }
}
