package cn.oyzh.fx.xserver.tools;

/**
 * What a client is told when a window that holds the focus <b>and the pointer</b> goes away.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1), and the harness for the corner that
 * cost a real report: a page or a dialog covers part of the window behind it, the client moves the
 * focus to it, and the click that closes it lands inside it - so the window that goes away is the one
 * the pointer is in, and the focus that reverts has to name the window the pointer is <em>now</em> in
 * rather than the one that is leaving.
 *
 * <pre>
 *   FocusRevertProbe &lt;host&gt; &lt;port&gt; [unmap|kill]
 * </pre>
 *
 * <p>Two windows of one client, both children of the root and overlapping - {@code A} at 0,0
 * 300x300 and {@code PAGE} at 100,100 100x100, so the point the pointer is parked on (150,150) is
 * inside both. {@code A} selects {@code FocusChangeMask} and the crossings, the pointer is warped in,
 * the focus is put on {@code PAGE} with {@code revert_to = Parent}, and then {@code PAGE} either
 * unmaps or is destroyed. Every message that follows is printed, which is the whole measurement: with
 * the pointer in the window that goes away, a reference server reports
 *
 * <pre>
 *   FocusOut win=PAGE detail=Ancestor
 *   FocusIn  win=A    detail=Inferior        (A is what the pointer is in now)
 *   LeaveNotify win=PAGE / EnterNotify win=A
 * </pre>
 *
 * and this server used to walk the chain from {@code PAGE} - reporting {@code FocusIn PAGE
 * detail=Pointer} to a window nobody can act on, and saying nothing at all about {@code A}.
 */
public final class FocusRevertProbe {

    private static final int FOCUS_DETAILS = 8;
    private static final String[] DETAIL_NAMES = {"Ancestor", "Virtual", "Inferior", "Nonlinear",
            "NonlinearVirtual", "Pointer", "PointerRoot", "None"};
    private static final String[] MODE_NAMES = {"NotifyNormal", "NotifyGrab", "NotifyUngrab",
            "NotifyWhileGrabbed"};

    private static int a;
    private static int page;
    private static int root;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: FocusRevertProbe <host> <port> [unmap|kill]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        boolean kill = args.length > 2 && args[2].equals("kill");

