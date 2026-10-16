package cn.oyzh.fx.xserver.tools;

/**
 * Opens a popup the way a menu does, then moves the pointer out of it and back in, printing every
 * crossing and flagging the one shape of event that costs a menu.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1), and the harness for the trap the
 * design notes record under "Grabs and crossing events": <b>a crossing that reports a window
 * "entered" without the pointer having moved is what tears EasyShell's menus down</b>, whether it is
 * sent at the map, at the release of the button, or at the end of a grab. That was measured by
 * accident twice, and a measurement that needs a real client, a real mouse and three runs is one that
 * gets re-derived rather than repeated - so this makes the same gesture scriptable.
 *
 * <pre>
 *   MenuProbe &lt;host&gt; &lt;port&gt;
 * </pre>
 *
 * <p>The gesture is the ordinary one: a main window is mapped and gets the pointer and a press, a
 * popup appears under the pointer while the button is still down, the popup grabs the pointer the way
 * a toolkit's menu does, the button comes up, and then the pointer leaves the popup and comes back in.
 * Every crossing is printed. An {@code EnterNotify} that arrives in a step where <b>no motion was
 * sent</b> is marked {@code ! enter without a move}, and if the button is down when one does, the
 * probe prints {@code ! would dismiss} - the rule EasyShell was measured applying (its log shows
 * {@code UngrabPointer} and an unmap one millisecond after such an enter, with no request in
 * between). The marker is about *when* the event arrived, not about its coordinates: a crossing that
 * a move produces always names the pointer's current place, so coordinates cannot tell the two
 * apart - only the fact that no move was asked for can.
 *
 * <p>This probe never actually dismisses: it is a client, and it prints what it was told. Run it
 * against Xvfb and against this server and diff the two outputs - they are the measurement.
 */
public final class MenuProbe {

    private static final int BUTTON_PRESS = 0x4;
    private static final int BUTTON_RELEASE = 0x8;
    private static final int ENTER_WINDOW = 0x10;
    private static final int LEAVE_WINDOW = 0x20;
    private static final int POINTER_MOTION = 0x40;
    private static final int FOCUS_CHANGE = 0x200000;

    /** Where the pointer is parked before anything is mapped: outside every window the probe makes. */
    private static final int PARK_X = 1000;
    private static final int PARK_Y = 600;

    private static final String[] DETAILS = {"Ancestor", "Virtual", "Inferior", "Nonlinear",
            "NonlinearVirtual"};
    private static final String[] MODES = {"NotifyNormal", "NotifyGrab", "NotifyUngrab",
            "NotifyWhileGrabbed"};

