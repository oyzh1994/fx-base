package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * The Composite extension as a compositing window manager asks for it - the harness for the last of
 * B5's three, and the thing that decides whether `picom` can run against this server.
 *
 * <p>What it does, in the order a compositor does it: asks for the version, asks for the overlay
 * window, creates a region from a window's border clip, redirects a window, names the pixmap that
 * holds that window's contents, reads a pixel out of it, and puts everything back. Every field is
 * printed whether or not the extension answered, because "the extension is missing", "the extension
 * answered with a different version" and "the extension answered but the pixmap is empty" are three
 * different findings and only the last one is a bug in the drawing path.
 *
 * <p><b>Nothing here is guessed.</b> The opcodes are read off the generated protocol classes (the
 * `OPCODE` constant on each request, printed by a one-off `javap` rather than recalled), and the
 * version a reference answers is measured. That is B6's lesson: a layout a few bytes out is a client
 * reading its own field boundaries wrong, and the error is invisible when the body is ignored.
 *
 * <p>Usage: {@code CompositeProbe &lt;host&gt; &lt;port&gt;} - adds {@code redirect} to leave the
 * window redirected at the end, which is how a second probe can read the named pixmap afterwards.
 */
public final class CompositeProbe {

    private static final int QUERY_VERSION = 0;
    private static final int REDIRECT_WINDOW = 1;
    private static final int UNREDIRECT_WINDOW = 3;
    private static final int CREATE_REGION_FROM_BORDER_CLIP = 5;
    private static final int NAME_WINDOW_PIXMAP = 6;
    private static final int GET_OVERLAY_WINDOW = 7;
    private static final int RELEASE_OVERLAY_WINDOW = 8;

    /** {@code Redirect}'s two values, as the extension numbers them. */
    private static final int AUTOMATIC = 0;

