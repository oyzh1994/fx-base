package cn.oyzh.fx.xserver.tools;

/**
 * Asks a server the XInput2 questions a client asks as it starts, and prints every reply <b>as bytes
 * as well as parsed</b>.
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1) and the harness for item B4. XI2 is
 * the one input extension modern toolkits prefer - {@code xeyes} prints a warning about the missing
 * {@code XInputExtension} and falls back to core input, and {@code xinput} cannot list a thing
 * without it - and its replies are positional to the byte, so a server that answers them has to
 * answer them exactly. The requests here are the ones a client makes before it will believe the
 * extension: the version handshake, the device list, which device a client gets, and the event
 * selection.
 *
 * <pre>
 *   XiProbe &lt;host&gt; &lt;port&gt;                query, version, client pointer, device list
 *   XiProbe &lt;host&gt; &lt;port&gt; device &lt;id&gt;    one device (0 means every device)
 *   XiProbe &lt;host&gt; &lt;port&gt; select &lt;mask&gt;  select XI2 events on a window and print what arrives
 *   XiProbe &lt;host&gt; &lt;port&gt; dump            the raw bytes of every reply above
 *   XiProbe &lt;host&gt; &lt;port&gt; versions        XIQueryVersion for every version a client might ask for
 *   XiProbe &lt;host&gt; &lt;port&gt; raw             select the raw events (XI 2.1) on the root and drive XTEST
 *   XiProbe &lt;host&gt; &lt;port&gt; barrier [id]    XIBarrierReleasePointer, which is refused by the barrier id
 * </pre>
 *
 * <p><b>The reply layouts are read off a reference server here, not derived.</b> {@code
 * XIQueryDevice} is the one that matters: the reply is a header whose {@code num_infos} says how many
 * {@code XIDeviceInfo} records follow, and each record carries a variable number of {@code
 * DeviceClass} records whose length is in the record itself. Getting any of it a byte out makes
 * {@code xinput} print nonsense or fault, and nothing in this repository's own model says what is
 * right - {@code tools/XkbProbe}'s lesson, which is why this tool prints bytes.
 */
public final class XiProbe {

    private static final String EXTENSION = "XInputExtension";

    // XI2 minor opcodes, from the extension's own numbering.
    private static final int GET_CLIENT_POINTER = 45;
    private static final int SELECT_EVENTS = 46;
    private static final int QUERY_VERSION = 47;
    private static final int QUERY_DEVICE = 48;

    /** The XI2 version this tool asks for, which is what a toolkit asks before anything else. */
    private static final int WANTED_MAJOR = 2;
    private static final int WANTED_MINOR = 0;

    /** {@code XIAllDevices} and {@code XIAllMasterDevices}. */
    private static final int ALL_DEVICES = 0;
    private static final int ALL_MASTER_DEVICES = 1;

    /**
     * {@code XI_ButtonPress | XI_ButtonRelease | XI_Motion} - the three a client watching the pointer
     * asks for.
     *
     * <p><b>Which bits those are was measured, one bit at a time, and the answer is not the one a
     * reading of the core masks suggests.</b> Selecting {@code 1<<3} delivers <em>nothing</em> and
     * lets the core events through; {@code 1<<4} delivers an XI2 {@code ButtonPress} and suppresses
     * the core one; {@code 1<<6} delivers a {@code Motion}. So the XI2 mask bits are one higher than
     * the XI1/core numbering puts them, which is what the fork's own {@code XIEventMask} says:
     * {@code DEVICE_CHANGED(0b10)}, {@code KEY_PRESS(0b100)}, {@code BUTTON_PRESS(0b10000)},
     * {@code MOTION(0b1000000)}. Choosing them off the core values by hand gives a mask that is
     * accepted, changes nothing, and looks exactly like a server that cannot deliver a device event.
     */
    private static final int BUTTON_MOTION_MASK = (1 << 4) | (1 << 5) | (1 << 6);

    /**
     * The raw masks (XI 2.1): {@code XI_RawKeyPress .. XI_RawMotion}, which are event numbers 13 to
     * 17 and therefore bits 13 to 17 - measured the same way every other mask bit here was, and
     * measured to be delivered on the <b>root</b> window alone.
     */
    private static final int RAW_MASK = (1 << 13) | (1 << 14) | (1 << 15) | (1 << 16) | (1 << 17);

