package cn.oyzh.fx.xserver.input;

import cn.oyzh.common.log.JulLog;
import com.github.moaxcp.x11.protocol.XEvent;
import com.github.moaxcp.x11.protocol.xproto.ButtonPressEvent;
import com.github.moaxcp.x11.protocol.xproto.ButtonReleaseEvent;
import com.github.moaxcp.x11.protocol.xproto.KeyPressEvent;
import com.github.moaxcp.x11.protocol.xproto.KeyReleaseEvent;
import com.github.moaxcp.x11.protocol.xproto.MotionNotifyEvent;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.display.CursorSprite;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.event.EventDispatcher;
import cn.oyzh.fx.xserver.event.EventDispatcher.EventFactory;
import cn.oyzh.fx.xserver.event.EventMasks;
import cn.oyzh.fx.xserver.event.XInputMasks;
import cn.oyzh.fx.xserver.extension.ShapeExtension;
import cn.oyzh.fx.xserver.handler.InputHandlers;
import cn.oyzh.fx.xserver.resource.XCursor;
import cn.oyzh.fx.xserver.resource.XWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Pointer, keyboard and focus state, and the input half of {@link DisplayBackend.InputSink}.
 *
 * <p>Input arrives from the display backend on the toolkit's thread. Everything here runs under the
 * server lock so the window tree, the pointer position and the event stream stay consistent with
 * request processing.
 */
public final class InputState implements DisplayBackend.InputSink {


    /** X button state occupies bits 8..12 of the event state field. */
    private static final int BUTTON_MASK_SHIFT = 8;

    /** {@code GrabButton}/{@code GrabKey} modifiers meaning "any": the protocol's own spelling. */
    private static final int ANY_MODIFIER = 0x8000;

    /** Keycodes that only apply while held; everything else with a modifier bit latches. */
    private static final int[] MOMENTARY_KEYCODES = {
            50, 62,     // Shift
            37, 105,    // Control
            64, 108,    // Alt (Mod1)
            133, 134,   // Super (Mod4)
    };

    private final XServer server;
    /**
     * The screen this input state belongs to. Pointer position, focus, grabs and the sprite are all
     * per screen: two clients drawing on two screens have two pointers, and neither can move the
     * other's.
     */
    private final Screen screen;

    private int pointerX;
    private int pointerY;
    private XWindow pointerWindow;

    /**
     * The device the input being processed right now came from, or 0 when it is the device itself.
     *
     * <p>XTEST input is the case: a reference's XTEST events carry the **XTEST slave** as their
     * {@code sourceid} (measured 2026-10-04: an XTEST-driven raw motion is {@code device 2,
     * sourceid 4} on Xvfb) and this server has that slave since A48. The host's own input leaves
     * this clear, which names the master - the remaining difference, because a reference has a
     * backend device of its own ("Xvfb mouse") in that place.
     */
    private int sourceDevice;
    /**
     * The window the client was last <em>told</em> the pointer is in, by a crossing event. Not the
     * same as {@link #pointerWindow}: that one is recomputed by every button or key event too, with
     * no crossing to report it, so it runs ahead of what the client knows. Crossings have to be
     * reported from here, or a client is handed a LeaveNotify for a window it was never told the
     * pointer entered.
     */
    private XWindow announcedWindow;
    /**
     * False until something has said where the pointer is. A window that opens under a stationary
     * pointer gets no mouse event until that pointer moves, and until then the server has no
     * position to draw a sprite at - drawing one anyway puts a second pointer in the window's
     * corner, next to the platform's own arrow.
     */
    private boolean pointerKnown;

    private int buttons;
    private int modifiers;
    private int latched;

    /**
     * How many motion events this screen remembers. Xorg's own {@code MOTION_HISTORY_SIZE} is 256,
     * and the number is a bound on memory per screen rather than a protocol requirement: a client
     * that asks for more than is kept is told what there is.
     */
    private static final int MOTION_HISTORY = 256;

    /**
     * The pointer's own history, oldest first, for {@code GetMotionEvents}.
     *
     * <p>It holds what the pointer <em>did</em>, not what was delivered: a motion reached through a
     * grab, or while the pointer was over another client's window, is part of the history all the
     * same. The window each one is filed under is the one the pointer was in at the time, which is
     * what the request asks about - and the times are the event clock, so they can be compared with
     * the timestamps a client has already seen.
     */
    private final java.util.ArrayDeque<Motion> motionHistory = new java.util.ArrayDeque<>();

    /** One remembered motion: when, where, and the window the pointer was in. */
    public record Motion(int time, int x, int y, XWindow window) {
    }

    /**
     * How long a key is held before it starts repeating, and how long between repeats, in
     * milliseconds: X's own defaults, which is what a client sees on a stock server (`xset q` reports
     * this delay and a rate of 25 per second).
     */
    private static final int DEFAULT_REPEAT_DELAY_MILLIS = 660;
    private static final int DEFAULT_REPEAT_INTERVAL_MILLIS = 40;

    /**
     * The keys that are held down and when each is due to repeat next.
     *
     * <p>The server generates the repeats itself, as a real one does: a toolkit that types a run of
     * characters from a held key depends on it - `xterm` and a Swing text field both print one
     * character per press and nothing else, so without this a held backspace deletes one character.
     */
    private final java.util.Map<Integer, Long> repeatDue = new java.util.HashMap<>();

    /**
     * Which keycodes may repeat. Every key starts out repeating except the modifiers, and a client
     * changes it with {@code ChangeKeyboardControl}'s {@code AutoRepeatMode} or with XKB's
     * {@code SetControls}, whose {@code perKeyRepeat} is the same bitmap.
     *
     * Whether a keycode auto-repeats: every key except the modifiers of the keymap in force, unless
     * a client has put its own bitmap in.
     *
     * <p><b>The modifiers repeat for nobody by default, and the modifier map is where that lives</b>,
     * not a rule applied at press time: measured against Xvfb, whose own {@code auto_repeats} list is
     * exactly the keys its modifier map names. A client that puts one back gets it to repeat, which
     * is what a reference does too.
     *
     * <p>Asked of the keymap rather than copied out of it at startup, because **the modifier rows
     * travel with the layout** ({@code Keymap.useLayout}) and a client can load one at any time: the
     * copy this used to be answered the map of whichever layout was in force when the *server*
     * started, so a {@code setxkbmap -layout de} left the repeat map describing the American
     * keyboard. Measured with {@code xset q}, whose "auto repeating keys" block then differs from a
     * reference's by exactly the keys the two layouts disagree about.
     */
    public boolean autoRepeatsOn(int keycode) {
        boolean[] custom = perKeyCustom;
        if (custom != null && keycode >= 0 && keycode <= Keymap.MAX_KEYCODE) {
            return custom[keycode];
        }
        return keycode >= (Keymap.MIN_KEYCODE & 0xFF) && keycode <= Keymap.MAX_KEYCODE
                && Keymap.modifierMaskForKeycode(keycode) == 0;
    }

    /**
     * The client's own per-key repeat map, or null while the keymap in force decides - see
     * {@link #autoRepeatsOn}. XKB's {@code SetControls} carries the whole bitmap and a per-key
     * {@code SetAutoRepeat} is the same idea one key at a time; either takes over from the layout,
     * and the keys the client did not name keep following it.
     */
    private volatile boolean[] perKeyCustom;

    /**
     * Whether the keyboard repeats at all - XKB's {@code XkbRepeatKeysMask}, the one control bit this
     * server honours. Core {@code ChangeKeyboardControl} has no request for it; XKB's
     * {@code SetControls} does, and a client that turns it off means "no key repeats, whatever the
     * bitmap says".
     */
    private boolean repeatKeysEnabled = true;

    private int repeatDelayMillis = DEFAULT_REPEAT_DELAY_MILLIS;
    private int repeatIntervalMillis = DEFAULT_REPEAT_INTERVAL_MILLIS;

    /** Focus revert modes, numbered as {@code SetInputFocus} numbers them. */
    public static final int REVERT_TO_NONE = 0;
    public static final int REVERT_TO_POINTER_ROOT = 1;
    public static final int REVERT_TO_PARENT = 2;

    private XWindow inputFocus;
    /**
     * True when {@link #inputFocus} - always the root window then - means PointerRoot rather than the
     * root window itself.
     *
     * <p>Two states a client can tell apart, since {@code GetInputFocus} answers {@code PointerRoot}
     * for one and the root's own id for the other, and a change between them reports a different chain
     * of focus events. Where a key goes is what they have in common: see {@link #focusWindow()}.
     */
    private boolean focusIsPointerRoot;
    private int focusRevertTo = 1;   // RevertToPointerRoot
    private boolean focusSet;

    private Grab pointerGrab;
    private Grab keyboardGrab;

    /**
     * The button or key a <em>passive</em> grab is being held for, so that its release ends the grab;
     * 0 when the grab is one a client took for itself with {@code GrabPointer}/{@code GrabKeyboard}.
     *
     * <p>A passive grab becomes active when the event it was armed for happens and stops when that
     * event ends - a button grab holds the pointer from the press to the release of the button it
     * matched, and a key grab from the press to the release of the key. That is the whole of its
     * lifetime, and it is the difference between it and an explicit grab, which lasts until the client
     * says otherwise.
     */
    private int passiveGrabDetail;

    /** The modifiers the passive grab matched with, for the crossing pair's state field. */
    private int passiveGrabModifiers;

    /**
     * X11's implicit grab: the window the last button press landed on.
     *
     * <p>While any button is down, every pointer event is reported against that window even once the
     * pointer has moved off it. A client relies on this to drag: the window it is moving follows the
     * pointer, so the pointer is regularly outside it for a moment, and a client that stops being
     * told about motion in the middle of a drag never sees the drag end - it stays in its "moving"
     * state and quietly ignores every later click.
     */
    private XWindow implicitGrabWindow;

    /**
     * Where the device physically is, which a frozen device keeps up to date while the pointer the
     * clients see does not move.
     *
     * <p>X separates the two for exactly this reason, and the reason is measured: with a device
     * frozen, {@code QueryPointer} answers the position it had when it froze, while the events
     * delivered when it starts again carry the position it was moved to in the meantime. A client
     * that freezes the pointer to re-centre it - the case a game and a remote-desktop client are in
     * - reads its own position through both of them, and a server that moves them together either
     * lies about where the pointer is or delivers a stale position.
     */
    private int deviceX;
    private int deviceY;

    // ------------------------------------------------------------------
    // Synchronous grabs: the freeze, the queue and the replay
    // ------------------------------------------------------------------

    /**
     * A device's synchronous-grab state - X's {@code GrabInfoRec.sync}, and the whole of A13.
     *
     * <p>{@code GrabModeSync} is a grab asking the server to <b>stop the device</b> so the client can
     * decide what happens to what comes next: a game that re-centres the pointer after every motion,
     * a remote-desktop client that re-delivers a click from somewhere else. The device a {@code Sync}
     * grab is taken on stops delivering after the event that triggered it, and {@code AllowEvents}
     * starts it again - or reprocesses the event it stopped on.
     *
     * <p>The states and the transitions are Xorg's, read off {@code include/inputstr.h} and
     * {@code dix/events.c} rather than guessed at, because the prose in the protocol specification
     * does not describe them (see the notes on each transition below).
     */
    private static final class Sync {
        /** One of the {@code GRAB_STATE_*} values below. */
        int state = NOT_GRABBED;
        /**
         * The grab that has this device frozen on <em>another</em> device's behalf, or null.
         *
         * <p>A grab names a mode for each device - {@code GrabPointer} has a {@code pointer_mode} and
         * a {@code keyboard_mode} - and asking for the other device's mode to be {@code Sync} freezes
         * that device too, which is how a menu that freezes the pointer also freezes the keyboard.
         */
        Grab other;
        /** The event the device froze on, kept for a {@code Replay*} mode to reprocess. */
        Held event;

        boolean frozen() {
            return other != null || state >= FROZEN;
        }
    }

