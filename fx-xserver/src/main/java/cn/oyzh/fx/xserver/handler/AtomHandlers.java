package cn.oyzh.fx.xserver.handler;

import com.github.moaxcp.x11.protocol.xproto.ChangeProperty;
import com.github.moaxcp.x11.protocol.xproto.ConvertSelection;
import com.github.moaxcp.x11.protocol.xproto.DeleteProperty;
import com.github.moaxcp.x11.protocol.xproto.GetAtomName;
import com.github.moaxcp.x11.protocol.xproto.GetAtomNameReply;
import com.github.moaxcp.x11.protocol.xproto.GetProperty;
import com.github.moaxcp.x11.protocol.xproto.GetPropertyReply;
import com.github.moaxcp.x11.protocol.xproto.GetSelectionOwner;
import com.github.moaxcp.x11.protocol.xproto.GetSelectionOwnerReply;
import com.github.moaxcp.x11.protocol.xproto.InternAtom;
import com.github.moaxcp.x11.protocol.xproto.InternAtomReply;
import com.github.moaxcp.x11.protocol.xproto.ListProperties;
import com.github.moaxcp.x11.protocol.xproto.ListPropertiesReply;
import com.github.moaxcp.x11.protocol.xproto.PropertyNotifyEvent;
import com.github.moaxcp.x11.protocol.xproto.RotateProperties;
import com.github.moaxcp.x11.protocol.xproto.SelectionClearEvent;
import com.github.moaxcp.x11.protocol.xproto.SelectionRequestEvent;
import com.github.moaxcp.x11.protocol.xproto.SendEvent;
import com.github.moaxcp.x11.protocol.xproto.SetSelectionOwner;
import cn.oyzh.fx.xserver.WindowIdentity;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.clipboard.ClipboardBridge;
import cn.oyzh.fx.xserver.event.EventMasks;
import cn.oyzh.fx.xserver.extension.XFixesExtension;
import cn.oyzh.fx.xserver.resource.AtomTable;
import cn.oyzh.fx.xserver.resource.XProperty;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.eclipse.collections.impl.factory.primitive.ByteLists;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Atoms, window properties, selections and {@code SendEvent}.
 *
 * <p>These four are grouped because they are the inter-client communication channel: a client
 * publishes state as a property, names it with an atom, hands a selection to another client, and
 * the notification reaches the peer either as a property notify or as an explicitly sent event.
 */
public final class AtomHandlers {

    /** {@code PropertyNotify} states: the property was written, or it went away. */
    public static final byte NEW_VALUE = 0;
    public static final byte DELETED = 1;

    /** ChangeProperty modes. */
    private static final int MODE_REPLACE = 0;
    private static final int MODE_PREPEND = 1;
    private static final int MODE_APPEND = 2;

    /** {@code longLength} values above this mean "read the whole property". */
    private static final long LONG_LENGTH_ALL = Integer.MAX_VALUE >> 2;

    private final XServer server;

    public AtomHandlers(XServer server) {
        this.server = server;
    }

    // ------------------------------------------------------------------
    // Atoms
    // ------------------------------------------------------------------

    public void internAtom(ClientConnection client, InternAtom r) {
        String name = ascii(r.getName());
        if (name.isEmpty()) {
            throw ProtocolException.badValue(0);
        }
        int atom = server.atoms().intern(name, r.isOnlyIfExists());
        client.sendReply(InternAtomReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .atom(atom)
                .build());
    }

    public void getAtomName(ClientConnection client, GetAtomName r) {
        String name = server.atoms().name(r.getAtom());
        if (name == null) {
            throw ProtocolException.badAtom(r.getAtom());
        }
        client.sendReply(GetAtomNameReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .name(ByteLists.immutable.of(name.getBytes(StandardCharsets.ISO_8859_1)))
                .build());
    }

    // ------------------------------------------------------------------
    // Properties
    // ------------------------------------------------------------------

