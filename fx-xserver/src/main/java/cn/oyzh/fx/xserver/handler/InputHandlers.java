package cn.oyzh.fx.xserver.handler;

import cn.oyzh.common.log.JulLog;
import com.github.moaxcp.x11.protocol.xproto.AllowEvents;
import com.github.moaxcp.x11.protocol.xproto.ChangeActivePointerGrab;
import com.github.moaxcp.x11.protocol.xproto.ChangeKeyboardMapping;
import com.github.moaxcp.x11.protocol.xproto.GetInputFocus;
import com.github.moaxcp.x11.protocol.xproto.GetInputFocusReply;
import com.github.moaxcp.x11.protocol.xproto.GetKeyboardMapping;
import com.github.moaxcp.x11.protocol.xproto.GetKeyboardMappingReply;
import com.github.moaxcp.x11.protocol.xproto.GetModifierMapping;
import com.github.moaxcp.x11.protocol.xproto.GetMotionEvents;
import com.github.moaxcp.x11.protocol.xproto.GetMotionEventsReply;
import com.github.moaxcp.x11.protocol.xproto.GetPointerMapping;
import com.github.moaxcp.x11.protocol.xproto.GetPointerMappingReply;
import com.github.moaxcp.x11.protocol.xproto.GrabButton;
import com.github.moaxcp.x11.protocol.xproto.GrabKey;
import com.github.moaxcp.x11.protocol.xproto.GrabKeyboard;
import com.github.moaxcp.x11.protocol.xproto.GrabKeyboardReply;
import com.github.moaxcp.x11.protocol.xproto.GrabPointer;
import com.github.moaxcp.x11.protocol.xproto.GrabPointerReply;
import com.github.moaxcp.x11.protocol.xproto.QueryKeymap;
import com.github.moaxcp.x11.protocol.xproto.QueryKeymapReply;
import com.github.moaxcp.x11.protocol.xproto.QueryPointer;
import com.github.moaxcp.x11.protocol.xproto.QueryPointerReply;
import com.github.moaxcp.x11.protocol.xproto.SetInputFocus;
import com.github.moaxcp.x11.protocol.xproto.SetModifierMapping;
import com.github.moaxcp.x11.protocol.xproto.SetPointerMapping;
import com.github.moaxcp.x11.protocol.xproto.UngrabButton;
import com.github.moaxcp.x11.protocol.xproto.UngrabKey;
import com.github.moaxcp.x11.protocol.xproto.UngrabKeyboard;
import com.github.moaxcp.x11.protocol.xproto.UngrabPointer;
import com.github.moaxcp.x11.protocol.xproto.WarpPointer;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.event.EventMasks;
import cn.oyzh.fx.xserver.input.InputState;
import cn.oyzh.fx.xserver.input.Keymap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.ByteLists;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Pointer, keyboard, focus and grab requests.
 *
 * <p>Grabs are the mechanism window managers and menus are built on: a client asks to receive
 * pointer or keyboard events even when they happen outside its windows. Active grabs are honoured
 * here; passive grabs are recorded and matched when a button or key is pressed.
 */
public final class InputHandlers {


    /** Grab status values reported to clients. */
    private static final byte GRAB_SUCCESS = InputState.GRAB_SUCCESS;

    /** AllowEvents modes. */
    private static final int MODE_ASYNC_POINTER = 0;
    private static final int MODE_SYNC_POINTER = 1;
    private static final int MODE_REPLAY_POINTER = 2;
    private static final int MODE_ASYNC_KEYBOARD = 3;
    private static final int MODE_SYNC_KEYBOARD = 4;
    private static final int MODE_REPLAY_KEYBOARD = 5;
    private static final int MODE_ASYNC_BOTH = 6;
    private static final int MODE_SYNC_BOTH = 7;

    private final XServer server;
    /** Keycodes whose keysyms a client replaced with {@code ChangeKeyboardMapping}. */
    /** Pointer button remapping; index 0 is button 1. */
    private byte[] pointerMapping = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    /** A modifier row holds two keycodes: the left and right variant of the modifier. */
    // Four, which is what a reference reports and what X's own headers assume: a client allocates its
    // own copy of the map from this number, and a server that answers two cannot describe the evdev
    // mapping at all (it has four keys in Mod1 and four in Mod4). Measured with `xmodmap -pm`, which
    // prints it as "up to N keys per modifier".
    private static final int KEYCODES_PER_MODIFIER = 4;

