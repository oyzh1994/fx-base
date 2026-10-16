package cn.oyzh.fx.xserver.tools;

import java.util.List;

/**
 * Measures what a grab with {@code pointer_mode = Sync} does to the events that follow, and what each
 * {@code AllowEvents} mode releases.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1), and the harness for item A13. A client
 * that grabs with the synchronous mode is asking the server to <b>stop the device</b> after the event
 * that triggered the grab, so it can decide what to do with it: a game that wants the mouse to stay
 * put while it re-centres the cursor, or a remote-desktop client that wants to re-deliver a click
 * after moving the pointer. The server therefore has to hold events back and let {@code AllowEvents}
 * release them - and this server delivered everything immediately, which makes every {@code Sync} and
 * {@code Replay} mode a no-op.
 *
 * <pre>
 *   AllowEventsProbe &lt;host&gt; &lt;port&gt; [active|passive] [sync|async|replay|all]
 * </pre>
 *
 * <p><b>Measured first, and the two arrangements differ.</b> An *active* grab taken with
 * {@code pointer_mode = Sync} does **not** freeze anything on a reference server - the motion that
 * follows arrives at once - so a probe that only tried that would conclude the modes were inert here
 * for the right reason. The freeze belongs to a **passive** grab that an event activates: the press
 * that fires it is delivered and the pointer is frozen from then on, which is the case a game or a
 * remote-desktop client is actually in.
 *
 * <p>What it prints, step by step: every motion and button event it receives, so the question "did the
 * events arrive now or later, and in what order" is read off the transcript rather than inferred.
 * The grab is taken with {@code owner_events = false} so every event is reported against the grab
 * window, and the pointer is moved in steps of a few pixels so a queued motion is easy to see.
 */
public final class AllowEventsProbe {

    private static final int MOTION_NOTIFY = 6;
    private static final int BUTTON_PRESS = 4;
    private static final int BUTTON_RELEASE = 5;

    /**
     * {@code GrabPointer}/{@code GrabButton}/{@code GrabKey} device modes, in X's own numbering:
     * {@code GrabModeSync} is <b>0</b> and {@code GrabModeAsync} is <b>1</b> (`X.h`), which is the
     * opposite way round from {@code AllowEvents}, where {@code SyncPointer} is 1.
     *
     * <p><b>The first version of this probe had these backwards</b>, and so measured an asynchronous
     * grab for a whole session while printing "pointer_mode = Sync": the reference "held nothing
     * back", which is precisely what an async grab does, and the item was closed on it. An async
     * grab never freezes anything, so that run said nothing about the modes at all.
     */
    private static final int GRAB_SYNC = 0;
    private static final int GRAB_ASYNC = 1;

    /** {@code AllowEvents} modes, numbered as the protocol numbers them. */
    private static final String[] ALLOW_MODES = {"AsyncPointer", "SyncPointer", "ReplayPointer",
            "AsyncKeyboard", "SyncKeyboard", "ReplayKeyboard", "AsyncBoth", "SyncBoth"};

    /**
     * A crossing's mode word is X's {@code NotifyMode}, which is a *different* numbering from
     * {@code AllowEvents}'s - and the two arrays start with a name in common, so reading one with the
     * other's table prints plausible nonsense ("SyncPointer" for what is really {@code NotifyGrab}).
     */
    private static final String[] NOTIFY_MODES = {"NotifyNormal", "NotifyGrab", "NotifyUngrab",
            "NotifyWhileGrabbed"};

    private static int window;
    private static int xtest;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: AllowEventsProbe <host> <port> [sync|async|replay|all]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        boolean passive = args.length > 2 && args[2].equals("passive");
        boolean keyCase = args.length > 2 && args[2].equals("key");
        // The key case is a method of its own and reads the flag for itself.
        boolean secondInjector = args.length > 4 && args[4].equals("injector");
        // XTEST is one way to move the pointer and a warp is another, and they take different paths
        // into the server. If a freeze were observable through one and not the other, the modes' state
        // would be real and only the injection that bypassed it; if neither shows it, the device is
        // not being frozen at all.
        boolean warpMoves = args.length > 6 && args[6].equals("warp");
        // "asyncgrab" asks for the grab that freezes nothing, so the two can be compared. Anything
        // else is the synchronous one, which is the mode this probe exists to measure.
        boolean asyncGrab = java.util.Arrays.asList(args).contains("asyncgrab");
        // "staletime" asks one AllowEvents with a time nothing can be: earlier than the grab. X's own
        // rule is that such a request is *ignored* - the device stays frozen - and a server without
        // that rule releases on it, which is the difference this arrangement exists to show.
        boolean staleTime = java.util.Arrays.asList(args).contains("staletime");
        int grabMode = asyncGrab ? GRAB_ASYNC : GRAB_SYNC;
        String grabName = asyncGrab ? "Async" : "Sync";
        String what = args.length > 3 ? args[3] : "sync";
        if (keyCase) {
            keyReplay(host, port, java.util.Arrays.asList(args));
            return;
        }

