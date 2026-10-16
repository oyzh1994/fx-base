package cn.oyzh.fx.xserver.input;

import cn.oyzh.fx.xserver.input.Keymap;
import cn.oyzh.fx.xserver.input.Keysyms;

import java.io.ByteArrayOutputStream;

/**
 * The XKEYBOARD version of {@link Keymap}: the {@code GetMap} reply, built from the same tables the
 * core protocol answers {@code GetKeyboardMapping} from.
 *
 * <p><b>Why this exists at all.</b> {@code xdotool} asks for the keymap through XKB, not through the
 * core request, and it does not check the answer: {@code _xdo_populate_charcode_map} walks
 * {@code xkb->map->key_sym_map[i]} straight away, so an X server without XKEYBOARD does not make it
 * fall back - it makes it dereference a null pointer, in {@code xdo_new_with_opened_display}, before
 * any subcommand has sent a request of its own. Measured here with a gdb backtrace (the fault is
 * {@code ldr x3, [x0, #32]} with x0 zero) and a trace of the XKB entry points, which shows exactly
 * two calls before the crash: {@code XkbUseExtension} and {@code XkbGetMap(which=0x7)}. Everything
 * after that is the client's own use of the map it was given, so the map has to be right, not merely
 * present.
 *
 * <p><b>One map, not two.</b> The keysyms here are {@link Keymap}'s own: a key whose two levels are
 * the same is one level wide, a letter is two, and the modifier map is the server's own modifier
 * rows. That is the point - a client that reads XKB and a client that reads the core mapping must
 * agree about what a keycode means, or the same keystroke types two different characters depending
 * on the toolkit. The wire format is Xvfb's, byte for byte, and was read off it with
 * {@code tools/XkbProbe} - including the two places the header misleads: the reply's fixed part is
 * forty bytes rather than thirty two, and each key's keysyms follow its own descriptor inline rather
 * than living in one pool at the end.
 *
 * <p><b>What is not here</b>: actions, behaviours, explicit components, the virtual-modifier map and
 * the compatibility map. They are parts of a {@code GetMap} a client may ask for, and a reply simply
 * does not claim them - {@code present} says which parts it carries. {@code xkbcomp} and
 * {@code setxkbmap} would need them; the toolkits that matter do not ask.
 */
public final class XkbMap {

    /** {@code XkbKeyTypesMask}: the key types, which say how a modifier selects a level. */
    public static final int KEY_TYPES_MASK = 1;
    /** {@code XkbKeySymsMask}: the keysyms per keycode. */
    public static final int KEY_SYMS_MASK = 2;
    /** {@code XkbKeyModifierMapMask}: which real modifiers each keycode sets. */
    public static final int MODIFIER_MAP_MASK = 4;

    /** {@code XkbAllClientInfoMask} - what {@code xdotool} asks for, and what a toolkit asks for. */
    public static final int ALL_CLIENT_INFO_MASK = KEY_TYPES_MASK | KEY_SYMS_MASK | MODIFIER_MAP_MASK;

    /**
     * The device id a {@code GetMap} reply names. Xvfb answers 3, which is Xorg's "Virtual core
     * keyboard"; this server has exactly one keyboard, and answering the reference's id keeps a
     * client that has one hard-coded (and a probe diffing the two servers) unconfused.
     */
    public static final int CORE_KEYBOARD_DEVICE = 3;

    /** The key range the map covers: every keycode, as the core mapping also reports. */
    private static final int MIN_KEYCODE = Keymap.MIN_KEYCODE & 0xFF;
    private static final int MAX_KEYCODE = Keymap.MAX_KEYCODE & 0xFF;

