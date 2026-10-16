package cn.oyzh.fx.xserver.drop;

import com.github.moaxcp.x11.protocol.xproto.SelectionNotifyEvent;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.resource.XProperty;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import cn.oyzh.common.log.JulLog;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A drag from the host's desktop into a forwarded application, which is XDND.
 *
 * <p>X's drag and drop is client to client, and it is the *source* that drives it: it takes the
 * {@code XdndSelection}, sends {@code XdndEnter}, {@code XdndPosition} and {@code XdndDrop} to the
 * window under the pointer, and hands the data over when that window asks for it. A forwarded
 * application is a target that works perfectly well - it has been dragging and dropping on real
 * desktops all along - so what is missing is a source on the X side that speaks for the host's drag.
 * This class is that source.
 *
 * <p>The awkward part is which side of the protocol runs where. The user drags a file from Finder
 * onto the host window; the platform reports the drop to the backend, and from then on nothing is
 * interactive - the X side is a short conversation that has to look exactly like a drag a client
 * started itself. So the bridge runs it in one go: take the selection, tell the window under the
 * pointer where the drag is and what it carries, drop, and answer the {@code ConvertSelection} that
 * comes back with a {@code text/uri-list}.
 *
 * <p>Dragging *out* of a forwarded application is deliberately not here. A JavaFX window cannot
 * start a platform drag - the platform's drag is the user's gesture and the toolkit only reports
 * one - so there is nothing to hand a client's drag to. That half needs a native drag source, and it
 * is recorded as the open part of this item rather than half-built.
 *
 * <p>Threading follows {@link cn.oyzh.fx.xserver.clipboard.ClipboardBridge}: the bridge's
 * state is guarded by the server lock, which request threads already hold, and the one thing that
 * could block - the platform's side of a drop - happens before any of it.
 */
public final class DropBridge {


    /** The selection a drag's data is served through, by the convention every toolkit implements. */
    private static final String XDND_SELECTION = "XdndSelection";

    /** The property a window publishes to say it takes drops, and the version it speaks. */
    private static final String XDND_AWARE = "XdndAware";

    /** What the drag carries: one URI per line, which is what a file drag is. */
    private static final String URI_LIST = "text/uri-list";

    /** The XDND version claimed: 5 is what toolkits have spoken for twenty years. */
    private static final int VERSION = 5;

    /** How long to wait for the target to answer the position before dropping anyway. */
    private static final long STATUS_TIMEOUT_MILLIS = 700;

    private final XServer server;
    /** The bridge's own window: the drag's source, and what the selection is owned by. */
    private final XWindow window;

    private final int selectionAtom;
    private final int uriListAtom;
    private final int textAtom;
    private final int awareAtom;
    private final int enterAtom;
    private final int positionAtom;
    private final int dropAtom;
    private final int finishedAtom;
    private final int actionCopyAtom;

    /** The property a dragging client is asked to leave its file list in. */
    private static final String DRAG_OUT_PROPERTY = "XSERVER_DRAG_OUT";

    // Guarded by the server lock.
    /** What the host handed over, as URIs, while a drag is in flight. */
    private List<String> uris = List.of();

    // Guarded by the server lock.
    /** The client window a forwarded application is dragging *from*, or 0 - see selectionOwnerChanged. */
    private int draggingOwner;
    /** What that drag offered, as URIs, once the client has answered. */
    private List<String> dragOutUris = List.of();
    /** True between asking the dragging client for its files and its answer arriving. */
    private boolean awaitingDragOut;
    private final int dragOutPropertyAtom;
    /** The window being dragged onto, or 0. */
    private int target;
    private long droppedAt;
    private boolean waitingForStatus;

    /** Where the host's paths are on the client's side, longest prefix first - see {@link #forClient}. */
    private final List<String[]> mounts;

