package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Captures a window with {@code GetImage} and writes it as a PNG, with no X server tooling on the
 * machine that runs it.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1). {@code xwd} on the VM does the same
 * job for a {@code --single-window} server, but it cannot see the per-client mode at all (a fresh
 * connection gets a root of its own) and it is not there when the client is on this machine. This
 * asks the server directly, so it works in either mode and against any server.
 *
 * <pre>
 *   Shot &lt;host&gt; &lt;port&gt; out.png [x y w h]     # the root, or a rectangle of it
 *   Shot &lt;host&gt; &lt;port&gt; out.png --window 0x1400003 [x y w h]
 * </pre>
 *
 * <p>The pixels come back as ZPixmap in the server's image byte order and are read as BGRA, the same
 * reading {@link RawXClient#pixelAt} uses. A 24 bit visual is stored 32 bits to the pixel, which is
 * the case for this server and for Xvfb alike; the image size is taken from the reply, so a capture
 * larger than one request's worth of data (which goes through BIG-REQUESTS) arrives whole.
 */
public final class Shot {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("usage: Shot <host> <port> out.png [x y w h]"
                    + " | Shot <host> <port> out.png --window <id> [x y w h]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String path = args[2];
        int window = -1;
        int at = 3;
        if (args.length > at && args[at].equals("--window")) {
            window = (int) Long.parseLong(args[at + 1].replace("0x", ""), 16);
            at += 2;
        }

        try (RawXClient client = RawXClient.connect(host, port)) {
            int drawable = window >= 0 ? window : client.root();
            int x = 0;
            int y = 0;
            int[] size = client.geometry(drawable);
            int width = size[2];
            int height = size[3];
            if (args.length >= at + 4) {
                x = Integer.parseInt(args[at]);
                y = Integer.parseInt(args[at + 1]);
                width = Integer.parseInt(args[at + 2]);
                height = Integer.parseInt(args[at + 3]);
            }

            byte[] reply = client.getImage(drawable, x, y, width, height);
            int depth = reply[1] & 0xFF;
            // A ZPixmap row is padded to four bytes, and a pixel wider than 24 bits is one 32 bit
            // word when it comes back - the depth says which, and the reply carries no other hint.
            int step = depth > 16 ? 4 : depth > 8 ? 2 : 1;
            int rowBytes = (width * step + 3) & ~3;

            int[] pixels = new int[width * height];
            for (int row = 0; row < height; row++) {
                int start = 32 + row * rowBytes;
                for (int column = 0; column < width; column++) {
                    int p = start + column * step;
                    int value = (reply[p] & 0xFF) | (reply[p + 1] & 0xFF) << 8
                            | (reply[p + 2] & 0xFF) << 16 | (reply[p + 3] & 0xFF) << 24;
                    // GetImage answered in the screen's formats, which for both this server and Xvfb
                    // is 24 bit colour in a 32 bit word: blue, green, red, then padding.
                    pixels[row * width + column] = value | 0xFF000000;
                }
            }

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, width, height, pixels, 0, width);
            ImageIO.write(image, "png", new File(path));
            System.out.printf("wrote %s: %s at %d,%d %dx%d depth %d%n", path,
                    window >= 0 ? "0x" + Integer.toHexString(window) : "root", x, y, width, height,
                    depth);
        }
    }

    private Shot() {
    }
}
