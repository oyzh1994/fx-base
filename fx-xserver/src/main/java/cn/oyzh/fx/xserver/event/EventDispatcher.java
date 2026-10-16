package cn.oyzh.fx.xserver.event;

import com.github.moaxcp.x11.protocol.XEvent;
import com.github.moaxcp.x11.protocol.xproto.EnterNotifyEvent;
import com.github.moaxcp.x11.protocol.xproto.FocusInEvent;
import com.github.moaxcp.x11.protocol.xproto.FocusOutEvent;
import com.github.moaxcp.x11.protocol.xproto.LeaveNotifyEvent;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.resource.XWindow;
import cn.oyzh.fx.xserver.resource.XWindow.OtherClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Builds and delivers events.
 *
 * <p>The generated event classes are immutable, so a delivery target is never patched into an
 * event after the fact: callers supply a factory and one event is built per recipient, carrying
 * that recipient's sequence number, the window the event is reported against, the child window
 * and window relative coordinates. That is what makes propagation and grabs work, since the same
 * logical event is reported relative to different windows for different clients.
 */
public final class EventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EventDispatcher.class);

    /**
     * Detail codes for EnterNotify, LeaveNotify, FocusIn and FocusOut: one lattice, shared.
     *
     * <p>The first five say how two windows are related, which is how a toolkit tells a move that
     * stayed inside its own tree from one that left it. The last two say that one of the two is not
     * a window at all, and only a focus change can produce them.
     */
    public static final byte NOTIFY_ANCESTOR = 0;
    public static final byte NOTIFY_VIRTUAL = 1;
    public static final byte NOTIFY_INFERIOR = 2;
    public static final byte NOTIFY_NONLINEAR = 3;
    public static final byte NOTIFY_NONLINEAR_VIRTUAL = 4;
    /**
     * The focus is PointerRoot, so the window this is reported against is the one the pointer is in
     * rather than one the focus moved to.
     */
    public static final byte NOTIFY_POINTER = 5;

    /**
     * Modes: what a grab did to a change, numbered as the protocol numbers them.
     *
     * <p>A keyboard grab puts the focus on the window it is taken on and gives it back when it ends,
     * and those two changes are reported as {@code NotifyGrab} and {@code NotifyUngrab}; a focus
     * change asked for *while* a grab is held is reported as {@code NotifyWhileGrabbed}. A toolkit
     * reads the mode to tell "my window lost the focus because somebody grabbed the keyboard" from
     * "because the user clicked elsewhere", which decides whether a menu or a dialog should keep
     * running its own focus handling.
     *
     * <p>All of it was measured rather than reasoned: {@code tools/FocusProbe} against a reference
     * server, step by step - including the two cases prose does not mention, that a grab on the
     * window that already has the focus still reports the pair, and that a grab taken with the focus
     * on nothing at all reports nothing.
     */
    public static final byte NOTIFY_MODE_NORMAL = 0;
    public static final byte NOTIFY_MODE_GRAB = 1;
    public static final byte NOTIFY_MODE_UNGRAB = 2;
    public static final byte NOTIFY_MODE_WHILE_GRABBED = 3;

    private final XServer server;

    public EventDispatcher(XServer server) {
        this.server = server;
    }

    /**
     * Builds one event for a specific delivery target.
     *
     * @param recipient   the client that will receive this copy
     * @param eventWindow the window the event is reported against
     * @param child       the child of that window the pointer is in, or null
     * @param x           x relative to {@code eventWindow}
     * @param y           y relative to {@code eventWindow}
     */
    public interface EventFactory {
        XEvent create(ClientConnection recipient, XWindow eventWindow, XWindow child, int x, int y);
    }

    /** Milliseconds truncated to the 32 bit X timestamp. */
    public int time() {
        return (int) (System.currentTimeMillis() & 0xFFFFFFFFL);
    }

    /**
     * Sends an already built event, silently dropping it for dead connections.
     *
     * <p><b>This is where a core event becomes its XInput2 twin</b>, and it is the only place - the
     * conversion needs the recipient (whose byte order and sequence number the event carries) and
     * the event itself, and both are here. A client that selected the XI2 version of an event is
     * sent that and <em>not</em> the core one, which is what a reference server was measured doing:
     * a client with {@code XI_ButtonPress} selected gets one event per click even though it also
     * asked for {@code ButtonPressMask}, and a toolkit that handles both rails would otherwise
     * handle every click twice.
     */
    public void send(ClientConnection client, XEvent event) {
        if (client == null || client.isClosed() || client.isShuttingDown()) {
            return;
        }
        var xinput = server.xinput();
        if (xinput != null) {
            XEvent twin = xinput.twinOf(client, event);
            if (twin != null) {
                event = twin;
            }
        }
        try {
            client.output().writeEvent(event);
            if (log.isDebugEnabled()) {
                // What the server tells the client matters as much as what the client asks for: a
                // toolkit that never learns its window is visible will never draw into it.
                // The event itself rather than its class name: half of what is delivered now is an
                // Xi2Event, and "Xi2Event" forty times over says nothing about which device event it
                // was - which is exactly what a delivery question turns on. Xi2Event's toString
                // carries the evtype and the device for that reason.
                log.debug("event {} -> {}", event.getClass() == Xi2Event.class
                        ? event.toString() : event.getClass().getSimpleName(), client);
            }
        } catch (IOException e) {
            log.debug("dropping event for {}: {}", client.peer(), e.getMessage());
            client.close();
        }
    }

    /**
     * Delivers to every client that selected {@code maskBit} on {@code window}: the owner plus
     * any client that registered interest through {@code ChangeWindowAttributes}.
     *
     * @return the number of clients the event reached
     */
    public int deliverToWindow(XWindow window, int maskBit, EventFactory factory) {
        int delivered = 0;
        ClientConnection owner = window.owner();
        if (owner != null && interested(owner, window, maskBit)) {
            send(owner, factory.create(owner, window, null, 0, 0));
            delivered++;
        }
        for (OtherClient other : window.otherClients()) {
            if (interested(other.client, window, maskBit)) {
                send(other.client, factory.create(other.client, window, null, 0, 0));
                delivered++;
            }
        }
        // **And a client that selected only the device event, which neither list above holds.** The
        // owner's mask and `otherClients` are the *core* selections; a client that called
        // XISelectEvents and never asked for the core mask appears in neither, so it was reached by
        // nothing - measured with `xinput test-xi2 --root`, whose mask is XI2 only: the server logged
        // the button press delivered to the root and sent the client no event at all. GDK asks for
        // both, which is why the tests written against it passed.
        var xinput = server.xinput();
        if (xinput != null) {
            for (XWindow.Xi2Selection selection : window.xi2Selections()) {
                ClientConnection client = selection.client();
                if (client == owner || inOtherClients(window, client)) {
                    continue;                       // already reached above, on one rail or the other
                }
                if (xinput.wants(client, window, maskBit)) {
                    send(client, factory.create(client, window, null, 0, 0));
                    delivered++;
                }
            }
        }
        return delivered;
    }

    /** Whether a client is in the window's core-selection list, so it is not told twice. */
    private static boolean inOtherClients(XWindow window, ClientConnection client) {
        for (OtherClient other : window.otherClients()) {
            if (other.client == client) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a client wants an event of this kind on this window, on either rail.
     *
     * <p>A client that selected only the XInput2 version of an event is as interested as one that
     * selected the core mask - that is the whole point of the extension - and the event it is handed
     * is the XI2 one, which {@link #send} substitutes on the way out. A toolkit that subscribes
     * through XI2 and nothing else is the ordinary case, not the exception.
     */
    private boolean interested(ClientConnection client, XWindow window, int maskBit) {
        if ((window.eventMaskFor(client) & maskBit) != 0) {
            return true;
        }
        var xinput = server.xinput();
        return xinput != null && xinput.wants(client, window, maskBit);
    }

    /**
     * Delivers a pointer or key event, propagating up the window tree when nobody on the starting
     * window is interested. Propagation stops at the first window that had an interested client, at
     * the root, or when a window blocks the mask with {@code doNotPropagateMask}.
     *
     * @param start   the deepest window the pointer is in
     * @param maskBit the event mask that makes a client interested
     * @param screenX pointer x in screen coordinates
     * @param screenY pointer y in screen coordinates
     */
    public int deliverPropagating(XWindow start, int maskBit, int screenX, int screenY,
                              EventFactory factory) {
        XWindow window = start;
        XWindow child = null;
        int delivered = 0;
        var xinput = server.xinput();
        while (window != null) {
            // An XInput2 selection stops the walk exactly as a core one does: a client that asked
            // for this event through XI2 on this window is who the event belongs to, and a window
            // above it must not be told as well.
            if (window.anyClientSelected(maskBit)
                    || (xinput != null && xinput.anyoneWants(window, maskBit))) {
                XWindow eventWindow = window;
                XWindow eventChild = child;
                int x = screenX - eventWindow.absoluteX();
                int y = screenY - eventWindow.absoluteY();
                delivered += deliverToWindow(eventWindow, maskBit,
                        (client, w, c, cx, cy) -> factory.create(client, w, eventChild, x, y));
                break;
            }
            if ((maskBit & EventMasks.PROPAGATE) == 0) {
                break;
            }
            if ((window.doNotPropagateMask() & maskBit) != 0) {
                break;
            }
            child = window;
            window = window.parent();
        }
        return delivered;
    }

    /**
     * Reports the input focus moving from one window to another: FocusOut to the window losing it
     * and FocusIn to the window gaining it, to whichever clients asked for focus events on them.
     *
     * <p>This is the only way a client is told it has the keyboard. A toolkit moves the focus to a
     * dialog and waits to be told the dialog got it; a window that is never told stays drawn but
     * inert, with every keystroke going somewhere else and no way to tell. Either end may be null -
     * the focus is nowhere - or a focus that follows the pointer, which is a state of its own and not
     * a window, and is reported against the windows the pointer is inside with a detail that says so.
     */
    public void deliverFocusChange(XWindow previous, XWindow current) {
        deliverFocusChange(previous, previous != null && previous.isRoot(), current,
                current != null && current.isRoot(), NOTIFY_MODE_NORMAL, false);
    }

    /**
     * The same, with the mode a grab gives the change and a way to ask for a pair a reference server
     * sends even when the focus did not move.
     *
     * @param mode {@code NotifyNormal}, or what a grab did to the change - see the mode constants
     * @param force true to report a change between two windows that are the same one, which is what a
     *     keyboard grab taken on the window that already has the focus amounts to. A reference server
     *     sends the pair there, and a client that grabbed to be told it has the keyboard is waiting
     *     for it: measured with {@code tools/FocusProbe}, {@code grabkey A} with the focus already on
     *     A reports {@code FocusOut A / FocusIn A}, both Nonlinear
     */
    public void deliverFocusChange(XWindow previous, XWindow current, byte mode, boolean force) {
        deliverFocusChange(previous, previous != null && previous.isRoot(), current,
                current != null && current.isRoot(), mode, force);
    }

    /**
     * The whole rule, with the two states that follow the pointer named as such.
     *
     * <p><b>The root window is not PointerRoot, and a reference server was measured treating them
     * differently</b> - {@code tools/FocusProbe} with {@code "focus A" "warp 50 50" "focus pointer"}
     * and the same with {@code "focus root"} produce completely different events, and
     * {@code GetInputFocus} answers {@code PointerRoot} for one and the root's own id for the other.
     * PointerRoot means "the focus is the windows the pointer is inside"; the root window is a window
     * like any other, and one that nothing can hold, so the two also differ in what they tell the
     * pointer's windows - see {@link #pointerLeft} and {@link #pointerStillIn}, whose asymmetry is
     * measured rather than derived.
     *
     * <p>Every event this sends was measured against Xvfb with {@code FocusProbe}, one sequence at a
     * time. The sequences are in {@code XkbTest}'s sibling {@code FocusProbeTest}, which pins them.
     */
    public void deliverFocusChange(XWindow previous, boolean previousFollowsPointer,
                                   XWindow current, boolean currentFollowsPointer,
                                   byte mode, boolean force) {
        boolean sameWindow = previous == current && previousFollowsPointer == currentFollowsPointer;
        if (sameWindow) {
            if (!force || previous == null || previous.isRoot()) {
                return;
            }
        }
        XWindow screenWindow = previous != null ? previous : current;
        XWindow underPointer = screenWindow == null ? null
                : server.inputFor(screenWindow.screen()).pointerWindow();

        // The windows the pointer is inside, innermost first. A change that moves the focus into or
        // out of the focus the pointer implies tells them so, with a detail of its own that says this
        // is about the pointer and not about another window - the only way a client can tell "the
        // focus moved into my subtree" from "the keyboard came to where the mouse is".
        java.util.List<XWindow> chain = pointerChain(underPointer);
        for (XWindow w : chain) {
            if (pointerLeft(previous, previousFollowsPointer, w)
                    && (sameWindow || !pointerStillIn(current, currentFollowsPointer, underPointer, w))) {
                deliverFocusWindow(w, false, NOTIFY_POINTER, mode);
            }
        }

        if (sameWindow) {
            // The two ends are one window, so there is no chain to walk: the reference sends the
            // pair against that window and nothing else - its own ancestors are not told, which the
            // ordinary path would do because there is no common ancestor above it to stop at.
            deliverFocusWindow(previous, false, NOTIFY_NONLINEAR, mode);
            deliverFocusWindow(previous, true, NOTIFY_NONLINEAR, mode);
        } else {
            // The window's own side of the change. The root window does not *lose* the focus - it
            // cannot look like it had it - so a focus leaving it reports nothing of its own, while a
            // focus arriving on it is reported like any other window's (measured: an outgoing focus of
            // C against an incoming root window reports {@code FocusOut C detail=Ancestor}).
            XWindow arriving = currentFollowsPointer ? null : current;
            // The root window does not *lose* the focus - it cannot look like it had it - so a focus
            // leaving it reports nothing of its own, while a focus arriving on it is reported like any
            // other window's. It is still the other end of the lattice for the detail, though: a
            // focus going from the root window to A reports {@code FocusIn A detail=Ancestor}, which
            // is measured, so the root has to be named to the detail and not to the event.
            XWindow leavingEnd = previousFollowsPointer ? null : previous;
            XWindow arrivingEnd = arriving;
            XWindow leaving = leavingEnd != null && !leavingEnd.isRoot() ? leavingEnd : null;
            XWindow common = commonAncestor(leaving != null ? leaving : leavingEnd, arrivingEnd);
            if (leaving != null) {
                // The window losing the focus, then its ancestors up to the one the two have in
                // common: a toolkit wants to know the focus left its whole subtree, not only its
                // leaf. Innermost first, so a menu hears about the leaf before the window that holds
                // it.
                deliverFocusWindow(leaving, false, focusDetail(leaving, arrivingEnd), mode);
                for (XWindow w = leaving.parent(); w != null && w != common; w = w.parent()) {
                    deliverFocusWindow(w, false, virtualDetail(w, arrivingEnd), mode);
                }
            }
            if (arriving != null) {
                // Outermost first on the way in, which is the other way round from the way out: a
                // toolkit tracks focus by walking down into the window that gained it, and can only do
                // that if the window holding the focus is named before the window inside it.
                java.util.List<XWindow> in = new java.util.ArrayList<>();
                for (XWindow w = arriving.parent(); w != null && w != common; w = w.parent()) {
                    in.add(w);
                }
                for (int i = in.size() - 1; i >= 0; i--) {
                    deliverFocusWindow(in.get(i), true, virtualDetail(in.get(i), leavingEnd), mode);
                }
                deliverFocusWindow(arriving, true, focusDetail(arriving, leavingEnd), mode);
            }
        }

        // And the other half of the pointer's pair, outermost first like every other way in.
        for (int i = chain.size() - 1; i >= 0; i--) {
            XWindow w = chain.get(i);
            if (!pointerArrived(current, currentFollowsPointer, w)) {
                continue;
            }
            // **A window that is leaving the focus is not told the pointer's half of the change** -
            // except under PointerRoot, where it is: measured, {@code "focus A" "warp 50 50" "focus
            // pointer"} reports {@code FocusIn A}, and the window the pointer is in is told the
            // keyboard never left it. Everywhere else the two ends are the windows the ordinary pair
            // above already named, and a window on its way out must not be handed a {@code FocusIn}:
            // measured with {@code tools/FocusRevertProbe} and with {@code FocusProbe}'s
            // {@code "warp 50 50" "focus B" "unmap B"}, where the pointer is *in* the window that goes
            // away and a reference reports {@code FocusOut B detail=Ancestor} and
            // {@code FocusIn A detail=Inferior} and nothing else - this server added
            // {@code FocusIn B detail=Pointer} to the window that was leaving, and GDK took that for
            // where the focus lives: the window underneath was left looking unfocused, and a toolkit
            // hides the popup of an owner it believes unfocused (the context menu that flashed and
            // vanished after the 片段 page was closed, reported 2026-09-29).
            if (w == current || (w == previous && !currentFollowsPointer)) {
                continue;
            }
            // And a window inside the one that is leaving is not told either: that half of the change
            // belongs to the window holding it, which the ordinary pair above has already named.
            if (!sameWindow && !currentFollowsPointer
                    && pointerLeft(previous, previousFollowsPointer, w)) {
                continue;
            }
            deliverFocusWindow(w, true, NOTIFY_POINTER, mode);
        }
    }

    /** The windows the pointer is inside, innermost first, or an empty list when it is on the root. */
    private java.util.List<XWindow> pointerChain(XWindow underPointer) {
        java.util.List<XWindow> chain = new java.util.ArrayList<>();
        for (XWindow w = underPointer; w != null; w = w.parent()) {
            chain.add(w);
        }
        return chain;
    }

    /**
     * Whether a window of the pointer's chain is <em>giving up</em> the focus the pointer implies.
     *
     * <p>PointerRoot makes the whole chain the focus, and so does the root window - everything is
     * inside it - which is why a change away from either reports the whole chain. For an ordinary
     * window it is the windows really inside it, and not the window itself: the focus moving from a
     * window to its child does not tell the parent's subtree that the focus left.
     */
    private boolean pointerLeft(XWindow focus, boolean followsPointer, XWindow window) {
        if (focus == null) {
            return false;
        }
        if (followsPointer || focus.isRoot()) {
            return true;
        }
        // Strictly inside: XWindow.isAncestorOf counts the window itself, and the focus moving from
        // a window to its child does not tell that window's own subtree that the focus left.
        return focus != window && focus.isAncestorOf(window);
    }

    /**
     * Whether the incoming focus takes a window of the pointer's chain in, which is what suppresses
     * the FocusOut above: a window that is inside both ends of the change has not moved.
     *
     * <p><b>The one asymmetry in this rule, and it is measured.</b> Entries whose incoming focus is
     * <em>PointerRoot</em> subtract nothing - {@code "focus A" "warp 50 50" "focus pointer"} reports
     * the FocusOut for the window the pointer is in, and the same sequence with {@code "focus root"}
     * does not. The window itself counts as inside the incoming focus unless it is the pointer's own
     * window, which is what the second half of that comparison is: measured with
     * {@code "focus root" "focus A"} and with {@code "focus A" "warp 150 150" "focus C"}.
     */
    private boolean pointerStillIn(XWindow focus, boolean followsPointer, XWindow underPointer,
                                   XWindow window) {
        if (focus == null || followsPointer) {
            return false;
        }
        if (focus.isRoot()) {
            return true;
        }
        // True for the windows inside the new focus, and for the new focus itself unless it is the
        // pointer's own window - the window that is only now becoming the focus has not "stayed".
        return focus.isAncestorOf(window) && !(window == focus && focus == underPointer);
    }

    /** Whether the incoming focus reports a window of the pointer's chain as having it. */
    private boolean pointerArrived(XWindow focus, boolean followsPointer, XWindow window) {
        if (focus == null) {
            return false;
        }
        // The window itself included, unlike {@link #pointerLeft}: the window the focus arrives on
        // is inside itself, and the caller takes it out again where the ordinary pair has already
        // named it.
        return followsPointer || focus.isRoot() || (focus != null && focus.isAncestorOf(window));
    }

    /**
     * The detail for a window on the chain between the two focus windows: a plain virtual ancestor
     * when the window on the other end is one of its descendants, and a nonlinear one otherwise -
     * measured against a reference server, which relates the two ends rather than only the window.
     */
    private byte virtualDetail(XWindow window, XWindow other) {
        return other != null && other.isAncestorOf(window)
                ? NOTIFY_VIRTUAL
                : NOTIFY_NONLINEAR_VIRTUAL;
    }

    /**
     * The detail of a focus change as the window it is reported against sees it: what the window on
     * the other side of the change is to this one, which is the same lattice a crossing uses.
     *
     * <p>A change to or from None has nothing to relate to, and reports Nonlinear - measured rather
     * than assumed: a reference server sends exactly that.
     */
    private byte focusDetail(XWindow window, XWindow other) {
        if (window == other) {
            // The focus moved between the pointer and this very window, so there is no other window
            // to relate it to. The lattice has no answer for that, and a reference server reports
            // Nonlinear.
            return NOTIFY_NONLINEAR;
        }
        return crossingDetail(window, other);
    }

    /**
     * Sends one FocusIn or FocusOut to the clients interested in the window it names.
     *
     * <p>Both are selected by {@code FocusChangeMask} - there is one bit for the pair, not one each -
     * and neither propagates here, because {@code FOCUS_CHANGE} is not in {@link EventMasks#PROPAGATE}.
     *
     * @param mode what a grab did to the change, or {@code NotifyNormal} - see the mode constants
     */
    private void deliverFocusWindow(XWindow window, boolean gained, byte detail, byte mode) {
        deliverToWindow(window, EventMasks.FOCUS_CHANGE, (client, w, child, x, y) -> {
            if (gained) {
                return FocusInEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(client.sequenceNumber())
                        .event(w.id())
                        .detail(detail)
                        .mode(mode)
                        .build();
            }
            return FocusOutEvent.builder()
                    .firstEventOffset((byte) 0)
                    .sentEvent(false)
                    .sequenceNumber(client.sequenceNumber())
                    .event(w.id())
                    .detail(detail)
                    .mode(mode)
                    .build();
        });
    }

    /**
     * Sends an event both to the window itself (for clients selecting {@code ownMaskBit}) and to
     * its parent (for clients selecting {@code parentMaskBit}), which is how structure changes are
     * reported.
     */
    public void sendStructureNotify(XWindow window, int ownMaskBit, int parentMaskBit,
                                    EventFactory factory) {
        if (ownMaskBit != 0) {
            deliverToWindow(window, ownMaskBit, factory);
        }
        XWindow parent = window.parent();
        if (parentMaskBit != 0 && parent != null) {
            deliverToWindow(parent, parentMaskBit, factory);
        }
    }

    // ------------------------------------------------------------------
    // Pointer crossing
    // ------------------------------------------------------------------

    /**
     * Generates the EnterNotify and LeaveNotify events for a pointer move from {@code previous} to
     * {@code current}.
     *
     * <p>The windows notified are those on the path up from the old window and down to the new one,
     * stopping at their common ancestor. The {@code detail} of each event names the relationship of
     * the *other* window to the event window, which is what toolkits rely on: moving from a window
     * into its child reports {@code NotifyInferior} on the parent and {@code NotifyAncestor} on the
     * child.
     */
    public void deliverCrossing(XWindow previous, XWindow current, int screenX, int screenY,
                                int rootX, int rootY) {
        deliverCrossing(previous, current, screenX, screenY, rootX, rootY, null);
    }

    /**
     * Crossing events for a move from {@code previous} to {@code current}.
     *
     * @param inside when non-null, only this window and its descendants are told. While a pointer
     *     grab is active, only that subtree is where the pointer "is" as far as any client is
     *     concerned: a menu that grabs the pointer must not have the window underneath it light up,
     *     and - the reason this matters - the window the pointer really left still has to hear about
     *     it, or a toolkit that tracks the pointer by crossing events never learns which of its own
     *     windows the pointer moved to.
     */
    public void deliverCrossing(XWindow previous, XWindow current, int screenX, int screenY,
                                int rootX, int rootY, XWindow inside) {
        deliverCrossing(previous, current, screenX, screenY, rootX, rootY, inside, null);
    }

    /**
     * @param detailGrab the window a grab is held on, whose crossings with the outside are reported
     *     as moves inside the grabbing client's own tree; null when no such grab is held
     */
    public void deliverCrossing(XWindow previous, XWindow current, int screenX, int screenY,
                                int rootX, int rootY, XWindow inside, XWindow detailGrab) {
        if (previous == current) {
            return;
        }
        XWindow common = commonAncestor(previous, current);
        int time = time();
        // Crossing is reported against the window the pointer is on now, or the one it left when
        // it left everything: that is whose screen's pointer state the event carries.
        XWindow crossed = current != null ? current : previous;
        int state = crossed == null ? server.input().pointerState()
                : server.inputFor(crossed.screen()).pointerState();

        if (previous != null) {
            // The window left first, then the windows holding it, innermost outwards: a toolkit walks
            // up out of the subtree it was in, and has to hear the leaf before the window holding it.
            deliverCrossingWindow(previous, false, crossingDetail(previous, current, detailGrab), time,
                    screenX, screenY, rootX, rootY, state, inside, NOTIFY_MODE_NORMAL);
            if (common != previous) {
                for (XWindow w = previous.parent(); w != null && w != common; w = w.parent()) {
                    deliverCrossingWindow(w, false, virtualDetail(w, current), time, screenX, screenY,
                            rootX, rootY, state, inside, NOTIFY_MODE_NORMAL);
                }
            }
        }
        if (current != null) {
            // And the way in is the other way round - outermost first, the window that gained the
            // pointer before the window inside it - which was measured and is not symmetrical with
            // the way out: the first version of this delivered both chains innermost first, and the
            // reference orders only the leaving one that way.
            if (common != current) {
                java.util.List<XWindow> chain = new java.util.ArrayList<>();
                for (XWindow w = current.parent(); w != null && w != common; w = w.parent()) {
                    chain.add(w);
                }
                for (int i = chain.size() - 1; i >= 0; i--) {
                    deliverCrossingWindow(chain.get(i), true, virtualDetail(chain.get(i), previous),
                            time, screenX, screenY, rootX, rootY, state, inside, NOTIFY_MODE_NORMAL);
                }
            }
            // The window itself last, after every window holding it: a toolkit walks down into the
            // window the pointer is in, and can only do that if the chain arrives in that order.
            deliverCrossingWindow(current, true, crossingDetail(current, previous, detailGrab), time,
                    screenX, screenY, rootX, rootY, state, inside, NOTIFY_MODE_NORMAL);
        }
    }

    /**
     * The crossing pair a grab's start or end implies: the pointer leaves the window it is in and
     * enters the grab window, with the mode that says a grab did it - or, at the end, the reverse.
     *
     * <p><b>This is the pair §7 withholds for grabs this server takes on a client's request</b>, and
     * that is measured: EasyShell's menus are torn down by it. A *passive* grab is the other case, and
     * a reference server sends the pair for it - measured with {@code tools/GrabProbe}, which is why
     * it is sent here and not there. The detail is the ordinary lattice: the window on the other side
     * of the change is what the event says, so a pointer leaving a child for the window holding it is
     * `Ancestor` in both directions.
     */
    public void deliverGrabCrossing(XWindow pointerWindow, XWindow grabWindow, byte mode) {
        if (pointerWindow == null || grabWindow == null || pointerWindow == grabWindow) {
            return;
        }
        int time = time();
        int state = server.inputFor(grabWindow.screen()).pointerState();
        boolean starting = mode == NOTIFY_MODE_GRAB;
        XWindow leaving = starting ? pointerWindow : grabWindow;
        XWindow arriving = starting ? grabWindow : pointerWindow;
        int x = server.inputFor(grabWindow.screen()).pointerX();
        int y = server.inputFor(grabWindow.screen()).pointerY();
        deliverCrossingWindow(leaving, false, crossingDetail(leaving, arriving), time, x, y, x, y,
                state, null, mode);
        deliverCrossingWindow(arriving, true, crossingDetail(arriving, leaving), time, x, y, x, y,
                state, null, mode);
    }

    private void deliverCrossingWindow(XWindow window, boolean enter, byte detail, int time,
                                       int screenX, int screenY, int rootX, int rootY, int state,
                                       XWindow inside, byte mode) {
        if (inside != null && inside != window && !inside.isAncestorOf(window)) {
            // A grab is held on another window: this one is outside the tree the pointer belongs to
            // for as long as the grab lasts, so it is told nothing.
            if (log.isDebugEnabled()) {
                log.debug("{}Notify for 0x{} withheld: the grab on 0x{} does not cover it",
                        enter ? "Enter" : "Leave", Integer.toHexString(window.id()),
                        Integer.toHexString(inside.id()));
            }
            return;
        }
        if (log.isDebugEnabled()) {
            // Which window a crossing is reported against, and whether a grab produced it, is what
            // tells a client where its pointer is: the events are indistinguishable in the log
            // otherwise, and this is the pair a menu's submenu lives or dies by.
            log.debug("{}Notify on 0x{} detail={}", enter ? "Enter" : "Leave",
                    Integer.toHexString(window.id()), detail);
        }
        int maskBit = enter ? EventMasks.ENTER_WINDOW : EventMasks.LEAVE_WINDOW;
        // The focus bit of a crossing is set when this window is **inside** the focus window -
        // the focus window itself, or a descendant of it. Measured against a reference server, which
        // sets it for a child of the focus and clears it for an unrelated window *and for the focus
        // window's own parent*: `focus A`, enter B (A's child) - set; `focus B`, enter A (B's
        // parent) - clear; `focus C`, enter A - clear. The relation is the other way round from the
        // reading the first version of this code took, and a toolkit that paints a window as active
        // from the bit is what notices.
        XWindow focus = server.inputFor(window.screen()).inputFocus();
        // A focus of null is None, and no window has it - the one case where the bit is clear for
        // every window. A focus of the root is PointerRoot, and the reference sets the bit for every
        // window it crosses there, leaves included; that is measured rather than derived, and it is
        // the case this short circuit exists for.
        boolean focused = focus != null && (focus.isRoot() || focus.isAncestorOf(window));
        deliverPropagating(window, maskBit, screenX, screenY, (client, w, child, x, y) -> {
            short sequence = client.sequenceNumber();
            int childId = child == null ? 0 : child.id();
            if (enter) {
                return EnterNotifyEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(sequence)
                        .detail(detail)
                        .time(time)
                        .root(window.root().id())
                        .event(w.id())
                        .child(childId)
                        .rootX((short) rootX)
                        .rootY((short) rootY)
                        .eventX((short) x)
                        .eventY((short) y)
                        .state((short) state)
                        .mode(mode)
                        .sameScreenFocus(sameScreenFocus(focused))
                        .build();
            }
            return LeaveNotifyEvent.builder()
                    .firstEventOffset((byte) 0)
                    .sentEvent(false)
                    .sequenceNumber(sequence)
                    .detail(detail)
                    .time(time)
                    .root(window.root().id())
                    .event(w.id())
                    .child(childId)
                    .rootX((short) rootX)
                    .rootY((short) rootY)
                    .eventX((short) x)
                    .eventY((short) y)
                    .state((short) state)
                    .mode(mode)
                    .sameScreenFocus(sameScreenFocus(focused))
                    .build();
        });
    }

    /**
     * The last byte of a crossing: **{@code same_screen} in bit 1 and the focus flag in bit 0**.
     *
     * <p>Which bit is which is worth writing down, because it is the opposite of what the field's
     * name suggests, and getting it backwards is invisible from this side of the socket: a client
     * decodes it with Xlib, which reads {@code same_screen} from bit 1. Measured, not reasoned -
     * `xev` on a reference server and on this one, with the focus on a window and then on nothing,
     * prints "same_screen YES, focus NO" for the byte a reference server sends there and
     * "same_screen NO, focus YES" for the one this server used to send. This server therefore
     * claimed the pointer was on another screen in **every** crossing it sent, and reported the
     * focus bit inverted whenever the crossing's window was not the focus - which a toolkit that
     * gates on {@code same_screen}, or paints a window active from the focus bit, reads as a
     * different event than the one that happened.
     *
     * @param focused whether the focus window is this window or inside it
     */
    private static byte sameScreenFocus(boolean focused) {
        return (byte) (0x02 | (focused ? 0x01 : 0));
    }

    /**
     * How the pointer moved between two windows, for the {@code detail} of a crossing.
     *
     * <p>While a grab that reports the client's own windows is held, a crossing between the grab
     * window and a window outside it is reported as a move inside the grabbing client's tree -
     * {@code NotifyAncestor} - rather than {@code NotifyNonlinear}, which is what the reference
     * server was measured sending for the same gesture.
     */
    private byte crossingDetail(XWindow window, XWindow other, XWindow inside) {
        if (inside != null && (other == inside || window == inside)) {
            return NOTIFY_ANCESTOR;
        }
        return crossingDetail(window, other);
    }

    /** How the pointer moved between two windows, for the {@code detail} of a crossing. */
    private byte crossingDetail(XWindow window, XWindow other) {
        if (other == null) {
            return NOTIFY_NONLINEAR;
        }
        if (other.isAncestorOf(window)) {
            return NOTIFY_ANCESTOR;
        }
        if (window.isAncestorOf(other)) {
            return NOTIFY_INFERIOR;
        }
        return NOTIFY_NONLINEAR;
    }

    /** The deepest window that is an ancestor of both, or null when they are unrelated. */
    public static XWindow commonAncestor(XWindow a, XWindow b) {
        for (XWindow window = a; window != null; window = window.parent()) {
            if (b != null && window.isAncestorOf(b)) {
                return window;
            }
        }
        return null;
    }
}
