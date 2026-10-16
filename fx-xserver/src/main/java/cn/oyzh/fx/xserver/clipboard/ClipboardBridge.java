package cn.oyzh.fx.xserver.clipboard;

import com.github.moaxcp.x11.protocol.xproto.SelectionNotifyEvent;
import com.github.moaxcp.x11.protocol.xproto.SelectionRequestEvent;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.clipboard.HostClipboard;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.extension.XFixesExtension;
import cn.oyzh.fx.xserver.resource.XProperty;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The host's clipboard and this server's {@code CLIPBOARD} selection, kept in step.
 *
 * <p>Crossing the two is not a copy: an X selection is <em>lazy</em> - the owner holds the data and
 * hands it over when a requestor asks - while a host clipboard is a place data is put. So the bridge
 * does two things, one for each direction, and neither is a transfer it initiates on the user's
 * behalf:
 *
 * <ul>
 *   <li><b>Host to X.</b> It owns {@code CLIPBOARD} itself whenever a client does not, and answers a
 *       client's {@code ConvertSelection} with the host's text. A forwarded application can paste
 *       what the user copied on the Mac without the Mac knowing anything happened.
 *   <li><b>X to host.</b> When a client takes the selection, the bridge asks that client for its
 *       data and puts it on the host clipboard. This is the half that has to be eager: macOS does
 *       not tell anybody that a pasteboard was <em>read</em>, so there is no "someone is about to
 *       paste" moment to fetch on. Every real bridge on a platform without that notification does
 *       the same thing, and it is why copying inside a forwarded application replaces the Mac's
 *       clipboard.
 * </ul>
 *
 * <p><b>There is one thread, and it is not a request thread.</b> Reading the host clipboard is a
 * round trip to the platform's clipboard service and can block; every X request is executed while
 * the server lock is held, so a bridge that read it on the request path would stall every client
 * whenever the platform's clipboard was slow. Instead a poll compares the host's text with the last
 * one it saw (every {@value #POLL_MILLIS} ms, which is the same order as the platform's own change
 * notifications), and the request path only ever reads the copy - see {@link #hostText}.
 *
 * <p><b>The same lock guards the bridge.</b> Its state is touched from request threads, which hold
 * the server lock, and from the poll thread, which takes it; nothing else is used for synchronization
 * so that the two can never take them in opposite orders. The host clipboard is read and written
 * <em>outside</em> that lock, deliberately.
 *
 * <p><b>One window per screen.</b> The bridge is a protocol participant rather than a client, so its
 * windows are the server's own - and in the default mode each connection has a screen of its own, so
 * a single window would be a window on a root that connection's setup reply never named. A toolkit
 * wraps whatever window the server names to it, so the id it is asked through has to be on a screen
 * it knows; {@link #windowsByRoot} has the measurement and {@link #window} what the exception is.
 *
 * <p>Text only, and {@code CLIPBOARD} only. A host pasteboard is not a three-button X pointer, so
 * {@code PRIMARY} has nothing here to mirror; and this server has one screen with one mouse, so a
 * {@code PRIMARY} selection would be a second, invisible clipboard nobody could reach.
 *
 * <p>Not implemented, and worth knowing: the bridge <em>serves</em> a property in one write rather
 * than through {@code INCR}. It can, because it is not a client and is not bounded by a request
 * size, and a requestor that asks for a large property gets all of it. It <em>receives</em> through
 * {@code INCR} when it must, because a source client's choice of {@code INCR} is not one this
 * server gets to make for it.
 */
public final class ClipboardBridge {

    private static final Logger log = LoggerFactory.getLogger(ClipboardBridge.class);

    /** The selection that is bridged. */
    private static final String CLIPBOARD = "CLIPBOARD";

    /** How often the host clipboard is compared with the last value seen. */
    private static final long POLL_MILLIS = 200;

    /** How long a fetch from a client may stall before it is given up on. */
    private static final long FETCH_TIMEOUT_MILLIS = 8000;

    /** The targets asked for, in the order they are tried: the modern name first. */
    private static final String[] FETCH_TARGETS = {"UTF8_STRING", "STRING", "TEXT"};

    private final XServer server;
    private final HostClipboard host;

    /**
     * The bridge's window on the master screen, which is the id the selection is owned by.
     *
     * <p>The one screen every client shares is where a selection's <em>owner</em> lives, so this is
     * the window {@code GetSelectionOwner} answers with and the one a client is told about when the
     * bridge takes the selection over. Nothing a client does with that id needs a window it can
     * place - a client asks the owner for data through {@code ConvertSelection}, and the answer is
     * written on the client's own window - which is why one window serves it for every screen: see
     * {@link #windowFor} for the half that does have to be per screen.
     */
    private final XWindow window;

    /**
     * The bridge's window on each screen, keyed by the screen's root window, made when one is first
     * needed.
     *
     * <p><b>A client must never be handed a window that is not on one of its own screens.</b> A
     * client's setup reply names the screens it may use, and a toolkit wraps whatever window this
     * server names to it - GDK does that for the {@code requestor} of a {@code SelectionRequest}, in
     * {@code gdk_x11_window_foreign_new_for_display}. A window whose root is not among those screens
     * is one the client cannot place, and GDK 3.24 crashes on it instead of refusing it: measured
     * 2026-09-30, a GTK3 client on a screen of its own died in {@code XScreenNumberOfScreen} - called
     * with the attributes struct a failed lookup left uninitialised - inside its own event loop, and
     * EasyShell died the same way every time a copy made this bridge ask it for the data.
     *
     * <p>That is the default mode's doing: every connection there gets a screen of its own (see
     * {@code XServer.openClientScreen}), so the master screen's window - which is what a bridge made
     * once at startup has - is on a root no client's setup reply mentions. In single-window mode
     * every client is on the master screen and this map holds nothing.
     */
    private final Map<Integer, XWindow> windowsByRoot = new HashMap<>();

    /** Every window above by id, which is how a requestor this bridge is asked about is recognised. */
    private final Map<Integer, XWindow> windowsById = new HashMap<>();

    private final int clipboardAtom;
    private final int propertyAtom;
    private final int targetsAtom;
    private final int atomAtom;
    private final int incrAtom;
    private final int[] textAtoms;

    private Thread worker;
    private volatile boolean running;

    // Everything below is guarded by the server lock, which request threads already hold and the
    // poll thread takes around each step. See the class comment for why there is no second lock.

    /** The host's text as last seen, and what a client is served from. */
    private String hostText;

    /** Text an X client copied, waiting to be written to the host by the poll thread. */
    private String outgoing;

    /**
     * Told whenever the text the selection serves changes, with the new text or null.
     *
     * <p>The hook the VNC backend uses (D2): a viewer has to be sent what X copied, and this is the
     * one place that knows the selection's text has changed - whichever direction it came from. It
     * is called <b>under the server lock</b>, so an implementation must not block: queue the text and
     * let the thread that owns the socket send it.
     */
    private java.util.function.Consumer<String> listener;

    /** The fetch in flight: whose data was asked for, with which target, and how it is going. */
    private int fetchWindow;
    /**
     * The bridge's window the request went out through, which is the one the owner was asked to put
     * the answer on - the owner's own screen's, see {@link #windowFor}. Kept because the answer
     * arrives as an event addressed to it and is read out of its properties.
     */
    private XWindow fetchRequestor;
    private int fetchTarget;
    private int fetchAttempt;
    private long fetchStartedAt;
    private boolean fetchingIncr;
    private long fetchProgressAt;
    private final ByteArrayOutputStream fetched = new ByteArrayOutputStream();

    public ClipboardBridge(XServer server, HostClipboard host) {
        this.server = server;
        this.host = host;
        window = new XWindow(server.nextServerResourceId(), null, server.screen(),
                server.screen().root(), server.screen().root().depth(), XWindow.INPUT_ONLY,
                server.screen().root().visual(), 0, 0, 1, 1, 0);
        // Registered here rather than in start(), because the window is a child of the root from the
        // moment it is made and **every window in the tree has to be a resource a client can look
        // up**: one that is not is a BadWindow for whoever walks the tree. A bridge that never starts
        // (--no-clipboard, --no-tray) used to leave its window behind unregistered, and xwininfo's
        // own walk then printed "X Error: 3: Bad Window" - measured 2026-09-28 with a real client.
        server.resources().add(window);
        windowsByRoot.put(server.screen().root().id(), window);
        windowsById.put(window.id(), window);
        clipboardAtom = server.atoms().intern(CLIPBOARD);
        // Where a source client puts the data it hands over. Named after this server so a client
        // that lists properties on the window it was asked about sees something recognisable.
        propertyAtom = server.atoms().intern("XSERVER_CLIPBOARD");
        targetsAtom = server.atoms().intern("TARGETS");
        atomAtom = server.atoms().intern("ATOM");
        incrAtom = server.atoms().intern("INCR");
        textAtoms = new int[FETCH_TARGETS.length];
        for (int i = 0; i < FETCH_TARGETS.length; i++) {
            textAtoms[i] = server.atoms().intern(FETCH_TARGETS[i]);
        }
    }

    /**
     * Creates the bridge's window and starts polling.
     *
     * <p>The window is never mapped and is {@code InputOnly}: it is a protocol participant, not a
     * piece of user interface, and the host window sizes itself to the client's mapped windows - a
     * visible bridge window would be a window the user never asked for.
     */
    /** The hook a backend registers to hear about clipboard changes; see {@link #listener}. */
    public void setListener(java.util.function.Consumer<String> listener) {
        this.listener = listener;
    }

    /**
     * Clipboard text from outside the X session - a VNC viewer's copy (D2).
     *
     * <p>It goes down the path an X client's copy takes: the poll thread writes it to the host's
     * pasteboard, and from there the bridge serves it to X clients the way it serves anything else.
     * That is deliberate and not a shortcut - the host's pasteboard remains the single source the
     * bridge serves from, so "somebody copied on the Mac" and "a viewer copied" cannot fight over
     * the selection, and a viewer's copy reaching the Mac's clipboard is what every remote-desktop
     * client does.
     */
    public void remoteCopy(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        server.lockServer();
        try {
            outgoing = text;
        } finally {
            server.unlockServer();
        }
    }

    public void start() {
        running = true;
        worker = new Thread(this::poll, "xserver-clipboard");
        worker.setDaemon(true);
        worker.start();
        log.info("host clipboard bridged to the X CLIPBOARD selection");
    }

    public void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
    }

    /** The window the selection is owned by, which is what makes the bridge findable. */
    public int windowId() {
        return window.id();
    }

    /**
     * Whether a window is the bridge's own, which is how a requestor that is not a client shows up.
     */
    public boolean ownsWindow(int windowId) {
        return windowsById.containsKey(windowId);
    }

    /**
     * The bridge's window on {@code screen}, made the first time it is needed.
     *
     * <p>Called under the server lock, which the map is guarded by like the rest of this class's
     * state. See {@link #windowsByRoot} for why this cannot be one window.
     */
    private XWindow windowFor(Screen screen) {
        int root = screen.root().id();
        XWindow existing = windowsByRoot.get(root);
        if (existing != null) {
            return existing;
        }
        XWindow made = new XWindow(server.nextServerResourceId(), null, screen, screen.root(),
                screen.root().depth(), XWindow.INPUT_ONLY, screen.root().visual(), 0, 0, 1, 1, 0);
        server.resources().add(made);
        windowsByRoot.put(root, made);
        windowsById.put(made.id(), made);
        return made;
    }

    // ------------------------------------------------------------------
    // Called from the request path, under the server lock
    // ------------------------------------------------------------------

    /**
     * A selection changed hands. The bridge listens to {@code CLIPBOARD} and does one of three
     * things: ignores its own taking of it, fetches from the client that just took it, or takes it
     * itself when it is left free - which is what keeps "copy on the Mac, paste in the application"
     * working after the application that owned it has gone away.
     */
    public void selectionOwnerChanged(String name, int owner) {
        if (!CLIPBOARD.equals(name) || ownsWindow(owner)) {
            return;
        }
        if (owner == 0) {
            // Free again: take it, so a paste still finds the host's text - but only when there is
            // text, or the bridge would hold a selection it has nothing to answer with.
            if (hostText != null) {
                takeOwnership();
            }
            return;
        }
        startFetch(owner);
    }

    /**
     * Answers a client's {@code ConvertSelection} for a selection this bridge owns.
     *
     * <p>Served from the copy the poll thread keeps, never by reading the host clipboard here: this
     * runs while the server lock is held, and a platform clipboard read can block.
     */
    public void serve(ClientConnection client, XWindow requestor, int selection, int target,
                      int property, int time) {
        if (log.isDebugEnabled()) {
            // Which target a client asks for is the whole of "why did my paste get nothing": a
            // toolkit that asks for one this bridge cannot serve goes away empty-handed, and the
            // request is the only place that says so.
            log.debug("clipboard: 0x{} asks for {} into {}", Integer.toHexString(requestor.id()),
                    name(target), name(property));
        }
        if (property == 0) {
            // A requestor that names no property is asking a question it does not want answered in
            // data - the convention is to refuse, which is what a real owner does.
            refuse(requestor, selection, target, time);
            return;
        }
        String text = hostText == null ? "" : hostText;
        if (target == targetsAtom) {
            int[] targets = {targetsAtom, textAtoms[0], textAtoms[1], textAtoms[2]};
            byte[] data = new byte[targets.length * 4];
            for (int i = 0; i < targets.length; i++) {
                putBigEndian(data, i * 4, targets[i]);
            }
            setProperty(requestor, property, atomAtom, 32, data);
        } else if (isTextTarget(target)) {
            setProperty(requestor, property, target, 8, text.getBytes(StandardCharsets.UTF_8));
        } else {
            refuse(requestor, selection, target, time);
            return;
        }
        notify(requestor, selection, target, property, time);
    }

    /**
     * A client answered a selection request the bridge made - the {@code SelectionNotify} it sent
     * back carries the property the data is in, or {@code None} when it refused the target.
     */
    public void selectionAnswered(int property) {
        if (fetchWindow == 0) {
            return;
        }
        if (property != propertyAtom) {
            // Refused for this target: ask for the next one down the list, or give up.
            fetchAttempt++;
            if (fetchAttempt >= textAtoms.length) {
                log.debug("the owner of {} offered none of the text targets", CLIPBOARD);
                clearFetch();
                return;
            }
            fetchTarget = textAtoms[fetchAttempt];
            askOwner();
            return;
        }
        XProperty value = fetchRequestor.properties().get(propertyAtom);
        if (value == null) {
            clearFetch();
            return;
        }
        if (value.typeAtom() == incrAtom) {
            // The data does not fit in one property. The requestor's half of the handshake is to
            // delete the property, which is what tells the source to start sending; from there the
            // poll reads and deletes each chunk as it arrives.
            fetchingIncr = true;
            fetched.reset();
            fetchRequestor.properties().remove(propertyAtom);
            server.atomHandlers().notifyProperty(fetchRequestor, propertyAtom,
                    cn.oyzh.fx.xserver.handler.AtomHandlers.DELETED);
            fetchProgressAt = System.currentTimeMillis();
            log.debug("the owner of {} is sending the data in chunks", CLIPBOARD);
            return;
        }
        outgoing = decode(value.typeAtom(), value.data());
        clearFetch();
    }

    // ------------------------------------------------------------------
    // The poll
    // ------------------------------------------------------------------

    private void poll() {
        while (running) {
            try {
                pump();
                Thread.sleep(POLL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException e) {
                // The bridge is not worth taking the server down for: a platform clipboard that
                // misbehaves costs the user copy and paste, not their session.
                log.warn("clipboard bridge: {}", e.toString());
            }
        }
    }

    /** One turn: push what an X client copied, notice what the host holds, and advance a fetch. */
    private void pump() {
        String toWrite = takeOutgoing();
        if (toWrite != null) {
            // Outside the server lock: this is the platform call that can block.
            host.writeText(toWrite);
            server.lockServer();
            try {
                // Recorded as the last text seen, so the comparison below does not read it back as
                // an outside change and steal the selection from the client that just copied.
                hostText = toWrite;
                if (listener != null) {
                    listener.accept(toWrite);
                }
            } finally {
                server.unlockServer();
            }
            return;
        }

        String now = host.readText();
        server.lockServer();
        try {
            if (now == null) {
                if (hostText != null) {
                    // The host clipboard stopped offering text - a picture was copied, or it was
                    // cleared. Give the selection up rather than answer a paste with nothing: an
                    // application that finds no owner leaves what the user had alone.
                    hostText = null;
                    releaseOwnership();
                    if (listener != null) {
                        listener.accept(null);
                    }
                }
            } else if (!now.equals(hostText)) {
                hostText = now;
                if (listener != null) {
                    listener.accept(now);
                }
                if (fetchWindow == 0) {
                    // The host changed and it was not this bridge that changed it, so somebody
                    // copied on the Mac: take the selection, or an X paste would be answered by the
                    // client that owned it with what it had before.
                    takeOwnership();
                }
            }
        } finally {
            server.unlockServer();
        }

        server.lockServer();
        try {
            drainFetch();
        } finally {
            server.unlockServer();
        }
    }

    private String takeOutgoing() {
        server.lockServer();
        try {
            String text = outgoing;
            outgoing = null;
            return text;
        } finally {
            server.unlockServer();
        }
    }

    /** Advances an {@code INCR} transfer, if one is in flight. Called under the server lock. */
    private void drainFetch() {
        if (fetchWindow == 0) {
            return;
        }
        if (!fetchingIncr) {
            if (System.currentTimeMillis() - fetchStartedAt > FETCH_TIMEOUT_MILLIS) {
                log.debug("the owner of {} never answered the selection request", CLIPBOARD);
                clearFetch();
            }
            return;
        }
        XProperty value = fetchRequestor.properties().get(propertyAtom);
        if (value == null) {
            if (System.currentTimeMillis() - fetchProgressAt > FETCH_TIMEOUT_MILLIS) {
                log.debug("{} transfer stalled after {} bytes", "INCR", fetched.size());
                finishIncr();
            }
            return;
        }
        byte[] chunk = value.data();
        fetchRequestor.properties().remove(propertyAtom);
        server.atomHandlers().notifyProperty(fetchRequestor, propertyAtom,
                cn.oyzh.fx.xserver.handler.AtomHandlers.DELETED);
        if (chunk.length == 0) {
            finishIncr();
            return;
        }
        fetched.write(chunk, 0, chunk.length);
        fetchProgressAt = System.currentTimeMillis();
    }

    private void finishIncr() {
        outgoing = decode(fetchTarget, fetched.toByteArray());
        clearFetch();
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    private void takeOwnership() {
        if (server.selectionOwner(CLIPBOARD) == window.id()) {
            return;
        }
        server.setSelectionOwner(CLIPBOARD, window.id(), server.events().time(),
                XFixesExtension.SELECTION_SET_OWNER);
    }

    /** Gives the selection up, unless a client has taken it in the meantime. */
    private void releaseOwnership() {
        if (server.selectionOwner(CLIPBOARD) != window.id()) {
            return;
        }
        server.setSelectionOwner(CLIPBOARD, 0, server.events().time(),
                XFixesExtension.SELECTION_SET_OWNER);
    }

    private void startFetch(int owner) {
        fetchWindow = owner;
        fetchAttempt = 0;
        fetchTarget = textAtoms[0];
        fetchingIncr = false;
        fetched.reset();
        fetchStartedAt = System.currentTimeMillis();
        askOwner();
    }

    /** Sends the source client a request for {@link #fetchTarget}. Called under the server lock. */
    private void askOwner() {
        XResource resource = server.resources().get(fetchWindow);
        if (!(resource instanceof XWindow source) || source.owner() == null) {
            clearFetch();
            return;
        }
        // The request is made through the bridge's window on the **owner's** screen, which is the
        // whole reason there is more than one: this id is what the owner wraps before it answers, so
        // it has to be a window the owner's setup reply put on a screen it knows (see windowsByRoot).
        fetchRequestor = windowFor(source.screen());
        // Whatever a previous attempt left behind: the answer is read from this property, so it has
        // to be empty first or a refusal would look like data.
        fetchRequestor.properties().remove(propertyAtom);
        ClientConnection owner = source.owner();
        server.events().send(owner, SelectionRequestEvent.builder()
                .firstEventOffset((byte) 0)
                .sentEvent(false)
                .sequenceNumber(owner.sequenceNumber())
                .time(0)
                .owner(fetchWindow)
                .requestor(fetchRequestor.id())
                .selection(clipboardAtom)
                .target(fetchTarget)
                .property(propertyAtom)
                .build());
    }

    private void clearFetch() {
        fetchWindow = 0;
        fetchRequestor = null;
        fetchAttempt = 0;
        fetchingIncr = false;
        fetched.reset();
    }

    private boolean isTextTarget(int target) {
        for (int textAtom : textAtoms) {
            if (textAtom == target) {
                return true;
            }
        }
        return false;
    }

    /**
     * The text a source client handed over.
     *
     * <p>{@code UTF8_STRING} and {@code TEXT} are UTF-8 by definition. {@code STRING} is Latin-1 by
     * definition, but is what a client that has no {@code UTF8_STRING} uses, and many of them put
     * UTF-8 in it anyway - so it is read as UTF-8 when it is valid UTF-8 and as Latin-1 when it is
     * not, which is the only reading that gets both kinds of client right.
     */
    private static String decode(int type, byte[] data) {
        if (type == 0 || data.length == 0) {
            return "";
        }
        if (isValidUtf8(data)) {
            return new String(data, StandardCharsets.UTF_8);
        }
        return new String(data, StandardCharsets.ISO_8859_1);
    }

    private static boolean isValidUtf8(byte[] data) {
        try {
            StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                    .decode(java.nio.ByteBuffer.wrap(data));
            return true;
        } catch (java.nio.charset.CharacterCodingException e) {
            return false;
        }
    }

    private void setProperty(XWindow target, int property, int type, int format, byte[] data) {
        target.properties().put(property, new XProperty(property, type, format, data));
        server.atomHandlers().notifyProperty(target, property,
                cn.oyzh.fx.xserver.handler.AtomHandlers.NEW_VALUE);
    }

    private void refuse(XWindow requestor, int selection, int target, int time) {
        notify(requestor, selection, target, 0, time);
    }

    private void notify(XWindow requestor, int selection, int target, int property, int time) {
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

    /** An atom's name for a log line, or its number when it has none. */
    private String name(int atom) {
        String name = server.atoms().name(atom);
        return name != null ? name : (atom == 0 ? "None" : "atom " + atom);
    }

    private static void putBigEndian(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) (value >> 24);
        bytes[offset + 1] = (byte) (value >> 16);
        bytes[offset + 2] = (byte) (value >> 8);
        bytes[offset + 3] = (byte) value;
    }
}