    private static final int WINDOW_COLOR = 0x00AA22;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: CompositeProbe <host> <port> [redirect]");
            return;
        }
        boolean leaveRedirected = args.length > 2 && args[2].equals("redirect");
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int[] info = client.queryExtensionInfo("Composite");
            System.out.println("Composite present=" + info[0] + " major=" + info[1]
                    + " firstEvent=" + info[2] + " firstError=" + info[3]);
            if (info[0] == 0) {
                return;
            }
            int opcode = info[1];

            // QueryVersion answers the server's own version, which need not be the client's: a
            // compositor reads it and decides which requests it may use. Asked twice, because
            // whether the answer is the server's version or the *lower* of the two is a rule the
            // reply itself settles and a guess would not.
            for (int minor : new int[]{4, 2}) {
                byte[] version = new byte[12];
                version[0] = (byte) opcode;
                version[1] = (byte) QUERY_VERSION;
                RawXClient.writeShortAt(version, 2, 3);
                RawXClient.writeIntAt(version, 4, 0);       // client major
                RawXClient.writeIntAt(version, 8, minor);   // client minor
                client.sendRequest(version);
                byte[] reply = client.readReply();
                System.out.println("  QueryVersion(0." + minor + ") -> " + RawXClient.readInt(reply, 8)
                        + "." + RawXClient.readInt(reply, 12));
            }

            int window = client.allocateId();
            client.createWindow(window, client.root(), 30, 30, 200, 150);
            client.mapWindow(window);
            int gc = client.createGC(client.allocateId(), client.root(), WINDOW_COLOR);
            client.fillRectangle(window, gc, 0, 0, 200, 150);
            client.flush();
            System.out.println("  window=0x" + Integer.toHexString(window)
                    + " painted #" + String.format("%06X", WINDOW_COLOR));

            // The overlay window is where the compositor draws what it has composited. A server
            // whose screen *is* the composited result may answer None, which is a measurement and
            // not a failure - so it is printed rather than required.
            byte[] overlay = new byte[8];
            overlay[0] = (byte) opcode;
            overlay[1] = (byte) GET_OVERLAY_WINDOW;
            RawXClient.writeShortAt(overlay, 2, 2);
            RawXClient.writeIntAt(overlay, 4, client.root());
            client.sendRequest(overlay);
            byte[] overlayReply = client.readReply();
            int overlayWindow = RawXClient.readInt(overlayReply, 8);
            System.out.println("  GetOverlayWindow(root) -> 0x" + Integer.toHexString(overlayWindow));
            if (overlayWindow != 0) {
                // What the overlay window *is* decides whether this server can have one honestly:
                // a real window a compositor draws on, or something else. Geometry and parent are the
                // two fields a guess would get wrong.
                int[] geometry = client.geometry(overlayWindow);
                // Printed field by field rather than as the usual `1300x800+0+0`: the diff tool
                // normalises every `0x…` literal to a role name, and "1300x800" contains one, so the
                // compact form arrives at the comparison mangled ("130R2+0+0") and reads as a
                // formatting fault in the tool rather than in the probe.
                System.out.println("    overlay geometry: width=" + geometry[2] + " height="
                        + geometry[3] + " x=" + geometry[0] + " y=" + geometry[1]);
                // The overlay's *depth* is the field a compositor's RENDER setup turns on: it asks
                // for a picture with the root's format, and a format that does not match the
                // drawable's depth is BadMatch. Read with a raw GetGeometry because the geometry
                // helper above returns only the four position fields.
                byte[] getGeometry = new byte[8];
                getGeometry[0] = 14;                        // GetGeometry
                RawXClient.writeShortAt(getGeometry, 2, 2);
                RawXClient.writeIntAt(getGeometry, 4, overlayWindow);
                client.sendRequest(getGeometry);
                byte[] geometryReply = client.readReply();
                System.out.println("    overlay depth=" + (geometryReply[1] & 0xFF));
            }

            int region = client.allocateId();
            byte[] borderClip = new byte[12];
            borderClip[0] = (byte) opcode;
            borderClip[1] = (byte) CREATE_REGION_FROM_BORDER_CLIP;
            RawXClient.writeShortAt(borderClip, 2, 3);
            RawXClient.writeIntAt(borderClip, 4, region);
            RawXClient.writeIntAt(borderClip, 8, window);
            client.sendRequest(borderClip);
            client.flush();
            System.out.println("  CreateRegionFromBorderClip -> region=0x"
                    + Integer.toHexString(region) + " (accepted unless an error follows)");

            // Redirect, then read the window's own pixels back through the pixmap the server names.
            // This is the whole point of the extension: a compositor never reads the screen, it
            // reads each window's pixmap and draws them itself.
            byte[] redirect = new byte[12];
            redirect[0] = (byte) opcode;
            redirect[1] = (byte) REDIRECT_WINDOW;
            RawXClient.writeShortAt(redirect, 2, 3);
            RawXClient.writeIntAt(redirect, 4, window);
            redirect[8] = (byte) AUTOMATIC;                 // update
            client.sendRequest(redirect);
            client.flush();

            int pixmap = client.allocateId();
            byte[] name = new byte[12];
            name[0] = (byte) opcode;
            name[1] = (byte) NAME_WINDOW_PIXMAP;
            RawXClient.writeShortAt(name, 2, 3);
            RawXClient.writeIntAt(name, 4, window);
            RawXClient.writeIntAt(name, 8, pixmap);
            client.sendRequest(name);
            client.flush();
            int pixmapPixel = client.pixelAt(pixmap, 100, 75);
            System.out.println("  RedirectWindow(Automatic) + NameWindowPixmap -> pixmap pixel at 100,75 = "
                    + String.format("#%08X", pixmapPixel)
                    + (pixmapPixel == (0xFF000000 | WINDOW_COLOR)
                            ? " (the window's own colour)" : ""));

            // **The measurement the whole extension turns on**: while a window is redirected, what it
            // draws must go to its own pixmap and *not* to the screen. Repainting it a different
            // colour and reading both sides says which of the two happened - and it is the one rule
            // this server cannot inherit, because every window here already draws into a store that
            // is then composited onto the screen (B5).
            int secondColor = 0xFF00FF;
            int repaintGc = client.createGC(client.allocateId(), client.root(), secondColor);
            client.fillRectangle(window, repaintGc, 0, 0, 200, 150);
            client.flush();
            int pixmapAfter = client.pixelAt(pixmap, 100, 75);
            int screenAfter = client.pixelAt(client.root(), 130, 105);
            System.out.println("  repainted #" + String.format("%06X", secondColor)
                    + " while redirected: pixmap at 100,75 = " + String.format("#%08X", pixmapAfter)
                    + ", screen at 130,105 = " + String.format("#%08X", screenAfter)
                    + (screenAfter == (0xFF000000 | secondColor)
                            ? " (the screen followed - this server composited it)"
                            : " (the screen did not follow - neither does the reference)"));

            // A second window over the first: with redirection on, the covered one must keep its
            // contents in its own pixmap rather than losing them to the window on top.
            int cover = client.allocateId();
            client.createWindow(cover, client.root(), 60, 60, 100, 100);
            client.mapWindow(cover);
            int coverGc = client.createGC(client.allocateId(), client.root(), 0x0000FF);
            client.fillRectangle(cover, coverGc, 0, 0, 100, 100);
            client.flush();
            int covered = client.pixelAt(pixmap, 100, 75);
            System.out.println("  after a window covers part of it, the named pixmap at 100,75 = "
                    + String.format("#%08X", covered));

            // **Drawing *into* a window's pixmap**: the specification calls the contents the
            // window's, so a client that writes there is doing something undefined - and what a
            // server does about it is a measurement, not a reading. The fill is followed by a round
            // trip, so a refusal shows up as an error rather than as silence.
            try {
                client.fillRectangle(pixmap, coverGc, 0, 0, 10, 10);
                client.flush();
                int afterFill = client.pixelAt(pixmap, 5, 5);
                System.out.println("  drawing into the named pixmap: accepted, pixel at 5,5 = "
                        + String.format("#%08X", afterFill));
            } catch (Exception e) {
                System.out.println("  drawing into the named pixmap: refused (" + e.getMessage() + ")");
            }

            if (!leaveRedirected) {
                byte[] unredirect = new byte[12];
                unredirect[0] = (byte) opcode;
                unredirect[1] = (byte) UNREDIRECT_WINDOW;
                RawXClient.writeShortAt(unredirect, 2, 3);
                RawXClient.writeIntAt(unredirect, 4, window);
                unredirect[8] = (byte) AUTOMATIC;
                client.sendRequest(unredirect);
                if (overlayWindow != 0) {
                    byte[] release = new byte[8];
                    release[0] = (byte) opcode;
                    release[1] = (byte) RELEASE_OVERLAY_WINDOW;
                    RawXClient.writeShortAt(release, 2, 2);
                    RawXClient.writeIntAt(release, 4, client.root());
                    client.sendRequest(release);
                }
                client.flush();
                System.out.println("  UnredirectWindow + ReleaseOverlayWindow sent");
            }
        }
    }

    private CompositeProbe() {
    }
}
