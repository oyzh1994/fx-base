package cn.oyzh.fx.xserver.tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

/**
 * Reads an XWD (what the VM's {@code xwd} writes) and either converts it to PNG or diffs two of them.
 *
 * <p>Usage: {@code Xwd png IN.xwd OUT.png} or {@code Xwd diff A.xwd B.xwd [x y w h]}.
 *
 * <p>It exists because Python's PIL **refuses the XWD that {@code xwd} writes on the VM**
 * (`UnidentifiedImageError`), and because a "do the two servers look the same" question needs numbers
 * rather than eyes. The format's two traps are encoded here: the header is written in the *server's*
 * byte order - nothing in the file says which, but field 1 is the file version, 7 - while header
 * field 7 names the order of the **pixels**, which need not be the same; and the pixel data starts at
 * {@code header_size + ncolors * 12}, not at the end of a fixed header.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1). The comparison rules that go with it:
 * park the pointer in the same place and the application in the same state before capturing both
 * sides, and run {@link Shift} before believing any difference is a rendering difference.
 */
public class Xwd {

    static int width;
    static int height;
    static int bitsPerPixel;
    static int bytesPerLine;
    static int redMask;
    static int greenMask;
    static int blueMask;
    static int[] pixels;

    static void read(String path) throws Exception {
        byte[] data = Files.readAllBytes(new File(path).toPath());
        // The header is written in the server's byte order, which nothing in the file states
        // directly: the way to tell the two apart is that field 1 is the file version, 7.
        int[] h = new int[25];
        readHeader(data, h, true);
        if (h[1] != 7) {
            readHeader(data, h, false);
        }
        // Field 7 is the order of the *pixels*, which need not be the header's.
        boolean pixelsLittleEndian = h[7] == 0;
        int headerSize = h[0];
        int depth = h[3];
        width = h[4];
        height = h[5];
        bitsPerPixel = h[11];
        bytesPerLine = h[12];
        redMask = h[14];
        greenMask = h[15];
        blueMask = h[16];
        int ncolors = h[19];
        int offset = headerSize + ncolors * 12;
        int step = bitsPerPixel / 8;
        pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int row = offset + y * bytesPerLine;
            for (int x = 0; x < width; x++) {
                int p = row + x * step;
                int value;
                if (step == 4) {
                    value = pixelsLittleEndian
                            ? (data[p] & 0xFF) | (data[p + 1] & 0xFF) << 8 | (data[p + 2] & 0xFF) << 16
                                    | (data[p + 3] & 0xFF) << 24
                            : (data[p + 3] & 0xFF) | (data[p + 2] & 0xFF) << 8
                                    | (data[p + 1] & 0xFF) << 16 | (data[p] & 0xFF) << 24;
                } else if (step == 3) {
                    value = (data[p] & 0xFF) | (data[p + 1] & 0xFF) << 8 | (data[p + 2] & 0xFF) << 16;
                } else {
                    value = (data[p] & 0xFF) | (data[p + 1] & 0xFF) << 8;
                }
                pixels[y * width + x] = 0xFF000000 | channel(value, redMask) << 16
                        | channel(value, greenMask) << 8 | channel(value, blueMask);
            }
        }
        System.out.printf("%s: %dx%d depth=%d bpp=%d masks %06x/%06x/%06x%n", path, width, height,
                depth, bitsPerPixel, redMask, greenMask, blueMask);
    }

    static void readHeader(byte[] data, int[] h, boolean bigEndian) {
        for (int i = 0; i < 25; i++) {
            int at = i * 4;
            h[i] = bigEndian
                    ? (data[at] & 0xFF) << 24 | (data[at + 1] & 0xFF) << 16 | (data[at + 2] & 0xFF) << 8
                            | (data[at + 3] & 0xFF)
                    : (data[at + 3] & 0xFF) << 24 | (data[at + 2] & 0xFF) << 16
                            | (data[at + 1] & 0xFF) << 8 | (data[at] & 0xFF);
        }
    }

    /** The whole capture as ARGB, for tools that want to compare two of them. */
    static int[] load(String path) throws Exception {
        read(path);
        return pixels;
    }

    static int channel(int value, int mask) {
        if (mask == 0) {
            return 0;
        }
        int shift = Integer.numberOfTrailingZeros(mask);
        int bits = Integer.bitCount(mask);
        int raw = (value & mask) >>> shift;
        int full = (1 << bits) - 1;
        return raw * 255 / full;
    }

    public static void main(String[] args) throws Exception {
        if (args[0].equals("png")) {
            read(args[1]);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, width, height, pixels, 0, width);
            ImageIO.write(image, "png", new File(args[2]));
            System.out.println("wrote " + args[2]);
            return;
        }
        read(args[1]);
        int w = width;
        int h = height;
        int[] a = pixels;
        read(args[2]);
        if (width != w || height != h) {
            throw new IllegalStateException("different sizes");
        }
        int[] b = pixels;
        int x0 = args.length > 3 ? Integer.parseInt(args[3]) : 0;
        int y0 = args.length > 4 ? Integer.parseInt(args[4]) : 0;
        int rw = args.length > 5 ? Integer.parseInt(args[5]) : w;
        int rh = args.length > 6 ? Integer.parseInt(args[6]) : h;
        int differing = 0;
        int worst = 0;
        long total = 0;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = -1;
        int maxY = -1;
        int[] histogram = new int[256];
        for (int y = y0; y < y0 + rh; y++) {
            for (int x = x0; x < x0 + rw; x++) {
                int pa = a[y * w + x];
                int pb = b[y * w + x];
                int delta = Math.max(Math.abs((pa >> 16 & 0xFF) - (pb >> 16 & 0xFF)),
                        Math.max(Math.abs((pa >> 8 & 0xFF) - (pb >> 8 & 0xFF)),
                                Math.abs((pa & 0xFF) - (pb & 0xFF))));
                if (delta > 0) {
                    differing++;
                    histogram[Math.min(255, delta)]++;
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
                worst = Math.max(worst, delta);
                total += delta;
            }
        }
        System.out.printf("region %d,%d %dx%d: %d of %d pixels differ (%.3f%%), worst channel delta %d, "
                        + "mean %.3f%n", x0, y0, rw, rh, differing, rw * rh, 100.0 * differing / (rw * rh),
                worst, (double) total / (rw * rh));
        if (differing > 0) {
            System.out.printf("  differing pixels span %d,%d .. %d,%d%n", minX, minY, maxX, maxY);
            StringBuilder line = new StringBuilder("  delta histogram (1..32):");
            for (int i = 1; i <= 32; i++) {
                if (histogram[i] > 0) {
                    line.append(' ').append(i).append('=').append(histogram[i]);
                }
            }
            System.out.println(line);
            int over = 0;
            for (int i = 33; i < 256; i++) {
                over += histogram[i];
            }
            System.out.println("  larger than 32: " + over);
        }
    }
}
