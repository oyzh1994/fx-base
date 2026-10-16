package cn.oyzh.fx.xserver.handler;

import com.github.moaxcp.x11.protocol.xproto.ChangeSaveSet;
import com.github.moaxcp.x11.protocol.xproto.ChangeWindowAttributes;
import com.github.moaxcp.x11.protocol.xproto.Circulate;
import com.github.moaxcp.x11.protocol.xproto.CirculateNotifyEvent;
import com.github.moaxcp.x11.protocol.xproto.CirculateWindow;
import com.github.moaxcp.x11.protocol.xproto.ConfigWindow;
import com.github.moaxcp.x11.protocol.xproto.ConfigureNotifyEvent;
import com.github.moaxcp.x11.protocol.xproto.ConfigureWindow;
import com.github.moaxcp.x11.protocol.xproto.CreateNotifyEvent;
import com.github.moaxcp.x11.protocol.xproto.CreateWindow;
import com.github.moaxcp.x11.protocol.xproto.Cw;
import com.github.moaxcp.x11.protocol.xproto.DestroyNotifyEvent;
import com.github.moaxcp.x11.protocol.xproto.DestroySubwindows;
import com.github.moaxcp.x11.protocol.xproto.DestroyWindow;
import com.github.moaxcp.x11.protocol.xproto.ExposeEvent;
import com.github.moaxcp.x11.protocol.xproto.GetGeometry;
import com.github.moaxcp.x11.protocol.xproto.GetGeometryReply;
import com.github.moaxcp.x11.protocol.xproto.GetWindowAttributes;
import com.github.moaxcp.x11.protocol.xproto.GetWindowAttributesReply;
import com.github.moaxcp.x11.protocol.xproto.MapNotifyEvent;
import com.github.moaxcp.x11.protocol.xproto.MapRequestEvent;
import com.github.moaxcp.x11.protocol.xproto.MapSubwindows;
import com.github.moaxcp.x11.protocol.xproto.MapWindow;
import com.github.moaxcp.x11.protocol.xproto.Place;
import com.github.moaxcp.x11.protocol.xproto.QueryBestSize;
import com.github.moaxcp.x11.protocol.xproto.QueryBestSizeReply;
import com.github.moaxcp.x11.protocol.xproto.QueryTree;
import com.github.moaxcp.x11.protocol.xproto.QueryTreeReply;
import com.github.moaxcp.x11.protocol.xproto.ReparentNotifyEvent;
import com.github.moaxcp.x11.protocol.xproto.ReparentWindow;
import com.github.moaxcp.x11.protocol.xproto.StackMode;
import com.github.moaxcp.x11.protocol.xproto.TranslateCoordinates;
import com.github.moaxcp.x11.protocol.xproto.TranslateCoordinatesReply;
import com.github.moaxcp.x11.protocol.xproto.UnmapNotifyEvent;
import com.github.moaxcp.x11.protocol.xproto.UnmapSubwindows;
import com.github.moaxcp.x11.protocol.xproto.UnmapWindow;
import com.github.moaxcp.x11.protocol.xproto.Visibility;
import com.github.moaxcp.x11.protocol.xproto.VisibilityNotifyEvent;
import com.github.moaxcp.x11.protocol.xproto.WindowClass;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.event.EventMasks;
import cn.oyzh.fx.xserver.event.RawEvent;
import cn.oyzh.fx.xserver.extension.XFixesExtension;
import cn.oyzh.fx.xserver.resource.ShapeState;
import cn.oyzh.fx.xserver.resource.XColormap;
import cn.oyzh.fx.xserver.resource.XCursor;
import cn.oyzh.fx.xserver.resource.XPixmap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Window requests: creating, mapping, stacking, resizing and destroying windows, plus the queries
 * that describe them.
 *
 * <p>Structure changes are reported to the window itself (StructureNotify) and to its parent
 * (SubstructureNotify), and the {@code event} field of those notifications is the window the
 * client selected on rather than the window that changed - which is why each notification is built
 * per delivery target rather than once.
 */
public final class WindowHandlers {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(WindowHandlers.class);

    /**
     * How many Exposes one change may put in a series before it is sent as its bounding rectangle.
     *
     * <p>A bounding shape arrives as a bit mask, and a mask that is not a plain rectangle - xeyes'
     * eyes, for one - leaves one rectangle per scanned row, none of them mergeable. One Expose each
     * is both useless to the client (a toolkit combines a series into one region anyway, which is
     * the bounding box) and a flood: a maximised xeyes is about two thousand rows, which overflows
     * the connection's own queue of a thousand messages and drops the client (2026-10-13).
     */
    private static final int MAX_EXPOSE_PIECES = 64;

    private final XServer server;

    public WindowHandlers(XServer server) {
        this.server = server;
    }

    // ------------------------------------------------------------------
    // Creation and destruction
    // ------------------------------------------------------------------

    public void create(ClientConnection client, CreateWindow r) {
        XWindow parent = window(client, r.getParent());
        int wid = r.getWid();
        if (wid == 0) {
            throw ProtocolException.badIdChoice(wid);
        }
        if (!client.ownsIdRange(wid)) {
            throw ProtocolException.badIdChoice(wid);
        }
        if (server.resources().contains(wid)) {
            throw ProtocolException.badIdChoice(wid);
        }

        int requestedClass = r.getClazz();
        int windowClass;
        if (requestedClass == WindowClass.COPY_FROM_PARENT.getValue()) {
            windowClass = parent.windowClass();
        } else if (requestedClass == WindowClass.INPUT_OUTPUT.getValue()) {
            windowClass = XWindow.INPUT_OUTPUT;
        } else if (requestedClass == WindowClass.INPUT_ONLY.getValue()) {
            windowClass = XWindow.INPUT_ONLY;
        } else {
            throw ProtocolException.badValue(requestedClass);
        }

        int depth = Byte.toUnsignedInt(r.getDepth());
        if (depth == 0) {
            depth = parent.depth();
        }
        // **A child need not share its parent's depth**, which this used to refuse as a match
        // failure - and that rule is what made the 32 bit ARGB visual useless here, because the ARGB
        // window every compositing toolkit creates is a child of the root: depth 32 under a depth 24
        // root. What has to exist is a *visual* of the depth asked for, which is checked below; a
        // reference server refuses "no visual of that depth on this screen", not "not your parent's
        // depth". (Xvfb cannot be asked about the difference: it advertises no depth 32 visual at
        // all, so the ARGB case does not arise there.)
        if (windowClass == XWindow.INPUT_ONLY && depth != 0 && depth != parent.depth()) {
            throw ProtocolException.badMatch();
        }

        VisualInfo visual;
        if (r.getVisual() == 0 || r.getVisual() == 0xFFFFFFFF) {
            visual = parent.visual();
        } else {
            visual = parent.screen().visualById(r.getVisual());
            if (visual == null) {
                throw ProtocolException.badMatch();
            }
        }
        if (visual.depth() != depth) {
            throw ProtocolException.badMatch();
        }

        int borderWidth = Short.toUnsignedInt(r.getBorderWidth());
        if (windowClass == XWindow.INPUT_ONLY && borderWidth != 0) {
            throw ProtocolException.badMatch();
        }

        XWindow window = new XWindow(wid, client, parent.screen(), parent, depth, windowClass,
                visual, r.getX(), r.getY(), Short.toUnsignedInt(r.getWidth()),
                Short.toUnsignedInt(r.getHeight()), borderWidth);
        // The colours the window starts with are the ones XWindow gives every window: the screen's
        // own default behind it, and a distinct border. A client that names its own in this request
        // overrides them below.

        applyAttributes(client, window, r.getValueMask(), r.getBackgroundPixmap(),
                r.getBackgroundPixel(), r.getBorderPixmap(), r.getBorderPixel(), r.getBitGravity(),
                r.getWinGravity(), r.getBackingStore(), r.getBackingPlanes(), r.getBackingPixel(),
                r.getOverrideRedirect() != 0, r.getSaveUnder() != 0, r.getEventMask(),
                r.getDoNotPropogateMask(), r.getColormap(), r.getCursor());

        server.resources().add(window);

        // CreateNotify goes to the parent's SubstructureNotify clients.
        server.events().deliverToWindow(parent, EventMasks.SUBSTRUCTURE_NOTIFY,
                (recipient, w, child, x, y) -> CreateNotifyEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(recipient.sequenceNumber())
                        .parent(w.id())
                        .window(window.id())
                        .x((short) window.x())
                        .y((short) window.y())
                        .width((short) window.width())
                        .height((short) window.height())
                        .borderWidth((short) window.borderWidth())
                        .overrideRedirect(window.isOverrideRedirect())
                        .build());
    }

    public void changeAttributes(ClientConnection client, ChangeWindowAttributes r) {
        XWindow window = window(client, r.getWindow());
        applyAttributes(client, window, r.getValueMask(), r.getBackgroundPixmap(),
                r.getBackgroundPixel(), r.getBorderPixmap(), r.getBorderPixel(), r.getBitGravity(),
                r.getWinGravity(), r.getBackingStore(), r.getBackingPlanes(), r.getBackingPixel(),
                r.getOverrideRedirect() != 0, r.getSaveUnder() != 0, r.getEventMask(),
                r.getDoNotPropogateMask(), r.getColormap(), r.getCursor());
        // Changing attributes does not repaint anything. A background change takes effect the next
        // time the window is cleared or exposed, and repainting here would paint over every child
        // window - which is precisely what erased xcalc's buttons the first time it ran.
    }

