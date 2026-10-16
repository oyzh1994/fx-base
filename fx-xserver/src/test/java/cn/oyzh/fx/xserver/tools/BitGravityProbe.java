package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * What a window's <em>own</em> contents do when the window is resized - {@code bitGravity}, A12's
 * leftover.
 *
 * <p>The item implemented {@code winGravity} (what happens to a child when its <em>parent</em>
 * changes size) and left {@code bitGravity} alone with the reason "with no per-window backing store
 * there is no content to shift". That is true of a store and not of the screen: the window's pixels
 * are in the framebuffer, and a reference server visibly moves them.
 *
 * <p>What the probe does: draws two markers into a window - one near its top left and one near its
 * bottom right, so a shift is visible and its direction is not ambiguous - grows the window, and
 * reads the whole window back as text. The markers' new positions are the rule; a server that does
 * not implement the gravity leaves them where they were, which is what this one did.
 *
 * <p>Usage: {@code BitGravityProbe <host> <port> [bitGravity] [growW] [growH]}. Gravity numbers are
 * X's own: 1 NorthWest, 2 North, 3 NorthEast, 4 West, 5 Center, 6 East, 7 SouthWest, 8 South,
 * 9 SouthEast, 10 Static, 0 Forget.
 */
public final class BitGravityProbe {

    private static final String[] NAMES = {"Forget", "NorthWest", "North", "NorthEast", "West",
            "Center", "East", "SouthWest", "South", "SouthEast", "Static"};

    private static final int WIDTH = 60;
    private static final int HEIGHT = 40;
    private static final int MARKER = 6;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: BitGravityProbe <host> <port> [bitGravity] [growW] [growH]");
            return;
        }
        int gravity = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        int growWidth = args.length > 3 ? Integer.parseInt(args[3]) : 20;
        int growHeight = args.length > 4 ? Integer.parseInt(args[4]) : 10;
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int window = client.allocateId();
            // NorthWest on the win gravity: this probe is about the window's own contents, and a
            // child that moves would confuse the picture.
            client.createWindowWithGravity(window, client.root(), 10, 10, WIDTH, HEIGHT, gravity, 1);
            int gc = client.allocateId();
            client.createGC(gc, client.root(), 0x00FF00);
            client.mapWindow(window);
            client.fillRectangle(window, gc, 1, 1, MARKER, MARKER);
            client.fillRectangle(window, gc, WIDTH - 1 - MARKER, HEIGHT - 1 - MARKER, MARKER, MARKER);
            client.queryTree(window);                       // a round trip: the markers are drawn

            int newWidth = WIDTH + growWidth;
            int newHeight = HEIGHT + growHeight;
            client.configureWindow(window, 10, 10, newWidth, newHeight);
            client.queryTree(window);

            System.out.printf("bitGravity %s (%d), %dx%d -> %dx%d%n", NAMES[gravity], gravity,
                    WIDTH, HEIGHT, newWidth, newHeight);
            byte[] image = client.getImage(window, 0, 0, newWidth, newHeight);
            // The marker's own rectangle, per row of ink: numbers rather than a picture, because what
            // this probe is for is the shift, and a column count read off ASCII art is how a first
            // version of this measurement went wrong.
            for (int y = 0; y < newHeight; y++) {
                int first = -1;
                int last = -1;
                for (int x = 0; x < newWidth; x++) {
                    int at = 32 + (y * newWidth + x) * 4;
                    boolean green = at + 2 < image.length && (image[at] & 0xFF) == 0x00
                            && (image[at + 1] & 0xFF) == 0xFF && (image[at + 2] & 0xFF) == 0x00;
                    if (green) {
                        first = first < 0 ? x : first;
                        last = x;
                    }
                }
                if (first >= 0) {
                    System.out.printf("  row %2d: x %d..%d%n", y, first, last);
                }
            }
        }
    }

    private BitGravityProbe() {
    }
}
