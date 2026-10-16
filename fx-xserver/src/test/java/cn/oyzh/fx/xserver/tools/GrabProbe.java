package cn.oyzh.fx.xserver.tools;

/**
 * Arms a passive grab and then uses the pointer or the keyboard the way a user would, printing every
 * event that comes back.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1), and the harness for item A1 - the
 * largest hole left in this server's input model. A passive grab is {@code GrabButton}/{@code GrabKey}:
 * a client says "when this button is pressed (or this key typed) on this window or anything inside it,
 * I want the input", and a *server* that records it and never matches it leaves every global shortcut,
 * every toolkit popup binding and every window manager's title-bar drag doing nothing at all.
 *
 * <pre>
 *   GrabProbe &lt;host&gt; &lt;port&gt; button [ownerEvents] [modifiers] [button] [eventMask]
 *   GrabProbe &lt;host&gt; &lt;port&gt; key [ownerEvents] [modifiers] [keycode] [eventMask]
 * </pre>
 *
 * <p><b>The grab is armed on the root, and the window the pointer is over belongs to a second
 * connection.</b> That is the arrangement that can tell "the grab took the press" from "the press went
 * where it landed": without the grab the press belongs to the other client, and with it to the
 * grabbing one. The two arrangements that look more natural do not work, and both were tried:
 * a press inside the grabbing client's own window belongs to that client either way, and a covering
 * window from another client cannot be made a child of the grab window (the reference refuses the
 * {@code CreateWindow}, so a grab on an ancestor of somebody else's window is not something a client
 * can build). The root is also the real case - {@code GrabButton} on the root is a global shortcut.
 *
 * <p>Every event <em>either</em> client receives is printed, labelled, with the window it names, its
 * detail and its mode: the mode is where a grab's own crossings show up, and which of them a reference
 * server sends is exactly what the design notes' §7 deviations needed to know (they were measured for
 * *active* grabs, and a passive one is not the same case).
 */
public final class GrabProbe {

    private static final int BUTTON_PRESS = 0x4;
    private static final int BUTTON_RELEASE = 0x8;
    private static final int ENTER_WINDOW = 0x10;
    private static final int LEAVE_WINDOW = 0x20;
    /** The left Shift keycode, for the modifier rule: a grab armed with a modifier needs it held. */
    private static final int SHIFT_L = 50;

    private static final int KEY_PRESS = 0x1;
    private static final int KEY_RELEASE = 0x2;

    private static final String[] DETAILS = {"Ancestor", "Virtual", "Inferior", "Nonlinear",
            "NonlinearVirtual"};
    private static final String[] MODES = {"NotifyNormal", "NotifyGrab", "NotifyUngrab",
            "NotifyWhileGrabbed"};
    private static final String[] TYPES = {"error", "reply", "KeyPress", "KeyRelease", "ButtonPress",
            "ButtonRelease", "MotionNotify", "EnterNotify", "LeaveNotify", "FocusIn", "FocusOut",
            "KeymapNotify", "Expose", "GraphicsExpose", "NoExpose", "VisibilityNotify",
            "CreateNotify", "DestroyNotify", "UnmapNotify", "MapNotify", "MapRequest", "ReparentNotify",
            "ConfigureNotify", "ConfigureRequest", "GravityNotify", "ResizeRequest", "CirculateNotify",
            "CirculateRequest", "PropertyNotify", "SelectionClear", "SelectionRequest",
            "SelectionNotify", "ColormapNotify", "ClientMessage", "MappingNotify", "GenericEvent"};