    /**
     * Applies a {@code CW} value mask shared by {@code CreateWindow} and
     * {@code ChangeWindowAttributes}. Only fields whose bit is set in the mask are meaningful, and
     * the generated decoder has already zeroed the rest.
     */
    private void applyAttributes(ClientConnection client, XWindow window, int valueMask,
                                 int backgroundPixmap, int backgroundPixel, int borderPixmap,
                                 int borderPixel, int bitGravity, int winGravity, int backingStore,
                                 int backingPlanes, int backingPixel, boolean overrideRedirect,
                                 boolean saveUnder, int eventMask, int doNotPropagateMask,
                                 int colormap, int cursor) {
        if (Cw.BACK_PIXMAP.isEnabled(valueMask)) {
            XResource pixmap = backgroundPixmap == 0 ? null
                    : server.resources().require(client, backgroundPixmap, XResource.CLASS_PIXMAP,
                            true);
            if (backgroundPixmap != 0 && ((XPixmap) pixmap).depth() != window.depth()) {
                throw ProtocolException.badMatch();
            }
            if (pixmap == null) {
                // Background None: the window shows what is behind it, which is the screen's own
                // default colour - the same thing a real server's root shows through.
                window.setBackgroundArgb(Screen.DEFAULT_BACKGROUND_ARGB);
            } else {
                window.setBackgroundPixmap(pixmap);
            }
        }
        if (Cw.BACK_PIXEL.isEnabled(valueMask)) {
            window.setBackgroundPixel(backgroundPixel);
        }
        if (Cw.BORDER_PIXMAP.isEnabled(valueMask)) {
            if (!window.isInputOnly()) {
                XResource pixmap = borderPixmap == 0 ? null
                        : server.resources().require(client, borderPixmap, XResource.CLASS_PIXMAP,
                        true);
                if (borderPixmap != 0 && ((XPixmap) pixmap).depth() != window.depth()) {
                    throw ProtocolException.badMatch();
                }
                if (pixmap == null) {
                    // Border None: nothing was named to draw it with, so it falls back to the colour
                    // a border starts with.
                    window.setBorderArgb(Screen.DEFAULT_BORDER_ARGB);
                } else {
                    window.setBorderPixmap(pixmap);
                }
            }
        }
        if (Cw.BORDER_PIXEL.isEnabled(valueMask) && !window.isInputOnly()) {
            window.setBorderPixel(borderPixel);
        }
        if (Cw.BIT_GRAVITY.isEnabled(valueMask)) {
            window.setBitGravity(bitGravity);
        }
        if (Cw.WIN_GRAVITY.isEnabled(valueMask)) {
            window.setWinGravity(winGravity);
        }
        if (Cw.BACKING_STORE.isEnabled(valueMask)) {
            // Recorded and reported back, and it decides nothing: every window keeps its pixels
            // whatever this says (B5). WhenMapped and Always are both stored as "not 0", because
            // nothing here distinguishes them, and it is the *going away* half of Always - contents
            // kept while the window is unmapped - that is not here.
            window.setBackingStore(backingStore == 0 ? 0 : 1);
        }
        if (Cw.BACKING_PLANES.isEnabled(valueMask) && !window.isInputOnly()) {
            window.setBackingPlanes(backingPlanes);
        }
        if (Cw.BACKING_PIXEL.isEnabled(valueMask) && !window.isInputOnly()) {
            window.setBackingPixel(backingPixel);
        }
        if (Cw.OVERRIDE_REDIRECT.isEnabled(valueMask)) {
            window.setOverrideRedirect(overrideRedirect);
        }
        if (Cw.SAVE_UNDER.isEnabled(valueMask)) {
            window.setSaveUnder(saveUnder);
        }
        if (Cw.EVENT_MASK.isEnabled(valueMask)) {
            if ((eventMask & ~EventMasks.ALL) != 0) {
                throw ProtocolException.badValue(eventMask);
            }
            if (log.isDebugEnabled()) {
                // Which events a client asked for is the first thing to check when one it needs never
                // arrives, and the request itself says nothing about it.
                log.debug("{} selected 0x{} on 0x{}", client.peer(),
                        Integer.toHexString(eventMask), Integer.toHexString(window.id()));
            }
            if (window.owner() == client) {
                window.setEventMask(eventMask);
            } else {
                window.setOtherClientMask(client, eventMask);
            }
        }
        if (Cw.DONT_PROPAGATE.isEnabled(valueMask)) {
            if ((doNotPropagateMask & ~EventMasks.ALL) != 0) {
                throw ProtocolException.badValue(doNotPropagateMask);
            }
            window.setDoNotPropagateMask(doNotPropagateMask);
        }
        if (Cw.COLORMAP.isEnabled(valueMask) && !window.isInputOnly()) {
            XColormap map = colormap == 0 ? null
                    : (XColormap) server.resources().require(client, colormap,
                    XResource.CLASS_COLORMAP, true);
            if (map != null && map.visual() != window.visual()) {
                throw ProtocolException.badMatch();
            }
            window.setColormap(map);
        }
        if (Cw.CURSOR.isEnabled(valueMask)) {
            XCursor value = cursor == 0 ? null
                    : (XCursor) server.resources().require(client, cursor, XResource.CLASS_CURSOR,
                    true);
            window.setCursor(value);
        }
    }

    public void getAttributes(ClientConnection client, GetWindowAttributes r) {
        XWindow window = window(client, r.getWindow());
        client.sendReply(GetWindowAttributesReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .backingStore((byte) window.backingStore())
                .visual(window.visual() == null ? window.screen().rootVisual().id()
                        : window.visual().id())
                .clazz((short) window.windowClass())
                .bitGravity((byte) window.bitGravity())
                .winGravity((byte) window.winGravity())
                .backingPlanes(window.backingPlanes())
                .backingPixel(window.backingPixel())
                .saveUnder(window.isSaveUnder())
                .mapIsInstalled(window.isMapped())
                .mapState((byte) window.mapState())
                .overrideRedirect(window.isOverrideRedirect())
                .colormap(window.colormap() == null ? server.rootColormap().id()
                        : window.colormap().id())
                .allEventMasks(window.allEventMasks())
                .yourEventMask(window.eventMaskFor(client))
                .doNotPropagateMask((short) window.doNotPropagateMask())
                .build());
    }

    public void destroy(ClientConnection client, DestroyWindow r) {
        XWindow window = window(client, r.getWindow());
        destroyWindowTree(client, window);
    }

    public void destroySubwindows(ClientConnection client, DestroySubwindows r) {
        XWindow window = window(client, r.getWindow());
        for (XWindow child : new ArrayList<>(window.children())) {
            destroyWindowTree(client, child);
        }
    }

    /**
     * Destroys {@code window} and everything below it, generating the destroy notifications in
     * bottom-up order so a client never sees a parent destroyed before its children.
     */
    public void destroyWindowTree(ClientConnection client, XWindow window) {
        if (window.isDestroyed()) {
            return;
        }
        // A docked tray icon (C9) goes with its window: the bridge keeps a tray window for it, and a
        // tray window whose client's window is gone is a window nobody will ever draw in again.
        if (server.tray() != null) {
            server.tray().forgetWindow(window);
        }
        // Unmapped before the children are destroyed rather than after, so that the whole subtree
        // stops being viewable at once: the focus of a window inside it then reverts past all of it,
        // the way a reference server reverts it, instead of landing on a window that this same
        // request is destroying. Measured with FocusProbe (E. with "focus B" then "kill A"): this
        // used to tell the dying parent it had gained the focus (FocusIn A, Inferior) and then that
        // it had lost it again, where the reference tells it FocusOut A, Virtual, once.
        if (window.isMapped()) {
            unmapWindow(window, false);
        }
        for (XWindow child : new ArrayList<>(window.children())) {
            destroyWindowTree(client, child);
        }
        XWindow parent = window.parent();
        if (parent != null) {
            parent.removeChild(window);
        }
        // And a DAMAGE object on it goes with it: a client that watched a window hears no more
        // about it, and the drawing path's index has to stop pointing at a window that is gone.
        cn.oyzh.fx.xserver.extension.DamageExtension.drawableGone(server, window);
        // A window that is gone cannot be the window a grab is held on, nor the window that has the
        // input focus: X11 releases the one and reverts the other with the window, and a client that
        // grabs for a dialog or focuses one and then destroys it instead of putting things back
        // depends on both.
        server.inputFor(window.screen()).forgetGrabWindow(window);
        server.inputFor(window.screen()).forgetFocusWindow(window);
        server.events().deliverToWindow(window, EventMasks.STRUCTURE_NOTIFY,
                (recipient, w, child, x, y) -> DestroyNotifyEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(recipient.sequenceNumber())
                        .event(w.id())
                        .window(window.id())
                        .build());
        if (parent != null) {
            server.events().deliverToWindow(parent, EventMasks.SUBSTRUCTURE_NOTIFY,
                    (recipient, w, child, x, y) -> DestroyNotifyEvent.builder()
                            .firstEventOffset((byte) 0)
                            .sentEvent(false)
                            .sequenceNumber(recipient.sequenceNumber())
                            .event(w.id())
                            .window(window.id())
                            .build());
        }
        // A selection is owned by a window, so it dies with it - and the clients that subscribed to
        // it hear about it, with the time its owner took it rather than the current time: nothing
        // took the selection away, it lost the window that held it.
        server.releaseSelectionsOwnedBy(window.id(), XFixesExtension.SELECTION_WINDOW_DESTROY);
        XFixesExtension xfixes = XFixesExtension.of(server);
        if (xfixes != null) {
            // The subscription itself lived on this window, so it goes too.
            xfixes.windowDestroyed(window);
        }
        server.resources().free(window.id());
        server.markScreenDirty(window);
    }

