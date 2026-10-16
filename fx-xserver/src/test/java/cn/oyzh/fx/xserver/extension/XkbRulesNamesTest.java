package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The root property that says which rules built the keyboard: {@code _XKB_RULES_NAMES}.
 *
 * <p>It is not decoration. `setxkbmap` reads it before it changes anything - to know which rules file
 * describes the keymap that is already loaded - and both `setxkbmap -print` and the property it writes
 * afterwards follow from that one answer. A reference writes it for itself at startup: measured on a
 * fresh Xvfb, before any client has run, as `"evdev", "pc105", "us", "", ""`, and raw as the
 * seventeen bytes {@code evdev\0pc105\0us\0\0\0}.
 *
 * <p>Without it ours fell back to whatever the client assumed - `"base"` and a keycodes component of
 * `xfree86` where a reference says `evdev` - which is how this was found (2026-09-28, comparing
 * `setxkbmap -print` between the two servers). The test runs in the **default one-screen-per-client
 * mode**, because that is where the property has to be published on every screen rather than once on
 * the master: a client asking its own root is the case that was wrong.
 */
class XkbRulesNamesTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 81;

    /** The five names, NUL separated, exactly as a reference's own bytes have them. */
    private static final String VALUE = "evdev\u0000pc105\u0000us\u0000\u0000\u0000";

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

    @Test
    void aClientSeesTheRulesNamesOnItsOwnRoot() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int atom = client.internAtom("_XKB_RULES_NAMES");
            byte[] reply = client.getProperty(client.root(), atom, 0, false);
            assertEquals(8, reply[1] & 0xFF, "a STRING property");
            assertEquals(31, RawXClient.readInt(reply, 8), "of type STRING");
            assertEquals(VALUE.length(), RawXClient.readInt(reply, 16), "seventeen bytes");
            assertEquals(VALUE, new String(reply, 32, VALUE.length(), StandardCharsets.ISO_8859_1),
                    "the five names a reference publishes, byte for byte");
        }
    }

    /** And on every screen, not only the first: a second client gets one of its own. */
    @Test
    void everyScreensRootHasIt() throws Exception {
        try (RawXClient first = RawXClient.connect(DISPLAY);
             RawXClient second = RawXClient.connect(DISPLAY)) {
            if (first.root() == second.root()) {
                return;                              // one shared screen: nothing more to check
            }
            int atom = second.internAtom("_XKB_RULES_NAMES");
            byte[] reply = second.getProperty(second.root(), atom, 0, false);
            assertEquals(VALUE, new String(reply, 32, VALUE.length(), StandardCharsets.ISO_8859_1),
                    "the second client's own screen carries it too");
        }
    }
}