    /**
     * The four types every X server must define, in the order XKB numbers them.
     *
     * <p><b>Four is a floor, not a style choice.</b> A map with three types is rejected whole: Xlib
     * answers {@code XkbGetMap} with null, which is exactly the NULL xdotool then dereferences - so
     * "fewer types than it needs" and "no XKEYBOARD at all" look identical from the client's side.
     * Measured: with the types replaced by Xvfb's own (24 of them) the same reply parsed; with three
     * it did not. See the class comment.
     */
    private static final int ONE_LEVEL = 0;
    private static final int TWO_LEVEL = 1;
    private static final int ALPHABETIC = 2;
    private static final int KEYPAD = 3;

    private static final int SHIFT = 0x01;
    private static final int LOCK = 0x02;
    /** Mod2: what this server sets for Num Lock, and what the keypad type turns levels with. */
    private static final int NUM_LOCK = 0x10;

    private XkbMap() {
    }

    /** Which parts of a {@code GetMap} request this server has an answer for. */
    public static int supportedParts(int requested) {
        return requested & ALL_CLIENT_INFO_MASK;
    }

    /**
     * How many key types this server defines, which a {@code GetNames} reply has to state: it is
     * four, and a client that asks for the per-type names gets that many of them.
     */
    public static int typeCount() {
        return buildTypes().length;
    }

