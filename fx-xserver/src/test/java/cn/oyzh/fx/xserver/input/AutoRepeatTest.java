package cn.oyzh.fx.xserver.input;

import cn.oyzh.fx.xserver.input.InputState;
import cn.oyzh.fx.xserver.input.Keymap;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The repeats a held key produces.
 *
 * <p>A real server generates them: a client is told the key went down once, and the run of
 * characters that follows is the server's work. Without it a held backspace deletes one character,
 * which for a terminal server is the difference between usable and not - and the reply to
 * {@code GetKeyboardControl} claiming every key repeats while nothing repeats is the kind of answer
 * that makes a client stop looking.
 *
 * <p>The repeats come from the display tick, so the test drives that call directly with the time it
 * wants: {@code XServer.pollAutoRepeat} is exactly what the tick runs, and taking the time as an
 * argument is what makes a 660 millisecond delay testable in a millisecond.
 */
class AutoRepeatTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 88;

    /** A key that produces a character, so it is one a stock server repeats: 'a'. */
    private static final int KEY_A = 38;

    /** And a modifier, which does not repeat however long it is held. */
    private static final int KEY_SHIFT = 50;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        // The repeats are the display tick's work, so this test needs the tick: without the loop the
        // server only answers requests and nothing is delivered on its own.
        server.startEventLoop();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void aHeldKeyRepeatsAfterTheDelayAndThenOnTheInterval() throws IOException {
        try (RawXClient client = focusedWindow()) {
            int xtest = client.queryExtension("XTEST");
            long now = System.currentTimeMillis();
            client.fakeKey(xtest, KEY_A, true);
            assertEvent(client, 2, "the press itself arrives once");

            // Nothing until the delay is up.
            server.pollAutoRepeat(now + 100);
            assertNoEvent(client, "a key held for 100 ms has not started repeating");

            server.pollAutoRepeat(now + 700);
            assertEvent(client, 2, "and then it repeats");
            server.pollAutoRepeat(now + 740);
            assertEvent(client, 2, "once per interval after that");

            // The key coming up stops it: a release is the only thing a client has to do.
            client.fakeKey(xtest, KEY_A, false);
            assertEvent(client, 3, "the release arrives");
            server.pollAutoRepeat(now + 5000);
            assertNoEvent(client, "a released key does not repeat");

            // And a key pressed again starts a fresh delay rather than inheriting the old one.
            long again = System.currentTimeMillis();
            client.fakeKey(xtest, KEY_A, true);
            assertEvent(client, 2, "the press arrives");
            server.pollAutoRepeat(again + 100);
            assertNoEvent(client, "the delay starts again with the new press");
            server.pollAutoRepeat(again + 700);
            assertEvent(client, 2, "and it repeats after the new delay");
        }
    }

    @Test
    void aModifierDoesNotRepeat() throws IOException {
        try (RawXClient client = focusedWindow()) {
            int xtest = client.queryExtension("XTEST");
            long now = System.currentTimeMillis();
            client.fakeKey(xtest, KEY_SHIFT, true);
            assertEvent(client, 2, "the press arrives");

            server.pollAutoRepeat(now + 5000);
            assertNoEvent(client, "a held Shift is not a run of presses");
        }
    }

    @Test
    void aKeyThatWasTurnedOffDoesNotRepeat() throws IOException {
        try (RawXClient client = focusedWindow()) {
            int xtest = client.queryExtension("XTEST");
            client.setAutoRepeat(KEY_A, false);
            long now = System.currentTimeMillis();
            client.fakeKey(xtest, KEY_A, true);
            assertEquals(2, client.readEvent()[0] & 0xFF, "the press still arrives");

            server.pollAutoRepeat(now + 5000);
            assertNoEvent(client, "a client that turned repeat off for a key gets one press");

            // Turned back on, it repeats again - so the flag is what decides, not the keycode.
            client.setAutoRepeat(KEY_A, true);
            client.fakeKey(xtest, KEY_A, false);
            assertEvent(client, 3, "the release arrives");
            long back = System.currentTimeMillis();
            client.fakeKey(xtest, KEY_A, true);
            assertEvent(client, 2, "and the press again");
            server.pollAutoRepeat(back + 700);
            assertEvent(client, 2, "repeat is back on for that key");
        }
    }

    @Test
    void everyKeyAtOnceIsTheSameAsNamingNone() throws IOException {
        try (RawXClient client = focusedWindow()) {
            int xtest = client.queryExtension("XTEST");
            client.setAutoRepeat(-1, false);
            long now = System.currentTimeMillis();
            client.fakeKey(xtest, KEY_A, true);
            assertEvent(client, 2, "the press arrives");
            server.pollAutoRepeat(now + 5000);
            assertNoEvent(client, "no keycode at all means every key");
            client.setAutoRepeat(-1, true);
        }
    }

    @Test
    void theDisplayTickIsWhatDeliversThem() throws IOException {
        // No call into the server: the repeats have to arrive on their own, from the tick, within
        // the delay - which is what a client sees and what fails outright without the feature.
        try (RawXClient client = focusedWindow()) {
            int xtest = client.queryExtension("XTEST");
            client.fakeKey(xtest, KEY_A, true);
            assertEvent(client, 2, "the press arrives");

            int repeats = 0;
            long deadline = System.currentTimeMillis() + 1500;
            while (System.currentTimeMillis() < deadline) {
                byte[] event = client.readEvent();      // null when nothing arrives in time
                if (event != null && (event[0] & 0xFF) == 2) {
                    repeats++;
                }
            }
            client.fakeKey(xtest, KEY_A, false);
            // 660 ms of delay and 40 ms between repeats leaves room for about twenty in the second
            // and a half; five is the floor a stalled test machine still manages.
            assertTrue(repeats >= 5, "a key held for a second and a half repeats (saw " + repeats + ")");
        }
    }

    /**
     * A client with a mapped window that has the keyboard focus, and that hears key presses - the
     * three things a repeat has to have to be observable.
     */
    private static RawXClient focusedWindow() throws IOException {
        RawXClient client = RawXClient.connect(DISPLAY);
        int window = client.allocateId();
        client.createWindow(window, client.root(), 0, 0, 200, 200);
        client.selectInput(window, 0x03);            // KeyPressMask | KeyReleaseMask
        client.mapWindow(window);
        client.setInputFocus(window, 1);             // RevertToPointerRoot
        client.readTimeoutMillis(250);
        return client;
    }

    /** Nothing arrived within the socket's timeout, which is the assertion. */
    private static void assertNoEvent(RawXClient client, String message) throws IOException {
        byte[] event = client.readEvent();
        assertNull(event, message + " (got event type " + (event == null ? 0 : event[0] & 0xFF) + ")");
    }

    /** One event of the wanted type, which is what a repeat has to arrive as. */
    private static void assertEvent(RawXClient client, int type, String message) throws IOException {
        byte[] event = client.readEvent();
        assertNotNull(event, message + ": nothing arrived");
        assertEquals(type, event[0] & 0xFF, message);
    }

    /**
     * The per-key repeat map follows the layout, and a client's own bitmap wins over it.
     *
     * <p>Both halves were wrong before they were measured. The map was a copy taken when the
     * <em>server</em> started, so a client that loaded a layout by name left it describing the
     * keyboard of whichever layout ran first - {@code xset q}'s "auto repeating keys" block then
     * differed from a reference's by exactly the keys the two layouts disagree about (2026-09-28).
     * The rule itself is "every key except the modifiers of the keymap in force", which is what a
     * reference's own bitmap holds; a client that sets the bitmap still takes over from it, which is
     * what a reference does too.
     */
    @Test
    void theRepeatMapFollowsTheLayout() throws Exception {
        InputState input = server.input();
        try {
            Keymap.useBuiltinLayout();
            assertTrue(input.autoRepeatsOn(92), "KP_Enter repeats in the built-in keymap");
            assertFalse(input.autoRepeatsOn(78), "Scroll_Lock is its Mod5, so it does not");

            assertTrue(Keymap.useLayout("de"), "the captured German layout");
            assertFalse(input.autoRepeatsOn(92), "AltGr is a modifier once the evdev rows are in force");
            assertTrue(input.autoRepeatsOn(78), "and Scroll_Lock is an ordinary key again");
        } finally {
            Keymap.useBuiltinLayout();
        }

        // A fresh state, so this does not leak into the tests that hold keys: the client's own
        // bitmap is what a reference lets it set, and the keys it does not name keep following the
        // keymap.
        InputState own = new InputState(server, server.screen());
        byte[] onlyA = new byte[32];
        onlyA[KEY_A >> 3] = (byte) (1 << (KEY_A & 7));
        own.setAutoRepeatBitmap(onlyA);
        assertTrue(own.autoRepeatsOn(KEY_A), "the key the client named repeats");
        assertFalse(own.autoRepeatsOn(24), "and one it did not, does not");
        assertFalse(own.autoRepeatsOn(KEY_SHIFT), "not even a modifier it left out");
    }
}
