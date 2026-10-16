package cn.oyzh.fx.xserver.tray;

import cn.oyzh.fx.xserver.tray.TrayBridge;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.CursorSprite;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The notification area: a client that docks a tray icon has it shown in the host's menu bar, and its
 * clicks come back (backlog C9).
 *
 * <p>The protocol is the freedesktop one, and a client is not asked to do anything special: it looks
 * for the owner of {@code _NET_SYSTEM_TRAY_S0}, sends {@code SYSTEM_TRAY_REQUEST_DOCK} with its icon
 * window, and from then on draws into that window. What these tests pin is the three things a client
 * would notice if they were missing - the selection is owned and says what it is, a docked icon
 * reaches the menu bar as the pixels the client drew, and a click on it arrives at the client's
 * window - plus the guard that a server with no menu bar serves no tray at all.
 *
 * <p>The backend is a recording one, so nothing here touches the real macOS menu bar: a test run
 * would otherwise put icons in it and take them away.
 */
class TrayBridgeTest {

    /** A display number nothing else in the suite uses. */
    private static final int DISPLAY = 68;

    private static final int ICON = 24;
    private static final int ICON_X = 16;
    private static final int ICON_Y = 12;
    private static final int RED = 0xFF0000;
    private static final int GREEN = 0x00FF00;

    private XServer server;
    private RawXClient client;
    private volatile RecordingWindow lastWindow;

