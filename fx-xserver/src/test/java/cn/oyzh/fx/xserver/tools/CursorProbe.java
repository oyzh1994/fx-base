package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

import java.io.IOException;

/**
 * Prints the XFixes cursor notifications a client gets while the pointer moves between windows with
 * different cursors, so the same run against this server and against a reference server can be
 * diffed.
 *
 * <p>Two windows are mapped with different glyph cursors from the {@code cursor} font, and the
 * pointer is warped between them, then onto the root. The observer subscribes with
 * {@code SelectCursorInput} and prints whatever arrives.
 *
 * <p>Usage: {@code CursorProbe <host> <port>}
 */
public final class CursorProbe {

    private static final int DISPLAY_CURSOR = 0b1;

    /** Glyph numbers in the cursor font: 68 is the standard arrow, 60 the pointing hand. */
    private static final int LEFT_PTR = 68;
    private static final int HAND2 = 60;

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 6095;

        try (RawXClient observer = RawXClient.connect(host, port)) {
            int[] xfixes = observer.queryExtensionInfo("XFIXES");
            System.out.println("XFIXES present=" + xfixes[0] + " major=" + xfixes[1]
                    + " firstEvent=" + xfixes[2]);
            if (xfixes[0] == 0) {
                System.out.println("no XFIXES: nothing to probe");
                return;
            }
            int opcode = xfixes[1];
            int firstEvent = xfixes[2];
            int[] version = observer.queryXfixesVersion(opcode, 4, 0);
            System.out.println("XFIXES version=" + version[0] + "." + version[1]);

            int watched = observer.allocateId();
            observer.createWindow(watched, observer.root(), 0, 0, 50, 50);
            observer.mapWindow(watched);
            observer.selectCursorInput(opcode, watched, DISPLAY_CURSOR);
            observer.flush();
            System.out.println("subscribed on 0x" + Integer.toHexString(watched));
            readEvents(observer, firstEvent, 500);

            // Two cursors out of the font every X client has.
            int font = observer.allocateId();
            observer.openFont(font, "cursor");
            observer.flush();
            int arrow = observer.allocateId();
            int hand = observer.allocateId();
            observer.createGlyphCursor(arrow, font, font, LEFT_PTR, LEFT_PTR + 1, 0, 0xFFFFFF);
            observer.createGlyphCursor(hand, font, font, HAND2, HAND2 + 1, 0, 0xFFFFFF);
            observer.flush();

            int first = observer.allocateId();
            int second = observer.allocateId();
            observer.createWindow(first, observer.root(), 100, 100, 200, 200, 0x00FF00);
            observer.createWindow(second, observer.root(), 400, 100, 200, 200, 0x0000FF);
            observer.setWindowCursor(first, arrow);
            observer.setWindowCursor(second, hand);
            observer.mapWindow(first);
            observer.mapWindow(second);
            observer.flush();
            readEvents(observer, firstEvent, 500);

            System.out.println();
            System.out.println("-- warp into the arrow window");
            observer.warpPointer(first, 100, 100);
            observer.flush();
            readEvents(observer, firstEvent, 700);

            System.out.println();
            System.out.println("-- warp into the hand window");
            observer.warpPointer(second, 100, 100);
            observer.flush();
            readEvents(observer, firstEvent, 700);

            System.out.println();
            System.out.println("-- warp back into the arrow window");
            observer.warpPointer(first, 100, 100);
            observer.flush();
            readEvents(observer, firstEvent, 700);

            System.out.println();
            System.out.println("-- warp onto the root");
            observer.warpPointer(observer.root(), 50, 400);
            observer.flush();
            readEvents(observer, firstEvent, 700);

            System.out.println();
            System.out.println("-- done");
        }
    }

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
        if (type == firstEvent + 1) {
            System.out.printf("   CursorNotify type=%d subtype=%d window=0x%x cursorSerial=%d "
                            + "timestamp=%d name=%d%n",
                    type, event[1] & 0xFF, RawXClient.readInt(event, 4), RawXClient.readInt(event, 8),
                    RawXClient.readInt(event, 12), RawXClient.readInt(event, 16));
        } else if (type == firstEvent) {
            System.out.printf("   SelectionNotify subtype=%d window=0x%x owner=0x%x%n",
                    event[1] & 0xFF, RawXClient.readInt(event, 4), RawXClient.readInt(event, 8));
        } else if (type == 0) {
            System.out.printf("   ERROR code=%d sequence=%d badValue=0x%x minor=%d major=%d%n",
                    event[1] & 0xFF, RawXClient.readShort(event, 2), RawXClient.readInt(event, 4),
                    RawXClient.readShort(event, 8), event[10] & 0xFF);
        } else {
            System.out.println("   event type=" + type + " (not an XFixes event)");
        }
    }

    private CursorProbe() {
    }
}
