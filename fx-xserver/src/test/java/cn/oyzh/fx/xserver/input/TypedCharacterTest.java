package cn.oyzh.fx.xserver.input;

import cn.oyzh.fx.xserver.input.Keymap;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Typing a character the host composed, which no key on the keyboard stands for.
 *
 * <p>A keycode says which key was pressed; the client turns it into a character with its own copy of
 * the keyboard mapping. That covers everything printed on a keyboard and nothing else - no keyboard
 * has a key per Chinese character - so text an input method composed arrives at the server as a
 * *character*, and the server has to make an X client see it. It does that the only way X allows:
 * claims a keycode no key carries, gives it the character as its keysym, tells the clients the
 * mapping changed, and presses it. This is the platform-independent half of input methods: every
 * toolkit on every host reports composed text the same way (JavaFX as {@code KEY_TYPED}, AWT as
 * {@code keyTyped}), and what happens after that is this server's.
 */
class TypedCharacterTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 71;

    private static final int KEY_PRESS = 2;
    private static final int KEY_RELEASE = 3;
    private static final int MAPPING_NOTIFY = 34;

    /** X's own way of naming a character that has no keysym of its own. */
    private static final int UNICODE_KEYSYM = 0x01000000;

    private XServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void aCharacterNoKeyCarriesReachesTheClient() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            watchKeys(client);

            server.inputFor(server.screen()).keyTyped('\u4f60');   // 你
            client.flush();

            List<byte[]> events = drain(client);
            assertNotNull(find(events, MAPPING_NOTIFY),
                    "a client caches the mapping, so it has to be told the mapping changed");
            byte[] press = find(events, KEY_PRESS);
            assertNotNull(press, "a composed character has to reach the client as a key press");
            int keycode = press[1] & 0xFF;
            assertEquals(UNICODE_KEYSYM | '\u4f60', client.keyboardMapping(keycode, 1)[0],
                    "the keycode carries the character, which is how the client reads it back");
            assertNotNull(find(events, KEY_RELEASE), "and the key comes up again");
        }
    }

    @Test
    void aCharacterTheKeyboardCarriesIsNotSentTwice() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            watchKeys(client);

            // "a" is on the keyboard: the press of that key has already been delivered, and the
            // client turns it into the character itself. Sending it again would type it twice.
            server.inputFor(server.screen()).keyTyped('a');
            client.flush();

            assertNull(find(drain(client), KEY_PRESS),
                    "the keycode path already carries this character");
        }
    }

    /**
     * Where the character is put, which is not a matter of taste.
     *
     * <p>Measured 2026-10-08 with a real toolkit on the other end: when the server claimed the
     * <em>highest</em> keycode, 255, for 中, a text field in a forwarded EasyShell received nothing at
     * all for it - the mapping answered the character, and the client still resolved the keycode to
     * nothing, because a keycode at the far end of the range is one the map's own tables can leave
     * without an entry. The same character typed by {@code xdotool}, which claims **keycode 8** (its
     * log line: {@code client mapping change: first=8 count=1 perKeycode=1 keysyms=[16797229]}),
     * arrived. So the claim is the lowest keycode nothing carries.
     */
    @Test
    void theCharacterGoesToTheLowestKeycodeNothingCarries() throws IOException {
        startServer();
        int synthetic = Keymap.syntheticKeycode();
        assertNotEquals(Keymap.MAX_KEYCODE & 0xFF, synthetic,
                "not the end of the range: a client could not resolve it (255 failed where 8 worked)");
        // The lowest free keycodes on these tables are 8 and 9; the first one a key really carries is
        // the digit row at 10. A choice that far up the range is the bug this pins.
        assertTrue(synthetic >= (Keymap.MIN_KEYCODE & 0xFF) && synthetic < 10,
                "the claim belongs at the bottom of the range, not at " + synthetic);
    }

    /**
     * The same character twice is two keystrokes - which it is not, if the server mistakes the
     * keycode it claimed for the last one for a key that was always there.
     */
    @Test
    void theSameCharacterCanBeTypedTwice() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            watchKeys(client);

            server.inputFor(server.screen()).keyTyped('\u4f60');
            client.flush();
            assertNotNull(find(drain(client), KEY_PRESS), "the first one");

            server.inputFor(server.screen()).keyTyped('\u4f60');
            client.flush();
            assertNotNull(find(drain(client), KEY_PRESS),
                    "and the second, which a character the keyboard really carries would not be");
        }
    }

    /**
     * Where two different characters go, which is *not* the same keycode - and that is a measurement.
     *
     * <p>The design used to be one keycode for every composed character, re-assigned each time ("a
     * mapping change per character would be a mapping change per keystroke"). Measured 2026-10-08
     * against a real client: a client resolves a keycode through the mapping it was *last* told, so
     * 你好 typed back to back on one keycode reached a forwarded text field as **好好** - with the two
     * keystrokes 25 ms apart and 150 ms apart alike. Each character now has a keycode of its own, and
     * the same character keeps the one it has.
     */
    @Test
    void eachComposedCharacterKeepsItsOwnKeycode() throws IOException {
        startServer();
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            watchKeys(client);

            server.inputFor(server.screen()).keyTyped('\u4f60');
            client.flush();
            byte[] first = find(drain(client), KEY_PRESS);
            assertNotNull(first, "the first character has to reach the client");

            server.inputFor(server.screen()).keyTyped('\u597d');   // 好
            client.flush();
            byte[] second = find(drain(client), KEY_PRESS);
            assertNotNull(second, "and so has the second");

            assertNotEquals(first[1] & 0xFF, second[1] & 0xFF,
                    "two different characters on one keycode is how 你好 arrives as 好好");
            assertEquals(UNICODE_KEYSYM | '\u4f60', client.keyboardMapping(first[1] & 0xFF, 1)[0],
                    "each keycode carries its own character");
            assertEquals(UNICODE_KEYSYM | '\u597d',
                    client.keyboardMapping(second[1] & 0xFF, 1)[0],
                    "each keycode carries its own character");

            server.inputFor(server.screen()).keyTyped('\u4f60');
            client.flush();
            byte[] again = find(drain(client), KEY_PRESS);
            assertNotNull(again, "a character typed twice is two keystrokes");
            assertEquals(first[1] & 0xFF, again[1] & 0xFF,
                    "and the second one comes off the keycode the first one already claimed");
        }
    }

    /**
     * A client that hears key presses, which is what a text field does: a window of its own, the
     * keyboard, and the focus - keys go to the focused window, and a client that never asks for the
     * focus is not told about any.
     */
    private int watchKeys(RawXClient client) throws IOException {
        int window = client.allocateId();
        client.createWindow(window, client.root(), 0, 0, 200, 200);
        client.selectInput(window, 0x3);            // KeyPressMask | KeyReleaseMask
        client.mapWindow(window);
        client.setInputFocus(window, 1);            // RevertToPointerRoot
        client.flush();
        // A round trip, so the server has certainly handled the focus before anything is typed:
        // requests are handled on the server's own thread, and the next line here runs regardless.
        client.queryInputFocus();
        return window;
    }

    /**
     * Everything the client is sent within a short window, collected rather than filtered.
     *
     * <p>Read one type at a time and the events that do not match are thrown away, so a test that is
     * waiting for the key press loses it while looking for the mapping change. A keystroke is several
     * events and the client sees all of them.
     */
    private static List<byte[]> drain(RawXClient client) throws IOException {
        List<byte[]> events = new java.util.ArrayList<>();
        client.readTimeoutMillis(400);
        try {
            // Until the client goes quiet, rather than a fixed number: one keystroke is a mapping
            // change, a press and a release, and a test that stops at two leaves the third in the
            // socket for the next one to trip over.
            for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
                events.add(event);
            }
        } finally {
            client.readTimeoutMillis(5000);
        }
        return events;
    }

    private static byte[] find(List<byte[]> events, int type) {
        for (byte[] event : events) {
            if ((event[0] & 0x7F) == type) {
                return event;
            }
        }
        return null;
    }

    private void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "400x300", "--backend", "headless", "--single-window", "--no-unix"}));
        server.start();
        server.startEventLoop();
    }
}