    @BeforeEach
    void startServer() throws Exception {
        // --single-window: one screen, so the icon window is on the screen the master session shows
        // and the backend this test records is the one the tray will hand the icon to.
        // The options say a window is presented, because that is what decides whether this server
        // serves a tray at all; the factory replaces the backend with one a test can read, so no
        // real window is opened and nothing is put in the real menu bar.
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "javafx", "--no-unix", "--single-window"}));
        server.setBackendFactory(RecordingWindow::new);
        server.start();
        client = RawXClient.connect(DISPLAY);
    }

    @AfterEach
    void stopServer() throws Exception {
        if (client != null) {
            client.close();
        }
        if (server != null) {
            server.stop();
        }
    }

    /** Owns the selection and publishes the three properties a tray client reads before docking. */
    @Test
    void theTrayOwnsTheSelectionAndSaysWhatItIs() throws Exception {
        TrayBridge bridge = server.tray();
        assertNotNull(bridge, "the tray bridge did not start");

        int selection = client.internAtom("_NET_SYSTEM_TRAY_S0");
        int owner = client.getSelectionOwner(selection);
        assertTrue(bridge.ownsWindow(owner),
                "the notification area is owned by 0x" + Integer.toHexString(owner)
                        + ", which is not the tray bridge's window");

        int opcodeAtom = client.internAtom("_NET_SYSTEM_TRAY_OPCODE");
        byte[] opcode = client.getProperty(owner, opcodeAtom, 0, false);
        assertEquals(opcodeAtom, RawXClient.readInt(opcode, 32),
                "the opcode property's value is the atom a dock request's type field must carry");

        int orientation = client.internAtom("_NET_SYSTEM_TRAY_ORIENTATION");
        byte[] orientationValue = client.getProperty(owner, orientation, 0, false);
        assertEquals(0, RawXClient.readInt(orientationValue, 32),
                "a menu bar lays its icons out horizontally");

        int visual = client.internAtom("_NET_SYSTEM_TRAY_VISUAL");
        byte[] visualValue = client.getProperty(owner, visual, 0, false);
        assertTrue(RawXClient.readInt(visualValue, 32) != 0, "the icons' visual id is published");
    }

    /** A docked icon is read out of the screen and handed to the menu bar, pixel for pixel. */
    @Test
    void aDockedIconReachesTheMenuBarAsTheClientsOwnPixels() throws Exception {
        RecordingWindow backend = awaitBackend();
        int icon = anIconWindow();

        dock(icon);
        server.tray().pollOnce();

        BufferedImage shown = backend.icon;
        assertNotNull(shown, "the menu bar was given no icon");
        assertEquals(ICON, shown.getWidth(), "the icon's own width");
        assertEquals(ICON, shown.getHeight(), "and its own height");
        // The middle of the icon is what the client drew there - read out of the framebuffer the
        // client is drawing into, not out of its window object.
        assertEquals(RED, shown.getRGB(ICON / 2, ICON / 2) & 0xFFFFFF,
                "the icon shows what the client drew");
    }

    /** A click on the menu-bar icon arrives at the client's window, as a press and a release. */
    @Test
    void anIconDrawnByAChildWindowReachesTheMenuBarToo() throws Exception {
        // This is how a real toolkit does it, and it is why the tray "looked wrong" for every Java
        // application: measured with `java.awt.SystemTray` on the test host (2026-10-02), the docked
        // window is "Content window" and the image is drawn into its child, a
        // `sun-awt-X11-XCanvasPeer`. Reading the icon window's own pixels only handed the menu bar
        // that window's empty background - a blank square.
        RecordingWindow backend = awaitBackend();
        int icon = client.allocateId();
        client.createWindow(icon, client.root(), ICON_X, ICON_Y, ICON, ICON, GREEN);
        client.mapWindow(icon);
        client.queryInputFocus();
        dock(icon);

        // Drawn after the dock, because docking reparents the window - which unmaps and maps it and
        // paints the background again, as it does on a reference server.
        int canvas = client.allocateId();
        client.createWindow(canvas, icon, 0, 0, ICON, ICON, 0);
        client.mapWindow(canvas);
        int gc = client.createGC(client.allocateId(), canvas, RED);
        client.fillRectangle(canvas, gc, 0, 0, ICON, ICON);
        client.queryInputFocus();
        server.tray().pollOnce();

        BufferedImage shown = backend.icon;
        assertNotNull(shown, "the menu bar was given no icon");
        assertEquals(RED, shown.getRGB(ICON / 2, ICON / 2) & 0xFFFFFF,
                "the icon shows what the client drew in its child, not the window's own background");
    }

    /** A click on the menu-bar icon arrives at the client's window, as a press and a release. */
    @Test
    void aClickOnTheMenuBarIconReachesTheIconWindow() throws Exception {
        int icon = anIconWindow();
        dock(icon);
        client.queryInputFocus();                   // the dock has been processed after this

        server.tray().clicked(3, 4, 1, 1);

        byte[] press = client.readEvent(4);         // ButtonPress
        assertNotNull(press, "the icon's client was not told about the click");
        assertEquals(icon, RawXClient.readInt(press, 12), "the event names the icon window");
        assertEquals(3, RawXClient.readShort(press, 24), "at the place inside the icon it was clicked");
        assertEquals(4, RawXClient.readShort(press, 26), "at the place inside the icon it was clicked");
        assertEquals(1, press[1] & 0xFF, "button 1");
        byte[] release = client.readEvent(5);       // ButtonRelease
        assertNotNull(release, "and the button coming up");
    }

    /** An icon goes away with its window: the menu bar is told, and the client is not asked to. */
    @Test
    void anIconGoesAwayWithItsWindow() throws Exception {
        RecordingWindow backend = awaitBackend();
        int icon = anIconWindow();
        dock(icon);
        server.tray().pollOnce();
        assertNotNull(backend.icon, "the icon was never shown");
        assertEquals(1, server.tray().icons().size(), "one icon is docked");

        client.destroyWindow(icon);
        client.queryInputFocus();                   // the destroy has been processed after this

        assertEquals(0, server.tray().icons().size(), "the icon is no longer docked");
        server.tray().pollOnce();
        assertNull(backend.icon, "the menu bar still shows the icon of a window that is gone");
    }

    /** A server with no menu bar serves no tray: a VNC screen has nowhere to put an icon. */
    @Test
    void aServerWithNoMenuBarHasNoTray() throws Exception {
        XServer withoutTray = new XServer(ServerOptions.parse(new String[]{"--display", ":67",
                "--backend", "headless", "--no-unix"}));
        withoutTray.setBackendFactory(HeadlessWindow::new);
        withoutTray.start();
        try {
            assertNull(withoutTray.tray(), "a backend with no tray started one anyway");
        } finally {
            withoutTray.stop();
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** An icon window with something drawn in it, mapped and ready to dock. */
    private int anIconWindow() throws Exception {
        awaitBackend();
        int icon = client.allocateId();
        client.createWindow(icon, client.root(), ICON_X, ICON_Y, ICON, ICON, RED);
        client.mapWindow(icon);
        // The window's background is painted at the map, so a round trip is all it takes for the
        // pixels the tray is going to read to be there.
        client.queryInputFocus();
        return icon;
    }

    /**
     * An icon whose window has an alpha channel keeps its transparent corners (B5, C9's leftover).
     *
     * <p>The tray publishes `_NET_SYSTEM_TRAY_VISUAL`, and a client creates its icon window with it -
     * so publishing the root's 24 bit visual is what made every icon a square with a light background
     * instead of a cut-out shape. It publishes the 32 bit ARGB visual now, and an icon window made
     * with it keeps its own pixels, so what reaches the menu bar is the client's shape.
     */
    @Test
    void anIconWithAnAlphaVisualKeepsItsTransparentCorners() throws Exception {
        RecordingWindow backend = awaitBackend();
        int icon = client.allocateId();
        client.createWindowWithVisual(icon, client.root(), ICON_X, ICON_Y, ICON, ICON, 32,
                server.screen().visuals().stream()
                        .filter(cn.oyzh.fx.xserver.display.VisualInfo::hasAlpha)
                        .findFirst().orElseThrow().id(), 0x00000000);
        client.mapWindow(icon);
        client.queryInputFocus();
        dock(icon);

        // The disc is drawn *after* the dock, because docking reparents the window and a reparent is
        // an unmap and a map - which paints the background again, as it does on a reference server:
        // a reparented window loses what it had drawn. A client's own icon is drawn on request and
        // stays drawn, which is what this reads.
        int gc = client.createGC(client.allocateId(), icon, 0xFF000000 | RED);
        client.fillRectangle(icon, gc, ICON / 4, ICON / 4, ICON / 2, ICON / 2);
        client.queryInputFocus();

        server.tray().pollOnce();

        BufferedImage shown = backend.icon;
        assertNotNull(shown, "the menu bar was given no icon");
        assertEquals(0, shown.getRGB(0, 0) >>> 24,
                "the icon's transparent corner is still transparent, which is what the ARGB visual"
                        + " it was created with is for");
        assertEquals(RED, shown.getRGB(ICON / 2, ICON / 2) & 0xFFFFFF,
                "and the disc the client drew is its colour");
    }

    /** Asks the tray to display a window, the way a client does: a ClientMessage to the owner. */
    private void dock(int window) throws Exception {
        awaitBackend();
        int selection = client.internAtom("_NET_SYSTEM_TRAY_S0");
        int owner = client.getSelectionOwner(selection);
        int opcodeAtom = client.internAtom("_NET_SYSTEM_TRAY_OPCODE");
        // data[0] is the timestamp, data[1] the opcode and data[2] the window: the shape Java's
        // tray peer sends, and the one a real dock request has to look like.
        client.sendClientMessage(owner, opcodeAtom, 0, 0, window, 0, 0);
        // A round trip, so the dock has been processed before the caller looks at the tray: the
        // message is a request like any other and is handled on the client's own thread.
        client.queryInputFocus();
    }

    private RecordingWindow awaitBackend() throws Exception {
        long deadline = System.currentTimeMillis() + 4000;
        while (System.currentTimeMillis() < deadline) {
            RecordingWindow window = lastWindow;
            if (window != null && window.started) {
                return window;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("the backend never started");
    }

    /** A backend with a menu bar, which records what it was asked to show in it. */
    private final class RecordingWindow implements DisplayBackend {

        volatile BufferedImage icon;
        volatile String tooltip;
        volatile boolean started;

        RecordingWindow() {
            lastWindow = this;
        }

        @Override
        public String name() {
            return "recording";
        }

        @Override
        public boolean hasTray() {
            return true;
        }

        @Override
        public void setTrayIcon(BufferedImage image, String tooltip) {
            this.icon = image;
            this.tooltip = tooltip;
        }

        @Override
        public void start(Screen screen, int displayNumber, InputSink input) {
            started = true;
        }

        @Override
        public void present(Screen screen, Rectangle damage, CursorSprite cursor) {
            // Nothing to show: the tests read the framebuffer directly.
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    }

    /** A backend with no menu bar at all, which is what a headless or VNC server has. */
    private static final class HeadlessWindow implements DisplayBackend {

        @Override
        public String name() {
            return "headless-test";
        }

        @Override
        public void start(Screen screen, int displayNumber, InputSink input) {
        }

        @Override
        public void present(Screen screen, Rectangle damage, CursorSprite cursor) {
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean isClosed() {
            return false;
        }
    }
}