    public void changeSaveSet(ClientConnection client, ChangeSaveSet r) {
        int mode = r.getMode();
        if (mode != 0 && mode != 1) {
            throw ProtocolException.badValue(mode);
        }
        window(client, r.getWindow());
    }

    public void reparent(ClientConnection client, ReparentWindow r) {
        XWindow window = window(client, r.getWindow());
        XWindow parent = window(client, r.getParent());
        if (window == parent || window.isAncestorOf(parent)) {
            throw ProtocolException.badMatch();
        }
        if (window.isInputOnly() != parent.isInputOnly()) {
            throw ProtocolException.badMatch();
        }
        reparentTo(window, parent, r.getX(), r.getY());
    }

    /**
     * The tree surgery and the notifications a reparent owes, without the protocol checks: the same
     * work {@code ReparentWindow} does, for a caller inside the server. The tray bridge reparents a
     * docked icon window into a tray window of its own (C9), and an XEmbed client is waiting for
     * this to happen - it is how the client learns that its icon is now somebody else's window.
     */
    public void reparentTo(XWindow window, XWindow parent, int atX, int atY) {
        XWindow oldParent = window.parent();
        boolean wasMapped = window.isMapped();
        if (wasMapped) {
            unmapWindow(window, true);
        }
        if (oldParent != null) {
            oldParent.removeChild(window);
        }
        parent.addChild(window, -1);
        window.setPosition(atX, atY);
        if (parent != null) {
            server.events().deliverToWindow(parent, EventMasks.STRUCTURE_NOTIFY,
                    (recipient, w, child, x, y) -> reparentNotify(recipient, w, window, parent));
            // The window that was moved is told as well, which is the half a tray's dock depends on:
            // an XEmbed client hands its icon window to the tray, and what it waits for is this
            // event on its *own* window - Java's tray peer blocks five seconds for it and then
            // reports that the icon could not be displayed (measured while C9 was written).
            server.events().deliverToWindow(window, EventMasks.STRUCTURE_NOTIFY,
                    (recipient, w, child, x, y) -> reparentNotify(recipient, w, window, parent));
        }
        if (wasMapped || window.isMapped()) {
            mapWindow(window);
        }
        server.markScreenDirty(window);
    }

    /** The event a reparent owes, reported against whatever window hears about it. */
    private static ReparentNotifyEvent reparentNotify(ClientConnection recipient, XWindow reported,
                                                     XWindow window, XWindow parent) {
        return ReparentNotifyEvent.builder()
                .firstEventOffset((byte) 0)
                .sentEvent(false)
                .sequenceNumber(recipient.sequenceNumber())
                .event(reported.id())
                .window(window.id())
                .parent(parent.id())
                .x((short) window.x())
                .y((short) window.y())
                .overrideRedirect(window.isOverrideRedirect())
                .build();
    }

    // ------------------------------------------------------------------
    // Mapping
    // ------------------------------------------------------------------

    public void map(ClientConnection client, MapWindow r) {
        XWindow window = window(client, r.getWindow());
        if (window.isInputOnly() && !window.parent().isInputOnly()) {
            // InputOnly windows may only be children of InputOnly windows in the protocol sense;
            // real servers allow this, so it is not an error here.
        }
        if (window.isMapped()) {
            return;
        }
        // Mapping a child of an unmapped parent is not an error: the window is mapped but not
        // viewable until its parent is. Toolkits map their children before the toplevel, and
        // window managers map frames before their clients, so rejecting this broke twm.
        mapWindow(window);
    }

    public void mapSubwindows(ClientConnection client, MapSubwindows r) {
        XWindow window = window(client, r.getWindow());
        for (XWindow child : new ArrayList<>(window.children())) {
            if (!child.isMapped()) {
                mapWindow(child);
            }
        }
    }

    /**
     * Maps a window and generates the notifications and exposes that go with becoming viewable.
     * Also used internally when a parent is remapped.
     */
    public void mapWindow(XWindow window) {
        // Nothing of it showed before it was mapped, so everything it is about to cover is
        // repainted from the windows underneath and the window itself is drawn and exposed.
        mapWindow(window, visibleRegionOf(window), true);
    }

    /**
     * @param before the area the window showed before this call. For a window being mapped that is
     *     nothing, and it is nothing for its children too however long they have been mapped: what
     *     a window that has just become viewable shows is new at every depth below it.
     * @param newlyVisible whether this map is what puts the window on screen. It has to be carried
     *     down the subtree rather than re-read, because a descendant's own region looks unchanged
     *     the moment the window above it is mapped - and a window that gained nothing is sent no
     *     Expose. A toolkit maps its children before the window that holds them, so that is a
     *     toolkit that never draws them: xclock, xeyes and xcalc come up black.
     */
    private void mapWindow(XWindow window, List<Rectangle> before, boolean newlyVisible) {
        window.setMapped(true);
        boolean viewable = window.isViewable();

        server.events().deliverToWindow(window, EventMasks.STRUCTURE_NOTIFY,
                (recipient, w, child, x, y) -> MapNotifyEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(recipient.sequenceNumber())
                        .event(w.id())
                        .window(window.id())
                        .overrideRedirect(window.isOverrideRedirect())
                        .build());
        XWindow parent = window.parent();
        if (parent != null) {
            server.events().deliverToWindow(parent, EventMasks.SUBSTRUCTURE_NOTIFY,
                    (recipient, w, child, x, y) -> MapNotifyEvent.builder()
                            .firstEventOffset((byte) 0)
                            .sentEvent(false)
                            .sequenceNumber(recipient.sequenceNumber())
                            .event(w.id())
                            .window(window.id())
                            .overrideRedirect(window.isOverrideRedirect())
                            .build());
            // A client that redirected its children's mapping gets a request instead of a map.
            if (parent.anyClientSelected(EventMasks.SUBSTRUCTURE_REDIRECT)) {
                server.events().deliverToWindow(parent, EventMasks.SUBSTRUCTURE_REDIRECT,
                        (recipient, w, child, x, y) -> MapRequestEvent.builder()
                                .firstEventOffset((byte) 0)
                                .sentEvent(false)
                                .sequenceNumber(recipient.sequenceNumber())
                                .parent(w.id())
                                .window(window.id())
                                .build());
            }
        }

        if (viewable) {
            // Painting and exposing is what visibilityChanged does, region by region: the parts of
            // the window a window on top already owns are left to it.
            visibilityChanged(window, before, visibleRegionOf(window));
            for (XWindow child : window.children()) {
                if (child.isMapped()) {
                    mapWindow(child, newlyVisible ? List.of() : visibleRegionOf(child), newlyVisible);
                }
            }
        }
        server.markScreenDirty(window);
        // A window that appears under a stationary pointer is entered by it, which is what a
        // reference server reports at the map. Leaving it to the move that follows makes the client's
        // first motion a crossing it has no reason to expect - and until it moves, a popup that
        // appeared under the pointer is a window its client does not know the pointer is in: nothing
        // in it highlights, and a click in it is read as a click outside.
        server.inputFor(window.screen()).refreshPointerWindow();
    }

    public void unmap(ClientConnection client, UnmapWindow r) {
        XWindow window = window(client, r.getWindow());
        if (!window.isMapped()) {
            return;
        }
        unmapWindow(window, true);
        server.markScreenDirty(window);
    }

    public void unmapSubwindows(ClientConnection client, UnmapSubwindows r) {
        XWindow window = window(client, r.getWindow());
        for (XWindow child : new ArrayList<>(window.children())) {
            if (child.isMapped()) {
                unmapWindow(child, true);
            }
        }
        server.markScreenDirty(window);
    }

    /** Unmaps a window, optionally reporting it to the clients that selected for it. */
    public void unmapWindow(XWindow window, boolean notify) {
        // The area it covered goes back to the windows below, which have to draw it again: their
        // pixels there are the ones this window painted over. This happens even when the unmap is
        // not reported, because a destroyed window's area has to be restored just the same - and it
        // is what the window *covers* rather than what it owns, so a frame whose content window
        // fills it gives back the dialog's picture rather than the nothing its own region is.
        List<Rectangle> before = coveredRegionOf(window);
        window.setMapped(false);
        // A window that is no longer on screen cannot be holding the pointer's implicit grab: pinning
        // events to it would leave every client without input until another button press.
        server.inputFor(window.screen()).forgetGrabWindow(window);
        // Nor can it hold the input focus - a window nobody can see must not go on receiving keys -
        // and X11 moves the focus out of a window that stops being viewable, or out of one whose
        // ancestor does. A reference server was measured doing it, and it is what a toolkit that
        // unmaps a dialog instead of putting the focus back itself is relying on.
        server.inputFor(window.screen()).forgetFocusWindow(window);
        // A window that goes away under the pointer takes the pointer out of itself and puts it in
        // whatever was underneath, so the client has to be told rather than left reporting events
        // against a window that no longer exists.
        server.inputFor(window.screen()).refreshPointerWindow();
        repaintUnder(before, window);
        if (!notify) {
            return;
        }
        server.events().deliverToWindow(window, EventMasks.STRUCTURE_NOTIFY,
                (recipient, w, child, x, y) -> UnmapNotifyEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(recipient.sequenceNumber())
                        .event(w.id())
                        .window(window.id())
                        .fromConfigure(false)
                        .build());
        XWindow parent = window.parent();
        if (parent != null) {
            server.events().deliverToWindow(parent, EventMasks.SUBSTRUCTURE_NOTIFY,
                    (recipient, w, child, x, y) -> UnmapNotifyEvent.builder()
                            .firstEventOffset((byte) 0)
                            .sentEvent(false)
                            .sequenceNumber(recipient.sequenceNumber())
                            .event(w.id())
                            .window(window.id())
                            .fromConfigure(false)
                            .build());
        }
        for (XWindow child : window.children()) {
            if (child.isMapped()) {
                unmapWindow(child, true);
            }
        }
    }