    /** The barrier a {@code barrier} run names when the caller does not: one that cannot exist. */
    private static int barrierId = 0x1234;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: XiProbe <host> <port> [device <id>] [select <mask> [deviceid]"
                    + " [root]] [dump]");
            return;
        }
        String mode = args.length > 2 ? args[2] : "query";
        boolean dump = false;
        int deviceId = ALL_DEVICES;
        String propertyName = "XServerBacklogProbe";
        int selectMask = BUTTON_MOTION_MASK;
        int selectDevice = ALL_MASTER_DEVICES;
        boolean selectRoot = false;
        boolean driveKey = false;
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "device" -> {
                    mode = "device";
                    deviceId = Integer.decode(args[++i]);
                }
                case "select" -> {
                    mode = "select";
                    if (i + 1 < args.length && !args[i + 1].equals("dump")
                            && !args[i + 1].equals("root")) {
                        selectMask = Integer.decode(args[++i]);
                    }
                    if (i + 1 < args.length && !args[i + 1].equals("dump")
                            && !args[i + 1].equals("root")) {
                        selectDevice = Integer.decode(args[++i]);
                    }
                    if (i + 1 < args.length && args[i + 1].equals("root")) {
                        selectRoot = true;
                        i++;
                    }
                    if (i + 1 < args.length && args[i + 1].equals("key")) {
                        driveKey = true;
                        i++;
                    }
                }
                case "listprops" -> {
                    mode = "listprops";
                    if (i + 1 < args.length && args[i + 1].matches("\\d+")) {
                        deviceId = Integer.parseInt(args[++i]);
                    }
                }
                case "getprop" -> {
                    propertyName = args[++i];
                    if (i + 1 < args.length && args[i + 1].matches("\\d+")) {
                        deviceId = Integer.parseInt(args[++i]);
                    }
                    if (i + 1 < args.length && args[i + 1].matches("\\d+")) {
                        PROPERTY_UNITS = Integer.parseInt(args[++i]);
                    }
                }
                case "dump" -> dump = true;
                case "versions" -> mode = "versions";
                case "raw" -> mode = "raw";
                case "barrier" -> {
                    mode = "barrier";
                    if (i + 1 < args.length && args[i + 1].startsWith("0x")) {
                        barrierId = Integer.decode(args[++i]);
                    }
                }
                default -> {
                }
            }
        }
        try (Wire wire = Wire.connect(args[0], Integer.parseInt(args[1]))) {
            int opcode = queryExtension(wire);
            if (opcode == 0) {
                System.out.println("XInputExtension is not offered by this server");
                return;
            }
            System.out.println("XInputExtension opcode " + opcode);
            if (mode.equals("versions")) {
                // One connection per ask: a *reference* negotiates once and answers the version it
                // latched for every later call (measured 2026-10-04: asking 2.0, then 2.3, then 2.9
                // on one connection gives 2.0, 2.0, 2.0), so asking repeatedly on one connection
                // measures the latch rather than the negotiation.
                versions(args[0], Integer.parseInt(args[1]));
                return;
            }
            legacyVersion(wire, opcode, dump);
            listInputDevices(wire, opcode, dump);
            if (mode.equals("raw")) {
                queryVersion(wire, opcode, dump);
                rawEvents(wire, opcode);
                return;
            }
            if (mode.equals("barrier")) {
                queryVersion(wire, opcode, dump);
                barrierRelease(wire, opcode, barrierId);
                return;
            }
            if (mode.equals("select")) {
                // The version handshake first, because that is the order a client uses and because
                // an XISelectEvents sent before it is a request the reference accepts and then does
                // nothing about: the run is quiet on the XI2 rail and loud on the core one, which
                // reads as a server that cannot deliver a device event at all.
                queryVersion(wire, opcode, dump);
                selectEvents(wire, opcode, selectMask, selectDevice, selectRoot, driveKey);
                return;
            }
            queryVersion(wire, opcode, dump);
            if (mode.equals("getprop")) {
                getProperty(wire, opcode, deviceId, propertyName, dump);
                return;
            }
            if (mode.equals("listprops")) {
                listProperties(wire, opcode, deviceId, dump);
                return;
            }
            // A window of the client's own, because the root is refused (see getClientPointer).
            int window = createWindow(wire);
            getClientPointer(wire, opcode, window, dump);
            if (mode.equals("device")) {
                queryDevice(wire, opcode, deviceId, dump);
            } else {
                queryDevice(wire, opcode, ALL_DEVICES, dump);
            }
        }
    }

    /**
     * {@code XIGetProperty}, the *device property* read - the request this server refuses, which cost
     * a forwarded client a protocol error as it started.
     *
     * <p>A property that does not exist is the interesting case, because it is the only answer this
     * server could give: it has no device properties at all. What a reference answers for one is what
     * this asks for - by name, so a reader can see whether the answer is a reply with
     * {@code type = None} or an error, and the reply's bytes settle where its fields are, since Xlib
     * reads them positionally.
     */
    private static int PROPERTY_UNITS = 7;

    /**
     * {@code XIListProperties}: which properties a device has, which is the request that tells a
     * client whether the property family is worth using at all ({@code xinput list-props} sends this
     * first). A device with none is the case this server can answer, and a reference's reply for a
     * device *with* properties is what fixes the layout: a bare header with the count at 8 and the
     * atoms after it.
     */
    private static void listProperties(Wire wire, int opcode, int device, boolean dump)
            throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = 56;                            // XIListProperties
        Wire.writeShort(request, 2, 2);
        Wire.writeShort(request, 4, device);
        wire.send(request);
        byte[] reply = replyTo(wire, "XIListProperties");
        int count = Wire.readShort(reply, 8);
        System.out.println("XIListProperties device=" + device + " -> " + count
                + " propert(ies), reply " + reply.length + " bytes");
        for (int i = 0; i < count && 32 + i * 4 + 4 <= reply.length; i++) {
            System.out.println("   property " + Wire.readInt(reply, 32 + i * 4));
        }
        hex("XIListProperties", reply);
    }

    private static void getProperty(Wire wire, int opcode, int device, String name, boolean dump)
            throws Exception {
        int property = internAtom(wire, name);
        byte[] request = new byte[PROPERTY_UNITS * 4];
        request[0] = (byte) opcode;
        request[1] = 59;                            // XIGetProperty
        Wire.writeShort(request, 2, PROPERTY_UNITS);
        Wire.writeShort(request, 4, device);        // deviceid
        Wire.writeInt(request, 8, property);
        Wire.writeInt(request, 12, 0);              // type: AnyPropertyType
        Wire.writeInt(request, 16, 0);              // offset
        Wire.writeInt(request, 20, 1000);           // len, in four byte units
        // delete (24) and padding stay zero.
        wire.send(request);
        byte[] reply = replyTo(wire, "XIGetProperty");
        System.out.println("XIGetProperty device=" + device + " property=" + name + " (atom "
                + property + ") -> " + reply.length + " bytes");
        if (reply.length >= 32) {
            // byte 0 says whether this is a reply (1) or an error (0), and an error's code is byte 1
            // - the difference between "this server has no such property" and "this request was not
            // understood", which look the same in a dump of zeros.
            System.out.println("  byte0=" + (reply[0] & 0xFF) + " errorCode=" + (reply[1] & 0xFF)
                    + " type=" + Wire.readInt(reply, 8)
                    + " bytesAfter=" + Wire.readInt(reply, 12)
                    + " numItems=" + Wire.readInt(reply, 16)
                    + " format=" + (reply[20] & 0xFF));
        }
        hex("XIGetProperty", reply);
    }

    /**
     * {@code XIQueryVersion} for every version a client might ask for, <b>one connection each</b>.
     *
     * <p>Measured on a reference 2026-10-04: 2.0 -&gt; 2.0, 2.3 -&gt; 2.3, 2.4 -&gt; 2.4, 2.5 -&gt;
     * 2.4, <b>3.0 -&gt; 2.4</b> (a client whose major is ahead is told what the server speaks and
     * decides for itself), and a major below 2 is <b>BadValue</b> with the requested major as its bad
     * value. A second call on the same connection is answered with the version that connection
     * already latched, which is why this opens a connection per ask.
     */
    private static void versions(String host, int port) throws Exception {
        int[][] wanted = {{2, 0}, {2, 1}, {2, 2}, {2, 3}, {2, 4}, {2, 5}, {2, 9}, {3, 0}, {1, 0}};
        for (int[] want : wanted) {
            try (Wire wire = Wire.connect(host, port)) {
                int opcode = queryExtension(wire);
                byte[] request = new byte[8];
                request[0] = (byte) opcode;
                request[1] = QUERY_VERSION;
                Wire.writeShort(request, 2, 2);
                Wire.writeShort(request, 4, want[0]);
                Wire.writeShort(request, 6, want[1]);
                wire.send(request);
                byte[] message = wire.readMessage();
                while (message != null && (message[0] & 0xFF) != 1 && (message[0] & 0xFF) != 0) {
                    message = wire.readMessage();
                }
                if (message == null) {
                    System.out.println("XIQueryVersion " + want[0] + "." + want[1]
                            + " -> no answer");
                } else if ((message[0] & 0xFF) == 0) {
                    System.out.println("XIQueryVersion " + want[0] + "." + want[1] + " -> error "
                            + (message[1] & 0xFF) + " value " + Wire.readInt(message, 4));
                } else {
                    System.out.println("XIQueryVersion " + want[0] + "." + want[1] + " -> "
                            + Wire.readShort(message, 8) + "." + Wire.readShort(message, 10));
                }
            }
        }
    }

    /**
     * Selects the raw family on the root and drives XTEST, printing what comes back.
     *
     * <p>What a reference answers, measured 2026-10-04: an XTEST motion arrives as 72 bytes -
     * {@code evtype} 17, the master as the device, the XTEST slave as the source, two words of
     * valuator mask with both axes set, and the raw and processed values as four {@code FP3232}s -
     * a button and a key as 40 bytes with an empty mask, and <b>a warp as nothing at all</b>.
     */
    private static void rawEvents(Wire wire, int opcode) throws Exception {
        int xtest = queryExtensionNamed(wire, "XTEST");
        if (xtest == 0) {
            System.out.println("this server has no XTEST, so the probe cannot drive it");
            return;
        }
        byte[] select = new byte[20];
        select[0] = (byte) opcode;
        select[1] = SELECT_EVENTS;
        Wire.writeShort(select, 2, 5);
        Wire.writeInt(select, 4, wire.root());
        Wire.writeShort(select, 8, 1);                     // one mask record
        Wire.writeShort(select, 12, ALL_MASTER_DEVICES);
        Wire.writeShort(select, 14, 1);                    // one mask word
        Wire.writeInt(select, 16, RAW_MASK);
        wire.send(select);
        System.out.println("selected the raw mask 0x" + Integer.toHexString(RAW_MASK)
                + " on root 0x" + Integer.toHexString(wire.root()) + ", driving XTEST");
        // The input comes from this same connection: XTEST is scoped to the screen of the client
        // that sends it, and a server built for more than one screen puts another connection's
        // motion on a root this one never selected on (measured against this project's server).
        drive(wire, xtest, 6, 0, 220, 220, "motion");
        drive(wire, xtest, 4, 1, 0, 0, "button press");
        drive(wire, xtest, 5, 1, 0, 0, "button release");
        drive(wire, xtest, 2, 38, 0, 0, "key press");
        drive(wire, xtest, 3, 38, 0, 0, "key release");
        // A warp: the server moving the pointer rather than the device reporting it.
        byte[] warp = new byte[24];
        warp[0] = 41;                                      // WarpPointer
        Wire.writeShort(warp, 2, 6);
        Wire.writeInt(warp, 20, 400);
        wire.send(warp);
        drainRaw(wire, "warp");
    }

    /** One XTEST event and everything it makes the server send back. */
    private static void drive(Wire wire, int xtest, int type, int detail, int x, int y, String what)
            throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) xtest;
        request[1] = 2;                                    // X_XTestFakeInput
        request[2] = 9;
        request[4] = (byte) type;
        request[5] = (byte) detail;
        Wire.writeShort(request, 24, x);
        Wire.writeShort(request, 26, y);
        wire.send(request);
        Thread.sleep(80);
        drainRaw(wire, what);
    }

    private static void drainRaw(Wire wire, String what) throws Exception {
        wire.readTimeoutMillis(200);
        try {
            byte[] message;
            while ((message = wire.readMessage()) != null) {
                if ((message[0] & 0xFF) != 35) {
                    System.out.println("  " + what + " -> message type " + (message[0] & 0xFF));
                    continue;
                }
                byte[] whole = wire.withExtraData(message);
                System.out.println("  " + what + " -> evtype " + Wire.readShort(whole, 8)
                        + ", device " + Wire.readShort(whole, 10)
                        + ", detail " + Wire.readInt(whole, 16)
                        + ", sourceid " + Wire.readShort(whole, 20)
                        + ", valuators_len " + Wire.readShort(whole, 22)
                        + ", flags " + Wire.readInt(whole, 24) + ", " + whole.length + " bytes");
                hex("raw event", whole);
            }
        } finally {
            wire.readTimeoutMillis(3000);
        }
    }

    /**
     * {@code XIBarrierReleasePointer} (XI 2.3) for a barrier that does not exist, which is the only
     * answer this server can give and the one a reference gives too: <b>the extension's own error,
     * code 12</b>, with the barrier as the bad value (measured 2026-10-04: error 141 of a base of
     * 129, value 0x1234, minor 61).
     */
    private static void barrierRelease(Wire wire, int opcode, int barrier) throws Exception {
        byte[] request = new byte[20];
        request[0] = (byte) opcode;
        request[1] = 61;                                   // X_XIBarrierReleasePointer
        Wire.writeShort(request, 2, 5);
        Wire.writeInt(request, 4, 1);                      // one barrier
        Wire.writeShort(request, 8, 2);                    // deviceid: the pointer
        Wire.writeInt(request, 12, barrier);
        wire.send(request);
        byte[] message = wire.readMessage();
        if (message == null) {
            System.out.println("XIBarrierReleasePointer -> no answer");
        } else if ((message[0] & 0xFF) == 0) {
            System.out.println("XIBarrierReleasePointer 0x" + Integer.toHexString(barrier)
                    + " -> error " + (message[1] & 0xFF) + " value 0x"
                    + Integer.toHexString(Wire.readInt(message, 4)) + " minor "
                    + (message[8] & 0xFF) + " (the extension's first error is "
                    + (queryExtensionInfo(wire)[3]) + ")");
        } else {
            System.out.println("XIBarrierReleasePointer -> reply " + (message.length) + " bytes");
        }
    }

    /** {@code {present, majorOpcode, firstEvent, firstError}} for the extension, in one request. */
    private static int[] queryExtensionInfo(Wire wire) throws Exception {
        byte[] request = new byte[(8 + EXTENSION.length() + 3) & ~3];
        request[0] = 98;                                   // QueryExtension
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeShort(request, 4, EXTENSION.length());
        for (int i = 0; i < EXTENSION.length(); i++) {
            request[8 + i] = (byte) EXTENSION.charAt(i);
        }
        wire.send(request);
        byte[] reply = replyTo(wire, "QueryExtension");
        return new int[] {reply[8] & 0xFF, reply[9] & 0xFF, reply[10] & 0xFF, reply[11] & 0xFF};
    }

    /** {@code InternAtom}, for naming a property in a request built here. */
    private static int internAtom(Wire wire, String name) throws Exception {
        // The core InternAtom: header, the onlyIfExists byte, a two byte name length, two bytes of
        // padding, then the name at eight. (The length is at 4 and not at 8 - writing it at 8 puts
        // zero there, which interns "None" and answers atom 0, which then reads as a server that
        // refuses the property request.)
        byte[] request = new byte[(8 + name.length() + 3) & ~3];
        request[0] = 16;                            // InternAtom
        request[1] = 0;                             // onlyIfExists: no, make it
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeShort(request, 4, name.length());
        for (int i = 0; i < name.length(); i++) {
            request[8 + i] = (byte) name.charAt(i);
        }
        wire.send(request);
        return Wire.readInt(replyTo(wire, "InternAtom"), 8);
    }

    /**
     * XInput <em>1.x</em>'s {@code GetExtensionVersion}, which is not an XI2 request at all and is
     * still the first thing some clients send.
     *
     * <p>It was added here because getting it wrong is not a wrong answer, it is a crash:
     * {@code xinput list} asks it before anything else, and a server that answered with the fields
     * four bytes out made it <b>segfault</b> - the same shape as A4's {@code xdotool}, where a reply
     * a client did not survive was the whole bug. The reply's layout is read off a reference here
     * rather than from the protocol XML, because the XML puts the reply's own type byte among the
     * fields after the header where the wire keeps it in the header's second byte.
     */
    private static void legacyVersion(Wire wire, int opcode, boolean dump) throws Exception {
        byte[] request = new byte[(8 + EXTENSION.length() + 3) & ~3];
        request[0] = (byte) opcode;
        request[1] = 1;                             // GetExtensionVersion
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeShort(request, 4, EXTENSION.length());
        for (int i = 0; i < EXTENSION.length(); i++) {
            request[8 + i] = (byte) EXTENSION.charAt(i);
        }
        wire.send(request);
        byte[] reply = replyTo(wire, "GetExtensionVersion");
        System.out.println("GetExtensionVersion (XI 1.x): byte1=" + (reply[1] & 0xFF)
                + " words at 8=" + Wire.readShort(reply, 8) + " 10=" + Wire.readShort(reply, 10)
                + " 12=" + Wire.readShort(reply, 12)
                + " byte12=" + (reply[12] & 0xFF) + " byte13=" + (reply[13] & 0xFF));
        if (dump) {
            hex("GetExtensionVersion", reply);
        }
    }

    /**
     * XInput <em>1.x</em>'s {@code ListInputDevices}, which is the next thing the {@code xinput}
     * tool asks and which a server without it refuses - and a refusal under Xlib's default handler
     * ends the process, so a server that answers the version handshake and then refuses this is a
     * server {@code xinput} dies against rather than one it reports.
     *
     * <p>The reply is printed as bytes: its device records, their class lists and their names are
     * three separate lists in one reply, and the devices' class count is what sizes the second.
     */
    private static void listInputDevices(Wire wire, int opcode, boolean dump) throws Exception {
        byte[] request = new byte[4];
        request[0] = (byte) opcode;
        request[1] = 2;                             // ListInputDevices
        Wire.writeShort(request, 2, 1);             // length in four byte units, which is not optional
        wire.send(request);
        byte[] reply = replyTo(wire, "ListInputDevices");
        int devices = reply[8] & 0xFF;
        System.out.println("ListInputDevices: " + devices + " device(s), reply " + reply.length
                + " bytes");
        int at = 32;
        int classes = 0;
        for (int i = 0; i < devices && at + 8 <= reply.length; i++) {
            int type = Wire.readInt(reply, at);
            int id = reply[at + 4] & 0xFF;
            int numClassInfo = reply[at + 5] & 0xFF;
            int use = reply[at + 6] & 0xFF;
            classes += numClassInfo;
            System.out.println("  device id " + id + " use " + use + " type atom " + type
                    + " with " + numClassInfo + " class(es)");
            at += 8;
        }
        for (int i = 0; i < classes && at + 2 <= reply.length; i++) {
            System.out.println("    class " + (reply[at] & 0xFF) + " event base "
                    + (reply[at + 1] & 0xFF));
            at += 2;
        }
        if (dump) {
            hex("ListInputDevices", reply);
        }
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < devices && at + 1 < reply.length; i++) {
            int length = reply[at] & 0xFF;
            if (at + 1 + length > reply.length) {
                // The class list is walked with a count the devices give, and getting that count
                // wrong lands the reader in the middle of a name: a probe that then throws has
                // measured nothing at all, which is worse than printing one short name.
                System.out.println("  names run past the reply: the class count is off");
                break;
            }
            names.append('"').append(new String(reply, at + 1, length,
                    java.nio.charset.StandardCharsets.ISO_8859_1)).append("\" ");
            at += 1 + length;
        }
        System.out.println("  names " + names);
    }

    /** {@code QueryExtension XInputExtension}, answering the extension's major opcode or 0. */
    private static int queryExtension(Wire wire) throws Exception {
        byte[] request = new byte[(8 + EXTENSION.length() + 3) & ~3];
        request[0] = 98;
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeShort(request, 4, EXTENSION.length());
        for (int i = 0; i < EXTENSION.length(); i++) {
            request[8 + i] = (byte) EXTENSION.charAt(i);
        }
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                throw new java.io.IOException("no answer to QueryExtension");
            }
            if ((message[0] & 0xFF) == 1) {
                // The reply is present, major-opcode, first-event, first-error - the opcode is the
                // *second* byte of the four, which is worth saying out loud because reading the
                // first one answers "1" and every request afterwards is then BadLength.
                if ((message[8] & 0xFF) == 0) {
                    return 0;
                }
                return message[9] & 0xFF;
            }
        }
    }

    private static void queryVersion(Wire wire, int opcode, boolean dump) throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = QUERY_VERSION;
        Wire.writeShort(request, 2, 2);
        Wire.writeShort(request, 4, WANTED_MAJOR);
        Wire.writeShort(request, 6, WANTED_MINOR);
        wire.send(request);
        byte[] reply = replyTo(wire, "XIQueryVersion");
        System.out.println("XIQueryVersion asked " + WANTED_MAJOR + "." + WANTED_MINOR
                + " -> " + Wire.readShort(reply, 8) + "." + Wire.readShort(reply, 10));
        if (dump) {
            hex("XIQueryVersion", reply);
        }
    }

    /**
     * {@code XIGetClientPointer}: which device this client's pointer events come from.
     *
     * <p><b>A reference server answers {@code BadWindow} for the root window</b> - measured, minor 45
     * with bad value {@code 0x1ff} - so the tool asks about a window it created rather than about the
     * root, which is what a client asks about anyway. Asking about the root here and reading the
     * error as "this server has no XInput2" is a wrong answer with a plausible shape.
     */
    private static void getClientPointer(Wire wire, int opcode, int window, boolean dump)
            throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = GET_CLIENT_POINTER;
        Wire.writeShort(request, 2, 2);
        Wire.writeInt(request, 4, window);
        wire.send(request);
        byte[] reply = replyTo(wire, "XIGetClientPointer");
        System.out.println("XIGetClientPointer on 0x" + Integer.toHexString(window)
                + " -> set=" + (reply[8] & 0xFF) + " device=" + Wire.readShort(reply, 10));
        if (dump) {
            hex("XIGetClientPointer", reply);
        }
    }

    private static void queryDevice(Wire wire, int opcode, int deviceId, boolean dump)
            throws Exception {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = QUERY_DEVICE;
        Wire.writeShort(request, 2, 2);
        Wire.writeShort(request, 4, deviceId);
        wire.send(request);
        // replyTo has already read the reply whole, list and all.
        byte[] reply = replyTo(wire, "XIQueryDevice");
        int count = Wire.readShort(reply, 8);
        System.out.println("XIQueryDevice " + deviceId + " -> " + count + " device(s), reply "
                + reply.length + " bytes");
        if (dump) {
            hex("XIQueryDevice " + deviceId, reply);
        }
        int at = 32;
        for (int i = 0; i < count && at + 12 <= reply.length; i++) {
            at = deviceInfo(reply, at);
        }
    }

    /**
     * One {@code XIDeviceInfo}, printing what it holds and answering where the next one starts.
     *
     * <p>The record is twelve bytes of fixed fields, the name, padding to a four byte boundary, then
     * its classes; each class is four bytes of its own followed by a body whose length the class
     * states.
     */
    private static int deviceInfo(byte[] reply, int at) {
        int id = Wire.readShort(reply, at);
        int type = Wire.readShort(reply, at + 2);
        int attachment = Wire.readShort(reply, at + 4);
        int classes = Wire.readShort(reply, at + 6);
        int nameLength = Wire.readShort(reply, at + 8);
        int enabled = reply[at + 10] & 0xFF;
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < nameLength; i++) {
            name.append((char) (reply[at + 12 + i] & 0xFF));
        }
        int body = at + 12 + ((nameLength + 3) & ~3);
        System.out.println("  device " + id + " '" + name + "' type " + type + " ("
                + deviceType(type) + ") attached to " + attachment + " enabled " + enabled + ": "
                + classes + " class(es)");
        int classAt = body;
        for (int c = 0; c < classes && classAt + 4 <= reply.length; c++) {
            int classType = Wire.readShort(reply, classAt);
            int length = Wire.readShort(reply, classAt + 2);
            int sourceId = Wire.readShort(reply, classAt + 4);
            StringBuilder detail = new StringBuilder();
            // Every class body starts at the sixth byte: type, length and sourceid are the four plus
            // two the header is made of. Reading the first field two bytes late is not a small error
            // here - a key class then reports its *first keycode* as the number of keys.
            if (classType == 0) {
                int keys = Wire.readShort(reply, classAt + 6);
                detail.append("num_keys ").append(keys).append(", keycodes ")
                        .append(Wire.readInt(reply, classAt + 8)).append("..")
                        .append(Wire.readInt(reply, classAt + 8 + (keys - 1) * 4));
            } else if (classType == 1) {
                int buttons = Wire.readShort(reply, classAt + 6);
                detail.append("num_buttons ").append(buttons);
            } else if (classType == 2) {
                int number = Wire.readShort(reply, classAt + 6);
                int label = Wire.readInt(reply, classAt + 8);
                int value = Wire.readInt(reply, classAt + 28);
                detail.append("number ").append(number).append(", label ").append(label)
                        .append(", value ").append(value);
            } else if (classType == 3) {
                int number = Wire.readShort(reply, classAt + 6);
                int scrollType = Wire.readShort(reply, classAt + 8);
                detail.append("number ").append(number).append(", scroll_type ").append(scrollType);
            }
            System.out.println("      class " + classType + " (" + className(classType) + ") len "
                    + length + " sourceid " + sourceId + ": " + detail);
            // The class records are variable length but never shorter than the body they declare.
            classAt += Math.max(8, length * 4);
        }
        return Math.max(body, classAt);
    }

    private static String deviceType(int type) {
        return switch (type) {
            case 1 -> "master pointer";
            case 2 -> "master keyboard";
            case 3 -> "slave pointer";
            case 4 -> "slave keyboard";
            case 5 -> "floating slave";
            default -> "?";
        };
    }

    /** The XI2 event names, numbered as the extension numbers them. */
    private static String xiEventName(int evtype) {
        return switch (evtype) {
            case 1 -> "DeviceChanged";
            case 2 -> "KeyPress";
            case 3 -> "KeyRelease";
            case 4 -> "ButtonPress";
            case 5 -> "ButtonRelease";
            case 6 -> "Motion";
            case 7 -> "Enter";
            case 8 -> "Leave";
            case 9 -> "FocusIn";
            case 10 -> "FocusOut";
            case 11 -> "Hierarchy";
            case 12 -> "Property";
            case 13 -> "RawKeyPress";
            case 14 -> "RawKeyRelease";
            case 15 -> "RawButtonPress";
            case 16 -> "RawButtonRelease";
            case 17 -> "RawMotion";
            default -> "?";
        };
    }

    /** An {@code FP1616} fixed point value, which is how an XI2 event carries a coordinate. */
    private static String fixed(int value) {
        return (value >> 16) + "." + String.format("%04d", ((value & 0xFFFF) * 10000) >> 16);
    }

    /** One XI2 event as a line, read with the offsets the header is known to use. */
    private static String describeXi2(byte[] message) {
        return "GenericEvent ext " + (message[1] & 0xFF)
                + " evtype " + Wire.readShort(message, 8) + " (" + xiEventName(
                        Wire.readShort(message, 8)) + ")"
                + " device " + Wire.readShort(message, 10)
                + " time " + Wire.readInt(message, 12)
                + " detail " + Wire.readInt(message, 16)
                + " root 0x" + Integer.toHexString(Wire.readInt(message, 20))
                + " event 0x" + Integer.toHexString(Wire.readInt(message, 24))
                + " child 0x" + Integer.toHexString(Wire.readInt(message, 28))
                + " at " + fixed(Wire.readInt(message, 32)) + ","
                + fixed(Wire.readInt(message, 36))
                + " in-window " + fixed(Wire.readInt(message, 40)) + ","
                + fixed(Wire.readInt(message, 44))
                + " source " + Wire.readShort(message, 52)
                // The keyboard state, which is what a client reads a Ctrl+A out of. A device event
                // packs it as four bytes at 60-63 (base, latched, locked, effective) where the two
                // shorter shapes spend four CARD32 on it; measured against a reference.
                + " mods base=" + (message[60] & 0xFF)
                + " latched=" + (message[61] & 0xFF)
                + " locked=" + (message[62] & 0xFF)
                + " effective=" + (message[63] & 0xFF)
                // The buttons the device is holding, which is what a click is read from: a device
                // event keeps its button mask at 80 (its fixed part is eighty bytes), where the two
                // shorter shapes have none. Which of the two halves of a click carries the button is
                // what the two servers are being compared for.
                + " buttons 0x" + Integer.toHexString(Wire.readInt(message, 80))
                + " bytes " + message.length;
    }

    private static String className(int type) {
        return switch (type) {
            case 0 -> "key";
            case 1 -> "button";
            case 2 -> "valuator";
            case 3 -> "scroll";
            case 8 -> "touch";
            default -> "?";
        };
    }

    /**
     * Subscribes to XI2 events on a window, drives the pointer into it with XTEST and prints every
     * message that arrives <b>as bytes as well as parsed</b>.
     *
     * <p>This is the half of XI2 a client actually sees. An XI2 event is a core {@code GenericEvent}
     * (type 35) whose second byte is the extension's opcode and whose {@code evtype} names the XI2
     * event - so "the extension is advertised" says nothing about whether a client can receive one,
     * which is the shape of gap this kit exists to find.
     *
     * <p>Three things here were measured by getting them wrong first, and all three are traps for
     * anyone implementing XI2 rather than using it: the <b>mask bits</b> are not the core ones shifted
     * the way they look ({@code 1<<3} selects nothing where {@code 1<<4} is {@code ButtonPress}); a
     * {@code GenericEvent} is read whole or not at all; and the pointer has to be warped to the window
     * the events are selected on, not to the root near it.
     */
    private static void selectEvents(Wire wire, int opcode, int mask, int device, boolean onRoot,
                                     boolean driveKey) throws Exception {
        int xtest = queryExtensionNamed(wire, "XTEST");
        if (xtest == 0) {
            System.out.println("this server has no XTEST, so the probe cannot drive it");
            return;
        }
        int window = onRoot ? wire.root() : createWindow(wire);
        int warpX = onRoot ? 400 : 20;
        int warpY = onRoot ? 400 : 20;

        // XISelectEvents: the window, the number of EventMask records, then one record of
        // deviceid, mask_len and the mask words.
        byte[] select = new byte[20];
        select[0] = (byte) opcode;
        select[1] = SELECT_EVENTS;
        Wire.writeShort(select, 2, 5);
        Wire.writeInt(select, 4, window);
        Wire.writeShort(select, 8, 1);              // one EventMask record
        Wire.writeShort(select, 10, 0);             // pad
        Wire.writeShort(select, 12, device);
        Wire.writeShort(select, 14, 1);             // mask_len, in four byte words
        Wire.writeInt(select, 16, mask);
        wire.send(select);
        System.out.println("selected mask 0x" + Integer.toHexString(mask) + " device " + device
                + " on 0x" + Integer.toHexString(window) + (onRoot ? " (the root)" : ""));

        // The core masks as well, so the same gesture can be read on both rails: a quiet XI2 run
        // means "no XI2 event" while a quiet run on both means "the gesture never happened", and
        // only having both in one output tells the two apart.
        byte[] maskRequest = new byte[16];
        maskRequest[0] = 2;                         // ChangeWindowAttributes
        Wire.writeShort(maskRequest, 2, 4);
        Wire.writeInt(maskRequest, 4, window);
        Wire.writeInt(maskRequest, 8, 0x800);       // CWEventMask
        Wire.writeInt(maskRequest, 12, 0x0C);       // ButtonPressMask | ButtonReleaseMask
        wire.send(maskRequest);

        // The pointer has to be inside the window for it to be the event's target, and "inside"
        // means relative to the window it is warped to: warping to the root at 20,20 leaves the
        // pointer outside a window that starts at 100,100, and the server then has every reason to
        // deliver nothing at all.
        byte[] warp = new byte[24];
        warp[0] = 41;                               // WarpPointer
        Wire.writeShort(warp, 2, 6);
        Wire.writeInt(warp, 4, 0);                  // src: none
        Wire.writeInt(warp, 8, window);             // dst
        Wire.writeShort(warp, 16, warpX);           // dst x
        Wire.writeShort(warp, 18, warpY);           // dst y
        wire.send(warp);

        // Say where the pointer actually is before driving it: a quiet run has two very different
        // causes - the device event was not delivered, or the gesture never happened where the
        // probe thought it did - and this is the line that tells them apart.
        byte[] query = new byte[8];
        query[0] = 38;                              // QueryPointer
        Wire.writeShort(query, 2, 2);
        Wire.writeInt(query, 4, wire.root());
        wire.send(query);
        byte[] answer = replyTo(wire, "QueryPointer");
        System.out.println("pointer is over 0x"
                + Integer.toHexString(Wire.readInt(answer, 12))
                + " at " + Wire.readShort(answer, 16) + "," + Wire.readShort(answer, 18));

        // And XTEST has to drive the device, because a hand cannot.
        if (driveKey) {
            // Keycode 38 is 'a' on the standard layout, and the window has to have the input focus
            // for the keyboard to deliver anything at all.
            setFocus(wire, window);
            fakeKey(wire, xtest, 38, true);
            fakeKey(wire, xtest, 38, false);
        } else {
            fakeButton(wire, xtest, 1, true);
            fakeButton(wire, xtest, 1, false);
        }

        wire.readTimeoutMillis(400);
        for (int i = 0; i < 12; i++) {
            byte[] message = wire.readMessage();
            if (message == null) {
                System.out.println("(quiet)");
                break;
            }
            int type = message[0] & 0xFF;
            // A reply and a GenericEvent carry a length field; a core event does not - and its bytes
            // four to seven are the *timestamp*, which read as a length is a wait for data that will
            // never come. That is how a probe that extends every message hangs on a ButtonPress.
            if (type == 1 || type == 35) {
                message = wire.withExtraData(message);
            }
            if (type == 35) {
                // The generic header is twelve bytes - type, extension, sequence, length, evtype,
                // deviceid - and the XI2 body starts at the timestamp. There is *no* 32 bit full
                // sequence on the wire here, which is worth saying out loud: assuming one (XGE's
                // other events do have it) puts every field four bytes late, and the timestamp then
                // reads as 1 and the detail as the root window's id. Measured against a reference:
                // the fields up to `child` end on the thirty two byte boundary the protocol's own
                // notes mention.
                System.out.println("GenericEvent ext " + (message[1] & 0xFF)
                        + " evtype " + Wire.readShort(message, 8) + " (" + xiEventName(
                                Wire.readShort(message, 8)) + ")"
                        + " device " + Wire.readShort(message, 10)
                        + " time " + Wire.readInt(message, 12)
                        + " detail " + Wire.readInt(message, 16)
                        + " root 0x" + Integer.toHexString(Wire.readInt(message, 20))
                        + " event 0x" + Integer.toHexString(Wire.readInt(message, 24))
                        + " child 0x" + Integer.toHexString(Wire.readInt(message, 28))
                        + " at " + fixed(Wire.readInt(message, 32)) + ","
                        + fixed(Wire.readInt(message, 36))
                        + " in-window " + fixed(Wire.readInt(message, 40)) + ","
                        + fixed(Wire.readInt(message, 44))
                        + " source " + Wire.readShort(message, 52)
                        + " bytes " + message.length);
                hex("GenericEvent", java.util.Arrays.copyOf(message, Math.min(96, message.length)));
            } else {
                System.out.println("core event type " + type + " bytes " + message.length);
                hex("event", message);
            }
        }
    }

    /**
     * {@code XTEST FakeInput}: press or release a button, which is how a probe drives the pointer.
     *
     * <p>Two things about this request read differently than they look, and both were measured by
     * being sent wrong: the request is **thirty six bytes** and wants a length of 9, not the sixteen
     * a reading of its fixed fields suggests - a short one is answered {@code BadLength} under an
     * extension major opcode, which reads as "XTEST is not working" - and the {@code type} field is
     * a **core event type number** (4 for ButtonPress, 5 for ButtonRelease, 6 for MotionNotify),
     * not a numbering of XTEST's own.
     */
    private static void fakeButton(Wire wire, int xtest, int button, boolean pressed)
            throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) xtest;
        request[1] = 2;                             // XTEST FakeInput
        request[2] = 9;
        request[4] = (byte) (pressed ? 4 : 5);      // core ButtonPress / ButtonRelease
        request[5] = (byte) button;
        wire.send(request);
    }

    /** Creates and maps a 200x200 window at 100,100 and answers its id. */
    private static int createWindow(Wire wire) throws Exception {
        int window = wire.allocateId();
        byte[] create = new byte[32];
        create[0] = 1;                              // CreateWindow
        create[1] = 24;                             // depth
        Wire.writeShort(create, 2, 8);
        Wire.writeInt(create, 4, window);
        Wire.writeInt(create, 8, wire.root());
        Wire.writeShort(create, 12, 100);
        Wire.writeShort(create, 14, 100);
        Wire.writeShort(create, 16, 200);
        Wire.writeShort(create, 18, 200);
        wire.send(create);
        byte[] map = new byte[8];
        map[0] = 8;                                 // MapWindow
        Wire.writeShort(map, 2, 2);
        Wire.writeInt(map, 4, window);
        wire.send(map);
        return window;
    }

    /** {@code SetInputFocus} on a window, so that key events have somewhere to be delivered. */
    private static void setFocus(Wire wire, int window) throws Exception {
        byte[] request = new byte[12];
        request[0] = 42;                            // SetInputFocus
        Wire.writeShort(request, 2, 3);
        Wire.writeInt(request, 4, window);
        request[8] = 1;                             // revert to PointerRoot
        wire.send(request);
    }

    /** XTEST FakeInput for a key: the type is the core event type, and the detail a keycode. */
    private static void fakeKey(Wire wire, int xtest, int keycode, boolean pressed)
            throws Exception {
        byte[] request = new byte[36];
        request[0] = (byte) xtest;
        request[1] = 2;
        request[2] = 9;
        request[4] = (byte) (pressed ? 2 : 3);      // core KeyPress / KeyRelease
        request[5] = (byte) keycode;
        wire.send(request);
    }

    private static int queryExtensionNamed(Wire wire, String name) throws Exception {
        byte[] request = new byte[(8 + name.length() + 3) & ~3];
        request[0] = 98;
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeShort(request, 4, name.length());
        for (int i = 0; i < name.length(); i++) {
            request[8 + i] = (byte) name.charAt(i);
        }
        wire.send(request);
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                throw new java.io.IOException("no answer to QueryExtension " + name);
            }
            if ((message[0] & 0xFF) == 1) {
                return (message[8] & 0xFF) == 0 ? 0 : (message[9] & 0xFF);
            }
        }
    }

    /**
     * Reads messages until the reply to the request just sent turns up, dropping events on the way.
     *
     * <p>An event passed over has to be read <em>whole</em>: a {@code GenericEvent}'s length field
     * says how much of it follows, and reading only its first thirty two bytes leaves the rest in the
     * stream, so the reply that comes next is read from the wrong offset and every field in it is
     * garbage. Measured - with a motion mask selected, the warp generates an XI2 {@code Motion}, and
     * the {@code QueryPointer} reply behind it decoded as {@code error 0, bad value 0x640000}, which
     * is the event's own {@code root_x}.
     */
    private static byte[] replyTo(Wire wire, String what) throws Exception {
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                throw new java.io.IOException("no answer to " + what);
            }
            if ((message[0] & 0xFF) == 35 || (message[0] & 0xFF) == 1) {
                message = wire.withExtraData(message);
            }
            if ((message[0] & 0xFF) == 35) {
                // **An event passed over is printed, not swallowed.** The kit's own A6 lesson: a
                // probe whose reply reader drops what it walks past produces a measurement that is
                // wrong in a way nothing else shows - and a motion generated by the warp below is
                // exactly the event that goes missing.
                System.out.println("(while waiting for " + what + ") " + describeXi2(message));
                hex("GenericEvent", message);
            }
            if ((message[0] & 0x7F) == 1) {
                return message;
            }
            if ((message[0] & 0x7F) == 0) {
                // An error names the request it is about: minor opcode at 8, major at 10. Printing
                // the error itself rather than the zeroed reply it stands in for is what says which
                // value the server objected to.
                System.out.println(what + " -> error " + (message[1] & 0xFF)
                        + " bad value 0x" + Integer.toHexString(Wire.readInt(message, 4))
                        + " on major " + (message[10] & 0xFF)
                        + " minor " + Wire.readShort(message, 8));
                hex("error", message);
                return new byte[32];
            }
        }
    }

    private static void hex(String what, byte[] bytes) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i % 16 == 0) {
                if (line.length() > 0) {
                    System.out.println("    " + line);
                }
                line = new StringBuilder();
                line.append(String.format("%04x  ", i));
            }
            line.append(String.format("%02x ", bytes[i] & 0xFF));
        }
        if (line.length() > 0) {
            System.out.println("    " + line);
        }
    }
}
