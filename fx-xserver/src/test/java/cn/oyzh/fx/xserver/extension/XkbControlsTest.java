package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.input.Keymap;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The keyboard's controls over XKB: how fast a held key repeats, whether it repeats at all, and which
 * keys do.
 *
 * <p>This is what A11 left open. The core protocol has a request for the per-key bitmap
 * ({@code ChangeKeyboardControl}) and none at all for the delay or the rate - those are XKB's, through
 * {@code XkbSetAutoRepeatRate}, which is a {@code SetControls} with one bit set. So a client that
 * wants a 200 ms delay could not have one before this was here, and a client that read the delay back
 * could not check it.
 *
 * <p><b>Both rails, and the reply layout is the protocol's own rather than derived.</b> A
 * {@code GetControls} reply is 92 bytes - the long one of the extension - and its offsets were read
 * off a reference server rather than from a header: changing the rate through {@code xset r rate}
 * moves the two bytes at 20 and 22, the enabled-controls word sits at 56, and the last thirty two
 * bytes are the same per-key bitmap {@code GetKeyboardControl} carries. The probe
 * ({@code XkbProbe}) prints both for both servers.
 *
 * <p><b>Three behaviours are measured against Xvfb rather than assumed</b>, and the code follows all
 * three: {@code changeControls}'s RepeatKeys bit is what makes the delay and the rate in the request
 * take; {@code affectEnabledControls}'s same bit turns the keyboard's repeat on or off and leaves the
 * per-key bitmap exactly as it was (measured: {@code 0x13a1} to {@code 0x13a0} and back); and the
 * {@code perKeyRepeat} array a client sends is <em>ignored</em> - measured three ways, all zeros, all
 * ones, and beside a rate change, and the reference's bitmap never moved. Per-key repeat stays the
 * core protocol's, where it has been since A11.
 */
class XkbControlsTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 91;

    private static final int GET_CONTROLS = 6;
    private static final int SET_CONTROLS = 7;

    /** {@code XkbUseCoreKbd}. */
    private static final int CORE_KEYBOARD = 0x0100;

    /** {@code XkbRepeatKeysMask}: the one control bit this server honours. */
    private static final int REPEAT_KEYS = 0x1;

    /** The reply's per-key bitmap, and the core reply's copy of the same table. */
    private static final int CONTROLS_BITMAP = 60;
    private static final int CORE_BITMAP = 20;

    /** Core {@code GetKeyboardControl}, the rail that has no extension number in it. */
    private static final int GET_KEYBOARD_CONTROL = 103;

    /** A key that produces a character, so it is one a stock server repeats. */
    private static final int KEY_A = 38;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--backend", "headless", "--no-unix"}));
        server.start();
        // The repeats are the display tick's work, so the tick has to be running for the behavioural
        // test below; the replies the others read do not need it.
        server.startEventLoop();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void theDelayAndTheRateAreReadableAndSettable() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XKEYBOARD");
            byte[] before = getControls(client, opcode);
            assertEquals(92, before.length, "the reply is the long one, and a short answer would"
                    + " leave the next reply read out of step");
            assertEquals(15, RawXClient.readInt(before, 4), "length: (92 - 32) / 4");
            assertEquals(660, shortAt(before, 20), "X's own default delay");
            assertEquals(40, shortAt(before, 22), "and rate - 25 a second, what `xset q` prints");
            assertTrue((RawXClient.readInt(before, 56) & REPEAT_KEYS) != 0,
                    "a keyboard repeats unless a client says otherwise");

            // XkbSetAutoRepeatRate as the wire sees it: changeControls' RepeatKeys bit is what makes
            // the delay and the rate in the request take, and affectEnabledControls is untouched.
            client.sendRequest(setControls(opcode, REPEAT_KEYS, 0, 0, 250, 30, bitmapOf(before)));

            byte[] after = getControls(client, opcode);
            assertEquals(250, shortAt(after, 20), "the delay a client set");
            assertEquals(30, shortAt(after, 22), "and the rate");
            assertTrue((RawXClient.readInt(after, 56) & REPEAT_KEYS) != 0,
                    "changing the rate is not changing whether repeat is on");

            // And a zero in either field means "leave it alone", which is what XkbSetAutoRepeatRate
            // sends when a caller passes 0 - so the reply must not have moved to 0.
            client.sendRequest(setControls(opcode, REPEAT_KEYS, 0, 0, 0, 0, bitmapOf(after)));
            byte[] kept = getControls(client, opcode);
            assertEquals(250, shortAt(kept, 20), "a delay of zero is not a delay of zero milliseconds");
            assertEquals(30, shortAt(kept, 22), "and neither is a rate of zero");
        }
    }

    /**
     * The per-key bitmap, on both rails at once.
     *
     * <p>This is A11's rule - what a client reads back is what the server does - extended to the keys
     * it excludes: a stock server does not repeat the modifiers, and this one does not either. The
     * bitmap is where that lives now rather than a rule applied at press time, so a client that puts
     * a key back in it gets it to repeat.
     */
    @Test
    void thePerKeyBitmapIsTheSameOnBothRails() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int xkb = client.queryExtension("XKEYBOARD");
            byte[] controls = getControls(client, xkb);
            byte[] core = getKeyboardControl(client);

            assertArrayEquals(java.util.Arrays.copyOfRange(controls, CONTROLS_BITMAP, 92),
                    java.util.Arrays.copyOfRange(core, CORE_BITMAP, 52),
                    "the extension's per-key bitmap and the core one are one table");

            assertTrue(bit(controls, KEY_A), "'a' repeats");
            // The modifier map decides, and these are the keycodes it names.
            for (int keycode : new int[] {37, 50, 62, 66, 77, 105, 108, 133, 134}) {
                assertNotEquals(0, Keymap.modifierMaskForKeycode(keycode),
                        "keycode " + keycode + " has to be a modifier for this to be the reason");
                assertTrue(!bit(controls, keycode),
                        "keycode " + keycode + " is a modifier and does not repeat");
            }
            for (int keycode = 0; keycode < Keymap.MIN_KEYCODE; keycode++) {
                assertTrue(!bit(controls, keycode),
                        "keycode " + keycode + " is below the keyboard's range and cannot repeat");
            }
        }
    }

    /**
     * The end-to-end one: a client turns the keyboard's repeat off through XKB and a held key really
     * does press once.
     *
     * <p>The reply above only says the number changed. What a user meets is the delivery, and the
     * two are separate code paths - the control bit is read when a key goes down, and the report is
     * read when a client asks. Turning it off and getting a repeat anyway is the failure this test is
     * for, and it is also the case a client that wants to type a single character cares about.
     */
    @Test
    void turningRepeatOffThroughXkbStopsTheRepeats() throws IOException {
        try (RawXClient client = focusedWindow()) {
            int xkb = client.queryExtension("XKEYBOARD");
            int xtest = client.queryExtension("XTEST");
            byte[] bitmap = bitmapOf(getControls(client, xkb));

            client.sendRequest(setControls(xkb, 0, REPEAT_KEYS, 0, 0, 0, bitmap));
            assertEquals(0, RawXClient.readInt(getControls(client, xkb), 56) & REPEAT_KEYS,
                    "repeat is off now");

            client.fakeKey(xtest, KEY_A, true);
            assertEvent(client, 2, "the press arrives");
            server.pollAutoRepeat(System.currentTimeMillis() + 5000);
            assertNoEvent(client, "RepeatKeys off is one press however long the key is held");
            client.fakeKey(xtest, KEY_A, false);
            assertEvent(client, 3, "the release arrives");

            // Back on, with the bitmap it had: the keys that repeated before repeat again, which is
            // why turning the control off must not wipe the table.
            client.sendRequest(setControls(xkb, 0, REPEAT_KEYS, REPEAT_KEYS, 0, 0, bitmap));
            assertEquals(REPEAT_KEYS, RawXClient.readInt(getControls(client, xkb), 56) & REPEAT_KEYS,
                    "repeat is on again");

            client.fakeKey(xtest, KEY_A, true);
            assertEvent(client, 2, "the press arrives");
            server.pollAutoRepeat(System.currentTimeMillis() + 5000);
            assertEvent(client, 2, "and a held key repeats again");
            assertTrue(bit(getControls(client, xkb), KEY_A), "with its bit still in the bitmap");
        }
    }

    // ------------------------------------------------------------------
    // The two requests, in bytes, because the layout is positional
    // ------------------------------------------------------------------

    private static byte[] getControls(RawXClient client, int opcode) throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_CONTROLS;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeShortAt(request, 4, CORE_KEYBOARD);
        client.sendRequest(request);
        return client.readReply();
    }

    /**
     * {@code XkbSetControls}, the whole hundred bytes of it.
     *
     * <p>Written from the field order the protocol declares, and the offsets are checked against a
     * live server rather than trusted: the first version was four bytes out, and the way it failed
     * was that a request which looked well formed changed nothing at all - which is also what a
     * correct request for a control the server does not implement looks like. What settled it was
     * sending the same request to Xvfb and watching the delay move.
     */
    private static byte[] setControls(int opcode, int changeControls, int affectEnabled,
                                      int enabled, int delay, int interval, byte[] perKeyRepeat) {
        byte[] request = new byte[100];
        request[0] = (byte) opcode;
        request[1] = (byte) SET_CONTROLS;
        RawXClient.writeShortAt(request, 2, request.length / 4);
        RawXClient.writeShortAt(request, 4, CORE_KEYBOARD);         // deviceSpec
        RawXClient.writeIntAt(request, 24, affectEnabled);          // affectEnabledControls
        RawXClient.writeIntAt(request, 28, enabled);                // enabledControls
        RawXClient.writeIntAt(request, 32, changeControls);         // changeControls
        RawXClient.writeShortAt(request, 36, delay);                // repeatDelay
        RawXClient.writeShortAt(request, 38, interval);             // repeatInterval
        System.arraycopy(perKeyRepeat, 0, request, 68, 32);
        return request;
    }

    private static byte[] getKeyboardControl(RawXClient client) throws IOException {
        byte[] request = new byte[4];
        request[0] = (byte) GET_KEYBOARD_CONTROL;
        RawXClient.writeShortAt(request, 2, 1);
        client.sendRequest(request);
        return client.readReply();
    }

    // ------------------------------------------------------------------
    // Reading the replies
    // ------------------------------------------------------------------

    /** The reply's own per-key bitmap, which is the last thirty two bytes. */
    private static byte[] bitmapOf(byte[] controls) {
        return java.util.Arrays.copyOfRange(controls, CONTROLS_BITMAP, 92);
    }

    private static boolean bit(byte[] controls, int keycode) {
        int index = CONTROLS_BITMAP + (keycode >> 3);
        return index < controls.length && (controls[index] & (1 << (keycode & 7))) != 0;
    }

    private static int shortAt(byte[] bytes, int offset) {
        return (short) ((bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8));
    }

    /** A client with a window that has the keyboard focus and hears key presses. */
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

    private static void assertNoEvent(RawXClient client, String message) throws IOException {
        byte[] event = client.readEvent();
        assertNull(event, message + " (got event type " + (event == null ? 0 : event[0] & 0xFF) + ")");
    }

    private static void assertEvent(RawXClient client, int type, String message) throws IOException {
        byte[] event = client.readEvent();
        assertNotNull(event, message + ": nothing arrived");
        assertEquals(type, event[0] & 0xFF, message);
    }
}
