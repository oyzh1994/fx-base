package cn.oyzh.fx.xserver.tools;

/**
 * Asks a server the RandR questions {@code xrandr} asks, and prints the replies <b>as bytes</b>.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1), and the harness for item B2. RandR's
 * replies are large and read positionally, so a field the server put in the wrong place does not
 * produce an error - it produces a plausible wrong number, or a client that dies in its own library.
 * Printing the raw reply next to the fields it is supposed to carry is how "the ids are there" is
 * told from "the client read a zero".
 *
 * <pre>
 *   RandrProbe &lt;host&gt; &lt;port&gt;
 * </pre>
 *
 * <p>What it does: {@code QueryVersion}, {@code GetScreenSizeRange}, {@code GetScreenResources}, then
 * {@code GetOutputInfo} and {@code GetCrtcInfo} for each id the resource list gave it, dumping every
 * reply. The ids are the interesting part: they are the server's own, and a client passes them back.
 */
public final class RandrProbe {

    private static final int QUERY_VERSION = 0;
    private static final int GET_SCREEN_SIZE_RANGE = 6;
    private static final int GET_SCREEN_RESOURCES = 8;
    private static final int GET_OUTPUT_INFO = 9;
    private static final int GET_CRTC_INFO = 20;
    private static final int GET_MONITORS = 42;
    private static final int GET_OUTPUT_PROPERTY = 15;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: RandrProbe <host> <port>");
            return;
        }
        try (Wire wire = Wire.connect(args[0], Integer.parseInt(args[1]))) {
            int opcode = queryExtension(wire, "RANDR");
            System.out.println("RANDR major opcode = " + opcode);
            if (opcode == 0) {
                return;
            }
            // Two four byte arguments - the client's major and minor - in a three unit request.
            byte[] version = request(wire, opcode, QUERY_VERSION, 3, 1, 5);
            System.out.printf("  QueryVersion: %d.%d%n", Wire.readInt(version, 8),
                    Wire.readInt(version, 12));

            byte[] range = request(wire, opcode, GET_SCREEN_SIZE_RANGE, 2, wire.root());
            System.out.printf("  GetScreenSizeRange: min %dx%d, max %dx%d%n",
                    Wire.readShort(range, 8), Wire.readShort(range, 10),
                    Wire.readShort(range, 12), Wire.readShort(range, 14));

            byte[] resources = request(wire, opcode, GET_SCREEN_RESOURCES, 2, wire.root());
            int crtcs = Wire.readShort(resources, 16);
            int outputs = Wire.readShort(resources, 18);
            int modes = Wire.readShort(resources, 20);
            int namesLen = Wire.readShort(resources, 22);
            System.out.printf("  GetScreenResources: %d crtc(s), %d output(s), %d mode(s), "
                            + "%d bytes of names%n", crtcs, outputs, modes, namesLen);
            int at = 32;
            int[] crtcIds = new int[crtcs];
            for (int i = 0; i < crtcs; i++) {
                crtcIds[i] = Wire.readInt(resources, at + i * 4);
            }
            int[] outputIds = new int[outputs];
            for (int i = 0; i < outputs; i++) {
                outputIds[i] = Wire.readInt(resources, at + crtcs * 4 + i * 4);
            }
            System.out.printf("    crtcs: %s, outputs: %s%n", hex(crtcIds), hex(outputIds));
            for (int i = 0; i < modes; i++) {
                // ModeInfo: id, width, height, dot clock, then the sync geometry, the name length and
                // the flags - 32 bytes each, before the names blob.
                // ModeInfo: id, width, height, dot clock, six sync words, name length, flags - so
                // the name length is at +26 and the flags at +28. Reading one off is invisible: it
                // prints a plausible count that happens to be the flags.
                int modeAt = 32 + (crtcs + outputs) * 4 + i * 32;
                System.out.printf("    mode 0x%x: %dx%d, name length %d, flags 0x%x%n",
                        Wire.readInt(resources, modeAt), Wire.readShort(resources, modeAt + 4),
                        Wire.readShort(resources, modeAt + 6), Wire.readShort(resources, modeAt + 26),
                        Wire.readInt(resources, modeAt + 28));
            }

            for (int output : outputIds) {
                byte[] info = request(wire, opcode, GET_OUTPUT_INFO, 3, output, 0);
                // The counts after the fixed part, in the order the protocol puts them: numCrtcs,
                // numModes, numClones, **numPreferred**, nameLen - which is not the order the
                // generated reply class writes them in, and that is what this probe found out the
                // hard way: its labels used to be shifted by one field, so a reference's
                // `numCrtcs = 1` was printed as "1 preferred" and read as evidence that the output
                // carries no modes. Measured against Xvfb's own bytes, 2026-09-28.
                System.out.printf("  GetOutputInfo(0x%x): status %d, crtc 0x%x, %dx%dmm, connection %d,"
                                + " %d crtc(s), %d mode(s), %d clone(s), %d preferred, nameLen %d%n",
                        output, info[1] & 0xFF, Wire.readInt(info, 12), Wire.readInt(info, 16),
                        Wire.readInt(info, 20), info[24] & 0xFF, Wire.readShort(info, 26),
                        Wire.readShort(info, 28), Wire.readShort(info, 30), Wire.readShort(info, 32),
                        Wire.readShort(info, 34));
            }
            // GetMonitors: window, get-active, then two words of pad - a three unit request whose
            // reply carries the monitor array.
            byte[] monitors = request(wire, opcode, GET_MONITORS, 3, wire.root(), 1);
            System.out.printf("  GetMonitors: %d monitor(s), %d output(s), reply %d bytes%n",
                    Wire.readInt(monitors, 12), Wire.readInt(monitors, 16), monitors.length);
            System.out.printf("    bytes: %s%n", hex(monitors));

            if (outputIds.length > 0) {
                // A property an output may have, and one that no server has: the answer to the second
                // is what a client reads before deciding there is nothing to read.
                outputProperty(wire, opcode, outputIds[0], wire.internAtom("non-desktop"));
                outputProperty(wire, opcode, outputIds[0], wire.internAtom("xserver-no-such-property"));
            }
            for (int crtc : crtcIds) {
                byte[] info = request(wire, opcode, GET_CRTC_INFO, 3, crtc, 0);
                // GetCrtcInfo: status, timestamp, x, y, width, height, mode, rotation, rotations,
                // then the two counts and the two lists.
                System.out.printf("  GetCrtcInfo(0x%x): status %d, %dx%d+%d+%d, mode 0x%x, rotation %d,"
                                + " %d output(s), %d possible%n",
                        crtc, info[1] & 0xFF, Wire.readShort(info, 16), Wire.readShort(info, 18),
                        (short) Wire.readShort(info, 12), (short) Wire.readShort(info, 14),
                        Wire.readInt(info, 20), Wire.readShort(info, 24), Wire.readShort(info, 28),
                        Wire.readShort(info, 30));
            }
        }
    }

    private static String hex(int[] values) {
        StringBuilder text = new StringBuilder();
        for (int value : values) {
            text.append(text.isEmpty() ? "" : ", ").append("0x").append(Integer.toHexString(value));
        }
        return text.toString();
    }

    /**
     * {@code GetOutputProperty}: the output, the property, the type (any), the range, then the
     * flags - seven units, which is what the request is: the last word carries {@code delete},
     * {@code pending} and two bytes of padding. Without it the request is a word short and **both**
     * servers refuse it - Xvfb with {@code BadLength}, this one with {@code BadRequest} - so the
     * probe measured nothing at all, and the reference's answer to a property that is not there
     * (which is {@code BadName}) had never been seen. Measured 2026-09-26.
     */
    private static void outputProperty(Wire wire, int opcode, int output, int property)
            throws Exception {
        byte[] request = new byte[28];
        request[0] = (byte) opcode;
        request[1] = (byte) GET_OUTPUT_PROPERTY;
        Wire.writeShort(request, 2, 7);
        Wire.writeInt(request, 4, output);
        Wire.writeInt(request, 8, property);
        Wire.writeInt(request, 12, 0);              // any type
        Wire.writeInt(request, 16, 0);              // long offset
        Wire.writeInt(request, 20, 0xFFFFFFFF);     // the whole property
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                System.out.println("  GetOutputProperty: no answer");
                return;
            }
            if ((message[0] & 0x7F) == 1) {
                byte[] whole = wire.withExtraData(message);
                System.out.printf("  GetOutputProperty(%s): format %d, type 0x%x, %d item(s), %d "
                                + "byte(s) after%n", wire.atomName(property), whole[1] & 0xFF,
                        Wire.readInt(whole, 8), Wire.readInt(whole, 16), Wire.readInt(whole, 12));
                return;
            }
            System.out.printf("  GetOutputProperty(%s): refused, error %d%n", wire.atomName(property),
                    message[1] & 0xFF);
            return;
        }
    }

    /** The bytes of a message, so a field in the wrong place can be seen rather than guessed at. */
    private static String hex(byte[] message) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < Math.min(message.length, 72); i++) {
            text.append(String.format("%02x", message[i] & 0xFF));
            text.append(i % 4 == 3 ? " " : "");
        }
        return text.toString();
    }

    /** A RandR request: the minor opcode, then however many four byte words the caller has. */
    private static byte[] request(Wire wire, int opcode, int minor, int units, int... args)
            throws Exception {
        byte[] request = new byte[units * 4];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        Wire.writeShort(request, 2, units);
        for (int i = 0; i < args.length; i++) {
            Wire.writeInt(request, 4 + i * 4, args[i]);
        }
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                throw new IllegalStateException("no answer to RANDR minor " + minor);
            }
            if ((message[0] & 0x7F) == 1) {
                return wire.withExtraData(message);
            }
            System.out.printf("  RANDR minor %d: refused, error %d%n", minor, message[1] & 0xFF);
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

    private RandrProbe() {
    }
}