    // ------------------------------------------------------------------
    // Geometry and stacking
    // ------------------------------------------------------------------

    public void configure(ClientConnection client, ConfigureWindow r) {
        XWindow target = window(client, r.getWindow());
        // Moving or resizing a window is not an untrusted connection's business when the window is
        // not its own, the root included (F7, measured: BadAccess).
        cn.oyzh.fx.xserver.extension.SecurityExtension.requireOwned(client, target,
                "ConfigureWindow");
        applyConfigure(target, r, client);
    }

    /**
     * Resizes a window on the user's behalf, as the frame the host window draws around it.
     *
     * <p>This is the one configure the server owes a client rather than the other way round: the
     * user dragged the host window's edge, so the client's window becomes that size, and the client
     * is told with the same ConfigureNotify it would get from a window manager. Everything else
     * about the change - the area it stops covering, the repaint, the request a client that
     * redirects its children's configures would get - is the same work as any other configure, which
     * is why this goes through the same path rather than resizing the window behind its back.
     */
    public void resizeWindow(XWindow window, int width, int height) {
        if (width <= 0 || height <= 0 || window.isRoot()) {
            return;
        }
        applyConfigure(window, ConfigureWindow.builder()
                .window(window.id())
                .width(width)
                .height(height)
                .build(), null);
    }

    /**
     * Moves a window on the user's behalf, as the placement a window manager does to a window whose
     * position the screen cannot show.
     *
     * <p>The other half of the server's own configure: a client is free to put its top level window
     * anywhere, including ten pixels off the left of a screen it has just been told is narrower than
     * the window it is creating - which is what a toolkit that centres a window larger than the
     * display comes out as (see {@code XServer.fitContentOnScreen}, and EasyShell's own 1300x700
     * window on a 1280x800 screen). Those pixels do not exist, so the window is put back on the
     * screen with the same ConfigureNotify a window manager's placement would send.
     */
    public void moveWindow(XWindow window, int x, int y) {
        if (window.isRoot()) {
            return;
        }
        applyConfigure(window, ConfigureWindow.builder()
                .window(window.id())
                .x((short) x)
                .y((short) y)
                .build(), null);
    }

    /**
     * @param client the connection a rejected change is reported to, or null when the server is the
     *     one making it. Only the stacking path ever needs it, and no server-initiated change
     *     restacks anything.
     */
    private void applyConfigure(XWindow window, ConfigureWindow r, ClientConnection client) {
        int mask = Short.toUnsignedInt(r.getValueMask());
        int oldX = window.x();
        int oldY = window.y();
        int oldWidth = window.width();
        int oldHeight = window.height();
        int oldBorder = window.borderWidth();
        // Where it showed before the change, so the parts it stops covering can be handed back to
        // the windows underneath and the parts it starts covering can be drawn. What it *covered*,
        // so a window driven by its children - every toolkit's dialog and toplevel - hands back
        // where its content was rather than the nothing its own region is.
        List<Rectangle> before = coveredRegionOf(window);

        if (ConfigWindow.X.isEnabled(mask)) {
            window.setPosition(r.getX(), window.y());
        }
        if (ConfigWindow.Y.isEnabled(mask)) {
            window.setPosition(window.x(), r.getY());
        }
        if (ConfigWindow.WIDTH.isEnabled(mask)) {
            int width = r.getWidth();
            if (width == 0) {
                throw ProtocolException.badValue(width);
            }
            window.setSize(width, window.height());
        }
        if (ConfigWindow.HEIGHT.isEnabled(mask)) {
            int height = r.getHeight();
            if (height == 0) {
                throw ProtocolException.badValue(height);
            }
            window.setSize(window.width(), height);
        }
        if (window.width() != oldWidth || window.height() != oldHeight) {
            // An extension holding buffers sized to this window has to follow it now rather than on
            // its next request: a client that resizes and then draws would otherwise draw into a
            // buffer the window has outgrown (DOUBLE-BUFFER; see Extension#windowResized).
            server.windowResized(window, oldWidth, oldHeight);
        }
        if (ConfigWindow.BORDER_WIDTH.isEnabled(mask)) {
            window.setBorderWidth(r.getBorderWidth());
        }
        if (ConfigWindow.STACK_MODE.isEnabled(mask)) {
            applyStackMode(client, window, r.getSibling(), r.getStackMode());
        }

        boolean moved = window.x() != oldX || window.y() != oldY;
        boolean resized = window.width() != oldWidth || window.height() != oldHeight
                || window.borderWidth() != oldBorder;
        boolean restacked = ConfigWindow.STACK_MODE.isEnabled(mask);

        if (moved || resized || restacked) {
            // A restack changes who is visible where just as a move does, so it is reported and
            // repainted the same way - until now it did neither.
            notifyConfigure(window, r, mask, moved, resized);
            visibilityChanged(window, before, visibleRegionOf(window), moved, resized);
            if (resized) {
                // The window's own pixels, moved the way its bit gravity asks - after the area it
                // gained has been painted, because what moves lands on top of that.
                applyBitGravity(window, oldWidth, oldHeight);
            }
            server.markScreenDirty(window);
        }

        // The children move with a resize of this window, each the way its own win gravity asks for.
        // Done after this window's own repaint rather than before it: the child's move hands back the
        // area it leaves, and the window it leaves it to has just been painted.
        if (window.width() != oldWidth || window.height() != oldHeight) {
            applyWinGravity(window, window.width() - oldWidth, window.height() - oldHeight);
        }
    }

    /**
     * Moves a window's own pixels the way its {@code bit_gravity} says they follow a resize - the
     * other half of A12, and the half the item left open with the reason "with no per-window backing
     * store there is no content to shift". The store is not the point: a window's pixels are in the
     * framebuffer while they are visible, and a reference server moves them. Measured one gravity at
     * a time with {@code tools/BitGravityProbe}, on a window grown by dW x dH with a marker drawn
     * near each corner:
     *
     * <pre>
     * gravity  1 NW  2 N      3 NE   4 W     5 Center   6 E    7 SW   8 S      9 SE   10 Static  0 Forget
     * moves by 0,0   dW/2,0   dW,0   0,dH/2  dW/2,dH/2  dW,dH/2 0,dH  dW/2,dH  dW,dH  0,0       gone
     * </pre>
     *
     * <p>It is the grid {@link #applyWinGravity} uses, and so is {@link #shift}: none, half or all of
     * the change. Two of the nine are not a shift at all - {@code Static} leaves the pixels where
     * they are (the same as {@code NorthWest} for a resize that does not also move the window), and
     * {@code Forget} throws them away, which is what the measured marker does: it is simply gone, and
     * the window is its background afterwards.
     *
     * <p>What moves is what was <em>visible</em>. This server keeps no per-window content, so the
     * parts another window covered are not there to move; the area the content leaves behind is
     * painted with the window's background and exposed, which is what a reveal leaves too.
     */
    private void applyBitGravity(XWindow window, int oldWidth, int oldHeight) {
        int deltaWidth = window.width() - oldWidth;
        int deltaHeight = window.height() - oldHeight;
        if (deltaWidth == 0 && deltaHeight == 0) {
            return;
        }
        if (window.keepsOwnPixels()) {
            // The window's pixels are in a store of its own now, so the gravity moves them *there*
            // and the screen is composited from the result. Moving screen pixels instead would move
            // the copy on the screen and leave the store behind it stale (B5).
            applyBitGravityToStore(window, oldWidth, oldHeight, deltaWidth, deltaHeight);
            return;
        }
        // Everything here is in *screen* coordinates: the visible region, `absoluteBounds`, and both
        // of the helpers this calls (`paintBackground`'s clip and `deliverExpose`'s rectangle) are
        // screen rectangles. Passing one that is window relative paints nothing at all when the
        // window is not at the origin, which is what the first version of this did.
        Rectangle windowRect = window.absoluteBounds();
        int gravity = window.bitGravity();
        if (gravity == FORGET_GRAVITY) {
            paintBackground(window, windowRect);
            deliverExpose(window, windowRect);
            log.debug("bit gravity Forget on 0x{}: the contents are thrown away",
                    Integer.toHexString(window.id()));
            return;
        }
        if (gravity < NORTH_WEST_GRAVITY || gravity > SOUTH_EAST_GRAVITY) {
            return;                                  // Static, and anything unknown: nothing moves
        }
        int row = (gravity - NORTH_WEST_GRAVITY) / 3;
        int column = (gravity - NORTH_WEST_GRAVITY) % 3;
        int shiftX = shift(deltaWidth, column);
        int shiftY = shift(deltaHeight, row);
        if (shiftX == 0 && shiftY == 0) {
            return;
        }
        Framebuffer framebuffer = window.screen().framebuffer();
        int absX = window.absoluteX();
        int absY = window.absoluteY();
        // The visible region is in *screen* coordinates and the framebuffer is the screen, so the copy
        // is done there; what the void arithmetic wants is window relative, and that is what `moved`
        // is translated back to.
        Rectangle oldBounds = new Rectangle(absX, absY, oldWidth, oldHeight);
        Rectangle newBounds = window.absoluteBounds();
        List<Rectangle> moved = new ArrayList<>();
        for (Rectangle visible : window.visibleRegion()) {
            Rectangle source = visible.intersection(oldBounds);
            if (source.isEmpty()) {
                continue;
            }
            Rectangle destination = new Rectangle(source.x + shiftX, source.y + shiftY,
                    source.width, source.height).intersection(newBounds);
            if (destination.isEmpty()) {
                continue;
            }
            framebuffer.copyFrom(framebuffer, destination.x - shiftX, destination.y - shiftY,
                    destination.x, destination.y, destination.width, destination.height);
            moved.add(destination);
        }
        // What the content left behind: the window, minus what was moved into it.
        List<Rectangle> left = ShapeState.subtract(List.of(windowRect), moved);
        for (Rectangle area : left) {
            paintBackground(window, area);
        }
        deliverExpose(window, left);
        if (log.isDebugEnabled()) {
            log.debug("bit gravity {} on 0x{}: the contents moved by {},{}",
                    gravity, Integer.toHexString(window.id()), shiftX, shiftY);
        }
    }

