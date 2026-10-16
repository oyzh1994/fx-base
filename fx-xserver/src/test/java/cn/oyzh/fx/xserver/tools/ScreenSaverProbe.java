package cn.oyzh.fx.xserver.tools;

/**
 * Asks a server for its screen-saver and energy-saving state, the way {@code xset} and a video player
 * do, and prints every field.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1), and the harness for item B7: the core
 * screen-saver requests were implemented but `MIT-SCREEN-SAVER` and `DPMS` were not advertised at all,
 * so anything that wants to say "do not blank while this plays" was told the extension was missing.
 *
 * <pre>
 *   ScreenSaverProbe &lt;host&gt; &lt;port&gt;
 *   ScreenSaverProbe &lt;host&gt; &lt;port&gt; set-timeout 60 30 0    # DPMS SetTimeouts, then read back
 * </pre>
 *
 * <p><b>The `core` mode is for this server only, and the live {@code xset} is the authority for the
 * core requests.</b> Its hand-built {@code SetScreenSaver} - sixteen bytes, the length field saying
 * four units, which is what Xlib's own request struct says - is accepted here and refused by Xvfb with
 * {@code BadLength}, while the *same* {@code xset s 60 0} works against both. Whatever the difference
 * is it is in this probe's bytes rather than in either server, and it was not run down: read the core
 * settings with {@code xset q} and change them with {@code xset s}, which is what a client does.
 *
 * <p>What it prints: for `MIT-SCREEN-SAVER`, the version, and `QueryInfo`'s state, saver window,
 * milliseconds until the saver runs and since the last user input, event mask and kind; for `DPMS`,
 * the version, whether the display is capable, the three timeouts in seconds, and the current power
 * level. Every field is printed whether or not the extension answered, because "the extension is
 * missing" and "the extension answered nothing useful" are different findings.
 */
public final class ScreenSaverProbe {

    /** {@code ScreenSaver} states, numbered as the extension numbers them. */
    private static final String[] SAVER_STATES = {"Off", "On", "Cycle", "Disabled"};

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: ScreenSaverProbe <host> <port> [set-timeouts standby suspend off]");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (Wire wire = Wire.connect(host, port)) {
            int saver = queryExtension(wire, "MIT-SCREEN-SAVER");
            int dpms = queryExtension(wire, "DPMS");
            System.out.printf("MIT-SCREEN-SAVER major opcode = %d, DPMS major opcode = %d%n", saver, dpms);