    public DropBridge(XServer server) {
        this.server = server;
        this.mounts = new ArrayList<>(server.options().dropMounts());
        mounts.sort((a, b) -> Integer.compare(b[0].length(), a[0].length()));
        window = new XWindow(server.nextServerResourceId(), null, server.screen(),
                server.screen().root(), server.screen().root().depth(), XWindow.INPUT_ONLY,
                server.screen().root().visual(), 0, 0, 1, 1, 0);
        // Registered here rather than in start(), because the window is a child of the root from the
        // moment it is made and **every window in the tree has to be a resource a client can look
        // up**: one that is not is a BadWindow for whoever walks the tree. A bridge that never starts
        // (--no-clipboard, --no-tray) used to leave its window behind unregistered, and xwininfo's
        // own walk then printed "X Error: 3: Bad Window" - measured 2026-09-28 with a real client.
        server.resources().add(window);
        selectionAtom = server.atoms().intern(XDND_SELECTION);
        uriListAtom = server.atoms().intern(URI_LIST);
        textAtom = server.atoms().intern("text/plain");
        awareAtom = server.atoms().intern(XDND_AWARE);
        enterAtom = server.atoms().intern("XdndEnter");
        positionAtom = server.atoms().intern("XdndPosition");
        dropAtom = server.atoms().intern("XdndDrop");
        finishedAtom = server.atoms().intern("XdndFinished");
        actionCopyAtom = server.atoms().intern("XdndActionCopy");
        dragOutPropertyAtom = server.atoms().intern(DRAG_OUT_PROPERTY);
    }

    /**
     * The window the drag's selection is owned by, and the one clients answer to.
     *
     * <p>Never mapped, and {@code InputOnly}: it is a protocol participant, not a piece of user
     * interface - the drag the user sees is the platform's own.
     */
    public void start() {
        // The bridge is a source, not a target: nothing drops onto its own window, so it does not
        // publish XdndAware for itself.
        JulLog.info("host drags into forwarded applications are bridged to XDND");
    }

    public void stop() {
    }

    /** The window the bridge owns the drag's selection with. */
    public int windowId() {
        return window.id();
    }

    public boolean ownsWindow(int windowId) {
        return window.id() == windowId;
    }

    // ------------------------------------------------------------------
    // From the host, under the server lock
    // ------------------------------------------------------------------

    /**
     * The user dropped files from the host's desktop onto a host window.
     *
     * @param paths the dropped files, as paths the host understands
     * @param x     where on the screen it happened, in framebuffer coordinates - the same ones
     *     {@code InputSink} reports, and what XDND wants as root coordinates
     */
    public void hostDropped(List<String> paths, int x, int y) {
        if (paths.isEmpty()) {
            return;
        }
        XWindow under = server.inputFor(server.screen()).pointerWindow();
        XWindow dropTarget = awareTarget(under);
        if (dropTarget == null) {
            // Nothing under the pointer takes drops, which is not an error: a drag onto an
            // application that does not do drag and drop is a drag that did nothing, the same as it
            // would be on a real desktop.
            JulLog.debug("a drop of {} file(s) landed on a window that does not take drops", paths.size());
            return;
        }
        uris = toUris(paths);
        target = dropTarget.id();
        droppedAt = System.currentTimeMillis();
        // The source owns the selection before anything is told about the drag: the target asks for
        // the data through it, and a target that asks in the middle of the conversation has to find
        // an owner.
        server.setSelectionOwner(XDND_SELECTION, window.id(), server.events().time(),
                cn.oyzh.fx.xserver.extension.XFixesExtension.SELECTION_SET_OWNER);
        send(enterAtom, VERSION << 24, uriListAtom, textAtom, 0);
        // The pointer's position and not the drop's: the target tracks a drag by the positions it is
        // sent, and this is the one the drag ended at.
        send(positionAtom, 0, (x << 16) | (y & 0xFFFF), server.events().time(), actionCopyAtom);
        // A target answers the position with XdndStatus and is dropped on afterwards. Toolkits that
        // answer promptly get their answer honoured; one that says nothing is dropped on anyway once
        // the wait is up, because a drag the user has already finished cannot hang on it.
        waitingForStatus = true;
        JulLog.debug("dropping {} file(s) on 0x{}", paths.size(), Integer.toHexString(target));
    }

    /**
     * Advances a drag that is waiting for the target's answer. Called from the display tick, so a
     * target that never answers is still dropped on.
     */
    public void pump() {
        if (!waitingForStatus || System.currentTimeMillis() - droppedAt < STATUS_TIMEOUT_MILLIS) {
            return;
        }
        waitingForStatus = false;
        send(dropAtom, 0, server.events().time());
    }