    /**
     * True while the events being read are the ones a motion the probe just sent produced, and false
     * for every other step - which is the whole of what makes "without a move" measurable.
     */
    private static boolean afterMotion;
    private static int lastX = Integer.MIN_VALUE;
    private static int lastY = Integer.MIN_VALUE;
    private static boolean buttonDown;
    private static int main;
    private static int menu;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: MenuProbe <host> <port>");
            return;
        }
        try (Wire wire = Wire.connect(args[0], Integer.parseInt(args[1]))) {
            int xtest = queryExtension(wire, "XTEST");
            if (xtest == 0) {
                throw new IllegalStateException("the server has no XTEST");
            }
            main = wire.allocateId();
            menu = wire.allocateId();
            // The pointer outlives the client, so park it where no window of ours reaches before
            // anything is mapped - otherwise a run starts with whatever the last one left, and the
            // first crossing is a difference between runs rather than between servers.
            fakeMotion(wire, xtest, PARK_X, PARK_Y);
            // And so does the focus, which this probe selects for: the crossings it prints carry the
            // focus bit, and that bit is set from *this* connection's view of the focus, so a run
            // against a server whose last connection left the focus somewhere else prints a
            // different answer for the same gesture. Parked to None every time, for the same reason
            // the pointer is parked.
            parkFocus(wire);

            // The application's own window, and the pointer parked inside it.
            createWindow(wire, main, wire.root(), 100, 100, 400, 300, false);
            selectInput(wire, main, BUTTON_PRESS | BUTTON_RELEASE | ENTER_WINDOW | LEAVE_WINDOW
                    | POINTER_MOTION | FOCUS_CHANGE);
            map(wire, main);
            System.out.println("step: warp into main");
            fakeMotion(wire, xtest, 200, 200);
            drain(wire, true);

            System.out.println("step: press button 1");
            buttonDown = true;
            fakeButton(wire, xtest, 1, true);
            drain(wire, false);

            // The popup appears under the pointer while the button is still down - the case the
            // design notes say a popup is not "entered" by until the button comes up.
            System.out.println("step: map the menu under the held button");
            createWindow(wire, menu, wire.root(), 220, 220, 150, 120, true);
            selectInput(wire, menu, BUTTON_PRESS | BUTTON_RELEASE | ENTER_WINDOW | LEAVE_WINDOW
                    | POINTER_MOTION | FOCUS_CHANGE);
            map(wire, menu);
            drain(wire, false);

            System.out.println("step: the menu grabs the pointer");
            grabPointer(wire, menu, true);
            drain(wire, false);

            System.out.println("step: release button 1");
            buttonDown = false;
            fakeButton(wire, xtest, 1, false);
            drain(wire, false);

            System.out.println("step: out of the menu");
            fakeMotion(wire, xtest, 400, 380);
            drain(wire, true);

            System.out.println("step: back into the menu");
            fakeMotion(wire, xtest, 260, 260);
            drain(wire, true);

            System.out.println("step: out and in again");
            fakeMotion(wire, xtest, 400, 380);
            drain(wire, true);
            fakeMotion(wire, xtest, 260, 260);
            drain(wire, true);

            System.out.println("step: ungrab and leave");
            ungrabPointer(wire);
            drain(wire, false);
            System.out.println("done");
        }
    }

    /**
     * Reads until the server is quiet, printing every message.
     *
     * @param moved true only when a motion was just sent, so an enter reported in any other step is
     *     one the server produced on its own
     */
    private static void drain(Wire wire, boolean moved) throws Exception {
        afterMotion = moved;
        wire.readTimeoutMillis(300);
        for (byte[] message = wire.readMessage(); message != null; message = wire.readMessage()) {
            describe(message);
        }
        afterMotion = false;
        wire.readTimeoutMillis(3000);
    }

    private static void describe(byte[] message) {
        int type = message[0] & 0x7F;
        int x = Wire.readShort(message, 20);
        int y = Wire.readShort(message, 22);
        switch (type) {
            case 7, 8 -> {
                boolean enter = type == 7;
                int detail = message[1] & 0xFF;
                int mode = message[30] & 0xFF;
                int window = Wire.readInt(message, 12);
                boolean spontaneous = enter && !afterMotion;
                System.out.printf("  %s win=%s detail=%s mode=%s at=%d,%d focus=%s%s%n",
                        enter ? "EnterNotify" : "LeaveNotify", name(window),
                        detail < DETAILS.length ? DETAILS[detail] : "detail" + detail,
                        mode < MODES.length ? MODES[mode] : "mode" + mode, (short) x, (short) y,
                        // The last byte packs the two: the focus bit is bit 0 and same_screen bit 1,
                        // which is the opposite of the order their names suggest and was measured
                        // with Xlib (tools/README.md). Reading the wrong one prints a plausible
                        // field with the wrong value, which is how it stayed wrong.
                        (message[31] & 0x01) != 0 ? "yes" : "no",
                        spontaneous ? "   ! enter without a move" : "");
                if (spontaneous && buttonDown) {
                    System.out.println("    ! would dismiss");
                }
                lastX = (short) x;
                lastY = (short) y;
            }
            case 4 -> {
                System.out.printf("  ButtonPress button=%d at=%d,%d win=%s%n", message[1] & 0xFF,
                        (short) x, (short) y, name(Wire.readInt(message, 12)));
                buttonDown = true;
            }
            case 5 -> {
                System.out.printf("  ButtonRelease button=%d at=%d,%d win=%s%n", message[1] & 0xFF,
                        (short) x, (short) y, name(Wire.readInt(message, 12)));
                buttonDown = false;
            }
            case 6 -> {
                lastX = (short) x;
                lastY = (short) y;
                // Motion on its own is not what is being compared; a run of it would bury the
                // crossings.
            }
            case 9, 10 -> System.out.printf("  %s win=%s detail=%s mode=%s%n",
                    type == 9 ? "FocusIn" : "FocusOut", name(Wire.readInt(message, 4)),
                    message[1] & 0xFF, message[8] & 0xFF);
            case 0 -> System.out.printf("  ERROR %d opcode=%d%n", message[1] & 0xFF, message[10] & 0xFF);
            default -> System.out.println("  other event type " + type);
        }
    }

    private static String name(int window) {
        if (window == main) {
            return "main";
        }
        if (window == menu) {
            return "menu";
        }
        return window == 0 ? "None" : "0x" + Integer.toHexString(window);
    }

    // --- requests ---

    private static void createWindow(Wire wire, int window, int parent, int x, int y, int width,
                                     int height, boolean overrideRedirect) throws Exception {
        byte[] request = new byte[overrideRedirect ? 36 : 32];
        request[0] = 1;                             // CreateWindow
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeInt(request, 4, window);
        Wire.writeInt(request, 8, parent);
        Wire.writeShort(request, 12, x);
        Wire.writeShort(request, 14, y);
        Wire.writeShort(request, 16, width);
        Wire.writeShort(request, 18, height);
        Wire.writeShort(request, 22, 1);            // InputOutput
        Wire.writeInt(request, 28, overrideRedirect ? 0x200 : 0);
        if (overrideRedirect) {
            Wire.writeInt(request, 32, 1);          // the value that goes with the mask
        }
        wire.send(request);
    }

    private static void map(Wire wire, int window) throws Exception {
        byte[] request = new byte[8];
        request[0] = 8;                             // MapWindow
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

    private static void grabPointer(Wire wire, int window, boolean ownerEvents) throws Exception {
        byte[] request = new byte[24];
        request[0] = 26;                            // GrabPointer
        request[1] = (byte) (ownerEvents ? 1 : 0);
        Wire.writeShort(request, 2, 6);
        Wire.writeInt(request, 4, window);
        Wire.writeShort(request, 8, 0x8000);
        request[10] = 1;                            // Async
        request[11] = 1;                            // Async
        wire.send(request);
        // GrabPointer replies with a status; leaving it unread puts the stream out of step and the
        // crossings then look like they arrive a step late.
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null || (message[0] & 0x7F) == 1) {
                return;
            }
        }
    }

    private static void ungrabPointer(Wire wire) throws Exception {
        byte[] request = new byte[8];
        request[0] = 27;                            // UngrabPointer
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, 0);
        wire.send(request);
    }

    /**
     * Puts the focus nowhere, so the run starts from a state the probe chose rather than from
     * whatever the last connection left. {@code None}, with {@code RevertToPointerRoot}.
     */
    private static void parkFocus(Wire wire) throws Exception {
        byte[] request = new byte[12];
        request[0] = 42;                            // SetInputFocus
        request[1] = 1;                             // RevertToPointerRoot
        Wire.writeShort(request, 2, 3);
        Wire.writeInt(request, 4, 0);               // None
        Wire.writeInt(request, 8, 0);               // CurrentTime
        wire.send(request);
        // A reply is what says the server has done it, and the crossings that follow depend on it:
        // without the round trip the first step can be measured against the state before the park.
        byte[] sync = new byte[4];
        sync[0] = 43;                               // GetInputFocus
        sync[2] = 1;
        wire.send(sync);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null || (message[0] & 0x7F) == 1) {
                return;
            }
        }
    }

    private static void fakeMotion(Wire wire, int xtest, int x, int y) throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) xtest;
        request[1] = 2;                             // X_XTestFakeInput
        Wire.writeShort(request, 2, 9);
        request[4] = 6;                             // MotionNotify, absolute
        Wire.writeShort(request, 24, x);
        Wire.writeShort(request, 26, y);
        wire.send(request);
    }

    private static void fakeButton(Wire wire, int xtest, int button, boolean pressed)
            throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) xtest;
        request[1] = 2;
        Wire.writeShort(request, 2, 9);
        request[4] = (byte) (pressed ? 4 : 5);
        request[5] = (byte) button;
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

    private MenuProbe() {
    }
}