    /**
     * {@link #applyBitGravity} for a window whose pixels are in a store of its own: the same table,
     * moved inside the store instead of on the screen.
     *
     * <p>The store was already reallocated at the new size when it was asked for, with the old
     * contents kept at the top left ({@code XWindow.ownPixels}), so "the old contents" here are the
     * {@code oldWidth x oldHeight} rectangle at the origin and the rest is the background the new
     * area was made with. The regions are the window's own coordinates; what the client is told
     * (an {@code Expose} for what the contents left behind) still goes out with screen rectangles,
     * which is what {@code paintBackground} and {@code deliverExpose} take.
     */
    private void applyBitGravityToStore(XWindow window, int oldWidth, int oldHeight,
                                        int deltaWidth, int deltaHeight) {
        Framebuffer pixels = window.ownPixels();
        int gravity = window.bitGravity();
        Rectangle oldBounds = new Rectangle(0, 0, oldWidth, oldHeight);
        Rectangle newBounds = new Rectangle(0, 0, window.width(), window.height());
        if (gravity == FORGET_GRAVITY) {
            paintBackground(window, window.absoluteBounds());
            deliverExpose(window, window.absoluteBounds());
            log.debug("bit gravity Forget on 0x{}: the contents are thrown away",
                    Integer.toHexString(window.id()));
            return;
        }
        if (gravity < NORTH_WEST_GRAVITY || gravity > SOUTH_EAST_GRAVITY) {
            // Static, and anything unknown: nothing moves, but the store's contents still have to
            // reach the screen, because the window has just changed size and the part it gained was
            // painted into the store underneath them.
            server.compositeOwnPixels(window, newBounds);
            return;
        }
        int row = (gravity - NORTH_WEST_GRAVITY) / 3;
        int column = (gravity - NORTH_WEST_GRAVITY) % 3;
        int shiftX = shift(deltaWidth, column);
        int shiftY = shift(deltaHeight, row);
        if (shiftX == 0 && shiftY == 0) {
            server.compositeOwnPixels(window, newBounds);
            return;
        }
        List<Rectangle> moved = new ArrayList<>();
        Rectangle source = oldBounds.intersection(newBounds);
        if (!source.isEmpty()) {
            Rectangle destination = new Rectangle(source.x + shiftX, source.y + shiftY,
                    source.width, source.height).intersection(newBounds);
            if (!destination.isEmpty()) {
                pixels.copyFrom(pixels, destination.x - shiftX, destination.y - shiftY,
                        destination.x, destination.y, destination.width, destination.height);
                moved.add(destination);
            }
        }
        // What the content left behind is painted with the background, and the client is told to
        // draw it - the same answer the screen path gives, in the window's own coordinates turned
        // back into screen ones for the two helpers that speak them.
        int originX = window.absoluteX();
        int originY = window.absoluteY();
        List<Rectangle> left = new ArrayList<>();
        for (Rectangle area : ShapeState.subtract(List.of(newBounds), moved)) {
            Rectangle onScreen = new Rectangle(area.x + originX, area.y + originY, area.width,
                    area.height);
            paintBackground(window, onScreen);
            left.add(onScreen);
        }
        deliverExpose(window, left);
        server.compositeOwnPixels(window, newBounds);
        if (log.isDebugEnabled()) {
            log.debug("bit gravity {} on 0x{}: the contents moved by {},{} in its own store",
                    gravity, Integer.toHexString(window.id()), shiftX, shiftY);
        }
    }

    /**
     * Moves a window's children the way their {@code win_gravity} says they follow a resize of their
     * parent - the second half of A12, and the half that is a *server's* to do.
     *
     * <p>A window's position is relative to its parent, so a parent that grows leaves every child
     * where it was unless something moves it. The gravity names the edge or the axis whose distance
     * from the parent's is to be kept: {@code East} keeps the gap to the parent's right edge, so the
     * child moves right by the whole of the width change, and its vertical half is `Center`, so it
     * moves down by half the height change. {@code Forget} and {@code Static} do not move it at all.
     * The whole table was measured against a reference server, one gravity at a time
     * ({@code tools/GravityProbe}), because the two that are easy to get wrong are exactly the halves:
     * {@code North} moves the child by half the width change and {@code South} by all of it.
     *
     * <p><b>The reference moves the child silently</b> - no {@code ConfigureNotify}, no expose - which
     * was measured too, and it is not as odd as it sounds: the client declared the gravity, so it can
     * predict where the child went. This server cannot copy the child's pixels along (there is no
     * backing store, see the design notes), so it does what it does for any window that moves: hands
     * back the area the child vacated, paints its new area with its own background and asks it to
     * draw. That is the one divergence here, and it is deliberate - the alternative is a child
     * showing whatever used to be under it.
     */
    private void applyWinGravity(XWindow window, int deltaWidth, int deltaHeight) {
        if (deltaWidth == 0 && deltaHeight == 0) {
            return;
        }
        for (XWindow child : List.copyOf(window.children())) {
            int gravity = child.winGravity();
            if (gravity < NORTH_WEST_GRAVITY || gravity > SOUTH_EAST_GRAVITY) {
                // Forget (0) and Static (10): the child stays where it is. Checked as a range so that
                // a gravity this server does not know is treated as "do not move it" rather than as
                // an array index.
                continue;
            }
            // The nine numbered gravities are a three by three grid, north to south and west to
            // east: the row says what happens vertically and the column horizontally.
            int row = (gravity - NORTH_WEST_GRAVITY) / 3;
            int column = (gravity - NORTH_WEST_GRAVITY) % 3;
            int x = child.x() + shift(deltaWidth, column);
            int y = child.y() + shift(deltaHeight, row);
            if (x == child.x() && y == child.y()) {
                continue;
            }
            List<Rectangle> was = coveredRegionOf(child);
            child.setPosition(x, y);
            if (log.isDebugEnabled()) {
                log.debug("child 0x{} moved to {},{} by win gravity {}: the parent grew by {},{}",
                        Integer.toHexString(child.id()), x, y, gravity, deltaWidth, deltaHeight);
            }
            visibilityChanged(child, was, coveredRegionOf(child), true, false);
            server.markScreenDirty(child);
        }
    }

    /**
     * How far a child moves on one axis, from its gravity's column (or row): the first third of the
     * grid keeps the distance to the near edge, the middle centres it, and the last keeps the distance
     * to the far edge - so the shift is none, half or all of the change.
     */
    private static int shift(int delta, int third) {
        return third == 0 ? 0 : third == 1 ? delta / 2 : delta;
    }

    /** X's gravity numbers: 1 is north-west and 9 is south-east, in a three by three grid. */
    private static final int NORTH_WEST_GRAVITY = 1;
    private static final int SOUTH_EAST_GRAVITY = 9;
    /** {@code Forget}: not a place to keep the contents but the instruction to drop them. */
    private static final int FORGET_GRAVITY = 0;

    private void notifyConfigure(XWindow window, ConfigureWindow r, int mask, boolean moved,
                                 boolean resized) {
        // The sibling directly above it now, which is how a client follows the stacking order; 0
        // (None) means it is the topmost.
        int sibling = 0;
        XWindow parent = window.parent();
        if (parent != null) {
            List<XWindow> siblings = parent.children();
            int index = siblings.indexOf(window);
            if (index >= 0 && index + 1 < siblings.size()) {
                sibling = siblings.get(index + 1).id();
            }
        }
        // Copied so the event factories can capture it.
        final int aboveSibling = sibling;
        server.events().deliverToWindow(window, EventMasks.STRUCTURE_NOTIFY,
                (recipient, w, child, x, y) -> ConfigureNotifyEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(recipient.sequenceNumber())
                        .event(w.id())
                        .window(window.id())
                        .aboveSibling(aboveSibling)
                        .x((short) window.x())
                        .y((short) window.y())
                        .width((short) window.width())
                        .height((short) window.height())
                        .borderWidth((short) window.borderWidth())
                        .overrideRedirect(window.isOverrideRedirect())
                        .build());
        if (parent != null) {
            server.events().deliverToWindow(parent, EventMasks.SUBSTRUCTURE_NOTIFY,
                    (recipient, w, child, x, y) -> ConfigureNotifyEvent.builder()
                            .firstEventOffset((byte) 0)
                            .sentEvent(false)
                            .sequenceNumber(recipient.sequenceNumber())
                            .event(w.id())
                            .window(window.id())
                            .aboveSibling(aboveSibling)
                            .x((short) window.x())
                            .y((short) window.y())
                            .width((short) window.width())
                            .height((short) window.height())
                            .borderWidth((short) window.borderWidth())
                            .overrideRedirect(window.isOverrideRedirect())
                            .build());
            if (parent.anyClientSelected(EventMasks.SUBSTRUCTURE_REDIRECT)) {
                int stackMode = ConfigWindow.STACK_MODE.isEnabled(mask) ? r.getStackMode()
                        : StackMode.ABOVE.getValue();
                // Built from raw bytes: the generated ConfigureRequestEvent builder does not expose
                // value_mask, and a window manager cannot act on the request without it.
                server.events().deliverToWindow(parent, EventMasks.SUBSTRUCTURE_REDIRECT,
                        (recipient, w, child, x, y) -> RawEvent.builder(recipient,
                                        EventMasks.CONFIGURE_REQUEST_CODE)
                                .byteAt(1, stackMode)
                                .card32(4, w.id())
                                .card32(8, window.id())
                                .card32(12, r.getSibling())
                                .card16(16, r.getX())
                                .card16(18, r.getY())
                                .card16(20, r.getWidth())
                                .card16(22, r.getHeight())
                                .card16(24, r.getBorderWidth())
                                .card16(26, mask)
                                .build());
            }
        }
    }

