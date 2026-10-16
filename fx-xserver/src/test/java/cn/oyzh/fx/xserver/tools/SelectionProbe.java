package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

import java.io.IOException;

/**
 * Prints the XFixes selection notifications a client gets, request for request, so the same run
 * against this server and against a reference server can be diffed.
 *
 * <p>This is backlog item A8's harness. A second connection is used because the events exist to tell
 * an observer that somebody <em>else</em> took the selection: one client subscribes and prints, and
 * other clients take ownership, destroy their window and then disconnect - the three subtypes the
 * subscription covers.
 *
 * <p>A reference server rejects every XFixes request from a client that never called
 * {@code XFixesQueryVersion}, which is measured here rather than assumed: the same probe without the
 * version call gets {@code BadRequest (minor 2)} back instead of a subscription. Every real client
 * asks first, so this only shows up in a hand-written probe.
 *
 * <p>Usage: {@code SelectionProbe <host> <port> [selection-name]} - the name defaults to
 * {@code CLIPBOARD}, but any selection can be probed, which is how a client that watches something
 * else (EasyShell watches {@code _NET_WM_CM_S0} to see whether a compositor appears) is checked.
 */
public final class SelectionProbe {

    /** The three selection bits: SetSelectionOwner, SelectionWindowDestroy, SelectionClientClose. */
    private static final int ALL = 0b111;

    private static final String CLIPBOARD = "CLIPBOARD";

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 6095;
        String selectionName = args.length > 2 ? args[2] : CLIPBOARD;

