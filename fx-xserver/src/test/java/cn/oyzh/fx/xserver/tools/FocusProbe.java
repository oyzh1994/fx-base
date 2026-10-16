package cn.oyzh.fx.xserver.tools;

import java.util.Locale;

/**
 * Drives a window tree through a sequence of focus changes and prints every focus and crossing event
 * that comes back, so two servers can be compared event for event.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1) - the probe the design notes call
 * {@code FocusProbe}, and the harness backlog items A5 and A6 are verified with. The rules for what a
 * client is told about the focus were taken from a reference server with it, not from the
 * specification's prose: which windows are told, in what order, with which detail, and - the thing
 * that is easy to get wrong - with which <em>mode</em> when a keyboard grab is in the way.
 *
 * <pre>
 *   FocusProbe &lt;host&gt; &lt;port&gt;                       # the default sequence
 *   FocusProbe &lt;host&gt; &lt;port&gt; "focus A" "warp 50 50" "focus pointer" "grabkey A" "focus C"
 * </pre>
 *
 * <p>The window tree is fixed and small - {@code A} at 0,0 300x300, {@code B} inside it at 10,10
 * 100x100, {@code C} at 400,0 300x300 - and the steps are:
 *
 * <pre>
 *   focus A|B|C|root|pointer|none   SetInputFocus, RevertToParent
 *   warp X Y                        move the pointer, through XTEST
 *   grabkey A|B|C [owner]           GrabKeyboard on that window
 *   ungrabkey                       UngrabKeyboard
 *   grabptr A|B|C [owner]           GrabPointer on that window, Async both ways
 *   ungrabptr                       UngrabPointer
 *   kill A|B|C                      DestroyWindow, for the focus revert rules
 *   unmap A|B|C                     UnmapWindow, the other way out of being viewable
 *   key KEYCODE                     one key press and release, through XTEST
 *   wait MS                         let the display tick run
 * </pre>
 *
 * <p>Motion is deliberately not selected: a probe whose output is half motion events is one nobody
 * diffs by eye, and the focus question is entirely about the crossings.
 *
 * <p><b>Reading the output.</b> Window ids differ between runs and between servers, so events name
 * the window as {@code A}, {@code B}, {@code C} or {@code R} (the root) instead of by id. Everything
 * else - the event's type, its {@code detail} and its {@code mode} - is printed as the server worded
 * it, because that is what is being compared. Each step is followed by a {@code GetInputFocus}, which
 * is only there to be a reply: the events a step caused are the ones printed before it, and the
 * sequencing makes that exact rather than a sleep. Its answer is printed too, as
 * {@code focus = A|B|C|None|PointerRoot} - what the server *says* the focus is, which is not always
 * what a step asked for and is invisible in the events alone.
 *
 * <p><b>Both the pointer and the focus outlive the connection</b>, so the probe parks the pointer
 * outside every window and sets the focus to {@code None} before its first step. Without that, the
 * starting state is whatever the last connection left, and the same request prints different events
 * on two runs - which is exactly how "the reference sends a pair here" and "the reference sends
 * nothing" were both true of the same request, measured an hour apart.
 */
public final class FocusProbe {

    private static final int FOCUS_CHANGE = 0x200000;
    private static final int ENTER_WINDOW = 0x10;
    private static final int LEAVE_WINDOW = 0x20;

    /** Where the pointer is parked before anything is mapped: outside every window the probe makes. */
    private static final int PARK_X = 1000;
    private static final int PARK_Y = 600;

    /**
     * The three windows, kept where {@link #describe} can name them.
     *
     * <p>They are fields rather than parameters because a reply reader has to be able to print the
     * events it passes over - see {@link #readReply}.
     */
    private static int a;
    private static int b;
    private static int c;

    /** The root window, so the probe can name a focus that is the root rather than PointerRoot. */
    private static int root;