        try (Wire wire = Wire.connect(host, port);
             Wire injector = secondInjector ? Wire.connect(host, port) : wire) {
            xtest = secondInjector ? queryExtension(injector, "XTEST") : queryExtension(wire, "XTEST");
            Wire injection = injector;
            window = wire.allocateId();
            createWindow(wire, window, wire.root(), 100, 100, 300, 300);
            selectInput(wire, window, 0x4 | 0x8 | 0x40 | 0x10 | 0x20);   // button, motion, crossings
            map(wire, window);

            System.out.println("step: park the pointer outside");
            fakeMotion(injection, 1000, 700);
            drain(wire);

            System.out.println("step: move into the window");
            fakeMotion(injection, 200, 200);
            drain(wire);

            if (passive) {
                System.out.println("step: arm a passive button grab on the root, pointer_mode = "
                        + grabName);
                grabButton(wire, wire.root(), false, 0x4 | 0x8 | 0x40, grabMode, grabMode);
                drain(wire);
            } else {
                System.out.println("step: grab the pointer with pointer_mode = " + grabName);
                int status = grabPointer(wire, window, false, 0x4 | 0x8 | 0x40, grabMode, grabMode);
                System.out.println("  grab status = " + status);
                drain(wire);
            }

            System.out.println("step: press button 1 (the press that freezes the pointer)");
            fakeButton(injection, 1, true);
            drain(wire);

            System.out.println("step: motion while frozen" + (warpMoves ? " (by WarpPointer)" : ""));
            if (warpMoves) {
                warpPointer(wire, 0, 320, 320);
                warpPointer(wire, 0, 340, 340);
            } else {
                fakeMotion(injection, 220, 220);
                fakeMotion(injection, 240, 240);
            }
            drain(wire);

            System.out.println("step: release button 1 while frozen");
            fakeButton(injection, 1, false);
            drain(wire);

            if (staleTime) {
                System.out.println("step: AllowEvents(SyncPointer) with a time of 1 (outside the"
                        + " grab, so it releases nothing)");
                allowEvents(wire, 1, 1);
                drain(wire);
            }
            if (what.equals("sync") || what.equals("all")) {
                System.out.println("step: AllowEvents(SyncPointer)");
                allowEvents(wire, 1);
                drain(wire);
            }
            if (what.equals("async") || what.equals("all")) {
                System.out.println("step: AllowEvents(AsyncPointer)");
                allowEvents(wire, 0);
                drain(wire);
            }
            if (what.equals("replay") || what.equals("all")) {
                System.out.println("step: press again, then AllowEvents(ReplayPointer)");
                fakeButton(wire, 3, true);
                drain(wire);
                allowEvents(wire, 2);
                drain(wire);
                fakeButton(wire, 3, false);
                drain(wire);
            }

            System.out.println("step: motion after the releases");
            fakeMotion(wire, 260, 260);
            drain(wire);

            System.out.println("step: ungrab");
            ungrabPointer(wire);
            drain(wire);
            System.out.println("done");
        }
    }

    /**
     * The keyboard's version, and the one arrangement where a replay is unmistakable: the grab's
     * client calls {@code AllowEvents(ReplayKeyboard)} and the key must then be reprocessed *as if the
     * grab had not fired* - so it arrives at the window that has the focus, which is another client's.
     *
     * <p>Two connections are what make it measurable at all: with one client the key would reach it
     * whether the grab fired or not, and a server that ignores the request is indistinguishable from
     * one that honoured it (which is the same trap `GrabProbe` fell into for A1).
     */
    private static void keyReplay(String host, int port, List<String> args) throws Exception {
        boolean asyncGrab = args.contains("asyncgrab");
        int grabMode = asyncGrab ? GRAB_ASYNC : GRAB_SYNC;
        String grabName = asyncGrab ? "Async" : "Sync";
        String what = args.size() > 3 ? args.get(3) : "replay";
        try (Wire graber = Wire.connect(host, port); Wire focused = Wire.connect(host, port)) {
            xtest = queryExtension(graber, "XTEST");
            int focusedWindow = focused.allocateId();
            createWindow(focused, focusedWindow, focused.root(), 100, 100, 300, 300);
            selectInput(focused, focusedWindow, 0x1 | 0x2);            // KeyPress | KeyRelease
            map(focused, focusedWindow);
            selectInput(graber, graber.root(), 0x1 | 0x2);
            drain(graber);
            drain(focused);

            System.out.println("step: the other client takes the focus");
            setInputFocus(focused, focusedWindow, 2);
            drain(graber);
            drain(focused);

            System.out.println("step: arm a passive key grab on the root, keyboard_mode = "
                    + grabName);
            grabKey(graber, graber.root(), false, 0x8000, 0, grabMode, grabMode);
            drain(graber);
            drain(focused);

            System.out.println("step: type the key");
            fakeKey(graber, 38, true);
            fakeKey(graber, 38, false);
            System.out.println("  -- events the grabbing client has:");
            drain(graber);
            System.out.println("  -- events the focused client has:");
            drain(focused);

            System.out.println("step: the grabbing client calls AllowEvents(" + what + ")");
            allowEvents(graber, what.equals("replay") ? 5 : 4);        // ReplayKeyboard / SyncKeyboard
            System.out.println("  -- events the grabbing client has:");
            drain(graber);
            System.out.println("  -- events the focused client has:");
            drain(focused);

            System.out.println("step: type the key again");
            fakeKey(graber, 38, true);
            fakeKey(graber, 38, false);
            System.out.println("  -- events the grabbing client has:");
            drain(graber);
            System.out.println("  -- events the focused client has:");
            drain(focused);
            System.out.println("done");
        }
    }

    private static void grabKey(Wire wire, int window, boolean ownerEvents, int modifiers, int key,
                                int pointerMode, int keyboardMode) throws Exception {
        byte[] request = new byte[16];
        request[0] = 33;                            // GrabKey
        request[1] = (byte) (ownerEvents ? 1 : 0);
        Wire.writeShort(request, 2, 4);
        Wire.writeInt(request, 4, window);
        Wire.writeShort(request, 8, modifiers);
        request[10] = (byte) key;
        request[11] = (byte) pointerMode;
        request[12] = (byte) keyboardMode;
        wire.send(request);
    }

    private static void setInputFocus(Wire wire, int window, int revertTo) throws Exception {
        byte[] request = new byte[12];
        request[0] = 42;                            // SetInputFocus
        request[1] = (byte) revertTo;
        Wire.writeShort(request, 2, 3);
        Wire.writeInt(request, 4, window);
        Wire.writeInt(request, 8, 0);
        wire.send(request);
    }

    private static void fakeKey(Wire wire, int keycode, boolean pressed) throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) xtest;
        request[1] = 2;                             // X_XTestFakeInput
        Wire.writeShort(request, 2, 9);
        request[4] = (byte) (pressed ? 2 : 3);
        request[5] = (byte) keycode;
        wire.send(request);
    }

    private static void drain(Wire wire) throws Exception {
        wire.readTimeoutMillis(400);
        for (byte[] message = wire.readMessage(); message != null; message = wire.readMessage()) {
            describe(message);
        }
        wire.readTimeoutMillis(3000);
    }

    private static void describe(byte[] message) {
        int type = message[0] & 0x7F;
        if (type == 1) {
            return;
        }
        if (type == 0) {
            System.out.printf("  ERROR %d opcode=%d%n", message[1] & 0xFF, message[10] & 0xFF);
            return;
        }
        String name = switch (type) {
            case MOTION_NOTIFY -> "MotionNotify";
            case BUTTON_PRESS -> "ButtonPress";
            case BUTTON_RELEASE -> "ButtonRelease";
            case 7 -> "EnterNotify";
            case 8 -> "LeaveNotify";
            default -> "event " + type;
        };
        // The event window is at 12 for all of these; the pointer's own position differs by type.
        int x = type == MOTION_NOTIFY ? Wire.readShort(message, 24) : Wire.readShort(message, 24);
        int y = type == MOTION_NOTIFY ? Wire.readShort(message, 26) : Wire.readShort(message, 26);
        if (type == BUTTON_PRESS || type == BUTTON_RELEASE) {
            System.out.printf("  %s button=%d at=%d,%d%n", name, message[1] & 0xFF, (short) x,
                    (short) y);
        } else if (type == MOTION_NOTIFY) {
            System.out.printf("  %s at=%d,%d%n", name, (short) x, (short) y);
        } else {
            System.out.printf("  %s win=0x%x mode=%s%n", name, Wire.readInt(message, 12),
                    message[30] < NOTIFY_MODES.length ? NOTIFY_MODES[message[30]]
                            : "mode" + (message[30] & 0xFF));
        }
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
     * {@code GrabPointer} with both device modes given, because the mode is the whole point - see
     * {@link #GRAB_SYNC}.
     */
    private static int grabPointer(Wire wire, int window, boolean ownerEvents, int eventMask,
                                   int pointerMode, int keyboardMode) throws Exception {
        byte[] request = new byte[24];
        request[0] = 26;                            // GrabPointer
        request[1] = (byte) (ownerEvents ? 1 : 0);
        Wire.writeShort(request, 2, 6);
        Wire.writeInt(request, 4, window);
        Wire.writeShort(request, 8, eventMask);
        request[10] = (byte) pointerMode;
        request[11] = (byte) keyboardMode;
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                return -1;
            }
            if ((message[0] & 0x7F) == 1) {
                return message[1] & 0xFF;
            }
            describe(message);
        }
    }

    /**
     * {@code GrabButton} with both device modes given - see {@link #GRAB_SYNC}. Armed on the root it
     * fires for a press anywhere, which is the arrangement a freeze can be seen in.
     */
    private static void grabButton(Wire wire, int window, boolean ownerEvents, int eventMask,
                                   int pointerMode, int keyboardMode) throws Exception {
        byte[] request = new byte[24];
        request[0] = 28;                            // GrabButton
        request[1] = (byte) (ownerEvents ? 1 : 0);
        Wire.writeShort(request, 2, 6);
        Wire.writeInt(request, 4, window);
        Wire.writeShort(request, 8, eventMask);
        request[10] = (byte) pointerMode;
        request[11] = (byte) keyboardMode;
        Wire.writeInt(request, 12, 0);              // confine to: none
        Wire.writeInt(request, 16, 0);              // cursor: none
        request[20] = 0;                            // AnyButton
        Wire.writeShort(request, 22, 0x8000);       // AnyModifier
        wire.send(request);
    }

    private static void ungrabPointer(Wire wire) throws Exception {
        byte[] request = new byte[8];
        request[0] = 27;                            // UngrabPointer
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, 0);               // CurrentTime
        wire.send(request);
    }

    private static void allowEvents(Wire wire, int mode) throws Exception {
        allowEvents(wire, mode, 0);                 // CurrentTime, which is what a client sends
    }

    /** The same with a time of its own: a request outside the grab's range is ignored. */
    private static void allowEvents(Wire wire, int mode, int time) throws Exception {
        System.out.printf("  (sending AllowEvents(%s) at time %d)%n", ALLOW_MODES[mode], time);
        byte[] request = new byte[8];
        request[0] = 35;                            // AllowEvents
        request[1] = (byte) mode;
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, time);
        wire.send(request);
    }

    /** {@code WarpPointer} to a root position: the other way to make the pointer move. */
    private static void warpPointer(Wire wire, int source, int x, int y) throws Exception {
        byte[] request = new byte[24];
        request[0] = 41;                            // WarpPointer
        Wire.writeShort(request, 2, 6);
        Wire.writeInt(request, 4, source);          // 0: a relative move from where it is
        Wire.writeInt(request, 8, wire.root());
        Wire.writeShort(request, 20, x);
        Wire.writeShort(request, 22, y);
        wire.send(request);
    }

    private static void fakeMotion(Wire wire, int x, int y) throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) xtest;
        request[1] = 2;                             // X_XTestFakeInput
        Wire.writeShort(request, 2, 9);
        request[4] = 6;                             // MotionNotify, absolute
        Wire.writeShort(request, 24, x);
        Wire.writeShort(request, 26, y);
        wire.send(request);
    }

    private static void fakeButton(Wire wire, int button, boolean pressed) throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) xtest;
        request[1] = 2;                             // X_XTestFakeInput
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

    private AllowEventsProbe() {
    }
}
