package cn.oyzh.fx.xserver.tools;

import java.util.Locale;

/**
 * Where a keystroke lands when the focus is not on a window of the client's own - on the root window,
 * on {@code PointerRoot}, or nowhere.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1). It exists because "the text field
 * stopped accepting input" is a whole class of client-side symptom that a protocol diff of the focus
 * <em>events</em> cannot see: the events a server sends around a revert can match a reference byte for
 * byte and the keys can still go somewhere else afterwards. What this prints is the one thing that
 * decides it - the window each {@code KeyPress} is reported against - for each of the states the focus
 * can be left in.
 *
 * <pre>
 *   KeyTargetProbe &lt;host&gt; &lt;port&gt; [unmap|kill|root|pointer|none|parent]
 * </pre>
 *
 * <p>The probe maps two windows of its own - {@code P}, which the pointer is parked inside, and
 * {@code D}, the "dialog" the focus is put on - and then, for the mode asked for, leaves the focus
 * somewhere that is not {@code P}:
 *
 * <ul>
 *   <li>{@code unmap} (the default) - {@code SetInputFocus(D, revert_to = Parent)} and then
 *       {@code UnmapWindow(D)}, which is the order a toolkit closes a page or dialog in: the window
 *       that held the focus goes away and the server reverts the focus. The revert lands on the root
 *       window, because {@code D} is a child of the root.</li>
 *   <li>{@code kill} - the same, with {@code DestroyWindow} instead of {@code UnmapWindow}.</li>
 *   <li>{@code root} - {@code SetInputFocus(root)}, the state a revert to the root leaves behind,
 *       reached without a revert.</li>
 *   <li>{@code pointer} - {@code SetInputFocus(PointerRoot)}: the control, where the keys are expected
 *       to follow the pointer into {@code P}.</li>
 *   <li>{@code none} - {@code SetInputFocus(None)}: the other state that is not a window.</li>
 *   <li>{@code parent} - focused on {@code P} itself with {@code revert_to = Parent}: the control for
 *       "the keys do reach the window the pointer is in".</li>
 * </ul>
 *
 * <p>Every message is printed, and a {@code KeyPress} names the window it was reported against, so the
 * answer is read off the transcript: which window the server chose, and whether the client that owns
 * the window under the pointer was told at all. The header of {@code tools/README.md} says what the
 * measurement against a reference server was and what it decided.
 */
public final class KeyTargetProbe {

    private static final int KEY_PRESS = 2;
    private static final int KEY_RELEASE = 3;

    /** {@code FocusChangeMask}, {@code EnterWindowMask}, {@code LeaveWindowMask}, and the key pair. */
    private static final int KEY_AND_FOCUS = 0x1 | 0x2 | 0x4 | 0x10 | 0x20;

    /** Keycode 38 is {@code a} on the layout both servers start with - a key no client redefines. */
    private static final int KEYCODE_A = 38;

    /** The root window is 0x1ff on a reference server and whatever the server numbers it here. */
    private static int p;
    private static int d;
    private static int root;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: KeyTargetProbe <host> <port>"
                    + " [unmap|kill|root|pointer|none|parent]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String mode = args.length > 2 ? args[2].toLowerCase(Locale.ROOT) : "unmap";