    private static int armed;
    private static int cover;

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("usage: GrabProbe <host> <port> button|key [ownerEvents] [modifiers] "
                    + "[detail] [eventMask]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        boolean keyboard = args[2].equals("key");
        boolean ownerEvents = args.length > 3 && args[3].equals("owner");
        int modifiers = args.length > 4 ? Integer.parseInt(args[4]) : 0x8000;   // AnyModifier
        int detail = args.length > 5 ? Integer.parseInt(args[5]) : 0;           // AnyButton / AnyKey
        int eventMask = args.length > 6 && !args[6].equals("shift") && !args[6].equals("noshift")
                ? Integer.parseInt(args[6])
                : keyboard ? KEY_PRESS | KEY_RELEASE : BUTTON_PRESS | BUTTON_RELEASE;
        // Whether Shift is held while the button goes down, which is the whole of the modifier rule:
        // a grab armed with a modifier only fires for a press that has it, and one armed with
        // AnyModifier fires for any state at all.
        boolean shift = java.util.Arrays.asList(args).contains("shift");
        System.out.printf("%s grab: ownerEvents=%s modifiers=0x%x detail=%d eventMask=0x%x%n",
                keyboard ? "key" : "button", ownerEvents, modifiers, detail, eventMask);

        try (Wire graber = Wire.connect(host, port); Wire other = Wire.connect(host, port)) {
            int xtest = queryExtension(graber, "XTEST");
            armed = graber.root();
            cover = other.allocateId();
            // The grab is armed on the **root** and the window the pointer will be over belongs to the
            // other client. That is the only arrangement in which "the grab took the press" can be
            // told from "the press went where it landed", and it is not an artificial one:
            // `GrabButton` on the root is how a client takes a global shortcut. A grab on a window of
            // the grabbing client's own cannot show it - a press inside that window's subtree belongs
            // to that client either way - and a covering window from another client cannot be made a
            // child of it (measured: the reference refuses the `CreateWindow`, `BadMatch`).
            createWindow(other, cover, other.root(), 100, 100, 200, 200);
            selectInput(other, cover, BUTTON_PRESS | BUTTON_RELEASE | ENTER_WINDOW | LEAVE_WINDOW
                    | KEY_PRESS | KEY_RELEASE);
            map(other, cover);

            System.out.printf("windows: the grab goes on the root 0x%x (grabbing client), the window "
                    + "the pointer will be over is 0x%x (other client)%n", armed, cover);

            System.out.println("step: park the pointer outside");
            fakeMotion(graber, xtest, 1000, 700);
            drain(graber, other);

            if (keyboard) {
                // A key grab fires for the *focus* window's chain, and the focus is the other client's
                // window: without the grab the key belongs to that client, which is what makes this
                // measurement discriminating for keys as well.
                setInputFocus(other, cover, 2);
            }
            System.out.printf("step: arm the %s grab on the root%n", keyboard ? "key" : "button");
            if (keyboard) {
                grabKey(graber, armed, ownerEvents, modifiers, detail, eventMask);
            } else {
                grabButton(graber, armed, ownerEvents, modifiers, detail, eventMask);
            }
            drain(graber, other);

            System.out.println("step: move onto the other client's window");
            fakeMotion(graber, xtest, 200, 200);
            drain(graber, other);

            System.out.println((keyboard ? "step: type the key" : "step: press the button")
                    + (shift ? " with Shift held" : ""));
            if (shift) {
                fakeKey(graber, xtest, SHIFT_L, true);
            }
            if (keyboard) {
                fakeKey(graber, xtest, detail == 0 ? 38 : detail, true);
                fakeKey(graber, xtest, detail == 0 ? 38 : detail, false);
            } else {
                fakeButton(graber, xtest, detail == 0 ? 1 : detail, true);
                fakeButton(graber, xtest, detail == 0 ? 1 : detail, false);
            }
            if (shift) {
                fakeKey(graber, xtest, SHIFT_L, false);
            }
            drain(graber, other);

            System.out.println("step: move away again");
            fakeMotion(graber, xtest, 1000, 700);
            drain(graber, other);

            System.out.println("step: ungrab");
            if (keyboard) {
                ungrabKey(graber, armed, modifiers, detail);
            } else {
                ungrabButton(graber, armed, modifiers, detail);
            }
            drain(graber, other);
            System.out.println("done");
        }
    }

    /** Both connections, so a press that went to the cover client is not mistaken for one that did not. */
    private static void drain(Wire graber, Wire other) throws Exception {
        drainFrom(graber, "grabbing client");
        drainFrom(other, "other client");
    }

    private static void drainFrom(Wire wire, String label) throws Exception {
        wire.readTimeoutMillis(400);
        for (byte[] message = wire.readMessage(); message != null; message = wire.readMessage()) {
            describe(message, label);
        }
        wire.readTimeoutMillis(3000);
    }

    private static void describe(byte[] message, String label) {
        int type = message[0] & 0x7F;
        if (type == 1) {
            return;
        }
        if (type == 0) {
            System.out.printf("  [%s] ERROR %d opcode=%d%n", label, message[1] & 0xFF,
                    message[10] & 0xFF);
            return;
        }
        String name = type < TYPES.length ? TYPES[type] : "type" + type;
        // A key or button event has its *time* at offset 4 and the window it is reported against at
        // 12; reading the window from 4 prints a timestamp, and a timestamp looks enough like a
        // window id that the mistake survives a reading of the output.
        int window = Wire.readInt(message, 12);
        switch (type) {
            case 2, 3 -> System.out.printf("  [%s] %s win=%s keycode=%d state=0x%x child=%s%n", label,
                    name, name(window), message[1] & 0xFF, Wire.readShort(message, 28),
                    name(Wire.readInt(message, 16)));
            case 4, 5 -> System.out.printf("  [%s] %s win=%s button=%d state=0x%x child=%s%n", label,
                    name, name(window), message[1] & 0xFF, Wire.readShort(message, 28),
                    name(Wire.readInt(message, 16)));
            case 7, 8 -> System.out.printf("  [%s] %s win=%s detail=%s mode=%s at=%d,%d%n", label, name,
                    name(Wire.readInt(message, 12)),
                    message[1] < DETAILS.length ? DETAILS[message[1]] : "detail" + (message[1] & 0xFF),
                    mode(message[30] & 0xFF), (short) Wire.readShort(message, 20),
                    (short) Wire.readShort(message, 22));
            default -> System.out.printf("  [%s] %s win=%s%n", label, name, name(window));
        }
    }

    private static String mode(int value) {
        return value < MODES.length ? MODES[value] : "mode" + value;
    }