    public void changeProperty(ClientConnection client, ChangeProperty r) {
        XWindow window = window(client, r.getWindow());
        cn.oyzh.fx.xserver.extension.SecurityExtension.requireOwned(client, window,
                "ChangeProperty");
        int property = r.getProperty();
        if (property == AtomTable.NONE) {
            throw ProtocolException.badAtom(property);
        }
        int type = r.getType();
        int format = Byte.toUnsignedInt(r.getFormat());
        if (format != 8 && format != 16 && format != 32) {
            throw ProtocolException.badValue(format);
        }
        if (r.getMode() < MODE_REPLACE || r.getMode() > MODE_APPEND) {
            throw ProtocolException.badValue(r.getMode());
        }

        byte[] incoming = normalize(r.getData().toArray(), format, client.isBigEndian());
        // X requires the data to be a whole number of items; anything trailing is ignored.
        int unit = format / 8;
        int usable = incoming.length - incoming.length % unit;
        if (usable != incoming.length) {
            incoming = java.util.Arrays.copyOf(incoming, usable);
        }

        XProperty existing = window.properties().get(property);
        if (r.getMode() == MODE_REPLACE || existing == null) {
            window.properties().put(property, new XProperty(property, type, format, incoming));
        } else {
            if (existing.typeAtom() != type || existing.format() != format) {
                throw ProtocolException.badMatch();
            }
            byte[] old = existing.data();
            byte[] combined;
            if (r.getMode() == MODE_PREPEND) {
                combined = new byte[incoming.length + old.length];
                System.arraycopy(incoming, 0, combined, 0, incoming.length);
                System.arraycopy(old, 0, combined, incoming.length, old.length);
            } else {
                combined = new byte[old.length + incoming.length];
                System.arraycopy(old, 0, combined, 0, old.length);
                System.arraycopy(incoming, 0, combined, old.length, incoming.length);
            }
            existing.setData(combined);
        }
        notifyProperty(window, property, NEW_VALUE);
    }

    public void deleteProperty(ClientConnection client, DeleteProperty r) {
        XWindow window = window(client, r.getWindow());
        if (window.properties().remove(r.getProperty()) != null) {
            notifyProperty(window, r.getProperty(), DELETED);
        }
    }

    public void getProperty(ClientConnection client, GetProperty r) {
        XWindow window = window(client, r.getWindow());
        cn.oyzh.fx.xserver.extension.SecurityExtension.requireOwned(client, window,
                "GetProperty");
        XProperty property = window.properties().get(r.getProperty());
        if (property == null) {
            // A missing property is not an error: the reply carries type None and no data.
            client.sendReply(GetPropertyReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .format((byte) 0)
                    .type(AtomTable.NONE)
                    .bytesAfter(0)
                    .valueLen(0)
                    .value(ByteLists.immutable.empty())
                    .build());
            return;
        }
        if (r.getType() != AtomTable.NONE && r.getType() != property.typeAtom()) {
            // Reading with the wrong type behaves like a missing property.
            client.sendReply(GetPropertyReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .format((byte) 0)
                    .type(property.typeAtom())
                    .bytesAfter(property.data().length)
                    .valueLen(0)
                    .value(ByteLists.immutable.empty())
                    .build());
            return;
        }

        int unit = property.format() / 8;
        byte[] data = property.data();
        int offsetBytes = r.getLongOffset() * 4;
        if (offsetBytes < 0 || offsetBytes > data.length) {
            throw ProtocolException.badValue(r.getLongOffset());
        }
        // longLength is a CARD32 and counts 4 byte units; values above LONG_LENGTH_ALL mean
        // "everything". It is read unsigned on purpose: the value a client sends to ask for the
        // whole property is 0xFFFFFFFF, which as a signed int is -1 - not "everything" but a
        // negative length, and one that used to reach the array copy below as an error the client
        // was told was BadImplementation rather than answered.
        long longLength = Integer.toUnsignedLong(r.getLongLength());
        int available = data.length - offsetBytes;
        int requestedBytes;
        if (longLength == 0) {
            requestedBytes = 0;
        } else if (longLength > LONG_LENGTH_ALL) {
            requestedBytes = available;
        } else {
            requestedBytes = (int) Math.min(longLength * 4, (long) available);
        }
        // The slice must end on an item boundary.
        requestedBytes -= requestedBytes % unit;

        byte[] slice = java.util.Arrays.copyOfRange(data, offsetBytes,
                offsetBytes + requestedBytes);
        int bytesAfter = data.length - offsetBytes - requestedBytes;

        if (r.isDelete() && bytesAfter == 0) {
            window.properties().remove(r.getProperty());
            notifyProperty(window, r.getProperty(), DELETED);
        }

        client.sendReply(GetPropertyReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .format((byte) property.format())
                .type(property.typeAtom())
                .bytesAfter(bytesAfter)
                .valueLen(requestedBytes / unit)
                .value(ByteLists.immutable.of(normalize(slice, property.format(),
                        client.isBigEndian())))
                .build());
    }

