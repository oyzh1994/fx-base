package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.input.Keymap;
import cn.oyzh.fx.xserver.input.Keysyms;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * XKEYBOARD - the extension that exists because its absence is not a fallback but a crash.
 *
 * <p>{@code xdotool} builds its character map from {@code XkbGetMap} and does not check the answer
 * for null, so a server without the extension makes it dereference one in its own startup, before
 * any subcommand has sent a request. That was measured with a backtrace (A4); what this test pins is
 * the part a Java client can see, and the one property of the reply that made the difference between
 * Xlib parsing it and Xlib returning null.
 *
 * <p><b>The map has to have at least the four mandatory types.</b> That is not a style choice: with
 * three (one-level, two-level and alphabetic - everything this server's keymap actually uses) Xlib's
 * {@code XkbGetMap} answers null, which xdotool cannot tell from the extension being missing, so the
 * crash is identical. It was found by bisecting: the same reply with Xvfb's own types parsed, the
 * types section being byte-for-byte the same in the two; adding the keypad type made it parse. Every
 * assertion about the map below is written against a reply that Xlib accepted.
 */
class XkbTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 65;

    private static final int USE_EXTENSION = 0;
    private static final int SELECT_EVENTS = 1;
    private static final int GET_STATE = 4;
    private static final int LATCH_LOCK_STATE = 5;
    private static final int GET_MAP = 8;
    private static final int SET_MAP = 9;
    private static final int GET_NAMES = 17;
    private static final int PER_CLIENT_FLAGS = 21;
    private static final int GET_KBD_BY_NAME = 23;

    /** {@code XkbAllNamesMask}: everything a client can ask to have named. */
    private static final int ALL_NAMES = 0x3FFF;

    /**
     * The {@code GetKbdByName} a real {@code setxkbmap -layout de} sends, captured with
     * {@code tools/tap.py} - the request's component strings are the rules file's own output and not
     * the word "de", so the layout is one of them and which one was measured: this is the sixth.
     */
    private static final String GET_KBD_BY_NAME_DE =
            "15000001bf00ff0001420017786672656538362b616c69617365732871776572747a2908636f6d706c"
                    + "65746508636f6d706c6574651170632b64652b696e6574287063313035290970632870633130352900";

    /** {@code XkbUseCoreKbd}. */
    private static final int CORE_KEYBOARD = 0x0100;

    /** {@code XkbKeyTypesMask | XkbKeySymsMask | XkbKeyModifierMapMask}, what xdotool asks for. */
    private static final int ALL_CLIENT_INFO = 0x7;

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "600x400", "--backend", "headless", "--no-unix"}));
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void itIsOfferedAndAgreesTheVersionTheClientSpeaks() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XKEYBOARD");
            assertNotEquals(0, opcode, "the whole point is that a client finds it");

            byte[] reply = useExtension(client, opcode);
            assertEquals(1, reply[1] & 0xFF, "supported: Xlib's own helpers stop here if it is zero");
            assertEquals(1, RawXClient.readShort(reply, 8), "the major version");
        }
    }

    @Test
    void itsMapCarriesTheFourMandatoryTypes() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            byte[] map = getMap(client, client.queryExtension("XKEYBOARD"), ALL_CLIENT_INFO);
            assertEquals(ALL_CLIENT_INFO, RawXClient.readShort(map, 12), "present");
            int types = map[15] & 0xFF;
            // This is the assertion the item turns on: three types is not a smaller map, it is no
            // map at all - Xlib rejects the reply whole and XkbGetMap returns null.
            assertTrue(types >= 4,
                    "XKB's four required types (one level, two level, alphabetic, keypad) have to be"
                            + " there; a reply with fewer is refused by Xlib, not trimmed by it, and"
                            + " the client then faults on a null keymap - got " + types);
            assertEquals(8, map[10] & 0xFF, "minKeyCode");
            assertEquals(Keymap.MAX_KEYCODE, map[11] & 0xFF, "maxKeyCode");
        }
    }

    @Test
    void itsMapSaysWhatEachKeycodeTypes() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            byte[] map = getMap(client, client.queryExtension("XKEYBOARD"), ALL_CLIENT_INFO);
            // 'a' is two levels and a letter; the keysym array is the server's own, so what XKB
            // answers and what GetKeyboardMapping answers cannot disagree.
            assertArrayEquals(new int[] {'a', 'A'}, keysymsOf(map, Keymap.keycodeForLetter('a')));
            assertArrayEquals(new int[] {Keysyms.ESCAPE}, keysymsOf(map, 9));
        }
    }

    @Test
    void itsMapSaysWhichKeycodesAreModifiers() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            byte[] map = getMap(client, client.queryExtension("XKEYBOARD"), ALL_CLIENT_INFO);
            int entries = map[33] & 0xFF;
            boolean leftShift = false;
            int at = arraysStart(map);
            for (int i = 0; i < entries; i++) {
                if ((map[at + i * 2] & 0xFF) == 50) {
                    leftShift = (map[at + i * 2 + 1] & 0xFF) == 1;      // ShiftMask
                }
            }
            assertTrue(leftShift, "the modifier map is a list of keycode/mods pairs, and Shift_L is"
                    + " the first one every client looks for");
        }
    }

    @Test
    void theOneWayRequestsAreAcceptedAndTheOnesWithRepliesAnswer() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XKEYBOARD");
            assertEquals(3, state(client, opcode)[1] & 0xFF, "the device the map names");

            // SelectEvents and LatchLockState answer with nothing at all - there is no reply to wait
            // for, and a refusal would be an error instead. So the check is the request *after*
            // them: an error would arrive first, and readReply throws rather than answering.
            client.sendRequest(oneWay(opcode, SELECT_EVENTS));
            client.sendRequest(oneWay(opcode, LATCH_LOCK_STATE));
            assertEquals(1, getMapBytes(client, opcode)[0] & 0xFF, "a reply, and not a refusal");

            // PerClientFlags is the one JavaFX actually sends as EasyShell starts. This server
            // supports none of the five flags, and says so in a reply - being silent about it made
            // the client read a protocol error instead.
            byte[] flags = client.extensionRequest(opcode, PER_CLIENT_FLAGS, 7);
            assertEquals(1, flags[0] & 0xFF, "a reply");
            assertEquals(0, RawXClient.readInt(flags, 8), "no per-client flags are supported here");
        }
    }

    @Test
    void aClientCanSetTheKeymapThroughXkbAndBothRailsAgree() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XKEYBOARD");
            int keycode = Keymap.keycodeForLetter('a');
            int[] previous = Keymap.keysymsFor(keycode).clone();
            try {
                client.sendRequest(setMap(opcode, keycode, new int[] {'q', 'Q'}));

                // Both rails read one table, which is the property this extension is built on: what
                // a client sets through XKB is what the core GetKeyboardMapping reports, and what
                // the next client's XkbGetMap will carry.
                System.out.println("DEBUG stored kc38 = " + java.util.Arrays.toString(
                        Keymap.keysymsFor(keycode)) + "  via-request=" + java.util.Arrays.toString(
                        keysymsOf(getMap(client, opcode, ALL_CLIENT_INFO), keycode)));
                assertArrayEquals(new int[] {'q', 'Q'},
                        keysymsOf(getMap(client, opcode, ALL_CLIENT_INFO), keycode));
                // And the one table both rails read holds it, which is the property that makes the
                // core rail follow without anything being written for it: the core reply itself is
                // checked live instead - `xmodmap -pke` and `xkbcomp` against a running server, where
                // a German keymap makes keycode 52 come back `y Y` on both (backlog B1).
                assertArrayEquals(new int[] {'q', 'Q'}, Keymap.keysymsFor(keycode));
            } finally {
                // Keymap is the server's static table, so a test that changes it has to put it back.
                Keymap.assign(keycode, previous);
            }
        }
    }

    /**
     * A {@code SetMap} carrying one keycode's keysyms, in the layout the protocol uses: a
     * thirty-five byte header, then one sym map with its keysyms after it. Written here rather than
     * built with the generated writer because that writer is the same generator as the server's
     * reader, and a mistake in it would be invisible; the wire format is pinned instead by the live
     * check in the backlog, where {@code xkbcomp} loads a real German keymap through this server and
     * keycode 52 comes back as {@code y Y} on both rails.
     */
    private static byte[] setMap(int opcode, int keycode, int[] keysyms) {
        // Thirty-five bytes the server reads after the opcode, and the opcode itself.
        int header = 36;
        int body = 8 + keysyms.length * 4;
        byte[] request = new byte[((header + body + 3) / 4) * 4];
        request[0] = (byte) opcode;
        request[1] = (byte) SET_MAP;
        RawXClient.writeShortAt(request, 2, request.length / 4);
        RawXClient.writeShortAt(request, 4, CORE_KEYBOARD);         // deviceSpec
        RawXClient.writeShortAt(request, 6, 2);                     // present: key syms
        RawXClient.writeShortAt(request, 8, 0);                     // flags
        request[10] = (byte) Keymap.MIN_KEYCODE;                    // minKeyCode
        request[11] = (byte) Keymap.MAX_KEYCODE;                    // maxKeyCode
        request[14] = (byte) keycode;                               // firstKeySym
        request[15] = 1;                                            // nKeySyms
        RawXClient.writeShortAt(request, 16, keysyms.length);       // totalSyms
        RawXClient.writeShortAt(request, 27, 0);                    // virtualMods
        // The sym map: four type indices, the group info, the width, then the keysyms inline.
        request[header + 4] = 1;                                    // groupInfo: one group
        request[header + 5] = (byte) keysyms.length;                // width
        RawXClient.writeShortAt(request, header + 6, keysyms.length);
        for (int i = 0; i < keysyms.length; i++) {
            RawXClient.writeIntAt(request, header + 8 + i * 4, keysyms[i]);
        }
        return request;
    }

    /**
     * A layout asked for **by name** is loaded, and the mapping a client reads afterwards is the
     * layout's own.
     *
     * <p>The name is not what a client sends: {@code setxkbmap -layout de} asks for the components the
     * rules file resolved to - {@code pc+de+inet(pc105)} - and the request below is that one, captured
     * off the wire. The keysym for keycode 29 is what says the layout was really installed, because
     * that key is `y` in the keymap this server starts with and `z` in German - the QWERTZ swap, and
     * the same value a reference server reports for the same keycode in the same layout
     * ({@code tools/GenLayouts} is where the table came from).
     *
     * <p><b>The layout is per server, not per client</b> - a keyboard is one keyboard - so this test
     * puts the built-in layout back when it is done, or every test after it shares the German one.
     */
    @Test
    void aLayoutAskedForByNameIsLoadedAndIsWhatClientsRead() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            try {
                byte[] reply = loadGermanByName(client);
                assertEquals(1, reply[10] & 0xFF,
                        "the layout was loaded, which is the flag a client reads before going on");

                int[] mapping = client.keyboardMapping(29, 1);
                assertEquals(0x7a, mapping[0],
                        "and keycode 29 is z, which is what makes this layout German rather than the "
                                + "one this server starts with");
            } finally {
                cn.oyzh.fx.xserver.input.Keymap.useBuiltinLayout();
            }
        }
    }

    /**
     * <b>A reply that says "loaded" and carries nothing is the same failure as not loading.</b> Xlib's
     * {@code XkbGetKeyboardByName} reads the reply by position: it gives up when {@code reported} is
     * zero, and for every bit that is set it reads one whole component reply out of the same buffer -
     * the map for {@code Types|Symbols}, a compat reply, a names reply, a geometry reply, in that
     * order - and then insists the buffer was consumed exactly. So this server used to install the
     * layout by name and still leave {@code setxkbmap} printing {@code Error loading new keyboard
     * description}: the flag was right and {@code reported} was zero.
     *
     * <p>What is asserted here is the structure a client walks, read the way Xlib reads it: the parts
     * claimed, a map reply at offset 32 whose own length accounts for the whole tail, and the keysyms
     * in that map being the loaded layout's - the German 'z' at keycode 29.
     */
    @Test
    void aByNameLoadAnswersWithTheKeymapItself() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            try {
                byte[] reply = loadGermanByName(client);
                int reported = RawXClient.readShort(reply, 14);
                assertEquals(0x0d, reported, "Xlib reads one embedded reply per bit set in `reported`,"
                        + " and gives up before reading any when it is zero: types 0x01, client and"
                        + " server symbols 0x04|0x08, which is exactly what the map here carries");

                byte[] map = java.util.Arrays.copyOfRange(reply, 32, reply.length);
                int mapExtra = RawXClient.readInt(map, 4) * 4;
                assertEquals(reply.length - 32, 32 + mapExtra,
                        "the tail is one whole map reply and nothing else, which is the accounting"
                                + " Xlib insists on (`extraLen` has to reach zero)");
                assertEquals(ALL_CLIENT_INFO, RawXClient.readShort(map, 12), "present, in the map");
                assertEquals(8, map[10] & 0xFF, "and its own key range");
                assertArrayEquals(new int[] {'z', 'Z'}, keysymsOf(map, 29));
            } finally {
                cn.oyzh.fx.xserver.input.Keymap.useBuiltinLayout();
            }
        }
    }

    /**
     * {@code GetNames} was refused, and a refusal is the worst answer XKB has: Xlib's default error
     * handler ends the process, which is what killed {@code setxkbmap} here before
     * {@code GetKbdByName} was answered. What this server can honestly name is the symbols component
     * it was asked to load - the client's own statement of the keymap it wanted - and that is what
     * comes back, as an atom the client can read with {@code GetAtomName}.
     */
    @Test
    void theNamesOfTheLoadedLayoutAreAnsweredRatherThanRefused() throws Exception {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            try {
                int opcode = client.queryExtension("XKEYBOARD");
                loadGermanByName(client);

                byte[] names = getNames(client, opcode);
                assertEquals(1, names[0] & 0xFF, "a reply, not an error");
                assertEquals(0x0c, RawXClient.readInt(names, 8),
                        "which: the symbols component and its physical twin, and nothing else - this"
                                + " server has no other component it could honestly name");
                assertEquals(8, names[12] & 0xFF, "minKeyCode");
                assertEquals(Keymap.MAX_KEYCODE, names[13] & 0xFF, "maxKeyCode");
                assertEquals(2, RawXClient.readInt(names, 4),
                        "the atoms live in the tail, one per bit set in `which`");
                assertEquals("pc+de+inet(pc105)", client.atomName(RawXClient.readInt(names, 32)));
            } finally {
                cn.oyzh.fx.xserver.input.Keymap.useBuiltinLayout();
            }
        }
    }

    /**
     * The {@code GetKbdByName} a real {@code setxkbmap -layout de} sends, as bytes, and the reply to
     * it. The captured body carries the request's length field, so it is copied in from offset 2.
     */
    private static byte[] loadGermanByName(RawXClient client) throws IOException {
        byte[] request = new byte[84];
        byte[] body = hex(GET_KBD_BY_NAME_DE);
        request[0] = (byte) client.queryExtension("XKEYBOARD");
        request[1] = (byte) GET_KBD_BY_NAME;
        System.arraycopy(body, 0, request, 2, body.length);
        client.sendRequest(request);
        return client.readReply();
    }

    /** The bytes of a hex string, for a request built from a captured one. */
    private static byte[] hex(String text) {
        byte[] bytes = new byte[text.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(text.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    // ------------------------------------------------------------------
    // The requests, in bytes, because the layout is positional
    // ------------------------------------------------------------------

    private static byte[] useExtension(RawXClient client, int opcode) throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) USE_EXTENSION;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeShortAt(request, 4, 1);     // wantedMajor
        RawXClient.writeShortAt(request, 6, 0);     // wantedMinor
        client.sendRequest(request);
        return client.readReply();
    }

    /** One of the requests that has no reply: four units, the device, everything else zero. */
    private static byte[] oneWay(int opcode, int minor) {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        RawXClient.writeShortAt(request, 2, 4);
        RawXClient.writeShortAt(request, 4, CORE_KEYBOARD);
        return request;
    }

    private static byte[] state(RawXClient client, int opcode) throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_STATE;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeShortAt(request, 4, CORE_KEYBOARD);
        client.sendRequest(request);
        return client.readReply();
    }

    private static byte[] getMap(RawXClient client, int opcode, int which) throws IOException {
        byte[] request = mapRequest(opcode, which);
        client.sendRequest(request);
        return client.readReply();
    }

    /** The bytes of the reply, for a caller that only wants to know it was a reply. */
    private static byte[] getMapBytes(RawXClient client, int opcode) throws IOException {
        return getMap(client, opcode, ALL_CLIENT_INFO);
    }

    /**
     * {@code GetNames}: device, a pad word, and the mask of names wanted - three units, which is the
     * length a reference server's own {@code BadLength} check settles.
     */
    private static byte[] getNames(RawXClient client, int opcode) throws IOException {
        byte[] request = new byte[12];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_NAMES;
        RawXClient.writeShortAt(request, 2, 3);
        RawXClient.writeShortAt(request, 4, CORE_KEYBOARD);
        RawXClient.writeIntAt(request, 8, ALL_NAMES);
        client.sendRequest(request);
        return client.readReply();
    }

    private static byte[] mapRequest(int opcode, int which) {
        byte[] request = new byte[28];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_MAP;
        RawXClient.writeShortAt(request, 2, 7);
        RawXClient.writeShortAt(request, 4, CORE_KEYBOARD);
        RawXClient.writeShortAt(request, 6, which);
        // Every other field zero: no partial, no slices, which is "the whole map".
        return request;
    }

    // ------------------------------------------------------------------
    // Reading the reply, whose offsets are the protocol's own
    // ------------------------------------------------------------------

    /** Where the modifier map starts: past the types, the sym maps and the keysyms they carry. */
    private static int arraysStart(byte[] map) {
        int at = 40;
        int types = map[15] & 0xFF;
        for (int i = 0; i < types; i++) {
            int entries = map[at + 5] & 0xFF;
            boolean preserve = (map[at + 6] & 0xFF) != 0;
            at += 8 + entries * (preserve ? 12 : 8);
        }
        int syms = map[20] & 0xFF;
        for (int i = 0; i < syms; i++) {
            at += 8 + RawXClient.readShort(map, at + 6) * 4;
        }
        return at;
    }

    /** The keysyms a keycode carries, read the way Xlib reads them: inline, after its own entry. */
    private static int[] keysymsOf(byte[] map, int keycode) {
        int at = 40;
        int types = map[15] & 0xFF;
        for (int i = 0; i < types; i++) {
            int entries = map[at + 5] & 0xFF;
            boolean preserve = (map[at + 6] & 0xFF) != 0;
            at += 8 + entries * (preserve ? 12 : 8);
        }
        int first = map[17] & 0xFF;
        int count = map[20] & 0xFF;
        for (int i = 0; i < count; i++) {
            int nsyms = RawXClient.readShort(map, at + 6);
            if (first + i == keycode) {
                int[] keysyms = new int[nsyms];
                for (int level = 0; level < nsyms; level++) {
                    keysyms[level] = RawXClient.readInt(map, at + 8 + level * 4);
                }
                return keysyms;
            }
            at += 8 + nsyms * 4;
        }
        return new int[0];
    }

    private static void assertArrayEquals(int[] expected, int[] actual) {
        org.junit.jupiter.api.Assertions.assertArrayEquals(expected, actual);
    }
}