    private void applyStackMode(ClientConnection client, XWindow window, int siblingId,
                                int stackMode) {
        XWindow parent = window.parent();
        if (parent == null) {
            return;
        }
        XWindow sibling = siblingId == 0 ? null : window(client, siblingId);
        if (sibling != null && sibling.parent() != parent) {
            throw ProtocolException.badMatch();
        }
        List<XWindow> children = parent.children();
        boolean topMost = children.indexOf(window) == children.size() - 1;
        if (stackMode == StackMode.ABOVE.getValue()) {
            children.remove(window);
            if (sibling == null) {
                children.add(window);
            } else {
                children.add(Math.min(children.indexOf(sibling) + 1, children.size()), window);
            }
        } else if (stackMode == StackMode.BELOW.getValue()) {
            children.remove(window);
            if (sibling == null) {
                children.add(0, window);
            } else {
                children.add(Math.max(0, children.indexOf(sibling)), window);
            }
        } else if (stackMode == StackMode.TOP_IF.getValue()) {
            if (window.isMapped()) {
                children.remove(window);
                children.add(window);
            }
        } else if (stackMode == StackMode.BOTTOM_IF.getValue()) {
            if (window.isMapped()) {
                children.remove(window);
                children.add(0, window);
            }
        } else if (stackMode == StackMode.OPPOSITE.getValue()) {
            children.remove(window);
            if (topMost) {
                children.add(0, window);
            } else {
                children.add(window);
            }
        } else {
            throw ProtocolException.badValue(stackMode);
        }
    }

    public void circulate(ClientConnection client, CirculateWindow r) {
        XWindow parent = window(client, r.getWindow());
        int direction = r.getDirection();
        if (direction != 0 && direction != 1) {
            throw ProtocolException.badValue(direction);
        }
        List<XWindow> mapped = new ArrayList<>();
        for (XWindow child : parent.children()) {
            if (child.isMapped()) {
                mapped.add(child);
            }
        }
        if (mapped.size() < 2) {
            return;
        }
        // Circulating is a restack of every mapped child, so what each of them shows changes: their
        // regions are captured before the order moves and compared after, which is what gets the
        // newly covered windows told to stop and the newly revealed ones told to draw. What is
        // captured is what each child covers - its own children included, as everywhere the
        // question is "what does this leave behind" - so a frame is not read as showing nothing.
        Map<XWindow, List<Rectangle>> before = new LinkedHashMap<>();
        for (XWindow child : mapped) {
            before.put(child, coveredRegionOf(child));
        }
        for (XWindow child : mapped) {
            int index = parent.children().indexOf(child);
            parent.children().remove(index);
            if (direction == Circulate.RAISE_LOWEST.getValue()) {
                parent.children().add(child);
            } else {
                parent.children().add(0, child);
            }
            server.events().deliverToWindow(child, EventMasks.STRUCTURE_NOTIFY,
                    (recipient, w, c, x, y) -> CirculateNotifyEvent.builder()
                            .firstEventOffset((byte) 0)
                            .sentEvent(false)
                            .sequenceNumber(recipient.sequenceNumber())
                            .event(w.id())
                            .window(child.id())
                            .place((byte) (direction == Circulate.RAISE_LOWEST.getValue()
                                    ? Place.ON_TOP.getValue() : Place.ON_BOTTOM.getValue()))
                            .build());
        }
        for (XWindow child : mapped) {
            visibilityChanged(child, before.get(child), visibleRegionOf(child));
        }
        server.markScreenDirty(parent);
    }

    public void getGeometry(ClientConnection client, GetGeometry r) {
        XResource drawable = server.resources().requireDrawable(client, r.getDrawable());
        if (drawable instanceof XWindow window) {
            client.sendReply(GetGeometryReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .depth((byte) window.depth())
                    .root(window.root().id())
                    .x((short) window.x())
                    .y((short) window.y())
                    .width((short) window.width())
                    .height((short) window.height())
                    .borderWidth((short) window.borderWidth())
                    .build());
        } else {
            XPixmap pixmap = (XPixmap) drawable;
            client.sendReply(GetGeometryReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .depth((byte) pixmap.depth())
                    .root(pixmap.screen().root().id())
                    .x((short) 0)
                    .y((short) 0)
                    .width((short) pixmap.width())
                    .height((short) pixmap.height())
                    .borderWidth((short) 0)
                    .build());
        }
    }

    public void queryTree(ClientConnection client, QueryTree r) {
        XWindow window = window(client, r.getWindow());
        List<Integer> children = new ArrayList<>();
        for (XWindow child : window.children()) {
            children.add(child.id());
        }
        client.sendReply(QueryTreeReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .root(window.root().id())
                .parent(window.parent() == null ? 0 : window.parent().id())
                .children(IntLists.immutable.ofAll(children))
                .build());
    }

    public void queryBestSize(ClientConnection client, QueryBestSize r) {
        // Every size is acceptable: the framebuffer has no tiling constraints and the cursor is
        // drawn from an ARGB image rather than a hardware sprite.
        server.resources().requireDrawable(client, r.getDrawable());
        client.sendReply(QueryBestSizeReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .width(r.getWidth())
                .height(r.getHeight())
                .build());
    }

    public void translateCoordinates(ClientConnection client, TranslateCoordinates r) {
        XWindow src = window(client, r.getSrcWindow());
        XWindow dst = window(client, r.getDstWindow());
        int screenX = src.absoluteX() + r.getSrcX();
        int screenY = src.absoluteY() + r.getSrcY();
        XWindow child = dst.windowAt(screenX, screenY);
        int childId = child == dst ? 0 : child.id();
        client.sendReply(TranslateCoordinatesReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .sameScreen(true)
                .child(childId)
                .dstX((short) (screenX - dst.absoluteX()))
                .dstY((short) (screenY - dst.absoluteY()))
                .build());
    }

    // ------------------------------------------------------------------
    // Painting
    // ------------------------------------------------------------------

    /**
     * Fills the window's inner rectangle with its background, clipped to the part of it that is
     * visible.
     *
     * <p>The visible region is applied here rather than left to the caller: a window's background
     * painted over the area a window on top of it occupies is exactly the way a dialog gets erased,
     * and every caller - a map, a configure, a clear, a resize - would have to remember not to.
     */
    public void paintBackground(XWindow window, Rectangle clip) {
        paintBackground(window, List.of(clip));
    }

    /**
     * The same, for a region to paint rather than one rectangle.
     *
     * <p>What a shape change hands back is a region, and a region can be thousands of rectangles - a
     * shape taken from a mask is one rectangle per row. Each rectangle used to pay for a visible
     * region computation of its own, which is where a shape change on a maximised window spent tens
     * of seconds; the whole region is painted with one here.
     */
    public void paintBackground(XWindow window, List<Rectangle> clips) {
        if (window.isInputOnly() || clips.isEmpty()) {
            return;
        }
        if (window.isTransparentUntilDrawn()) {
            // The server's own surface has no background: a reference creates it with
            // `background = None`, so nothing is painted and nothing is composited until a
            // compositor draws there. Painting the desktop colour would cover the screen.
            return;
        }
        if (window.keepsOwnPixels()) {
            // A window with pixels of its own is painted into them - the whole window, not only the
            // part that shows - and composited from there (B5). The clip the caller passes is in
            // screen coordinates, as every caller works in them.
            Rectangle bounds = window.absoluteBounds();
            List<Rectangle> areas = new ArrayList<>(clips.size());
            List<Rectangle> local = new ArrayList<>(clips.size());
            for (Rectangle clip : clips) {
                Rectangle area = bounds.intersection(clip);
                if (area.isEmpty()) {
                    continue;
                }
                areas.add(area);
                local.add(new Rectangle(area.x - window.absoluteX(), area.y - window.absoluteY(),
                        area.width, area.height));
            }
            if (areas.isEmpty()) {
                return;
            }
            Framebuffer pixels = window.ownPixels();
            if (window.isBackgroundPixmap() && window.backgroundPixmap() instanceof XPixmap tile) {
                for (Rectangle part : local) {
                    tileInto(pixels, tile, part, 0, 0);
                }
            } else {
                for (Rectangle part : local) {
                    pixels.fillRect(part.x, part.y, part.width, part.height,
                            window.backgroundPixel());
                }
            }
            server.compositeOwnPixels(window, local);
            for (Rectangle area : areas) {
                cn.oyzh.fx.xserver.extension.DamageExtension.drew(server, window, area);
            }
            return;
        }
        Framebuffer framebuffer = window.screen().framebuffer();
        List<Rectangle> visible = window.visibleRegion();
        for (Rectangle clip : clips) {
            Rectangle clipped = window.absoluteBounds().intersection(clip);
            if (clipped.isEmpty()) {
                continue;
            }
            for (Rectangle piece : visible) {
                Rectangle area = clipped.intersection(piece);
                if (area.isEmpty()) {
                    continue;
                }
                if (window.isBackgroundPixmap() && window.backgroundPixmap() instanceof XPixmap tile) {
                    tileInto(framebuffer, tile, area, window.absoluteX(), window.absoluteY());
                } else {
                    framebuffer.fillRect(area.x, area.y, area.width, area.height,
                            window.backgroundPixel());
                }
                // The server painting a window's background is a change to that window's pixels like
                // any other, and a client that asked to be told about changes hears it: measured with
                // tools/DamageProbe, where a reference server's accumulated damage covers the whole
                // window once it has been mapped and this server (which hooked only the drawing
                // requests) reported only what the client drew itself.
                cn.oyzh.fx.xserver.extension.DamageExtension.drew(server, window, area);
            }
        }
    }