    /**
     * Grab states, exactly as X numbers them.
     *
     * <p>The numbering is load-bearing: {@code FROZEN} is a <em>sentinel</em>, not a state of its own
     * - "any state at or above this one has the device frozen" - and {@code FREEZE_NEXT_EVENT} is
     * deliberately <em>below</em> it, because a device told to freeze on the next event is still
     * delivering until that event arrives.
     */
    private static final int NOT_GRABBED = 0;
    private static final int THAWED = 1;
    // 2 is X's THAWED_BOTH, and it is not a state a device is ever left in: AsyncBoth sets each
    // device to THAWED. The gap is kept so the numbers below stay X's.
    private static final int FREEZE_NEXT_EVENT = 3;
    private static final int FREEZE_BOTH_NEXT_EVENT = 4;
    private static final int FROZEN = 5;
    private static final int FROZEN_NO_EVENT = 5;
    private static final int FROZEN_WITH_EVENT = 6;

    /** {@code GrabModeSync}, which is 0 - the opposite way round from {@code AllowEvents}. */
    private static final int GRAB_MODE_SYNC = 0;

    private final Sync pointerSync = new Sync();
    private final Sync keyboardSync = new Sync();

    /**
     * The events that arrived while their device was frozen, in the order they arrived - X's
     * {@code syncEvents.pending}.
     *
     * <p>Held rather than dropped, because that is what makes {@code AllowEvents} meaningful: the
     * motion the user made while the device was stopped is delivered the moment the client releases
     * it, at the position it was made. A device that silently dropped them would make a {@code Sync}
     * grab into a way of losing input.
     */
    private final List<Held> held = new ArrayList<>();

    /** The frozen event a {@code Replay*} mode asked to reprocess, or null when there is none. */
    private Replay replay;

    /** Set while the queue is being drained, so playing an event cannot restart the drain. */
    private boolean playingEvents;

    /**
     * The window a replayed event must not be grabbed on again.
     *
     * <p>A replay means "the event is reprocessed as if the grab had not fired", so the grab the
     * event was taken from must not simply fire again on it - measured against a reference server,
     * which starts its search for a passive grab <em>below</em> the window the replayed grab was held
     * on. Any other grab the event would have matched still fires.
     */
    private XWindow passiveGrabFloor;

    /** The current cursor's pixels, copied once per cursor because the backend asks every frame. */
    private int[] cursorPixels;
    private XCursor cursorPixelsFor;
    /** The image version of {@link #cursorPixelsFor} the cached pixels were read from. */
    private int cursorPixelsVersion;

    public InputState(XServer server) {
        this(server, server.screen());
    }

    public InputState(XServer server, Screen screen) {
        this.server = server;
        this.screen = screen;
        this.inputFocus = screen.root();

    }

    /** The screen whose pointer, focus and grabs this is. */
    public Screen screen() {
        return screen;
    }

    /** Which device the input right now came from, or 0 when it is the event's own device. */
    public int sourceDevice() {
        return sourceDevice;
    }

    /** Marks the input that follows as coming from another device, until it is cleared again. */
    public void setSourceDevice(int device) {
        sourceDevice = device;
    }

    /**
     * An active pointer or keyboard grab.
     *
     * <p>The two modes are the ones the client asked for in {@code GrabPointer}/{@code GrabKey}:
     * {@code GrabModeSync} (0) stops the device, and the mode named for the <em>other</em> device
     * stops that one as well. They are kept here because a grab's lifetime is the only thing that
     * decides which device freezes - see {@link #grabActivated}.
     */
    public record Grab(ClientConnection client, XWindow window, boolean ownerEvents, int eventMask,
                       boolean keyboard, int pointerMode, int keyboardMode, int grabTime) {

        /** The mode this grab names for the device it is held on. */
        int ownMode() {
            return keyboard ? keyboardMode : pointerMode;
        }

        /** The mode this grab names for the other device. */
        int otherMode() {
            return keyboard ? pointerMode : keyboardMode;
        }
    }

    /**
     * One device event, in the form the queue holds it.
     *
     * <p>An event here is what a caller handed the server - a motion, a button, a key - with
     * everything needed to process it later: {@link #deliver} runs it through the ordinary path when
     * the device is running, and {@link #playHeld} does the same for one the queue held back.
     */
    private record Held(boolean pointer, int kind, int detail, boolean pressed, int x, int y) {
    }

    private static final int KIND_MOTION = 0;
    private static final int KIND_BUTTON = 1;
    private static final int KIND_KEY = 2;

    /** A frozen event a {@code Replay*} mode asked to reprocess, and the window it was grabbed on. */
    private record Replay(Held event, XWindow window) {
    }

    // ------------------------------------------------------------------
    // State accessors
    // ------------------------------------------------------------------

    public int pointerX() {
        return pointerX;
    }

    public int pointerY() {
        return pointerY;
    }

    public XWindow pointerWindow() {
        return pointerWindow;
    }

    /**
     * The deepest mapped window containing the pointer, or the root as a fallback.
     *
     * <p>Descent is done here rather than with {@code XWindow.windowAt} because a window's input
     * shape can make a corner transparent to the pointer; a point over a shaped out region belongs
     * to whatever is behind it, which is the whole point of the extension.
     */
    public XWindow windowAtPointer() {
        XWindow window = screen.root();
        while (true) {
            XWindow child = null;
            List<XWindow> children = window.children();
            for (int i = children.size() - 1; i >= 0; i--) {
                XWindow candidate = children.get(i);
                if (candidate.isMapped()
                        && candidate.absoluteBoundsWithBorder().contains(pointerX, pointerY)
                        && ShapeExtension.acceptsPointer(server, candidate, pointerX, pointerY)) {
                    child = candidate;
                    break;
                }
            }
            if (child == null) {
                return window;
            }
            window = child;
        }
    }

    public XWindow inputFocus() {
        return inputFocus;
    }

    /**
     * Whether the focus is {@code PointerRoot} rather than the window {@link #inputFocus} names.
     *
     * <p>The two are different states and a client can tell them apart: {@code GetInputFocus} answers
     * {@code PointerRoot} for one and the root window's own id for the other, and a key press goes to
     * whatever the pointer is in for one and to the root window for the other. The root window is
     * how both are stored here because it is a real window object; this flag is what separates them.
     */
    public boolean focusIsPointerRoot() {
        return inputFocus != null && focusIsPointerRoot;
    }

    /** Sets the focus to a window, to {@code None} when the window is null, or to PointerRoot when it
     * is the root - the reading every caller inside this class wants. A client's own request says
     * which of the two it meant; see the three argument form. */
    public void setInputFocus(XWindow window, int revertTo) {
        setInputFocus(window, revertTo, window != null && window.isRoot());
    }