    public void listProperties(ClientConnection client, ListProperties r) {
        XWindow window = window(client, r.getWindow());
        List<Integer> atoms = new ArrayList<>(window.properties().keySet());
        java.util.Collections.sort(atoms);
        client.sendReply(ListPropertiesReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .atoms(IntLists.immutable.ofAll(atoms))
                .build());
    }

    public void rotateProperties(ClientConnection client, RotateProperties r) {
        XWindow window = window(client, r.getWindow());
        List<Integer> atoms = new ArrayList<>();
        for (int atom : r.getAtoms().toArray()) {
            if (!window.properties().containsKey(atom)) {
                throw ProtocolException.badAtom(atom);
            }
            atoms.add(atom);
        }
        if (atoms.isEmpty()) {
            return;
        }
        int count = atoms.size();
        int delta = ((r.getDelta() % count) + count) % count;
        if (delta == 0) {
            return;
        }
        List<XProperty> rotated = new ArrayList<>(java.util.Collections.nCopies(count, null));
        for (int i = 0; i < count; i++) {
            rotated.set((i + delta) % count, window.properties().get(atoms.get(i)));
        }
        for (int i = 0; i < count; i++) {
            window.properties().put(atoms.get(i), rotated.get(i));
            notifyProperty(window, atoms.get(i), NEW_VALUE);
        }
    }

    /** {@code _NET_WM_STATE}'s actions, as the ClientMessage carries them. */
    private static final int STATE_REMOVE = 0;
    private static final int STATE_ADD = 1;
    private static final int STATE_TOGGLE = 2;

    /**
     * Applies an EWMH {@code _NET_WM_STATE} message to the window it names.
     *
     * <p>This, and not a property write, is how a toolkit asks for its window to be fullscreen or
     * maximised: it sends a {@code ClientMessage} to the root and the window manager is expected to
     * say yes by putting the state into the property - a client that is ignored is free to ask again
     * for ever, and several do. So the message is turned into the property here, which is also the
     * form the rest of this server reads it in ({@code WindowIdentity}).
     *
     * <p>Only the window's own state is kept: this server is not a window manager and has no other
     * state to arbitrate, so the message is applied rather than decided on.
     */
    private void applyWindowStateMessage(byte[] bytes, boolean bigEndian) {
        if ((bytes[1] & 0xFF) != 32) {
            return;                                 // only the 32 bit form is defined
        }
        int stateAtom = server.atoms().intern(WindowIdentity.NET_WM_STATE, true);
        if (stateAtom == AtomTable.NONE || readCard32(bytes, 8, bigEndian) != stateAtom) {
            return;
        }
        XResource resource = server.resources().get(readCard32(bytes, 4, bigEndian));
        if (!(resource instanceof XWindow window)) {
            return;                                 // for a window that has already gone
        }
        int action = readCard32(bytes, 12, bigEndian);
        int first = readCard32(bytes, 16, bigEndian);
        int second = readCard32(bytes, 20, bigEndian);

        List<Integer> states = new ArrayList<>(
                readState(window, stateAtom));
        boolean changed = applyState(states, action, first);
        if (second != 0) {
            changed |= applyState(states, action, second);
        }
        if (changed) {
            writeState(window, stateAtom, states);
        }
    }

    private static boolean applyState(List<Integer> states, int action, int atom) {
        if (atom == 0) {
            return false;
        }
        boolean present = states.contains(atom);
        switch (action) {
            case STATE_ADD -> {
                if (!present) {
                    states.add(atom);
                    return true;
                }
            }
            case STATE_REMOVE -> {
                if (present) {
                    states.remove(Integer.valueOf(atom));
                    return true;
                }
            }
            case STATE_TOGGLE -> {
                if (present) {
                    states.remove(Integer.valueOf(atom));
                } else {
                    states.add(atom);
                }
                return true;
            }
            default -> {
                // An action this server does not know: leave the list exactly as it was.
                return false;
            }
        }
        return false;
    }

