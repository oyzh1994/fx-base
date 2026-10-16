package cn.oyzh.fx.xserver.tools;

/**
 * Asks a server the XKB questions a client asks as it starts, and prints every reply <b>as bytes</b>.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1), and the harness for item B1 - which
 * is also item A4's fix. {@code xdotool} does not check {@code XkbGetMap}'s NULL, so a server without
 * XKEYBOARD does not make it fall back: it makes it segfault, in {@code xdo_new_with_opened_display},
 * before any subcommand sends a request of its own. That was measured with a gdb backtrace and a
 * trace of the XKB entry points (see the backlog); what this tool adds is the other half - the
 * <em>bytes</em> the reference server answers with, so a reply built here can be compared field by
 * field rather than "the extension is advertised and it did not crash".
 *
 * <pre>
 *   XkbProbe &lt;host&gt; &lt;port&gt;
 * </pre>
 *
 * <p>The requests are the two {@code XkbUseExtension} calls and the {@code XkbGetMap} with
 * {@code which = 0x7} ({@code XkbAllClientInfoMask}) that {@code xdotool} makes, plus {@code
 * XkbGetState} and {@code XkbGetControls}, which the toolkits ask for. The GetMap reply is the big
 * one: a header whose counts say how far each array reaches, then the arrays.
 */
public final class XkbProbe {

    private static final int USE_EXTENSION = 0;
    private static final int SELECT_EVENTS = 1;
    private static final int GET_STATE = 4;
    private static final int GET_CONTROLS = 6;
    private static final int SET_CONTROLS = 7;
    private static final int GET_MAP = 8;
    private static final int GET_NAMES = 17;
    private static final int GET_KBD_BY_NAME = 23;
    private static final int PER_CLIENT_FLAGS = 21;
    private static final int GET_DEVICE_INFO = 24;

    /** {@code XkbUseCoreKbd} - the device every client means when it does not name one. */
    private static final int USE_CORE_KBD = 0x0100;

    /** {@code XkbKeyTypesMask | XkbKeySymsMask | XkbModifierMapMask} - what xdotool asks for. */
    private static final int ALL_CLIENT_INFO = 0x7;

    /** {@code XkbRepeatKeysMask}: the one control bit this server honours, and the request's bit 0. */
    private static final int REPEAT_KEYS = 0x1;