    /** A modifier mapping is always eight rows: Shift, Lock, Control, Mod1 to Mod5. */
    private static final int MODIFIER_ROWS = 8;

    /** Modifier keycodes, eight rows of {@link #KEYCODES_PER_MODIFIER} keycodes. */

    public InputHandlers(XServer server) {
        this.server = server;
    }

    private static byte[] defaultModifierMapping() {
        byte[] result = new byte[MODIFIER_ROWS * KEYCODES_PER_MODIFIER];
        for (int row = 0; row < MODIFIER_ROWS; row++) {
            int[] keycodes = Keymap.modifierRow(row);
            for (int i = 0; i < KEYCODES_PER_MODIFIER && i < keycodes.length; i++) {
                result[row * KEYCODES_PER_MODIFIER + i] = (byte) keycodes[i];
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // Pointer and focus queries
    // ------------------------------------------------------------------

    public void queryPointer(ClientConnection client, QueryPointer r) {
        XWindow window = window(client, r.getWindow());
        InputState input = server.inputFor(client);
        // The protocol asks for the *child of the queried window* that the pointer is in, not the
        // deepest window under it: a client uses this answer to name the window it is pointing at
        // (`xdotool getmouselocation`, a drag-and-drop source), and handing it a grandchild makes it
        // query a window the client never asked about. Xdotool segfaulted against this server, which
        // is what led here.
        XWindow child = window.childAt(input.pointerX(), input.pointerY());
        int childId = child == null ? 0 : child.id();
        client.sendReply(QueryPointerReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .sameScreen(true)
                .root(input.screen().root().id())
                .child(childId)
                .rootX((short) input.pointerX())
                .rootY((short) input.pointerY())
                .winX((short) (input.pointerX() - window.absoluteX()))
                .winY((short) (input.pointerY() - window.absoluteY()))
                .mask((short) input.pointerState())
                .build());
    }

    public void getMotionEvents(ClientConnection client, GetMotionEvents r) {
        InputState input = server.inputFor(client);
        XWindow window = window(client, r.getWindow());
        // The answer is what the pointer did inside this window between the two times the client
        // named, out of the bounded history the screen keeps. A client asks twice - once for the
        // count, once with room for it - so an answer that is empty the first time is normal.
        List<com.github.moaxcp.x11.protocol.xproto.Timecoord> events = new ArrayList<>();
        for (InputState.Motion motion : input.motionHistory(window, r.getStart(), r.getStop())) {
            events.add(com.github.moaxcp.x11.protocol.xproto.Timecoord.builder()
                    .time(motion.time())
                    .x((short) motion.x())
                    .y((short) motion.y())
                    .build());
        }
        client.sendReply(GetMotionEventsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .events(Lists.immutable.ofAll(events))
                .build());
    }

    public void warpPointer(ClientConnection client, WarpPointer r) {
        InputState input = server.inputFor(client);
        int x;
        int y;
        if (r.getDstWindow() == 0) {
            // A null destination means "do not move", except when the source is also null, in
            // which case the coordinates are relative to the current position.
            if (r.getSrcWindow() == 0) {
                x = input.pointerX() + r.getDstX();
                y = input.pointerY() + r.getDstY();
            } else {
                return;
            }
        } else {
            XWindow destination = window(client, r.getDstWindow());
            int baseX = destination.absoluteX() + r.getDstX();
            int baseY = destination.absoluteY() + r.getDstY();
            if (r.getSrcWindow() == 0) {
                x = baseX;
                y = baseY;
            } else {
                XWindow source = window(client, r.getSrcWindow());
                int srcX = source.absoluteX() + r.getSrcX();
                int srcY = source.absoluteY() + r.getSrcY();
                int srcW = r.getSrcWidth() == 0 ? source.width() : r.getSrcWidth();
                int srcH = r.getSrcHeight() == 0 ? source.height() : r.getSrcHeight();
                if (input.pointerX() < srcX || input.pointerX() >= srcX + srcW
                        || input.pointerY() < srcY || input.pointerY() >= srcY + srcH) {
                    // The pointer is outside the source rectangle, so nothing happens.
                    return;
                }
                x = baseX + (input.pointerX() - srcX);
                y = baseY + (input.pointerY() - srcY);
            }
        }
        input.warpPointer(x, y);
        server.markScreenDirty();
    }

    public void setInputFocus(ClientConnection client, SetInputFocus r) {
        int focus = r.getFocus();
        XWindow window;
        boolean pointerRoot = false;
        if (focus == 0) {
            // None: the keyboard focus is nowhere.
            window = null;
        } else if (focus == 1 || focus == 3) {
            // PointerRoot - and FollowKeyboard, which is as close to it as this server comes. Kept as
            // the root rather than resolved to the window the pointer is in right now: a client that
            // asks for PointerRoot is asking for a focus that follows the pointer, and it can tell
            // that apart from a focus it placed on a window of its own.
            window = server.inputFor(client).screen().root();
            pointerRoot = true;
        } else {
            // The root window asked for by id is *not* PointerRoot, and a client can tell the two
            // apart: GetInputFocus answers the id, and the two report different chains of FocusIn and
            // FocusOut. Where the *keys* go is the one thing they share - either way they belong to
            // the window the pointer is in, measured with tools/KeyTargetProbe - so this is the state
            // a RevertToParent out of a child of the root leaves a client in, and its text fields go
            // on working.
            window = window(client, focus);
        }
        // The focus is machine-wide, so an untrusted connection may not move it onto a window that
        // is not its own - which includes the root (F7, measured: BadAccess). PointerRoot names no
        // window and is left alone, as a reference leaves it.
        cn.oyzh.fx.xserver.extension.SecurityExtension.requireOwned(client,
                pointerRoot ? null : window, "SetInputFocus");
        server.inputFor(client).setInputFocus(window, r.getRevertTo(), pointerRoot);
    }

    public void getInputFocus(ClientConnection client, GetInputFocus r) {
        InputState input = server.inputFor(client);
        XWindow focus = input.inputFocus();
        client.sendReply(GetInputFocusReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .revertTo((byte) input.focusRevertTo())
                .focus(focus == null ? 0 : (input.focusIsPointerRoot() ? 1 : focus.id()))
                .build());
    }

    public void queryKeymap(ClientConnection client, QueryKeymap r) {
        byte[] keys = new byte[32];
        for (int row = 0; row < 8; row++) {
            keys[row] = (byte) modifierRowMask(client, row);
        }
        client.sendReply(QueryKeymapReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .keys(ByteLists.immutable.of(keys))
                .build());
    }

    private int modifierRowMask(ClientConnection client, int row) {
        return (server.inputFor(client).modifierState() & (1 << row)) != 0 ? 0xFF : 0;
    }

    // ------------------------------------------------------------------
    // Pointer grabs
    // ------------------------------------------------------------------

    public void grabPointer(ClientConnection client, GrabPointer r) {
        XWindow window = window(client, r.getGrabWindow());
        byte status = grabStatus(client, window);
        client.sendReply(GrabPointerReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .status(status)
                .build());
        if (JulLog.isDebugEnabled()) {
            // A grab decides which window every later button and motion event is reported against,
            // which is otherwise invisible from the client's side: a menu that ignores clicks looks
            // exactly like a server that delivered them somewhere else.
            JulLog.debug("grab pointer on 0x{} ownerEvents={} mask=0x{} confining={} -> {}",
                    Integer.toHexString(window.id()), r.isOwnerEvents(),
                    Integer.toHexString(Short.toUnsignedInt(r.getEventMask())),
                    r.getConfineTo() == 0 ? "no" : "0x" + Integer.toHexString(r.getConfineTo()),
                    status == GRAB_SUCCESS ? "granted" : "refused (" + status + ")");
        }
        if (status != GRAB_SUCCESS) {
            return;
        }
        server.inputFor(client).setDeviceGrab(client, window, false, r.isOwnerEvents(),
                Short.toUnsignedInt(r.getEventMask()), Byte.toUnsignedInt(r.getPointerMode()),
                Byte.toUnsignedInt(r.getKeyboardMode()), (int) server.events().time());
    }

    public void ungrabPointer(ClientConnection client, UngrabPointer r) {
        InputState.Grab grab = server.inputFor(client).pointerGrab();
        if (grab != null && grab.client() == client) {
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("ungrab pointer held on 0x{}", Integer.toHexString(grab.window().id()));
            }
            server.inputFor(client).setPointerGrab(null);
        }
    }

    public void changeActivePointerGrab(ClientConnection client, ChangeActivePointerGrab r) {
        InputState.Grab grab = server.inputFor(client).pointerGrab();
        if (grab == null || grab.client() != client) {
            return;
        }
        // The modes carry over: ChangeActivePointerGrab changes the event mask and the cursor, not
        // what the grab does to the devices.
        server.inputFor(client).setPointerGrab(new InputState.Grab(grab.client(), grab.window(),
                grab.ownerEvents(), Short.toUnsignedInt(r.getEventMask()), false, grab.pointerMode(),
                grab.keyboardMode(), grab.grabTime()));
    }

    /**
     * Arms a button grab.
     *
     * <p>An arm with the same button and modifiers replaces the one before it - the protocol says so
     * ("if a passive grab with the same button and modifiers exists, it is replaced"), and it is what
     * makes the *last* arm the one that fires. Without the removal the list would grow and the
     * matching rules would have to invent a tie-break the protocol already has.
     */
    public void grabButton(ClientConnection client, GrabButton r) {
        XWindow window = window(client, r.getGrabWindow());
        int modifiers = Short.toUnsignedInt(r.getModifiers());
        int button = Byte.toUnsignedInt(r.getButton());
        server.passiveGrabs().removeIf(grab -> grab.client() == client && grab.window() == window
                && grab.pointer() && grab.detail() == button && grab.modifiers() == modifiers);
        server.passiveGrabs().add(new PassiveGrab(client, window, r.isOwnerEvents(),
                Short.toUnsignedInt(r.getEventMask()), true, button, modifiers,
                Byte.toUnsignedInt(r.getPointerMode()),
                Byte.toUnsignedInt(r.getKeyboardMode())));
    }

    public void ungrabButton(ClientConnection client, UngrabButton r) {
        int button = Byte.toUnsignedInt(r.getButton());
        int modifiers = Short.toUnsignedInt(r.getModifiers());
        XWindow window = window(client, r.getGrabWindow());
        server.passiveGrabs().removeIf(grab -> grab.client() == client && grab.window() == window
                && grab.pointer() && (button == 0 || grab.detail() == button)
                && (modifiers == 0x8000 || grab.modifiers() == modifiers));
    }

    // ------------------------------------------------------------------
    // Keyboard grabs
    // ------------------------------------------------------------------

    public void grabKeyboard(ClientConnection client, GrabKeyboard r) {
        XWindow window = window(client, r.getGrabWindow());
        byte status = grabStatus(client, window);
        client.sendReply(GrabKeyboardReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .status(status)
                .build());
        if (status != GRAB_SUCCESS) {
            return;
        }
        server.inputFor(client).setDeviceGrab(client, window, true, r.isOwnerEvents(),
                EventMasks.KEYBOARD_GRAB, Byte.toUnsignedInt(r.getPointerMode()),
                Byte.toUnsignedInt(r.getKeyboardMode()), (int) server.events().time());
    }

    public void ungrabKeyboard(ClientConnection client, UngrabKeyboard r) {
        InputState.Grab grab = server.inputFor(client).keyboardGrab();
        if (grab != null && grab.client() == client) {
            server.inputFor(client).setKeyboardGrab(null);
        }
    }

    /** Arms a key grab, on the same terms as {@link #grabButton}: the last arm for a key wins. */
    public void grabKey(ClientConnection client, GrabKey r) {
        XWindow window = window(client, r.getGrabWindow());
        int modifiers = Short.toUnsignedInt(r.getModifiers());
        int key = Byte.toUnsignedInt(r.getKey());
        server.passiveGrabs().removeIf(grab -> grab.client() == client && grab.window() == window
                && !grab.pointer() && grab.detail() == key && grab.modifiers() == modifiers);
        server.passiveGrabs().add(new PassiveGrab(client, window, r.isOwnerEvents(),
                EventMasks.KEYBOARD_GRAB, false, key, modifiers,
                Byte.toUnsignedInt(r.getPointerMode()), Byte.toUnsignedInt(r.getKeyboardMode())));
    }

    public void ungrabKey(ClientConnection client, UngrabKey r) {
        int key = Byte.toUnsignedInt(r.getKey());
        int modifiers = Short.toUnsignedInt(r.getModifiers());
        XWindow window = window(client, r.getGrabWindow());
        server.passiveGrabs().removeIf(grab -> grab.client() == client && grab.window() == window
                && !grab.pointer() && (key == 0 || grab.detail() == key)
                && (modifiers == 0x8000 || grab.modifiers() == modifiers));
    }

    /**
     * {@code AllowEvents}: starts a device a synchronous grab stopped, or reprocesses the event it
     * stopped on. The state machine is {@code InputState.allowEvents}; what lives here is the check
     * that this server knows the mode at all.
     */
    public void allowEvents(ClientConnection client, AllowEvents r) {
        int mode = r.getMode();
        if (mode < MODE_ASYNC_POINTER || mode > MODE_SYNC_BOTH) {
            throw ProtocolException.badValue(mode);
        }
        server.inputFor(client).allowEvents(client, mode, r.getTime());
    }

    /**
     * The status a grab request is answered with.
     *
     * <p>The rule lives in {@link InputState#grabStatus}, not here, because XInput2's
     * {@code XIGrabDevice} takes the same grab on the same device and has to answer the same thing -
     * a client that grabs on one rail and reads the status on the other must not be told two
     * different stories about the one pointer.
     */
    private byte grabStatus(ClientConnection client, XWindow window) {
        return server.inputFor(client).grabStatus(client, window);
    }

    // ------------------------------------------------------------------
    // Mappings
    // ------------------------------------------------------------------

    public void setPointerMapping(ClientConnection client, SetPointerMapping r) {
        byte[] requested = r.getMap().toArray();
        for (byte value : requested) {
            if ((value & 0xFF) > 10) {
                throw ProtocolException.badValue(value & 0xFF);
            }
        }
        // A mapping that is not a permutation is only partially applied, per the protocol.
        this.pointerMapping = requested.length == 0 ? new byte[] {1} : requested;
        client.sendReply(com.github.moaxcp.x11.protocol.xproto.SetPointerMappingReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .status(mapIsPermutation(requested) ? (byte) 0 : (byte) 1)
                .build());
    }

    private static boolean mapIsPermutation(byte[] map) {
        Set<Integer> seen = new LinkedHashSet<>();
        for (byte value : map) {
            int v = value & 0xFF;
            if (v == 0 || !seen.add(v)) {
                return false;
            }
        }
        return true;
    }

    public void getPointerMapping(ClientConnection client, GetPointerMapping r) {
        client.sendReply(GetPointerMappingReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .map(ByteLists.immutable.of(pointerMapping))
                .build());
    }

    public void setModifierMapping(ClientConnection client, SetModifierMapping r) {
        byte[] keycodes = r.getKeycodes().toArray();
        int maxKeycode = Keymap.MAX_KEYCODE & 0xFF;
        Set<Integer> seen = new LinkedHashSet<>();
        for (byte value : keycodes) {
            int keycode = value & 0xFF;
            if (keycode == 0) {
                continue;
            }
            if (keycode < (Keymap.MIN_KEYCODE & 0xFF) || keycode > maxKeycode) {
                throw ProtocolException.badValue(keycode);
            }
            if (!seen.add(keycode)) {
                throw ProtocolException.badValue(keycode);
            }
        }
        client.sendReply(com.github.moaxcp.x11.protocol.xproto.SetModifierMappingReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .status((byte) 0)
                .build());
    }

    /**
     * Answers {@code GetModifierMapping}.
     *
     * <p>The reply is encoded here because the generated writer fills the keycodes-per-modifier byte
     * with the total number of keycodes rather than the number per row. A client that trusts it
     * allocates eight times too much and reads past the reply - xterm segfaults on the spot - so the
     * byte has to be the row length.
     */
    public void getModifierMapping(ClientConnection client, GetModifierMapping r) {
        // Built here rather than cached: a client can load a layout by name at any time, and the
        // modifier rows travel with the keymap (Keymap.useLayout) - a cached copy answered the map
        // of whichever layout was in force when the server started.
        byte[] modifierMapping = defaultModifierMapping();
        byte[] bytes = new byte[32 + modifierMapping.length];
        bytes[0] = 1;                                   // reply
        bytes[1] = (byte) (modifierMapping.length / MODIFIER_ROWS);
        int sequence = client.sequenceNumber() & 0xFFFF;
        int length = modifierMapping.length / 4;
        if (client.isBigEndian()) {
            bytes[2] = (byte) (sequence >> 8);
            bytes[3] = (byte) sequence;
            bytes[4] = (byte) (length >> 24);
            bytes[5] = (byte) (length >> 16);
            bytes[6] = (byte) (length >> 8);
            bytes[7] = (byte) length;
        } else {
            bytes[2] = (byte) sequence;
            bytes[3] = (byte) (sequence >> 8);
            bytes[4] = (byte) length;
            bytes[5] = (byte) (length >> 8);
            bytes[6] = (byte) (length >> 16);
            bytes[7] = (byte) (length >> 24);
        }
        System.arraycopy(modifierMapping, 0, bytes, 32, modifierMapping.length);
        try {
            client.output().writeRaw(bytes);
        } catch (java.io.IOException e) {
            client.close();
        }
    }

    public void changeKeyboardMapping(ClientConnection client, ChangeKeyboardMapping r) {
        int count = Byte.toUnsignedInt(r.getKeycodeCount());
        int first = Byte.toUnsignedInt(r.getFirstKeycode());
        int perKeycode = Byte.toUnsignedInt(r.getKeysymsPerKeycode());
        if (count == 0 || perKeycode == 0) {
            throw ProtocolException.badValue(count == 0 ? count : perKeycode);
        }
        int maxKeycode = Keymap.MAX_KEYCODE & 0xFF;
        if (first < (Keymap.MIN_KEYCODE & 0xFF) || first + count - 1 > maxKeycode) {
            throw ProtocolException.badValue(first);
        }
        int[] keysyms = r.getKeysyms().toArray();
        if (keysyms.length < count * perKeycode) {
            throw ProtocolException.badLength();
        }
        for (int i = 0; i < count; i++) {
            int[] row = new int[perKeycode];
            System.arraycopy(keysyms, i * perKeycode, row, 0, perKeycode);
            Keymap.assign(first + i, row);
        }
        JulLog.debug("client mapping change: first={} count={} perKeycode={} keysyms={}", first, count,
                perKeycode, keysyms);
        // Clients are told the mapping changed so they can refresh their caches.
        server.broadcastMappingNotify((byte) 0, (byte) first, (byte) count);
    }

    public void getKeyboardMapping(ClientConnection client, GetKeyboardMapping r) {
        int first = Byte.toUnsignedInt(r.getFirstKeycode());
        int count = Byte.toUnsignedInt(r.getCount());
        if (count == 0) {
            throw ProtocolException.badValue(count);
        }
        int maxKeycode = Keymap.MAX_KEYCODE & 0xFF;
        if (first + count - 1 > maxKeycode) {
            throw ProtocolException.badValue(first);
        }
        int perKeycode = Keymap.width();
        int[] keysyms = new int[count * perKeycode];
        for (int i = 0; i < count; i++) {
            int keycode = first + i;
            for (int level = 0; level < perKeycode; level++) {
                keysyms[i * perKeycode + level] = Keymap.keysym(keycode, level);
            }
        }
        client.sendReply(GetKeyboardMappingReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .keysymsPerKeycode((byte) perKeycode)
                .keysyms(IntLists.immutable.of(keysyms))
                .build());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private XWindow window(ClientConnection client, int id) {
        return (XWindow) server.resources().require(client, id, XResource.CLASS_WINDOW, false);
    }

    /** A keyboard or pointer grab that activates when a matching input event arrives. */
    public record PassiveGrab(ClientConnection client, XWindow window, boolean ownerEvents,
                              int eventMask, boolean pointer, int detail, int modifiers,
                              int pointerMode, int keyboardMode) {
    }
}