        try (RawXClient observer = RawXClient.connect(host, port)) {
            int[] xfixes = observer.queryExtensionInfo("XFIXES");
            System.out.println("XFIXES present=" + xfixes[0] + " major=" + xfixes[1]
                    + " firstEvent=" + xfixes[2] + " firstError=" + xfixes[3]);
            if (xfixes[0] == 0) {
                System.out.println("no XFIXES: nothing to probe");
                return;
            }
            int opcode = xfixes[1];
            int firstEvent = xfixes[2];

            int[] version = observer.queryXfixesVersion(opcode, 4, 0);
            System.out.println("XFIXES version=" + version[0] + "." + version[1]);

            int clipboard = observer.internAtom(selectionName);
            System.out.println(selectionName + " atom=" + clipboard);

            int watched = observer.allocateId();
            observer.createWindow(watched, observer.root(), 0, 0, 100, 100);
            observer.selectSelectionInput(opcode, watched, clipboard, ALL);
            observer.flush();
            System.out.println("subscribed on 0x" + Integer.toHexString(watched) + " mask=" + ALL);

            // The window goes away first: the owner is gone but the client is still connected.
            System.out.println();
            System.out.println("-- take ownership, then destroy the owner window");
            try (RawXClient owner = connect(host, port, selectionName)) {
                int holder = owner.allocateId();
                owner.createWindow(holder, owner.root(), 0, 0, 100, 100);
                owner.flush();
                System.out.println("   owner window 0x" + Integer.toHexString(holder));
                owner.setSelectionOwner(holder, clipboard, 0);
                owner.flush();
                readEvents(observer, firstEvent, 700);

                System.out.println("   DestroyWindow");
                owner.destroyWindow(holder);
                owner.flush();
                readEvents(observer, firstEvent, 700);
            }

            // And now the other order: the client disconnects while its window is still alive.
            System.out.println();
            System.out.println("-- take ownership, then disconnect with the window alive");
            int stillAlive;
            try (RawXClient owner = connect(host, port, selectionName)) {
                stillAlive = owner.allocateId();
                owner.createWindow(stillAlive, owner.root(), 0, 0, 100, 100);
                owner.flush();
                System.out.println("   owner window 0x" + Integer.toHexString(stillAlive));
                owner.setSelectionOwner(stillAlive, clipboard, 0);
                owner.flush();
                readEvents(observer, firstEvent, 700);
                System.out.println("   disconnect");
            }
            readEvents(observer, firstEvent, 700);

            // Giving the selection up explicitly is a fourth way for it to change hands.
            System.out.println();
            System.out.println("-- SetSelectionOwner(None)");
            try (RawXClient owner = connect(host, port, selectionName)) {
                int holder = owner.allocateId();
                owner.createWindow(holder, owner.root(), 0, 0, 100, 100);
                owner.setSelectionOwner(holder, clipboard, 0);
                owner.flush();
                readEvents(observer, firstEvent, 700);
                System.out.println("   SetSelectionOwner(None)");
                owner.setSelectionOwner(0, clipboard, 0);
                owner.flush();
                readEvents(observer, firstEvent, 700);
            }

            // The same owner taking the selection again: does it count as a change?
            System.out.println();
            System.out.println("-- the same window takes the selection twice");
            try (RawXClient owner = connect(host, port, selectionName)) {
                int holder = owner.allocateId();
                owner.createWindow(holder, owner.root(), 0, 0, 100, 100);
                owner.setSelectionOwner(holder, clipboard, 0);
                owner.flush();
                readEvents(observer, firstEvent, 700);
                System.out.println("   SetSelectionOwner, same window again");
                owner.setSelectionOwner(holder, clipboard, 0);
                owner.flush();
                readEvents(observer, firstEvent, 700);
            }

            // Giving up a selection nobody owns.
            System.out.println();
            System.out.println("-- SetSelectionOwner(None) with no owner at all");
            try (RawXClient owner = connect(host, port, selectionName)) {
                owner.setSelectionOwner(0, clipboard, 0);
                owner.flush();
                readEvents(observer, firstEvent, 700);
            }

            // Does the subscription survive the window it was made on? The window is only there to
            // name the screen and to be carried in the event, so this says whether a client has to
            // keep a window alive to keep hearing about the clipboard.
            System.out.println();
            System.out.println("-- destroy the subscribed window, then change the owner");
            observer.destroyWindow(watched);
            observer.flush();
            readEvents(observer, firstEvent, 700);
            try (RawXClient owner = connect(host, port, selectionName)) {
                int holder = owner.allocateId();
                owner.createWindow(holder, owner.root(), 0, 0, 100, 100);
                owner.setSelectionOwner(holder, clipboard, 0);
                owner.flush();
                readEvents(observer, firstEvent, 700);
            }

            System.out.println();
            System.out.println("-- done");
        }
    }

    /** A second connection that has interned the selection, so both sides name the same atom. */
    private static RawXClient connect(String host, int port, String anotherSelection) throws IOException {
        RawXClient client = RawXClient.connect(host, port);
        client.internAtom(anotherSelection);
        return client;
    }

    /** Drains everything the server sends for {@code millis}, printing each event. */
    private static void readEvents(RawXClient client, int firstEvent, int millis)
            throws IOException {
        client.readTimeoutMillis(millis);
        for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
            print(event, firstEvent);
        }
        client.readTimeoutMillis(5000);
    }

    private static void print(byte[] event, int firstEvent) {
        int type = event[0] & 0x7F;
        int subtype = event[1] & 0xFF;
        if (type == firstEvent) {
            System.out.printf("   SelectionNotify type=%d subtype=%d window=0x%x owner=0x%x "
                            + "selection=%d timestamp=%d selectionTimestamp=%d%n",
                    type, subtype, RawXClient.readInt(event, 4), RawXClient.readInt(event, 8),
                    RawXClient.readInt(event, 12), RawXClient.readInt(event, 16),
                    RawXClient.readInt(event, 20));
        } else if (type == firstEvent + 1) {
            System.out.printf("   CursorNotify type=%d subtype=%d window=0x%x cursorSerial=%d "
                            + "timestamp=%d name=%d%n",
                    type, subtype, RawXClient.readInt(event, 4), RawXClient.readInt(event, 8),
                    RawXClient.readInt(event, 12), RawXClient.readInt(event, 16));
        } else if (type == 0) {
            System.out.printf("   ERROR code=%d sequence=%d badValue=0x%x minor=%d major=%d%n",
                    event[1] & 0xFF, RawXClient.readShort(event, 2),
                    RawXClient.readInt(event, 4), RawXClient.readShort(event, 8),
                    event[10] & 0xFF);
        } else {
            System.out.println("   event type=" + type + " (not an XFixes event)");
        }
    }

    private SelectionProbe() {
    }
}