    /** A client answered something the bridge sent it. */
    public void clientMessage(int messageType, int[] data, ClientConnection from) {
        if (!waitingForStatus || data.length == 0 || data[0] != window.id()) {
            return;
        }
        if (messageType == finishedAtom) {
            // The target has taken the data (or refused it) and the conversation is over.
            waitingForStatus = false;
            target = 0;
            uris = List.of();
            server.setSelectionOwner(XDND_SELECTION, 0, server.events().time(),
                    cn.oyzh.fx.xserver.extension.XFixesExtension.SELECTION_SET_OWNER);
            return;
        }
        // XdndStatus: bit 0 of the flags says the target will take this drop.
        boolean willTake = (data[1] & 1) != 0;
        waitingForStatus = false;
        if (willTake) {
            send(dropAtom, 0, server.events().time());
        } else {
            JulLog.debug("0x{} refused the drop", Integer.toHexString(target));
            target = 0;
            uris = List.of();
        }
    }

    /**
     * Answers a target's {@code ConvertSelection} for the drag's data - which is the only way the
     * files get to it. Lazy, like every X selection: the target asks when it is ready.
     */
    public void serve(XWindow requestor, int selection, int target, int property, int time) {
        if (selection != selectionAtom || property == 0) {
            refuse(requestor, selection, target, time);
            return;
        }
        if (target != uriListAtom && target != textAtom) {
            // TARGETS and anything else: this bridge carries files and nothing else.
            refuse(requestor, selection, target, time);
            return;
        }
        // One URI per line, CRLF separated, which is what text/uri-list is - and what every
        // file-manager drag looks like to the application that receives it.
        StringBuilder list = new StringBuilder();
        for (String uri : uris) {
            list.append(uri).append("\r\n");
        }
        byte[] value = list.toString().getBytes(StandardCharsets.UTF_8);
        int type = server.atoms().intern(URI_LIST, true);
        requestor.properties().put(property, new XProperty(property, type, 8, value));
        server.atomHandlers().notifyProperty(requestor, property,
                cn.oyzh.fx.xserver.handler.AtomHandlers.NEW_VALUE);
        sendNotify(requestor, selection, target, property, time);
    }

    // ------------------------------------------------------------------
    // Out of a forwarded application, which is the other direction
    // ------------------------------------------------------------------