    private static String name(int window) {
        if (window == armed) {
            return "armed";
        }
        if (window == cover) {
            return "cover";
        }
        return window == 0 ? "None" : "0x" + Integer.toHexString(window);
    }

    // --- requests ---

    private static void createWindow(Wire wire, int window, int parent, int x, int y, int width,
                                     int height) throws Exception {
        byte[] request = new byte[36];
        request[0] = 1;                             // CreateWindow
        Wire.writeShort(request, 2, 9);
        Wire.writeInt(request, 4, window);
        Wire.writeInt(request, 8, parent);
        Wire.writeShort(request, 12, x);
        Wire.writeShort(request, 14, y);
        Wire.writeShort(request, 16, width);
        Wire.writeShort(request, 18, height);
        Wire.writeShort(request, 22, 1);            // InputOutput
        Wire.writeInt(request, 28, 0x2);            // CWBackPixel
        Wire.writeInt(request, 32, 0xFFFFFF);
        wire.send(request);
    }

    private static void map(Wire wire, int window) throws Exception {
        byte[] request = new byte[8];
        request[0] = 8;                             // MapWindow
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, window);
        wire.send(request);
    }

    private static void stackOnTop(Wire wire, int window) throws Exception {
        byte[] request = new byte[16];
        request[0] = 12;                            // ConfigureWindow
        Wire.writeShort(request, 2, 4);
        Wire.writeInt(request, 4, window);
        Wire.writeInt(request, 8, 0x40);            // CWStackMode
        Wire.writeInt(request, 12, 0);              // Above, relative to nothing: the top
        wire.send(request);
    }

    private static void setInputFocus(Wire wire, int window, int revertTo) throws Exception {
        byte[] request = new byte[12];
        request[0] = 42;                            // SetInputFocus
        request[1] = (byte) revertTo;
        Wire.writeShort(request, 2, 3);
        Wire.writeInt(request, 4, window);
        Wire.writeInt(request, 8, 0);               // CurrentTime
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

    private static void grabButton(Wire wire, int window, boolean ownerEvents, int modifiers,
                                   int button, int eventMask) throws Exception {
        byte[] request = new byte[24];
        request[0] = 28;                            // GrabButton
        request[1] = (byte) (ownerEvents ? 1 : 0);
        Wire.writeShort(request, 2, 6);
        Wire.writeInt(request, 4, window);
        Wire.writeShort(request, 8, eventMask);
        request[10] = 1;                            // pointer mode: Async
        request[11] = 1;                            // keyboard mode: Async
        Wire.writeInt(request, 12, 0);              // confine to: none
        Wire.writeInt(request, 16, 0);              // cursor: none
        request[20] = (byte) button;
        request[22] = (byte) modifiers;
        request[23] = (byte) (modifiers >> 8);
        wire.send(request);
    }

    private static void ungrabButton(Wire wire, int window, int modifiers, int button)
            throws Exception {
        // button(1), length(2), window(4), modifiers(2), pad(2): the button is the byte right after
        // the opcode, and writing it where the window goes loses the window entirely.
        byte[] request = new byte[12];
        request[0] = 29;                            // UngrabButton
        request[1] = (byte) button;
        Wire.writeShort(request, 2, 3);
        Wire.writeInt(request, 4, window);
        Wire.writeShort(request, 8, modifiers);
        wire.send(request);
    }

    private static void grabKey(Wire wire, int window, boolean ownerEvents, int modifiers, int key,
                                int eventMask) throws Exception {
        byte[] request = new byte[16];
        request[0] = 33;                            // GrabKey
        request[1] = (byte) (ownerEvents ? 1 : 0);
        Wire.writeShort(request, 2, 4);
        Wire.writeInt(request, 4, window);
        Wire.writeShort(request, 8, modifiers);
        request[10] = (byte) key;
        request[11] = 1;                            // pointer mode: Async
        request[12] = 1;                            // keyboard mode: Async
        wire.send(request);
    }

    private static void ungrabKey(Wire wire, int window, int modifiers, int key) throws Exception {
        byte[] request = new byte[12];
        request[0] = 34;                            // UngrabKey
        request[1] = (byte) key;
        Wire.writeShort(request, 2, 3);
        Wire.writeInt(request, 4, window);
        Wire.writeShort(request, 8, modifiers);
        wire.send(request);
    }

    private static void fakeMotion(Wire wire, int xtest, int x, int y) throws Exception {
        fakeInput(wire, xtest, 6, 0, x, y);
    }

    private static void fakeButton(Wire wire, int xtest, int button, boolean pressed)
            throws Exception {
        fakeInput(wire, xtest, pressed ? 4 : 5, button, 0, 0);
    }

    private static void fakeKey(Wire wire, int xtest, int keycode, boolean pressed)
            throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) xtest;
        request[1] = 2;                             // X_XTestFakeInput
        Wire.writeShort(request, 2, 9);
        request[4] = (byte) (pressed ? 2 : 3);
        request[5] = (byte) keycode;
        wire.send(request);
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
            if ((message[0] & 0x7F) == 1) {
                return message[8] == 0 ? 0 : message[9] & 0xFF;
            }
        }
    }

    private GrabProbe() {
    }
}