    /** The states a window currently carries, read from its {@code _NET_WM_STATE} list. */
    private static List<Integer> readState(XWindow window, int stateAtom) {
        XProperty property = window.properties().get(stateAtom);
        if (property == null || property.format() != XProperty.FORMAT_32) {
            return List.of();
        }
        List<Integer> states = new ArrayList<>();
        for (int atom : WindowIdentity.readWords(property.data())) {
            states.add(atom);
        }
        return states;
    }

    /** Writes the list back, which is how the client is told its request was granted. */
    private void writeState(XWindow window, int stateAtom, List<Integer> states) {
        byte[] data = new byte[states.size() * 4];
        for (int i = 0; i < states.size(); i++) {
            putCard32(data, i * 4, states.get(i), true);
        }
        int type = server.atoms().intern("ATOM", true);
        window.properties().put(stateAtom, new XProperty(stateAtom, type, 32, data));
        notifyProperty(window, stateAtom, NEW_VALUE);
    }

    /**
     * Announces a property write to whoever is watching the window - the clients that selected for
     * {@code PropertyChange} and the server's own listeners, which is what keeps the host window's
     * title and the clipboard bridge's {@code INCR} handshake in step with the property itself.
     *
     * <p>Public because the bridge writes properties on windows that are not its own clients': it is
     * acting as a selection owner, and an owner that changes a property owes the window the same
     * notification a client's {@code ChangeProperty} would produce.
     */
    public void notifyProperty(XWindow window, int atom, byte state) {
        // A title or an icon is an ordinary property, and the host window shows them, so whoever
        // listens has to hear about the write as well as the clients that selected for it.
        server.windowPropertyChanged(window, atom);
        server.events().deliverToWindow(window, EventMasks.PROPERTY_CHANGE,
                (recipient, w, child, x, y) -> PropertyNotifyEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(recipient.sequenceNumber())
                        .window(w.id())
                        .atom(atom)
                        .time(server.events().time())
                        .state(state)
                        .build());
    }

    // ------------------------------------------------------------------
    // Selections
    // ------------------------------------------------------------------

    /**
     * Hands a selection to a window, or takes it away from everyone when the owner is {@code None}.
     *
     * <p>Three things are told that the owner changed, and they are told different things: the
     * outgoing owner hears {@code SelectionClear}, while the clients that subscribed through XFixes
     * hear a {@code SelectionNotify} - including the subscribers of a selection that had no owner
     * and of one whose owner did not actually change, because a reference server was measured
     * sending both.
     */
    public void setSelectionOwner(ClientConnection client, SetSelectionOwner r) {
        // A selection is shared state rather than a drawable, so this is refused outright for an
        // untrusted connection: a reference refuses it BadAccess even when the window it names is
        // the client's own (measured).
        cn.oyzh.fx.xserver.extension.SecurityExtension.requireTrusted(client,
                "SetSelectionOwner");
        String name = selectionName(r.getSelection());
        XWindow owner = r.getOwner() == 0 ? null : window(client, r.getOwner());
        if (r.getTime() != 0 && !isNewerThanCurrent(r.getTime())) {
            return;
        }
        int now = server.events().time();
        int newOwner = owner == null ? 0 : owner.id();
        int previous = server.selectionOwner(name);
        server.setSelectionOwner(name, newOwner, now, XFixesExtension.SELECTION_SET_OWNER);
        if (previous != 0 && previous != newOwner) {
            // The outgoing owner is told it lost the selection.
            XResource previousResource = server.resources().get(previous);
            if (previousResource instanceof XWindow previousWindow
                    && previousWindow.owner() != null) {
                ClientConnection previousOwner = previousWindow.owner();
                server.events().send(previousOwner, SelectionClearEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(previousOwner.sequenceNumber())
                        .time(now)
                        .owner(previous)
                        .selection(r.getSelection())
                        .build());
            }
        }
    }

    public void getSelectionOwner(ClientConnection client, GetSelectionOwner r) {
        client.sendReply(GetSelectionOwnerReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .owner(server.selectionOwner(selectionName(r.getSelection())))
                .build());
    }

