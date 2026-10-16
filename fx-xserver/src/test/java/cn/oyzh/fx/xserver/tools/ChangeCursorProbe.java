package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * What {@code XFixesChangeCursor} does to the two cursors it names - A8's last refusal.
 *
 * <p>The request takes two cursors, {@code source} and {@code destination}, and what it means for
 * one of them to change is measured rather than read off the specification: the two questions are
 * **which** of the two ends up with the other's image, and whether a window already showing it sees
 * the change (a cursor is a shared resource in X, so a rewrite should reach every window at once).
 *
 * <p>The probe makes two cursors with different glyphs from the font {@code fixed}, puts them on two
 * windows, and reads each window's cursor back through XFIXES' {@code GetCursorImage} - before and
 * after the change. It also watches the cursor *notifications*, because a rewrite is a change to a
 * cursor a client may be watching, and the event's serial says which cursor it thinks changed.
 *
 * <p>Usage: {@code ChangeCursorProbe <host> <port>} - step 3 asks for the {@code fixed} font, so a
 * server that does not have it will say so rather than draw nothing.
 */
public final class ChangeCursorProbe {

    private static final int GET_CURSOR_IMAGE = 4;
    private static final int CHANGE_CURSOR = 26;
    private static final int SELECT_CURSOR_INPUT = 3;

