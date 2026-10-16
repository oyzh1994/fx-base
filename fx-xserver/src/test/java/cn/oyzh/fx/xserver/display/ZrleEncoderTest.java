package cn.oyzh.fx.xserver.display;

import cn.oyzh.fx.xserver.display.ZrleEncoder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ZRLE encoder, against a decoder written from the protocol description.
 *
 * <p>An encoder tested through the whole VNC stack tells you only that *something* is wrong; this is
 * where the subencodings, the CPIXEL width, the tile edges and the run lengths are pinned one at a
 * time. The decoder here is deliberately naive - it reads what RFC 6143 says is on the wire, in the
 * order it says - because the point is to catch the encoder agreeing with itself.
 */
class ZrleEncoderTest {

    /** What the encoder does to an ARGB pixel for this test's format: the low three bytes, as RGB. */
    private static final java.util.function.IntUnaryOperator PACK = argb -> argb & 0xFFFFFF;

    /** A 32 bit format's compressed pixel is three bytes, which is the trap this test exists for. */
    private static final int CPIXEL = 3;

    @Test
    void aSolidTileIsItsColourAndNothingElse() {
        int[] pixels = new int[64 * 64];
        java.util.Arrays.fill(pixels, 0x112233);
        byte[] zrle = encode(pixels, 64, 64);

        assertEquals(1, raw(zrle)[0], "one colour is the solid subencoding");
        assertArrayEquals(pixels, decode(zrle, 64, 64, PACK, pixels), "the colour fills the tile");
    }