    /**
     * Forwards a selection request to the current owner.
     *
     * <p>The server does not mediate the data transfer: it hands the request to the owner, which
     * answers the requestor directly with a {@code SelectionNotify}. That is how X works, and it
     * is what lets a clipboard manager own the selection without the server holding any data.
     */
    public void convertSelection(ClientConnection client, ConvertSelection r) {
        XWindow requestor = window(client, r.getRequestor());
        int ownerId = server.selectionOwner(selectionName(r.getSelection()));
        // The host clipboard bridge owns CLIPBOARD as a window of the server's own, so there is no
        // client to forward the request to: it answers here, with the same two events any owner
        // would send back.
        ClipboardBridge bridge = server.clipboard();
        if (bridge != null && ownerId != 0 && bridge.ownsWindow(ownerId)) {
            bridge.serve(requestor.owner(), requestor, r.getSelection(), r.getTarget(),
                    r.getProperty(), r.getTime());
            return;
        }
        cn.oyzh.fx.xserver.drop.DropBridge drops = server.dropBridge();
        if (drops != null && ownerId != 0 && drops.ownsWindow(ownerId)) {
            drops.serve(requestor, r.getSelection(), r.getTarget(), r.getProperty(), r.getTime());
            return;
        }
        if (ownerId == 0) {
            server.events().send(requestor.owner(), com.github.moaxcp.x11.protocol.xproto
                    .SelectionNotifyEvent.builder()
                    .firstEventOffset((byte) 0)
                    .sentEvent(false)
                    .sequenceNumber(requestor.owner().sequenceNumber())
                    .time(r.getTime())
                    .requestor(r.getRequestor())
                    .selection(r.getSelection())
                    .target(r.getTarget())
                    .property(AtomTable.NONE)
                    .build());
            return;
        }
        XResource ownerResource = server.resources().get(ownerId);
        if (!(ownerResource instanceof XWindow owner)) {
            return;
        }
        server.events().send(owner.owner(), SelectionRequestEvent.builder()
                .firstEventOffset((byte) 0)
                .sentEvent(false)
                .sequenceNumber(owner.owner().sequenceNumber())
                .time(r.getTime())
                .owner(ownerId)
                .requestor(r.getRequestor())
                .selection(r.getSelection())
                .target(r.getTarget())
                .property(r.getProperty())
                .build());
    }

    private String selectionName(int atom) {
        String name = server.atoms().name(atom);
        if (name == null) {
            throw ProtocolException.badAtom(atom);
        }
        return name;
    }

    private boolean isNewerThanCurrent(int time) {
        // Timestamps are only compared to reject clearly stale requests; without a global
        // ordering of client times the current owner always loses a tie.
        return true;
    }

    // ------------------------------------------------------------------
    // SendEvent
    // ------------------------------------------------------------------

    /**
     * Field layout of every core event, as pairs of {@code offset, size}. Events are the one place
     * the protocol cannot be re-encoded generically: the sender produced raw bytes in its own byte
     * order and the server has to hand them on in the recipient's, so the multi byte fields have to
     * be located exactly. Single byte fields and padding are deliberately absent.
     */
    private static final int[][] EVENT_FIELDS = buildEventFieldTable();

    /** Offset of the window the event is reported against, or -1 when the event has none. */
    private static final int[] EVENT_WINDOW_OFFSET = buildEventWindowOffsets();

    private static final int KEYMAP_NOTIFY = 11;
    private static final int SELECTION_NOTIFY = 31;
    private static final int CLIENT_MESSAGE = 33;

    /** Offset of {@code SelectionNotify}'s property field, which says where the data was put. */
    private static final int SELECTION_NOTIFY_PROPERTY = 20;