            // Only when asked: the core round trip ends with a ForceScreenSaver, which *changes* the
            // state this probe is here to read - so running it by default made every reading of the
            // state its own doing. (It did, and the reading said so.)
            if (args.length > 2 && args[2].equals("core")) {
                coreSettings(wire);
            } else if (args.length > 2 && args[2].equals("core-read")) {
                // Read only: no Set before it, so a refusal here is about the Get's own shape.
                coreRead(wire);
            }
            if (saver != 0) {
                readVersion(wire, saver, 0, "MIT-SCREEN-SAVER");
                readSaverInfo(wire, saver, wire.root());
                if (args.length > 2 && args[2].equals("set-timeouts")) {
                    writeRequest(wire, saver, 3, 0);        // SetAttributes(0,0,0,0...): nothing to change
                    System.out.println("  (sent SetAttributes)");
                }
            }
            if (dpms != 0) {
                readVersion(wire, dpms, 0, "DPMS");
                readBool(wire, dpms, 1, "capable");
                readTimeouts(wire, dpms);
                readInfo(wire, dpms);
                if (args.length > 2 && args[2].equals("set-timeouts") && args.length > 5) {
                    int[] timeouts = {Integer.parseInt(args[3]), Integer.parseInt(args[4]),
                            Integer.parseInt(args[5])};
                    byte[] request = new byte[20];
                    request[0] = (byte) dpms;
                    request[1] = 3;                     // SetTimeouts
                    Wire.writeShort(request, 2, 5);
                    Wire.writeShort(request, 8, timeouts[0]);
                    Wire.writeShort(request, 10, timeouts[1]);
                    Wire.writeShort(request, 12, timeouts[2]);
                    wire.send(request);
                    System.out.printf("  (sent DPMS SetTimeouts %d/%d/%d)%n", timeouts[0], timeouts[1],
                            timeouts[2]);
                    readTimeouts(wire, dpms);
                }
            }
        }
    }

    /** Just the core read, for telling a refusal of the Get from a refusal of the Set. */
    private static void coreRead(Wire wire) throws Exception {
        byte[] get = new byte[4];
        get[0] = 108;                                   // GetScreenSaver
        Wire.writeShort(get, 2, 1);
        wire.send(get);
        byte[] reply = readReply(wire, "core GetScreenSaver (no set before it)");
        if (reply != null) {
            System.out.printf("  core screen saver: timeout=%d interval=%d preferBlanking=%d "
                            + "allowExposures=%d%n", Wire.readShort(reply, 8),
                    Wire.readShort(reply, 10), reply[12] & 0xFF, reply[13] & 0xFF);
        }
    }

    /**
     * The *core* screen-saver requests, which is where a client without the extension goes: set a
     * timeout and read it back, so the opcodes and the field layout are both confirmed by a server
     * that answers rather than by a table.
     */
    private static void coreSettings(Wire wire) throws Exception {
        byte[] set = new byte[16];
        set[0] = 107;                                   // SetScreenSaver
        Wire.writeShort(set, 2, 4);
        Wire.writeShort(set, 4, 60);                    // timeout seconds
        Wire.writeShort(set, 6, 0);                     // interval
        set[8] = 1;                                     // prefer blanking: yes
        set[9] = 1;                                     // allow exposures: yes
        wire.send(set);

        byte[] get = new byte[4];
        get[0] = 108;                                   // GetScreenSaver
        Wire.writeShort(get, 2, 1);
        wire.send(get);
        byte[] reply = readReply(wire, "core GetScreenSaver");
        if (reply != null) {
            System.out.printf("  core screen saver: timeout=%d interval=%d preferBlanking=%d "
                            + "allowExposures=%d%n", Wire.readShort(reply, 8),
                    Wire.readShort(reply, 10), reply[12] & 0xFF, reply[13] & 0xFF);
        }

        // Four bytes: a mode byte and a request length, which is what Xlib sends. An eight byte one
        // is `BadLength` on a reference server and silently accepted here.
        byte[] force = new byte[4];
        force[0] = 115;                                 // ForceScreenSaver
        force[1] = 1;                                   // activate
        Wire.writeShort(force, 2, 1);
        wire.send(force);
        System.out.println("  (sent core SetScreenSaver 120/60 and ForceScreenSaver activate)");
    }

    /** A version request with two argument bytes, which is the shape both extensions use. */
    private static void readVersion(Wire wire, int opcode, int minor, String name) throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        Wire.writeShort(request, 2, 2);
        request[4] = 1;                                 // client major
        request[5] = 1;                                 // client minor
        wire.send(request);
        byte[] reply = readReply(wire, name + " QueryVersion");
        if (reply == null) {
            return;
        }
        System.out.printf("  %s version %d.%d%n", name, Wire.readShort(reply, 8),
                Wire.readShort(reply, 10));
    }

    /**
     * {@code QueryInfo}: what the saver is doing. The reply's two timers are what a client uses to
     * decide whether "do not blank" is needed yet, so they are printed as numbers rather than as a
     * summary.
     */
    private static void readSaverInfo(Wire wire, int opcode, int drawable) throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = 1;                                 // QueryInfo
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, drawable);
        wire.send(request);
        byte[] reply = readReply(wire, "MIT-SCREEN-SAVER QueryInfo");
        if (reply == null) {
            return;
        }
        int state = reply[1] & 0xFF;
        System.out.printf("  QueryInfo: state=%s window=0x%x until_server=%d since_input=%d "
                        + "eventMask=0x%x kind=%d%n",
                state < SAVER_STATES.length ? SAVER_STATES[state] : "state" + state,
                Wire.readInt(reply, 8), Wire.readInt(reply, 12), Wire.readInt(reply, 16),
                Wire.readInt(reply, 20), reply[24] & 0xFF);
    }

    private static void readBool(Wire wire, int opcode, int minor, String what) throws Exception {
        byte[] request = new byte[4];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        Wire.writeShort(request, 2, 1);
        wire.send(request);
        byte[] reply = readReply(wire, "DPMS " + what);
        if (reply != null) {
            System.out.printf("  %s = %s%n", what, reply[8] == 0 ? "no" : "yes");
        }
    }

    private static void readTimeouts(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[4];
        request[0] = (byte) opcode;
        request[1] = 2;                                 // GetTimeouts
        Wire.writeShort(request, 2, 1);
        wire.send(request);
        byte[] reply = readReply(wire, "DPMS GetTimeouts");
        if (reply != null) {
            System.out.printf("  timeouts: standby=%d suspend=%d off=%d (seconds; 0 means never)%n",
                    Wire.readShort(reply, 8), Wire.readShort(reply, 10), Wire.readShort(reply, 12));
        }
    }

    private static void readInfo(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[4];
        request[0] = (byte) opcode;
        request[1] = 7;                                 // Info
        Wire.writeShort(request, 2, 1);
        wire.send(request);
        byte[] reply = readReply(wire, "DPMS Info");
        if (reply != null) {
            System.out.printf("  power level = %d, state = %s%n", Wire.readShort(reply, 8),
                    reply[10] == 0 ? "off" : "on");
        }
    }

    /** A one-unit extension request with no arguments, for the calls that only need to be accepted. */
    private static void writeRequest(Wire wire, int opcode, int minor, int units) throws Exception {
        byte[] request = new byte[Math.max(4, units * 4)];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        Wire.writeShort(request, 2, request.length / 4);
        wire.send(request);
    }

    /** The next reply, or null when the server answered with an error or wrote nothing. */
    private static byte[] readReply(Wire wire, String what) throws Exception {
        wire.readTimeoutMillis(1000);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                System.out.printf("  %s: no answer%n", what);
                wire.readTimeoutMillis(3000);
                return null;
            }
            if ((message[0] & 0x7F) == 1) {
                wire.readTimeoutMillis(3000);
                if (Wire.hasExtraData(message)) {
                    // The replies here are all fixed size; extra data would mean the layout differs
                    // from what this probe assumes, and saying so beats printing shifted fields.
                    System.out.printf("  %s: reply carries extra data this probe does not read%n", what);
                }
                return message;
            }
            System.out.printf("  %s: refused, error %d for opcode %d (value 0x%x)%n", what,
                    message[1] & 0xFF, message[10] & 0xFF, Wire.readInt(message, 4));
            wire.readTimeoutMillis(3000);
            return null;
        }
    }

    private static int queryExtension(Wire wire, String name) throws Exception {
        byte[] request = new byte[(8 + name.length() + 3) & ~3];
        request[0] = 98;                            // QueryExtension
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeShort(request, 4, name.length());
        for (int i = 0; i < name.length(); i++) {
            request[8 + i] = (byte) name.charAt(i);
        }
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                return 0;
            }
            if ((message[0] & 0x7F) == 1) {
                return message[8] == 0 ? 0 : message[9] & 0xFF;
            }
        }
    }

    private ScreenSaverProbe() {
    }
}