    @Test
    void aTileOfTwoColoursKeepsBothWhereTheyWere() {
        int[] pixels = new int[64 * 64];
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                pixels[y * 64 + x] = y < 32 ? 0xFF0000 : 0x00FF00;
            }
        }
        byte[] zrle = encode(pixels, 64, 64);
        int[] decoded = decode(zrle, 64, 64, PACK, pixels);
        assertArrayEquals(pixels, decoded, "a two colour tile comes back where it was");
    }

    @Test
    void everyPixelOfAGradientSurvives() {
        // 4096 distinct colours is more than a palette can hold, so this is the raw path - and it is
        // the case that catches a CPIXEL of the wrong width, because everything shifts by one byte.
        int[] pixels = new int[64 * 64];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0x400000 | (i & 0x3FFF);
        }
        byte[] zrle = encode(pixels, 64, 64);
        int[] decoded = decode(zrle, 64, 64, PACK, pixels);
        assertArrayEquals(pixels, decoded, "a tile with too many colours is sent raw");
    }

    @Test
    void aLongRunOfOneColourSurvives() {
        // A run of 300 crosses the RLE encoding's 255 boundary, which is where a length that is read
        // as a single byte stops and the continuation begins.
        int[] pixels = new int[64 * 64];
        java.util.Arrays.fill(pixels, 0x0000FF);
        for (int i = 300; i < 320; i++) {
            pixels[i] = 0xFF00FF;
        }
        byte[] zrle = encode(pixels, 64, 64);
        int[] decoded = decode(zrle, 64, 64, PACK, pixels);
        assertArrayEquals(pixels, decoded, "a run longer than 255 comes back whole");
    }

    @Test
    void aFrameWiderThanOneTileIsTiledInOrder() {
        // 130x70 is two tiles across and two down, with the last row and column short - which is
        // where a tile size that is always 64 either loses pixels or reads past the end.
        int width = 130;
        int height = 70;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = (x < 64 ? 0x111111 : 0x222222) | (y < 64 ? 0 : 0x0000FF);
            }
        }
        byte[] zrle = encode(pixels, width, height);
        int[] decoded = decode(zrle, width, height, PACK, pixels);
        assertArrayEquals(pixels, decoded, "tiles at the edges are the size that is left");
    }

    /**
     * A frame big enough that the compressed output does not fit in one buffer.
     *
     * <p>The live check found this one: the deflate loop stopped when a call returned no bytes, so a
     * real desktop's stream was truncated after four output buffers - and every test that used a tile
     * or two passed, because their output never reached the second buffer. Noise is the point: it does
     * not compress, so the output is as large as the input.
     */
    @Test
    void aFrameWhoseCompressedOutputIsLargerThanOneBufferSurvives() {
        int width = 320;
        int height = 300;
        int[] pixels = new int[width * height];
        java.util.Random random = new java.util.Random(20260926L);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = random.nextInt(0x1000000);
        }
        byte[] zrle = encode(pixels, width, height);
        assertTrue(zrle.length > 65536, "noise should not compress: " + zrle.length + " bytes");
        int[] decoded = decode(zrle, width, height, PACK, pixels);
        assertArrayEquals(pixels, decoded, "a frame whose output spans many buffers");
    }

    @Test
    void theStreamIsReusableAcrossUpdates() {
        // One Deflater per session, and this is why: two encodes through the same stream must both
        // decompress, which a stream that was finished rather than flushed would not.
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        int[] first = new int[64 * 64];
        java.util.Arrays.fill(first, 0x00FF00);
        int[] second = new int[64 * 64];
        java.util.Arrays.fill(second, 0xFF0000);

        byte[] one = ZrleEncoder.encode(first, 64, 64, PACK, CPIXEL, deflater);
        byte[] two = ZrleEncoder.encode(second, 64, 64, PACK, CPIXEL, deflater);
        deflater.end();

        // One inflater for both, because the stream is one: the second update's bytes are a
        // continuation of the first, which is the whole point of keeping the Deflater per session.
        Inflater inflater = new Inflater();
        assertArrayEquals(first, decode(one, 64, 64, inflater), "the first update");
        assertArrayEquals(second, decode(two, 64, 64, inflater),
                "and the second through the same stream");
        assertTrue(two.length < 64 * 64 * CPIXEL, "a flat tile should not cost a pixel each");
    }

    // ------------------------------------------------------------------
    // The harness
    // ------------------------------------------------------------------

    private static byte[] encode(int[] pixels, int width, int height) {
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        byte[] zrle = ZrleEncoder.encode(pixels, width, height, PACK, CPIXEL, deflater);
        deflater.end();
        return zrle;
    }

    /** The ZRLE bytes before compression, which is where the subencodings are visible. */
    private static byte[] raw(byte[] compressed) {
        Inflater inflater = new Inflater();
        try {
            return raw(compressed, inflater);
        } finally {
            inflater.end();
        }
    }

    /** The inflated bytes of one update, through an inflater the caller keeps for the next one. */
    private static byte[] raw(byte[] compressed, Inflater inflater) {
        inflater.setInput(compressed);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        try {
            while (!inflater.needsInput()) {
                int count = inflater.inflate(buffer);
                if (count <= 0) {
                    break;
                }
                out.write(buffer, 0, count);
            }
        } catch (java.util.zip.DataFormatException e) {
            throw new IllegalStateException(e);
        }
        return out.toByteArray();
    }

    /** Decodes the tiles the way RFC 6143 describes them, into fully expanded pixels. */
    private static int[] decode(byte[] compressed, int width, int height,
                                java.util.function.IntUnaryOperator pack, int[] expected) {
        Inflater inflater = new Inflater();
        try {
            return decode(compressed, width, height, inflater);
        } finally {
            inflater.end();
        }
    }

    private static int[] decode(byte[] compressed, int width, int height, Inflater inflater) {
        byte[] raw = raw(compressed, inflater);
        int[] expected = new int[width * height];
        int[] pixels = new int[width * height];
        int at = 0;
        int written = 0;
        for (int tileY = 0; tileY < height; tileY += 64) {
            int tileHeight = Math.min(64, height - tileY);
            for (int tileX = 0; tileX < width; tileX += 64) {
                int tileWidth = Math.min(64, width - tileX);
                int count = tileWidth * tileHeight;
                int tileX0 = tileX;
                int tileY0 = tileY;
                int subencoding = raw[at++] & 0xFF;
                int[] tile = new int[count];
                if (subencoding == 0) {
                    for (int i = 0; i < count; i++) {
                        tile[i] = value(raw, at + i * CPIXEL);
                    }
                    at += count * CPIXEL;
                } else if (subencoding == 1) {
                    java.util.Arrays.fill(tile, value(raw, at));
                    at += CPIXEL;
                } else if (subencoding == 128) {
                    int i = 0;
                    while (i < count) {
                        int colour = value(raw, at);
                        at += CPIXEL;
                        int length = 1;
                        int b;
                        do {
                            b = raw[at++] & 0xFF;
                            length += b;
                        } while (b == 255);
                        for (int j = 0; j < length && i < count; j++, i++) {
                            tile[i] = colour;
                        }
                    }
                } else if (subencoding >= 2 && subencoding <= 16) {
                    int[] palette = new int[subencoding];
                    for (int i = 0; i < subencoding; i++) {
                        palette[i] = value(raw, at + i * CPIXEL);
                    }
                    at += subencoding * CPIXEL;
                    int bits = subencoding == 2 ? 1 : subencoding <= 4 ? 2 : 4;
                    int perByte = 8 / bits;
                    int mask = (1 << bits) - 1;
                    for (int i = 0; i < count; i++) {
                        int value = raw[at + i / perByte] & 0xFF;
                        int shift = 8 - bits * (i % perByte + 1);
                        tile[i] = palette[(value >> shift) & mask];
                    }
                    at += (count + perByte - 1) / perByte;
                } else {
                    throw new IllegalStateException("unsupported subencoding " + subencoding);
                }
                // Into the region's own coordinates: the tiles arrive in order, but a screen is
                // two dimensional and the reader above indexes it that way.
                for (int y = 0; y < tileHeight; y++) {
                    System.arraycopy(tile, y * tileWidth, pixels, (tileY0 + y) * width + tileX0,
                            tileWidth);
                }
                written += count;
            }
        }
        assertEquals(expected.length, written, "the tiles should cover the region exactly");
        return pixels;
    }

    private static int value(byte[] raw, int at) {
        return (raw[at] & 0xFF) | ((raw[at + 1] & 0xFF) << 8) | ((raw[at + 2] & 0xFF) << 16);
    }
}