    private static final String[] FOCUS_DETAILS = {"Ancestor", "Virtual", "Inferior", "Nonlinear",
            "NonlinearVirtual", "Pointer", "PointerRoot", "None"};
    private static final String[] CROSSING_MODES = {"NotifyNormal", "NotifyGrab", "NotifyUngrab",
            "NotifyWhileGrabbed"};
    private static final String[] CROSSING_DETAILS = {"Ancestor", "Virtual", "Inferior", "Nonlinear",
            "NonlinearVirtual"};

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: FocusProbe <host> <port> [step ...]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String[] steps = args.length > 2
                ? java.util.Arrays.copyOfRange(args, 2, args.length)
                : defaultSteps();

        try (Wire wire = Wire.connect(host, port)) {
            a = wire.allocateId();
            b = wire.allocateId();
            c = wire.allocateId();
            root = wire.root();
            int xtest = queryExtension(wire, "XTEST");
            // Park the pointer before the windows exist, and somewhere none of them reaches: the
            // pointer outlives every client, so a probe that starts wherever the last run left it
            // gets a crossing at the map on one run and not on the next - and a diff between two
            // servers then shows the *previous* run's difference.
            fakeInput(wire, xtest, 6, 0, PARK_X, PARK_Y);
            createWindow(wire, a, wire.root(), 0, 0, 300, 300);
            createWindow(wire, b, a, 10, 10, 100, 100);
            createWindow(wire, c, wire.root(), 400, 0, 300, 300);
            for (int window : new int[] {a, b, c}) {
                selectInput(wire, window, FOCUS_CHANGE | ENTER_WINDOW | LEAVE_WINDOW);
                map(wire, window);
            }
            // The focus outlives the client just as the pointer does, so the starting state is
            // whatever the last connection left - and every request below is measured *from* it.
            // Two runs that differ only in where the previous one stopped therefore print different
            // events, which is how "the reference reports a pair here" and "it reports nothing" were
            // both true of the same request on different runs. Start from None, every time.
            setInputFocus(wire, 0, 2);
            sync(wire);

            for (String step : steps) {
                System.out.println("step: " + step);
                run(wire, xtest, step, a, b, c);
            }
            System.out.println("done");
        }
    }

    private static String[] defaultSteps() {
        return new String[] {
                "warp 5 5",                         // into A
                "focus A",
                "warp 50 50",                       // into B
                "focus B",
                "warp 500 150",                     // into C
                "focus C",
                "focus pointer",                    // the focus is the window the pointer is in
                "warp 50 50",                       // still inside B: the focus moves with it
                "focus none",
                "focus A",
                "grabkey A",                        // a keyboard grab, and the focus it implies
                "focus C",                          // a focus change asked for inside the grab
                "ungrabkey",
                "focus C",
                "grabkey C",
                "ungrabkey",
                "focus root",
                "focus none",
        };
    }

    private static void run(Wire wire, int xtest, String step, int a, int b, int c)
            throws Exception {
        String[] parts = step.trim().split("\\s+");
        switch (parts[0].toLowerCase(Locale.ROOT)) {
            case "focus" -> {
                int focus = switch (parts[1].toLowerCase(Locale.ROOT)) {
                    case "a" -> a;
                    case "b" -> b;
                    case "c" -> c;
                    case "root" -> wire.root();
                    case "pointer" -> 1;            // PointerRoot
                    case "none" -> 0;
                    default -> throw new IllegalArgumentException(parts[1]);
                };
                setInputFocus(wire, focus, 2);      // RevertToParent
            }
            case "warp" -> fakeInput(wire, xtest, 6, 0, Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]));
            case "grabkey" -> grabKeyboard(wire, windowOf(parts[1], a, b, c), parts.length > 2
                    && parts[2].equals("owner"));
            case "ungrabkey" -> ungrabKeyboard(wire);
            case "grabptr" -> grabPointer(wire, windowOf(parts[1], a, b, c), parts.length > 2
                    && parts[2].equals("owner"));
            case "ungrabptr" -> ungrabPointer(wire);
            case "key" -> {
                int keycode = Integer.parseInt(parts[1]);
                fakeInput(wire, xtest, 2, keycode, 0, 0);
                fakeInput(wire, xtest, 3, keycode, 0, 0);
            }
            case "kill" -> destroyWindow(wire, windowOf(parts[1], a, b, c));
            case "unmap" -> unmapWindow(wire, windowOf(parts[1], a, b, c));
            case "wait" -> {
                Thread.sleep(Long.parseLong(parts[1]));
                // Nothing to sync against: the wait is for the tick, not for a request.
            }
            default -> throw new IllegalArgumentException(step);
        }
        sync(wire);
        // A step that only slept has already drained what the tick produced; a step that sent
        // requests has its events printed above the sync's.
        wire.readTimeoutMillis(150);
        for (byte[] message = wire.readMessage(); message != null; message = wire.readMessage()) {
            describe(message, a, b, c);
        }
        wire.readTimeoutMillis(3000);
    }

    /**
     * Sends a {@code GetInputFocus} and prints every message until its reply arrives.
     *
     * <p>The reply is the point: requests are answered in order, so once it is back, everything the
     * step before it caused has been read - a sleep long enough for the slowest server would be both
     * slower and less certain.
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
                // What the server *says* the focus is, which is not always what the steps asked for:
                // a state machine that quietly keeps something else is invisible otherwise, and the
                // events alone cannot distinguish it from a server that answered wrongly.
                System.out.println("  focus = " + name(Wire.readInt(message, 8), root, a, b, c));
                return;
            }
            describe(message, a, b, c);
        }
    }

    /** One line per message, with the window as a letter and the fields a comparison cares about. */
    private static void describe(byte[] message, int a, int b, int c) {
        int type = type(message);
        switch (type) {
            // FocusIn/FocusOut: detail at 1, the event window at 4, the mode at 8. A crossing is
            // laid out differently - its event window is at 12 and its mode at 30 - and the last
            // byte packs same-screen into bit 0 and the focus bit into bit 1.
            case 9, 10 -> System.out.printf("  %s win=%s detail=%s mode=%s%n",
                    type == 9 ? "FocusIn" : "FocusOut", name(Wire.readInt(message, 4), root, a, b, c),
                    detail(FOCUS_DETAILS, message[1] & 0xFF), mode(message[8] & 0xFF));
            // The last byte of a crossing packs same-screen and the focus bit, and which is which
            // is easy to get backwards: printing the byte rather than a reading of it is what settled
            // this against Xlib, which disagreed with the first reading this probe made.
            case 7, 8 -> System.out.printf("  %s win=%s detail=%s mode=%s last=0x%02x%n",
                    type == 7 ? "EnterNotify" : "LeaveNotify", name(Wire.readInt(message, 12), root, a, b, c),
                    detail(CROSSING_DETAILS, message[1] & 0xFF), mode(message[30] & 0xFF),
                    message[31] & 0xFF);
            case 0 -> System.out.printf("  ERROR %d opcode=%d resource=0x%x%n", message[1] & 0xFF,
                    message[10] & 0xFF, Wire.readInt(message, 4));
            default -> System.out.println("  other event type " + type);
        }
    }

    private static String detail(String[] names, int value) {
        return value < names.length ? names[value] : "detail" + value;
    }

    /** The mode word is not the same numbering for focus events as for crossings. */
    private static String mode(int value) {
        return value < CROSSING_MODES.length ? CROSSING_MODES[value] : "mode" + value;
    }

    private static String name(int window, int root, int a, int b, int c) {
        if (window == a) {
            return "A";
        }
        if (window == b) {
            return "B";
        }
        if (window == c) {
            return "C";
        }
        if (window == root) {
            // The root window's id, which is a *different* state from PointerRoot: GetInputFocus
            // answers PointerRoot for one and this id for the other, and a reference server answers
            // the id here where this one used to answer PointerRoot. Both are named, so a diff shows
            // which. Where a *key* goes is not one of the differences - see `KeyTargetProbe`, which
            // is the probe that asks that question.
            return "R";
        }
        // A focus of 1 is PointerRoot, and 0 is None - the two states that are not windows. Their
        // names matter: a server that answers the root window's id where another answers PointerRoot
        // is keeping two different states that behave differently, and only the name shows it.
        if (window == 0) {
            return "None";
        }
        return window == 1 ? "PointerRoot" : "0x" + Integer.toHexString(window);
    }

    private static int type(byte[] message) {
        return message[0] & 0x7F;
    }

    private static int windowOf(String name, int a, int b, int c) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "a" -> a;
            case "b" -> b;
            case "c" -> c;
            default -> throw new IllegalArgumentException(name);
        };
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

    private static void selectInput(Wire wire, int window, int mask) throws Exception {
        byte[] request = new byte[16];
        request[0] = 2;                             // ChangeWindowAttributes
        Wire.writeShort(request, 2, 4);
        Wire.writeInt(request, 4, window);
        Wire.writeInt(request, 8, 0x800);           // CWEventMask
        Wire.writeInt(request, 12, mask);
        wire.send(request);
    }

    /**
     * Destroys one of the probe's windows, which is the case the focus revert rules exist for: the
     * window holding the focus goes away, and where the focus lands depends on what the last
     * {@code SetInputFocus} asked for. No reply is read here - the step's events are printed by the
     * {@code GetInputFocus} that follows every step.
     */
    /** Unmaps one of the probe's windows: the other way a window stops being viewable. */
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

    private static void setInputFocus(Wire wire, int focus, int revertTo) throws Exception {
        byte[] request = new byte[12];
        request[0] = 42;                            // SetInputFocus
        request[1] = (byte) revertTo;
        Wire.writeShort(request, 2, 3);
        Wire.writeInt(request, 4, focus);
        Wire.writeInt(request, 8, 0);               // CurrentTime
        wire.send(request);
    }

    private static void grabKeyboard(Wire wire, int window, boolean ownerEvents) throws Exception {
        byte[] request = new byte[16];
        request[0] = 31;                            // GrabKeyboard - 32 is UngrabKeyboard
        request[1] = (byte) (ownerEvents ? 1 : 0);
        Wire.writeShort(request, 2, 4);
        Wire.writeInt(request, 4, window);
        Wire.writeInt(request, 8, 0);               // CurrentTime
        request[12] = 1;                            // pointer mode: Async
        request[13] = 1;                            // keyboard mode: Async
        wire.send(request);
        readReply(wire);
    }

    private static void ungrabKeyboard(Wire wire) throws Exception {
        byte[] request = new byte[8];
        request[0] = 32;                            // UngrabKeyboard
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, 0);               // CurrentTime
        wire.send(request);
    }

    private static void grabPointer(Wire wire, int window, boolean ownerEvents) throws Exception {
        byte[] request = new byte[24];
        request[0] = 26;                            // GrabPointer
        request[1] = (byte) (ownerEvents ? 1 : 0);
        Wire.writeShort(request, 2, 6);
        Wire.writeInt(request, 4, window);
        Wire.writeShort(request, 8, 0x8000);        // every event the pointer can report
        request[10] = 1;                            // Async
        request[11] = 1;                            // Async
        wire.send(request);
        readReply(wire);
    }

    private static void ungrabPointer(Wire wire) throws Exception {
        byte[] request = new byte[8];
        request[0] = 27;                            // UngrabPointer
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, 0);               // CurrentTime
        wire.send(request);
    }

    /**
     * Consumes the reply a {@code GrabPointer}/{@code GrabKeyboard} answers with.
     *
     * <p>Both of those requests reply with a status, and a reply left unread is the whole stream out
     * of step: the next sync reads it as its own and returns before any event has been printed, which
     * shows up as events arriving one step late and an error on a request that was perfectly well
     * formed.
     */
    private static void readReply(Wire wire) throws Exception {
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                throw new IllegalStateException("no answer to the grab");
            }
            if (type(message) == 1) {
                return;
            }
            // Printed, not skipped: a reference server reports the focus the grab *takes* as well as
            // the one it gives back, and a reader that swallows what it passes over hides exactly the
            // events a grab test is looking for. The first version of this did that, and the
            // measurement it produced was wrong in a way nothing else would have shown.
            describe(message, a, b, c);
        }
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

    private FocusProbe() {
    }
}