    /** Core {@code GetKeyboardControl}, whose per-key bitmap is the same table XKB publishes. */
    private static final int GET_KEYBOARD_CONTROL = 103;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: XkbProbe <host> <port> [which] [dump <file>]"
                    + " [setrate <delay> <interval>] [setkeys zero|all]"
                    + " [setrepeat off|on]");
            return;
        }
        int which = args.length > 2 && !args[2].equals("dump") && !args[2].equals("setrate")
                && !args[2].equals("setkeys") && !args[2].equals("setrepeat")
                && !args[2].equals("getkbdname") && !args[2].equals("getnames")
                ? Integer.decode(args[2]) : ALL_CLIENT_INFO;
        String dump = null;
        Integer setDelay = null;
        Integer setInterval = null;
        byte[] setBitmap = null;
        Boolean setRepeat = null;
        String kbdName = null;
        Integer namesWhich = null;
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "dump" -> dump = args[++i];
                case "getkbdname" -> kbdName = args[++i];
                case "getnames" -> namesWhich = i + 1 < args.length ? Integer.decode(args[++i]) : 0x3fff;
                case "setrate" -> {
                    setDelay = Integer.parseInt(args[++i]);
                    setInterval = Integer.parseInt(args[++i]);
                }
                case "setkeys" -> {
                    // What a client sends when it builds the request by hand: all zeros or all ones.
                    setBitmap = new byte[32];
                    if (args[++i].equals("all")) {
                        java.util.Arrays.fill(setBitmap, (byte) 0xFF);
                    }
                }
                case "setrepeat" -> setRepeat = args[++i].equals("on");
                default -> {
                }
            }
        }
        try (Wire wire = Wire.connect(args[0], Integer.parseInt(args[1]))) {
            String name = "XKEYBOARD";
            byte[] request = new byte[(8 + name.length() + 3) & ~3];
            request[0] = 98;                        // QueryExtension
            Wire.writeShort(request, 2, request.length / 4);
            Wire.writeShort(request, 4, name.length());
            for (int i = 0; i < name.length(); i++) {
                request[8 + i] = (byte) name.charAt(i);
            }
            wire.send(request);
            byte[] reply = null;
            while (reply == null) {
                byte[] message = wire.readMessage();
                if (message == null) {
                    System.out.println("XKEYBOARD: no answer to QueryExtension");
                    return;
                }
                if ((message[0] & 0x7F) == 1) {
                    reply = message;
                }
            }
            int opcode = reply[9] & 0xFF;
            System.out.printf("XKEYBOARD present=%d opcode=%d firstEvent=%d firstError=%d%n",
                    reply[8] & 0xFF, opcode, reply[10] & 0xFF, reply[11] & 0xFF);
            if (opcode == 0) {
                return;
            }

            System.out.println("UseExtension: " + hex(useExtension(wire, opcode)));
            System.out.println("GetState:     " + hex(getState(wire, opcode)));
            byte[] controls = getControls(wire, opcode);
            System.out.println("GetControls:  " + hex(controls));
            if (controls.length >= 92) {
                printControls(controls);
            }
            if (setDelay != null || setBitmap != null || setRepeat != null) {
                // The controls struct a real client sends carries every field, so the ones this
                // probe does not mean to change are sent as the server just reported them.
                int change = 0;
                int affectEnabled = 0;
                int enabled = 0;
                if (setDelay != null) {
                    change |= REPEAT_KEYS;
                }
                if (setRepeat != null) {
                    affectEnabled |= REPEAT_KEYS;
                    enabled = setRepeat ? REPEAT_KEYS : 0;
                }
                byte[] bitmap = setBitmap != null ? setBitmap
                        : java.util.Arrays.copyOfRange(controls, 60, 92);
                System.out.printf("SetControls: changeControls=0x%x affectEnabled=0x%x enabled=0x%x"
                                + " delay=%d interval=%d%n", change, affectEnabled, enabled,
                        setDelay == null ? shortAt(controls, 20) : setDelay,
                        setInterval == null ? shortAt(controls, 22) : setInterval);
                setControls(wire, opcode, change, affectEnabled, enabled,
                        setDelay == null ? shortAt(controls, 20) : setDelay,
                        setInterval == null ? shortAt(controls, 22) : setInterval, bitmap);
                byte[] after = getControls(wire, opcode);
                System.out.println("GetControls:  " + hex(after));
                printControls(after);
                // A core GetKeyboardControl too: the per-key bitmap is the one table both rails
                // carry, and this is how the claim is checked rather than assumed.
                System.out.println("GetKeyboardControl: " + hex(getKeyboardControl(wire)));
            }
            System.out.println("PerClientFlags: " + hex(perClientFlags(wire, opcode)));
            System.out.println("GetDeviceInfo:  " + hex(getDeviceInfo(wire, opcode)));
            byte[] map = getMap(wire, opcode, which);
            System.out.println("GetMap<" + which + ">: " + map.length + " bytes");
            if (dump != null) {
                java.nio.file.Files.write(java.nio.file.Path.of(dump), map);
                System.out.println("  dumped to " + dump);
            }
            printMap(map, which);
            if (kbdName != null) {
                byte[] kbdReply = getKbdByName(wire, opcode, kbdName);
                System.out.println("GetKbdByName<" + kbdName + ">: " + hex(kbdReply));
                printWords(wire, kbdReply);
            }
            if (namesWhich != null) {
                byte[] namesReply = getNames(wire, opcode, namesWhich);
                System.out.println("GetNames<0x" + Integer.toHexString(namesWhich) + ">: "
                        + hex(namesReply));
                printWords(wire, namesReply);
            }
        }
    }

    /**
     * A reply as four byte words with their offsets and, where the word is an atom, the atom's name
     * read back from the server - so a reply can be read off the reference instead of derived from a
     * specification this fork has no class for. Words whose atom lookup fails print as {@code -}, and
     * only replies small enough to be headers and names get looked up at all (a GetMap reply's arrays
     * are not atoms and there would be hundreds of lookups).
     */
    private static void printWords(Wire wire, byte[] reply) {
        boolean atoms = reply.length <= 256;
        for (int at = 8; at + 4 <= reply.length; at += 4) {
            int word = intAt(reply, at);
            // The fixed part of an XKB reply is packed with counts, not atoms; asking the server to
            // name one is an error in its log, so only the tail is looked up.
            String name = (!atoms || at < 32) ? "" : atomName(wire, word);
            System.out.printf("  %3d: 0x%08x (%d)  %s%n", at, word, word, name);
        }
    }

    /** {@code GetAtomName}, for reading a reply whose fields are atom ids. */
    private static String atomName(Wire wire, int atom) {
        if (atom == 0) {
            return "None";
        }
        try {
            byte[] request = new byte[8];
            request[0] = 17;                            // GetAtomName
            Wire.writeShort(request, 2, 2);
            Wire.writeInt(request, 4, atom);
            wire.send(request);
            while (true) {
                byte[] message = wire.readMessage();
                if (message == null) {
                    return "-";
                }
                if ((message[0] & 0x7F) == 1) {
                    byte[] whole = wire.withExtraData(message);
                    int length = Wire.readShort(whole, 8);
                    StringBuilder text = new StringBuilder();
                    for (int i = 0; i < length; i++) {
                        text.append((char) (whole[32 + i] & 0xFF));
                    }
                    return text.toString();
                }
                if (message[0] == 0) {
                    return "-";
                }
            }
        } catch (Exception e) {
            return "-";
        }
    }

    /** {@code XkbUseExtension}: the version a client offers, and the one the server has. */
    private static byte[] useExtension(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) USE_EXTENSION;
        Wire.writeShort(request, 2, 2);
        Wire.writeShort(request, 4, 1);             // wantedMajor
        Wire.writeShort(request, 6, 0);             // wantedMinor
        return answer(wire, request, "UseExtension");
    }

    /** {@code XkbGetState}: the modifier and group state the keyboard is in. */
    private static byte[] getState(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_STATE;
        Wire.writeShort(request, 2, 2);
        Wire.writeShort(request, 4, USE_CORE_KBD);
        return answer(wire, request, "GetState");
    }

    /** {@code XkbGetControls}: the repeat delay/rate and the detectable-auto-repeat flag live here. */
    private static byte[] getControls(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_CONTROLS;
        Wire.writeShort(request, 2, 2);
        Wire.writeShort(request, 4, USE_CORE_KBD);
        return answer(wire, request, "GetControls");
    }

    /** {@code XkbSetControls}, which has no reply: the answer is the next GetControls. */
    private static void setControls(Wire wire, int opcode, int changeControls, int affectEnabled,
                                    int enabled, int delay, int interval, byte[] perKeyRepeat)
            throws Exception {
        byte[] request = new byte[100];
        request[0] = (byte) opcode;
        request[1] = (byte) SET_CONTROLS;
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeShort(request, 4, USE_CORE_KBD);                  // deviceSpec
        Wire.writeShort(request, 20, 0);                            // accessXOptions
        Wire.writeInt(request, 24, affectEnabled);                  // affectEnabledControls
        Wire.writeInt(request, 28, enabled);                        // enabledControls
        Wire.writeInt(request, 32, changeControls);                 // changeControls
        Wire.writeShort(request, 36, delay);                        // repeatDelay
        Wire.writeShort(request, 38, interval);                     // repeatInterval
        // The nine delays after the repeat pair stay zero, and so do the three accessXTimeout
        // fields and their two option words: this probe changes the repeat and nothing else.
        System.arraycopy(perKeyRepeat, 0, request, 68, 32);
        wire.send(request);
    }

    /**
     * Prints a {@code GetControls} reply field by field.
     *
     * <p>The offsets are the protocol's own, and they were read off this reply rather than derived:
     * the repeat delay sits at 20 and the interval at 22, which is where changing the rate through
     * {@code xset r rate} moves the bytes. The per-key bitmap is the last thirty two bytes, one bit
     * per keycode, least significant bit of each byte first.
     */
    private static void printControls(byte[] reply) {
        System.out.printf("  deviceID=%d mouseKeysDfltBtn=%d numGroups=%d groupsWrap=%d%n",
                reply[1] & 0xFF, reply[8] & 0xFF, reply[9] & 0xFF, reply[10] & 0xFF);
        System.out.printf("  repeatDelay=%d repeatInterval=%d%n",
                shortAt(reply, 20), shortAt(reply, 22));
        System.out.printf("  slowKeysDelay=%d debounceDelay=%d mouseKeysDelay=%d"
                        + " mouseKeysInterval=%d mouseKeysTimeToMax=%d mouseKeysMaxSpeed=%d"
                        + " mouseKeysCurve=%d%n",
                shortAt(reply, 24), shortAt(reply, 26), shortAt(reply, 28), shortAt(reply, 30),
                shortAt(reply, 32), shortAt(reply, 34), shortAt(reply, 36));
        System.out.printf("  accessXOption=0x%x accessXTimeout=%d accessXTimeoutOptionsMask=0x%x"
                        + " accessXTimeoutOptionsValues=0x%x%n",
                shortAt(reply, 38), shortAt(reply, 40), shortAt(reply, 42), shortAt(reply, 44));
        System.out.printf("  accessXTimeoutMask=0x%x accessXTimeoutValues=0x%x enabledControls=0x%x%n",
                intAt(reply, 48), intAt(reply, 52), intAt(reply, 56));
        String bits = "";
        for (int keycode = 0; keycode < 248; keycode++) {
            int index = 60 + (keycode >> 3);
            if (index < reply.length && (reply[index] & (1 << (keycode & 7))) != 0) {
                bits += keycode + " ";
            }
        }
        System.out.println("  perKeyRepeat: " + (bits.isEmpty() ? "(none)" : bits.trim()));
    }

    /**
     * Core {@code GetKeyboardControl}: the per-key repeat bitmap on the rail that has no extension
     * number in it. Its reply is fifty two bytes - the global flag at 1, the LED mask at 8, the bell
     * and click settings at 12, and the thirty two bitmap bytes at 20 - and the bitmap is the same
     * table XKB's {@code GetControls} publishes, which is the point of asking for both.
     */
    private static byte[] getKeyboardControl(Wire wire) throws Exception {
        byte[] request = new byte[4];
        request[0] = (byte) GET_KEYBOARD_CONTROL;
        request[1] = 0;
        Wire.writeShort(request, 2, 1);
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                return new byte[0];
            }
            if ((message[0] & 0x7F) == 1) {
                return message;
            }
            if (message[0] == 0) {
                return message;
            }
        }
    }

    private static int shortAt(byte[] bytes, int offset) {
        return (short) ((bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8));
    }

    private static int intAt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8)
                | ((bytes[offset + 2] & 0xFF) << 16) | ((bytes[offset + 3] & 0xFF) << 24);
    }

    /**
     * {@code XkbGetMap}: the request {@code xdotool} builds. Seven four byte units, and the field
     * order is the one the protocol header declares - device, what to send, what to leave out, then
     * one first/count pair per part.
     */
    private static byte[] getMap(Wire wire, int opcode, int which) throws Exception {
        byte[] request = new byte[28];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_MAP;
        Wire.writeShort(request, 2, 7);
        Wire.writeShort(request, 4, USE_CORE_KBD);
        Wire.writeShort(request, 6, which);         // full
        Wire.writeShort(request, 8, 0);             // partial
        // firstType/nTypes, firstKeySym/nKeySyms, firstKeyAct/nKeyActs, firstKeyBehavior/
        // nKeyBehaviors, virtualMods, firstKeyExplicit/nKeyExplicit, firstModMapKey/nModMapKeys,
        // firstVModMapKey/nVModMapKeys: all zero, meaning "everything".
        return answer(wire, request, "GetMap");
    }

    /**
     * {@code PerClientFlags}: what a toolkit asks to be allowed to change for itself. JavaFX asks
     * this one as EasyShell starts, which is why the probe does too.
     */
    private static byte[] perClientFlags(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[28];
        request[0] = (byte) opcode;
        request[1] = (byte) PER_CLIENT_FLAGS;
        Wire.writeShort(request, 2, 7);
        Wire.writeShort(request, 4, USE_CORE_KBD);
        Wire.writeInt(request, 8, 0);               // change
        Wire.writeInt(request, 12, 0);              // value
        Wire.writeInt(request, 16, 0);              // ctrlsToChange
        Wire.writeInt(request, 20, 0);              // autoCtrls
        Wire.writeInt(request, 24, 0);              // autoCtrlsValues
        return answer(wire, request, "PerClientFlags");
    }

    /** {@code GetDeviceInfo}: the one keyboard, as a device. */
    private static byte[] getDeviceInfo(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[20];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_DEVICE_INFO;
        Wire.writeShort(request, 2, 5);
        Wire.writeShort(request, 4, USE_CORE_KBD);
        Wire.writeShort(request, 6, 0xFFFF);        // wanted: everything
        Wire.writeInt(request, 8, 0);               // all buttons
        Wire.writeInt(request, 12, 0);              // all LEDs
        return answer(wire, request, "GetDeviceInfo");
    }

    /**
     * {@code XkbGetKbdByName}: "find this layout in a rules file and load it", which is what
     * {@code setxkbmap} asks and what a server with no keymap compiler cannot do.
     *
     * <p><b>The request's name is a list of counted strings, and which one carries the layout is
     * measured rather than read.</b> The reader in this server pulls the layout out of the sixth
     * string (index 5), a position that was fixed by sending {@code setxkbmap -layout de} and the
     * same with {@code -variant nodeadkeys} and watching only that string move. This probe sends the
     * same string in that position and leaves the other seven empty, so a reference server's answer
     * says whether that is the position it reads - which is the thing a reply built here depends on.
     */
    private static byte[] getKbdByName(Wire wire, int opcode, String symbols) throws Exception {
        // The strings, as a real `setxkbmap -layout de` sends them, read off the wire (strace, see
        // the backlog): the keycodes and geometry components are the ones the rules file resolved
        // to, the types and compat maps are "complete", and the layout the user asked for is inside
        // the symbols component. **Seven** counted strings and not five or eight - a request with
        // eight is refused with BadLength, which is how the count was measured rather than read.
        String[] fields = {" ", "", "evdev+aliases(qwertz)", "complete", "complete",
                symbols, "pc(pc105)"};
        int length = 10;
        for (String field : fields) {
            length += 1 + field.length();
        }
        byte[] request = new byte[(length + 3) & ~3];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_KBD_BY_NAME;
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeShort(request, 4, USE_CORE_KBD);   // deviceSpec
        Wire.writeShort(request, 6, 0xBF);           // want: all components but geometry
        request[8] = (byte) 0xFF;                    // load: True, as Xlib writes it
        int at = 10;
        for (String field : fields) {
            request[at++] = (byte) field.length();
            for (int i = 0; i < field.length(); i++) {
                request[at++] = (byte) field.charAt(i);
            }
        }
        return answer(wire, request, "GetKbdByName");
    }

    /**
     * {@code XkbGetNames}: the component names a client reads to know which keymap is loaded, and the
     * one request this server refuses - which is worse than an empty answer, because Xlib's default
     * error handler ends the client's process.
     */
    private static byte[] getNames(Wire wire, int opcode, int which) throws Exception {
        byte[] request = new byte[12];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_NAMES;
        Wire.writeShort(request, 2, 3);
        Wire.writeShort(request, 4, USE_CORE_KBD);   // deviceSpec
        Wire.writeShort(request, 6, 0);              // pad
        Wire.writeInt(request, 8, which);            // which: XkbAllNamesMask is 0x3fff
        return answer(wire, request, "GetNames");
    }

    private static byte[] answer(Wire wire, byte[] request, String what) throws Exception {
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                throw new IllegalStateException("no answer to " + what);
            }
            if ((message[0] & 0x7F) == 1) {
                return wire.withExtraData(message);
            }
            System.out.printf("  %s: refused, error %d%n", what, message[1] & 0xFF);
            return new byte[0];
        }
    }

    /**
     * The reply's counts and the arrays they reach. The offsets are the protocol header's own: the
     * GetMap reply is forty bytes before any array starts, not thirty two, because its two tail
     * fields (the modifier map's first/count) are read as part of it.
     */
    private static void printMap(byte[] map, int which) {
        if (map.length < 40) {
            return;
        }
        System.out.printf("  deviceID=%d minKeyCode=%d maxKeyCode=%d present=0x%x length=%d%n",
                map[1] & 0xFF, map[10] & 0xFF, map[11] & 0xFF, Wire.readShort(map, 12),
                Wire.readInt(map, 4));
        System.out.printf("  types: first=%d n=%d total=%d%n",
                map[14] & 0xFF, map[15] & 0xFF, map[16] & 0xFF);
        System.out.printf("  syms:  firstKey=%d n=%d total=%d%n",
                map[17] & 0xFF, map[20] & 0xFF, Wire.readShort(map, 18));
        System.out.printf("  actions: firstKey=%d n=%d total=%d%n",
                map[21] & 0xFF, map[24] & 0xFF, Wire.readShort(map, 22));
        System.out.printf("  behaviors: firstKey=%d n=%d total=%d%n",
                map[25] & 0xFF, map[26] & 0xFF, map[27] & 0xFF);
        System.out.printf("  explicit: firstKey=%d n=%d total=%d%n",
                map[28] & 0xFF, map[29] & 0xFF, map[30] & 0xFF);
        System.out.printf("  modmap: firstKey=%d n=%d total=%d%n",
                map[31] & 0xFF, map[32] & 0xFF, map[33] & 0xFF);
        System.out.printf("  vmodmap: firstKey=%d n=%d total=%d virtualMods=0x%x%n",
                map[34] & 0xFF, map[35] & 0xFF, map[36] & 0xFF, Wire.readShort(map, 38));

        int at = 40;
        // The type descriptors: eight bytes each, and each is followed by its own map entries.
        int types = map[15] & 0xFF;
        for (int i = 0; i < types && at + 8 <= map.length; i++) {
            int mods = map[at] & 0xFF;
            int realMods = map[at + 1] & 0xFF;
            int virtualMods = Wire.readShort(map, at + 2);
            int levels = map[at + 4] & 0xFF;
            int entries = map[at + 5] & 0xFF;
            int preserve = map[at + 6] & 0xFF;
            System.out.printf("  type %d: mask=0x%x realMods=0x%x virtualMods=0x%x levels=%d "
                            + "entries=%d preserve=%d%n",
                    i, mods, realMods, virtualMods, levels, entries, preserve);
            at += 8;
            for (int e = 0; e < entries && at + 8 <= map.length; e++) {
                // A GetMap reply's entries are the eight byte form, and a preserving type adds the
                // four byte preserve mask after each one.
                System.out.printf("    entry: active=%d mask=0x%x level=%d realMods=0x%x "
                                + "virtualMods=0x%x%n",
                        map[at] & 0xFF, map[at + 1] & 0xFF, map[at + 2] & 0xFF, map[at + 3] & 0xFF,
                        Wire.readShort(map, at + 4));
                at += preserve != 0 ? 12 : 8;
            }
        }
        // The sym maps: eight bytes each, then all the keysyms as four byte values.
        int syms = map[20] & 0xFF;
        int firstKeySym = map[17] & 0xFF;
        // Each sym map is eight bytes and its keysyms follow it inline - not a shared pool at the
        // end of the reply, which is what a first reading of the header suggests.
        for (int i = 0; i < syms && at + 8 <= map.length; i++) {
            int width = map[at + 5] & 0xFF;
            int nSyms = Wire.readShort(map, at + 6);
            StringBuilder row = new StringBuilder(String.format(
                    "  symmap kc%d: kt=%d,%d,%d,%d groupInfo=0x%x width=%d nSyms=%d =",
                    firstKeySym + i, map[at] & 0xFF, map[at + 1] & 0xFF, map[at + 2] & 0xFF,
                    map[at + 3] & 0xFF, map[at + 4] & 0xFF, width, nSyms));
            for (int level = 0; level < nSyms; level++) {
                row.append(String.format(" 0x%x", Wire.readInt(map, at + 8 + level * 4)));
            }
            System.out.println(row);
            at += 8 + nSyms * 4;
        }
        // The modifier map is a list of keycode/mods pairs, and its length is the *total*, not the
        // count in this reply's slice.
        int modMapKeys = map[33] & 0xFF;
        if (modMapKeys > 0) {
            System.out.printf("  modmap %d..%d: ", map[31] & 0xFF, (map[31] & 0xFF) + modMapKeys - 1);
            for (int i = 0; i < modMapKeys && at + 2 * i + 2 <= map.length; i++) {
                System.out.printf("%d=0x%x ", map[at + 2 * i] & 0xFF, map[at + 2 * i + 1] & 0xFF);
            }
            System.out.println();
            at += modMapKeys * 2;
        }
        System.out.println("  arrays end at " + at + " of " + map.length);
    }

    private static String hex(byte[] message) {
        StringBuilder text = new StringBuilder();
        for (byte b : message) {
            text.append(String.format("%02x ", b & 0xFF));
        }
        return text.toString();
    }
}