    /**
     * The whole {@code GetMap} reply, header and arrays, in the byte order the client asked for.
     *
     * @param requested the {@code full} mask from the request
     * @param firstType the first type to return, or 0
     * @param typeCount how many types to return, or 0 for all of them
     * @param firstKeySym the first keycode whose syms to return, or 0
     * @param keySymCount how many keycodes of syms to return, or 0 for all
     * @param firstModMapKey the first keycode of the modifier map, or 0
     * @param modMapKeyCount how many keycodes of it, or 0 for all
     */
    public static byte[] getMapReply(int requested, int firstType, int typeCount,
                                     int firstKeySym, int keySymCount,
                                     int firstModMapKey, int modMapKeyCount,
                                     int sequenceNumber, boolean bigEndian) {
        int present = supportedParts(requested);
        int[][] types = buildTypes();

        int typesFrom = clamp(firstType, 0, types.length);
        int typesTo = typeCount == 0 ? types.length : clamp(typesFrom + typeCount, typesFrom, types.length);
        int symsFrom = firstKeySym == 0 ? MIN_KEYCODE : clamp(firstKeySym, MIN_KEYCODE, MAX_KEYCODE + 1);
        int symsTo = keySymCount == 0 ? MAX_KEYCODE + 1 : clamp(symsFrom + keySymCount, symsFrom, MAX_KEYCODE + 1);
        int modsFrom = firstModMapKey == 0 ? MIN_KEYCODE
                : clamp(firstModMapKey, MIN_KEYCODE, MAX_KEYCODE + 1);
        int modsTo = modMapKeyCount == 0 ? MAX_KEYCODE + 1
                : clamp(modsFrom + modMapKeyCount, modsFrom, MAX_KEYCODE + 1);

        // The keysym part: one descriptor per keycode and that key's own keysyms after it.
        ByteArrayOutputStream syms = new ByteArrayOutputStream();
        int totalSyms = 0;
        int keySyms = 0;
        if ((present & KEY_SYMS_MASK) != 0) {
            for (int keycode = symsFrom; keycode < symsTo; keycode++) {
                int[] keysyms = keysymsFor(keycode);
                card8(syms, typeIndexFor(keysyms));
                card8(syms, 0);                     // the three unused group type indices
                card8(syms, 0);
                card8(syms, 0);
                card8(syms, 1);                     // groupInfo: one group
                card8(syms, keysyms.length);        // width
                card16(syms, keysyms.length, bigEndian);
                for (int keysym : keysyms) {
                    card32(syms, keysym, bigEndian);
                }
                totalSyms += keysyms.length;
                keySyms++;
            }
        }

        // The modifier map: one (keycode, real modifiers) pair per key that sets any, which is a list
        // rather than the byte-per-key the field names suggest.
        ByteArrayOutputStream modmap = new ByteArrayOutputStream();
        int modEntries = 0;
        if ((present & MODIFIER_MAP_MASK) != 0) {
            for (int keycode = modsFrom; keycode < modsTo; keycode++) {
                int mods = Keymap.modifierMaskForKeycode(keycode);
                if (mods != 0) {
                    card8(modmap, keycode);
                    card8(modmap, mods);
                    modEntries++;
                }
            }
        }

        ByteArrayOutputStream typesOut = new ByteArrayOutputStream();
        if ((present & KEY_TYPES_MASK) != 0) {
            for (int i = typesFrom; i < typesTo; i++) {
                writeType(typesOut, types[i], bigEndian);
            }
        }

        ByteArrayOutputStream reply = new ByteArrayOutputStream();
        card8(reply, 1);                            // reply
        card8(reply, present == 0 ? 0 : CORE_KEYBOARD_DEVICE);
        card16(reply, sequenceNumber & 0xFFFF, bigEndian);
        card32(reply, 0, bigEndian);                // length, patched below
        card16(reply, 0, bigEndian);                // pad
        card8(reply, MIN_KEYCODE);
        card8(reply, MAX_KEYCODE);
        card16(reply, present, bigEndian);
        card8(reply, (present & KEY_TYPES_MASK) != 0 ? typesFrom : 0);
        card8(reply, typesOut.size() == 0 ? 0 : typesTo - typesFrom);
        card8(reply, typesOut.size() == 0 ? 0 : types.length);
        card8(reply, (present & KEY_SYMS_MASK) != 0 ? symsFrom : 0);
        card16(reply, totalSyms, bigEndian);
        card8(reply, keySyms);
        card8(reply, 0);                            // firstKeyAction
        card16(reply, 0, bigEndian);                // totalActions
        card8(reply, 0);                            // nKeyActions
        card8(reply, 0);                            // firstKeyBehavior
        card8(reply, 0);                            // nKeyBehaviors
        card8(reply, 0);                            // totalKeyBehaviors
        card8(reply, 0);                            // firstKeyExplicit
        card8(reply, 0);                            // nKeyExplicit
        card8(reply, 0);                            // totalKeyExplicit
        card8(reply, (present & MODIFIER_MAP_MASK) != 0 ? modsFrom : 0);
        card8(reply, (present & MODIFIER_MAP_MASK) != 0 ? modsTo - modsFrom : 0);
        card8(reply, modEntries);                   // totalModMapKeys: the entries, not the range
        card8(reply, 0);                            // firstVModMapKey
        card8(reply, 0);                            // nVModMapKeys
        card8(reply, 0);                            // totalVModMapKeys
        card8(reply, 0);                            // pad
        card16(reply, 0, bigEndian);                // virtualMods: none are used

        reply.writeBytes(typesOut.toByteArray());
        reply.writeBytes(syms.toByteArray());
        reply.writeBytes(modmap.toByteArray());

        byte[] bytes = reply.toByteArray();
        // X replies are whole four byte units, and a modifier map with an odd number of entries ends
        // two bytes short of one.
        int padded = (bytes.length + 3) & ~3;
        if (padded != bytes.length) {
            byte[] longer = new byte[padded];
            System.arraycopy(bytes, 0, longer, 0, bytes.length);
            bytes = longer;
        }
        putCard32(bytes, 4, (bytes.length - 32) / 4, bigEndian);
        return bytes;
    }

    /**
     * The keysyms a keycode carries, with a level dropped when both levels are the same.
     *
     * <p>Width is a real thing in XKB: a key whose unshifted and shifted keysym are equal is one
     * level wide, and the reference says so (Escape comes back with one keysym, not two). A client
     * reads {@code width} keysyms out of the reply, so padding ours to two would put the next key's
     * symbols in the wrong place. A keycode nothing is assigned to still gets one level, carrying
     * {@code NoSymbol}: every keycode in the range is described, which is what the reference does.
     */
    private static int[] keysymsFor(int keycode) {
        int plain = Keymap.keysym(keycode, 0);
        int shifted = Keymap.keysym(keycode, 1);
        if (plain == Keysyms.NONE) {
            return new int[] {Keysyms.NONE};
        }
        return shifted == plain ? new int[] {plain} : new int[] {plain, shifted};
    }

