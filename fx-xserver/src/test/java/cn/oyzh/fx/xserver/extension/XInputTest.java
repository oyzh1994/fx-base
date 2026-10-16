package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.event.Xi2Event;
import cn.oyzh.fx.xserver.test.RawXClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * XInput2 - the extension whose absence is a fallback and whose half-implementation is a trap.
 *
 * <p>What is pinned here is what a client reads, and every offset in it was measured against Xvfb
 * with {@code tools/XiProbe} rather than derived from the protocol XML, because the XML is wrong in
 * one place and the wrongness is invisible: <b>the fixed part of a device event is eighty bytes, not
 * the sixty eight its field list adds up to</b>, and an event written to sixty eight is fifty two
 * bytes too short. A reference sends a hundred and twenty for a key press and a hundred and thirty
 * six for a motion with two axes, which is exactly what eighty plus the two masks comes to - and GDK,
 * told the wrong size, does not refuse the event: it reads the key code and the pointer position out
 * of the middle of it and the application quietly stops producing characters.
 *
 * <p>Two of the assertions below are about requests that are not answered at all, which is the other
 * half of the same lesson and was also measured by getting it wrong: a request <em>with</em> a reply
 * that is accepted in silence hangs the client (the XInput 1.x handshake, which is what `xinput` asks
 * first), and a request <em>without</em> one that is answered shifts the client's whole stream (the
 * cursor change GDK sends when a text field is focused, after which it asked for the pointer position
 * a million times in two minutes).
 */
class XInputTest {

    /** A display number the rest of the suite does not use. */
    private static final int DISPLAY = 68;

    private static final int GET_EXTENSION_VERSION = 1;
    private static final int XI_CHANGE_CURSOR = 42;
    private static final int XI_GET_CLIENT_POINTER = 45;
    private static final int XI_SELECT_EVENTS = 46;
    private static final int XI_QUERY_VERSION = 47;
    private static final int XI_QUERY_DEVICE = 48;
    private static final int XI_QUERY_POINTER = 40;
    private static final int XI_LIST_PROPERTIES = 56;
    private static final int XI_GET_PROPERTY = 59;
    private static final int XI_PASSIVE_GRAB_DEVICE = 54;

    /**
     * {@code XI_ButtonPress | XI_ButtonRelease}: measured to be {@code 1<<4 | 1<<5}, not the core
     * masks' {@code 1<<2 | 1<<3}.
     *
     * <p>Both, because that is what a toolkit selects - GDK asks for the two together with motion
     * and the crossings - and because a client that selects only one of a pair is a case with no
     * right answer: the release it did not select arrives on the core rail.
     */
    private static final int XI_BUTTONS = (1 << 4) | (1 << 5);

