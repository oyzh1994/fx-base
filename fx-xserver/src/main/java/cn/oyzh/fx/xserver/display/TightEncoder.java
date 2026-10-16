package cn.oyzh.fx.xserver.display;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * The JPEG half of RFB's Tight encoding (encoding 7): what a photographic rectangle is sent as.
 *
 * <p>Why this exists, measured (2026-10-01): ZRLE is a palette-and-run encoding, so a rectangle where
 * nearly every pixel differs from its neighbour - a photograph, a gradient, a video frame - falls
 * through to its raw subencoding and costs **3.1 MB and 57 ms** for one 1300x800 frame, where the
 * palette search at least no longer wastes time on it (A30). Nothing about that is a bug in ZRLE; it
 * is the wrong tool. JPEG is the right one: the same frame is a few hundred kilobytes, and the
 * encoding is a library call rather than a per-pixel analysis.
 *
 * <p><b>What the protocol says, and where it is easy to get wrong</b> (community RFB specification,
 * "Tight Encoding"; the numbers here are the ones that matter and were checked there rather than
 * remembered):
 *
 * <ul>
 *   <li>JPEG is a <b>compression method inside the Tight encoding</b>, not an encoding of its own: the
 *       rectangle is sent with encoding 7 and a control byte whose high nibble is {@code 1001}
 *       ({@link #CONTROL_JPEG}). The JPEG quality pseudo-encodings are what the *client* advertises to
 *       say it can decode JPEG at all - and {@code -23} is the <b>highest</b> quality, {@code -32} the
 *       lowest, which is the opposite of what the numbering suggests.
 *   <li>The JPEG data is prefixed by its length in a <b>compact</b> 1-3 byte form
 *       ({@link #writeCompactLength}), not as a 32 bit integer.
 *   <li>A Tight-encoded rectangle may not be wider than <b>2048</b> pixels, so the caller has to keep
 *       its rectangles inside that (see {@code RfbSession.splitForTight}).
 * </ul>
 */
final class TightEncoder {

    /**
     * The control byte of a JPEG rectangle: no zlib stream to reset (the low nibble is zero) and the
     * high nibble {@code 1001}, which is {@code JpegCompression}.
     */
    static final int CONTROL_JPEG = 0x90;

    /**
     * How many colours a sampled rectangle needs before it is treated as a photograph.
     *
     * <p>A flat user interface, text and icons have few: measured on the bed, a terminal's text rows
     * hold a handful of antialiasing greys, well under this. A photograph, a gradient or a video frame
     * has thousands. The rectangle is sampled rather than counted exactly, so the check costs a few
     * hundred lookups whatever the rectangle's size - and a rectangle that contains a photograph
     * *among* flat panels is a photograph for this purpose, which is the right answer: JPEG renders
     * the flat parts of it faithfully and ZRLE would be the wrong tool for the whole of it.
     */
    private static final int PHOTOGRAPHIC_COLOURS = 64;
    /** How many pixels the test above looks at, however large the rectangle is. */
    private static final int SAMPLE_PIXELS = 4096;

    private TightEncoder() {
    }

    /**
     * Whether a rectangle is one JPEG is the right tool for. Static and package-private so the rule can
     * be tested without a server or a window.
     */
    static boolean looksPhotographic(int[] argb, int width, int height) {
        int pixels = width * height;
        if (pixels < 64) {
            return false;
        }
        int stride = Math.max(1, pixels / SAMPLE_PIXELS);
        java.util.HashSet<Integer> colours = new java.util.HashSet<>();
        for (int at = 0; at < pixels; at += stride) {
            colours.add(argb[at]);
            if (colours.size() > PHOTOGRAPHIC_COLOURS) {
                return true;
            }
        }
        return false;
    }

    /**
     * One rectangle as a JFIF stream at {@code quality} (0..1), which is what
     * {@code JpegCompression} carries.
     */
    static byte[] jpeg(int[] argb, int width, int height, float quality) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, argb, 0, width);
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("this JVM has no JPEG writer");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam parameter = writer.getDefaultWriteParam();
        parameter.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        parameter.setCompressionQuality(Math.max(0.05f, Math.min(1f, quality)));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.max(1024, width * height / 8));
        try (ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), parameter);
        } finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }

    /**
     * The length of the {@code jpeg-data} that follows, in the protocol's compact form: seven bits in
     * the first byte, and a continuation bit that brings in seven more, and then eight more after
     * that - so up to 4194303 bytes, far more than a rectangle's worth.
     */
    static void writeCompactLength(java.io.OutputStream out, int length) throws IOException {
        if (length < 0x80) {
            out.write(length);
        } else if (length < 0x4000) {
            out.write(0x80 | (length & 0x7F));
            out.write((length >> 7) & 0xFF);
        } else {
            out.write(0x80 | (length & 0x7F));
            out.write(0x80 | ((length >> 7) & 0x7F));
            out.write((length >> 14) & 0xFF);
        }
    }
}