    /**
     * A client took or gave up the drag selection, and that is how this server knows a drag is
     * happening *inside* a forwarded application.
     *
     * <p>X's own drag and drop has no "I am about to drag" message: a client that decides to drag
     * takes the {@code XdndSelection} and starts talking to whatever is under the pointer. So the
     * selection's owner <em>is</em> the signal, and it is the one thing a server can see before the
     * application has committed to a target - which is exactly what the host's side needs, because a
     * platform drag has to be started from the mouse gesture that is already in progress.
     *
     * <p>The bridge's own window owns the same selection during a host-to-X drag; that is the other
     * direction and is not a client's drag, so it is filtered out here.
     */
    public void selectionOwnerChanged(String name, int owner) {
        if (!XDND_SELECTION.equals(name) || ownsWindow(owner)) {
            return;
        }
        draggingOwner = owner;
        dragOutUris = List.of();
        awaitingDragOut = false;
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("a forwarded application is dragging from 0x{}", Integer.toHexString(owner));
        }
        // **Not asked here.** The ask is a round trip to a client that is in the middle of setting up
        // its own drag, and an application dragging something *inside itself* takes the same
        // selection - so asking on sight put a request in front of every internal drag for the sake
        // of a drag that may never leave the screen. Whoever wants the files asks for them (see the
        // C5 entry: the trigger that did this was reverted the same day).
    }

    /** Whether a forwarded application is offering a drag right now. */
    public boolean isDraggingOut() {
        return draggingOwner != 0;
    }

    /**
     * Asks the dragging client what it is offering, as a {@code SelectionRequest} for
     * {@code text/uri-list}.
     *
     * <p>Sent as an event rather than as a protocol request because the bridge is a *requestor* here
     * and the client is the owner - the same shape as the host clipboard's fetch, and the answer
     * arrives through {@link #selectionAnswered}.
     */
    public void askDraggingClient() {
        if (draggingOwner == 0) {
            return;
        }
        XResource resource = server.resources().get(draggingOwner);
        if (!(resource instanceof XWindow source) || source.owner() == null) {
            clearDragOut();
            return;
        }
        // Whatever a previous ask left behind: the answer is read from this property, so it has to
        // be empty first or a refusal would look like data.
        window.properties().remove(dragOutPropertyAtom);
        ClientConnection owner = source.owner();
        server.events().send(owner, com.github.moaxcp.x11.protocol.xproto.SelectionRequestEvent
                .builder()
                .firstEventOffset((byte) 0)
                .sentEvent(false)
                .sequenceNumber(owner.sequenceNumber())
                .time(0)
                .owner(draggingOwner)
                .requestor(window.id())
                .selection(selectionAtom)
                .target(uriListAtom)
                .property(dragOutPropertyAtom)
                .build());
        awaitingDragOut = true;
    }

    /**
     * The dragging client answered the bridge's request: the property it named holds the file list,
     * or {@code None} when it refused this target.
     */
    public void selectionAnswered(int property) {
        if (!awaitingDragOut) {
            return;
        }
        awaitingDragOut = false;
        if (property != dragOutPropertyAtom) {
            JulLog.debug("the dragging client offered no text/uri-list");
            dragOutUris = List.of();
            return;
        }
        XProperty value = window.properties().get(dragOutPropertyAtom);
        if (value == null) {
            dragOutUris = List.of();
            return;
        }
        dragOutUris = parseUriList(value.data());
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("the dragging client offers {} file(s)", dragOutUris.size());
        }
    }

    /** What the drag offers, as paths on the host - what a platform drag's promise resolves to. */
    public List<String> dragOutPaths() {
        List<String> paths = new ArrayList<>(dragOutUris.size());
        for (String uri : dragOutUris) {
            String path = toPath(uri);
            if (path != null) {
                paths.add(path);
            }
        }
        return paths;
    }

    private void clearDragOut() {
        draggingOwner = 0;
        dragOutUris = List.of();
        awaitingDragOut = false;
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    /**
     * The window under the pointer that takes drops, or null.
     *
     * <p>Looked for up the tree, because a toolkit puts the property on the window that should
     * receive the drag and a child of it is usually what the pointer is actually over.
     */
    private XWindow awareTarget(XWindow window) {
        for (XWindow candidate = window; candidate != null; candidate = candidate.parent()) {
            XProperty aware = candidate.properties().get(awareAtom);
            if (aware != null && aware.format() == 32) {
                return candidate;
            }
        }
        return null;
    }

    /** A {@code ClientMessage} to the target, with no event mask - the only form XDND uses. */
    private void send(int messageType, int... data) {
        if (target == 0) {
            return;
        }
        XResource resource = server.resources().get(target);
        if (!(resource instanceof XWindow destination) || destination.owner() == null) {
            target = 0;
            return;
        }
        ClientConnection owner = destination.owner();
        server.events().send(owner, com.github.moaxcp.x11.protocol.xproto.ClientMessageEvent.builder()
                .firstEventOffset((byte) 0)
                .sentEvent(false)
                .sequenceNumber(owner.sequenceNumber())
                .window(destination.id())
                .type(messageType)
                .format((byte) 32)
                .data(new com.github.moaxcp.x11.protocol.xproto.ClientMessageData32(pad(data)))
                .build());
    }

    /** The message's five data words, with the source window first as every XDND message has it. */
    private int[] pad(int[] data) {
        int[] words = new int[5];
        words[0] = window.id();
        for (int i = 0; i < Math.min(4, data.length); i++) {
            words[i + 1] = data[i];
        }
        return words;
    }

    private void refuse(XWindow requestor, int selection, int target, int time) {
        sendNotify(requestor, selection, target, 0, time);
    }

    private void sendNotify(XWindow requestor, int selection, int target, int property, int time) {
        ClientConnection owner = requestor.owner();
        if (owner == null) {
            return;
        }
        server.events().send(owner, SelectionNotifyEvent.builder()
                .firstEventOffset((byte) 0)
                .sentEvent(false)
                .sequenceNumber(owner.sequenceNumber())
                .time(time)
                .requestor(requestor.id())
                .selection(selection)
                .target(target)
                .property(property)
                .build());
    }

    /**
     * A {@code text/uri-list} as URIs: one per line, {@code #} starts a comment, and CRLF is the
     * separator every file manager writes.
     */
    private static List<String> parseUriList(byte[] value) {
        List<String> uris = new ArrayList<>();
        for (String line : new String(value, StandardCharsets.UTF_8).split("\r?\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                uris.add(trimmed);
            }
        }
        return uris;
    }

    /** The path a {@code file:} URI names, or null for a URI that is not a local file. */
    private static String toPath(String uri) {
        if (!uri.startsWith("file://")) {
            return null;
        }
        String path = uri.substring("file://".length());
        if (path.isEmpty()) {
            return null;
        }
        // A URI names a host between the scheme and the path; an empty one, or this machine's own
        // name, is a local file and anything else is not ours to hand over.
        if (!path.startsWith("/")) {
            int slash = path.indexOf('/');
            if (slash < 0) {
                return null;
            }
            String host = path.substring(0, slash);
            if (!host.equalsIgnoreCase("localhost")) {
                return null;
            }
            path = path.substring(slash);
        }
        return decode(path);
    }

    /**
     * The dropped paths as URIs, which is what a target understands.
     *
     * <p><b>Every byte outside the unreserved set is escaped, and only spaces were before</b> - which
     * breaks on the names files actually have: `#` starts a fragment, `%` starts an escape, and a
     * non-ASCII name (which is the common case for this server's own user) is not a byte a URI may
     * carry raw. The escape is UTF-8, byte by byte, which is what a target decodes.
     *
     * <p><b>And the path is the one the user sees, not the one the platform resolved.</b> macOS
     * reports a dragged file as {@code /private/tmp/...} because `/tmp` is a symlink to it, so a
     * target that shares `/tmp` - a client on the same machine, or a mounted directory - is handed a
     * path that does not exist for it. The prefix is dropped when what is left still exists, which is
     * how this was found: a real drop reached a forwarded application and the file was, from its side,
     * nowhere.
     */
    private List<String> toUris(List<String> paths) {
        List<String> uris = new ArrayList<>(paths.size());
        for (String path : paths) {
            String normalised = forClient(userFacing(path));
            uris.add(java.io.File.separatorChar == '\\'
                    ? "file:///" + encode(normalised.replace('\\', '/'))
                    : "file://" + encode(normalised));
        }
        return uris;
    }

    /**
     * The path as the <em>client</em> will see it, when the user has said where the host's directory
     * is on that side ({@code --drop-mount /Users/me/Shared=/mnt/hgfs/Shared}).
     *
     * <p>This is the one thing that makes a drag onto a forwarded application do something useful
     * across machines: the application opens the path it is handed, so the path has to exist where
     * the application runs. Without a mapping it is reported as it is, which is right for a client on
     * this machine and for one that mounts the same path.
     */
    private String forClient(String path) {
        for (String[] mount : mounts) {
            String from = mount[0];
            if (path.equals(from) || path.startsWith(from.endsWith("/") ? from : from + "/")) {
                return mount[1] + path.substring(from.length());
            }
        }
        return path;
    }

    /** The path without macOS's resolved-file prefix, when the remainder is the same file. */
    private static String userFacing(String path) {
        if (path.startsWith("/private/") && new java.io.File(path.substring("/private".length()))
                .exists()) {
            return path.substring("/private".length());
        }
        return path;
    }

    /** A path as the URI form: everything but the unreserved set and the separator escaped, UTF-8. */
    private static String encode(String path) {
        StringBuilder out = new StringBuilder(path.length() + 8);
        for (byte b : path.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xFF;
            boolean plain = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '.' || c == '_' || c == '~' || c == '/';
            if (plain) {
                out.append((char) c);
            } else {
                out.append('%').append(String.format("%02X", c));
            }
        }
        return out.toString();
    }

    /** The inverse: every {@code %XX} decoded as the UTF-8 byte it stands for. */
    private static String decode(String uri) {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream(uri.length());
        for (int i = 0; i < uri.length(); i++) {
            char c = uri.charAt(i);
            if (c == '%' && i + 2 < uri.length()) {
                int hi = Character.digit(uri.charAt(i + 1), 16);
                int lo = Character.digit(uri.charAt(i + 2), 16);
                if (hi >= 0 && lo >= 0) {
                    bytes.write((hi << 4) | lo);
                    i += 2;
                    continue;
                }
            }
            byte[] raw = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
            bytes.write(raw, 0, raw.length);
        }
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }
}