    /** Which of the four types a key's keysyms call for. */
    private static int typeIndexFor(int[] keysyms) {
        if (keysyms.length < 2) {
            return ONE_LEVEL;
        }
        int plain = keysyms[0];
        int shifted = keysyms[1];
        // A letter: the shifted keysym is the capital of the plain one. Caps Lock reaches those, and
        // only those, so they get the type that carries Lock.
        if (plain >= 'a' && plain <= 'z' && shifted == plain - 32) {
            return ALPHABETIC;
        }
        // The keypad, whose levels Num Lock selects - which is what carrying the digit on the
        // shifted level was always for.
        if (plain >= Keysyms.KP_HOME && plain <= Keysyms.KP_EQUAL) {
            return KEYPAD;
        }
        return TWO_LEVEL;
    }

    /**
     * The four types, as the reference defines them.
     *
     * <p>An entry says "these modifiers are held, so use this level"; {@code mask} is what the entry
     * matches on and {@code level} is the level it selects. All four are one group wide.
     */
    private static int[][] buildTypes() {
        // mask, realMods, numLevels, then per entry: active, mask, level, realMods, virtualMods.
        return new int[][] {
            {0, 0, 1},
            {SHIFT, SHIFT, 2, 1, SHIFT, 1, SHIFT, 0},
            {SHIFT | LOCK, SHIFT | LOCK, 2, 1, SHIFT, 1, SHIFT, 0, 1, LOCK, 1, LOCK, 0},
            // Keypad: the digit is what Num Lock selects, and Shift does not. Measured against
            // Xvfb with xev - Shift and keycode 79 stays KP_Home there, and giving the keypad a
            // Shift entry made it KP_7 here.
            {SHIFT | NUM_LOCK, SHIFT | NUM_LOCK, 2, 1, NUM_LOCK, 1, NUM_LOCK, 0},
        };
    }

    /** One key type: its eight byte descriptor, then its map entries (eight bytes each). */
    private static void writeType(ByteArrayOutputStream out, int[] type, boolean bigEndian) {
        card8(out, type[0]);
        card8(out, type[1]);
        card16(out, 0, bigEndian);                  // virtual modifiers
        card8(out, type[2]);
        card8(out, (type.length - 3) / 5);
        card8(out, 0);                              // hasPreserve
        card8(out, 0);                              // pad
        for (int at = 3; at < type.length; at += 5) {
            card8(out, type[at]);
            card8(out, type[at + 1]);
            card8(out, type[at + 2]);
            card8(out, type[at + 3]);
            card16(out, type[at + 4], bigEndian);
            card16(out, 0, bigEndian);              // pad
        }
    }

    private static int clamp(int value, int low, int high) {
        return Math.max(low, Math.min(high, value));
    }

    private static void card8(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
    }

    private static void card16(ByteArrayOutputStream out, int value, boolean bigEndian) {
        if (bigEndian) {
            card8(out, value >> 8);
        }
        card8(out, value);
        if (!bigEndian) {
            card8(out, value >> 8);
        }
    }

    private static void card32(ByteArrayOutputStream out, int value, boolean bigEndian) {
        if (bigEndian) {
            card8(out, value >> 24);
            card8(out, value >> 16);
        }
        card8(out, value);
        card8(out, value >> 8);
        if (!bigEndian) {
            card8(out, value >> 16);
            card8(out, value >> 24);
        }
    }

    private static void putCard32(byte[] bytes, int offset, int value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 24);
            bytes[offset + 1] = (byte) (value >> 16);
            bytes[offset + 2] = (byte) (value >> 8);
            bytes[offset + 3] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
            bytes[offset + 2] = (byte) (value >> 16);
            bytes[offset + 3] = (byte) (value >> 24);
        }
    }
}