    private static XServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new XServer(ServerOptions.parse(new String[]{"--display", ":" + DISPLAY,
                "--geometry", "600x400", "--backend", "headless", "--no-unix"}));
        server.start();
        server.startEventLoop();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void theOldHandshakeIsAnsweredWhereTheClientReadsIt() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            assertNotEquals(0, opcode, "a client that asks has to find it");
            byte[] reply = request(client, opcode, GET_EXTENSION_VERSION, 0, 0);
            // The reply's own type byte is the header's data byte, and the fields follow from offset
            // eight - four words earlier than a reply written like XKB's GetKbdByName, which is what
            // this did first. With them four out, `present` reads as zero and `xinput list` segfaults.
            assertEquals(1, reply[1] & 0xFF, "the reply type belongs in the header's second byte");
            assertEquals(2, RawXClient.readShort(reply, 8), "server_major");
            // **2.4 since A47**: the minor a reference answers, because this server now answers the
            // surfaces 2.1 to 2.4 add (raw events, the barrier release, no touch device anywhere in
            // this bed). It used to say 2.0 for exactly one reason - "a client told 2.4 will ask for
            // requests that would be refused" - and that reason is what the rest of this file now
            // pins as answered.
            assertEquals(4, RawXClient.readShort(reply, 10), "server_minor: what a reference answers");
            assertEquals(1, reply[12] & 0xFF, "present");
        }
    }

    @Test
    void itNamesTheDevicesAReferenceNames() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            assertEquals(4, RawXClient.readShort(version(client, opcode, 2, 4), 10), "2.4, as a"
                    + " reference answers");

            byte[] devices = request(client, opcode, XI_QUERY_DEVICE, 0);
            assertEquals(6, RawXClient.readShort(devices, 8),
                    "the two masters and their two slave pairs: the XTEST one and the host's, which"
                            + " is where a reference keeps its backend's (A48)");
            // The records are positional and the classes carry their own lengths. The first device
            // is the pointer at 32, and its name follows the twelve byte header; the keyboard's name
            // is the second record's, which starts after the pointer's three classes.
            assertEquals(2, RawXClient.readShort(devices, 32), "the pointer's device id");
            assertEquals(1, RawXClient.readShort(devices, 34), "MasterPointer");
            assertEquals(3, RawXClient.readShort(devices, 36), "attached to the keyboard");
            assertEquals(3, RawXClient.readShort(devices, 38), "a button class and two axes");
            assertEquals("Virtual core pointer".length(), RawXClient.readShort(devices, 40));

            byte[] masters = request(client, opcode, XI_QUERY_DEVICE, 1);   // XIAllMasterDevices
            assertEquals(2, RawXClient.readShort(masters, 8),
                    "the selector for masters alone leaves the slaves out, as a reference does");
        }
    }

    /**
     * The XTEST slaves are real devices here since A48, and the input they generate says so.
     *
     * <p>Measured on Xvfb 2026-10-04: {@code Virtual core XTEST pointer} is device 4 attached to 2
     * and {@code Virtual core XTEST keyboard} is 5 attached to 3, and an XTEST-driven raw motion
     * arrives as {@code device 2, sourceid 4} - the master as the device, the slave that produced
     * the input as its source. This server listed two devices and named the master as the source of
     * everything before that, which is the difference this closes.
     */
    @Test
    void theXtestSlavesAreListedAndTheirInputSaysSo() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            int xtest = client.queryExtension("XTEST");
            version(client, opcode, 2, 4);
            byte[] devices = request(client, opcode, XI_QUERY_DEVICE, 4);
            assertEquals(1, RawXClient.readShort(devices, 8), "one device");
            assertEquals(4, RawXClient.readShort(devices, 32), "the XTEST pointer's id");
            assertEquals(3, RawXClient.readShort(devices, 34), "SlavePointer");
            assertEquals(2, RawXClient.readShort(devices, 36), "attached to the core pointer");
            assertEquals("Virtual core XTEST pointer".length(), RawXClient.readShort(devices, 40),
                    "the name a reference gives it, so a client that caches ids sees the same tree");
            // The class records of a *master* name the slave the class comes from - device 2's
            // button class is the XTEST pointer's, which is what a reference answers.
            byte[] pointer = request(client, opcode, XI_QUERY_DEVICE, 2);
            assertEquals(4, RawXClient.readShort(pointer, 32 + 12 + 20 + 4),
                    "the master's first class belongs to the slave");
            // And the XI 1 list has them too: AWT reads its device model from that one.
            byte[] legacy = client.extensionRequest(opcode, 2);
            assertEquals(6, legacy[8] & 0xFF, "ListInputDevices lists the same six");

            // A device event driven by XTEST carries the slave as its source id (offset 52 in the
            // device shape), where it used to name the master.
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 200, 200, 0x00FF00);
            client.mapWindow(window);
            client.flush();
            byte[] select = new byte[20];
            select[0] = (byte) opcode;
            select[1] = XI_SELECT_EVENTS;
            RawXClient.writeShortAt(select, 2, 5);
            RawXClient.writeIntAt(select, 4, window);
            RawXClient.writeShortAt(select, 8, 1);
            RawXClient.writeShortAt(select, 12, 2);          // the pointer device
            RawXClient.writeShortAt(select, 14, 1);
            RawXClient.writeIntAt(select, 16, 1 << 6);       // XI_Motion
            client.sendRequest(select);
            client.fakeMotion(xtest, 40, 40);
            client.flush();
            byte[] motion = null;
            for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
                if ((event[0] & 0xFF) == RawXClient.GENERIC_EVENT
                        && (event[1] & 0xFF) == opcode
                        && RawXClient.readShort(event, 8) == 6) {
                    motion = event;
                    break;
                }
            }
            assertNotNull(motion, "the device motion has to arrive");
            assertEquals(2, RawXClient.readShort(motion, 10), "the device is still the master");
            assertEquals(4, RawXClient.readShort(motion, 52),
                    "and the source is the XTEST slave that produced it");
        }
    }

    /**
     * The version negotiation, which is the lower of the two versions and never a lie - and
     * {@code BadValue} for a major below 2, which is not a negotiation at all.
     *
     * <p>Measured against Xvfb 2026-10-04, one connection per ask: 2.0 -> 2.0, 2.3 -> 2.3, 2.5 ->
     * 2.4, 3.0 -> 2.4 (a client whose major is ahead is told what this server speaks and decides
     * for itself), and 1.0 -> error 2 with the requested major as its bad value.
     */
    @Test
    void theVersionIsTheLowerOfTheTwoAndNeverALie() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            assertEquals(0, RawXClient.readShort(version(client, opcode, 2, 0), 10),
                    "a client that asks for less is answered what it asked for");
            assertEquals(3, RawXClient.readShort(version(client, opcode, 2, 3), 10),
                    "and a client that asks for 2.3 gets 2.3");
            assertEquals(4, RawXClient.readShort(version(client, opcode, 2, 9), 10),
                    "a minor ahead of this server's is capped at 2.4, as a reference caps it");
            byte[] ahead = version(client, opcode, 3, 0);
            assertEquals(2, RawXClient.readShort(ahead, 8), "a major ahead is told this server's");
            assertEquals(4, RawXClient.readShort(ahead, 10), "with the minor it really has, not 0");

            // A major below 2 is BadValue, and the value it names is the version asked for.
            byte[] request = new byte[8];
            request[0] = (byte) opcode;
            request[1] = XI_QUERY_VERSION;
            RawXClient.writeShortAt(request, 2, 2);
            RawXClient.writeShortAt(request, 4, 1);
            RawXClient.writeShortAt(request, 6, 0);
            client.sendRequest(request);
            client.flush();
            byte[] error = client.readEvent();
            assertNotNull(error, "the server has to answer");
            assertEquals(0, error[0] & 0xF, "an error, not a reply");
            assertEquals(2, error[1] & 0xFF, "BadValue");
            assertEquals(1, RawXClient.readInt(error, 4), "the version that was asked for");
        }
    }

    /**
     * A raw motion (XI 2.1) reaches the client that selected it on the root, in the shape a
     * reference sends - and a {@code WarpPointer} produces none at all.
     *
     * <p>Measured against Xvfb 2026-10-04 with {@code tools/XiProbe raw}: the event is 72 bytes, its
     * fixed part is 32 (it names no window at all), {@code valuators_len} is <b>two words</b> even
     * for the keyboard, the mask has both axes, and behind it come the raw values and the processed
     * ones - two {@code FP3232} pairs. A warp sends nothing: that is the server moving the pointer,
     * not the device reporting it.
     */
    @Test
    void aRawMotionReachesARootSelectionInTheShapeAReferenceSends() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            int xtest = client.queryExtension("XTEST");
            version(client, opcode, 2, 4);
            selectRaw(client, opcode, 1 << 17);          // XI_RawMotion, on the root

            // **The input has to come from this client**: XTEST is scoped to the screen of the
            // connection that sends it, and a server with a screen per client puts a second
            // connection's faked motion on a root this one never selected on (measured: the
            // selection was on 0x9 while the motion landed on the screen root 0xc).
            client.fakeMotion(xtest, 100, 200);
            client.flush();

            byte[] raw = null;
            for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
                if ((event[0] & 0xFF) == RawXClient.GENERIC_EVENT
                        && (event[1] & 0xFF) == opcode
                        && RawXClient.readShort(event, 8) == 17) {
                    raw = event;
                    break;
                }
            }
            assertNotNull(raw, "the raw motion has to arrive");
            assertEquals(72, raw.length, "32 bytes of fixed part, two words of mask, four values");
            assertEquals(10, RawXClient.readInt(raw, 4), "length is the size in words, less eight");
            assertEquals(2, RawXClient.readShort(raw, 10), "the pointer, as the device a reference"
                    + " names here");
            assertEquals(0, RawXClient.readInt(raw, 16), "a motion's detail is nought");
            assertEquals(2, RawXClient.readShort(raw, 22), "valuators_len: two words, measured");
            assertEquals(0x03, RawXClient.readInt(raw, 32), "both axes are in this event");
            assertEquals(100, RawXClient.readInt(raw, 40), "x, as FP3232");
            assertEquals(200, RawXClient.readInt(raw, 48), "y");
            assertEquals(100, RawXClient.readInt(raw, 56), "and the processed x: this server's"
                    + " pointer does not accelerate, so the two pairs are the same numbers");
            assertEquals(200, RawXClient.readInt(raw, 64), "processed y");

            // **A warp is not a device event**: measured on a reference, where six XTEST motions
            // arrive and a WarpPointer sends nothing.
            client.warpPointer(client.root(), 300, 300);
            client.flush();
            assertNull(client.readEvent(), "a warp must not be reported as the device's own event");
        }
    }

    /** A raw key is the same 40-byte shape with no axis in it: an empty mask and the keycode. */
    @Test
    void aRawKeyIsTheShortShapeWithItsKeycode() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            int xtest = client.queryExtension("XTEST");
            version(client, opcode, 2, 4);
            selectRaw(client, opcode, 1 << 13);          // XI_RawKeyPress, on the root

            client.fakeKey(xtest, 38, true);
            client.flush();

            byte[] raw = null;
            for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
                if ((event[0] & 0xFF) == RawXClient.GENERIC_EVENT
                        && (event[1] & 0xFF) == opcode
                        && RawXClient.readShort(event, 8) == 13) {
                    raw = event;
                    break;
                }
            }
            assertNotNull(raw, "the raw key has to arrive");
            assertEquals(40, raw.length, "the fixed part and two words of mask, and nothing else");
            assertEquals(2, RawXClient.readInt(raw, 4), "length in words, less eight");
            assertEquals(3, RawXClient.readShort(raw, 10), "the keyboard is the device");
            assertEquals(38, RawXClient.readInt(raw, 16), "the keycode is the detail");
            assertEquals(2, RawXClient.readShort(raw, 22), "valuators_len: two words even for a"
                    + " device with no axes, measured");
            assertEquals(0, RawXClient.readInt(raw, 32), "and the mask is empty: no axis is in it");
        }
    }

    /**
     * The barrier release (XI 2.3) is refused with <b>the extension's own barrier error</b>, which
     * is what a reference answers for a barrier that does not exist - and the error has to be
     * inside the range this extension declared, or a client cannot name it.
     *
     * <p>Measured against Xvfb 2026-10-04: {@code XIBarrierReleasePointer} for a made-up barrier
     * answers {@code firstError + 12} with the barrier as the bad value and 61 as the minor opcode.
     * This server answered {@code BadRequest}, which tells a client its request was malformed when
     * it was fine - and its extension then declared a single error code, so code 12 landed outside
     * the range a client had been told about.
     */
    @Test
    void theBarrierReleaseIsTheExtensionsOwnError() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int[] info = client.queryExtensionInfo("XInputExtension");
            int opcode = info[1];
            int firstError = info[3];
            version(client, opcode, 2, 4);
            byte[] barrier = new byte[20];
            barrier[0] = (byte) opcode;
            barrier[1] = 61;                            // XIBarrierReleasePointer
            RawXClient.writeShortAt(barrier, 2, 5);
            RawXClient.writeIntAt(barrier, 4, 1);       // one barrier
            RawXClient.writeShortAt(barrier, 8, 2);     // the pointer
            RawXClient.writeIntAt(barrier, 12, 0x1234);
            client.sendRequest(barrier);
            client.flush();

            byte[] error = client.readEvent();
            assertNotNull(error, "the server has to answer");
            assertEquals(0, error[0] & 0xF, "an error");
            assertEquals(firstError + 12, error[1] & 0xFF,
                    "the extension's barrier error, inside the range QueryExtension named");
            assertEquals(0x1234, RawXClient.readInt(error, 4), "the barrier that was named");
            assertEquals(61, RawXClient.readShort(error, 8), "and the request it came from");
            assertEquals(opcode, error[10] & 0xFF, "raised by this extension");
        }
    }

    /**
     * {@code XIQueryVersion}, whose body is two <b>CARD16</b>s - unlike every other request here,
     * whose body is whole words. Written by hand because the word-based helper puts the minor at 8
     * and a server reads it at 6, which is how a test can pass while asking for a version it never
     * sends (measured: the first version test asked for 2.3 and got 2.0, because only the major
     * landed where the server looks).
     */
    private static byte[] version(RawXClient client, int opcode, int major, int minor)
            throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = XI_QUERY_VERSION;
        RawXClient.writeShortAt(request, 2, 2);
        RawXClient.writeShortAt(request, 4, major);
        RawXClient.writeShortAt(request, 6, minor);
        client.sendRequest(request);
        client.flush();
        return client.readReply();
    }

    /** Selects one raw mask on the root window, which is where raw events are delivered. */
    /**
     * The three device-state events are selectable on {@code XIAllDevices} alone, and a reference
     * says so with {@code BadValue} naming the bit.
     *
     * <p>Measured 2026-10-04: bit 11 on {@code XIAllMasterDevices} is error 2 value 11, while bits
     * 1, 11 and 12 on {@code XIAllDevices} are accepted. This server accepted them everywhere, which
     * is a client being told it will hear about a device appearing when it will not.
     */
    @Test
    void theDeviceStateEventsAreSelectableOnAllDevicesAlone() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            version(client, opcode, 2, 4);
            assertNull(selectMask(client, opcode, 0, (1 << 1) | (1 << 11) | (1 << 12)),
                    "XIAllDevices takes the device-state events");
            assertNull(selectMask(client, opcode, 2, 1 << 4),
                    "and an ordinary event on a master is accepted as before");
            byte[] error = selectMask(client, opcode, 1, 1 << 11);
            assertNotNull(error, "XIAllMasterDevices may not select the hierarchy event");
            assertEquals(2, error[1] & 0xFF, "BadValue");
            assertEquals(11, RawXClient.readInt(error, 4), "the bit, which is what a reference names");
        }
    }

    /** Selects a mask and returns null when it was accepted, or the error message when refused. */
    private static byte[] selectMask(RawXClient client, int opcode, int device, int mask)
            throws IOException {
        byte[] select = new byte[20];
        select[0] = (byte) opcode;
        select[1] = XI_SELECT_EVENTS;
        RawXClient.writeShortAt(select, 2, 5);
        RawXClient.writeIntAt(select, 4, client.root());
        RawXClient.writeShortAt(select, 8, 1);
        RawXClient.writeShortAt(select, 12, device);
        RawXClient.writeShortAt(select, 14, 1);
        RawXClient.writeIntAt(select, 16, mask);
        client.sendRequest(select);
        client.flush();
        client.readTimeoutMillis(300);
        try {
            byte[] event = client.readEvent();
            return event != null && (event[0] & 0xFF) == 0 ? event : null;
        } finally {
            client.readTimeoutMillis(3000);
        }
    }

    /**
     * {@code XIPassiveGrabDevice}: armed, answered, and released by
     * {@code XIPassiveUngrabDevice}.
     *
     * <p>Measured on Xvfb 2026-10-04: the grab is answered with a 32 byte reply whose
     * {@code num_modifiers} is nought when nothing conflicts, and the ungrab is accepted in silence.
     * The conflict case - another client already holding the same window, detail and modifier - is
     * this server's own reading of the same field (the reference was not measured there), so it is
     * pinned here as what it is: the second client is told which modifier was already taken.
     */
    @Test
    void aPassiveGrabIsArmedAnsweredAndTakenBack() throws IOException {
        try (RawXClient first = RawXClient.connect(DISPLAY);
             RawXClient second = RawXClient.connect(DISPLAY)) {
            int opcode = first.queryExtension("XInputExtension");
            version(first, opcode, 2, 4);
            int window = first.allocateId();
            first.createWindow(window, first.root(), 0, 0, 200, 200, 0x00FF00);
            first.mapWindow(window);
            first.flush();

            byte[] reply = passiveGrab(first, opcode, window, 38, 1 << 2);
            assertEquals(1, reply[0] & 0xFF, "a reply, which is what the request expects");
            assertEquals(XI_PASSIVE_GRAB_DEVICE, reply[1] & 0xFF, "the minor opcode, as a reference"
                    + " puts it");
            assertEquals(0, RawXClient.readShort(reply, 8), "nothing conflicts yet");
            assertEquals(0, RawXClient.readInt(reply, 4), "and then no modifiers follow the header");

            byte[] conflict = passiveGrab(second, opcode, window, 38, 1 << 2);
            assertEquals(1, RawXClient.readShort(conflict, 8), "the other client's grab is reported");
            assertEquals(0x80000000, RawXClient.readInt(conflict, 32),
                    "naming the modifier that was already taken: XIAnyModifier, as it was asked for");

            // The ungrab is accepted in silence: no reply, and the grab is gone.
            byte[] ungrab = new byte[24];
            ungrab[0] = (byte) opcode;
            ungrab[1] = 55;                                  // X_XIPassiveUngrabDevice
            RawXClient.writeShortAt(ungrab, 2, 6);           // 24 bytes: the modifier is in it
            RawXClient.writeIntAt(ungrab, 4, window);
            RawXClient.writeIntAt(ungrab, 8, 38);
            RawXClient.writeShortAt(ungrab, 12, 1);          // XIAllMasterDevices
            RawXClient.writeShortAt(ungrab, 14, 1);          // one modifier
            ungrab[16] = 1;                                  // XIGrabtypeKeycode
            RawXClient.writeIntAt(ungrab, 20, 0x80000000);   // the modifier list follows the pads
            first.sendRequest(ungrab);
            first.flush();
            first.readTimeoutMillis(250);
            assertNull(first.readEvent(), "an ungrab is not answered");
            first.readTimeoutMillis(3000);

            byte[] after = passiveGrab(second, opcode, window, 38, 1 << 2);
            assertEquals(0, RawXClient.readShort(after, 8), "the first client's grab is gone");
        }
    }

    /** {@code XIPassiveGrabDevice} for one key with {@code XIAnyModifier}, answering the reply. */
    private static byte[] passiveGrab(RawXClient client, int opcode, int window, int detail,
                                      int mask) throws IOException {
        byte[] request = new byte[40];
        request[0] = (byte) opcode;
        request[1] = 54;                                     // X_XIPassiveGrabDevice
        RawXClient.writeShortAt(request, 2, 10);
        RawXClient.writeIntAt(request, 4, 0);                // time: CurrentTime
        RawXClient.writeIntAt(request, 8, window);
        RawXClient.writeIntAt(request, 12, 0);               // cursor: None
        RawXClient.writeIntAt(request, 16, detail);
        RawXClient.writeShortAt(request, 20, 1);             // XIAllMasterDevices
        RawXClient.writeShortAt(request, 22, 1);             // num_modifiers
        RawXClient.writeShortAt(request, 24, 1);             // mask_len, in words
        request[26] = 1;                                     // XIGrabtypeKeycode
        request[27] = 1;                                     // XIGrabmodeAsync
        request[28] = 1;                                     // paired mode
        request[29] = 0;                                     // owner_events
        RawXClient.writeIntAt(request, 32, 0x80000000);      // XIAnyModifier
        RawXClient.writeIntAt(request, 36, mask);
        client.sendRequest(request);
        client.flush();
        return client.readReply();
    }

    /**
     * {@code XIChangeHierarchy} with {@code XIAddMaster}: the four devices a reference creates, and
     * the event that says so.
     *
     * <p>Measured on Xvfb 2026-10-04: a master called N brings **four** devices - "N pointer", "N
     * keyboard", "N XTEST pointer" and "N XTEST keyboard", attached to each other - the list grows by
     * four, and the {@code XI_HierarchyChanged} event that follows has flags **0x55**
     * ({@code XIMasterAdded | XISlaveAdded | XISlaveAttached | XIDeviceEnabled}), device id 0, and
     * one twelve byte record per device the server has. The event names no window, so the selection
     * is the {@code XIAllDevices} one on the root - the only device those bits are selectable on.
     *
     * <p><b>Its own server</b>, because the devices a test adds are that server's for the rest of its
     * life and every other test here counts them.
     */
    @Test
    void addingAMasterCreatesFourDevicesAndOneEvent() throws IOException {
        XServer isolated = new XServer(ServerOptions.parse(new String[]{"--display", ":69",
                "--geometry", "600x400", "--backend", "headless", "--no-unix"}));
        isolated.start();
        isolated.startEventLoop();
        try (RawXClient client = RawXClient.connect(69)) {
            int opcode = client.queryExtension("XInputExtension");
            version(client, opcode, 2, 4);
            assertEquals(6, RawXClient.readShort(request(client, opcode, XI_QUERY_DEVICE, 0), 8),
                    "six devices before the change");
            client.sendRequest(selectMessage(opcode, client.root(), 0, (1 << 1) | (1 << 11)));
            client.flush();
            client.readTimeoutMillis(300);
            assertNull(client.readEvent(), "a selection is not answered");
            client.readTimeoutMillis(3000);

            byte[] add = hierarchyMessage(opcode, "Probe master", 1);
            client.sendRequest(add);
            client.flush();

            byte[] event = null;
            for (byte[] message = client.readEvent(); message != null; message = client.readEvent()) {
                if ((message[0] & 0xFF) == RawXClient.GENERIC_EVENT
                        && (message[1] & 0xFF) == opcode
                        && RawXClient.readShort(message, 8) == 11) {
                    event = message;
                    break;
                }
            }
            assertNotNull(event, "the hierarchy change has to be reported");
            assertEquals(0, RawXClient.readShort(event, 10), "device id 0: it is about the set");
            assertEquals(0x55, RawXClient.readInt(event, 16),
                    "MasterAdded | SlaveAdded | SlaveAttached | DeviceEnabled, measured on Xvfb");
            assertEquals(10, RawXClient.readShort(event, 20), "one record per device: the six plus"
                    + " the four the change added");
            assertEquals(152, event.length, "32 bytes and ten 12 byte records");
            // The last four records are the new devices: a master pair and the XTEST pair under it.
            assertEquals(8, RawXClient.readShort(event, 32 + 6 * 12), "the new master pointer");
            assertEquals(9, RawXClient.readShort(event, 32 + 6 * 12 + 2), "attached to its keyboard");
            assertEquals(1, event[32 + 6 * 12 + 4] & 0xFF, "MasterPointer");
            assertEquals(9, RawXClient.readShort(event, 32 + 7 * 12), "the new master keyboard");
            assertEquals(2, event[32 + 7 * 12 + 4] & 0xFF, "MasterKeyboard");
            assertEquals(10, RawXClient.readShort(event, 32 + 8 * 12), "its XTEST pointer");
            assertEquals(3, event[32 + 8 * 12 + 4] & 0xFF, "SlavePointer, attached to the new master");
            assertEquals(11, RawXClient.readShort(event, 32 + 9 * 12), "and its XTEST keyboard");
            assertEquals(4, event[32 + 9 * 12 + 4] & 0xFF, "SlaveKeyboard");

            byte[] devices = request(client, opcode, XI_QUERY_DEVICE, 0);
            assertEquals(10, RawXClient.readShort(devices, 8), "the list grew by four");
            byte[] newPointer = request(client, opcode, XI_QUERY_DEVICE, 8);
            assertEquals(1, RawXClient.readShort(newPointer, 34), "MasterPointer");
            assertEquals("Probe master pointer".length(), RawXClient.readShort(newPointer, 40),
                    "the name a reference gives it");

            // The core masters are not a client's to remove: a reference answers BadValue, and this
            // server says the same rather than quietly taking the server's own pointer away.
            byte[] remove = hierarchyMessage(opcode, "", 2);
            RawXClient.writeShortAt(remove, 12, 2);          // the core pointer
            client.sendRequest(remove);
            client.flush();
            byte[] refusal = client.readEvent();
            assertNotNull(refusal, "the server has to answer");
            assertEquals(0, refusal[0] & 0xF, "an error");
            assertEquals(2, refusal[1] & 0xFF, "BadValue");
        } finally {
            isolated.stop();
        }
    }

    /** An {@code XISelectEvents} message for one mask. */
    private static byte[] selectMessage(int opcode, int window, int device, int mask) {
        byte[] select = new byte[20];
        select[0] = (byte) opcode;
        select[1] = XI_SELECT_EVENTS;
        RawXClient.writeShortAt(select, 2, 5);
        RawXClient.writeIntAt(select, 4, window);
        RawXClient.writeShortAt(select, 8, 1);
        RawXClient.writeShortAt(select, 12, device);
        RawXClient.writeShortAt(select, 14, 1);
        RawXClient.writeIntAt(select, 16, mask);
        return select;
    }

    /**
     * An {@code XIChangeHierarchy} with one change: kind 1 is {@code XIAddMaster} (which carries the
     * name), 2 is {@code XIRemoveMaster} (the caller writes the device id at 12).
     */
    private static byte[] hierarchyMessage(int opcode, String name, int kind) {
        int padded = (name.length() + 3) & ~3;
        byte[] request = new byte[8 + 8 + padded];
        request[0] = (byte) opcode;
        request[1] = 43;                                 // X_XIChangeHierarchy
        RawXClient.writeShortAt(request, 2, request.length / 4);
        request[4] = 1;                                  // one change
        RawXClient.writeShortAt(request, 8, kind);
        RawXClient.writeShortAt(request, 10, 2 + padded / 4);
        if (kind == 1) {
            RawXClient.writeShortAt(request, 12, name.length());
            request[14] = 1;                             // send_core
            request[15] = 1;                             // enable
            for (int i = 0; i < name.length(); i++) {
                request[16 + i] = (byte) name.charAt(i);
            }
        } else {
            request[14] = 1;                             // return_mode: XIFloating
        }
        return request;
    }

    private static void selectRaw(RawXClient client, int opcode, int mask) throws IOException {
        byte[] select = new byte[20];
        select[0] = (byte) opcode;
        select[1] = XI_SELECT_EVENTS;
        RawXClient.writeShortAt(select, 2, 5);
        RawXClient.writeIntAt(select, 4, client.root());
        RawXClient.writeShortAt(select, 8, 1);          // one mask record
        RawXClient.writeShortAt(select, 12, 1);         // XIAllMasterDevices
        RawXClient.writeShortAt(select, 14, 1);         // one mask word
        RawXClient.writeIntAt(select, 16, mask);
        client.sendRequest(select);
        client.flush();
    }

    @Test
    void aClientThatSelectsTheDeviceEventGetsThatAndNotTheCoreOne() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            int xtest = client.queryExtension("XTEST");
            request(client, opcode, XI_QUERY_VERSION, 2, 0);
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 200, 200, 0x00FF00);
            client.mapWindow(window);
            client.flush();
            // The core mask as well, deliberately: a client that asks for both is the case the
            // suppression is about, and it is what GDK does.
            client.selectInput(window, 0x0C);      // ButtonPressMask | ButtonReleaseMask

            byte[] select = new byte[20];
            select[0] = (byte) opcode;
            select[1] = XI_SELECT_EVENTS;
            RawXClient.writeShortAt(select, 2, 5);
            RawXClient.writeIntAt(select, 4, window);
            RawXClient.writeShortAt(select, 8, 1);          // one mask record
            RawXClient.writeShortAt(select, 12, 0);         // XIAllDevices
            RawXClient.writeShortAt(select, 14, 1);         // one mask word
            RawXClient.writeIntAt(select, 16, XI_BUTTONS);
            client.sendRequest(select);
            client.flush();

            client.fakeMotion(xtest, 20, 20);
            client.fakeButton(xtest, 1, true);
            client.fakeButton(xtest, 1, false);
            client.flush();

            byte[] device = null;
            java.util.List<String> seen = new java.util.ArrayList<>();
            for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
                int type = event[0] & 0x7F;
                if (type == RawXClient.GENERIC_EVENT) {
                    int evtype = RawXClient.readShort(event, 8);
                    seen.add("device evtype " + evtype);
                    if (device == null && evtype == 4 && (event[1] & 0xFF) == opcode) {
                        device = event;
                    }
                } else {
                    seen.add("core " + type);
                }
            }
            // The click's two events, and nothing else on the button rail: a client that asked for
            // the device event and got the core one as well would act on every click twice.
            assertTrue(seen.contains("device evtype 4"),
                    "the device ButtonPress has to arrive: " + seen);
            assertTrue(seen.contains("device evtype 5"),
                    "and the device ButtonRelease: " + seen);
            assertFalse(seen.contains("core 4"), "the core ButtonPress arrived too: " + seen);
            assertFalse(seen.contains("core 5"), "the core ButtonRelease arrived too: " + seen);
            assertNotNull(device, "the device event has to arrive at all");
            assertEquals(opcode, device[1] & 0xFF, "the extension opcode in the header");
            assertEquals(4, RawXClient.readShort(device, 8), "XI_ButtonPress");
            assertEquals(2, RawXClient.readShort(device, 10), "the pointer device");
        }
    }

    @Test
    void aClientThatSelectsOnlyTheDeviceEventStillGetsIt() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            int xtest = client.queryExtension("XTEST");
            request(client, opcode, XI_QUERY_VERSION, 2, 0);
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 200, 200, 0x00FF00);
            client.mapWindow(window);
            client.flush();
            // **No core mask at all** - which is the case the delivery missed. The owner's mask and
            // the list of other clients are the *core* selections, so a client that only ever called
            // XISelectEvents appears in neither and was reached by nothing: measured with `xinput
            // test-xi2 --root`, where the server logged the button press delivered to the window and
            // sent the client no event at all. GDK asks for both rails, which is why every test
            // written against it passed.
            byte[] select = new byte[20];
            select[0] = (byte) opcode;
            select[1] = XI_SELECT_EVENTS;
            RawXClient.writeShortAt(select, 2, 5);
            RawXClient.writeIntAt(select, 4, window);
            RawXClient.writeShortAt(select, 8, 1);          // one mask record
            RawXClient.writeShortAt(select, 12, 0);         // XIAllDevices
            RawXClient.writeShortAt(select, 14, 1);         // one mask word
            RawXClient.writeIntAt(select, 16, XI_BUTTONS);
            client.sendRequest(select);
            client.flush();

            client.fakeMotion(xtest, 20, 20);
            client.fakeButton(xtest, 1, true);
            client.flush();

            byte[] event = client.readEvent();
            assertNotNull(event, "a client that asked only through XInput2 has to be told");
            assertEquals(RawXClient.GENERIC_EVENT, event[0] & 0x7F, "and told the device event");
            assertEquals(4, RawXClient.readShort(event, 8), "XI_ButtonPress");
        }
    }

    /**
     * When the window holding the focus - **and the pointer** - goes away and the focus reverts to the
     * root window, the window the pointer is in is told **only** its crossing, on either rail.
     *
     * <p>Measured against a reference with {@code tools/FocusRevertProbe}, which drives exactly this:
     * {@code PAGE} (100,100 100x100) covers part of {@code A} (0,0 300x300), both children of the
     * root, the pointer is at 150,150 - inside both - the client focuses {@code PAGE}, and then unmaps
     * it. A reference answers
     *
     * <pre>
     *   EnterNotify win=A detail=Nonlinear
     *   focus = R
     * </pre>
     *
     * and nothing else: the focus went to the *root window*, and every window is inside it, so no
     * window of the pointer's chain is named on the focus rail. This server reported a {@code FocusIn}
     * to the window that was going away ({@code detail=Pointer}) instead - a window a client can do
     * nothing with, and GDK took it as where the focus lives, leaving the window underneath looking
     * unfocused. That is what a toolkit hides a popup over: the context menu that flashed and vanished
     * after the 片段 page was closed, reported 2026-09-29.
     */
    @Test
    void aChangeToTheRootWindowNamesNoWindowOfThePointersChain() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            int xtest = client.queryExtension("XTEST");
            request(client, opcode, XI_QUERY_VERSION, 2, 0);

            // The window the pointer ends up in: XInput2 focus events, the core focus mask and the
            // crossings, so both rails are watched and the crossing is one of them.
            int under = client.allocateId();
            client.createWindow(under, client.root(), 0, 0, 300, 300, 0x00FF00);
            client.mapWindow(under);
            client.selectInput(under, 0x10 | 0x20 | 0x200000);   // Enter, Leave and FocusChange
            selectXi2Focus(client, opcode, under);
            // The page: a window of its own that covers the pointer's position.
            int page = client.allocateId();
            client.createWindow(page, client.root(), 100, 100, 100, 100, 0x0000FF);
            client.mapWindow(page);
            client.flush();

            client.fakeMotion(xtest, 150, 150);     // the pointer into the page, inside `under` too
            client.setInputFocus(page, 2);          // the client focuses the page...
            client.flush();
            client.unmapWindow(page);               // ...and takes it away with the pointer still in it
            client.flush();

            boolean focusNamed = false;
            boolean crossing = false;
            for (byte[] event = client.readEvent(); event != null; event = client.readEvent()) {
                int type = event[0] & 0x7F;
                if (type == RawXClient.GENERIC_EVENT) {
                    if (RawXClient.readShort(event, 8) == 9 && RawXClient.readInt(event, 24) == under) {
                        focusNamed = true;
                    }
                } else if (type == 9 || type == 10) {
                    if (RawXClient.readInt(event, 4) == under) {
                        focusNamed = true;
                    }
                } else if (type == 7 && RawXClient.readInt(event, 12) == under) {
                    crossing = true;
                }
            }
            assertFalse(focusNamed, "no window of the pointer's chain is named on the focus rail");
            assertTrue(crossing, "and the crossing is what tells it where the pointer is");
        }
    }

    /** Selects XI2 focus events on a window, the way a toolkit does. */
    private static void selectXi2Focus(RawXClient client, int opcode, int window) throws IOException {
        byte[] select = new byte[20];
        select[0] = (byte) opcode;
        select[1] = XI_SELECT_EVENTS;
        RawXClient.writeShortAt(select, 2, 5);
        RawXClient.writeIntAt(select, 4, window);
        RawXClient.writeShortAt(select, 8, 1);              // one mask record
        RawXClient.writeShortAt(select, 12, 1);             // XIAllMasterDevices
        RawXClient.writeShortAt(select, 14, 1);             // one mask word
        RawXClient.writeIntAt(select, 16, (1 << 9) | (1 << 10));   // XI_FocusIn | XI_FocusOut
        client.sendRequest(select);
        client.flush();
    }

    @Test
    void theDeviceEventIsTheSizeAClientSizesItFrom() {
        // Built for a little endian reader, which is what every client in this suite is.
        Xi2Event event = Xi2Event.builder(false, (short) 1, 130)
                .evtype(4)
                .device(2)
                .source(2)
                .time(1000)
                .detail(1)
                .root(0x1ff)
                .event(0x200001)
                .child(0)
                .rootPosition(10, 20)
                .windowPosition(5, 6)
                .modifiers(0)
                .build();
        byte[] wire = event.wire();
        // A reference's key event is a hundred and twenty bytes and its motion, with two axes, a
        // hundred and thirty six. A generated event class could not have carried this: every core
        // event is thirty two bytes, and this one's size is written down in its own length field.
        assertEquals(120, wire.length, "eighty bytes of fixed part and the two masks");
        assertEquals(22, RawXClient.readInt(wire, 4), "length is the size in words, less eight");
        assertEquals(35, wire[0] & 0xFF, "a GenericEvent");
        assertEquals(130, wire[1] & 0xFF, "the extension's opcode");
        assertEquals(8, RawXClient.readShort(wire, 48), "buttons_len");
        assertEquals(2, RawXClient.readShort(wire, 50), "valuators_len");
        assertEquals(2, RawXClient.readShort(wire, 52), "sourceid");
        assertEquals(10 << 16, RawXClient.readInt(wire, 32), "root x as FP1616");
        assertEquals(1, RawXClient.readInt(wire, 16), "the detail is the button");
    }

    /**
     * A device event carries the keyboard's modifier state <b>twice</b>, and a toolkit reads the
     * second one.
     *
     * <p>The four bytes at 60-63 are the state the protocol documents - base, latched, locked,
     * effective - and the word at 72 carries the same bits again, which is the field a reference
     * fills for a Ctrl+A (measured on the event: {@code 04} at 63 and {@code 04} at 72) and which
     * this server left nought. GDK's own key event takes its {@code state} from that word, so a
     * forwarded application saw no Control at all: Ctrl+A, Ctrl+C and Ctrl+V did nothing in its
     * input fields while ordinary typing worked. The A/B is the measurement: with the word set, GDK
     * reports {@code state=0x4 ctrl=True} for the gesture that reported {@code state=0x0} without it.
     */
    @Test
    void aDeviceEventCarriesTheModifierStateWhereAToolkitReadsIt() {
        Xi2Event event = Xi2Event.builder(false, (short) 1, 130)
                .evtype(2)
                .device(3)
                .source(3)
                .time(1000)
                .detail(38)
                .root(0x1ff)
                .event(0x200001)
                .child(0)
                .rootPosition(1, 1)
                .windowPosition(1, 1)
                .modifiers(0x4)                         // Control, as the core event carries it
                .build();
        byte[] wire = event.wire();
        assertEquals(4, wire[60] & 0xFF, "base");
        assertEquals(0, wire[62] & 0xFF, "locked: Control is not a lock");
        assertEquals(4, wire[63] & 0xFF, "effective");
        assertEquals(4, RawXClient.readInt(wire, 72),
                "and the same bits at 72, which is where a toolkit reads a Ctrl+A from");
    }

    /**
     * A crossing and a focus event are <b>not</b> device events, and a reference sends each of the
     * three in its own shape.
     *
     * <p>Measured 2026-09-29 by tapping a byte-transparent relay in front of Xvfb while a real
     * client started (`tools/tap.py`, with the summary in `tools/README.md`): a key is 120 bytes, an
     * {@code EnterNotify} 76 and a {@code FocusIn} 104, and from offset 48 on the fields are in
     * different places - {@code same_screen} at 48 and {@code focus} at 49 where a device event has
     * its two lengths, {@code buttons_len} at 50 where a device event has {@code valuators_len}, the
     * modifier state as four CARD32 from 52 where a device event has its source id, and the button
     * mask at 72 where a device event's is at 80.
     *
     * <p>This server sent all three in the device shape, which left a toolkit reading this server's
     * two length fields as its flags and its mask eight bytes late - the visible cost was a window
     * that never looked focused in a forwarded application. Each assertion below is a field of the
     * reference's own event.
     */
    @Test
    void aCrossingAndAFocusEventHaveTheShapesAReferenceSends() {
        Xi2Event crossing = Xi2Event.builder(false, (short) 7, 130, Xi2Event.Shape.CROSSING)
                .evtype(7)
                .device(2)
                .source(2)
                .time(1000)
                .detail(0)
                .root(0x1ff)
                .event(0x200001)
                .child(0)
                .rootPosition(188, 82)
                .windowPosition(188, 49)
                .crossing((byte) 0)
                .sameScreen(true)
                .focus(true)
                .modifiers(0)
                .build();
        byte[] wire = crossing.wire();
        assertEquals(76, wire.length, "seventy-two bytes of fixed part and one word of button mask");
        assertEquals(11, RawXClient.readInt(wire, 4), "length is the size in words, less eight");
        assertEquals(2, RawXClient.readShort(wire, 16), "sourceid, where a device event has none");
        assertEquals(1, wire[48] & 0xFF, "same_screen, where a device event's buttons_len sits");
        assertEquals(1, wire[49] & 0xFF, "focus");
        assertEquals(1, RawXClient.readShort(wire, 50), "buttons_len");
        assertEquals(188 << 16, RawXClient.readInt(wire, 32), "root x as FP1616");

        Xi2Event focus = Xi2Event.builder(false, (short) 7, 130, Xi2Event.Shape.FOCUS)
                .evtype(10)
                .device(3)
                .source(3)
                .time(1000)
                .detail(3)
                .root(0x1ff)
                .event(0x200001)
                .child(0)
                .rootPosition(188, 82)
                .windowPosition(188, 49)
                .sameScreen(true)
                .focus(false)
                .modifiers(0)
                .build();
        wire = focus.wire();
        assertEquals(104, wire.length, "seventy-two bytes of fixed part and eight words of button mask");
        assertEquals(18, RawXClient.readInt(wire, 4), "length is the size in words, less eight");
        assertEquals(3, RawXClient.readInt(wire, 16), "the detail is a word here, where a crossing has "
                + "its source id, mode and detail in those four bytes");
        assertEquals(1, wire[48] & 0xFF, "same_screen");
        assertEquals(0, wire[49] & 0xFF, "focus, which a reference leaves clear on a focus event");
        assertEquals(8, RawXClient.readShort(wire, 50), "buttons_len: eight words, where a crossing "
                + "sends one");

        // A motion is a device event like a key, and the two things beyond that shape are the ones a
        // client reads a drag from: the buttons held, and the two axes as values behind their mask.
        // Written without either - or, as this was once, with a crossing's fields at 48/50 - a client
        // that believes `valuators_len` reads thirty two bytes of mask that are not there and walks
        // into the next message: GDK died of it the first time a title bar was dragged.
        Xi2Event motion = Xi2Event.builder(false, (short) 7, 130, Xi2Event.Shape.MOTION)
                .evtype(6)
                .device(2)
                .source(2)
                .time(1000)
                .detail(0)
                .root(0x1ff)
                .event(0x200001)
                .child(0)
                .rootPosition(620, 66)
                .windowPosition(610, 33)
                .modifiers(0x100)
                .buttons(0x100)
                .axes(620, 66)
                .build();
        wire = motion.wire();
        assertEquals(136, wire.length, "eighty bytes of fixed part, both masks, and two axis values");
        assertEquals(26, RawXClient.readInt(wire, 4), "length is the size in words, less eight");
        assertEquals(8, RawXClient.readShort(wire, 48), "buttons_len, which a motion has as well");
        assertEquals(2, RawXClient.readShort(wire, 50), "valuators_len");
        assertEquals(0x02, RawXClient.readInt(wire, 80), "button 1 held, numbered from one as a "
                + "reference numbers it");
        assertEquals(0x03, RawXClient.readInt(wire, 112), "the valuator mask: X and Y are in this one");
        assertEquals(620, RawXClient.readInt(wire, 120), "the X axis, as FP3232");
        assertEquals(66, RawXClient.readInt(wire, 128), "and Y, behind it");
    }

    @Test
    void aRequestWithNoReplyIsNotAnswered() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);
            client.flush();

            // XIChangeCursor has no reply. Answering it shifts everything the client reads after it
            // by a reply's worth, and the client then acts on data that is not the answer to
            // anything - which is what GDK did, a million times over, when a text field was focused.
            byte[] cursor = new byte[12];
            cursor[0] = (byte) opcode;
            cursor[1] = XI_CHANGE_CURSOR;
            RawXClient.writeShortAt(cursor, 2, 3);
            RawXClient.writeIntAt(cursor, 4, window);
            RawXClient.writeIntAt(cursor, 8, 0);
            client.sendRequest(cursor);

            // And the next request that does have a reply gets its own back, undisturbed.
            byte[] query = request(client, opcode, XI_GET_CLIENT_POINTER, window);
            assertEquals(0, query[8] & 0xFF, "set: this client never chose a pointer");
        }
    }

    /**
     * A device property read is answered "absent", not refused - and the reply's shape is a
     * reference's, measured rather than reasoned.
     *
     * <p>A forwarded {@code EasyShell} asks this as it starts, between two {@code InternAtom} calls,
     * and the refusal was a {@code BadRequest}: an unknown minor reached the decoder, which throws.
     * The reply is thirty two bytes whose only non-zero fields are the header's, and byte 1 carries
     * the <em>minor opcode</em> - measured by asking about two devices and watching it stay 59, which
     * is what makes it the request's number and not the device's.
     *
     * <p>The request is <b>six units</b>: a reference refuses seven with {@code BadLength}, so the
     * shorter form is what a client sends and what this decoder reads.
     */
    @Test
    void theDevicePropertyReadIsAnsweredAbsentRatherThanRefused() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            int property = client.internAtom("XServerBacklogProbe");
            byte[] request = new byte[24];
            request[0] = (byte) opcode;
            request[1] = XI_GET_PROPERTY;
            RawXClient.writeShortAt(request, 2, 6);
            RawXClient.writeShortAt(request, 4, 2);          // the pointer device
            RawXClient.writeIntAt(request, 8, property);
            RawXClient.writeIntAt(request, 12, 0);           // type: AnyPropertyType
            RawXClient.writeIntAt(request, 20, 1000);        // len, in four byte units
            client.sendRequest(request);

            byte[] reply = client.readReply();               // refuses by throwing
            assertEquals(1, reply[0] & 0xFF, "a reply, not an error - and a property read expects one,"
                    + " so accepting the request and answering nothing would be worse");
            assertEquals(XI_GET_PROPERTY, reply[1] & 0xFF, "the minor opcode, where a reference puts it");
            assertEquals(0, RawXClient.readInt(reply, 8), "type = None: no such property");
            assertEquals(0, RawXClient.readInt(reply, 12), "bytes_after");
            assertEquals(0, RawXClient.readInt(reply, 16), "num_items");
            assertEquals(0, reply[20] & 0xFF, "format");
        }
    }

    /**
     * The list of a device's properties is answered "none" rather than refused - the same shape of
     * bug as the property read, and the request {@code xinput list-props} sends first.
     *
     * <p>The reply is a bare header: the count is a CARD16 at 8, this server has no properties so
     * there is no tail, and byte 1 carries the request's minor opcode - read off a reference's reply
     * for a device that <em>has</em> properties ({@code XiProbe listprops 2}), since the empty answer
     * is that reply with the count at zero. Measured: {@code xinput list-props 2} prints the device
     * and no properties against a reference, and was a {@code BadRequest} here.
     */
    @Test
    void theDevicePropertyListIsAnsweredEmptyRatherThanRefused() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            byte[] request = new byte[8];
            request[0] = (byte) opcode;
            request[1] = XI_LIST_PROPERTIES;
            RawXClient.writeShortAt(request, 2, 2);
            RawXClient.writeShortAt(request, 4, 2);              // the pointer device
            client.sendRequest(request);

            byte[] reply = client.readReply();                   // refuses by throwing
            assertEquals(1, reply[0] & 0xFF, "a reply, not an error");
            assertEquals(XI_LIST_PROPERTIES, reply[1] & 0xFF, "the minor opcode, where a reference"
                    + " puts it");
            assertEquals(0, RawXClient.readShort(reply, 8), "num_properties");
            assertEquals(0, RawXClient.readInt(reply, 4), "length: no atoms follow");
        }
    }

    @Test
    void thePointerQueryAnswersInTheSizeItIsDefinedWith() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            int window = client.allocateId();
            client.createWindow(window, client.root(), 0, 0, 100, 100);
            client.flush();
            byte[] reply = request(client, opcode, XI_QUERY_POINTER, window, 2 << 16);
            // Forty four bytes: the two coordinate pairs and the modifier groups come after the
            // point where a core reply would have ended, and written into a thirty two byte array
            // the last pair runs off the end.
            assertEquals(3, RawXClient.readInt(reply, 4), "length: (44 - 32) / 4");
            assertEquals(1, reply[32] & 0xFF, "same_screen, which is at 32 and not in the header");
        }
    }

    /**
     * The reply's {@code child} is the queried window's **own child**, and None when the pointer is
     * in the queried window itself.
     *
     * <p>A client walks the tree with this - ask about a window, ask again about the child the
     * answer names, until the answer is None - so an answer of "the deepest window the pointer is
     * in" ends the walk nowhere: the query about that window names itself, and the client asks about
     * it for ever. Measured live (A16): GDK does exactly that walk after a click, and EasyShell
     * against this server asked {@code XIQueryPointer} ~3700 times a second, while against Xvfb it
     * asks twice - child = the toplevel for the root query, then None for the query about it.
     */
    @Test
    void theChildIsTheQueriedWindowsOwnChildAndNotTheDeepestWindow() throws IOException {
        try (RawXClient client = RawXClient.connect(DISPLAY)) {
            int opcode = client.queryExtension("XInputExtension");
            int parent = client.allocateId();
            client.createWindow(parent, client.root(), 100, 100, 200, 200);
            client.mapWindow(parent);
            int child = client.allocateId();
            client.createWindow(child, parent, 10, 10, 100, 100);
            client.mapWindow(child);
            // The pointer inside the child, so it is inside both windows - which is the case the
            // walk depends on. The warp is relative to the child, and the round trip that follows
            // is what makes the server's state readable by the next query.
            client.warpPointer(child, 50, 50);
            client.geometry(parent);

            byte[] aboutParent = request(client, opcode, XI_QUERY_POINTER, parent, 2 << 16);
            assertEquals(child, RawXClient.readInt(aboutParent, 12),
                    "the queried window's own child, which is what a client descends to");
            byte[] aboutChild = request(client, opcode, XI_QUERY_POINTER, child, 2 << 16);
            assertEquals(0, RawXClient.readInt(aboutChild, 12),
                    "and None when the pointer is in that window itself: answering the child again"
                            + " is a walk with no bottom");
        }
    }

    /**
     * Sends an XInput2 request whose body is whole words, and reads the reply.
     *
     * <p>Hand written rather than taken from the generated classes: this extension has none - the
     * fork's {@code x11-protocol-xinput} is not published - so a test that wants to speak it has to
     * lay the bytes out itself, which is also what a client on the other side of the wire does.
     */
    private static byte[] request(RawXClient client, int opcode, int minor, int... body)
            throws IOException {
        byte[] request = new byte[4 + body.length * 4];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        RawXClient.writeShortAt(request, 2, 1 + body.length);
        for (int i = 0; i < body.length; i++) {
            RawXClient.writeIntAt(request, 4 + i * 4, body[i]);
        }
        client.sendRequest(request);
        client.flush();
        return client.readReply();
    }

}
