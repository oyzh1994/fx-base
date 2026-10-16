package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.composite.CompositePlugin;
import com.github.moaxcp.x11.protocol.composite.CreateRegionFromBorderClip;
import com.github.moaxcp.x11.protocol.composite.GetOverlayWindow;
import com.github.moaxcp.x11.protocol.composite.GetOverlayWindowReply;
import com.github.moaxcp.x11.protocol.composite.NameWindowPixmap;
import com.github.moaxcp.x11.protocol.composite.QueryVersion;
import com.github.moaxcp.x11.protocol.composite.QueryVersionReply;
import com.github.moaxcp.x11.protocol.composite.RedirectSubwindows;
import com.github.moaxcp.x11.protocol.composite.RedirectWindow;
import com.github.moaxcp.x11.protocol.composite.ReleaseOverlayWindow;
import com.github.moaxcp.x11.protocol.composite.UnredirectSubwindows;
import com.github.moaxcp.x11.protocol.composite.UnredirectWindow;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.resource.RegionResource;
import cn.oyzh.fx.xserver.resource.XPixmap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Composite: what a compositing window manager asks for, and the last of B5's three extensions.
 *
 * <p><b>What this server can honestly offer, and why it is cheap here.</b> The store B5 added means
 * every window already draws into pixels of its own and is composited from them, so redirection is
 * not a change to the drawing path at all: it is the *stopping* of that compositing, and a window's
 * pixels are somewhere a compositor can already read. {@code NameWindowPixmap} hands over a live view
 * of that store, so a redirected window's draws land in it and nowhere else - which is the whole of
 * the extension's behaviour as far as a client can tell.
 *
 * <p><b>Every layout and every rule here was measured against a reference before it was written</b>
 * ({@code tools/CompositeProbe}, Xvfb), because the two mistakes this area invites are both silent:
 *
 * <ul>
 *   <li><b>the version answered is the server's, not the client's</b> - the reference answers
 *       {@code 0.4} to a client asking for 0.4 *and* to one asking for 0.2, where the obvious
 *       implementation returns the lower of the two;
 *   <li><b>a redirected window's drawing must not reach the screen</b>. Repainting a redirected
 *       window on the reference leaves its pixmap holding the new colour and the screen holding the
 *       old one. This server composites every window by default, so an un-guarded version is
 *       correct in every test that never redirects anything - which is all of them.
 * </ul>
 *
 * <p><b>The overlay window is real here, and it is the one thing a compositor draws on.</b> The
 * reference answers {@code GetOverlayWindow} with a screen-sized window of its own; this server
 * creates one the same way - a server-owned window parented to the root, mapped and raised - rather
 * than answering {@code None}, because a compositor given {@code None} has nowhere to put its
 * result. It is created on the first request and released when a client releases it. **It is
 * translucent**, which is what this server's drawing model needs and a real overlay does not: see
 * {@link #overlay} for why a screen-sized opaque window would hide every window behind it and take
 * their visible regions away, and the one rule that had to go with it.
 *
 * <p><b>What is deliberately left out.</b> {@code Redirect} in the {@code Manual} update mode is
 * recorded and treated as {@code Automatic}: the store is always current, so a compositor that asked
 * to be told when to update sees fresher pixels than it asked for rather than stale ones. The
 * difference shows only for a compositor that relies on *not* seeing a change, which is a
 * tearing-avoidance trick rather than a correctness one.
 */
public final class CompositeExtension implements Extension {

    /**
     * Measured: the reference answers 0.4 both to a client that asks for 0.4 and to one that asks
     * for 0.2 - it is the server's version and not the result of a negotiation.
     */
    private static final int MAJOR_VERSION = 0;
    private static final int MINOR_VERSION = 4;

    private final CompositePlugin plugin = new CompositePlugin();

    /** The windows each client redirected, so one that dies does not leave the screen frozen. */
    private final Map<ClientConnection, Set<XWindow>> redirected = new ConcurrentHashMap<>();

    /** The overlay window, one per server, created on demand and released by the clients. */
    private XWindow overlay;

    @Override
    public String name() {
        return "Composite";
    }

    @Override
    public byte majorOpcode() {
        return plugin.getMajorOpcode();
    }

    @Override
    public byte firstEvent() {
        return plugin.getFirstEvent();
    }

    @Override
    public byte firstError() {
        return plugin.getFirstError();
    }

    @Override
    public XProtocolPlugin plugin() {
        return plugin;
    }

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        if (request instanceof QueryVersion) {
            client.sendReply(QueryVersionReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .majorVersion(MAJOR_VERSION)
                    .minorVersion(MINOR_VERSION)
                    .build());
        } else if (request instanceof RedirectWindow r) {
            XWindow window = window(server, client, r.getWindow());
            window.setRedirected(true, r.getUpdate());
            track(client, window);
        } else if (request instanceof RedirectSubwindows r) {
            XWindow window = window(server, client, r.getWindow());
            window.setSubwindowsRedirected(true);
            window.setRedirected(true, r.getUpdate());
            track(client, window);
        } else if (request instanceof UnredirectWindow r) {
            XWindow window = window(server, client, r.getWindow());
            window.setRedirected(false, r.getUpdate());
            untrack(client, window);
            // Back on screen: what it drew while it was somebody else's to place has to reach the
            // screen now, or the area keeps whatever the compositor last put there.
            backOnScreen(server, window);
        } else if (request instanceof UnredirectSubwindows r) {
            XWindow window = window(server, client, r.getWindow());
            window.setSubwindowsRedirected(false);
            window.setRedirected(false, r.getUpdate());
            untrack(client, window);
            backOnScreen(server, window);
            for (XWindow child : window.children()) {
                backOnScreen(server, child);
            }
        } else if (request instanceof CreateRegionFromBorderClip r) {
            region(server, client, r.getRegion(), borderClip(window(server, client, r.getWindow())));
        } else if (request instanceof NameWindowPixmap r) {
            nameWindowPixmap(server, client, r);
        } else if (request instanceof GetOverlayWindow r) {
            client.sendReply(GetOverlayWindowReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .overlayWin(overlay(server, server.screenOf(client)).id())
                    .build());
        } else if (request instanceof ReleaseOverlayWindow) {
            releaseOverlay(server);
        } else {
            throw new IllegalArgumentException("unhandled Composite request " + request.getClass());
        }
    }

    @Override
    public void clientDisconnected(XServer server, ClientConnection client) {
        Set<XWindow> windows = redirected.remove(client);
        if (windows == null) {
            return;
        }
        // A compositor that dies must not leave the windows it redirected invisible for ever: with
        // nobody left to draw them the screen would keep whatever was composited last. The same
        // reasoning as a disconnecting client's passive grabs being dropped with it (A13), and it is
        // measured - the probe kills a redirecting client and reads the screen back.
        for (XWindow window : windows) {
            if (window.isDestroyed()) {
                continue;
            }
            window.setRedirected(false, 0);
            window.setSubwindowsRedirected(false);
            backOnScreen(server, window);
        }
    }

    // ------------------------------------------------------------------

    /**
     * The window's border clip: the part of it that lies inside every one of its ancestors, in the
     * window's own coordinates.
     *
     * <p>The coordinate space is measured rather than read out of the specification, whose words do
     * not fix it: the reference answers a window at {@code +30+30} of size {@code 200x150} with
     * {@code 0,0 200x150}, so the region is in the window's own space and not the screen's
     * ({@code tools/CompositeProbe}'s {@code region} mode reads it back through
     * {@code XFixesFetchRegion}).
     */
    private static List<Rectangle> borderClip(XWindow window) {
        Rectangle clip = new Rectangle(0, 0, window.width(), window.height());
        int offsetX = 0;
        int offsetY = 0;
        XWindow child = window;
        for (XWindow ancestor = window.parent(); ancestor != null; ancestor = ancestor.parent()) {
            // The window's origin inside this ancestor, which is where the ancestor's own rectangle
            // starts when read in the window's coordinates.
            offsetX += child.x();
            offsetY += child.y();
            clip = clip.intersection(new Rectangle(-offsetX, -offsetY, ancestor.width(),
                    ancestor.height()));
            if (clip.isEmpty()) {
                return List.of();
            }
            child = ancestor;
        }
        return List.of(clip);
    }

    private void nameWindowPixmap(XServer server, ClientConnection client, NameWindowPixmap request) {
        XWindow window = window(server, client, request.getWindow());
        int id = request.getPixmap();
        if (!client.ownsIdRange(id) || server.resources().contains(id)) {
            throw ProtocolException.badIdChoice(id);
        }
        // The window's own store, live: a compositor reads it after the window draws, and a copy
        // taken now would be a stale picture within one frame. The view resolves the store on every
        // access, because a resize replaces it (XWindow.ownPixels).
        server.resources().add(XPixmap.view(id, client, window.screen(), window.depth(),
                window.visual(), window::ownPixels));
    }

    /**
     * The overlay window: a screen-sized window of the server's own, above everything, which is what
     * the reference answers with and what a compositor draws its result on.
     *
     * <p><b>It is translucent, and that is not a detail.</b> This server paints every window's
     * background, so a screen-sized opaque overlay would hide every window behind it the moment it
     * was mapped - and, because the obscuring machinery subtracts every mapped window above, it would
     * also leave every window below with an empty visible region and no `Expose` for ever. The
     * overlay takes the depth 32 visual and a fully transparent background instead, so until a
     * compositor draws something it contributes nothing at all, and a window under it goes on being
     * told to paint. That needed one rule to go with it - a translucent window does not take anything
     * away from {@code XWindow.visibleRegion} - and the rule is right on its own: a window that shows
     * what is behind it is not in the way.
     *
     * <p>Its geometry is measured against the reference, which answers {@code 1300x800+0+0} for a
     * 1300x800 screen, so it is the screen's size and not the root's drawable rectangle.
     */
    private XWindow overlay(XServer server, Screen screen) {
        if (overlay == null || overlay.isDestroyed()) {
            // The root's own visual and depth, which is what a reference answers with (measured:
            // depth 24 on a depth 24 screen). A depth 32 overlay is the trap - picom asks RENDER for
            // a picture with the *root's* format and a format that does not match the drawable's
            // depth is BadMatch, which is where it crashed (`tools/CompositeProbe` prints the depth).
            VisualInfo visual = screen.rootVisual();
            overlay = new XWindow(server.nextServerResourceId(), null, screen, screen.root(),
                    screen.rootDepth(), XWindow.INPUT_OUTPUT, visual, 0, 0,
                    screen.width(), screen.height(), 0);
            // The compositor will redirect the root's whole subtree, this window included, and on a
            // reference that costs nothing - the display driver shows the overlay by a path of its
            // own. Here being composited *is* the overlay's only path to the screen, so it is exempt.
            overlay.setRedirectExempt(true);
            overlay.setTransparentUntilDrawn(true);
            server.resources().add(overlay);
            // The stacking order is the parent's children list, bottom first, so a raise is a move to
            // the end. A compositor's result belongs above everything it composited.
            screen.root().children().remove(overlay);
            screen.root().children().add(overlay);
            server.windows().mapWindow(overlay);
        }
        return overlay;
    }

    private void releaseOverlay(XServer server) {
        if (overlay == null) {
            return;
        }
        server.windows().unmapWindow(overlay, true);
        server.resources().free(overlay.id());
        overlay = null;
    }

    /**
     * Puts a window back on the screen, and everything below it, after it stopped being redirected.
     *
     * <p>Walked down the subtree because clearing a subwindows redirect has to undo it for every
     * descendant at once, and the walk is over the same tree the redirect covered.
     */
    private static void backOnScreen(XServer server, XWindow window) {
        if (!window.isInputOnly()) {
            server.compositeOwnPixels(window, new Rectangle(0, 0, window.width(), window.height()));
        }
        for (XWindow child : window.children()) {
            backOnScreen(server, child);
        }
    }

    private void region(XServer server, ClientConnection client, int id, List<Rectangle> rectangles) {
        if (!client.ownsIdRange(id) || server.resources().contains(id)) {
            throw ProtocolException.badIdChoice(id);
        }
        server.resources().add(new RegionResource(id, client, new ArrayList<>(rectangles)));
    }

    private static XWindow window(XServer server, ClientConnection client, int id) {
        return (XWindow) server.resources().require(client, id, XResource.CLASS_WINDOW, false);
    }

    private void track(ClientConnection client, XWindow window) {
        redirected.computeIfAbsent(client, c -> ConcurrentHashMap.newKeySet()).add(window);
    }

    private void untrack(ClientConnection client, XWindow window) {
        Set<XWindow> windows = redirected.get(client);
        if (windows != null) {
            windows.remove(window);
        }
    }
}
