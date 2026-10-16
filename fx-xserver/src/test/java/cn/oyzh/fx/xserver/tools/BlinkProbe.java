package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * Times the changes in a small rectangle of the screen, so a blink can be told from a still picture.
 *
 * <pre>
 *   BlinkProbe &lt;host&gt; &lt;port&gt; x y w h [seconds]
 * </pre>
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1). It exists because the cheap
 * instruments lie about this: a frame dump writes one image a <em>second</em>, and a blink at thirty
 * frames a second lands on the same phase every sample, so a flicker reads as a still picture - and so
 * does a region nothing is drawing into. This reads the rectangle as fast as {@code GetImage} answers
 * (thousands a second for a box this size) and prints a line whenever the bytes it got back change,
 * with the time since the previous change.
 *
 * <p>The rectangle is on the root, so what it watches is what the <em>screen</em> shows - an
 * application's own drawing, not the server's pointer sprite, which is drawn over the frame and never
 * into the framebuffer (see the design notes).
 */
public final class BlinkProbe {

    /**
     * The pixels of the rectangle, and <b>only</b> the pixels.
     *
     * <p>{@code getImage} answers with the whole reply, header included, and the header carries the
     * sequence number - so comparing what it returns compares two different numbers every time and
     * every region reads as changing thousands of times a second, a still one included. That is what
     * the first version of this probe did: it reported sixteen thousand changes a second in an empty
     * corner of the root window.
     */
    private static byte[] pixels(RawXClient client, int x, int y, int w, int h) throws Exception {
        byte[] reply = client.getImage(client.root(), x, y, w, h);
        return java.util.Arrays.copyOfRange(reply, 32, reply.length);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 6) {
            System.out.println("usage: BlinkProbe <host> <port> x y w h [seconds]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int x = Integer.parseInt(args[2]);
        int y = Integer.parseInt(args[3]);
        int w = Integer.parseInt(args[4]);
        int h = Integer.parseInt(args[5]);
        double seconds = args.length > 6 ? Double.parseDouble(args[6]) : 5;

        try (RawXClient client = RawXClient.connect(host, port)) {
            byte[] previous = pixels(client, x, y, w, h);
            long started = System.nanoTime();
            long last = started;
            int changes = 0;
            long deadline = started + (long) (seconds * 1e9);
            while (System.nanoTime() < deadline) {
                byte[] now = pixels(client, x, y, w, h);
                if (!java.util.Arrays.equals(now, previous)) {
                    changes++;
                    long at = System.nanoTime();
                    System.out.printf("  change %3d at %6.3fs (%.1f ms since the last one)%n",
                            changes, (at - started) / 1e9, (at - last) / 1e6);
                    last = at;
                    previous = now;
                }
            }
            System.out.printf("%d change(s) in %.1fs over %dx%d at %d,%d%n", changes, seconds, w, h,
                    x, y);
        }
    }
}