    private void paintBorder(XWindow window, Rectangle clip) {
        int border = window.borderWidth();
        if (border == 0) {
            return;
        }
        Rectangle outer = window.absoluteBoundsWithBorder().intersection(clip);
        if (outer.isEmpty()) {
            return;
        }
        Rectangle inner = window.absoluteBounds();
        Framebuffer framebuffer = window.screen().framebuffer();
        XPixmap tile = tileOf(window);
        int colour = window.borderPixel();
        // Four bands around the inner rectangle.
        fill(framebuffer, tile, colour, outer.x, outer.y, outer.width, inner.y - outer.y);
        fill(framebuffer, tile, colour, outer.x, inner.y + inner.height, outer.width,
                outer.y + outer.height - (inner.y + inner.height));
        fill(framebuffer, tile, colour, outer.x, inner.y, inner.x - outer.x, inner.height);
        fill(framebuffer, tile, colour, inner.x + inner.width, inner.y,
                outer.x + outer.width - (inner.x + inner.width), inner.height);
    }

    private XPixmap tileOf(XWindow window) {
        return window.isBorderPixmap() && window.borderPixmap() instanceof XPixmap pixmap
                ? pixmap : null;
    }

    /** Fills one border band, with the border pixmap when there is one and the pixel otherwise. */
    private void fill(Framebuffer framebuffer, XPixmap tile, int colour, int x, int y, int w,
                      int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        if (tile == null) {
            framebuffer.fillRect(x, y, w, h, colour);
            return;
        }
        tileInto(framebuffer, tile, new Rectangle(x, y, w, h), 0, 0);
    }

    /** Tiles a pixmap across an area, aligned so that (originX, originY) is tile position 0,0. */
    private void tileInto(Framebuffer framebuffer, XPixmap tile, Rectangle area, int originX,
                          int originY) {
        if (tile.width() <= 0 || tile.height() <= 0) {
            return;
        }
        for (int y = area.y; y < area.y + area.height; y++) {
            int ty = Math.floorMod(y - originY, tile.height());
            for (int x = area.x; x < area.x + area.width; x++) {
                int tx = Math.floorMod(x - originX, tile.width());
                framebuffer.setPixel(x, y, tile.pixel(tx, ty));
            }
        }
    }

    /**
     * Tells the clients watching a window that its visibility changed.
     *
     * <p>Toolkits use this to decide when a window is worth drawing: a window that is mapped but
     * never reported as visible can leave an application waiting for a notification that never
     * comes, with its window sitting unpainted. The server keeps no obscured-region tracking, so a
     * viewable window is reported as unobscured - which is right for the common case of a window
     * that is on top.
     */
    public void deliverVisibilityNotify(XWindow window, int state) {
        if (!window.isViewable()) {
            return;
        }
        server.events().deliverToWindow(window, EventMasks.VISIBILITY_CHANGE,
                (recipient, w, child, x, y) -> VisibilityNotifyEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(recipient.sequenceNumber())
                        .window(w.id())
                        .state((byte) state)
                        .build());
    }

    /**
     * Sends an Expose for the part of {@code rect} that is actually visible.
     *
     * <p>One change's worth on its own; a change that is more than one rectangle goes out as one
     * series through {@link #deliverExpose(XWindow, List)}.
     */
    public void deliverExpose(XWindow window, Rectangle rect) {
        deliverExpose(window, List.of(rect));
    }

    /**
     * Sends the Exposes for the rectangles one change left to be drawn, as a single series.
     *
     * <p>The rectangles are the pieces of one thing that happened to the window - the strip a resize
     * gained, the areas a shape gave back, the parts a reveal uncovered - and the client is told
     * about them together. The {@code count} on every Expose but the last is how many more are still
     * on the way, and it is not decoration: it is what a toolkit reads to know that a series is
     * unfinished. Xt's exposure compression waits for the rest of one and hands the widget a single
     * region, whose bounding box is then the whole window - and that decides what {@code xclock}
     * draws. Grown by its window, xclock throws its backing pixmap away, makes one at the new size,
     * and paints only the area the Exposes name; told that each rectangle was the last, it paints
     * only those strips and then copies the rest of its new, untouched pixmap over the window, which
     * is the black box in the corner of a maximised xclock (2026-10-13).
     *
     * <p>Measured against a reference server (Xvfb) with a raw client: a 600x500 window grown to
     * 800x600 is sent {@code (600,0,200,500)} with count 1 and then {@code (0,500,800,100)} with
     * count 0.
     *
     * <p>{@code rects} arrive in screen coordinates, which is what every caller works in, but the
     * event carries window relative ones. Without the translation a window away from the origin is
     * told to repaint a rectangle that lies outside it - a window at y=600 is told "repaint from
     * y=600" - and a client that believes the rectangle redraws nothing at all, leaving the window
     * as whatever the framebuffer held before: a black window that never paints.
     */
    public void deliverExpose(XWindow window, List<Rectangle> rects) {
        if (!window.isViewable()) {
            return;
        }
        // The rectangles are one region, so they are combined before the visible part of them is
        // taken: each piece of it is named once, whatever the caller's own rectangles did.
        List<Rectangle> pieces = ShapeState.intersect(ShapeState.union(rects, List.of()),
                window.visibleRegion());
        // A region this finely divided is sent whole rather than a piece at a time - see
        // MAX_EXPOSE_PIECES. The pieces are all on the window, so their bounding box is too.
        if (pieces.size() > MAX_EXPOSE_PIECES) {
            pieces = List.of(ShapeState.extents(pieces));
        }
        for (int i = 0; i < pieces.size(); i++) {
            Rectangle visible = pieces.get(i);
            int x = visible.x - window.absoluteX();
            int y = visible.y - window.absoluteY();
            short remaining = (short) (pieces.size() - 1 - i);
            server.events().deliverToWindow(window, EventMasks.EXPOSURE,
                    (recipient, w, child, eventX, eventY) -> ExposeEvent.builder()
                            .firstEventOffset((byte) 0)
                            .sentEvent(false)
                            .sequenceNumber(recipient.sequenceNumber())
                            .window(w.id())
                            .x((short) x)
                            .y((short) y)
                            .width((short) visible.width)
                            .height((short) visible.height)
                            .count(remaining)
                            .build());
        }
    }

    /**
     * Repaints and notifies after a window's visibility changed.
     *
     * <p>{@code before} and {@code after} are the window's visible regions, in screen coordinates,
     * captured around whatever changed its geometry or its place in the stacking order.
     *
     * <p>The parts it gained have to be drawn again: their pixels are whatever was on top of it
     * until now. The parts it lost belong to the windows underneath, which have to be asked to draw
     * as well - nothing of theirs is kept anywhere, so a revealed area is restored by painting the
     * lower window's background and letting its client repaint the content.
     */
    private void visibilityChanged(XWindow window, List<Rectangle> before, List<Rectangle> after) {
        visibilityChanged(window, before, after, false, false);
    }

    /**
     * @param moved true when the window itself moved rather than something about its stacking: a
     *     move leaves the window's own pixels behind - nothing here copies them along - so the whole
     *     of its new area has to be drawn again, not just the strip it gained. Without that a moved
     *     window shows a mix of its old content and whatever it used to be covering, which is what
     *     makes a dragged window look like it stopped working.
     * @param resized true when the window's size changed, which owes the client the *whole* window
     *     rather than the strip it gained - see the Expose below.
     */
    private void visibilityChanged(XWindow window, List<Rectangle> before, List<Rectangle> after,
                                   boolean moved, boolean resized) {
        List<Rectangle> gained = moved ? after : ShapeState.subtract(after, before);
        for (Rectangle area : gained) {
            if (window.keepsOwnPixels() && moved) {
                // A move carries the window's own pixels along with it - only the screen copy is
                // left behind - so the area shows the store, not a fresh background. Painting a
                // background here would wipe the window's contents on every move (B5). Everything
                // else that gains area (a map, a resize, a restack) is painted with the background:
                // it is an area the window has to be told to draw anyway.
                compositeArea(window, area);
            } else {
                paintBackground(window, area);
            }
        }
        // **A resize is a whole window's worth of exposure, not the strip it gained.** Measured on
        // the reference (Xvfb) with a raw client: a 300x300 window grown to 800x300, grown to
        // 300x800, a 600x500 window grown to 800x600, and a shrink to 400x400 are each sent *one*
        // Expose covering the whole new window with count 0 - never the gained strip alone. A
        // toolkit repaints exactly what it is named - xclock builds a fresh pixmap at the new size
        // and paints the damage into it - so the strip alone leaves the rest of a stretched window
        // undefined: the black band down the side of a wide xclock, and the clock that comes back
        // half drawn from a restore. Everything else that gains area (a map, a reveal, a restack,
        // the area a shape gives back) is the strip itself, and goes out as one series so that a
        // toolkit sees it as the single change it is (see {@link #deliverExpose(XWindow, List)}).
        deliverExpose(window, resized ? after : gained);
        if (window.isViewable()) {
            deliverVisibilityNotify(window, visibilityStateOf(window));
        }
        // The windows below keep what they have where this window still covers them, so only the
        // area it stopped covering is redrawn.
        repaintUnder(ShapeState.subtract(before, after), window);
        // The border comes last: it sits outside the window's content, on top of whatever is below
        // it, so painting it with the content only let the windows underneath - the root included -
        // cover it again. Its own visible region is its clip, because the content region stops at
        // the content rectangle and would leave all four bands zero wide.
        for (Rectangle edge : window.outerVisibleRegion()) {
            paintBorder(window, edge);
        }
    }

