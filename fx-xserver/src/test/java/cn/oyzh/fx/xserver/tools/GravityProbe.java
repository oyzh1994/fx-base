package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;
import java.util.ArrayList;
import java.util.List;

/**
 * Asks a server what window gravity does: where a child ends up when its parent is resized, and which
 * part of a resized window's own content it keeps.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1), and the harness for item A12 - which
 * claims the server stores {@code bitGravity}/{@code winGravity} and never uses them. Whether a
 * *server* applies {@code winGravity} at all is the first thing to settle, because the protocol
 * describes it as the window manager's field, and the answer is not obvious from the wording.
 *
 * <pre>
 *   GravityProbe &lt;host&gt; &lt;port&gt; [winGravity] [bitGravity] [dw] [dh]
 *   GravityProbe 192.168.22.152 6098 6 1        # EastGravity on the child, NorthWest on the parent
 * </pre>
 *
 * <p>What it prints: the child's geometry before and after the parent grows, whether the child moved
 * beyond what its own position was set to, and the expose rectangles a resize of the *parent* produces.
 * Gravity numbers are X's own: 1 NorthWest, 2 North, 3 NorthEast, 4 West, 5 Center, 6 East, 7
 * SouthWest, 8 South, 9 SouthEast, 10 Static.
 */
public final class GravityProbe {

    private static final String[] GRAVITY_NAMES = {"Forget", "NorthWest", "North", "NorthEast", "West",
            "Center", "East", "SouthWest", "South", "SouthEast", "Static"};

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: GravityProbe <host> <port> [winGravity] [bitGravity]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        int winGravity = args.length > 2 ? Integer.parseInt(args[2]) : 6;
        int bitGravity = args.length > 3 ? Integer.parseInt(args[3]) : 1;
        int growWidth = args.length > 4 ? Integer.parseInt(args[4]) : 200;
        int growHeight = args.length > 5 ? Integer.parseInt(args[5]) : 100;
        System.out.printf("WinGravity %s (%d), BitGravity %s (%d)%n", name(winGravity), winGravity,
                name(bitGravity), bitGravity);

        try (RawXClient client = RawXClient.connect(host, port)) {
            int parent = client.allocateId();
            int child = client.allocateId();
            client.createWindow(parent, client.root(), 0, 0, 300, 300);
            client.createWindowWithGravity(child, parent, 150, 150, 100, 100, bitGravity, winGravity);
            client.mapWindow(parent);
            client.mapWindow(child);
            client.selectInput(child, 0x8000 | 0x20000); // ExposureMask | StructureNotifyMask
            client.flush();
            int[] before = client.geometry(child);
            System.out.printf("child before: %dx%d+%d+%d%n", before[2], before[3], before[0], before[1]);
            drain(client);

            // Grow the parent by 200 to the right and 100 down. EastGravity is supposed to leave the
            // child's right-hand distance unchanged, so it would move right by the same 200.
            client.configureWindow(parent, 0, 0, 300 + growWidth, 300 + growHeight);
            client.flush();
            int[] after = client.geometry(child);
            System.out.printf("child after parent grew by %dx%d: %dx%d+%d+%d%n", growWidth, growHeight,
                    after[2], after[3], after[0], after[1]);
            System.out.printf("  moved by %+d,%+d%n", after[0] - before[0], after[1] - before[1]);
            for (int[] expose : exposes(client)) {
                System.out.printf("  expose on the child: %dx%d+%d+%d%n", expose[2], expose[3],
                        expose[0], expose[1]);
            }

            System.out.println("done");
        }
    }

    /**
     * What the child was told while the parent changed: its own {@code ConfigureNotify} (which is how
     * it learns it was moved by its gravity at all) and the expose rectangles, which say which part of
     * it the server expects it to redraw.
     */
    private static List<int[]> exposes(RawXClient client) throws Exception {
        List<int[]> rectangles = new ArrayList<>();
        client.readTimeoutMillis(300);
        for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
            if ((event[0] & 0x7F) == 12) {              // Expose
                rectangles.add(new int[] {RawXClient.readShort(event, 8),
                        RawXClient.readShort(event, 10), RawXClient.readShort(event, 12),
                        RawXClient.readShort(event, 14)});
            } else if ((event[0] & 0x7F) == 22) {       // ConfigureNotify
                // x and y at 16/18, the size at 20/22: the fields between the header and them are the
                // window, the one above it in the stack, and nothing else.
                System.out.printf("  child ConfigureNotify: %dx%d+%d+%d%n",
                        RawXClient.readShort(event, 20), RawXClient.readShort(event, 22),
                        RawXClient.readShort(event, 16), RawXClient.readShort(event, 18));
            } else {
                // Anything else is worth naming: a server that answers a resize with an event nobody
                // expected is exactly what this probe is for.
                System.out.printf("  child got event type %d%n", event[0] & 0x7F);
            }
        }
        client.readTimeoutMillis(5000);
        return rectangles;
    }

    /** Drops whatever the map produced, so the exposes printed are the resize's own. */
    private static void drain(RawXClient client) throws Exception {
        exposes(client);
    }

    private static String name(int gravity) {
        return gravity >= 0 && gravity < GRAVITY_NAMES.length ? GRAVITY_NAMES[gravity] : "?" + gravity;
    }

    private GravityProbe() {
    }
}