        try (Wire wire = Wire.connect(host, port)) {
            a = wire.allocateId();
            page = wire.allocateId();
            root = wire.root();
            int xtest = queryExtension(wire, "XTEST");
            // The pointer outlives the client, so it is parked outside both windows first.
            fakeMotion(wire, xtest, 1000, 700);
            createWindow(wire, a, root, 0, 0, 300, 300);
            createWindow(wire, page, root, 100, 100, 100, 100);
            selectInput(wire, a, 0x1 | 0x2 | 0x4 | 0x10 | 0x20
                    | 0x800000);                    // FocusChange, crossings and the key pair
            map(wire, a);
            map(wire, page);
            drain(wire);
            System.out.println("A = 0x" + Integer.toHexString(a) + " (0,0 300x300), PAGE = 0x"
                    + Integer.toHexString(page) + " (100,100 100x100)");

            System.out.println("step: the pointer into both windows, at 150,150");
            fakeMotion(wire, xtest, 150, 150);
            drain(wire);

            System.out.println("step: focus PAGE (revert_to = Parent)");
            setInputFocus(wire, page, 2);
            sync(wire);

            System.out.println("step: " + (kill ? "destroy" : "unmap") + " PAGE, with the pointer"
                    + " still in it");
            if (kill) {
                destroyWindow(wire, page);
            } else {
                unmapWindow(wire, page);
            }
            sync(wire);
            System.out.println("done");
        }
    }

    /** Prints every message until a {@code GetInputFocus} reply arrives. */
    private static void sync(Wire wire) throws Exception {
        byte[] request = new byte[4];
        request[0] = 43;                            // GetInputFocus
        request[2] = 1;
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                System.out.println("  (no answer to GetInputFocus)");
                return;
            }
            if (type(message) == 1) {
                System.out.println("  focus = " + name(Wire.readInt(message, 8)));
                return;
            }
            describe(message);
        }
    }

    private static void drain(Wire wire) throws Exception {
        wire.readTimeoutMillis(150);
        for (byte[] message = wire.readMessage(); message != null; message = wire.readMessage()) {
            describe(message);
        }
        wire.readTimeoutMillis(3000);
    }

    private static void describe(byte[] message) {
        int type = type(message);
        switch (type) {
            case 9, 10 -> System.out.printf("  %s win=%s detail=%s mode=%s%n",
                    type == 9 ? "FocusIn" : "FocusOut", name(Wire.readInt(message, 4)),
                    detail(message[1] & 0xFF), mode(message[8] & 0xFF));
            // The last two bytes of a crossing are the two flags a toolkit acts on: same_screen at 30
            // and focus at 31 - and "the focus is in this window" is what a client with no
            // window manager has to go by.
            case 7, 8 -> System.out.printf("  %s win=%s detail=%s mode=%s same_screen=%d focus=%d%n",
                    type == 7 ? "EnterNotify" : "LeaveNotify", name(Wire.readInt(message, 12)),
                    detail(message[1] & 0xFF), mode(message[30] & 0xFF), message[30] & 0xFF,
                    message[31] & 0xFF);
            case 0 -> System.out.printf("  ERROR %d opcode=%d resource=0x%x%n", message[1] & 0xFF,
                    message[10] & 0xFF, Wire.readInt(message, 4));
            default -> System.out.println("  other event type " + type);
        }
    }

    private static String detail(int value) {
        return value < FOCUS_DETAILS ? DETAIL_NAMES[value] : "detail" + value;
    }

    private static String mode(int value) {
        return value < MODE_NAMES.length ? MODE_NAMES[value] : "mode" + value;
    }

    private static String name(int window) {
        if (window == a) {
            return "A";
        }
        if (window == page) {
            return "PAGE";
        }
        if (window == root) {
            return "R";
        }
        return window == 0 ? "None" : window == 1 ? "PointerRoot" : "0x"
                + Integer.toHexString(window);
    }

    private static int type(byte[] message) {
        return message[0] & 0x7F;
    }

    // --- the requests, hand written, like every probe here ---

    private static void createWindow(Wire wire, int window, int parent, int x, int y, int width,
                                     int height) throws Exception {
        byte[] request = new byte[32];
        request[0] = 1;                             // CreateWindow
        Wire.writeShort(request, 2, 8);
        Wire.writeInt(request, 4, window);
        Wire.writeInt(request, 8, parent);
        Wire.writeShort(request, 12, x);
        Wire.writeShort(request, 14, y);
        Wire.writeShort(request, 16, width);
        Wire.writeShort(request, 18, height);
        Wire.writeShort(request, 22, 1);            // InputOutput
        wire.send(request);
    }

    private static void map(Wire wire, int window) throws Exception {
        byte[] request = new byte[8];
        request[0] = 8;                             // MapWindow
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, window);
        wire.send(request);
    }

    private static void unmapWindow(Wire wire, int window) throws Exception {
        byte[] request = new byte[8];
        request[0] = 10;                            // UnmapWindow
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, window);
        wire.send(request);
    }

    private static void destroyWindow(Wire wire, int window) throws Exception {
        byte[] request = new byte[8];
        request[0] = 4;                             // DestroyWindow
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, window);
        wire.send(request);
    }

    private static void selectInput(Wire wire, int window, int mask) throws Exception {
        byte[] request = new byte[16];
        request[0] = 2;                             // ChangeWindowAttributes
        Wire.writeShort(request, 2, 4);
        Wire.writeInt(request, 4, window);
        Wire.writeInt(request, 8, 0x800);           // CWEventMask
        Wire.writeInt(request, 12, mask);
        wire.send(request);
    }

    private static void setInputFocus(Wire wire, int focus, int revertTo) throws Exception {
        byte[] request = new byte[12];
        request[0] = 42;                            // SetInputFocus
        request[1] = (byte) revertTo;
        Wire.writeShort(request, 2, 3);
        Wire.writeInt(request, 4, focus);
        Wire.writeInt(request, 8, 0);               // CurrentTime
        wire.send(request);
    }

    private static void fakeMotion(Wire wire, int opcode, int x, int y) throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) opcode;
        request[1] = 2;                             // X_XTestFakeInput
        Wire.writeShort(request, 2, 9);
        request[4] = 6;                             // MotionNotify
        Wire.writeShort(request, 24, x);
        Wire.writeShort(request, 26, y);
        wire.send(request);
    }

    private static int queryExtension(Wire wire, String name) throws Exception {
        byte[] request = new byte[(8 + name.length() + 3) & ~3];
        request[0] = 98;                            // QueryExtension
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeShort(request, 4, name.length());
        for (int i = 0; i < name.length(); i++) {
            request[8 + i] = (byte) name.charAt(i);
        }
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                return 0;
            }
            if (type(message) == 1) {
                return message[8] == 0 ? 0 : message[9] & 0xFF;
            }
        }
    }

    private FocusRevertProbe() {
    }
}
