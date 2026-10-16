package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The hints that tell a client there is a window manager here to ask: {@code _NET_SUPPORTED} on the
 * root, and the {@code _NET_SUPPORTING_WM_CHECK} window that a client reads to tell a live manager
 * from a stale property.
 *
 * <p>**A state is not refused when it is missing, it is never asked for.** GDK's
 * {@code gdk_x11_window_set_fullscreen} returns without sending anything unless the root's
 * {@code _NET_SUPPORTED} lists {@code _NET_WM_STATE_FULLSCREEN}, and AWT asks the root for
 * {@code _NET_SUPPORTING_WM_CHECK} before it will use EWMH at all. Measured 2026-10-13 on the real
 * machine: {@code eog} forwarded to display :5, F11 pressed, nothing happened and the log showed no
 * request at all. What the client would have sent is a {@code _NET_WM_STATE} {@code ClientMessage},
 * and the server already answers it - see {@code AtomHandlers.applyWindowStateMessage}.
 *
 * <p>The test runs in the **default one-screen-per-client mode**, because a client asks its own root
 * rather than the master's: publishing it once on the master is what the same mistake looked like for
 * {@code _XKB_RULES_NAMES} (see {@link XkbRulesNamesTest}).
 */
class WindowManagerHintsTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 82;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    /** The state a full screen request names has to be one the display says it honours. */
    @Test
    void aClientSeesFullScreenAdvertisedOnItsOwnRoot() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int atom = client.internAtom("_NET_SUPPORTED");
            byte[] reply = client.getProperty(client.root(), atom, 0, false);
            assertEquals(4, RawXClient.readInt(reply, 8), "of type ATOM");
            int count = RawXClient.readInt(reply, 16);
            assertTrue(count > 0, "a client checks the list, so it has to be there");
            int fullScreen = client.internAtom("_NET_WM_STATE_FULLSCREEN");
            boolean found = false;
            for (int i = 0; i < count; i++) {
                found |= RawXClient.readInt(reply, 32 + i * 4) == fullScreen;
            }
            assertTrue(found, "_NET_WM_STATE_FULLSCREEN is what a client looks for before asking");
        }
    }

    /** The check window is the root's answer that points at a window that points back at itself. */
    @Test
    void theCheckWindowPointsAtItself() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int check = client.internAtom("_NET_SUPPORTING_WM_CHECK");
            int window = RawXClient.readInt(client.getProperty(client.root(), check, 0, false), 32);
            assertNotEquals(0, window, "the root names a window of the manager's own");
            assertEquals(window,
                    RawXClient.readInt(client.getProperty(window, check, 0, false), 32),
                    "and that window names itself, which is how a stale property is told from a live one");
        }
    }

    /** And on every screen, not only the first: a second client gets one of its own. */
    @Test
    void everyScreensRootHasThem() throws Exception {
        try (RawXClient first = RawXClient.connect(DISPLAY);
             RawXClient second = RawXClient.connect(DISPLAY)) {
            if (first.root() == second.root()) {
                return;                              // one shared screen: nothing more to check
            }
            int supported = second.internAtom("_NET_SUPPORTED");
            int count = RawXClient.readInt(
                    second.getProperty(second.root(), supported, 0, false), 16);
            assertTrue(count > 0, "the second client's own screen advertises it too");
            int check = second.internAtom("_NET_SUPPORTING_WM_CHECK");
            assertNotEquals(0,
                    RawXClient.readInt(second.getProperty(second.root(), check, 0, false), 32),
                    "and names its own check window");
        }
    }
}