    /** The visible region of a window, or nothing when it is not on screen at all. */
    private List<Rectangle> visibleRegionOf(XWindow window) {
        return window != null && window.isViewable() && !window.isInputOnly()
                ? window.visibleRegion() : List.of();
    }

    /**
     * The area a window covers - its children included - or nothing when it is not on screen.
     *
     * <p>This is the region to ask for when the question is "what does this window leave behind
     * when it goes away or moves", and it is deliberately not {@link #visibleRegionOf}: a toolkit's
     * toplevel is a frame whose content window covers it entirely, so the frame's own region is
     * empty and giving back only that restores nothing at all. Read it before the window is
     * unmapped or moved - the answer is about where it was.
     */
    private List<Rectangle> coveredRegionOf(XWindow window) {
        return window != null && window.isViewable() && !window.isInputOnly()
                ? window.coveredRegion() : List.of();
    }

    /**
     * A window's bounding shape changed: hand back the area it no longer covers, and ask it to draw
     * the part it has just gained.
     *
     * <p>A shape set on a window that is already on screen changes what is on it just as surely as a
     * move does, and the two halves are the same as a move's: the area the shape took away belongs to
     * whatever is underneath again (its pixels are still the shaped window's - nothing copies window
     * contents along), and the area it gained has to be drawn by the client, which is told with an
     * {@code Expose}.
     */
    public void shapeChanged(XWindow window, List<Rectangle> lost, List<Rectangle> gained) {
        if (!window.isViewable()) {
            return;
        }
        if (!lost.isEmpty()) {
            repaintUnder(lost, window);
        }
        if (!gained.isEmpty()) {
            // The area a shape gives back was never part of the window - its own region stops at the
            // shape, so nothing was ever drawn there - so it is painted with the background and the
            // client is asked to draw it, exactly as a resize's new area is. It is painted in one
            // call rather than a rectangle at a time: a region from a mask is thousands of one pixel
            // tall rectangles, and each of them used to cost a region operation of its own.
            paintBackground(window, gained);
            // The shape clips what is *inside* the window too, so the area it gained belongs to the
            // children that show there as much as to the window itself: they have to be told to draw
            // it, exactly as the window is.
            exposeInferiors(window, gained);
        }
        deliverExpose(window, gained);
    }

    /**
     * Paints and asks to draw the windows inside {@code window} that a shape revealed.
     *
     * <p>A bounding shape clips a window's inferiors as surely as the window itself: a child's
     * visible region is intersected with every ancestor's shape, so when the shape gains area the
     * child's does too. The window cannot stand in for it - {@code paintBackground(window)} fills
     * only the part of it the child does not cover, and {@code deliverExpose(window)} names only
     * that part - so the Expose a shape gain owes has to reach the child that shows there.
     *
     * <p>xeyes is where this was found: the shell carries the eyes' mask and the eyes widget covers
     * the whole shell, so the shell's own region is empty. Maximised, the eyes were never redrawn -
     * only their pupils moved, on the client's own timer - and the eyes stayed at the size they had
     * before (2026-10-13).
     */
    private void exposeInferiors(XWindow window, List<Rectangle> gained) {
        for (XWindow child : window.children()) {
            if (child.isViewable() && !child.isInputOnly()) {
                List<Rectangle> parts = ShapeState.intersect(gained, child.visibleRegion());
                if (!parts.isEmpty()) {
                    paintBackground(child, parts);
                    deliverExpose(child, parts);
                }
            }
            exposeInferiors(child, gained);
        }
    }

    /**
     * Puts the part of a window's own store that falls in a screen rectangle onto the screen.
     *
     * <p>{@code XServer.compositeOwnPixels} works in the window's coordinates and clips to what
     * shows; every caller here has a screen rectangle, and some of it may be outside the window
     * entirely (a move's new area reaches beyond the window it came from), so it is trimmed first.
     */
    private void compositeArea(XWindow window, Rectangle onScreen) {
        Rectangle area = onScreen.intersection(window.absoluteBounds());
        if (area.isEmpty()) {
            return;
        }
        server.compositeOwnPixels(window, new Rectangle(area.x - window.absoluteX(),
                area.y - window.absoluteY(), area.width, area.height));
    }

    /**
     * Repaints the parts of the windows under {@code window} that {@code areas} uncovered.
     *
     * <p>Walked bottom to top so the topmost window of the ones below is painted last, and clipped
     * to each window's own visible region, which is what keeps a window that is itself covered from
     * painting where it does not show.
     */
    private void repaintUnder(List<Rectangle> areas, XWindow window) {
        if (areas.isEmpty()) {
            return;
        }
        for (XWindow candidate : stackingOrder(window)) {
            if (candidate == window || candidate.isInputOnly() || !candidate.isViewable()
                    || candidate.isAbove(window)) {
                // A window above the one that changed was never covered by it.
                continue;
            }
            // One region computation per candidate rather than one per rectangle: what a shape change
            // uncovers is a region - thousands of one pixel tall rectangles when the shape came from a
            // mask - and paying a region operation for each of them is what made that quadratically
            // slow.
            List<Rectangle> visible = candidate.visibleRegion();
            List<Rectangle> parts = new ArrayList<>();
            for (Rectangle area : areas) {
                for (Rectangle piece : visible) {
                    Rectangle part = area.intersection(piece);
                    if (!part.isEmpty()) {
                        parts.add(part);
                    }
                }
            }
            if (parts.isEmpty()) {
                continue;
            }
            // **A window in the way is composited from its own pixels, and asked to draw nothing**:
            // nothing was lost while it was covered (B5). The root is the one window with no store, so
            // it is painted and exposed the way every window used to be.
            List<Rectangle> uncovered = new ArrayList<>();
            if (candidate.keepsOwnPixels()) {
                server.compositeOwnPixels(candidate, localParts(candidate, parts));
                // Pixels that came back changed the window's pixels like any other, and a client that
                // watches damage is told about them.
                for (Rectangle part : parts) {
                    cn.oyzh.fx.xserver.extension.DamageExtension.drew(server, candidate, part);
                }
            } else {
                paintBackground(candidate, parts);
                uncovered.addAll(parts);
            }
            // The border ring is outside the candidate's content, so it is the area this repaint is
            // about that says whether any of it needs drawing - and the ring already excludes the
            // windows above, which have just been painted. Painting an edge again paints the same
            // pixels, so each edge is painted once however many of the parts touch it.
            for (Rectangle edge : candidate.outerVisibleRegion()) {
                for (Rectangle area : areas) {
                    if (!edge.intersection(area).isEmpty()) {
                        paintBorder(candidate, edge);
                        break;
                    }
                }
            }
            // All of the parts this window got back are one series, for the same reason a resize's
            // strip is one: it is a single change to the window, and the count field is what says so.
            deliverExpose(candidate, uncovered);
        }
    }

    /** A region of screen rectangles, in a window's own coordinates. */
    private static List<Rectangle> localParts(XWindow window, List<Rectangle> parts) {
        List<Rectangle> local = new ArrayList<>(parts.size());
        for (Rectangle part : parts) {
            local.add(new Rectangle(part.x - window.absoluteX(), part.y - window.absoluteY(),
                    part.width, part.height));
        }
        return local;
    }

    /** The windows of the tree, a parent before its children and siblings bottom to top. */
    private List<XWindow> stackingOrder(XWindow below) {
        List<XWindow> order = new ArrayList<>();
        collectStackingOrder(below.root(), order);
        return order;
    }

    private static void collectStackingOrder(XWindow window, List<XWindow> order) {
        order.add(window);
        for (XWindow child : window.children()) {
            collectStackingOrder(child, order);
        }
    }

    /**
     * How much of a window is visible, for {@code VisibilityNotify}: invisible when nothing of it
     * shows, partially obscured when something covers part of it, unobscured otherwise.
     */
    private int visibilityStateOf(XWindow window) {
        List<Rectangle> visible = window.visibleRegion();
        if (visible.isEmpty()) {
            return Visibility.FULLY_OBSCURED.getValue();
        }
        return ShapeState.extents(visible).equals(window.clipRect())
                ? Visibility.UNOBSCURED.getValue()
                : Visibility.PARTIALLY_OBSCURED.getValue();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private XWindow window(ClientConnection client, int id) {
        return (XWindow) server.resources().require(client, id, XResource.CLASS_WINDOW, false);
    }
}