    public void sendEvent(ClientConnection client, SendEvent r) {
        XWindow destination;
        if (r.getDestination() == 0) {
            destination = server.inputFor(client).windowAtPointer();
        } else if (r.getDestination() == 1) {
            destination = server.inputFor(client).inputFocus();
        } else {
            destination = window(client, r.getDestination());
        }
        byte[] bytes = r.getEvent().toArray();
        if (bytes.length < 32) {
            throw ProtocolException.badValue(bytes.length);
        }
        int eventCode = bytes[0] & 0x7F;
        boolean senderBigEndian = client.isBigEndian();

        // The server stamps the event as synthetic and fills in the reporting window; neither is
        // something the sending client gets to decide.
        bytes[0] = (byte) (bytes[0] | 0x80);
        int windowOffset = eventCode < EVENT_WINDOW_OFFSET.length
                ? EVENT_WINDOW_OFFSET[eventCode] : -1;
        if (windowOffset >= 0 && eventCode != CLIENT_MESSAGE) {
            // Every other event is *reported against* a window, and the server is the one that knows
            // which - so the field is filled in here. A ClientMessage is the exception: that field
            // is the window the message is *about*, put there by the client that sent it, and the
            // destination is only whose selector hears it. Rewriting it loses the subject of the
            // message, which is what every EWMH request is made of: a window manager told "this
            // window wants to be fullscreen" with the window left out cannot act at all.
            putCard32(bytes, windowOffset, destination.id(), senderBigEndian);
        }

        // A SendEvent aimed at the bridge's window is a client answering a selection request:
        // the bridge has no connection of its own, so the SelectionNotify the client sent is handed
        // over rather than delivered - there is nobody on that window to deliver it to.
        ClipboardBridge bridge = server.clipboard();
        if (bridge != null && eventCode == SELECTION_NOTIFY
                && bridge.ownsWindow(destination.id())) {
            bridge.selectionAnswered(
                    readCard32(bytes, SELECTION_NOTIFY_PROPERTY, senderBigEndian));
        }
        cn.oyzh.fx.xserver.drop.DropBridge dragging = server.dropBridge();
        if (dragging != null && eventCode == SELECTION_NOTIFY
                && dragging.ownsWindow(destination.id())) {
            // The same, for the other direction: a client answering the bridge's request for the
            // files a drag inside a forwarded application is offering (C5).
            dragging.selectionAnswered(
                    readCard32(bytes, SELECTION_NOTIFY_PROPERTY, senderBigEndian));
        }
        cn.oyzh.fx.xserver.tray.TrayBridge tray = server.tray();
        if (tray != null && eventCode == CLIENT_MESSAGE && tray.ownsWindow(destination.id())) {
            // A notification-area client asking to be displayed: SYSTEM_TRAY_REQUEST_DOCK, sent as a
            // ClientMessage to the selection's owner, which is the bridge. Its data is opcode,
            // window id, then three words the three opcodes use differently.
            int[] data = new int[5];
            for (int i = 0; i < data.length; i++) {
                data[i] = readCard32(bytes, 12 + i * 4, senderBigEndian);
            }
            tray.clientMessage(readCard32(bytes, 8, senderBigEndian), data);
            return;
        }
        cn.oyzh.fx.xserver.drop.DropBridge drops = server.dropBridge();
        if (drops != null && eventCode == CLIENT_MESSAGE && drops.ownsWindow(destination.id())) {
            // The target answering the drag: XdndStatus says whether it will take the drop, and
            // XdndFinished says the conversation is over.
            int[] data = new int[5];
            for (int i = 0; i < data.length; i++) {
                data[i] = readCard32(bytes, 12 + i * 4, senderBigEndian);
            }
            drops.clientMessage(readCard32(bytes, 8, senderBigEndian), data, client);
        }

        if (eventCode == CLIENT_MESSAGE) {
            applyWindowStateMessage(bytes, senderBigEndian);
        }

        int mask = r.getEventMask();
        if (mask == 0 && !r.isPropagate()) {
            // X's own rule, and the one every EWMH and XDND message depends on: an event sent with
            // no event mask goes to the client that *created* the destination window, whatever that
            // client selected. It has to be this way round - a target cannot select for a message it
            // does not know is coming - so requiring a mask here drops all of them.
            ClientConnection owner = destination.owner();
            if (owner != null) {
                server.events().send(owner, rawEvent(owner, bytes, senderBigEndian));
            }
            return;
        }
        if (r.isPropagate()) {
            server.events().deliverPropagating(destination, mask, server.inputFor(client).pointerX(),
                    server.inputFor(client).pointerY(),
                    (recipient, w, child, x, y) -> rawEvent(recipient, bytes, senderBigEndian));
            return;
        }
        server.events().deliverToWindow(destination, mask,
                (recipient, w, child, x, y) -> rawEvent(recipient, bytes, senderBigEndian));
    }

    private com.github.moaxcp.x11.protocol.XEvent rawEvent(ClientConnection recipient, byte[] bytes,
                                                           boolean senderBigEndian) {
        boolean recipientBigEndian = recipient.isBigEndian();
        byte[] translated = senderBigEndian == recipientBigEndian
                ? bytes.clone()
                : translateEvent(bytes);
        putCard16(translated, 2, recipient.sequenceNumber(), recipientBigEndian);
        return new RawEvent(translated, recipient.sequenceNumber(), recipientBigEndian);
    }

