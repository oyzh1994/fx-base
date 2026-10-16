package cn.oyzh.fx.xserver.display;

import cn.oyzh.fx.xserver.display.TightEncoder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The JPEG half of Tight: the content rule, the compact length and the stream itself (A32).
 *
 * <p>Both ends of the content rule are pinned, because only one of them is a win: a photograph has to
 * take the lossy encoder and a flat panel or text has to stay on ZRLE, which is lossless. A heuristic
 * that answered "photographic" too often would blur exactly the text this server exists to forward.
 */
class TightEncoderTest {

    @Test
    void aFlatPanelOrTextIsNotPhotographicAndAPhotoIs() {
        int[] flat = new int[200 * 150];
        java.util.Arrays.fill(flat, 0xFFE8E8E8);
        assertFalse(TightEncoder.looksPhotographic(flat, 200, 150), "a flat panel");

        int[] text = new int[200 * 150];
        Random random = new Random(1);
        int[] greys = {0xFFE8E8E8, 0xFFC8C8C8, 0xFFA0A0A0, 0xFF707070, 0xFF484848, 0xFF202020};
        for (int i = 0; i < text.length; i++) {
            text[i] = random.nextInt(4) == 0 ? greys[random.nextInt(greys.length)] : 0xFFE8E8E8;
        }
        assertFalse(TightEncoder.looksPhotographic(text, 200, 150),
                "antialiased text with a handful of greys");

        int[] photo = new int[200 * 150];
        for (int i = 0; i < photo.length; i++) {
            photo[i] = 0xFF000000 | random.nextInt(0x1000000);
        }
        assertTrue(TightEncoder.looksPhotographic(photo, 200, 150), "a photograph");

        // A photograph inside flat panels is one for this purpose: JPEG renders the flat parts of it
        // faithfully and ZRLE would be the wrong tool for the whole rectangle.
        int[] mixed = new int[200 * 150];
        java.util.Arrays.fill(mixed, 0xFFE8E8E8);
        for (int y = 40; y < 110; y++) {
            for (int x = 40; x < 160; x++) {
                mixed[y * 200 + x] = 0xFF000000 | random.nextInt(0x1000000);
            }
        }
        assertTrue(TightEncoder.looksPhotographic(mixed, 200, 150), "a photograph among panels");

        assertFalse(TightEncoder.looksPhotographic(new int[] {1, 2, 3, 4}, 2, 2),
                "a rectangle too small to be worth a JPEG header");
    }

    @Test
    void theCompactLengthIsTheProtocolsOneTwoOrThreeBytes() throws IOException {
        // 0xxxxxxx for 0..127, 1xxxxxxx 0yyyyyyy for 128..16383, and three bytes above that - the
        // spec's own example is 10000 = 90 4E. The second byte carries bits 7-13, so 128 is 80 01 and
        // 16383 (all fourteen bits set) is ff 7f.
        assertEquals("7f", hex(127));
        assertEquals("8001", hex(128));
        assertEquals("904e", hex(10000));
        assertEquals("ff7f", hex(16383));
        assertEquals("808001", hex(16384));
    }

    private static String hex(int length) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TightEncoder.writeCompactLength(out, length);
        StringBuilder hex = new StringBuilder();
        for (byte value : out.toByteArray()) {
            hex.append(String.format("%02x", value));
        }
        return hex.toString();
    }

    @Test
    void theJpegIsAStreamAnotherDecoderCanRead() throws IOException {
        int width = 40;
        int height = 30;
        int[] argb = new int[width * height];
        Random random = new Random(3);
        for (int i = 0; i < argb.length; i++) {
            argb[i] = 0xFF000000 | random.nextInt(0x1000000);
        }
        byte[] jpeg = TightEncoder.jpeg(argb, width, height, 0.8f);
        assertTrue(jpeg.length > 2 && (jpeg[0] & 0xFF) == 0xFF && (jpeg[1] & 0xFF) == 0xD8,
                "a JFIF stream starts with SOI");
        java.awt.image.BufferedImage decoded = javax.imageio.ImageIO.read(
                new java.io.ByteArrayInputStream(jpeg));
        assertNotNull(decoded, "and this JVM's own decoder reads it back");
        assertEquals(width, decoded.getWidth());
        assertEquals(height, decoded.getHeight());
    }
}