    /**
     * The same, with the two states that are not an ordinary window spelled out.
     *
     * @param window the window, or null for None, or the root for either of the states below
     * @param pointerRoot true when the focus follows the pointer rather than naming that window
     */
    public void setInputFocus(XWindow window, int revertTo, boolean pointerRoot) {
        XWindow previous = inputFocus;
        boolean previousFollows = focusIsPointerRoot();
        // The three states are kept apart, because a client can tell them apart: null is None (the
        // keyboard goes nowhere), the root with this flag is PointerRoot (it goes to whatever the
        // pointer is in), the root without it is the root window, and anything else is a window.
        // Folding any two of them together would report a focus the client did not ask for.
        this.inputFocus = window;
        this.focusIsPointerRoot = pointerRoot && window != null && window.isRoot();
        this.focusRevertTo = revertTo;
        this.focusSet = true;
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("focus set to {}, was {}, reverting to {}",
                    describeFocus(inputFocus, focusIsPointerRoot), describeFocus(previous,
                            previousFollows), revertTo);
        }
        // A client that asks for the focus is waiting to be told it got it, and this is the only
        // thing that tells it. While a keyboard grab is held the change is reported as made *by*
        // the grab, which is what a toolkit reads the mode for: measured with tools/FocusProbe, a
        // reference server reports every focus change made inside a grab as NotifyWhileGrabbed.
        server.events().deliverFocusChange(previous, previousFollows, inputFocus,
                focusIsPointerRoot(),
                keyboardGrab == null ? EventDispatcher.NOTIFY_MODE_NORMAL
                        : EventDispatcher.NOTIFY_MODE_WHILE_GRABBED,
                false);
    }

    /** A focus state's name for a log line: the two that are not a window say so. */
    private static String describeFocus(XWindow window, boolean pointerRoot) {
        if (window == null) {
            return "nowhere";
        }
        if (pointerRoot) {
            return "the pointer";
        }
        return "0x" + Integer.toHexString(window.id()) + (window.isRoot() ? " (the root window)" : "");
    }

    private static String describe(XWindow window) {
        return window == null ? "nowhere" : "0x" + Integer.toHexString(window.id());
    }

    public int focusRevertTo() {
        return focusRevertTo;
    }

    public boolean isFocusSet() {
        return focusSet;
    }

    /** Modifier and button state packed the way the X event {@code state} field expects. */
    public int pointerState() {
        return modifiers | (buttons << BUTTON_MASK_SHIFT);
    }

    public int modifierState() {
        return modifiers;
    }

    public Grab pointerGrab() {
        return pointerGrab;
    }

    public Grab keyboardGrab() {
        return keyboardGrab;
    }

    /** The status a grab request is answered with: 0 success, 1 held by another client, 4 not viewable. */
    public static final byte GRAB_SUCCESS = 0;
    public static final byte GRAB_ALREADY_GRABBED = 1;
    public static final byte GRAB_NOT_VIEWABLE = 4;

    /**
     * Whether a grab may be taken on this window, for {@code client}.
     *
     * <p>Shared by the core requests and XInput2's {@code XIGrabDevice} rather than written twice: a
     * window has one pointer and one keyboard, so "the device is already grabbed" means the same
     * thing on both rails, and a client that takes a core grab and then reads an XI2 one must not be
     * told something different from a client that only ever used XI2. GDK does exactly that - it
     * grabs with {@code XIGrabDevice} and may have taken a core grab earlier in its life.
     */
    public byte grabStatus(ClientConnection client, XWindow window) {
        if (!window.isViewable()) {
            return GRAB_NOT_VIEWABLE;
        }
        if (pointerGrab != null && pointerGrab.client() != client) {
            return GRAB_ALREADY_GRABBED;
        }
        if (keyboardGrab != null && keyboardGrab.client() != client) {
            return GRAB_ALREADY_GRABBED;
        }
        return GRAB_SUCCESS;
    }

    /**
     * Takes a device grab and answers the status, for either rail.
     *
     * <p>The modes are the core ones ({@code GrabModeSync = 0}, {@code GrabModeAsync = 1}), which is
     * what {@link Grab} stores; a caller whose own protocol numbers them differently - XInput2 has
     * {@code XIGrabModeAsync = 1} and {@code XIGrabModeSync = 2} - converts before calling.
     */
    public byte setDeviceGrab(ClientConnection client, XWindow window, boolean keyboard,
                              boolean ownerEvents, int eventMask, int ownMode, int pairedMode,
                              int grabTime) {
        byte status = grabStatus(client, window);
        if (status != GRAB_SUCCESS) {
            return status;
        }
        int pointerMode = keyboard ? pairedMode : ownMode;
        int keyboardMode = keyboard ? ownMode : pairedMode;
        Grab grab = new Grab(client, window, ownerEvents, eventMask, keyboard, pointerMode,
                keyboardMode, grabTime);
        if (keyboard) {
            setKeyboardGrab(grab);
        } else {
            setPointerGrab(grab);
        }
        return GRAB_SUCCESS;
    }

    public void setPointerGrab(Grab grab) {
        Grab previous = this.pointerGrab;
        this.pointerGrab = grab;
        if (previous != null && previous != grab) {
            grabReleased(previous);
        }
        if (grab != null && grab != previous) {
            grabActivated(pointerSync, keyboardSync, grab);
        }
    }

    /**
     * Takes or releases the keyboard grab, reporting the focus it moves the way a reference server
     * does.
     *
     * <p>A keyboard grab is not only a change of where keys are delivered: the client that took it
     * is told the focus is on the window it grabbed, and told again when the grab ends. Both are
     * events, and this is the only place they can be raised, because this is the only thing that
     * changes a grab.
     */
    public void setKeyboardGrab(Grab grab) {
        Grab previous = this.keyboardGrab;
        this.keyboardGrab = grab;
        boolean grabbed = previous != null;
        boolean grabbing = grab != null;
        boolean changed = !grabbed || !grabbing || previous.window() != grab.window();
        if (grabbed && changed) {
            reportKeyboardGrabEnd(previous);
        }
        if (grabbing && changed) {
            reportKeyboardGrabStart(grab);
        }
        if (previous != null && previous != grab) {
            grabReleased(previous);
        }
        if (grab != null && grab != previous) {
            grabActivated(keyboardSync, pointerSync, grab);
        }
    }

    // ------------------------------------------------------------------
    // Freezing, the held queue and the replay (A13)
    // ------------------------------------------------------------------

    /**
     * X's {@code CheckGrabForSyncs}: a grab's two device modes decide which devices it freezes and
     * when.
     *
     * <p>The device the grab is held on is frozen outright when its own mode is {@code Sync} - from
     * then on nothing it produces is delivered until the client releases it - and is left running
     * otherwise. The <em>other</em> device is frozen only if its mode says so, and it is frozen
     * <em>on this grab's behalf</em>: the client that owns the grab is the one that can release it,
     * and the device does not count as grabbed by anybody in the meantime.
     */
    private void grabActivated(Sync sync, Sync paired, Grab grab) {
        int thisMode = grab.ownMode();
        int otherMode = grab.otherMode();
        if (thisMode == GRAB_MODE_SYNC) {
            sync.state = FROZEN_NO_EVENT;
        } else {
            sync.state = THAWED;
            if (sync.other != null && sync.other.client() == grab.client()) {
                sync.other = null;
            }
        }
        if (otherMode == GRAB_MODE_SYNC) {
            paired.other = grab;
        } else if (paired.other != null && paired.other.client() == grab.client()) {
            paired.other = null;
        }
        computeFreezes();
    }

    /**
     * X's {@code DeactivatePointerGrab}/{@code DeactivateKeyboardGrab}: the grab is gone, so the
     * device it froze stops being frozen and anything held on its behalf is let go.
     *
     * <p>Without the second half a client that froze the other device and then disconnected - or
     * simply let go of its own grab - would leave that device stopped for ever, with no client able
     * to start it again: {@code AllowEvents} only speaks for the device the caller holds.
     */
    private void grabReleased(Grab grab) {
        if (grab.keyboard()) {
            keyboardSync.state = NOT_GRABBED;
        } else {
            pointerSync.state = NOT_GRABBED;
        }
        if (pointerSync.other == grab) {
            pointerSync.other = null;
        }
        if (keyboardSync.other == grab) {
            keyboardSync.other = null;
        }
        computeFreezes();
    }

    /**
     * X's {@code FreezeThisEventIfNeededForSyncGrab}, called after an event has been delivered: the
     * event that arrives while its device was told to freeze on the next one is what the device
     * freezes <em>on</em>, and it is the event a {@code Replay*} mode would later reprocess.
     *
     * <p><b>Only a key or a button does it</b>, and that asymmetry is not an accident of this code:
     * a motion goes on being delivered while a frozen device waits for one of them, which is why
     * {@code AllowEvents(SyncPointer)} on a device frozen from a grab with no event yet releases
     * everything up to and including the first button, and nothing after it.
     */
    private void freezeThisEvent(Sync sync, Held event) {
        if (event.kind() == KIND_MOTION) {
            return;
        }
        boolean pointer = sync == pointerSync;
        Sync paired = pointer ? keyboardSync : pointerSync;
        Grab grab = pointer ? pointerGrab : keyboardGrab;
        Grab pairedGrab = pointer ? keyboardGrab : pointerGrab;
        switch (sync.state) {
            case FREEZE_NEXT_EVENT -> {
                sync.state = FROZEN_WITH_EVENT;
                sync.event = event;
            }
            case FREEZE_BOTH_NEXT_EVENT -> {
                // The paired device is frozen too, and told the same thing: it waits for its own
                // next event unless the same client asked both to freeze, in which case there is
                // nothing left for it to wait for.
                if (paired.state == FREEZE_BOTH_NEXT_EVENT && pairedGrab != null && grab != null
                        && pairedGrab.client() == grab.client()) {
                    paired.state = FROZEN_NO_EVENT;
                } else if (grab != null) {
                    paired.other = grab;
                }
                sync.state = FROZEN_WITH_EVENT;
                sync.event = event;
            }
            default -> {
            }
        }
    }

    /**
     * The one way an input event reaches the server: held back when its device is frozen, delivered
     * otherwise, with the freeze the delivery itself may ask for applied after it.
     */
    private void deliver(Held event) {
        Sync sync = event.pointer() ? pointerSync : keyboardSync;
        if (event.kind() == KIND_MOTION) {
            // The device moves whether or not it is frozen; only its events wait. X's own enqueue
            // path does the same - it stamps the sprite's physical position for every motion it
            // holds - and that is the position the events carry once the client lets them through.
            deviceX = clampX(event.x());
            deviceY = clampY(event.y());
        }
        if (sync.frozen()) {
            held.add(event);
            if (held.size() > MAX_HELD_EVENTS) {
                // The reference has no bound here either, and pays for it with an unbounded queue
                // when a client freezes a device and never starts it again; this server refuses to
                // grow without limit, for the same reason its output queue to a client does (E5).
                Held dropped = held.remove(0);
                JulLog.warn("dropped a {} held for a frozen {}: {} events were waiting for a client"
                                + " that has not released it", kindName(dropped.kind()),
                        dropped.pointer() ? "pointer" : "keyboard", MAX_HELD_EVENTS);
            }
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("held a {} for a frozen {}: {} event(s) waiting", kindName(event.kind()),
                        event.pointer() ? "pointer" : "keyboard", held.size());
            }
            return;
        }
        play(event, false);
        freezeThisEvent(sync, event);
    }

    /** How many events one frozen device may hold before the oldest is dropped. */
    private static final int MAX_HELD_EVENTS = 1024;

    private int clampX(int x) {
        return Math.max(0, Math.min(x, screen.width() - 1));
    }

    private int clampY(int y) {
        return Math.max(0, Math.min(y, screen.height() - 1));
    }

    /**
     * Where an event is stamped: the device's own position, not the pointer the clients see.
     *
     * <p>The two are the same except while a device is frozen, and there the difference is the whole
     * of what a client that froze the pointer to re-centre it is reading: it gets events at the
     * position the pointer was moved to, while {@code QueryPointer} still says where it froze.
     */
    private int reportedX() {
        return deviceX;
    }

    private int reportedY() {
        return deviceY;
    }

    /**
     * Processes one event through the ordinary path, whatever the device's sync state is.
     *
     * <p>{@code fromQueue} is what a held event knows and a live one does not: the device has moved
     * on since it was held, so the event's own coordinates are no longer where the pointer is and
     * must not drag it back. X does the same - a queued event is stamped with the device's position
     * at delivery, and only a queued <em>motion</em> moves the sprite.
     */
    private void play(Held event, boolean fromQueue) {
        switch (event.kind()) {
            case KIND_MOTION -> moveSprite(event.x(), event.y(), true);
            case KIND_BUTTON -> processButton(event, fromQueue);
            default -> processKey(event, fromQueue);
        }
    }

    private static String kindName(int kind) {
        return switch (kind) {
            case KIND_MOTION -> "motion";
            case KIND_BUTTON -> "button";
            default -> "key";
        };
    }

    /**
     * X's {@code ComputeFreezes}: reprocess a replayed event, then deliver what the queue holds whose
     * device is no longer frozen.
     *
     * <p>Called wherever the frozen state can change - a grab starting or ending, and every
     * {@code AllowEvents} that means anything.
     */
    private void computeFreezes() {
        if (replay != null) {
            Replay pending = replay;
            replay = null;
            // "Reprocessed as if the grab had not fired": the event goes through the ordinary path
            // again - a passive grab it matches still fires - with the one exception that the grab
            // it was taken from cannot fire on it a second time.
            passiveGrabFloor = pending.window();
            try {
                play(pending.event(), true);
            } finally {
                passiveGrabFloor = null;
            }
        }
        playHeld();
    }

    /**
     * X's {@code PlayReleasedEvents}: delivers the held events in the order they arrived, stopping
     * as soon as every device is frozen again.
     *
     * <p>The stopping rule is what makes a {@code Sync} release deliver a <em>run</em> of events
     * rather than one: the event that freezes the device again is the first key or button played,
     * and everything that was queued behind it waits for the next release.
     */
    private void playHeld() {
        if (playingEvents || held.isEmpty()) {
            return;
        }
        playingEvents = true;
        try {
            for (java.util.Iterator<Held> events = held.iterator(); events.hasNext(); ) {
                Held event = events.next();
                if ((event.pointer() ? pointerSync : keyboardSync).frozen()) {
                    continue;
                }
                events.remove();
                play(event, true);
                freezeThisEvent(event.pointer() ? pointerSync : keyboardSync, event);
                if (pointerSync.frozen() && keyboardSync.frozen()) {
                    break;
                }
            }
        } finally {
            playingEvents = false;
        }
    }

    /**
     * X's {@code AllowSome}: {@code AllowEvents}, mode by mode.
     *
     * <p>Every mode is a way of starting a device a {@code Sync} grab stopped, and the client has to
     * be the one that stopped it - a client that asks to release a device it never froze is ignored
     * rather than refused, which is what a reference server does.
     *
     * <p>Not modelled: the protocol's time check (a request older than the grab is ignored) - this
     * server has no per-grab timestamp to compare against, and the only clients that send a time
     * send {@code CurrentTime}.
     */
    public void allowEvents(ClientConnection client, int mode, int time) {
        boolean onPointer = mode <= 2;
        Sync sync = onPointer ? pointerSync : keyboardSync;
        Sync paired = onPointer ? keyboardSync : pointerSync;
        Grab grab = onPointer ? pointerGrab : keyboardGrab;
        Grab pairedGrab = onPointer ? keyboardGrab : pointerGrab;
        boolean thisGrabbed = grab != null && grab.client() == client;
        boolean thisSynced = sync.other != null && sync.other.client() == client;
        boolean othersFrozen = pairedGrab != null && pairedGrab.client() == client && paired.frozen();
        if (!((thisGrabbed && sync.state >= FROZEN) || thisSynced)) {
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("AllowEvents(mode {}) from {} ignored: it does not hold a frozen device",
                        mode, client);
            }
            return;
        }
        // X's own time check, and it is a *silent* one: a request whose time is later than now, or
        // earlier than the grab itself, is ignored rather than refused. CurrentTime (0) means "now"
        // and is what every real client sends; the check is here because the protocol has it and a
        // client that names a time is entitled to the same answer a reference server gives it.
        if (time != 0 && grab != null
                && (laterThan(time, (int) server.events().time()) || laterThan(grab.grabTime(), time))) {
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("AllowEvents(mode {}) from {} ignored: its time is outside the grab ({} vs"
                        + " now {}, grabbed at {})", mode, client, time, server.events().time(),
                        grab.grabTime());
            }
            return;
        }
        switch (mode) {
            case 0, 3 -> {                                  // AsyncPointer / AsyncKeyboard
                if (thisGrabbed) {
                    sync.state = THAWED;
                }
                if (thisSynced) {
                    sync.other = null;
                }
            }
            case 1, 4 -> {                                  // SyncPointer / SyncKeyboard
                if (thisGrabbed) {
                    sync.state = FREEZE_NEXT_EVENT;
                    if (thisSynced) {
                        sync.other = null;
                    }
                }
            }
            case 6 -> {                                     // AsyncBoth
                if (othersFrozen) {
                    thawBoth(client, THAWED);
                }
            }
            case 7 -> {                                     // SyncBoth
                if (othersFrozen) {
                    thawBoth(client, FREEZE_BOTH_NEXT_EVENT);
                }
            }
            default -> {                                    // ReplayPointer / ReplayKeyboard
                if (thisGrabbed && sync.state == FROZEN_WITH_EVENT) {
                    if (thisSynced) {
                        sync.other = null;
                    }
                    // The event is handed to the replay *before* the grab goes away, because the
                    // release is what drains the queue and the replayed event has to be first.
                    replay = new Replay(sync.event, grab.window());
                    endGrab(grab, passiveGrabDetail != 0 && passiveGrabDetail == sync.event.detail());
                }
            }
        }
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("AllowEvents(mode {}) from {}: pointer {} keyboard {}", mode, client,
                    syncName(pointerSync), syncName(keyboardSync));
        }
        computeFreezes();
    }

    /**
     * X's {@code CompareTimeStamps}: whether {@code a} is later than {@code b}, on times that wrap.
     *
     * <p>Both are 32 bit values from a millisecond clock that wraps in about fifty days, so "later" is
     * the sign of their difference and not their order - the same rule {@code XServer} uses for
     * {@code GetMotionEvents}' range.
     */
    private static boolean laterThan(int a, int b) {
        return a != b && a - b > 0;
    }

    /** X's {@code AsyncBoth}/{@code SyncBoth}: every device this client grabbed gets the new state. */
    private void thawBoth(ClientConnection client, int state) {
        if (pointerGrab != null && pointerGrab.client() == client) {
            pointerSync.state = state;
        }
        if (keyboardGrab != null && keyboardGrab.client() == client) {
            keyboardSync.state = state;
        }
        if (pointerSync.other != null && pointerSync.other.client() == client) {
            pointerSync.other = null;
        }
        if (keyboardSync.other != null && keyboardSync.other.client() == client) {
            keyboardSync.other = null;
        }
    }

    private static String syncName(Sync sync) {
        return switch (sync.state) {
            case NOT_GRABBED -> "running";
            case THAWED -> "thawed";
            case FREEZE_NEXT_EVENT -> "freezes on the next event";
            case FREEZE_BOTH_NEXT_EVENT -> "freezes both on the next event";
            case FROZEN_WITH_EVENT -> "frozen on an event";
            default -> "frozen";
        };
    }

    /**
     * Reports the focus the grab takes: the window the keyboard was on hears that it left, and the
     * grab window hears that it arrived, both with mode {@code NotifyGrab}.
     *
     * <p>Two details are measured rather than derived from the prose: the pair is sent even when the
     * grab window already had the focus (a client grabs to be told it holds the keyboard, and a
     * server that treats "it is already there" as nothing to report leaves it waiting), and a grab
     * taken while the focus is on nothing at all reports nothing - there is no window to be told it
     * lost anything, and the reference does not tell the grab window either.
     */
    private void reportKeyboardGrabStart(Grab grab) {
        if (inputFocus == null) {
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("keyboard grabbed by 0x{} with the focus nowhere: nothing to report",
                        Integer.toHexString(grab.window().id()));
            }
            return;
        }
        server.events().deliverFocusChange(inputFocus, focusIsPointerRoot(), grab.window(),
                grab.window().isRoot(), EventDispatcher.NOTIFY_MODE_GRAB, true);
    }

    /**
     * Reports the focus the grab gives back: from the grab window to wherever the focus is now, with
     * mode {@code NotifyUngrab}.
     *
     * <p>"Wherever the focus is now" is not the focus the grab took - a client that calls
     * {@code SetInputFocus} while it holds the keyboard has moved it, and the reference reports the
     * way back from the grab window to the new window rather than to the old one.
     */
    private void reportKeyboardGrabEnd(Grab grab) {
        server.events().deliverFocusChange(grab.window(), grab.window().isRoot(), inputFocus,
                focusIsPointerRoot(), EventDispatcher.NOTIFY_MODE_UNGRAB, true);
    }

    public void clientDisconnected(ClientConnection client) {
        releaseGrabs(client);
    }

    /** Releases any grab held by a client, freeing the keyboard or pointer for others. */
    public void releaseGrabs(ClientConnection client) {
        // The arms go with the client that made them, as they do on a reference server. An arm that
        // outlived its client is worse than a missing one: it is a global shortcut that keeps
        // matching, holding the events of whoever presses it and delivering them to a connection
        // that is gone - measured, and it is what made two runs of the same probe disagree.
        int arms = server.passiveGrabs().size();
        server.passiveGrabs().removeIf(grab -> grab.client() == client);
        if (arms != server.passiveGrabs().size() && JulLog.isDebugEnabled()) {
            JulLog.debug("dropped {} passive grab(s) with the client that armed them",
                    arms - server.passiveGrabs().size());
        }
        if (pointerGrab != null && pointerGrab.client() == client) {
            Grab grab = pointerGrab;
            pointerGrab = null;
            grabReleased(grab);
        }
        if (keyboardGrab != null && keyboardGrab.client() == client) {
            Grab grab = keyboardGrab;
            keyboardGrab = null;
            grabReleased(grab);
        }
        if (implicitGrabWindow != null && implicitGrabWindow.owner() == client) {
            implicitGrabWindow = null;
        }
    }

    /** The window the implicit grab is holding the pointer on, or null when there is none. */
    public XWindow implicitGrabWindow() {
        return implicitGrabWindow;
    }

    /** Moves the pointer without generating events, used by {@code WarpPointer}. */
    public void warpPointer(int x, int y) {
        movePointer(x, y, false);
    }

    /**
     * Sounds the bell. {@code percent} is the volume as a client asked for it; -1 means "use the
     * server default". The host bell is used when one is available.
     */
    public void ringBell(int percent) {
        try {
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Throwable e) {
            // Headless or no audio device: a silent bell is an acceptable outcome.
        }
    }

    // ------------------------------------------------------------------
    // InputSink
    // ------------------------------------------------------------------

    @Override
    public void pointerMoved(int x, int y) {
        server.screenSaver().noteInput();
        server.lockServer();
        try {
            // **The raw event goes out before the server has done anything with the motion** (XI 2.1):
            // it is the device's own report, so it is not about a window, a grab or a frozen device -
            // and a client that asked for it on the root hears about input that was delivered to
            // nobody. See XInputExtension.deliverRaw.
            rawMotion(x, y);
            deliver(new Held(true, KIND_MOTION, 0, false, x, y));
        } finally {
            server.unlockServer();
        }
    }

    @Override
    public void buttonChanged(int button, boolean pressed, int x, int y) {
        if (button < 1 || button > 12) {
            return;
        }
        // The idle countdown is about the user's own input, and a button is input as much as a motion
        // or a key is: a machine whose pointer is still but whose buttons are being pressed is not
        // idle, and MIT-SCREEN-SAVER's countdown has to agree.
        server.screenSaver().noteInput();
        server.lockServer();
        try {
            rawButton(button, pressed);
            // A frozen device holds the press rather than acting on it - even the button state is not
            // updated, because the client that stopped the device has not said what to do with it yet.
            deliver(new Held(true, KIND_BUTTON, button, pressed, x, y));
        } finally {
            server.unlockServer();
        }
    }

    /** The button's own work, once {@link #deliver} has decided it is this device's turn. */
    private void processButton(Held event, boolean fromQueue) {
        int button = event.detail();
        boolean pressed = event.pressed();
        int x = event.x();
        int y = event.y();
        if (fromQueue) {
            notePointerWindow();
        } else {
            movePointer(x, y, false);
        }
        int mask = 1 << (button - 1);
        if (pressed) {
            buttons |= mask;
        } else {
            buttons &= ~mask;
        }
        if (JulLog.isDebugEnabled()) {
            // Which window a click is *reported against* is otherwise invisible from the server
            // side, and it is the first thing to check when a client behaves as if it was never
            // clicked: while a button is down that is the window the press landed on, not the one
            // under the pointer, and an active grab overrides both.
            XWindow target = eventTarget();
            JulLog.debug("button {} {} at ({},{}) -> {}{}", button, pressed ? "down" : "up", x, y,
                    target == null ? "no window" : "0x" + Integer.toHexString(target.id()),
                    pointerGrab != null
                            ? " (grab on 0x" + Integer.toHexString(pointerGrab.window().id())
                                    + ", ownerEvents=" + pointerGrab.ownerEvents() + ")"
                            : "");
        }
        if (pressed) {
            focusOnClick();
        }
        if (pressed) {
            // A passive grab is matched before the event is delivered: if one fires, the press
            // belongs to the client that armed it, wherever it landed.
            // The *modifiers*, not the whole pointer state: the state also carries the button
            // that was just pressed, and a grab armed for Shift must not be looking at that.
            InputHandlers.PassiveGrab passive = matchingPassiveGrab(true, pointerWindow, button,
                    modifierState());
            if (passive != null) {
                activatePassiveGrab(passive, button, modifierState(), event);
            }
        }
        deliverButton(button, pressed, mask);
        if (!pressed && passiveGrabDetail == button) {
            // The release has been delivered through the grab; the grab itself ends here.
            endPassiveGrab();
        }
    }

    @Override
    /**
     * A character the host platform composed for the user: what an input method produced, or a dead
     * key's accent.
     *
     * <p>This is the half of typing that a keycode cannot carry. A keycode says which key was
     * pressed, and a client turns that into a character with its own copy of the keyboard mapping -
     * which works for everything printed on a keyboard and for nothing else: no keyboard has a key
     * per Chinese character. So the host's input method composes the text, the platform hands it
     * over as a character, and the server types it by giving a keycode it has claimed the character
     * as its keysym and telling the clients the mapping changed - which is the only way an X client
     * learns a character that is on no key at all.
     *
     * <p>Nothing is sent for a character the keyboard already carries: the key press has been
     * delivered for it, and a client would type it twice.
     */
    public void keyTyped(char character) {
        int keysym = Keymap.keysymFor(character);
        int onTheKeyboard = Keymap.keycodeForKeysym(keysym);
        // A keycode the server claimed for an earlier character does not count as "on the keyboard":
        // the same character typed twice is two keystrokes, and the second one has to be sent like
        // the first.
        boolean alreadyOnAKey = onTheKeyboard != 0 && !Keymap.isClaimed(onTheKeyboard);
        JulLog.debug("keyTyped '{}' keysym=0x{} onTheKeyboard={} already={}", character,
                Integer.toHexString(keysym), onTheKeyboard, alreadyOnAKey);
        if (keysym == Keysyms.NONE || alreadyOnAKey) {
            return;
        }
        typeComposed(keysym);
    }

    /**
     * A character the host's input method is putting *into the application*, rather than a keystroke
     * the platform delivered: the pre-edit it is showing in the field, or the text it committed.
     *
     * <p><b>It is always typed, even for a character a key carries</b>, because the keystroke that
     * would have carried it went to the input method instead and the application never saw it. A
     * character the keyboard carries goes on its own key - a key that was always there needs no
     * mapping change, and a client can resolve it however stale its own copy is - and anything else
     * goes on the keycode claimed for it. Shift goes around the key when the character is the shifted
     * half of the pair, which is the one case where "the key carries it" is not enough by itself.
     */
    public void inputMethodTyped(char character) {
        int keysym = Keymap.keysymFor(character);
        if (keysym == Keysyms.NONE) {
            return;
        }
        int onTheKeyboard = Keymap.keycodeForKeysym(keysym);
        if (onTheKeyboard != 0 && !Keymap.isClaimed(onTheKeyboard)) {
            boolean shifted = Keymap.keysym(onTheKeyboard, 0) != keysym;
            JulLog.debug("input method typed '{}' on keycode {}{}", character, onTheKeyboard,
                    shifted ? " with shift" : "");
            pressKey(onTheKeyboard, shifted);
            return;
        }
        JulLog.debug("input method typed '{}' on a claimed keycode", character);
        typeComposed(keysym);
    }

    /**
     * Puts {@code keysym} on the keycode claimed for it and presses that key.
     *
     * <p>The mapping first, then the key: a client that has not re-read the mapping yet would read
     * the keycode as whatever it used to carry.
     */
    private void typeComposed(int keysym) {
        server.lockServer();
        try {
            int keycode = Keymap.claimKeycodeFor(keysym);
            Keymap.assign(keycode, new int[] {keysym, keysym});
            server.broadcastMappingNotify((byte) 0, (byte) keycode, (byte) 1);
            pressKey(keycode, false);
        } finally {
            server.unlockServer();
        }
    }

    /**
     * One key down and up, with Shift held around it when the character is the shifted half of its
     * key. The key pair goes the way every key does, so a frozen keyboard holds it - the mapping
     * change is not the client's to defer, but the keystroke is.
     */
    private void pressKey(int keycode, boolean shiftHeld) {
        int shift = Keymap.keycodeForKeysym(Keysyms.SHIFT_L);
        if (shiftHeld && shift != 0) {
            deliver(new Held(false, KIND_KEY, shift, true, reportedX(), reportedY()));
        }
        deliver(new Held(false, KIND_KEY, keycode, true, reportedX(), reportedY()));
        deliver(new Held(false, KIND_KEY, keycode, false, reportedX(), reportedY()));
        if (shiftHeld && shift != 0) {
            deliver(new Held(false, KIND_KEY, shift, false, reportedX(), reportedY()));
        }
    }

    /**
     * Files the user dropped on the host window, which the drag bridge turns into an XDND drag for
     * whichever application is under the pointer. Called from the toolkit's thread, so it takes the
     * server lock like the rest of this class.
     */
    @Override
    public void hostDrop(List<String> paths, int x, int y) {
        cn.oyzh.fx.xserver.drop.DropBridge bridge = server.dropBridge();
        if (bridge == null) {
            return;
        }
        server.lockServer();
        try {
            movePointer(x, y, false);
            bridge.hostDropped(paths, x, y);
        } finally {
            server.unlockServer();
        }
    }

    public void keyChanged(int keycode, boolean pressed, int x, int y) {
        if (keycode < (Keymap.MIN_KEYCODE & 0xFF) || keycode > (Keymap.MAX_KEYCODE & 0xFF)) {
            return;
        }
        server.screenSaver().noteInput();
        server.lockServer();
        try {
            rawKey(keycode, pressed);
            // The repeat follows the *device's* key state, not what was delivered: X cancels a
            // key's repeat as the release is enqueued, and the difference is not academic - a
            // device frozen with a released key that still repeated would fill the queue at 25
            // events a second for as long as the client kept it frozen (measured: 372 events).
            if (pressed) {
                armRepeat(keycode);
            } else {
                repeatDue.remove(keycode);
            }
            deliver(new Held(false, KIND_KEY, keycode, pressed, x, y));
        } finally {
            server.unlockServer();
        }
    }

    /** One raw event, when the extension is registered - it always is, but a test may not. */
    private void rawMotion(int x, int y) {
        cn.oyzh.fx.xserver.extension.XInputExtension xinput = server.xinput();
        if (xinput != null) {
            // **This screen's root**, not the server's: a client is told about the screen it was
            // given (see `ConnectionSetup.sendSetup`), and in per-client mode that is a screen of its
            // own - so a raw event delivered to the wrong root reaches nobody, which is exactly how
            // this behaved first (measured: the selection logged on 0x9 while the delivery looked at
            // 0x2).
            xinput.rawMotion(screen.root(), sourceOf(XInputMasks.POINTER_DEVICE), x, y);
        }
    }

    private void rawButton(int button, boolean pressed) {
        cn.oyzh.fx.xserver.extension.XInputExtension xinput = server.xinput();
        if (xinput != null) {
            xinput.rawButton(screen.root(), sourceOf(XInputMasks.POINTER_DEVICE), button, pressed);
        }
    }

    private void rawKey(int keycode, boolean pressed) {
        cn.oyzh.fx.xserver.extension.XInputExtension xinput = server.xinput();
        if (xinput != null) {
            xinput.rawKey(screen.root(), sourceOf(XInputMasks.KEYBOARD_DEVICE), keycode, pressed);
        }
    }

    /** The device a raw event names as its source: the one that generated it, or the device. */
    private int sourceOf(int device) {
        return sourceDevice == 0 ? device : sourceDevice;
    }

    /** The key's own work, once {@link #deliver} has decided it is this device's turn. */
    private void processKey(Held event, boolean fromQueue) {
        int keycode = event.detail();
        boolean pressed = event.pressed();
        if (fromQueue) {
            notePointerWindow();
        } else {
            movePointer(event.x(), event.y(), false);
        }
        // **The state an event carries is the state before this key's own effect.** Measured
        // against Xvfb (2026-09-28): a Control press arrives with state 0x00 and its release with
        // 0x04, while a plain key pressed *while* Control is held carries 0x04 - so a modifier key's
        // own press does not include itself and its release still does. Taking the state after the
        // update gave the mirror image of that (0x04 and then 0x00), which a client that tracks
        // modifiers from the state field alone reads one event early.
        int stateBefore = pointerState();
        updateModifiers(keycode, pressed);
        if (pressed && keyboardGrab == null) {
            // A passive key grab fires for the focus window's chain, and it *consumes* the press:
            // the client that armed it gets the event and the focused window does not, which is
            // the whole point of a global shortcut.
            InputHandlers.PassiveGrab passive = matchingPassiveGrab(false, focusWindow(), keycode,
                    modifierState());
            if (passive != null) {
                activatePassiveGrab(passive, keycode, modifierState(), event);
            }
        }
        deliverKey(keycode, pressed, stateBefore);
        if (!pressed && passiveGrabDetail == keycode) {
            endPassiveGrab();
        }
    }

    @Override
    public void focusChanged(boolean focused) {
        // Losing the host window's focus does not change the X input focus: clients keep theirs.
    }

    /**
     * A size the user asked for by dragging the host window's edge, waiting for the display tick to
     * apply it.
     *
     * <p>Recorded rather than applied: this arrives on the toolkit's thread, and resizing a client's
     * window means taking the server lock and walking the window tree, which the tick is doing at
     * the same time. The tick picks it up and resizes the client's window itself.
     */
    private volatile java.awt.Dimension requestedSize;

    /** The size a maximize or a restore asked for, which wins over {@link #requestedSize} once. */
    private volatile java.awt.Dimension maximizedSize;
    /** Where the user last dragged the host window, reported by the backend. */
    private volatile java.awt.Point hostWindowPosition;

    @Override
    public void windowResized(int width, int height) {
        if (width > 0 && height > 0) {
            requestedSize = new java.awt.Dimension(width, height);
        }
    }

    /**
     * The host window was maximized or un-maximized, and this is the size the client's window should
     * take - the display's usable area, or the size the window had before.
     *
     * <p><b>Kept apart from {@link #windowResized} on purpose.</b> The platform reports its own idea
     * of the maximized geometry through the ordinary resize path as well - on macOS a forwarded
     * window's "standard frame" is worked out from the content, so it is the size the client's window
     * already had (measured 2026-10-03: a double click on the title bar produced a resize to
     * 1280x886, the client's width, before the real target arrived) - and the last request written
     * wins. This one wins over those instead, once, and a later drag is an ordinary resize again.
     */
    @Override
    public void windowMaximized(boolean maximized, int width, int height) {
        if (!maximized) {
            maximizedSize = null;
        }
        if (width > 0 && height > 0) {
            maximizedSize = new java.awt.Dimension(width, height);
        }
    }

    /** The size the user last dragged the host window to, clearing it. Null when there is none. */
    public java.awt.Dimension takeRequestedSize() {
        java.awt.Dimension maximized = maximizedSize;
        maximizedSize = null;
        if (maximized != null) {
            requestedSize = null;
            return maximized;
        }
        java.awt.Dimension requested = requestedSize;
        requestedSize = null;
        return requested;
    }

    /**
     * The user moved the host window. Called from the toolkit's thread, like
     * {@link #windowResized}, so it only records where the window is: the display tick is what
     * remembers it, because that is where the server's own bookkeeping happens and where a write to
     * a file is not holding anybody up.
     */
    @Override
    public void windowMoved(int x, int y) {
        hostWindowPosition = new java.awt.Point(x, y);
    }

    /** Where the host window is now, or null when it has not been moved since the server started. */
    public java.awt.Point hostWindowPosition() {
        return hostWindowPosition;
    }

    // ------------------------------------------------------------------
    // Pointer
    // ------------------------------------------------------------------

    private void movePointer(int x, int y, boolean generateMotion) {
        // A running device's physical position and the pointer the clients see are the same thing;
        // they only come apart while one is frozen, which is what {@link #deviceX} is for.
        deviceX = clampX(x);
        deviceY = clampY(y);
        moveSprite(deviceX, deviceY, generateMotion);
    }

    /**
     * Moves the pointer the clients see, leaving the device's own position alone.
     *
     * <p>What playing a held motion does: the sprite catches up with a position the device reached
     * while it was frozen, and the events that come out of the queue are still stamped with where the
     * device is <em>now</em> - which is what a reference server delivers, measured, and the reason
     * two held motions both arrive carrying the last position rather than their own.
     */
    private void moveSprite(int x, int y, boolean generateMotion) {
        pointerKnown = true;
        int clampedX = clampX(x);
        int clampedY = clampY(y);
        boolean moved = clampedX != pointerX || clampedY != pointerY;
        pointerX = clampedX;
        pointerY = clampedY;

        XWindow window = windowAtPointer();
        pointerWindow = window;
        if (moved) {
            rememberMotion(clampedX, clampedY, window);
        }
        if (moved && window != announcedWindow) {
            // Only a pointer that really moved reports a crossing. A button or key event recomputes
            // the window under the pointer too - that is how a window that appeared underneath is
            // noticed - and reporting the crossing there would tell a client its pointer had moved
            // when it had not, in the middle of handling a button: a menu that is opening takes that
            // for its popup being dismissed. What the client is owed is remembered instead, and
            // reported when the gesture that pinned the pointer is over.
            reportPointerWindow(window);
        }
        if (generateMotion && moved) {
            deliverMotion();
        }
    }

    /**
     * Arms the repeat for a key that has just gone down.
     *
     * <p>Two things decide it, and both are the client's to change: the keyboard's repeat control
     * (XKB's {@code RepeatKeys}) and the key's own bit in the per-key bitmap - which is where "a
     * modifier does not repeat" comes from, rather than a rule applied here. A held Shift repeating
     * would be a run of presses no toolkit expects.
     */
    private void armRepeat(int keycode) {
        if (!repeatKeysEnabled || !autoRepeatsOn(keycode)) {
            return;
        }
        repeatDue.put(keycode, System.currentTimeMillis() + repeatDelayMillis);
    }

    /**
     * Delivers the key repeats that have come due. Called from the display tick, under the server
     * lock, once per frame.
     *
     * <p>One repeat per key per call, never a catch-up burst: a tick that was late - a stalled
     * screen, a machine that slept - would otherwise deliver a run of presses at once, which a
     * terminal prints as a line of characters nobody typed.
     */
    public void pollAutoRepeat(long nowMillis) {
        if (repeatDue.isEmpty()) {
            return;
        }
        for (java.util.Map.Entry<Integer, Long> entry : new ArrayList<>(repeatDue.entrySet())) {
            int keycode = entry.getKey();
            long due = entry.getValue();
            if (nowMillis < due) {
                continue;
            }
            long next = due + repeatIntervalMillis;
            if (next <= nowMillis) {
                // Late enough that the tick missed a whole interval: resume from now.
                next = nowMillis + repeatIntervalMillis;
            }
            repeatDue.put(keycode, next);
            // A repeat is a key press like any other, so a frozen keyboard holds it back too.
            deliver(new Held(false, KIND_KEY, keycode, true, reportedX(), reportedY()));
        }
    }

    /** Whether a keycode repeats when it is held, and the keyboard is repeating at all. */
    public boolean autoRepeats(int keycode) {
        return repeatKeysEnabled && autoRepeatsOn(keycode);
    }

    /** Whether the per-key bitmap says this keycode repeats, whatever the keyboard's control is. */
    public boolean autoRepeatBit(int keycode) {
        return autoRepeatsOn(keycode);
    }

    /** Whether the keyboard repeats at all: XKB's {@code RepeatKeys} control. */
    public boolean repeatKeysEnabled() {
        return repeatKeysEnabled;
    }

    /**
     * Turns the whole keyboard's repeat on or off, which is XKB's {@code RepeatKeys} control.
     *
     * <p>The per-key bitmap is left alone, because a client that disables RepeatKeys is not saying
     * "forget which keys repeat" - it is saying "not now", and a client that enables it again expects
     * the keys it configured.
     */
    public void setRepeatKeysEnabled(boolean on) {
        repeatKeysEnabled = on;
        if (!on) {
            repeatDue.clear();
        }
    }

    /** How long a key is held before it repeats, in milliseconds. */
    public int repeatDelayMillis() {
        return repeatDelayMillis;
    }

    /** How long between repeats, in milliseconds. */
    public int repeatIntervalMillis() {
        return repeatIntervalMillis;
    }

    /**
     * Changes how fast a held key repeats: XKB's {@code XkbSetAutoRepeatRate}, and the only way a
     * client can reach these - the core protocol has no request for either.
     *
     * <p>A key that is already repeating keeps its current due time: the new rate applies from the
     * next repeat on, which is what a reference server does.
     */
    public void setRepeatRate(int delayMillis, int intervalMillis) {
        if (delayMillis > 0) {
            repeatDelayMillis = delayMillis;
        }
        if (intervalMillis > 0) {
            repeatIntervalMillis = intervalMillis;
        }
    }

    /**
     * The per-key repeat bitmap in the wire's own form: 32 bytes, one bit per keycode, the least
     * significant bit of each byte first. This is the same array {@code GetKeyboardControl} and
     * XKB's {@code GetControls} both carry.
     */
    public byte[] autoRepeatBitmap() {
        byte[] bits = new byte[32];
        for (int keycode = 0; keycode <= Keymap.MAX_KEYCODE; keycode++) {
            if (autoRepeatsOn(keycode)) {
                bits[keycode >> 3] |= (byte) (1 << (keycode & 7));
            }
        }
        return bits;
    }

    /**
     * Replaces the per-key repeat bitmap from the wire's form, which is what XKB's
     * {@code SetControls} carries. A key that has just stopped repeating stops at once rather than
     * finishing its run.
     */
    public void setAutoRepeatBitmap(byte[] bits) {
        boolean[] custom = new boolean[Keymap.MAX_KEYCODE + 1];
        for (int keycode = 0; keycode <= Keymap.MAX_KEYCODE; keycode++) {
            int index = keycode >> 3;
            custom[keycode] = index < bits.length && (bits[index] & (1 << (keycode & 7))) != 0;
        }
        perKeyCustom = custom;
        repeatDue.keySet().removeIf(keycode -> !autoRepeatsOn(keycode));
    }

    /**
     * Turns repeating on or off, for one keycode or for every key.
     *
     * <p>The two are different things, and that was measured rather than assumed: naming no keycode
     * is the keyboard's own repeat control, not a per-key bit - against Xvfb, {@code xset r off}
     * clears the {@code enabledControls} RepeatKeys bit and leaves the per-key bitmap exactly as it
     * was, so a client that turns repeat off and on again gets back the keys it had configured.
     *
     * @param keycode the key, or -1 for the whole keyboard (which is what a client that names no
     *                keycode means)
     * @param on      true for repeat, false for a key that presses once however long it is held
     */
    public void setAutoRepeat(int keycode, boolean on) {
        if (keycode < 0) {
            setRepeatKeysEnabled(on);
            return;
        }
        if (keycode > Keymap.MAX_KEYCODE) {
            return;
        }
        // A single key, on top of whatever the keymap in force says for the others: the client's own
        // map is materialised first, so the keys it did not name keep following the layout.
        if (perKeyCustom == null) {
            boolean[] custom = new boolean[Keymap.MAX_KEYCODE + 1];
            for (int kc = 0; kc <= Keymap.MAX_KEYCODE; kc++) {
                custom[kc] = autoRepeatsOn(kc);
            }
            perKeyCustom = custom;
        }
        perKeyCustom[keycode] = on;
        if (!on) {
            repeatDue.remove(keycode);
        }
    }

    /**
     * Adds one motion to the pointer's history, dropping the oldest when it is full.
     */
    private void rememberMotion(int x, int y, XWindow window) {
        motionHistory.addLast(new Motion(server.events().time(), x, y, window));
        while (motionHistory.size() > MOTION_HISTORY) {
            motionHistory.removeFirst();
        }
    }

    /**
     * The remembered motions inside {@code window} between two times, oldest first.
     *
     * <p>The times are the client's own - the ones its motion events carried - so they are compared
     * the way X compares {@code Time}: as 32 bit values that wrap, and with a range that wraps with
     * them (a client that asks for a range across the wrap gets it). An empty range - {@code start}
     * equal to {@code stop} - is the "how many are there" question, and answers none.
     */
    public List<Motion> motionHistory(XWindow window, long start, long stop) {
        List<Motion> found = new ArrayList<>();
        for (Motion motion : motionHistory) {
            if (motion.window() == window && inTimeRange(motion.time(), start, stop)) {
                found.add(motion);
            }
        }
        return found;
    }

    /** Whether a 32 bit timestamp falls in the half-open range, allowing for the wrap. */
    private static boolean inTimeRange(int time, long start, long stop) {
        long from = start & 0xFFFFFFFFL;
        long until = stop & 0xFFFFFFFFL;
        long at = time & 0xFFFFFFFFL;
        if (from == until) {
            return false;
        }
        return from < until ? at >= from && at < until : at >= from || at < until;
    }

    /**
     * The crossing events for a window that appeared, moved, was raised or went away under a
     * stationary pointer.
     *
     * <p>The pointer has not moved, but the window it is in has, and a client only ever learns
     * which window the pointer is in from crossing events. A window that goes away under the
     * pointer is the case this exists for: without it the pointer stays pinned to a window that no
     * longer exists, and the next click is reported against nothing at all.
     *
     * <p>A window that <em>appears</em> under the pointer is deliberately not reported here. Its
     * client is usually mid-gesture - a menu popping up as the pointer presses on the item that
     * opens it - and the crossing that arrives out of the middle of that gesture is read as the
     * pointer having left the window the gesture belongs to, which tears the menu down as fast as
     * it went up. The change is picked up by the move that follows instead, which is where a
     * client expects to learn where its pointer is.
     */
    public void refreshPointerWindow() {
        if (!pointerKnown) {
            // Nothing has said where the pointer is yet, and the origin is not a position that a
            // window appeared under.
            return;
        }
        XWindow window = windowAtPointer();
        if (window != announcedWindow && JulLog.isDebugEnabled()) {
            JulLog.debug("the window under the pointer is now 0x{}: the tree changed, not the pointer",
                    window == null ? "none" : Integer.toHexString(window.id()));
        }
        settlePointerWindow();
    }

    private void notePointerWindow() {
        if (pointerKnown) {
            pointerWindow = windowAtPointer();
        }
    }

    /**
     * Reports the crossing a client is owed for a window change that happened without the pointer
     * moving, if one is still outstanding.
     *
     * <p>Two things need this. A window that goes away under the pointer leaves the client pinning
     * events to a window that no longer exists, so the change is reported at once. A window that
     * appears under the pointer - the menu a press just opened - cannot be reported while the button
     * that opened it is still down, because the implicit grab withholds the enter, and the crossing
     * is owed until the button comes up; that is where a reference server reports it too.
     */

    public void settlePointerWindow() {
        if (!pointerKnown) {
            return;
        }
        XWindow window = windowAtPointer();
        pointerWindow = window;
        if (window == announcedWindow) {
            return;
        }
        reportPointerWindow(window);
    }

    /**
     * Delivers the crossing pair that says where the pointer is now, and remembers what the client
     * was actually told.
     *
     * <p>Where the pointer is and what the client knows are not the same thing once a grab is
     * involved: a grab can withhold the enter into the window the pointer reached (see
     * {@link #confinedToGrab}), and a client that hears the leave but not the enter has been told the
     * pointer is in none of its windows. That is remembered as such, so the enter is still owed and
     * goes out when the grab lets go rather than being written off.
     */
    private void reportPointerWindow(XWindow window) {
        XWindow inside = confinedToGrab();
        boolean entered = window != null
                && (inside == null || inside == window || inside.isAncestorOf(window));
        XWindow previous = announcedWindow;
        announcedWindow = entered ? window : null;
        server.events().deliverCrossing(previous, window, pointerX, pointerY, pointerX, pointerY,
                inside);
    }

    /**
     * The window crossing events are confined to while the pointer is grabbed, or null when they go
     * everywhere.
     *
     * <p>A grab that reports events to itself takes the pointer over: only the grab window and what
     * is inside it hear where the pointer went. That covers the implicit grab a press starts as
     * well, which is what keeps a window that merely appears under a dragged pointer from behaving
     * as though the pointer entered it - and it is why a popup that a menu maps under the pointer
     * while the button is still down is not entered until the button comes up.
     *
     * <p>A grab with owner_events confines nothing: the grabbing client goes on receiving the
     * crossings of its own windows, and confining those would leave a menu that grabbed the pointer
     * on its main window never learning that the pointer reached one of the menu's own popups,
     * which is where it takes its highlight from.
     */
    private XWindow confinedToGrab() {
        if (implicitGrabWindow != null) {
            // A button is down: the press pinned the pointer to the window it landed on, and that is
            // where the pointer stays until the button comes up. A menu whose popup is mapped under
            // the pointer by the press that opened it is the case this is about - the reference
            // server reports the leave from the window underneath and withholds the enter until the
            // button is released, and a toolkit that is told the popup was entered in the middle of
            // the gesture tears the menu down.
            return implicitGrabWindow;
        }
        if (pointerGrab != null) {
            return pointerGrab.ownerEvents() ? null : pointerGrab.window();
        }
        return null;
    }

    private void deliverMotion() {
        Grab grab = pointerGrab;
        if (grab != null && !grab.ownerEvents()) {
            deliverToGrab(grab, EventMasks.POINTER_MOTION, motionFactory());
            return;
        }
        if (grab != null && deliverToGrabbingClient(grab, EventMasks.POINTER_MOTION,
                motionFactory())) {
            return;
        }
        server.events().deliverPropagating(grabbedWindow(), EventMasks.POINTER_MOTION, reportedX(),
                reportedY(), motionFactory());
    }

    /**
     * Delivers an event for a grab that reports the client's own windows, and says whether it was
     * one of them: a motion, click or key that lands anywhere else - on the root, on another
     * client's window - belongs to the grabbing client all the same, and is reported against the
     * window it holds the grab on.
     *
     * <p>A menu with the pointer grabbed depends on it. Without this the motion that takes the
     * pointer off the menu goes nowhere at all, so the menu hears that the pointer left and then
     * nothing more, and a click outside it - which is how it learns to close - is never delivered
     * either.
     */
    private boolean deliverToGrabbingClient(Grab grab, int maskBit, EventFactory factory) {
        XWindow window = grabbedWindow();
        if (window != null && window.owner() == grab.client()) {
            return false;                       // the client's own window: the normal path applies
        }
        if ((grab.eventMask() & maskBit) != 0) {
            deliverToGrab(grab, maskBit, factory);
        }
        return true;
    }

    /**
     * The window pointer events are reported against: the one a button press pinned the pointer to
     * while the implicit grab lasts, otherwise the one under the pointer.
     *
     * <p>An explicit grab that reports events to the client's own windows supersedes the implicit
     * one. A popup that appears while a button is still held is exactly that case: the press pins
     * the pointer to the window underneath, the client then grabs the pointer for the popup it just
     * created, and the release has to be reported against the popup - reporting it against the
     * window the press landed on is a release the menu reads as a click outside itself, which
     * dismisses it.
     */
    private XWindow grabbedWindow() {
        if (pointerGrab != null) {
            return pointerWindow;
        }
        return implicitGrabWindow != null ? implicitGrabWindow : pointerWindow;
    }

    /**
     * The window a pointer event is reported against right now: the active grab's window when the
     * grab reports events as though the client had selected them itself, otherwise the window the
     * pointer is pinned to.
     */
    private XWindow eventTarget() {
        Grab grab = pointerGrab;
        if (grab != null && !grab.ownerEvents()) {
            return grab.window();
        }
        return grabbedWindow();
    }

    /**
     * Releases every grab held on a window that is going away.
     *
     * <p>X11 releases an active grab when its grab window is destroyed, and clients rely on it: one
     * that grabs the pointer for a modal dialog and then lets the dialog be destroyed never sends
     * {@code UngrabPointer}, so the grab has to end here. A grab left pointing at a window that no
     * longer exists is worse than a stuck one - every pointer event is reported against nothing, and
     * the pointer goes dead for every client while the keyboard, which follows the focus instead,
     * keeps working.
     */
    public void forgetGrabWindow(XWindow window) {
        if (implicitGrabWindow == window) {
            implicitGrabWindow = null;
        }
        if (pointerGrab != null && pointerGrab.window() == window) {
            pointerGrab = null;
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("released the pointer grab held on 0x{}: the window went away",
                        Integer.toHexString(window.id()));
            }
        }
        if (keyboardGrab != null && keyboardGrab.window() == window) {
            keyboardGrab = null;
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("released the keyboard grab held on 0x{}: the window went away",
                        Integer.toHexString(window.id()));
            }
        }
    }

    private EventFactory motionFactory() {
        return (client, w, child, x, y) -> MotionNotifyEvent.builder()
                .firstEventOffset((byte) 0)
                .sentEvent(false)
                .sequenceNumber(client.sequenceNumber())
                .detail((byte) 0)                       // Normal
                .time(server.events().time())
                .root(screen.root().id())
                .event(w.id())
                .child(child == null ? 0 : child.id())
                .rootX((short) reportedX())
                .rootY((short) reportedY())
                .eventX((short) x)
                .eventY((short) y)
                .state((short) pointerState())
                .sameScreen(true)
                .build();
    }

    private void deliverButton(int button, boolean pressed, int mask) {
        int eventMask = pressed ? EventMasks.BUTTON_PRESS : EventMasks.BUTTON_RELEASE;
        // The state field reports the buttons held *before* this event.
        short stateBefore = (short) (pointerState() & ~(mask << BUTTON_MASK_SHIFT));
        short stateAfter = (short) (pointerState() | (mask << BUTTON_MASK_SHIFT));
        EventFactory factory = (client, w, child, x, y) -> {
            short state = pressed ? stateBefore : stateAfter;
            int childId = child == null ? 0 : child.id();
            int time = server.events().time();
            XEvent event;
            if (pressed) {
                event = ButtonPressEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(client.sequenceNumber())
                        .detail((byte) button)
                        .time(time)
                        .root(screen.root().id())
                        .event(w.id())
                        .child(childId)
                        .rootX((short) reportedX())
                        .rootY((short) reportedY())
                        .eventX((short) x)
                        .eventY((short) y)
                        .state(state)
                        .sameScreen(true)
                        .build();
            } else {
                event = ButtonReleaseEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(client.sequenceNumber())
                        .detail((byte) button)
                        .time(time)
                        .root(screen.root().id())
                        .event(w.id())
                        .child(childId)
                        .rootX((short) reportedX())
                        .rootY((short) reportedY())
                        .eventX((short) x)
                        .eventY((short) y)
                        .state(state)
                        .sameScreen(true)
                        .build();
            }
            return event;
        };
        Grab grab = pointerGrab;
        if (grab != null && !grab.ownerEvents()) {
            deliverToGrab(grab, eventMask, factory);
            updateImplicitGrab(pressed);
            return;
        }
        if (grab != null && deliverToGrabbingClient(grab, eventMask, factory)) {
            updateImplicitGrab(pressed);
            return;
        }
        server.events().deliverPropagating(grabbedWindow(), eventMask, reportedX(), reportedY(),
                factory);
        updateImplicitGrab(pressed);
    }

    /**
     * The passive grab a press should fire, or null when none of them matches.
     *
     * <p>{@code GrabButton}/{@code GrabKey} record "when this button (or key) is pressed on this window
     * or anything inside it, I want the input", and this is where that is honoured. The window the
     * event landed on is tried first and its ancestors after it, so a grab on a frame beats one on the
     * root - which is how a window manager's frame buttons work.
     *
     * <p>The rules, and all of them were measured against a reference server with
     * {@code tools/GrabProbe}:
     *
     * <ul>
     *   <li><b>Modifiers</b>: {@code AnyModifier} (0x8000) matches any state; a specific modifier
     *       matches only a state that equals it exactly. Measured: a grab armed with {@code ShiftMask}
     *       fires with Shift held and does not fire without it.
     *   <li><b>The detail</b>: {@code AnyButton}/{@code AnyKey} (0) matches every button or key; a
     *       specific one matches only itself.
     *   <li><b>Two arms that both match</b>: the one with a specific modifier beats {@code AnyModifier}
     *       - the closest match - and among equals the last armed wins, which is what
     *       {@code GrabButton}'s "an existing grab with the same parameters is replaced" amounts to.
     *   <li><b>A window that is not mapped never fires</b>: a grab on a window nobody can see is not
     *       something the user can press.
     * </ul>
     *
     * @param pointer true for a button, false for a key
     * @param from the window the event landed on, or the focus window for a key
     */
    private InputHandlers.PassiveGrab matchingPassiveGrab(boolean pointer, XWindow from, int detail,
                                                          int modifiers) {
        return matchingPassiveGrab(pointer, from, detail, modifiers, passiveGrabFloor);
    }

    /**
     * The same, stopping below a window the search may not look at.
     *
     * <p>That bound is what a replay needs: the event a {@code Replay*} mode reprocesses is not
     * allowed to fire the grab it was taken from again, so the search starts at the window
     * <em>below</em> the one that grab was held on. A reference server does it the same way, and the
     * reason is visible in the measurement - the replayed press reaches the window underneath as an
     * ordinary event, without the {@code NotifyGrab} crossing a second firing would bring.
     */
    private InputHandlers.PassiveGrab matchingPassiveGrab(boolean pointer, XWindow from, int detail,
                                                          int modifiers, XWindow floor) {
        if (floor != null && !isAtOrBelow(floor, from)) {
            // The window the replayed grab was held on is not above the event's window at all, which
            // is what a reference server answers with "no grab can apply" rather than a wider search.
            return null;
        }
        for (XWindow window = from; window != null; window = window.parent()) {
            if (window == floor) {
                break;
            }
            if (!window.isMapped() && !window.isRoot()) {
                // The window itself is not on screen; its ancestors are still worth trying, which is
                // what continuing the walk does.
                continue;
            }
            InputHandlers.PassiveGrab best = null;
            for (InputHandlers.PassiveGrab grab : server.passiveGrabs()) {
                if (grab.pointer() != pointer || grab.window() != window) {
                    continue;
                }
                if (grab.detail() != 0 && grab.detail() != detail) {
                    continue;
                }
                if (!modifiersMatch(grab.modifiers(), modifiers)) {
                    continue;
                }
                if (best == null || closerMatch(grab.modifiers(), best.modifiers())) {
                    best = grab;
                }
            }
            if (best != null) {
                return best;
            }
            if (window.isRoot()) {
                break;
            }
        }
        return null;
    }

    /** A modifier list matches when it is AnyModifier, or when the state equals it exactly. */
    private static boolean modifiersMatch(int wanted, int state) {
        return wanted == ANY_MODIFIER || wanted == state;
    }

    /**
     * Which of two matching arms is the closer one. A specific modifier beats {@code AnyModifier}, and
     * two of the same kind are equally close - the last one armed then wins, which is the order the
     * list is in.
     */
    private static boolean closerMatch(int candidate, int current) {
        return candidate != ANY_MODIFIER && current == ANY_MODIFIER;
    }

    /**
     * Takes the grab a passive one asks for: it is an ordinary active grab from the press to the
     * release, and the clients are told the crossing pair a reference server sends for it.
     *
     * <p>{@code event} is what the client did to make it fire, and it is not only for the log: a
     * passive grab that asked for {@code Sync} freezes the device <b>on that event</b>, and that is
     * the event a {@code Replay*} mode reprocesses - see X's {@code ActivateGrabNoDelivery}.
     */
    private void activatePassiveGrab(InputHandlers.PassiveGrab passive, int detail, int modifiers,
                                     Held event) {
        Grab grab = new Grab(passive.client(), passive.window(), passive.ownerEvents(),
                passive.eventMask(), !passive.pointer(), passive.pointerMode(),
                passive.keyboardMode(), (int) server.events().time());
        passiveGrabDetail = detail;
        passiveGrabModifiers = modifiers;
        if (passive.pointer()) {
            setPointerGrab(grab);
        } else {
            setKeyboardGrab(grab);
        }
        Sync sync = passive.pointer() ? pointerSync : keyboardSync;
        if (sync.state == FROZEN_NO_EVENT && event != null) {
            sync.state = FROZEN_WITH_EVENT;
            sync.event = event;
        }
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("passive {} grab on 0x{} fired by {}: the input belongs to {} until the release"
                            + " ({})", passive.pointer() ? "button" : "key",
                    Integer.toHexString(passive.window().id()), detail, passive.client(),
                    syncName(sync));
        }
        // The pair a grab's start implies: the pointer leaves the window it was in and enters the
        // grab window, with the mode that says a grab did it. A reference server sends this for a
        // passive grab (measured), and §7's deliberate silence is about *active* ones.
        if (passive.pointer()) {
            server.events().deliverGrabCrossing(pointerWindow, passive.window(), grabCrossing(true));
        }
    }

    /** Gives back the grab a passive one took, and reports the crossing pair that ends it. */
    private void endPassiveGrab() {
        Grab grab = passiveGrabDetail == 0 ? null
                : (pointerGrab != null && !pointerGrab.keyboard() ? pointerGrab
                        : keyboardGrab != null && keyboardGrab.keyboard() ? keyboardGrab : null);
        passiveGrabDetail = 0;
        if (grab == null) {
            return;
        }
        endGrab(grab, true);
    }

    /**
     * Ends a grab and lets the device it froze go.
     *
     * <p>{@code passive} is what the grab owed its clients on the way out: a passive one reports the
     * crossing pair that says the grab is over, an active one reports nothing (§7's deliberate
     * silence, which is where the menus are), and the pair comes <em>before</em> the release because
     * the release is what hands the held events back and the client has to be told the grab ended
     * before it is sent what the grab was holding.
     */
    private void endGrab(Grab grab, boolean passive) {
        if (passive) {
            passiveGrabDetail = 0;
            if (!grab.keyboard()) {
                server.events().deliverGrabCrossing(pointerWindow, grab.window(),
                        grabCrossing(false));
            }
        }
        if (grab.keyboard()) {
            keyboardGrab = null;
            grabReleased(grab);
        } else {
            pointerGrab = null;
            grabReleased(grab);
        }
    }

    /** Whether {@code window} is {@code from} itself or one of its ancestors. */
    private static boolean isAtOrBelow(XWindow window, XWindow from) {
        for (XWindow candidate = from; candidate != null; candidate = candidate.parent()) {
            if (candidate == window) {
                return true;
            }
        }
        return false;
    }

    /**
     * The mode of an event a grab's own crossings carry, numbered as the protocol numbers them.
     *
     * <p>{@code NotifyGrab} on the way in and {@code NotifyUngrab} on the way out: a client that reads
     * the mode tells "the pointer entered because somebody grabbed it" from "because the user moved
     * it", and a menu that grabs the pointer depends on the difference.
     */
    private static byte grabCrossing(boolean starting) {
        return starting ? EventDispatcher.NOTIFY_MODE_GRAB : EventDispatcher.NOTIFY_MODE_UNGRAB;
    }

    /**
     * Starts the implicit grab when a press lands with no button yet down, and ends it when the last
     * button comes up - the two moments X11 pins the pointer to a window and lets it go again.
     */
    private void updateImplicitGrab(boolean pressed) {
        if (pressed) {
            if (implicitGrabWindow == null && pointerGrab == null) {
                implicitGrabWindow = pointerWindow;
            }
        } else if (buttons == 0) {
            implicitGrabWindow = null;
            // The pointer is the client's to follow again. A window that appeared over it while the
            // button was down - the menu the press opened - has been waiting for this moment to be
            // reported, because a crossing out of the middle of a gesture is read as the pointer
            // leaving the window the gesture belongs to.
            settlePointerWindow();
        }
    }

    // ------------------------------------------------------------------
    // Keyboard
    // ------------------------------------------------------------------

    private void updateModifiers(int keycode, boolean pressed) {
        int bit = Keymap.modifierMaskForKeycode(keycode);
        if (bit == 0) {
            return;
        }
        if (isMomentary(keycode)) {
            modifiers = pressed ? modifiers | bit : modifiers & ~bit;
            return;
        }
        // Caps Lock, Num Lock and Scroll Lock toggle and stay set after the key is released.
        if (pressed) {
            latched ^= bit;
            modifiers = (modifiers & ~bit) | (latched & bit);
        }
    }

    private static boolean isMomentary(int keycode) {
        for (int candidate : MOMENTARY_KEYCODES) {
            if (candidate == keycode) {
                return true;
            }
        }
        return false;
    }

    private void deliverKey(int keycode, boolean pressed, int state) {
        int eventMask = pressed ? EventMasks.KEY_PRESS : EventMasks.KEY_RELEASE;
        EventFactory factory = (client, w, child, x, y) -> {
            int childId = child == null ? 0 : child.id();
            int time = server.events().time();
            short eventState = (short) state;
            if (pressed) {
                return KeyPressEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(client.sequenceNumber())
                        .detail((byte) keycode)
                        .time(time)
                        .root(screen.root().id())
                        .event(w.id())
                        .child(childId)
                        .rootX((short) reportedX())
                        .rootY((short) reportedY())
                        .eventX((short) x)
                        .eventY((short) y)
                        .state(eventState)
                        .sameScreen(true)
                        .build();
            }
            return KeyReleaseEvent.builder()
                    .firstEventOffset((byte) 0)
                    .sentEvent(false)
                    .sequenceNumber(client.sequenceNumber())
                    .detail((byte) keycode)
                    .time(time)
                    .root(screen.root().id())
                    .event(w.id())
                    .child(childId)
                    .rootX((short) reportedX())
                    .rootY((short) reportedY())
                    .eventX((short) x)
                    .eventY((short) y)
                    .state(eventState)
                    .sameScreen(true)
                    .build();
        };
        Grab grab = keyboardGrab;
        if (grab != null && !grab.ownerEvents()) {
            deliverToGrab(grab, eventMask, factory);
            return;
        }
        if (grab != null) {
            XWindow focus = focusWindow();
            if (focus != null && focus.owner() != grab.client()
                    && (grab.eventMask() & eventMask) != 0) {
                deliverToGrab(grab, eventMask, factory);
                return;
            }
        }
        server.events().deliverPropagating(focusWindow(), eventMask, reportedX(), reportedY(),
                factory);
    }

    /**
     * The window key events are reported against.
     *
     * <p><b>A focus of the root window means the keys follow the pointer</b> - exactly what
     * {@code PointerRoot} means, and not what the prose of the specification suggests. The two states
     * are still two: {@code GetInputFocus} answers the root's own id for one and {@code PointerRoot}
     * for the other, a change between them reports a different chain of {@code FocusIn}/{@code FocusOut}
     * (see {@link EventDispatcher#deliverFocusChange}), and this field keeps them apart. What they have
     * in common is where a key goes, and it is the case a toolkit lands in without asking for it: a
     * {@code RevertToParent} out of a window whose parent is the root reverts to the root window, and a
     * client whose page or dialog has just been unmapped is left in that state.
     *
     * <p>Measured rather than reasoned, because the obvious reading of the specification says the keys
     * belong to the root: {@code tools/KeyTargetProbe} parks the pointer in a window of its own and
     * presses a key with the focus on the root - reached both by {@code SetInputFocus(root)} and by the
     * revert - and a reference server reports the {@code KeyPress} against the window the pointer is
     * in, byte for byte what it does with the focus on {@code PointerRoot}. Sending it to the root
     * window instead is invisible to any client diff of the focus events and is fatal to a text field:
     * the window under the pointer looks focused, the clicks keep working, and not one character
     * arrives. Only {@code None} leaves the keys with nowhere to go.
     */
    public XWindow focusWindow() {
        if (inputFocus == null) {
            // None: the keys belong to nobody and are dropped. Reporting them against the root would
            // hand them to a client that never asked for them.
            return null;
        }
        if (inputFocus.isRoot()) {
            // Both states that name the root: the keys belong to the window the pointer is in, and to
            // the root itself when the pointer is over no window at all - which is where a reference
            // server puts them too, the root being the window the pointer is then in.
            return pointerWindow == null ? screen.root() : pointerWindow;
        }
        return inputFocus;
    }

    /**
     * Moves the input focus to the window a click landed in - the window-manager half this server
     * leaves out unless {@code --focus-on-click} asks for it.
     *
     * <p>It focuses the **toplevel** the click landed in, with {@code revert_to = PointerRoot}: that is
     * what a window manager does, and the revert mode is the half that matters afterwards - a window
     * that goes away hands the focus back to the window the pointer is in rather than leaving it on
     * the root, which is the state an application cannot get itself out of (its own menus stop opening
     * there, because a toolkit hides the popup of an owner it believes is unfocused; measured
     * 2026-09-29 and identical on a reference server, so it is the application's behaviour and this is
     * the switch that gives it what a desktop would).
     *
     * <p>Nothing happens when the state is already what the click asks for: the window under the
     * pointer is inside the window that has the focus, or the focus is {@code PointerRoot}, where the
     * keys already follow the pointer and a client that asked for it would only see a change it did
     * not ask for.
     */
    private void focusOnClick() {
        if (!server.options().focusOnClick() || pointerWindow == null) {
            return;
        }
        XWindow target = pointerWindow;
        while (target.parent() != null && !target.parent().isRoot()) {
            target = target.parent();
        }
        // **An override-redirect toplevel is not focused** - a menu, a tooltip or any other popup an
        // application puts up for itself, and a window manager leaves the focus where it is for those
        // (one that needs the keyboard grabs it, which is what a menu bar or a launcher does). It is
        // not a nicety: the focus belongs to the window such a popup was opened *from*, so moving it
        // there takes the focus away from that window, and a toolkit hides a popup whose owner it
        // believes unfocused. Measured on EasyShell: the press went to the popup, the owner was told
        // `FocusOut`, and the menu was unmapped three milliseconds later - so every item of an input
        // box's context menu was dead to the mouse while the same item worked from the keyboard
        // (A23, 2026-09-30). With the focus left alone the press and the release both stay in the
        // popup and the item activates.
        if (target.isOverrideRedirect()) {
            return;
        }
        // A focus that is already on an ordinary window the click landed inside is left alone. The
        // root window and None are *not*: those are exactly the states a client cannot get out of by
        // itself - a click in a window while the focus sits on the root is what has to move it - and
        // PointerRoot already follows the pointer.
        if (inputFocus != null && !inputFocus.isRoot() && !focusIsPointerRoot
                && inputFocus.isAncestorOf(target)) {
            return;
        }
        setInputFocus(target, REVERT_TO_POINTER_ROOT, false);
    }

    /**
     * Reverts the input focus when the focused window is destroyed.
     *
     * <p>X11 reverts the focus the way the last {@code SetInputFocus} asked for - to the parent, to
     * PointerRoot or to nothing - and clients rely on it: a toolkit moves the focus to a dialog when
     * it opens one and lets the dialog be destroyed rather than putting the focus back itself. A
     * focus left on a window that no longer exists swallows every keystroke it was meant to deliver.
     */
    public void forgetFocusWindow(XWindow window) {
        if (inputFocus == null || inputFocus.isRoot()) {
            // PointerRoot follows the pointer and None is nowhere: neither is a window, so neither is
            // touched by a window going away. Moving the focus as the pointer crosses into another
            // window is the half of PointerRoot that is not implemented - see the design notes.
            return;
        }
        // A window that goes away takes the focus with it even when the focus is on something inside
        // it: the focused window stops being viewable then, and a client left sending keys to a
        // window nobody can see is a client whose typing has quietly stopped working.
        if (inputFocus != window && !window.isAncestorOf(inputFocus)) {
            return;
        }
        XWindow previous = inputFocus;
        inputFocus = revertTarget(previous);
        // Only RevertToPointerRoot leaves the focus following the pointer; RevertToParent landing on
        // the root leaves it on the root *window*, which is a different state - see the field.
        focusIsPointerRoot = focusRevertTo != REVERT_TO_PARENT && inputFocus != null
                && inputFocus.isRoot();
        // The focus moved, so both sides of the move are told. The window that was holding it is
        // named as it was even though it is on its way out: being told the focus left is the half
        // its client can still act on.
        server.events().deliverFocusChange(previous, false, inputFocus, focusIsPointerRoot,
                EventDispatcher.NOTIFY_MODE_NORMAL, false);
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("focus reverted from {} to {}: 0x{} went away", describe(previous),
                    describeFocus(inputFocus, focusIsPointerRoot), Integer.toHexString(window.id()));
        }
    }

    /**
     * Where the focus goes when the window holding it goes away, as the last {@code SetInputFocus}
     * asked for.
     *
     * <p>{@code RevertToParent} means the nearest ancestor that is still <em>viewable</em> rather
     * than the parent itself: a dialog and its parent are often on their way out together, and the
     * focus has to land somewhere a client can be told about. When only the root is left the focus
     * becomes PointerRoot, which is what the root window means throughout this class.
     */
    private XWindow revertTarget(XWindow focused) {
        if (focusRevertTo == REVERT_TO_NONE) {
            return null;
        }
        if (focusRevertTo != REVERT_TO_PARENT) {
            return screen.root();
        }
        for (XWindow window = focused.parent(); window != null; window = window.parent()) {
            if (window.isViewable()) {
                return window;
            }
        }
        return screen.root();
    }

    /** Delivers straight to a grabbing client's window, bypassing normal window selection. */
    private void deliverToGrab(Grab grab, int maskBit, EventFactory factory) {
        if ((grab.eventMask() & maskBit) == 0) {
            return;
        }
        XWindow window = grab.window();
        int x = reportedX() - window.absoluteX();
        int y = reportedY() - window.absoluteY();
        // The child of the grab window the pointer is in, which is what a press reported to a client
        // that owns the whole screen tells it *where* it was: a reference server names it, and a
        // client that takes a global shortcut uses it to decide which of its own windows was clicked.
        XWindow child = window.childAt(reportedX(), reportedY());
        server.events().send(grab.client(), factory.create(grab.client(), window, child, x, y));
    }

    // ------------------------------------------------------------------
    // Cursor sprite
    // ------------------------------------------------------------------

    /**
     * The sprite to draw over the screen at the current pointer position, or null when there is
     * none.
     *
     * <p>It is handed to the display backend rather than painted into the framebuffer - see
     * {@link CursorSprite} for why - so this is a description of what to draw: the top left corner
     * the cursor's hotspot puts the image at, and the image itself.
     *
     * <p>Pixels are cached per cursor. A backend asks for this once a frame, and a cursor's image
     * only changes when a client replaces it.
     */
    public CursorSprite cursorSprite() {
        if (!pointerKnown || cursorHiddenHere()) {
            return null;
        }
        XCursor cursor = cursorFor();
        if (cursor == null) {
            return null;
        }
        int width = cursor.width();
        int height = cursor.height();
        // The image version too: a cursor whose image was rewritten in place is the same cursor, and
        // the cached pixels would otherwise outlive the shape they were copied from (XFIXES'
        // ChangeCursor, see XCursor.adopt).
        if (cursorPixelsFor != cursor || cursorPixelsVersion != cursor.imageVersion()) {
            int[] pixels = new int[width * height];
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    pixels[row * width + col] = cursor.pixel(col, row);
                }
            }
            cursorPixels = pixels;
            cursorPixelsFor = cursor;
            cursorPixelsVersion = cursor.imageVersion();
        }
        return new CursorSprite(pointerX - cursor.hotspotX(), pointerY - cursor.hotspotY(),
                width, height, cursor.hotspotX(), cursor.hotspotY(), cursorPixels);
    }

    /**
     * The cursor the pointer is showing: the nearest one set on the window it is in or above it.
     *
     * <p>Distinct from {@link #cursorSprite()}, which is where to draw it: this is the identity a
     * client is told about when it changes, before anything is drawn and whether or not a frame goes
     * out, which is why XFixes can use it while the display is idle. Null until the pointer's
     * position is known, for the same reason the sprite is: until then there is no window under it to
     * ask.
     */
    public XCursor displayedCursor() {
        return pointerKnown ? cursorFor() : null;
    }

    private XCursor cursorFor() {
        for (XWindow window = pointerWindow; window != null; window = window.parent()) {
            XCursor cursor = window.cursor();
            if (cursor != null) {
                return cursor;
            }
        }
        return screen.rootCursor();
    }

    /**
     * Whether the pointer's own window - or a window above it - asked for the cursor to be hidden
     * ({@code XFixesHideCursor}).
     *
     * <p>The walk is the one {@link #cursorFor()} makes and for the same reason: "hidden in this
     * window" means the window and everything inside it, so an application that hides the pointer on
     * its toplevel keeps it hidden over its children, while one that hides it on a single child
     * window hides it only there.
     */
    private boolean cursorHiddenHere() {
        for (XWindow window = pointerWindow; window != null; window = window.parent()) {
            if (window.cursorHideCount() > 0) {
                return true;
            }
        }
        return false;
    }
}