    /** Swaps every multi byte field of a core event into the opposite byte order. */
    private static byte[] translateEvent(byte[] bytes) {
        byte[] out = bytes.clone();
        int eventCode = out[0] & 0x7F;
        int[] fields = eventCode < EVENT_FIELDS.length ? EVENT_FIELDS[eventCode] : null;
        if (fields != null) {
            for (int i = 0; i + 1 < fields.length; i += 2) {
                int offset = fields[i];
                int size = fields[i + 1];
                if (offset + size > out.length) {
                    continue;
                }
                for (int j = 0; j < size / 2; j++) {
                    byte tmp = out[offset + j];
                    out[offset + j] = out[offset + size - 1 - j];
                    out[offset + size - 1 - j] = tmp;
                }
            }
        }
        if (eventCode == CLIENT_MESSAGE) {
            // The data union is 5 CARD32s, 10 CARD16s or 20 bytes depending on the format field.
            int format = out[1];
            int size = format == 32 ? 4 : format == 16 ? 2 : 0;
            if (size != 0) {
                for (int offset = 12; offset + size <= 32; offset += size) {
                    for (int j = 0; j < size / 2; j++) {
                        byte tmp = out[offset + j];
                        out[offset + j] = out[offset + size - 1 - j];
                        out[offset + size - 1 - j] = tmp;
                    }
                }
            }
        }
        return out;
    }

    private static int[][] buildEventFieldTable() {
        int[][] table = new int[35][];
        int[] keyLike = {2, 2, 4, 4, 8, 4, 12, 4, 16, 4, 20, 2, 22, 2, 24, 2, 26, 2, 28, 2};
        for (int code : new int[] {2, 3, 4, 5, 6}) {
            table[code] = keyLike;
        }
        int[] enterLike = {2, 2, 4, 4, 8, 4, 12, 4, 16, 4, 20, 2, 22, 2, 24, 2, 26, 2, 28, 2};
        table[7] = enterLike;
        table[8] = enterLike;
        table[9] = new int[] {2, 2, 4, 4};
        table[10] = new int[] {2, 2, 4, 4};
        table[11] = new int[0];
        table[12] = new int[] {2, 2, 4, 4, 8, 2, 10, 2, 12, 2, 14, 2, 16, 2};
        table[13] = new int[] {2, 2, 4, 4, 8, 2, 10, 2, 12, 2, 14, 2, 16, 2, 18, 2};
        table[14] = new int[] {2, 2, 4, 4, 8, 2};
        table[15] = new int[] {2, 2, 4, 4};
        table[16] = new int[] {2, 2, 4, 4, 8, 4, 12, 2, 14, 2, 16, 2, 18, 2, 20, 2};
        table[17] = new int[] {2, 2, 4, 4, 8, 4};
        table[18] = new int[] {2, 2, 4, 4, 8, 4};
        table[19] = new int[] {2, 2, 4, 4, 8, 4};
        table[20] = new int[] {2, 2, 4, 4, 8, 4};
        table[21] = new int[] {2, 2, 4, 4, 8, 4, 12, 4, 16, 2, 18, 2};
        table[22] = new int[] {2, 2, 4, 4, 8, 4, 12, 4, 16, 2, 18, 2, 20, 2, 22, 2, 24, 2};
        table[23] = new int[] {2, 2, 4, 4, 8, 4, 12, 4, 16, 2, 18, 2, 20, 2, 22, 2, 24, 2, 26, 2};
        table[24] = new int[] {2, 2, 4, 4, 8, 4, 12, 2, 14, 2};
        table[25] = new int[] {2, 2, 4, 4, 8, 2, 10, 2};
        table[26] = new int[] {2, 2, 4, 4, 8, 4};
        table[27] = new int[] {2, 2, 4, 4, 8, 4};
        table[28] = new int[] {2, 2, 4, 4, 8, 4, 12, 4};
        table[29] = new int[] {2, 2, 4, 4, 8, 4, 12, 4};
        table[30] = new int[] {2, 2, 4, 4, 8, 4, 12, 4, 16, 4, 20, 4, 24, 4};
        table[31] = new int[] {2, 2, 4, 4, 8, 4, 12, 4, 16, 4, 20, 4};
        table[32] = new int[] {2, 2, 4, 4, 8, 4};
        table[33] = new int[] {2, 2, 4, 4, 8, 4};
        table[34] = new int[] {2, 2};
        return table;
    }