    public static void main(String[] args) throws Exception {
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int[] xfixes = client.queryExtensionInfo("XFIXES");
            if (xfixes[0] == 0) {
                System.out.println("no XFIXES");
                return;
            }
            int opcode = xfixes[1];
            int firstEvent = xfixes[2];
            int[] version = client.queryXfixesVersion(opcode, 4, 0);
            System.out.println("XFIXES version " + version[0] + "." + version[1]);

            int watcher = client.allocateId();
            client.createWindow(watcher, client.root(), 0, 0, 30, 30);
            client.mapWindow(watcher);
            client.selectCursorInput(opcode, watcher, 1);
            client.flush();

            int font = client.allocateId();
            client.openFont(font, "fixed");
            int first = client.allocateId();
            int second = client.allocateId();
            // 'A' and 'B' rather than two cursor-font shapes: their glyphs are visibly different, and
            // the reading is the shape rather than a size both would share.
            client.createGlyphCursor(first, font, font, 'A', 'A', 0xFFFFFF, 0x000000);
            client.createGlyphCursor(second, font, font, 'B', 'B', 0xFFFFFF, 0x000000);

            int windowOne = client.allocateId();
            windowTwo = client.allocateId();
            firstCursor = first;
            secondCursor = second;
            client.createWindow(windowOne, client.root(), 100, 100, 60, 60, 0x303030);
            client.createWindow(windowTwo, client.root(), 300, 100, 60, 60, 0x303030);
            client.setWindowCursor(windowOne, first);
            client.setWindowCursor(windowTwo, second);
            client.mapWindow(windowOne);
            client.mapWindow(windowTwo);
            client.flush();

            System.out.println("-- before");
            show(client, opcode, windowOne, "window with source");
            // The pointer is left over the *destination* window, so the change is to the cursor being
            // displayed - which is the case that says whether subscribers hear about a rewrite.
            show(client, opcode, windowTwo, "window with destination");

            // XFixesChangeCursor: source, then destination.
            byte[] request = new byte[12];
            request[0] = (byte) opcode;
            request[1] = (byte) CHANGE_CURSOR;
            RawXClient.writeShortAt(request, 2, 3);
            RawXClient.writeIntAt(request, 4, first);
            RawXClient.writeIntAt(request, 8, second);
            client.sendRequest(request);
            client.flush();
            // A round trip after it, so the events it raises have arrived before they are counted.
            client.queryTree(watcher);
            System.out.println("-- after ChangeCursor(source, destination)");
            show(client, opcode, windowOne, "window with source");
            show(client, opcode, windowTwo, "window with destination");

            // And ChangeCursorByName: one cursor and a name from the cursor font, so the question is
            // whether the name is looked up in that font and what the cursor reports afterwards.
            byName(client, opcode, windowTwo, "left_ptr");
            byName(client, opcode, windowTwo, "hand2");
            byName(client, opcode, windowTwo, "xserver-no-such-cursor-name");

            // And the notifications, which say whether a rewrite is a cursor change a client hears
            // about, and which serial it carries.
            int events = 0;
            client.readTimeoutMillis(600);
            for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
                if ((event[0] & 0x7F) == firstEvent) {
                    events++;
                    System.out.printf("  CursorNotify window=0x%x serial=%d%n",
                            RawXClient.readInt(event, 4), RawXClient.readInt(event, 8));
                }
            }
            client.readTimeoutMillis(5000);
            System.out.println("  cursor notifications during the change: " + events);
        }
    }

    /** XFixesChangeCursorByName(cursor, name), then the cursor that window shows. */
    private static void byName(RawXClient client, int opcode, int window, String name)
            throws Exception {
        byte[] bytes = name.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        byte[] request = new byte[12 + ((bytes.length + 3) & ~3)];
        request[0] = (byte) opcode;
        request[1] = 27;                                // X_XFixesChangeCursorByName
        RawXClient.writeShortAt(request, 2, request.length / 4);
        // The cursor to change is the one the window shows, so this is the window's own cursor id:
        // the probe keeps it in the window's attributes, and the caller passes the window.
        RawXClient.writeIntAt(request, 4, cursorOf(window));
        RawXClient.writeShortAt(request, 8, bytes.length);
        System.arraycopy(bytes, 0, request, 12, bytes.length);
        client.sendRequest(request);
        client.flush();
        // The round trip is only how the server is known to have processed the request, and **an
        // error is delivered before the reply** - so a server that refuses this request answers the
        // round trip with the error and the helper that waits for a reply reports it as a failure.
        // That refusal is one of the answers this probe was built to measure (a reference accepts
        // every name and does nothing with it; this server refuses the request outright, see A8), so
        // it is printed and the probe carries on to the next name rather than dying here.
        try {
            client.queryTree(window);
        } catch (java.io.IOException refused) {
            System.out.printf("-- ChangeCursorByName(%s): refused (%s)%n", name,
                    refused.getMessage());
            // The server answered the refused request and *then* the round trip that followed it,
            // so that reply is still queued: read it, or every later answer is one sequence out of
            // step - which the client reports as "a reply was left unread, and every answer after
            // it is suspect", and which is what the first version of this fix ran into.
            client.readTimeoutMillis(5000);
            client.readReply();
            return;
        }
        // An error for a request like this arrives asynchronously, so it is read here rather than
        // assumed away: a name the server does not know is refused, and the first version of this
        // probe swallowed that and reported "no change" for every name alike.
        client.readTimeoutMillis(300);
        for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
            if ((event[0] & 0x7F) == 0) {
                System.out.printf("-- ChangeCursorByName(%s): refused, error %d%n", name,
                        event[1] & 0xFF);
                client.readTimeoutMillis(5000);
                return;
            }
        }
        client.readTimeoutMillis(5000);
        System.out.println("-- after ChangeCursorByName(" + name + ")");
        show(client, opcode, window, "window that was showing it");
        // And a window that takes the *same cursor id* afterwards: whether the name changed the
        // cursor the id means, or the cursor the window holds, is the difference between "nothing
        // happened" and "the id was rebound" - and a window already holding it cannot tell them apart.
        int fresh = client.allocateId();
        client.createWindow(fresh, client.root(), 500, 300, 60, 60, 0x303030);
        client.setWindowCursor(fresh, cursorOf(window));
        client.mapWindow(fresh);
        client.flush();
        show(client, opcode, fresh, "window created afterwards");
        client.destroyWindow(fresh);
        client.queryTree(client.root());
    }

    /** The cursor this probe gave that window: the second one it made. */
    private static int cursorOf(int window) {
        return window == windowTwo ? secondCursor : firstCursor;
    }

    private static int windowTwo;
    private static int firstCursor;
    private static int secondCursor;

    /** XFixesGetCursorImage for the cursor the given window's pointer shows. */
    private static void show(RawXClient client, int opcode, int window, String what)
            throws Exception {
        client.warpPointer(window, 30, 30);
        client.queryTree(window);                       // the pointer has moved
        byte[] request = new byte[4];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_CURSOR_IMAGE;
        RawXClient.writeShortAt(request, 2, 1);
        client.sendRequest(request);
        byte[] reply = client.readReply();
        int width = RawXClient.readShort(reply, 12);
        int height = RawXClient.readShort(reply, 14);
        StringBuilder shape = new StringBuilder();
        for (int i = 0; i < Math.min(width * height, 78); i++) {
            int argb = RawXClient.readInt(reply, 32 + i * 4);
            shape.append((argb >>> 24) == 0 ? '.' : '#');
        }
        System.out.printf("  %-24s %dx%d serial=%d %s%n", what, width, height,
                RawXClient.readInt(reply, 20), shape);
    }

    private ChangeCursorProbe() {
    }
}