        try (Wire wire = Wire.connect(host, port)) {
            p = wire.allocateId();
            d = wire.allocateId();
            root = wire.root();
            int xtest = queryExtension(wire, "XTEST");
            if (xtest == 0) {
                System.out.println("this display has no XTEST: the probe cannot type");
                return;
            }

            // The pointer outlives the client, so it is parked somewhere neither window reaches
            // before anything is mapped: a run that starts with the pointer already inside P would
            // otherwise report a different first crossing than the run before it.
            fakeMotion(wire, xtest, 1000, 700);
            createWindow(wire, p, root, 600, 200, 300, 300);
            selectInput(wire, p, KEY_AND_FOCUS);
            map(wire, p);
            createWindow(wire, d, root, 900, 400, 200, 200);
            map(wire, d);
            drain(wire);
            System.out.println("P = 0x" + Integer.toHexString(p) + " (600,200 300x300), pointer parked"
                    + " outside; D = 0x" + Integer.toHexString(d) + " (900,400 200x200)");

            System.out.println("step: move the pointer into P");
            fakeMotion(wire, xtest, 700, 300);
            drain(wire);

            switch (mode) {
                case "unmap" -> {
                    System.out.println("step: focus D (revert_to = Parent), then unmap it");
                    setInputFocus(wire, d, 2);
                    sync(wire);
                    unmapWindow(wire, d);
                }
                case "kill" -> {
                    System.out.println("step: focus D (revert_to = Parent), then destroy it");
                    setInputFocus(wire, d, 2);
                    sync(wire);
                    destroyWindow(wire, d);
                }
                case "root" -> {
                    System.out.println("step: focus the root window (revert_to = Parent)");
                    setInputFocus(wire, root, 2);
                }
                case "pointer" -> {
                    System.out.println("step: focus PointerRoot");
                    setInputFocus(wire, 1, 2);
                }
                case "none" -> {
                    System.out.println("step: focus None");
                    setInputFocus(wire, 0, 2);
                }
                case "parent" -> {
                    System.out.println("step: focus P itself (revert_to = Parent)");
                    setInputFocus(wire, p, 2);
                }
                default -> throw new IllegalArgumentException(mode);
            }
            sync(wire);

            System.out.println("step: press and release keycode " + KEYCODE_A + " with the pointer"
                    + " inside P");
            fakeKey(wire, xtest, KEY_PRESS, KEYCODE_A);
            fakeKey(wire, xtest, KEY_RELEASE, KEYCODE_A);
            sync(wire);
            System.out.println("done");
        }
    }

    /**
     * Prints every message until a {@code GetInputFocus} reply arrives, so a step's events are read
     * before the next step's are sent - requests are answered in order, which is a cheaper and more
     * certain sync than sleeping.
     */
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
                System.out.println("  focus = " + describeFocus(Wire.readInt(message, 8)));
                return;
            }
            describe(message);
        }
    }

    /** Whatever the server sent while nothing was being asked for, with the window named. */
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
            // A key event's window is its "event" field, which is what says *which* window the server
            // decided the key belonged to - the whole point of this probe. It sits at 12, after the
            // time and the root window; offset 4 is the *time*, which reads as a plausible window id
            // and is how the first version of this printed a window that exists on no server.
            case KEY_PRESS, KEY_RELEASE -> System.out.printf("  %s win=%s state=0x%02x keycode=%d%n",
                    type == KEY_PRESS ? "KeyPress" : "KeyRelease", name(Wire.readInt(message, 12)),
                    Wire.readShort(message, 28), message[1] & 0xFF);
            case 9, 10 -> System.out.printf("  %s win=%s detail=%d mode=%d%n",
                    type == 9 ? "FocusIn" : "FocusOut", name(Wire.readInt(message, 4)),
                    message[1] & 0xFF, message[8] & 0xFF);
            case 7, 8 -> System.out.printf("  %s win=%s detail=%d mode=%d%n",
                    type == 7 ? "EnterNotify" : "LeaveNotify", name(Wire.readInt(message, 12)),
                    message[1] & 0xFF, message[30] & 0xFF);
            case 0 -> System.out.printf("  ERROR %d opcode=%d resource=0x%x%n", message[1] & 0xFF,
                    message[10] & 0xFF, Wire.readInt(message, 4));
            default -> System.out.println("  other event type " + type);
        }
    }

    /** A focus that is not one of the probe's windows is named as the state it is. */
    private static String describeFocus(int focus) {
        if (focus == 0) {
            return "None";
        }
        if (focus == 1) {
            return "PointerRoot";
        }
        return "win " + name(focus);
    }

    private static String name(int window) {
        if (window == p) {
            return "P";
        }
        if (window == d) {
            return "D";
        }
        if (window == root) {
            return "R";
        }
        return "0x" + Integer.toHexString(window);
    }

    private static int type(byte[] message) {
        return message[0] & 0x7F;
    }

    // --- the requests, hand written, because the generated decoders are the server's business ---

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
        fakeInput(wire, opcode, 6, 0, x, y);        // XTest's types are X's own event numbers
    }

    private static void fakeKey(Wire wire, int opcode, int type, int keycode) throws Exception {
        fakeInput(wire, opcode, type, keycode, 0, 0);
    }

    private static void fakeInput(Wire wire, int opcode, int type, int detail, int x, int y)
            throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) opcode;
        request[1] = 2;                             // X_XTestFakeInput
        Wire.writeShort(request, 2, 9);
        request[4] = (byte) type;
        request[5] = (byte) detail;
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

    private KeyTargetProbe() {
    }
}