    private static int[] buildEventWindowOffsets() {
        int[] offsets = new int[35];
        java.util.Arrays.fill(offsets, -1);
        for (int code : new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10}) {
            offsets[code] = 12;             // event
        }
        offsets[12] = 4;                    // window
        offsets[13] = 4;                    // drawable
        offsets[14] = 4;                    // drawable
        offsets[15] = 4;                    // window
        offsets[16] = 4;                    // parent
        for (int code : new int[] {17, 18, 19, 21, 22, 24, 26, 27}) {
            offsets[code] = 4;              // event
        }
        offsets[20] = 4;                    // parent
        offsets[23] = 4;                    // parent
        offsets[25] = 4;                    // window
        offsets[28] = 4;                    // window
        offsets[29] = 8;                    // owner
        offsets[33] = 4;                    // window
        return offsets;
    }

    private static void putCard16(byte[] bytes, int offset, short value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 8);
            bytes[offset + 1] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
        }
    }

    private static int readCard32(byte[] bytes, int offset, boolean bigEndian) {
        if (bigEndian) {
            return ((bytes[offset] & 0xFF) << 24) | ((bytes[offset + 1] & 0xFF) << 16)
                    | ((bytes[offset + 2] & 0xFF) << 8) | (bytes[offset + 3] & 0xFF);
        }
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8)
                | ((bytes[offset + 2] & 0xFF) << 16) | ((bytes[offset + 3] & 0xFF) << 24);
    }

    private static void putCard32(byte[] bytes, int offset, int value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 24);
            bytes[offset + 1] = (byte) (value >> 16);
            bytes[offset + 2] = (byte) (value >> 8);
            bytes[offset + 3] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
            bytes[offset + 2] = (byte) (value >> 16);
            bytes[offset + 3] = (byte) (value >> 24);
        }
    }

    /** An event whose bytes are already in the recipient's byte order. */
    private record RawEvent(byte[] bytes, short sequenceNumber, boolean bigEndian)
            implements com.github.moaxcp.x11.protocol.XEvent {

        @Override
        public byte getNumber() {
            return (byte) (bytes[0] & 0x7F);
        }

        @Override
        public byte getResponseCode() {
            return getNumber();
        }

        @Override
        public short getSequenceNumber() {
            return sequenceNumber;
        }

        @Override
        public byte getFirstEventOffset() {
            return 0;
        }

        @Override
        public void write(com.github.moaxcp.x11.protocol.X11Output out) throws java.io.IOException {
            byte[] copy = bytes.clone();
            // The output stream writes in the recipient's order, so patch the sequence number in
            // that same order before handing the bytes over.
            if (bigEndian) {
                copy[2] = (byte) (sequenceNumber >> 8);
                copy[3] = (byte) sequenceNumber;
            } else {
                copy[2] = (byte) sequenceNumber;
                copy[3] = (byte) (sequenceNumber >> 8);
            }
            for (int i = 0; i < 32; i++) {
                out.writeByte(copy[i]);
            }
        }

        @Override
        public int getSize() {
            return 32;
        }

        @Override
        public String getPluginName() {
            return "xproto";
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private XWindow window(ClientConnection client, int id) {
        return (XWindow) server.resources().require(client, id, XResource.CLASS_WINDOW, false);
    }

    /**
     * Converts a property value between the client's byte order and the canonical big-endian
     * storage order. Swapping is its own inverse, so the same call works in both directions.
     */
    private static byte[] normalize(byte[] data, int format, boolean alreadyBigEndian) {
        if (format == 8 || alreadyBigEndian) {
            return data;
        }
        byte[] out = data.clone();
        int unit = format / 8;
        for (int offset = 0; offset + unit <= out.length; offset += unit) {
            for (int i = 0; i < unit / 2; i++) {
                byte tmp = out[offset + i];
                out[offset + i] = out[offset + unit - 1 - i];
                out[offset + unit - 1 - i] = tmp;
            }
        }
        return out;
    }

    private static String ascii(org.eclipse.collections.api.list.primitive.ByteList bytes) {
        return new String(bytes.toArray(), StandardCharsets.ISO_8859_1);
    }

    /** True when a property exists on the window; used by diagnostics. */
    public boolean hasProperty(XWindow window, int atom) {
        Map<Integer, XProperty> properties = window.properties();
        return properties.containsKey(atom);
    }
}
