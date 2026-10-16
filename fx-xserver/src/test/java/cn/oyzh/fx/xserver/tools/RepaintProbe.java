package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * A client that repaints a window in a loop, so "the pointer flickers over a busy client" can be
 * reproduced without an application.
 *
 * <pre>
 *   RepaintProbe &lt;host&gt; &lt;port&gt; [milliseconds between frames] [seconds to run]
 * </pre>
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1): a client that repaints in a loop, so
 * anything about the frame path can be driven without an application and a VM. It maps one window,
 * fills a square of it with a colour through {@code PutImage} and repeats - park the pointer over the
 * window and watch the <em>host</em> window (the sprite is not in the framebuffer, so no X-level tool
 * sees it).
 *
 * <p><b>It does not reproduce A21 on its own</b>, and that is worth knowing before trusting it: its
 * damage is the square it paints, which always covers the sprite, so the sprite is repainted from the
 * framebuffer every frame either way. What exposed A21 was a client whose damage <em>alternates</em>
 * between rectangles that reach the sprite and rectangles that do not (EasyShell's SSH tab) - which is
 * the case the fix covers and this probe cannot.
 */
public final class RepaintProbe {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: RepaintProbe <host> <port> [intervalMillis] [seconds]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        long interval = args.length > 2 ? Long.parseLong(args[2]) : 5;
        double seconds = args.length > 3 ? Double.parseDouble(args[3]) : 60;

        try (RawXClient client = RawXClient.connect(host, port)) {
            int window = client.allocateId();
            int gc = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 700, 500, 0x00FFFFFF);
            // CreateGC by hand: the client here has no helper for it, and a PutImage needs one.
            byte[] createGc = new byte[16];
            createGc[0] = 55;                                       // CreateGC
            RawXClient.writeShortAt(createGc, 2, 4);
            RawXClient.writeIntAt(createGc, 4, gc);
            RawXClient.writeIntAt(createGc, 8, window);
            client.sendRequest(createGc);
            client.mapWindow(window);
            client.flush();
            System.out.println("window 0x" + Integer.toHexString(window) + " mapped 700x500 at 0,0;"
                    + " painting every " + interval + " ms for " + seconds + "s");

            long deadline = System.currentTimeMillis() + (long) (seconds * 1000);
            int frame = 0;
            while (System.currentTimeMillis() < deadline) {
                // A colour that changes every frame, so the client is really repainting rather than
                // filling with the same value (a server is free to notice that and do nothing).
                byte[] pixels = new byte[200 * 200 * 4];
                for (int i = 0; i < pixels.length; i += 4) {
                    pixels[i] = (byte) (frame * 4);                 // BGRA, as the wire wants it
                    pixels[i + 1] = (byte) (frame * 8);
                    pixels[i + 2] = (byte) 0x40;
                }
                client.putImage(window, gc, 0, 0, 200, 200, pixels);
                client.flush();
                frame++;
                if (interval > 0) {
                    Thread.sleep(interval);
                }
            }
            System.out.println(frame + " frames painted");
        }
    }
}
