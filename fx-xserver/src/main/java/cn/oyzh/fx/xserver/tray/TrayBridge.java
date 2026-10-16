package cn.oyzh.fx.xserver.tray;

import com.github.moaxcp.x11.protocol.XEvent;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.resource.XProperty;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import cn.oyzh.common.log.JulLog;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The notification area (the freedesktop system tray), with the icons shown in the host's menu bar.
 *
 * <p>A client that wants a tray icon - Java's {@code SystemTray}, Qt's {@code QSystemTrayIcon},
 * GTK's {@code StatusIcon} - looks for the owner of the {@code _NET_SYSTEM_TRAY_S0} selection and
 * gives up when there is none, which is what happened here: the application silently lost a piece of
 * its own interface, and nothing said so. This bridge takes that selection and <b>is</b> the tray
 * host: a client that docks is displayed in the host's menu bar, and a click on the menu-bar icon is
 * delivered to the window the client drew its icon into.
 *
 * <p><b>How it crosses.</b> The protocol is ICCCM-shaped and small. Owning the selection with a
 * window makes the bridge findable; three properties on that window say what it is; a client asks to
 * be displayed with a {@code ClientMessage} of type {@code _NET_SYSTEM_TRAY_OPCODE} carrying
 * {@code SYSTEM_TRAY_REQUEST_DOCK} and its icon window. The icon window is then reparented into a
 * tray window of the server's own - which is what an XEmbed client waits for - and the bridge copies
 * its pixels into the menu bar every {@value #POLL_MILLIS} ms, because there is no X event for "the
 * icon was redrawn".
 *
 * <p><b>Where this differs from a panel, and why.</b> On a real desktop the docked window is shown
 * <em>in the tray</em>, which is a piece of the screen. Here the tray is the menu bar, which is not
 * on the X screen at all - so the icon appears in the menu bar <em>and</em> stays where the client
 * drew it, because this server has one surface (the screen's framebuffer) and a window has to be on
 * it to have pixels at all. There is no way to have the pixels and not the window: an icon window
 * off the edge of the screen is a window whose pixels the framebuffer does not hold, which is also
 * why A9's backing store is the only place a covered window's pixels exist.
 *
 * <p>The second difference is the client's own menu. A tray client draws its context menu as a popup
 * of its own, placed relative to the icon <em>on the X screen</em>, and this bridge cannot move it
 * into the menu bar - the menu is a window the client places itself. Clicking the menu-bar icon
 * therefore delivers a click to the icon window, and the client's menu opens where its window is.
 * That is the honest behaviour for a server whose tray is not part of the screen.
 */
public final class TrayBridge {


    /** The selection the protocol reserves for the first screen's notification area. */
    private static final String SELECTION = "_NET_SYSTEM_TRAY_S0";

    /** How often the docked icons are compared with what the menu bar was last given. */
    private static final long POLL_MILLIS = 200;

    /** {@code SYSTEM_TRAY_REQUEST_DOCK}, and the two opcodes the specification reserves. */
    private static final int OPCODE_REQUEST_DOCK = 0;
    private static final int OPCODE_BEGIN_MESSAGE = 1;
    private static final int OPCODE_CANCEL_MESSAGE = 2;

    /** The largest icon the menu bar is asked to draw; a bigger one is scaled down. */
    private static final int ICON_MAX = 64;

    /** {@code _NET_SYSTEM_TRAY_ORIENTATION} horizontal, which is what a menu bar is. */
    private static final int ORIENTATION_HORIZONTAL = 0;

    private final XServer server;

    /** Where the clicks go once a backend with a menu bar exists, and whether that is wired yet. */
    private boolean listenerAttached;

    /** The window the selection is owned by, and the one dock requests are addressed to. */
    private final XWindow ownerWindow;

    /** The docked icons, in the order they docked. */
    private final List<Docked> docked = new ArrayList<>();

    /** The XEmbed message an icon's client waits for before it will draw anything. */
    private final int xembedAtom;

    private final int opcodeAtom;
    private final int orientationAtom;
    private final int visualAtom;

    /** What the menu bar is showing, so an icon that has not changed is not handed over again. */
    private BufferedImage shown;
    private String shownTooltip;

    private Thread worker;
    private volatile boolean running;

    /** One docked icon: the client's window, the tray window it was reparented into, its pixels. */
    private record Docked(XWindow window, XWindow trayWindow) {
    }

    public TrayBridge(XServer server) {
        this.server = server;
        Screen screen = server.screen();
        this.ownerWindow = new XWindow(server.nextServerResourceId(), null, screen, screen.root(),
                screen.root().depth(), XWindow.INPUT_OUTPUT, screen.root().visual(), 0, 0, 1, 1, 0);
        // Registered here rather than in start(), because the window is a child of the root from the
        // moment it is made and **every window in the tree has to be a resource a client can look
        // up**: one that is not is a BadWindow for whoever walks the tree. A bridge that never starts
        // (--no-clipboard, --no-tray) used to leave its window behind unregistered, and xwininfo's
        // own walk then printed "X Error: 3: Bad Window" - measured 2026-09-28 with a real client.
        server.resources().add(ownerWindow);
        this.xembedAtom = server.atoms().intern("_XEMBED");
        this.opcodeAtom = server.atoms().intern("_NET_SYSTEM_TRAY_OPCODE");
        this.orientationAtom = server.atoms().intern("_NET_SYSTEM_TRAY_ORIENTATION");
        this.visualAtom = server.atoms().intern("_NET_SYSTEM_TRAY_VISUAL");
    }

    /** Whether a window id belongs to this bridge - the window dock requests are sent to. */
    public boolean ownsWindow(int windowId) {
        return windowId == ownerWindow.id();
    }

    public void start() {
        // The properties that make the owner findable and usable, before the selection is taken: a
        // client that sees the owner first asks a tray that is not ready for it yet. The opcode
        // property's *value* is the atom of the message type the client must use, which is how the
        // protocol tells a client what to put in a ClientMessage's type field.
        ownerWindow.properties().put(opcodeAtom,
                new XProperty(opcodeAtom, atomAtom(), 32, card32(opcodeAtom)));
        ownerWindow.properties().put(orientationAtom, new XProperty(orientationAtom, cardinalAtom(), 32,
                card32(ORIENTATION_HORIZONTAL)));
        // The **32 bit ARGB** visual, which is what makes an icon's transparent corners arrive
        // transparent - the client creates its icon window with the visual named here, and a window
        // with an alpha channel keeps its pixels and is composited (B5). Publishing the root's 24 bit
        // visual instead - which is what this did - is why the icon in the menu bar was a square with
        // a light background rather than a cut-out disc.
        int visual = server.screen().root().visual().id();
        for (cn.oyzh.fx.xserver.display.VisualInfo candidate : server.screen().visuals()) {
            if (candidate.hasAlpha()) {
                visual = candidate.id();
                break;
            }
        }
        ownerWindow.properties().put(visualAtom, new XProperty(visualAtom, visualIdAtom(), 32,
                card32(visual)));
        server.setSelectionOwner(SELECTION, ownerWindow.id(), server.events().time(),
                cn.oyzh.fx.xserver.extension.XFixesExtension.SELECTION_SET_OWNER);
        running = true;
        worker = new Thread(this::poll, "xserver-tray");
        worker.setDaemon(true);
        worker.start();
        JulLog.info("the notification area is served by the host window's menu bar: {} is owned",
                SELECTION);
    }

    public void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
        cn.oyzh.fx.xserver.display.DisplayBackend backend = server.backend();
        if (shown != null && backend != null) {
            backend.setTrayIcon(null, null);
            shown = null;
        }
    }

    private int atomAtom() {
        return server.atoms().intern("ATOM", true);
    }

    private int cardinalAtom() {
        return server.atoms().intern("CARDINAL", true);
    }

    private int visualIdAtom() {
        return server.atoms().intern("VISUALID", true);
    }

    /**
     * A 32 bit property value in the order properties are stored in: X11's own, which is big endian.
     * A client reads its 32 bit property data in its own order and swaps what it has to - Xlib does
     * it for every {@code XGetWindowProperty} - so what the server keeps is the canonical form.
     */
    private static byte[] card32(int value) {
        return new byte[] {(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8),
                (byte) value};
    }

    /**
     * A {@code ClientMessage} addressed to the tray: a client asking to be displayed, or a "balloon
     * message" the specification reserves and which nothing here shows - a menu bar has no place to
     * put one, and the protocol has no reply the sender waits for either way.
     */
    public void clientMessage(int messageType, int[] data) {
        if (messageType != opcodeAtom || data.length < 2) {
            return;
        }
        // The opcode is the *second* word and the window the third, which is not how the message
        // reads at a glance and is worth saying out loud: data[0] is the timestamp. Java's tray peer
        // sends it this way and is the client this was measured against - reading the opcode out of
        // data[0] makes every dock request look like "dock the window 0x0", which is a warning in
        // the log and an application that reports it cannot show its icon.
        int opcode = data[1];
        int windowId = data[2];
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("tray message: opcode={} window=0x{} data={}", opcode,
                    Integer.toHexString(windowId), Arrays.toString(data));
        }
        if (opcode == OPCODE_REQUEST_DOCK) {
            server.lockServer();
            try {
                dock(windowId);
            } finally {
                server.unlockServer();
            }
        } else if (opcode != OPCODE_BEGIN_MESSAGE && opcode != OPCODE_CANCEL_MESSAGE) {
            JulLog.debug("the tray was sent opcode {}, which is not one of the three the protocol has",
                    opcode);
        }
    }

    /** Displays a client's icon window: what a dock request asks for, and all of it. */
    private void dock(int windowId) {
        XResource resource = server.resources().get(windowId);
        if (!(resource instanceof XWindow window)) {
            JulLog.warn("a client asked the tray to dock 0x{}, which is not a window",
                    Integer.toHexString(windowId));
            return;
        }
        for (Docked existing : docked) {
            if (existing.window() == window) {
                return;                             // already displayed
            }
        }
        Screen screen = window.screen();
        // One tray window per icon, where the icon already is and the size it asked for: a reparent
        // moves a window by its new parent's origin, and an icon that jumped to the corner of the
        // screen as it docked would be a fault the user can see. The tray window is mapped, because
        // a client's window is only viewable - and so only drawn - under a mapped parent.
        int width = Math.max(1, window.width());
        int height = Math.max(1, window.height());
        XWindow trayWindow = new XWindow(server.nextServerResourceId(), null, screen, screen.root(),
                screen.root().depth(), XWindow.INPUT_OUTPUT, screen.root().visual(),
                window.absoluteX(), window.absoluteY(), width, height, 0);
        server.resources().add(trayWindow);
        server.windows().mapWindow(trayWindow);
        server.windows().reparentTo(window, trayWindow, 0, 0);
        if (!window.isMapped()) {
            server.windows().mapWindow(window);
        }
        docked.add(new Docked(window, trayWindow));
        // XEmbed's "you are embedded now", which is what a client waits for: an icon's peer creates
        // its window, asks to be docked and then *blocks* until the embedder says the reparent has
        // happened - Java's SystemTray, measured here, throws "TrayIcon couldn't be displayed" if
        // this message never comes. data[3] is the embedder's window, which the client uses as the
        // parent it was reparented into, and data[4] is the protocol version.
        sendEmbeddedNotify(window, trayWindow);
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("docked 0x{} ({}x{}) into the tray: icon {} of {}", Integer.toHexString(windowId),
                    width, height, docked.size(), docked.size());
        }
    }

    /** {@code XEMBED_EMBEDDED_NOTIFY}: the message that tells a client its icon is in the tray. */
    private void sendEmbeddedNotify(XWindow window, XWindow trayWindow) {
        ClientConnection recipient = window.owner();
        if (recipient == null) {
            return;
        }
        boolean bigEndian = recipient.isBigEndian();
        byte[] message = new byte[32];
        message[0] = 33;                            // ClientMessage
        message[1] = 32;                            // format
        put2(message, 2, recipient.sequenceNumber(), bigEndian);
        put4(message, 4, window.id(), bigEndian);
        put4(message, 8, xembedAtom, bigEndian);
        put4(message, 12, (int) (server.events().time() & 0xFFFFFFFFL), bigEndian);
        put4(message, 16, XEMBED_EMBEDDED_NOTIFY, bigEndian);
        put4(message, 20, 0, bigEndian);            // detail
        put4(message, 24, trayWindow.id(), bigEndian);      // the embedder's window
        put4(message, 28, 0, bigEndian);            // XEmbed version
        server.events().send(recipient,
                cn.oyzh.fx.xserver.event.RawEvent.of(message, (short) recipient.sequenceNumber()));
    }

    /** {@code XEMBED_EMBEDDED_NOTIFY}, the first of the protocol's messages. */
    private static final int XEMBED_EMBEDDED_NOTIFY = 0;

    private static void put2(byte[] message, int at, int value, boolean bigEndian) {
        if (bigEndian) {
            message[at] = (byte) (value >> 8);
            message[at + 1] = (byte) value;
        } else {
            message[at] = (byte) value;
            message[at + 1] = (byte) (value >> 8);
        }
    }

    private static void put4(byte[] message, int at, int value, boolean bigEndian) {
        if (bigEndian) {
            message[at] = (byte) (value >> 24);
            message[at + 1] = (byte) (value >> 16);
            message[at + 2] = (byte) (value >> 8);
            message[at + 3] = (byte) value;
        } else {
            message[at] = (byte) value;
            message[at + 1] = (byte) (value >> 8);
            message[at + 2] = (byte) (value >> 16);
            message[at + 3] = (byte) (value >> 24);
        }
    }

    /** An icon window (or something inside it) is going away: it leaves the tray. */
    public void forgetWindow(XWindow window) {
        List<Docked> leaving = new ArrayList<>();
        server.lockServer();
        try {
            for (Docked icon : docked) {
                if (icon.window() == window || window.isAncestorOf(icon.window())) {
                    leaving.add(icon);
                }
            }
            docked.removeAll(leaving);
        } finally {
            server.unlockServer();
        }
        for (Docked icon : leaving) {
            if (server.resources().get(icon.trayWindow().id()) != null) {
                server.resources().free(icon.trayWindow().id());
            }
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("an icon left the tray: 0x{}", Integer.toHexString(icon.window().id()));
            }
        }
    }

    /** A client that disconnected takes its icons with it. */
    public void clientDisconnected(ClientConnection client) {
        List<XWindow> orphans = new ArrayList<>();
        server.lockServer();
        try {
            for (Docked icon : docked) {
                if (icon.window().owner() == client) {
                    orphans.add(icon.window());
                }
            }
        } finally {
            server.unlockServer();
        }
        for (XWindow window : orphans) {
            forgetWindow(window);
        }
    }

    /** How wide the composed strip of icons is, which is what a position inside it is relative to. */
    private int stripWidth() {
        int width = 0;
        for (Docked icon : docked) {
            width += Math.max(1, icon.window().width());
        }
        return width;
    }

    /** The icon windows on display, for tests and for the JulLog. */
    public List<XWindow> icons() {
        List<XWindow> windows = new ArrayList<>();
        server.lockServer();
        try {
            for (Docked icon : docked) {
                windows.add(icon.window());
            }
        } finally {
            server.unlockServer();
        }
        return windows;
    }

    /** What the menu bar is showing, or null when it shows nothing. */
    public BufferedImage shownIcon() {
        return shown;
    }

    /** The tooltip the menu bar was last given, or null for none. */
    public String shownTooltip() {
        return shownTooltip;
    }

    // ------------------------------------------------------------------
    // The menu bar's half
    // ------------------------------------------------------------------

    private void poll() {
        while (running) {
            try {
                Thread.sleep(POLL_MILLIS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
            try {
                updateIcon();
            } catch (RuntimeException e) {
                // A poll that throws must not kill the thread: the next one may be fine, and a tray
                // that stops updating is worse than one that logged.
                JulLog.warn("could not read the docked icons", e);
            }
        }
    }

    /**
     * One poll, for a test that will not wait {@value #POLL_MILLIS} ms for the thread to do it.
     */
    void pollOnce() {
        updateIcon();
    }

    private void updateIcon() {
        BufferedImage image;
        String tooltip;
        server.lockServer();
        try {
            image = docked.isEmpty() ? null : compose();
            tooltip = docked.isEmpty() ? null : "X notification area (" + docked.size() + ")";
        } finally {
            server.unlockServer();
        }
        cn.oyzh.fx.xserver.display.DisplayBackend backend = server.backend();
        if (backend == null || !backend.hasTray()) {
            // No menu bar yet, or none on this machine: the selection is owned - a client that looks
            // for it finds it - and there is nowhere to put what it docks. A backend appears when a
            // window is opened, and the icons show up on the first poll after that.
            return;
        }
        if (!listenerAttached) {
            backend.setTrayClickListener(this::clicked);
            listenerAttached = true;
        }
        if (image == null) {
            if (shown != null) {
                shown = null;
                shownTooltip = null;
                backend.setTrayIcon(null, null);
            }
            return;
        }
        if (sameAsShown(image)) {
            return;
        }
        shown = image;
        shownTooltip = tooltip;
        if (JulLog.isDebugEnabled()) {
            JulLog.debug("the menu bar is given a {}x{} icon ({} icon(s) docked)",
                    image.getWidth(), image.getHeight(), docked.size());
        }
        backend.setTrayIcon(image, tooltip);
    }

    private boolean sameAsShown(BufferedImage image) {
        if (shown == null || shown.getWidth() != image.getWidth()
                || shown.getHeight() != image.getHeight()) {
            return false;
        }
        int[] before = shown.getRGB(0, 0, shown.getWidth(), shown.getHeight(), null, 0,
                shown.getWidth());
        int[] after = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0,
                image.getWidth());
        return Arrays.equals(before, after);
    }

    /** The icons as one image, left to right, each read out of the screen they are drawn on. */
    private BufferedImage compose() {
        List<BufferedImage> images = new ArrayList<>();
        int height = 1;
        int width = 0;
        for (Docked icon : docked) {
            BufferedImage image = capture(icon.window());
            if (image == null) {
                continue;
            }
            images.add(image);
            height = Math.max(height, image.getHeight());
            width += image.getWidth();
        }
        if (images.isEmpty()) {
            return null;
        }
        BufferedImage strip = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D graphics = strip.createGraphics();
        int x = 0;
        for (BufferedImage image : images) {
            graphics.drawImage(image, x, 0, null);
            x += image.getWidth();
        }
        graphics.dispose();
        return strip;
    }

    /** One icon window's pixels, or null when it has none to read. */
    private BufferedImage capture(XWindow window) {
        if (!window.isMapped() || window.width() <= 0 || window.height() <= 0) {
            return null;
        }
        Screen screen = window.screen();
        int x0 = Math.max(0, window.absoluteX());
        int y0 = Math.max(0, window.absoluteY());
        int width = Math.min(window.width(), screen.width() - x0);
        int height = Math.min(window.height(), screen.height() - y0);
        if (width <= 0 || height <= 0) {
            return null;
        }
        // An icon window is created with the ARGB visual this bridge publishes, so it keeps its own
        // pixels and they carry the alpha the client drew (B5): the icon is read from there, and its
        // transparent corners stay transparent so the menu bar shows a shape rather than a square. A
        // window with no store - a client that ignored the published visual - is read out of the
        // screen instead, which is what this always did, and reads as what is over it.
        //
        // **And the children count**: a real toolkit does not draw into the window it docked, it
        // docks a window of its own and paints the image into a child of it. Measured with
        // `java.awt.SystemTray` on the test host (2026-10-02): the docked window is "Content window"
        // and the image is drawn by its child, `sun-awt-X11-XCanvasPeer`. Reading only the window's
        // own pixels handed the menu bar that window's empty background - a blank square, which is
        // what "the tray icon is not right" turned out to be. `pixelFromInferiors` is the same read
        // `IncludeInferiors` gives a client: the deepest window that covers the point, in its own
        // alpha, which is exactly what the icon looks like.
        int[] pixels = new int[width * height];
        boolean keepsPixels = window.keepsOwnPixels();
        Framebuffer own = keepsPixels ? window.ownPixels() : null;
        Framebuffer framebuffer = keepsPixels ? null : screen.framebuffer();
        int originX = window.absoluteX();
        int originY = window.absoluteY();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                int localX = x0 + column - originX;
                int localY = y0 + row - originY;
                int fromChild = window.pixelFromInferiors(localX, localY);
                if (fromChild != XWindow.NO_CHILD_PIXEL) {
                    pixels[row * width + column] = fromChild;
                } else if (keepsPixels) {
                    pixels[row * width + column] = own.pixel(localX, localY);
                } else {
                    pixels[row * width + column] = framebuffer.pixel(x0 + column, y0 + row)
                            | 0xFF000000;
                }
            }
        }
        int scale = Math.max(width, height) > ICON_MAX ? ICON_MAX : 0;
        if (scale > 0) {
            int scaledWidth = Math.max(1, width * ICON_MAX / Math.max(width, height));
            int scaledHeight = Math.max(1, height * ICON_MAX / Math.max(width, height));
            BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            source.setRGB(0, 0, width, height, pixels, 0, width);
            BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight,
                    BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(source, 0, 0, scaledWidth, scaledHeight, null);
            graphics.dispose();
            return scaled;
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    /**
     * A click on the menu-bar icon, delivered to the icon window it landed on.
     *
     * <p>Synthetic, which is how every tray host does it: the icon's client is not the one the user
     * clicked, and the protocol has no other way to tell it. The coordinates are the icon window's
     * own, which is the space its client places its context menu in.
     */
    public void clicked(int x, int y, int button, int clickCount) {
        XWindow target = null;
        int localX = x;
        server.lockServer();
        try {
            boolean insideTheStrip = x >= 0 && x < stripWidth();
            for (Docked icon : docked) {
                int width = Math.max(1, icon.window().width());
                if (localX < width || !insideTheStrip) {
                    // A backend that knows where its icons are sends a position inside the strip the
                    // tray composed; one that does not - AWT reports a menu-bar click in *screen*
                    // coordinates, and a status item's own geometry is not something it exposes -
                    // sends something outside it, and a click on the one icon there is belongs to
                    // that icon wherever in it the pointer was. Measured: the platform does deliver
                    // the click (button 1 at 1060,12 for an icon at that place on the screen), which
                    // is what made the first version of this look like a platform that does not.
                    target = icon.window();
                    localX = insideTheStrip ? localX : width / 2;
                    break;
                }
                localX -= width;
            }
        } finally {
            server.unlockServer();
        }
        if (target == null || target.owner() == null) {
            return;
        }
        int iconX = Math.max(0, Math.min(localX, Math.max(0, target.width() - 1)));
        int iconY = Math.max(0, Math.min(y, Math.max(0, target.height() - 1)));
        server.lockServer();
        try {
            for (int i = 0; i < Math.max(1, clickCount); i++) {
                sendButton(target, button, true, iconX, iconY);
                sendButton(target, button, false, iconX, iconY);
            }
        } finally {
            server.unlockServer();
        }
    }

    private void sendButton(XWindow window, int button, boolean pressed, int x, int y) {
        int time = server.events().time();
        int root = window.screen().root().id();
        short rootX = (short) (window.absoluteX() + x);
        short rootY = (short) (window.absoluteY() + y);
        XEvent event;
        if (pressed) {
            event = com.github.moaxcp.x11.protocol.xproto.ButtonPressEvent.builder()
                    .firstEventOffset((byte) 0)
                    .sentEvent(true)
                    .sequenceNumber((short) 0)
                    .detail((byte) button)
                    .time(time)
                    .root(root)
                    .event(window.id())
                    .child(0)
                    .rootX(rootX)
                    .rootY(rootY)
                    .eventX((short) x)
                    .eventY((short) y)
                    // A click from the menu bar carries no modifiers: the channel it came down does
                    // not report any, and the button being pressed is what the state would say.
                    .state((short) 0)
                    .sameScreen(true)
                    .build();
        } else {
            event = com.github.moaxcp.x11.protocol.xproto.ButtonReleaseEvent.builder()
                    .firstEventOffset((byte) 0)
                    .sentEvent(true)
                    .sequenceNumber((short) 0)
                    .detail((byte) button)
                    .time(time)
                    .root(root)
                    .event(window.id())
                    .child(0)
                    .rootX(rootX)
                    .rootY(rootY)
                    .eventX((short) x)
                    .eventY((short) y)
                    .state((short) 0)
                    .sameScreen(true)
                    .build();
        }
        server.events().send(window.owner(), event);
    }
}
