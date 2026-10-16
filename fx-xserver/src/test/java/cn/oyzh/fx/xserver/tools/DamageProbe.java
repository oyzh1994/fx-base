package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * The DAMAGE extension as a client uses it - B5's first part, and the part a screencast or a
 * compositor needs.
 *
 * <p>What it does: asks for the version, creates a damage object on a window, draws, and prints every
 * {@code DamageNotify} with its level, its area and the window's geometry. Then it subtracts what has
 * been reported (which is what a client does when it has redrawn or re-read the pixels) and draws
 * again, because "does the damage come back" is the question the whole extension turns on.
 *
 * <p>The three levels are the part worth measuring rather than reading: {@code Bounding} reports every
 * change as one rectangle, {@code NonEmpty} holds everything back until a subtract asks for it, and
 * {@code RawRectangles} reports what was drawn rather than where the drawn things end up.
 *
 * <p>Usage: {@code DamageProbe <host> <port> [level]} - the level defaults to 0 ({@code Bounding}).
 */
public final class DamageProbe {

    private static final int QUERY_VERSION = 0;
    private static final int CREATE = 1;
    private static final int DESTROY = 2;
    private static final int SUBTRACT = 3;

    public static void main(String[] args) throws Exception {
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int[] damage = client.queryExtensionInfo("DAMAGE");
            System.out.println("DAMAGE present=" + damage[0] + " major=" + damage[1]
                    + " firstEvent=" + damage[2]);
            if (damage[0] == 0) {
                return;
            }
            int opcode = damage[1];
            int firstEvent = damage[2];
            int level = args.length > 2 ? Integer.parseInt(args[2]) : 0;

            byte[] version = new byte[12];
            version[0] = (byte) opcode;
            version[1] = (byte) QUERY_VERSION;
            RawXClient.writeShortAt(version, 2, 3);
            RawXClient.writeIntAt(version, 4, 1);
            RawXClient.writeIntAt(version, 8, 1);
            client.sendRequest(version);
            byte[] reply = client.readReply();
            System.out.println("  QueryVersion -> " + RawXClient.readInt(reply, 8) + "."
                    + RawXClient.readInt(reply, 12));

            int window = client.allocateId();
            int gc = client.allocateId();
            client.createWindow(window, client.root(), 20, 20, 100, 80, 0x202020);
            client.mapWindow(window);
            client.createGC(gc, client.root(), 0x00FF00);
            client.flush();

            int damageId = client.allocateId();
            byte[] create = new byte[16];
            create[0] = (byte) opcode;
            create[1] = (byte) CREATE;
            create[2] = 4;
            RawXClient.writeIntAt(create, 4, damageId);
            RawXClient.writeIntAt(create, 8, window);
            create[12] = (byte) level;
            client.sendRequest(create);
            client.flush();
            client.queryTree(window);
            System.out.println("  damage object at level " + level);

            System.out.println("-- two draws, 5,5 10x10 and 30,30 10x10");
            client.fillRectangle(window, gc, 5, 5, 10, 10);
            client.fillRectangle(window, gc, 30, 30, 10, 10);
            client.flush();
            report(client, firstEvent, 600);

            System.out.println("-- draw a rectangle at 10,10 40x30");
            client.fillRectangle(window, gc, 10, 10, 40, 30);
            client.flush();
            report(client, firstEvent, 600);

            System.out.println("-- nothing drawn");
            client.queryTree(window);
            report(client, firstEvent, 400);

            System.out.println("-- subtract, then draw at 60,40 20x20");
            // Four units: the damage object, the repair region, and the parts word - sixteen bytes,
            // which is what the first version of this got wrong (three units is a malformed request
            // and a reference server drops the connection over it).
            byte[] subtract = new byte[16];
            subtract[0] = (byte) opcode;
            subtract[1] = (byte) SUBTRACT;
            RawXClient.writeShortAt(subtract, 2, 4);
            RawXClient.writeIntAt(subtract, 4, damageId);
            RawXClient.writeIntAt(subtract, 8, 0);      // no repair region: just clear it
            client.sendRequest(subtract);
            client.fillRectangle(window, gc, 60, 40, 20, 20);
            client.flush();
            report(client, firstEvent, 600);

            byte[] destroy = new byte[8];
            destroy[0] = (byte) opcode;
            destroy[1] = (byte) DESTROY;
            destroy[2] = 2;
            RawXClient.writeIntAt(destroy, 4, damageId);
            client.sendRequest(destroy);
            client.flush();
        }
    }

    /** Every DamageNotify within the timeout, one line each. */
    private static void report(RawXClient client, int firstEvent, int millis) throws Exception {
        int seen = 0;
        client.readTimeoutMillis(millis);
        for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
            if ((event[0] & 0x7F) != firstEvent) {
                System.out.printf("  (other event type %d)%n", event[0] & 0x7F);
                continue;
            }
            if (System.getenv("DAMAGE_HEX") != null) {
                StringBuilder hex = new StringBuilder();
                for (byte b : event) {
                    hex.append(String.format("%02x", b));
                }
                System.out.println("  raw: " + hex);
            }
            seen++;
            // The event is thirty two bytes like every other one this kit reads: a rectangle in it is
            // four shorts, not the sixteen bytes the first version of this assumed.
            System.out.printf("  DamageNotify level=%d drawable=0x%x damage=0x%x "
                            + "area=%d,%d %dx%d geometry=%d,%d %dx%d%n",
                    event[1] & 0xFF, RawXClient.readInt(event, 4), RawXClient.readInt(event, 8),
                    RawXClient.readShort(event, 16), RawXClient.readShort(event, 18),
                    RawXClient.readShort(event, 20), RawXClient.readShort(event, 22),
                    RawXClient.readShort(event, 24), RawXClient.readShort(event, 26),
                    RawXClient.readShort(event, 28), RawXClient.readShort(event, 30));
        }
        client.readTimeoutMillis(5000);
        System.out.println("  notifications: " + seen);
    }

    private DamageProbe() {
    }
}
