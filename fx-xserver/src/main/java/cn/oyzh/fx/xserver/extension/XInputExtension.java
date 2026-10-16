package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.X11Input;
import com.github.moaxcp.x11.protocol.XGenericEvent;
import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.event.EventMasks;
import cn.oyzh.fx.xserver.event.XInputMasks;
import cn.oyzh.fx.xserver.event.Xi2Event;
import cn.oyzh.fx.xserver.event.Xi2RawEvent;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * XInput2, the part a toolkit actually asks for (backlog B4).
 *
 * <p>{@code xeyes} prints a warning about the missing {@code XInputExtension} and falls back to core
 * input, and until now so did everything else. What is here is the extension's spine: the version
 * handshake, the device list, which device a client's pointer is, the event selection and the events
 * themselves.
 *
 * <p><b>Why this is all-or-nothing, and why that was measured rather than assumed.</b> Advertising an
 * extension changes what a client does, not just what it could do. JavaFX's Linux backend is GTK:
 * {@code libglassgtk3.so} grabs through {@code gdk_device_grab}, and <b>{@code libgdk-3.so} resolves
 * {@code XIGrabDevice}/{@code XIUngrabDevice}</b> wherever they exist - measured with {@code strings}
 * on the AppImage's own copies of both. GDK3's device grab <em>is</em> {@code XIGrabDevice} once the
 * extension is advertised, and every menu, drag and modal dialog a forwarded GTK application opens
 * goes through one, so a server that answers {@code XIQueryVersion} and then refuses
 * {@code XIGrabDevice} does not give that application better scrolling; it takes away its menus.
 * {@link #grabDevice} is therefore part of the spine and not a later addition.
 *
 * <p><b>Every wire layout here was measured against a reference server</b> with
 * {@code tools/XiProbe} on 2026-09-26, not derived: see {@link XInputMasks} for the mask numbering,
 * the generic header and the length field, and {@link #queryDevice} for the device list's own shape.
 * The requests are decoded by hand, because the protocol fork's {@code x11-protocol-xinput} is not
 * published to the local Maven repository and nothing generated is therefore available - the same
 * position {@code XkbExtension} is in for {@code SetMap}. The replies are written as bytes for the
 * same reason: they carry lists whose shape no class here reproduces.
 *
 * <p><b>What is not here</b>, deliberately: touch and tablets, which a client asks for by name and
 * this server has no device behind (a reference in this bed has none either - measured: no device
 * reports a touch class on Xvfb); the property requests that <em>change</em> something
 * ({@code XIChangeProperty}, {@code XIDeleteProperty}), because there are no device properties here
 * to change - the two that <em>read</em> them are answered "none" ({@link #listProperties} and
 * {@link #getProperty}, both of which a forwarded client sends); the hierarchy's changing requests,
 * which would let a client add a device this server cannot invent; and pointer barriers, which are
 * <em>created</em> through XFIXES 5.0 and therefore cannot exist here at all (XFIXES answers 4.0),
 * so {@link #releaseBarrier} answers exactly what a reference answers for a barrier that is not
 * there. Each of the rest is refused rather than accepted and ignored - a refusal a client can see
 * is worth more than silence it cannot - except where a client asks for something it will wait on.
 *
 * <p><b>The raw family is here since A47, which is what let the version below move.</b> XI 2.1's raw
 * events are the one addition of the three releases this server can have in full: 2.2's touch needs
 * a touch device (neither server has one), 2.4 only rewords touch ownership, and 2.3's barriers need
 * XFIXES 5.0 for anything to create. So the version is a reference's 2.4 and the three surfaces
 * that could be reached are answered or delivered - measured rather than assumed, because the
 * alternative (2.0 with a note) was the state this replaced.
 */
public final class XInputExtension implements Extension {

    private static final Logger log = LoggerFactory.getLogger(XInputExtension.class);

    /**
     * The version this server answers with, which is now the one a reference answers (2.4, measured
     * 2026-10-04) rather than 2.0 - see {@link #queryVersion} for what each release adds and what
     * that costs.
     */
    private static final int MAJOR_VERSION = 2;
    private static final int MINOR_VERSION = 4;

    // Minor opcodes, from the extension's own numbering.
    private static final int GET_EXTENSION_VERSION = 1;
    private static final int LIST_INPUT_DEVICES = 2;
    private static final int OPEN_DEVICE = 3;
    private static final int CLOSE_DEVICE = 4;
    private static final int GET_DEVICE_KEY_MAPPING = 24;
    private static final int GET_DEVICE_BUTTON_MAPPING = 28;
    private static final int XI_QUERY_POINTER = 40;
    private static final int XI_WARP_POINTER = 41;
    private static final int XI_CHANGE_CURSOR = 42;
    private static final int XI_SET_CLIENT_POINTER = 44;
    private static final int XI_GET_CLIENT_POINTER = 45;
    private static final int XI_SELECT_EVENTS = 46;
    private static final int XI_QUERY_VERSION = 47;
    private static final int XI_QUERY_DEVICE = 48;
    private static final int XI_SET_FOCUS = 49;
    private static final int XI_GET_FOCUS = 50;
    private static final int XI_GRAB_DEVICE = 51;
    private static final int XI_UNGRAB_DEVICE = 52;
    private static final int XI_ALLOW_EVENTS = 53;
    private static final int XI_LIST_PROPERTIES = 56;
    private static final int XI_GET_PROPERTY = 59;
    private static final int XI_GET_SELECTED_EVENTS = 60;
    private static final int XI_BARRIER_RELEASE_POINTER = 61;
    private static final int XI_CHANGE_HIERARCHY = 43;
    private static final int XI_PASSIVE_GRAB_DEVICE = 54;
    private static final int XI_PASSIVE_UNGRAB_DEVICE = 55;

    /** {@code XIGrabtypeKeycode} and {@code XIGrabtypeButton}: the two grabs this server arms. */
    private static final int XI_GRABTYPE_KEYCODE = 1;
    private static final int XI_GRABTYPE_BUTTON = 2;

    /** {@code XIAnyModifier} in XI2's numbering, where the core protocol says {@code 0x8000}. */
    private static final int XI_ANY_MODIFIER = 0x80000000;

    /** The four changes {@code XIChangeHierarchy} can carry, and the flags the event reports. */
    private static final int XI_ADD_MASTER = 1;
    private static final int XI_REMOVE_MASTER = 2;
    private static final int XI_ATTACH_SLAVE = 3;
    private static final int XI_DETACH_SLAVE = 4;
    private static final int MASTER_ADDED = 1 << 0;
    private static final int MASTER_REMOVED = 1 << 1;
    private static final int SLAVE_ADDED = 1 << 2;
    private static final int SLAVE_REMOVED = 1 << 3;
    private static final int SLAVE_ATTACHED = 1 << 4;
    private static final int SLAVE_DETACHED = 1 << 5;
    private static final int DEVICE_ENABLED = 1 << 6;

    /** XInput's own errors, as offsets from the base the registry hands out. */
    private static final int ERROR_DEVICE = 0;

    /**
     * The extension's own error for a barrier, which is code 12 in its range and has no public name.
     *
     * <p>Measured, not read off a header: {@code XIBarrierReleasePointer} on Xvfb answers error
     * {@code firstError + 12} with the barrier as its bad value (141 of a base of 129, value
     * {@code 0x1234}, minor 61). The public {@code XI.h} only names the first five of the
     * extension's errors, so the number is what a client can compare against.
     */
    private static final int ERROR_BARRIER = 12;

    /** How many buttons and valuators the pointer device says it has. */
    private static final int BUTTONS = 10;

    /** {@code XIAllDevices}/{@code XIAllMasterDevices} are not devices to grab. */
    private final XInputPlugin plugin = new XInputPlugin();

    private XServer server;

    /** The device model, which a client can change with {@code XIChangeHierarchy} (A48's second half). */
    private final cn.oyzh.fx.xserver.input.DeviceTable devices =
            new cn.oyzh.fx.xserver.input.DeviceTable();

    /**
     * The clients that selected the device-state events, which are selectable on
     * {@code XIAllDevices} alone and are about the device set rather than a window.
     *
     * <p>The event carries no window at all (measured: {@code XI_HierarchyChanged} has a fixed part
     * of deviceid, time, flags and the device records - nothing else), so the answer to "who hears
     * it" cannot be a window's selection: a reference delivers it to every client that selected the
     * bit, and GDK is one of them (measured from its mask: device 0, bits 1, 11 and 12). The set is
     * kept here because the selections themselves live on windows.
     */
    private final java.util.Set<ClientConnection> deviceStateWatchers =
            java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

    /** The atom naming an axis, interned once: a valuator class carries one as its label. */
    private int axisX;
    private int axisY;

    @Override
    public String name() {
        return "XInputExtension";
    }

    @Override
    public byte majorOpcode() {
        return plugin.getMajorOpcode();
    }

    @Override
    public byte firstEvent() {
        return plugin.getFirstEvent();
    }

    @Override
    public byte firstError() {
        return plugin.getFirstError();
    }

    @Override
    public XProtocolPlugin plugin() {
        return plugin;
    }

    @Override
    public void init(XServer server) {
        this.server = server;
        this.axisX = server.atoms().intern("Abs X");
        this.axisY = server.atoms().intern("Abs Y");
    }

    @Override
    public XRequest decodeRequest(int minorOpcode, X11Input in) throws IOException {
        return switch (minorOpcode) {
            // **A request that has a reply and is accepted without one hangs the client.** Learned
            // by doing it: `xinput list` begins with the XInput 1.x handshake (minor 1), which is a
            // *reply*, and a server that read the request and said nothing left it waiting for ever
            // with no error and no output. Every request below is therefore either answered,
            // genuinely done, or refused - never accepted and dropped.
            case GET_EXTENSION_VERSION -> GetExtensionVersion.read(in);
            // XInput *1*'s device list, which is not optional: AWT's toolkit asks it as it starts
            // (`XToolkit.checkXInput` then `getNumButtons`) and dereferences what it gets back, so a
            // refusal is a **SIGSEGV** in `libawt_xawt.so`, measured with termora - the same shape as
            // A4, where xdotool died on a keymap it was never really given.
            case LIST_INPUT_DEVICES -> new Bodyless(minorOpcode);
            case OPEN_DEVICE, CLOSE_DEVICE, GET_DEVICE_BUTTON_MAPPING, GET_DEVICE_KEY_MAPPING ->
                    DeviceRequest.read(minorOpcode, in);
            case XI_SELECT_EVENTS -> SelectEvents.read(in);
            case XI_QUERY_VERSION -> VersionQuery.read(in);
            case XI_QUERY_DEVICE -> DeviceQuery.read(in);
            case XI_GET_CLIENT_POINTER -> ClientPointerQuery.read(in);
            case XI_LIST_PROPERTIES -> ListPropertiesRequest.read(in);
            case XI_GET_PROPERTY -> GetPropertyRequest.read(in);
            case XI_GET_SELECTED_EVENTS -> SelectedEventsQuery.read(in);
            case XI_BARRIER_RELEASE_POINTER -> BarrierRelease.read(in);
            case XI_CHANGE_HIERARCHY -> HierarchyChange.read(in);
            case XI_PASSIVE_GRAB_DEVICE -> PassiveGrabRequest.read(in);
            case XI_PASSIVE_UNGRAB_DEVICE -> PassiveUngrabRequest.read(in);
            case XI_QUERY_POINTER -> XIQueryPointerRequest.read(in);
            case XI_WARP_POINTER -> XIWarpPointerRequest.read(in);
            case XI_GRAB_DEVICE -> GrabDevice.read(in);
            case XI_GET_FOCUS -> UnusedQuery.read(minorOpcode);
            // **This one has no reply, and answering it desynchronises the client's whole stream.**
            // Measured: GDK sends XIChangeCursor when a widget's cursor changes - clicking into
            // EasyShell's search box is enough - and a server that answered it with a thirty two
            // byte reply made the client read every later reply four words out of step. It then
            // asked XIQueryPointer almost a million times in two minutes, reading a pointer position
            // that was really a reply header, until the server had no time for anything else.
            case XI_CHANGE_CURSOR -> ChangeCursor.read(in);
            // Decoded only as far as the opcode, because the body is already buffered by the
            // connection and nothing has to be read to stay in step. Each of these has no reply, so
            // there is nothing to hang a client waiting for.
            case XI_SET_CLIENT_POINTER, XI_UNGRAB_DEVICE, XI_ALLOW_EVENTS, XI_SET_FOCUS ->
                    new Accepted(minorOpcode);
            default -> null;
        };
    }

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        switch (request) {
            case GetExtensionVersion legacy -> getExtensionVersion(client, legacy);
            case VersionQuery query -> queryVersion(client, query);
            case ClientPointerQuery query -> getClientPointer(client, query);
            case DeviceQuery query -> queryDevice(client, query);
            case SelectEvents select -> selectEvents(client, select);
            case SelectedEventsQuery query -> getSelectedEvents(client, query);
            case BarrierRelease release -> releaseBarrier(client, release);
            case HierarchyChange change -> changeHierarchy(change);
            case PassiveGrabRequest grab -> passiveGrab(client, grab);
            case PassiveUngrabRequest ungrab -> passiveUngrab(client, ungrab);
            case GetPropertyRequest property -> getProperty(client, property);
            case ListPropertiesRequest list -> listProperties(client, list);
            case XIQueryPointerRequest query -> queryPointer(client, query);
            case XIWarpPointerRequest warp -> warpPointer(client, warp);
            case GrabDevice grab -> grabDevice(server, client, grab);
            case ChangeCursor cursor -> changeCursor(cursor);
            case Bodyless bodyless -> listInputDevices(client, bodyless.minor());
            case DeviceRequest device -> deviceRequest(client, device);
            case UnusedQuery unused -> answerUnused(client, unused);
            case Accepted accepted -> log.debug("{} sent XInput2 request {}: accepted, nothing"
                    + " here to do for it", client, accepted.minor());
            default -> throw ProtocolException.unimplemented(
                    "XInput2 request " + request.getClass().getSimpleName());
        }
    }

    /**
     * {@code GetExtensionVersion} - XInput <em>1.x</em>'s handshake, which is not XI2's.
     *
     * <p>Old, and still the first thing a client asks: {@code xinput} uses it to learn the
     * extension's version before it will speak either, which is why a server that only implements
     * XI2 has to answer it anyway.
     *
     * <p><b>Its layout is not the one the protocol XML suggests, and getting it wrong crashes the
     * client.</b> Measured against Xvfb: the reply's own type byte is the <em>second byte of the
     * message</em> - the header's data byte, not the first field after the header - and the rest
     * follows from a {@code server_major}/{@code server_minor} pair at offsets 8 and 10, with
     * {@code present} as a single byte at 12. Written the way {@code XkbExtension.getKbdByName}
     * writes a reply, whose first field really does start at 8, every field lands four bytes late and
     * {@code present} reads as zero - which is how a server that answers "no" makes
     * {@code xinput list} <b>segfault</b>, exactly as A4's {@code xdotool} did on a keymap it was
     * never really given.
     *
     * <p>The minor version is 2.0 rather than the 2.4 a reference reports: this is the version this
     * server implements, and a client that is told 2.4 will ask for requests that would be refused.
     */
    private void getExtensionVersion(ClientConnection client, GetExtensionVersion request) {
        byte[] reply = new byte[32];
        reply[0] = 1;
        reply[1] = 1;                               // X_GetExtensionVersion, in the header's data byte
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard16(reply, 8, MAJOR_VERSION, client.isBigEndian());
        putCard16(reply, 10, MINOR_VERSION, client.isBigEndian());
        reply[12] = 1;                              // present
        log.debug("{} asked for the XInput extension version, answering {}.{}", client,
                MAJOR_VERSION, MINOR_VERSION);
        send(client, reply);
    }

    /**
     * {@code ListInputDevices}: the XInput 1.x device list, which is what makes AWT survive.
     *
     * <p>Two devices and three class records, in the shape the protocol defines and that a reference
     * was measured answering: a twelve byte header with the device count at byte eight, one eight
     * byte {@code DeviceInfo} per device, one two byte {@code InputClassInfo} per class, then the
     * names as length-prefixed strings. The classes' {@code event_type_base} is the core event type
     * that class's events would be reported with - 4 for a button, 2 for a key, 6 for a motion - and
     * nothing arrives on that rail here, because this server's device events are the XI2 ones.
     */
    private void listInputDevices(ClientConnection client, int minor) {
        List<int[]> devices = xi1Devices();
        int classes = 0;
        for (int[] device : devices) {
            classes += device[1];
        }
        int names = 0;
        for (int[] device : devices) {
            names += 1 + xi1Name(device[0]).length();
        }
        byte[] reply = new byte[align4(32 + devices.size() * 8 + classes * 2 + names)];
        reply[0] = 1;
        reply[1] = (byte) minor;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(reply, 4, (reply.length - 32) / 4, client.isBigEndian());
        reply[8] = (byte) devices.size();
        int at = 32;
        for (int[] device : devices) {
            putCard16(reply, at + 4, device[0], client.isBigEndian());   // device id
            reply[at + 6] = (byte) device[1];                            // class count
            reply[at + 7] = (byte) device[2];                            // use
            at += 8;
        }
        for (int[] device : devices) {
            for (int[] info : xi1Classes(device[0])) {
                reply[at] = (byte) info[0];
                reply[at + 1] = (byte) info[1];
                at += 2;
            }
        }
        for (int[] device : devices) {
            String name = xi1Name(device[0]);
            reply[at] = (byte) name.length();
            for (int i = 0; i < name.length(); i++) {
                reply[at + 1 + i] = (byte) name.charAt(i);
            }
            at += 1 + name.length();
        }
        send(client, reply);
    }

    /**
     * {@code OpenDevice} and the two mappings AWT reads its input model from.
     *
     * <p>AWT opens the pointer to count its buttons and the keyboard to read its keymap, so these
     * answer from the same tables {@code XIQueryDevice} and the core requests do - one device model
     * with two faces, rather than two that can disagree.
     */
    private void deviceRequest(ClientConnection client, DeviceRequest request) {
        if (request.minor() == CLOSE_DEVICE) {
            return;                                 // nothing was opened: there is no per-client state
        }
        int device = request.deviceId();
        checkDevice(device);
        byte[] reply = new byte[32 + (request.minor() == OPEN_DEVICE
                ? xi1Classes(device).size() * 2 : 0)];
        reply[0] = 1;
        reply[1] = (byte) request.minor();
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(reply, 4, (reply.length - 32) / 4, client.isBigEndian());
        if (request.minor() == OPEN_DEVICE) {
            List<int[]> classes = xi1Classes(device);
            reply[8] = (byte) classes.size();
            int at = 32;
            for (int[] info : classes) {
                reply[at] = (byte) info[0];
                reply[at + 1] = (byte) info[1];
                at += 2;
            }
        } else if (request.minor() == GET_DEVICE_BUTTON_MAPPING) {
            // The default mapping, which is what a stock server answers: button n maps to button n.
            reply = new byte[36];
            reply[0] = 1;
            reply[1] = (byte) request.minor();
            putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
            putCard32(reply, 4, 1, client.isBigEndian());
            reply[8] = (byte) BUTTONS;
            for (int i = 0; i < BUTTONS; i++) {
                reply[32 + i] = (byte) (i + 1);
            }
        } else {
            // GetDeviceKeyMapping: the keysyms of the requested keycodes, from the one keymap both
            // rails read.
            int first = request.firstKeycode();
            int count = request.count();
            int perKeycode = 2;
            reply = new byte[align4(32 + count * perKeycode * 4)];
            reply[0] = 1;
            reply[1] = (byte) request.minor();
            putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
            putCard32(reply, 4, count * perKeycode, client.isBigEndian());
            reply[8] = (byte) perKeycode;
            for (int i = 0; i < count; i++) {
                int keycode = first + i;
                if (keycode > cn.oyzh.fx.xserver.input.Keymap.MAX_KEYCODE) {
                    break;
                }
                int[] syms = cn.oyzh.fx.xserver.input.Keymap.keysymsFor(keycode);
                for (int level = 0; level < perKeycode; level++) {
                    int sym = syms != null && level < syms.length ? syms[level] : 0;
                    putCard32(reply, 32 + (i * perKeycode + level) * 4, sym, client.isBigEndian());
                }
            }
        }
        send(client, reply);
    }

    /** This server's devices as XInput 1 numbers them: id, class count and {@code DeviceUse}. */
    private List<int[]> xi1Devices() {
        // **A master a client *added* is not in this list, which is measured**: after an
        // `XIAddMaster` on a reference, `ListInputDevices` listed eight devices - the core pointer
        // and keyboard, its four slaves, and the two XTEST slaves of the master that was added - and
        // not the added master pair itself. XI 1 is the core pointer's and keyboard's own model.
        List<int[]> listed = new ArrayList<>();
        for (cn.oyzh.fx.xserver.input.DeviceTable.Device device : devices.all()) {
            if (device.isMaster() && device.id() != XInputMasks.POINTER_DEVICE
                    && device.id() != XInputMasks.KEYBOARD_DEVICE) {
                continue;
            }
            int use;
            if (device.id() == XInputMasks.POINTER_DEVICE) {
                use = USE_IS_X_POINTER;
            } else if (device.id() == XInputMasks.KEYBOARD_DEVICE) {
                use = USE_IS_X_KEYBOARD;
            } else if (device.isPointer()) {
                use = USE_IS_X_EXTENSION_POINTER;
            } else {
                use = USE_IS_X_EXTENSION_KEYBOARD;
            }
            listed.add(new int[] {device.id(), device.isPointer() ? 2 : 1, use});
        }
        return listed;
    }

    /** The class records of one device: class id and the core event type it would be reported with. */
    private List<int[]> xi1Classes(int device) {
        cn.oyzh.fx.xserver.input.DeviceTable.Device known = devices.byId(device);
        return known == null || known.isPointer()
                ? List.of(new int[] {1, 4}, new int[] {2, 6})       // Button, Valuator
                : List.of(new int[] {0, 2});                        // Key
    }

    private static int align4(int size) {
        return (size + 3) & ~3;
    }

    /** {@code DeviceUse}: what a device is for, in XInput 1's numbering. */
    private static final int USE_IS_X_POINTER = 0;
    private static final int USE_IS_X_KEYBOARD = 1;

    /**
     * And the two a *slave* has, which is what a reference's XTEST pair reports: measured with
     * {@code XiProbe} on 2026-10-04 - device 4 is {@code use 4} and device 5 is {@code use 3}.
     */
    private static final int USE_IS_X_EXTENSION_POINTER = 4;
    private static final int USE_IS_X_EXTENSION_KEYBOARD = 3;

    /** The names the two devices answer to, by id. */
    private String xi1Name(int device) {
        cn.oyzh.fx.xserver.input.DeviceTable.Device known = devices.byId(device);
        return known == null ? "" : known.name();
    }

    /**
     * {@code XIChangeCursor}: sets a window's cursor, and says nothing back.
     *
     * <p>With one pointer this is the core request with a device name on it, so it sets the same
     * window attribute the core path does - a client that changes the cursor over a text field and
     * gets neither the change nor an error would keep the arrow over its own I-beam.
     */
    private void changeCursor(ChangeCursor request) {
        XWindow window = checkWindow(request.window());
        cn.oyzh.fx.xserver.resource.XResource cursor =
                request.cursor() == 0 ? null : server.resources().get(request.cursor());
        if (request.cursor() != 0
                && !(cursor instanceof cn.oyzh.fx.xserver.resource.XCursor)) {
            throw ProtocolException.badCursor(request.cursor());
        }
        window.setCursor((cn.oyzh.fx.xserver.resource.XCursor) cursor);
        log.debug("XInput2 sets the cursor of 0x{} to {}", Integer.toHexString(window.id()),
                request.cursor() == 0 ? "none" : "0x" + Integer.toHexString(request.cursor()));
    }

    /**
     * The requests this server has nothing to say about, answered rather than refused.
     *
     * <p>{@code XIGetFocus} asks where the keyboard focus is - a question with an answer, and this
     * server's answer is the window the focus is on. {@code XIChangeCursor} sets a window's cursor,
     * which every client that draws its own pointer does. Neither is core to XI2, and both are sent
     * by real toolkits, so refusing them would be an X protocol error where a client expects a
     * working call - and an error under Xlib's default handler ends the client's process.
     */
    private void answerUnused(ClientConnection client, UnusedQuery request) {
        byte[] reply = new byte[32];
        reply[0] = 1;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        if (request.minor() == XI_GET_FOCUS) {
            XWindow focus = server.input().inputFocus();
            putCard32(reply, 8, focus == null ? 0 : focus.id(), client.isBigEndian());
        }
        send(client, reply);
        log.debug("{} sent XInput2 request {}: answered", client, request.minor());
    }

    // ------------------------------------------------------------------
    // The requests
    // ------------------------------------------------------------------

    /**
     * {@code XIQueryVersion}: the client says what it speaks and is told what this server does.
     *
     * <p>A client that is not told it may use XI2 will not: GDK, JavaFX and every toolkit that has
     * the code path ask this before anything else.
     */
    private void queryVersion(ClientConnection client, VersionQuery query) {
        // **The lower of the two versions, and never a lie about the minor** - measured against Xvfb
        // 2026-10-04, one connection per ask: 2.0 -> 2.0, 2.3 -> 2.3, 2.5 -> 2.4, **3.0 -> 2.4**
        // (a client whose major is ahead is told what this server speaks and decides for itself),
        // and a major *below* 2 is not a version negotiation at all: it is **BadValue** with the
        // requested major as its bad value (measured: error 2, value 1, minor 47).
        if (query.major() < MAJOR_VERSION) {
            throw new ProtocolException(ProtocolException.VALUE, query.major(),
                    "XInput2 was asked for major version " + query.major());
        }
        int major = Math.min(query.major(), MAJOR_VERSION);
        int minor = query.major() > MAJOR_VERSION
                ? MINOR_VERSION
                : Math.min(query.minor(), MINOR_VERSION);
        byte[] reply = new byte[32];
        reply[0] = 1;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard16(reply, 8, major, client.isBigEndian());
        putCard16(reply, 10, minor, client.isBigEndian());
        log.debug("{} asked for XInput2 {}.{}, answering {}.{}", client, query.major(),
                query.minor(), major, minor);
        send(client, reply);
    }

    /** {@code XIGetClientPointer}: which device this client's pointer events come from. */
    private void getClientPointer(ClientConnection client, ClientPointerQuery query) {
        checkWindow(query.window());
        byte[] reply = new byte[32];
        reply[0] = 1;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        // Not set, which is what a reference answers for a client that has never chosen one
        // (measured: XIGetClientPointer on Xvfb answers set=0 device=0). A client reads that as "the
        // default pointer", which is the one there is.
        reply[8] = 0;
        putCard16(reply, 10, 0, client.isBigEndian());
        send(client, reply);
    }

    /**
     * {@code XIQueryDevice}: the devices a client may select events from.
     *
     * <p>Two masters, which is the honest answer for a server with one pointer and one keyboard, and
     * the one a reference server's first two entries are. Their ids (2 and 3) and their names are
     * Xvfb's, measured, because a client caches them.
     *
     * <p>The classes are what a client reads to know what the device can do, and each one's length
     * is in the class record itself: a key class lists the keycodes the device has, a button class
     * its buttons and their atom labels, a valuator class one axis with its range and its current
     * value. Everything is positional, so the fields were read off a captured reference reply -
     * {@code tools/XiProbe ... dump}.
     */
    private void queryDevice(ClientConnection client, DeviceQuery query) {
        List<cn.oyzh.fx.xserver.input.DeviceTable.Device> wanted =
                devicesFor(query.deviceId());
        List<byte[]> records = new ArrayList<>();
        for (cn.oyzh.fx.xserver.input.DeviceTable.Device device : wanted) {
            records.add(deviceRecord(device));
        }
        int body = records.stream().mapToInt(d -> d.length).sum();
        int padding = (4 - body % 4) % 4;
        byte[] reply = new byte[32 + body + padding];
        reply[0] = 1;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(reply, 4, (reply.length - 32) / 4, client.isBigEndian());
        putCard16(reply, 8, records.size(), client.isBigEndian());
        int at = 32;
        for (byte[] device : records) {
            System.arraycopy(device, 0, reply, at, device.length);
            at += device.length;
        }
        send(client, reply);
    }

    /** {@code XIQueryDevice}'s devices for a selector, or the one device named. */
    private List<cn.oyzh.fx.xserver.input.DeviceTable.Device> devicesFor(
            int deviceId) {
        if (deviceId == XInputMasks.ALL_DEVICES || deviceId == XInputMasks.ALL_MASTER_DEVICES) {
            // XIAllMasterDevices takes the masters alone, which is what that selector means.
            return devices.forSelector(deviceId);
        }
        cn.oyzh.fx.xserver.input.DeviceTable.Device device = devices.byId(deviceId);
        if (device == null) {
            throw new ProtocolException((byte) (firstError() + ERROR_DEVICE), deviceId,
                    "bad device " + deviceId);
        }
        return List.of(device);
    }

    /**
     * One device's {@code XIDeviceInfo}, as a reference describes it.
     *
     * <p>The four devices this server has since A48 are the reference's own first four, measured
     * with {@code XiProbe device 0} on 2026-10-04: the two masters (2, 3) and the two **XTEST**
     * slaves (4, 5) attached to them, and the class records of a master name the **slave** the class
     * comes from as their {@code sourceid} - device 2's button and axis classes say {@code sourceid
     * 4}, not 2, which is what makes a client able to tell where a master's input really comes from.
     */
    private byte[] deviceRecord(cn.oyzh.fx.xserver.input.DeviceTable.Device device) {
        if (device.isPointer()) {
            return pointerLike(device.id(), device.type(), device.attachment(), device.name(),
                    device.classSource());
        }
        return keyboardLike(device.id(), device.type(), device.attachment(), device.name(),
                device.classSource());
    }

    /** The pointer: a button class and the two axes, as a reference describes one. */
    private byte[] pointerLike(int id, int type, int attachment, String name, int source) {
        // A button class is its header, the button count, one state word per thirty two buttons and
        // one atom label per button. The state word is easy to leave out and the class length then
        // stops being a multiple of four - twelve units where a reference sends thirteen - which
        // shifts every class after it and makes the *next* device read as nonsense.
        int stateWords = (BUTTONS + 31) / 32;
        byte[] buttonClass = new byte[6 + 2 + stateWords * 4 + BUTTONS * 4];
        putCard16(buttonClass, 0, 1, false);                    // DeviceClassType.Button
        putCard16(buttonClass, 2, buttonClass.length / 4, false);
        putCard16(buttonClass, 4, source, false);
        putCard16(buttonClass, 6, BUTTONS, false);
        cn.oyzh.fx.xserver.input.InputState input = server.inputFor(server.screen());
        byte[][] classes = {
                buttonClass,
                valuatorClass(0, axisX, 0, server.screen().width(), input.pointerX(), source),
                valuatorClass(1, axisY, 0, server.screen().height(), input.pointerY(), source),
        };
        return deviceInfo(id, type, attachment, name, classes);
    }

    /** The keyboard: one key class, listing every keycode it has. */
    private byte[] keyboardLike(int id, int type, int attachment, String name, int source) {
        int first = cn.oyzh.fx.xserver.input.Keymap.MIN_KEYCODE;
        int count = cn.oyzh.fx.xserver.input.Keymap.MAX_KEYCODE - first + 1;
        byte[] keys = new byte[6 + 2 + count * 4];
        putCard16(keys, 0, 0, false);
        putCard16(keys, 2, keys.length / 4, false);
        putCard16(keys, 4, source, false);
        putCard16(keys, 6, count, false);
        for (int i = 0; i < count; i++) {
            putCard32(keys, 8 + i * 4, first + i, false);
        }
        return deviceInfo(id, type, attachment, name, new byte[][] {keys});
    }

    /**
     * One valuator class: an axis with its number, its atom label, its range, its value and how it
     * is reported.
     *
     * <p>{@code mode} 0 is absolute, which is what a pointer's axes are - the pointer's position,
     * not a delta. The values are {@code FP3232}, a 32 bit integer part and a 32 bit fraction.
     */
    private byte[] valuatorClass(int number, int label, int min, int max, int value, int source) {
        byte[] axis = new byte[44];
        putCard16(axis, 0, 2, false);
        putCard16(axis, 2, axis.length / 4, false);
        putCard16(axis, 4, source, false);
        putCard16(axis, 6, number, false);
        putCard32(axis, 8, label, false);
        putFixed(axis, 12, min);
        putFixed(axis, 20, max);
        putFixed(axis, 28, value);
        putCard32(axis, 36, 0, false);              // resolution: unknown
        axis[40] = 0;                               // mode: absolute
        return axis;
    }

    /** One {@code XIDeviceInfo}: the device, its name, and its classes. */
    private byte[] deviceInfo(int id, int type, int attachment, String name, byte[][] classes) {
        int nameLength = name.length();
        int at = 12 + ((nameLength + 3) & ~3);
        int body = at;
        for (byte[] deviceClass : classes) {
            body += deviceClass.length;
        }
        byte[] info = new byte[body];
        putCard16(info, 0, id, false);
        putCard16(info, 2, type, false);
        putCard16(info, 4, attachment, false);
        putCard16(info, 6, classes.length, false);
        putCard16(info, 8, nameLength, false);
        info[10] = 1;                               // enabled
        for (int i = 0; i < nameLength; i++) {
            info[12 + i] = (byte) name.charAt(i);
        }
        int classAt = at;
        for (byte[] deviceClass : classes) {
            System.arraycopy(deviceClass, 0, info, classAt, deviceClass.length);
            classAt += deviceClass.length;
        }
        return info;
    }

    /**
     * {@code XISelectEvents}: what a client wants to hear from a device on a window.
     *
     * <p>The selection is kept on the window, so it goes away with it. A zero mask clears it, which
     * is how a client unsubscribes.
     */
    private void selectEvents(ClientConnection client, SelectEvents select) {
        XWindow window = checkWindow(select.window());
        for (SelectEvents.Mask mask : select.masks()) {
            checkDevice(mask.deviceId());
            checkSelectable(mask.deviceId(), mask.mask());
            if (mask.deviceId() == XInputMasks.ALL_DEVICES
                    && XInputMasks.hasBit(mask.mask(), XInputMasks.HIERARCHY_CHANGED)) {
                // The hierarchy event names no window, so "who hears it" cannot be a window's own
                // selection: a reference delivers it to every client that selected the bit, and GDK
                // is one of them (measured from its mask: XIAllDevices, bits 1, 11 and 12).
                deviceStateWatchers.add(client);
            }
            window.selectXi2(client, mask.deviceId(), mask.mask());
            if (log.isDebugEnabled()) {
                log.debug("{} selects XInput2 device {} mask {} on 0x{}", client, mask.deviceId(),
                        java.util.Arrays.toString(mask.mask()), Integer.toHexString(window.id()));
            }
        }
    }

    /**
     * {@code XIPassiveGrabDevice} (XI 2.0): a grab armed on a window that fires when its key or
     * button is pressed there.
     *
     * <p>Measured on Xvfb 2026-10-04: the request is answered with a reply whose
     * {@code num_modifiers} counts the modifiers that are <em>already</em> grabbed for the same
     * window and detail by another client - nought when nothing conflicts - and the grab itself takes
     * effect (the reply is the answer, not a promise). This server arms it on the table its core
     * {@code GrabKey}/{@code GrabButton} already use, so a press inside the window activates the grab
     * and the owner gets the events its mask names until the release.
     *
     * <p><b>The mask is translated</b>: a passive grab names it in XI2's numbering (one bit per event
     * number) and the delivery here works in core masks - {@link XInputMasks#coreMaskFor} is that
     * correspondence. The events the owner receives are therefore the ones it named, in whichever
     * rail its own selection on that window chooses: a client that selected XI2 gets the device
     * events, which is what a toolkit does.
     *
     * <p>Enter/leave and touch grabs (types 3 and up) are refused with {@code BadValue} naming the
     * type: this server has no touch device (either server's bed has none) and a crossing grab is not
     * something its input path arms, so an error a client can see is better than a grab that never
     * fires.
     */
    private void passiveGrab(ClientConnection client, PassiveGrabRequest request) {
        XWindow window = checkWindow(request.window());
        boolean pointer = grabIsPointer(request.grabType());
        checkDevice(request.deviceId());
        int coreMask = 0;
        for (int bit = 0; bit < request.mask().length * 32; bit++) {
            if (XInputMasks.hasBit(request.mask(), bit)) {
                coreMask |= XInputMasks.coreMaskFor(bit);
            }
        }
        List<Integer> conflicts = new ArrayList<>();
        for (int modifier : request.modifiers()) {
            for (cn.oyzh.fx.xserver.handler.InputHandlers.PassiveGrab armed
                    : server.passiveGrabs()) {
                if (armed.client() != client && armed.window() == window
                        && armed.pointer() == pointer && armed.detail() == request.detail()
                        && armed.modifiers() == coreModifier(modifier)) {
                    conflicts.add(modifier);
                    break;
                }
            }
        }
        for (int modifier : request.modifiers()) {
            int core = coreModifier(modifier);
            server.passiveGrabs().removeIf(grab -> grab.client() == client
                    && grab.window() == window && grab.pointer() == pointer
                    && grab.detail() == request.detail() && grab.modifiers() == core);
            server.passiveGrabs().add(new cn.oyzh.fx.xserver.handler.InputHandlers
                    .PassiveGrab(client, window, request.ownerEvents(), coreMask, pointer,
                    request.detail(), core, request.grabMode(), request.grabMode()));
        }
        byte[] reply = new byte[32 + conflicts.size() * 4];
        reply[0] = 1;
        reply[1] = (byte) XI_PASSIVE_GRAB_DEVICE;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(reply, 4, (reply.length - 32) / 4, client.isBigEndian());
        putCard16(reply, 8, conflicts.size(), client.isBigEndian());
        for (int i = 0; i < conflicts.size(); i++) {
            putCard32(reply, 32 + i * 4, conflicts.get(i), client.isBigEndian());
        }
        send(client, reply);
        if (log.isDebugEnabled()) {
            log.debug("{} armed an XI2 passive {} grab on 0x{} for detail {} with mask 0x{}: {}"
                            + " conflict(s)", client, pointer ? "button" : "key",
                    Integer.toHexString(window.id()), request.detail(),
                    Integer.toHexString(coreMask), conflicts.size());
        }
    }

    /**
     * {@code XIPassiveUngrabDevice}: the counter-request, accepted in silence (measured).
     *
     * <p>{@code detail} 0 means every detail, and {@code XIAnyModifier} every modifier, which is the
     * same rule the core {@code UngrabKey} follows.
     */
    private void passiveUngrab(ClientConnection client, PassiveUngrabRequest request) {
        XWindow window = checkWindow(request.window());
        boolean pointer = grabIsPointer(request.grabType());
        for (int modifier : request.modifiers()) {
            server.passiveGrabs().removeIf(grab -> grab.client() == client && grab.window() == window
                    && grab.pointer() == pointer
                    && (request.detail() == 0 || grab.detail() == request.detail())
                    && (modifier == XI_ANY_MODIFIER || grab.modifiers() == coreModifier(modifier)));
        }
    }

    /** Whether a grab type is about the pointer, and refusing the ones that are neither. */
    private static boolean grabIsPointer(int grabType) {
        if (grabType == XI_GRABTYPE_KEYCODE) {
            return false;
        }
        if (grabType == XI_GRABTYPE_BUTTON) {
            return true;
        }
        throw new ProtocolException(ProtocolException.VALUE, grabType,
                "grab type " + grabType + " is not one this server arms");
    }

    /** XI2's modifier values in the core numbering the grab table uses. */
    private static int coreModifier(int modifier) {
        return modifier == XI_ANY_MODIFIER ? 0x8000 : modifier;
    }

    /**
     * {@code XI_PassiveGrabDevice}: the window, the detail, the device, the modifiers and the mask,
     * whose two lists are sized by fields inside the request.
     */
    private record PassiveGrabRequest(int window, int detail, int deviceId, int grabType,
                                      int grabMode, boolean ownerEvents,
                                      List<Integer> modifiers, int[] mask)
            implements XRequest {
        static PassiveGrabRequest read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            in.readInt32();                                 // time
            int window = in.readInt32();
            in.readInt32();                                 // cursor
            int detail = in.readInt32();
            int deviceId = in.readCard16();
            int numModifiers = in.readCard16();
            int maskLen = in.readCard16();
            int grabType = in.readCard8();
            int grabMode = in.readCard8();
            in.readCard8();                                 // paired_device_mode
            int ownerEvents = in.readCard8();
            in.readPad(2);
            List<Integer> modifiers = new ArrayList<>();
            for (int i = 0; i < numModifiers; i++) {
                modifiers.add(in.readInt32());
            }
            int[] mask = new int[maskLen];
            for (int i = 0; i < maskLen; i++) {
                mask[i] = in.readInt32();
            }
            return new PassiveGrabRequest(window, detail, deviceId, grabType, grabMode,
                    ownerEvents != 0, modifiers, mask);
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** {@code XI_PassiveUngrabDevice}: the same names with no mask and no reply. */
    private record PassiveUngrabRequest(int window, int detail, int grabType,
                                        List<Integer> modifiers) implements XRequest {
        static PassiveUngrabRequest read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            int window = in.readInt32();
            int detail = in.readInt32();
            in.readCard16();                                // deviceid
            int numModifiers = in.readCard16();
            int grabType = in.readCard8();
            in.readCard8();                                 // pad
            in.readCard16();                                // pad
            List<Integer> modifiers = new ArrayList<>();
            for (int i = 0; i < numModifiers; i++) {
                modifiers.add(in.readInt32());
            }
            return new PassiveUngrabRequest(window, detail, grabType, modifiers);
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /**
     * {@code XIChangeHierarchy}: the request that makes the device model change (A48's second half).
     *
     * <p>What each change does is measured, not recalled - the device list and the event were read
     * off a reference on 2026-10-04, and {@link cn.oyzh.fx.xserver.input.DeviceTable} carries
     * the numbers. The two refusals are measured too: removing the **core** master is
     * {@code BadValue} (error 2, value 0) and attaching something that is not a slave is this
     * extension's {@code BadDevice} naming the device.
     *
     * <p>The event goes out after every change in the list has been applied, with the flags of all of
     * them: it describes the device tree <em>after</em> the request, which is what a client needs to
     * rebuild its model, and it is one event per request rather than one per change.
     */
    private void changeHierarchy(HierarchyChange request) {
        int flags = 0;
        for (HierarchyChange.Change change : request.changes()) {
            switch (change.type()) {
                case XI_ADD_MASTER -> {
                    if (change.name().isEmpty()) {
                        throw new ProtocolException(ProtocolException.VALUE, 0,
                                "a master needs a name");
                    }
                    devices.addMaster(change.name());
                    flags |= MASTER_ADDED | SLAVE_ADDED | SLAVE_ATTACHED | DEVICE_ENABLED;
                }
                case XI_REMOVE_MASTER -> {
                    if (!devices.isRemovable(change.deviceId())) {
                        // The core pointer and keyboard are not a client's to remove, which is what a
                        // reference answers for them (measured: BadValue with a bad value of 0).
                        throw new ProtocolException(ProtocolException.VALUE, 0,
                                "the core masters cannot be removed");
                    }
                    devices.removeMaster(change.deviceId());
                    flags |= MASTER_REMOVED | SLAVE_REMOVED;
                }
                case XI_ATTACH_SLAVE -> {
                    cn.oyzh.fx.xserver.input.DeviceTable.Device slave =
                            devices.byId(change.deviceId());
                    if (slave == null || slave.isMaster()) {
                        throw new ProtocolException((byte) (firstError() + ERROR_DEVICE),
                                change.deviceId(), "attach: not a slave " + change.deviceId());
                    }
                    if (!devices.isDevice(change.returnPointer())) {
                        throw new ProtocolException((byte) (firstError() + ERROR_DEVICE),
                                change.returnPointer(), "attach: not a master");
                    }
                    devices.attachSlave(change.deviceId(), change.returnPointer());
                    flags |= SLAVE_ATTACHED;
                }
                case XI_DETACH_SLAVE -> {
                    cn.oyzh.fx.xserver.input.DeviceTable.Device slave =
                            devices.byId(change.deviceId());
                    if (slave == null || slave.isMaster()) {
                        throw new ProtocolException((byte) (firstError() + ERROR_DEVICE),
                                change.deviceId(), "detach: not a slave " + change.deviceId());
                    }
                    devices.detachSlave(change.deviceId());
                    flags |= SLAVE_DETACHED;
                }
                default -> throw new ProtocolException(ProtocolException.VALUE, change.type(),
                        "unknown hierarchy change " + change.type());
            }
        }
        sendHierarchyChanged(flags);
    }

    /**
     * {@code XI_HierarchyChanged}, in the shape a reference sends (measured 2026-10-04): a thirty
     * two byte fixed part - {@code deviceid} 0, the time, the flags and the count - and then one
     * twelve byte record per device the server has, which is the whole tree after the change (344
     * bytes with 26 devices on the reference this was measured on).
     */
    private void sendHierarchyChanged(int flags) {
        List<cn.oyzh.fx.xserver.input.DeviceTable.Device> all = devices.all();
        for (ClientConnection client : deviceStateWatchers) {
            byte[] event = new byte[32 + all.size() * 12];
            event[0] = 35;                                  // GenericEvent
            event[1] = majorOpcode();
            putCard16(event, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
            putCard32(event, 4, (event.length - 32) / 4, client.isBigEndian());
            putCard16(event, 8, XInputMasks.HIERARCHY_CHANGED, client.isBigEndian());
            putCard16(event, 10, XInputMasks.ALL_DEVICES, client.isBigEndian());
            putCard32(event, 12, (int) server.events().time(), client.isBigEndian());
            putCard32(event, 16, flags, client.isBigEndian());
            putCard16(event, 20, all.size(), client.isBigEndian());
            int at = 32;
            for (cn.oyzh.fx.xserver.input.DeviceTable.Device device : all) {
                putCard16(event, at, device.id(), client.isBigEndian());
                putCard16(event, at + 2, device.attachment(), client.isBigEndian());
                event[at + 4] = (byte) device.type();
                event[at + 5] = (byte) (device.enabled() ? 1 : 0);
                putCard32(event, at + 8, 0, client.isBigEndian());   // per-device change flags
                at += 12;
            }
            send(client, event);
        }
    }

    /**
     * The three device-state events are about the server's device set, not about a window, and a
     * reference refuses them for any device but {@code XIAllDevices}.
     *
     * <p>Measured 2026-10-04: {@code XI_HierarchyChanged} (bit 11) selected on {@code
     * XIAllMasterDevices} is <b>BadValue naming the bit</b> (error 2, value 11), while the same bit
     * and its two neighbours (1, 12) on {@code XIAllDevices} are accepted - and the second is what
     * GDK selects, measured from this server's own log of its masks. Accepting them everywhere looks
     * harmless until a client uses the answer to know whether it will hear about a device appearing:
     * a reference tells it plainly that it will not.
     */
    private void checkSelectable(int deviceId, int[] mask) {
        if (deviceId == XInputMasks.ALL_DEVICES) {
            return;
        }
        for (int bit : new int[] {XInputMasks.DEVICE_CHANGED, XInputMasks.HIERARCHY_CHANGED,
                XInputMasks.PROPERTY_EVENT}) {
            if (XInputMasks.hasBit(mask, bit)) {
                throw new ProtocolException(ProtocolException.VALUE, bit,
                        "event " + bit + " is only selectable on XIAllDevices");
            }
        }
    }

    /**
     * {@code XIBarrierReleasePointer} (XI 2.3), for a barrier that is not here.
     *
     * <p>Pointer barriers are created through <b>XFIXES 5.0</b> ({@code CreatePointerBarrier}) and
     * this server's XFIXES answers 4.0, so no barrier can exist on it - and the answer for a
     * barrier that does not exist is exactly what a client is owed and what a reference gives it:
     * the extension's own barrier error, code 12, with the barrier as its bad value (measured
     * 2026-10-04: 141 of a base of 129, value {@code 0x1234}, minor 61, on Xvfb - where the barrier
     * really did not exist either). Answering {@code BadRequest} instead, which is what this server
     * did, tells a client its request was malformed when in fact it was fine.
     *
     * <p>An empty request is accepted in silence: there is nothing to release and nothing to
     * refuse.
     */
    private void releaseBarrier(ClientConnection client, BarrierRelease request) {
        if (request.count() == 0) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("{} released barrier 0x{} on device {}: no such barrier here (XFIXES is"
                            + " {} and creates none)", client, Integer.toHexString(request.barrier()),
                    request.deviceId(), XFixesExtension.reportedVersion());
        }
        throw new ProtocolException((byte) (firstError() + ERROR_BARRIER), request.barrier(),
                "no such barrier " + request.barrier());
    }

    /** {@code XIGetSelectedEvents}: what this client selected on a window. */
    private void getSelectedEvents(ClientConnection client, SelectedEventsQuery query) {
        XWindow window = checkWindow(query.window());
        List<XWindow.Xi2Selection> mine = new ArrayList<>();
        for (XWindow.Xi2Selection selection : window.xi2Selections()) {
            if (selection.client() == client) {
                mine.add(selection);
            }
        }
        int body = 0;
        for (XWindow.Xi2Selection selection : mine) {
            body += 4 + selection.mask().length * 4;
        }
        byte[] reply = new byte[32 + body + ((4 - body % 4) % 4)];
        reply[0] = 1;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(reply, 4, (reply.length - 32) / 4, client.isBigEndian());
        putCard16(reply, 8, mine.size(), client.isBigEndian());
        int at = 32;
        for (XWindow.Xi2Selection selection : mine) {
            putCard16(reply, at, selection.deviceId(), client.isBigEndian());
            putCard16(reply, at + 2, selection.mask().length, client.isBigEndian());
            for (int word = 0; word < selection.mask().length; word++) {
                putCard32(reply, at + 4 + word * 4, selection.mask()[word], client.isBigEndian());
            }
            at += 4 + selection.mask().length * 4;
        }
        send(client, reply);
    }

    /**
     * {@code XIListProperties}: which properties a device has, and the answer is "none" - the request
     * {@code xinput list-props} sends first, and the one that decides whether the property family is
     * worth using at all.
     *
     * <p><b>It used to be refused as an unknown minor</b> - a {@code BadRequest}, which is the same
     * shape as {@link #getProperty}'s and was found the same way ({@code xinput list-props 2} against
     * this server refuses where it lists two properties against a reference). The reply is a bare
     * header: the count is a CARD16 at 8, the atoms follow the header, and byte 1 carries the request's
     * minor opcode - all three read off a reference's own reply for a device that <em>has</em>
     * properties ({@code XiProbe listprops 2}), because the empty answer this server gives is the same
     * reply with the count at zero.
     */
    private void listProperties(ClientConnection client, ListPropertiesRequest request) {
        checkRealDevice(request.device());
        byte[] reply = new byte[32];
        reply[0] = 1;
        reply[1] = (byte) XI_LIST_PROPERTIES;   // measured: a reference puts the minor opcode here
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        // num_properties at 8 stays zero, and there is no tail: this server's devices have none.
        send(client, reply);
        log.debug("{} listed device properties on device {}: this server has no device properties",
                client, request.device());
    }

    /**
     * {@code XIGetProperty}: a device property read, and <b>this server has no device properties</b> -
     * so every one of them is absent, and that is what is answered.
     *
     * <p><b>It used to be refused, and refusing cost a forwarded client a protocol error as it
     * started.</b> A live {@code EasyShell} asks this (minor 59) between two {@code InternAtom} calls,
     * and an unknown minor reaches the hand-written decoder and throws - so the answer was
     * {@code BadRequest}, not "this device has no such property". A client that reads a property and
     * gets an error has the shape of the failure that already crashed one client here
     * ({@code libawt_xawt.so} on {@code ListInputDevices}), which is why the refusal was worth
     * turning into an answer.
     *
     * <p><b>The answer is a reference's, measured rather than reasoned.</b> {@code Xvfb} answers a
     * property that does not exist with a thirty two byte reply whose only non-zero fields are the
     * header's: {@code type = None}, {@code bytes_after = 0}, {@code num_items = 0},
     * {@code format = 0} - and byte 1 carries the request's <em>minor opcode</em>, which was measured
     * by asking about two different devices and watching it stay 59. That last field is why a reply
     * built as "thirty two zero bytes with a one in front" is worth checking: a field a client reads
     * positionally is a plausible wrong number rather than an error.
     *
     * <p><b>{@code XIGetProperty} expects a reply, so "accept and do nothing" is not an option</b> -
     * it would leave the client waiting for one, which is worse than the error. The request's fixed
     * part is <b>six four byte units</b>, which was measured too: a reference refuses a seven unit
     * request with {@code BadLength} and answers a six unit one, so what a real client sends is the
     * shorter form and the {@code delete} flag some readings of the request put at the end is not
     * part of it.
     */
    private void getProperty(ClientConnection client, GetPropertyRequest request) {
        checkRealDevice(request.device());
        if (request.property() == 0 || server.atoms().name(request.property()) == null) {
            throw ProtocolException.badAtom(request.property());
        }
        byte[] reply = new byte[32];
        reply[0] = 1;
        reply[1] = (byte) XI_GET_PROPERTY;      // measured: a reference puts the minor opcode here
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        // type, bytes_after, num_items and format stay zero: none of this server's devices has a
        // property, so every read comes back "not there" - which is what a reference answers for the
        // read of a property that does not exist (`format = 0` and nothing after the header).
        send(client, reply);
        log.debug("{} read device property {} on device {}: this server has no device properties,"
                        + " answering \"absent\"", client, server.atoms().name(request.property()),
                request.device());
    }

    /**
     * {@code XIQueryPointer}: where the pointer is, and what it is over.
     *
     * <p>The same question the core request asks, answered from the same state, and with the same
     * positional care: the reply is <b>forty four bytes</b>, not the thirty two every core reply is,
     * because the two fixed-point coordinate pairs and the modifier groups come after the point
     * where a core reply would have ended. Written into a thirty two byte array - which is what this
     * did first - the last pair runs off the end and the client is answered an exception instead of a
     * position.
     *
     * <p>Measured against Xvfb by way of the client: GDK asks this whenever it wants to know what
     * the pointer is over, and one wrong reply made EasyShell die with {@code free(): invalid size} -
     * a client's own allocator, told a length that did not match what it had read.
     *
     * <p>{@code same_screen} is at offset 32 here and not in the header's data byte, which the
     * protocol XML states by padding byte 1 and putting the flag after the coordinates.
     */
    private void queryPointer(ClientConnection client, XIQueryPointerRequest query) {
        XWindow window = checkWindow(query.window());
        cn.oyzh.fx.xserver.input.InputState input = server.inputFor(window.screen());
        XWindow child = childOf(window, input.pointerWindow());
        int screenX = input.pointerX();
        int screenY = input.pointerY();
        int eventX = screenX - window.absoluteX();
        int eventY = screenY - window.absoluteY();
        byte[] reply = new byte[44];
        reply[0] = 1;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(reply, 4, (reply.length - 32) / 4, client.isBigEndian());
        putCard32(reply, 8, window.root().id(), client.isBigEndian());
        putCard32(reply, 12, child == null ? 0 : child.id(), client.isBigEndian());
        // **Four bytes each, not eight.** These are FP1616 - a fraction in the low sixteen bits of
        // one word - where a valuator class's range is FP3232, two words. Written as FP3232 the four
        // of them overlap each other and the last one runs over the flags: the client then reads a
        // position that is not where the pointer is and keeps asking. Measured on EasyShell: GDK
        // asked XIQueryPointer **seven thousand times a second** until the server could do nothing
        // else, and the whole session looked like a frozen application.
        putFp1616(reply, 16, screenX);
        putFp1616(reply, 20, screenY);
        putFp1616(reply, 24, eventX);
        putFp1616(reply, 28, eventY);
        reply[32] = 1;                              // same screen
        putCard16(reply, 34, 0, client.isBigEndian());  // buttons_len: no button mask follows
        int mods = input.modifierState();
        reply[36] = (byte) (mods & ~0x02);          // base modifiers, without the lock
        reply[38] = (byte) (mods & 0x02);           // locked
        reply[39] = (byte) mods;                    // effective
        send(client, reply);
    }

    /**
     * The window the reply's {@code child} field must name: the **queried window's own child** that
     * contains the pointer, or null when the pointer is in the queried window itself or not in it at
     * all.
     *
     * <p>Not "the window the pointer is in", which is what this answered and is a different
     * question. A client walks the tree with this - ask about a window, ask again about the child
     * the answer names, and so on until the answer is None - and that walk never ends when the reply
     * to "what is under the pointer in W" is W itself: it asks about W again, and again.
     *
     * <p>Measured (A16) against Xvfb and this server, with the pointer inside EasyShell's toplevel:
     * **Xvfb answers child = the toplevel** for the query about the root, then **child = None** for
     * the query about the toplevel, and the client stops after those two. This server answered
     * `input.pointerWindow()` for both - the deepest window, which for the second query is the
     * toplevel itself - and the client asked about it ~3700 times a second for ever.
     */
    private static XWindow childOf(XWindow window, XWindow pointerWindow) {
        for (XWindow candidate = pointerWindow; candidate != null && candidate != window;
                candidate = candidate.parent()) {
            if (candidate.parent() == window) {
                return candidate;
            }
        }
        return null;
    }

    /** {@code XIWarpPointer}: the same as the core request, with the device named. */
    private void warpPointer(ClientConnection client, XIWarpPointerRequest warp) {
        XWindow destination = warp.destination() == 0 ? null : checkWindow(warp.destination());
        if (destination == null) {
            return;
        }
        checkDevice(warp.deviceId());
        // The core warp takes screen coordinates and the XI2 one takes the destination's, so the
        // destination's origin is added here rather than by a second entry point into the pointer.
        cn.oyzh.fx.xserver.input.InputState input = server.inputFor(destination.screen());
        input.warpPointer(destination.absoluteX() + warp.destinationX(),
                destination.absoluteY() + warp.destinationY());
    }

    /**
     * {@code XIGrabDevice}: the device grab, which is the reason this extension cannot be half done.
     *
     * <p>With one pointer and one keyboard an XI2 device grab <em>is</em> the core grab for that
     * device, so this is the core path with an XI2 face: the same state, the same delivery, the same
     * release rules. The reply's status is what a client waits for - GDK treats "not granted" as a
     * grab it did not get and keeps its own state accordingly.
     */
    private void grabDevice(XServer server, ClientConnection client, GrabDevice grab) {
        XWindow window = checkWindow(grab.window());
        checkDevice(grab.deviceId());
        boolean pointer = grab.deviceId() != XInputMasks.KEYBOARD_DEVICE;
        cn.oyzh.fx.xserver.input.InputState input = server.inputFor(window.screen());
        byte status = input.setDeviceGrab(client, window, !pointer, grab.ownerEvents(),
                grab.eventMask(), coreMode(grab.thisDeviceMode()),
                coreMode(grab.pairedDeviceMode()), grab.time());
        byte[] reply = new byte[32];
        reply[0] = 1;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        reply[8] = status;
        send(client, reply);
        log.debug("{} asked to grab the {} with XI2 on 0x{}: status {}", client,
                pointer ? "pointer" : "keyboard", Integer.toHexString(window.id()), status);
    }

    /**
     * XInput2's grab modes as the core ones.
     *
     * <p>The two rails number them differently and both start at one, so a mode passed through is
     * always wrong in one direction: {@code XIGrabModeAsync} is 1 where {@code GrabModeAsync} is 1
     * too - by luck - and {@code XIGrabModeSync} is 2 where {@code GrabModeSync} is 0. Left
     * unconverted, every synchronised grab would be taken as an asynchronous one, which is a menu
     * that never freezes the device it meant to.
     */
    private static int coreMode(int xi2Mode) {
        return xi2Mode == XI2_GRAB_MODE_SYNC ? 0 : 1;
    }

    private static final int XI2_GRAB_MODE_SYNC = 2;

    // ------------------------------------------------------------------
    // Delivery: the events themselves
    // ------------------------------------------------------------------

    /**
     * The XI2 event a core event stands for, if this client asked for it.
     *
     * <p>This is the whole of the delivery rule and it lives in one place on purpose: a client that
     * selected the XI2 event is sent the XI2 event <b>and not the core one</b>, which is what a
     * reference server does - measured, a client with {@code XI_ButtonPress} selected gets one event
     * for a click and the core {@code ButtonPress} never arrives, even though it asked for that too.
     * Sending both would make every click arrive twice for a toolkit that handles both rails.
     */
    public Xi2Event twinOf(ClientConnection client, com.github.moaxcp.x11.protocol.XEvent core) {
        Core coreEvent = coreEvent(core);
        if (coreEvent == null) {
            return null;
        }
        cn.oyzh.fx.xserver.resource.XResource resource =
                server.resources().get(coreEvent.window());
        if (!(resource instanceof XWindow window)) {
            return null;
        }
        int device = XInputMasks.deviceOf(coreEvent.eventNumber());
        if (!wants(client, window, device, coreEvent.eventNumber())) {
            return null;
        }
        return build(client, core, coreEvent.eventNumber(), device);
    }

    /**
     * The raw events of XI 2.1, delivered from the input path as the device's own events.
     *
     * <p><b>A raw event is not about a window: it goes to every client that selected it on the
     * screen's root</b>, whatever the pointer is over and whether anything was delivered at all -
     * which is the whole point of the family (a client watching the device rather than the
     * application). Measured against Xvfb 2026-10-04 with {@code tools/XiProbe raw}: an
     * XTEST-driven motion arrives as {@code XI_RawMotion} with the master as the device and the
     * XTEST slave as the source, a button and a key are the same shape with no axis in them, and a
     * {@code WarpPointer} produces <b>nothing</b> - a warp is the server moving the pointer, not the
     * device reporting it.
     *
     * <p>This server has one pointer and one keyboard and no slaves (Xvfb's four slave devices are a
     * separate gap, measured in the backlog), so the source id is the device itself: the same choice
     * its device events already make, and the honest one for a server with nothing else to name.
     */
    public void rawMotion(XWindow root, int source, int x, int y) {
        deliverRaw(root, XInputMasks.RAW_MOTION, XInputMasks.POINTER_DEVICE, source, 0, x, y);
    }

    /** One raw button event, from the press or the release. */
    public void rawButton(XWindow root, int source, int button, boolean pressed) {
        deliverRaw(root, pressed ? XInputMasks.RAW_BUTTON_PRESS : XInputMasks.RAW_BUTTON_RELEASE,
                XInputMasks.POINTER_DEVICE, source, button, 0, 0);
    }

    /** One raw key event, from the press or the release. */
    public void rawKey(XWindow root, int source, int keycode, boolean pressed) {
        deliverRaw(root, pressed ? XInputMasks.RAW_KEY_PRESS : XInputMasks.RAW_KEY_RELEASE,
                XInputMasks.KEYBOARD_DEVICE, source, keycode, 0, 0);
    }

    private void deliverRaw(XWindow root, int eventNumber, int device, int source, int detail,
                            int x, int y) {
        if (root == null) {
            return;
        }
        int time = (int) server.events().time();
        for (XWindow.Xi2Selection selection : root.xi2Selections()) {
            if (!XInputMasks.selectionCovers(selection.deviceId(), device)
                    || !XInputMasks.hasBit(selection.mask(), eventNumber)) {
                continue;
            }
            ClientConnection client = selection.client();
            byte[] bytes = switch (eventNumber) {
                case XInputMasks.RAW_MOTION -> Xi2RawEvent
                        .motion(client, majorOpcode(), device, source, time, x, y).wire();
                case XInputMasks.RAW_BUTTON_PRESS, XInputMasks.RAW_BUTTON_RELEASE -> Xi2RawEvent
                        .button(client, majorOpcode(), device, source, time, detail,
                                eventNumber == XInputMasks.RAW_BUTTON_PRESS).wire();
                default -> Xi2RawEvent
                        .key(client, majorOpcode(), device, source, time, detail,
                                eventNumber == XInputMasks.RAW_KEY_PRESS).wire();
            };
            send(client, bytes);
        }
    }

    /** Whether {@code client} selected the XI2 twin of core mask {@code coreMask} on a window. */
    public boolean wants(ClientConnection client, XWindow window, int coreMask) {
        int eventNumber = XInputMasks.eventForCoreMask(coreMask);
        if (eventNumber == 0) {
            return false;
        }
        int device = XInputMasks.deviceOf(eventNumber);
        return wants(client, window, device, eventNumber)
                || (coreMask == EventMasks.FOCUS_CHANGE
                        && wants(client, window, device, XInputMasks.FOCUS_OUT));
    }

    /** Whether any client selected the XI2 twin of {@code coreMask} on a window. */
    public boolean anyoneWants(XWindow window, int coreMask) {
        int eventNumber = XInputMasks.eventForCoreMask(coreMask);
        if (eventNumber == 0 || window == null) {
            return false;
        }
        int device = XInputMasks.deviceOf(eventNumber);
        for (XWindow.Xi2Selection selection : window.xi2Selections()) {
            if (XInputMasks.selectionCovers(selection.deviceId(), device)
                    && (XInputMasks.hasBit(selection.mask(), eventNumber)
                    || (coreMask == EventMasks.FOCUS_CHANGE
                    && XInputMasks.hasBit(selection.mask(), XInputMasks.FOCUS_OUT)))) {
                return true;
            }
        }
        return false;
    }

    private boolean wants(ClientConnection client, XWindow window, int device, int eventNumber) {
        for (XWindow.Xi2Selection selection : window.xi2Selections()) {
            if (selection.client() == client
                    && XInputMasks.selectionCovers(selection.deviceId(), device)
                    && XInputMasks.hasBit(selection.mask(), eventNumber)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The device an event names as its {@code sourceid}: the slave the input came from, or the
     * device itself.
     *
     * <p>Measured (A48): a reference's XTEST input is {@code device 2, sourceid 4} - the master as
     * the device and the slave that produced it as the source - and this server has the same two
     * XTEST slaves. Host input names the master, which is where this server's own device sits.
     */
    private int sourceOf(ClientConnection client, int device) {
        cn.oyzh.fx.xserver.input.InputState input = server.inputFor(client);
        int source = input == null ? 0 : input.sourceDevice();
        // No source marks the host's own input, which a reference puts on its backend's pair
        // ("Xvfb mouse"/"Xvfb keyboard", 6/7); XTEST marks its slaves (4/5).
        return source == 0 ? XInputMasks.hostDeviceFor(device) : source;
    }

    /** An XI2 event number and the window a core event is reported against. */
    private record Core(int eventNumber, int window) {
    }

    /** The XI2 event a core event stands for and the window it names, or null when it has no twin. */
    private static Core coreEvent(com.github.moaxcp.x11.protocol.XEvent core) {
        return switch (core) {
            case com.github.moaxcp.x11.protocol.xproto.KeyPressEvent e ->
                    new Core(XInputMasks.KEY_PRESS, e.getEvent());
            case com.github.moaxcp.x11.protocol.xproto.KeyReleaseEvent e ->
                    new Core(XInputMasks.KEY_RELEASE, e.getEvent());
            case com.github.moaxcp.x11.protocol.xproto.ButtonPressEvent e ->
                    new Core(XInputMasks.BUTTON_PRESS, e.getEvent());
            case com.github.moaxcp.x11.protocol.xproto.ButtonReleaseEvent e ->
                    new Core(XInputMasks.BUTTON_RELEASE, e.getEvent());
            case com.github.moaxcp.x11.protocol.xproto.MotionNotifyEvent e ->
                    new Core(XInputMasks.MOTION, e.getEvent());
            case com.github.moaxcp.x11.protocol.xproto.EnterNotifyEvent e ->
                    new Core(XInputMasks.ENTER, e.getEvent());
            case com.github.moaxcp.x11.protocol.xproto.LeaveNotifyEvent e ->
                    new Core(XInputMasks.LEAVE, e.getEvent());
            case com.github.moaxcp.x11.protocol.xproto.FocusInEvent e ->
                    new Core(XInputMasks.FOCUS_IN, e.getEvent());
            case com.github.moaxcp.x11.protocol.xproto.FocusOutEvent e ->
                    new Core(XInputMasks.FOCUS_OUT, e.getEvent());
            default -> null;
        };
    }

    /**
     * Builds the XI2 event for a core one, field for field.
     *
     * <p>A button press carries the button as its detail and the pointer's position twice - on the
     * screen and in the event window - and the keyboard's state in the two places XI2 keeps it. A
     * focus event has no state in the core protocol, so the modifiers are zero there, which is what
     * a client gets for free: it reads the modifiers it needs from the state it keeps itself.
     */
    private Xi2Event build(ClientConnection client, com.github.moaxcp.x11.protocol.XEvent core,
                           int eventNumber, int device) {
        // **The event's shape follows its number, not the server's convenience.** A key, a button and
        // a motion are device events; enter, leave and focus are not, and a reference sends them
        // sixteen and forty-four bytes shorter with their fields somewhere else entirely. Measured
        // 2026-09-29: 120 against 76 for a crossing and 104 for a focus, and every field from 48 on
        // in a different place - see Xi2Event.Shape, which has the offsets.
        Xi2Event.Shape shape = switch (eventNumber) {
            case XInputMasks.ENTER, XInputMasks.LEAVE -> Xi2Event.Shape.CROSSING;
            case XInputMasks.FOCUS_IN, XInputMasks.FOCUS_OUT -> Xi2Event.Shape.FOCUS;
            default -> Xi2Event.Shape.DEVICE;
        };
        Xi2Event.Builder event = Xi2Event.builder(client, majorOpcode(), shape)
                .evtype(eventNumber)
                .device(device)
                .source(sourceOf(client, device));
        int rootX = 0;
        int rootY = 0;
        int eventX = 0;
        int eventY = 0;
        int detail = 0;
        // Only a crossing has these two, and only a crossing's shape has room for them: a device
        // event's byte 18 is the high half of its own detail.
        byte crossingMode = 0;
        boolean crossingFocus = false;
        boolean sameScreen = true;
        int time;
        int root;
        int window;
        int child;
        int state = 0;
        switch (core) {
            case com.github.moaxcp.x11.protocol.xproto.KeyPressEvent e -> {
                time = e.getTime();
                root = e.getRoot();
                window = e.getEvent();
                child = e.getChild();
                detail = e.getDetail();
                rootX = e.getRootX();
                rootY = e.getRootY();
                eventX = e.getEventX();
                eventY = e.getEventY();
                state = e.getState();
            }
            case com.github.moaxcp.x11.protocol.xproto.KeyReleaseEvent e -> {
                time = e.getTime();
                root = e.getRoot();
                window = e.getEvent();
                child = e.getChild();
                detail = e.getDetail();
                rootX = e.getRootX();
                rootY = e.getRootY();
                eventX = e.getEventX();
                eventY = e.getEventY();
                state = e.getState();
            }
            case com.github.moaxcp.x11.protocol.xproto.ButtonPressEvent e -> {
                time = e.getTime();
                root = e.getRoot();
                window = e.getEvent();
                child = e.getChild();
                detail = e.getDetail();
                rootX = e.getRootX();
                rootY = e.getRootY();
                eventX = e.getEventX();
                eventY = e.getEventY();
                state = e.getState();
            }
            case com.github.moaxcp.x11.protocol.xproto.ButtonReleaseEvent e -> {
                time = e.getTime();
                root = e.getRoot();
                window = e.getEvent();
                child = e.getChild();
                detail = e.getDetail();
                rootX = e.getRootX();
                rootY = e.getRootY();
                eventX = e.getEventX();
                eventY = e.getEventY();
                state = e.getState();
            }
            case com.github.moaxcp.x11.protocol.xproto.MotionNotifyEvent e -> {
                time = e.getTime();
                root = e.getRoot();
                window = e.getEvent();
                child = e.getChild();
                detail = e.getDetail();
                rootX = e.getRootX();
                rootY = e.getRootY();
                eventX = e.getEventX();
                eventY = e.getEventY();
                state = e.getState();
            }
            case com.github.moaxcp.x11.protocol.xproto.EnterNotifyEvent e -> {
                time = e.getTime();
                root = e.getRoot();
                window = e.getEvent();
                child = e.getChild();
                detail = e.getDetail();
                rootX = e.getRootX();
                rootY = e.getRootY();
                eventX = e.getEventX();
                eventY = e.getEventY();
                state = e.getState();
                crossingMode = e.getMode();
                // The core crossing packs the two flags a client reads: same-screen in bit 1 and
                // "this window is the focus window" in bit 0.
                crossingFocus = (e.getSameScreenFocus() & 0x01) != 0;
                sameScreen = (e.getSameScreenFocus() & 0x02) != 0;
            }
            case com.github.moaxcp.x11.protocol.xproto.LeaveNotifyEvent e -> {
                time = e.getTime();
                root = e.getRoot();
                window = e.getEvent();
                child = e.getChild();
                detail = e.getDetail();
                rootX = e.getRootX();
                rootY = e.getRootY();
                eventX = e.getEventX();
                eventY = e.getEventY();
                state = e.getState();
                crossingMode = e.getMode();
                crossingFocus = (e.getSameScreenFocus() & 0x01) != 0;
                sameScreen = (e.getSameScreenFocus() & 0x02) != 0;
            }
            // A core FocusIn has no time and no root - the event is thirty two bytes of type,
            // detail, sequence and window - where the XI2 one has both, and the pointer's position
            // as well (a reference sends the pointer's coordinates in a focus event, measured). The
            // time is the server's own clock, which is the same one the crossing and key events
            // carry, and the root is the root of the window the event names.
            case com.github.moaxcp.x11.protocol.xproto.FocusInEvent e -> {
                time = server.events().time();
                root = rootOf(e.getEvent());
                window = e.getEvent();
                child = 0;
                detail = e.getDetail();
                int[] at = pointerAt(window);
                rootX = at[0];
                rootY = at[1];
                eventX = at[2];
                eventY = at[3];
            }
            case com.github.moaxcp.x11.protocol.xproto.FocusOutEvent e -> {
                time = server.events().time();
                root = rootOf(e.getEvent());
                window = e.getEvent();
                child = 0;
                detail = e.getDetail();
                int[] at = pointerAt(window);
                rootX = at[0];
                rootY = at[1];
                eventX = at[2];
                eventY = at[3];
            }
            default -> {
                return null;
            }
        }
        Xi2Event.Builder built = event
                .time(time)
                .detail(detail)
                .root(root)
                .event(window)
                .child(child)
                .rootPosition(rootX, rootY)
                .windowPosition(eventX, eventY)
                .modifiers(state)
                // The buttons the pointer is holding, which is what a client reads a drag from. Zero
                // for every event here until 2026-09-29, and the visible cost was a window that could
                // not be dragged by its title bar: the motions arrived with nothing held.
                .buttons(state);
        if (shape == Xi2Event.Shape.MOTION) {
            // A motion also carries where the pointer is as the two axis values, not only as the
            // FP1616 positions above - a reference sends both, and a client that reads
            // `valuators_len` and then finds no values reads the next message's bytes.
            built.axes(rootX, rootY);
        }
        if (shape == Xi2Event.Shape.CROSSING) {
            // A crossing carries its mode and detail as two bytes where a device event's detail is a
            // word, and the two flags a client reads off it: same-screen, and whether this window is
            // the focus window - which is what tells a toolkit the pointer arrived in the window it
            // already had the keyboard in.
            built.crossing(crossingMode).sameScreen(sameScreen).focus(crossingFocus);
        } else if (shape == Xi2Event.Shape.FOCUS) {
            // A reference sets same-screen and leaves `focus` clear on a focus event itself
            // (measured), where a crossing beside it carries both.
            built.sameScreen(true).focus(false);
        }
        return built.build();
    }

    /**
     * The pointer's position, twice, for a focus event: on the root and in the window's coordinates.
     *
     * <p>A core focus event carries no coordinates and the XI2 one does, so they are read off the
     * pointer here - which is what a reference sends (measured: its focus event had the pointer's
     * own position in both fields, the same numbers its crossing for the same gesture carried).
     * Answers zeros when the window is gone, which a focus pair can still name.
     */
    private int[] pointerAt(int windowId) {
        cn.oyzh.fx.xserver.resource.XResource resource = server.resources().get(windowId);
        if (!(resource instanceof XWindow window)) {
            return new int[]{0, 0, 0, 0};
        }
        cn.oyzh.fx.xserver.input.InputState input = server.inputFor(window.screen());
        int x = input.pointerX();
        int y = input.pointerY();
        return new int[]{x, y, x - window.absoluteX(), y - window.absoluteY()};
    }

    // ------------------------------------------------------------------
    // Checks and helpers
    // ------------------------------------------------------------------

    /** The root of the screen a window is on, or 0 when it is gone - a focus event outlives one. */
    private int rootOf(int windowId) {
        cn.oyzh.fx.xserver.resource.XResource resource = server.resources().get(windowId);
        return resource instanceof XWindow window ? window.root().id() : 0;
    }

    private XWindow checkWindow(int id) {
        cn.oyzh.fx.xserver.resource.XResource resource = server.resources().get(id);
        if (!(resource instanceof XWindow window)) {
            throw ProtocolException.badWindow(id);
        }
        return window;
    }

    private void checkDevice(int deviceId) {
        if (deviceId == XInputMasks.ALL_DEVICES || deviceId == XInputMasks.ALL_MASTER_DEVICES
                || devices.isDevice(deviceId)) {
            return;
        }
        throw new ProtocolException((byte) (firstError() + ERROR_DEVICE), deviceId,
                "bad device " + deviceId);
    }

    /**
     * The same, for the requests that are about <em>one</em> device's own properties.
     *
     * <p>{@code XIAllDevices} and {@code XIAllMasterDevices} are selectors, not devices, and a
     * property belongs to a device - so a reference refuses them here, which was measured with
     * {@code XiProbe}: {@code XIListProperties} and {@code XIGetProperty} both answer an error for
     * device 0. Being lenient about it was this server's own invention, and it read as a device that
     * has no properties rather than as the bad request it is.
     */
    private void checkRealDevice(int deviceId) {
        if (deviceId != XInputMasks.POINTER_DEVICE && deviceId != XInputMasks.KEYBOARD_DEVICE) {
            throw new ProtocolException((byte) (firstError() + ERROR_DEVICE), deviceId,
                    "bad device " + deviceId + " for a device property request");
        }
    }

    private static void send(ClientConnection client, byte[] bytes) {
        try {
            client.output().writeRaw(bytes);
        } catch (IOException e) {
            client.close();
        }
    }

    private static void putCard16(byte[] bytes, int offset, int value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 8);
            bytes[offset + 1] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
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

    /** An {@code FP3232}: a whole number in two words, which is how a valuator's range is carried. */
    private static void putFixed(byte[] bytes, int offset, int value) {
        putCard32(bytes, offset, value, false);
        putCard32(bytes, offset + 4, 0, false);
    }

    /**
     * An {@code FP1616}: a position, in <b>four</b> bytes - the whole part in the high sixteen bits
     * of one word and the fraction in the low sixteen.
     *
     * <p>The two fixed point types are not interchangeable and using the wrong one is not a rounding
     * error: it is four bytes of overrun into whatever follows.
     */
    private static void putFp1616(byte[] bytes, int offset, int value) {
        putCard32(bytes, offset, value << 16, false);
    }

    // ------------------------------------------------------------------
    // Requests
    // ------------------------------------------------------------------

    /** {@code XIQueryVersion}'s body: the two versions the client speaks. */
    private record VersionQuery(int major, int minor) implements XRequest {
        static VersionQuery read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            return new VersionQuery(in.readCard16(), in.readCard16());
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** {@code XIQueryDevice}'s body: one device id, or {@code XIAllDevices}. */
    private record DeviceQuery(int deviceId) implements XRequest {
        static DeviceQuery read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            return new DeviceQuery(in.readCard16());
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** A request with no body at all: the two XInput 1 requests that carry one are that simple. */
    private record Bodyless(int minor) implements XRequest {
        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /**
     * The XInput 1 requests that name a device and a few numbers with it.
     *
     * <p>Read by hand and kept in one record because their bodies differ only in which of the three
     * numbers is present: {@code OpenDevice} and {@code CloseDevice} name a device, the two mappings
     * add a keycode range or nothing at all.
     */
    private record DeviceRequest(int minor, int deviceId, int firstKeycode, int count)
            implements XRequest {
        static DeviceRequest read(int minor, X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            int deviceId = Byte.toUnsignedInt(in.readCard8());
            int first = 0;
            int count = 0;
            if (minor == GET_DEVICE_KEY_MAPPING) {
                first = Byte.toUnsignedInt(in.readCard8());
                count = Byte.toUnsignedInt(in.readCard8());
            }
            return new DeviceRequest(minor, deviceId, first, count);
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** {@code GetExtensionVersion}: XInput 1.x's handshake, whose body is a name it is not read for. */
    private record GetExtensionVersion(int nameLength) implements XRequest {
        static GetExtensionVersion read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            return new GetExtensionVersion(in.readCard16());
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** A request with no reply this server accepts and has nothing to do for. */
    private record Accepted(int minor) implements XRequest {
        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** {@code XIGetClientPointer}: which client, for whom the answer is "the default pointer". */
    private record ClientPointerQuery(int window) implements XRequest {
        static ClientPointerQuery read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            return new ClientPointerQuery(in.readInt32());
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** {@code XIChangeCursor}: a window, a cursor resource, and no reply. */
    private record ChangeCursor(int window, int cursor) implements XRequest {
        static ChangeCursor read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            int window = in.readInt32();
            int cursor = in.readInt32();
            return new ChangeCursor(window, cursor);
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** A request this server answers from its own state rather than from a reading of the body. */
    private record UnusedQuery(int minor) implements XRequest {
        static UnusedQuery read(int minor) {
            return new UnusedQuery(minor);
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** {@code XIGetSelectedEvents}: the window to read the selection back from. */
    private record SelectedEventsQuery(int window) implements XRequest {
        static SelectedEventsQuery read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            return new SelectedEventsQuery(in.readInt32());
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /**
     * {@code XISelectEvents}: a window and the per-device masks a client wants on it.
     *
     * <p>Each mask is {@code deviceid}, a length in four byte words, and the words - the shape the
     * protocol gives it, and the reason a mask that names devices cannot be read as one flat word.
     */
    /**
     * {@code XIChangeHierarchy}: a change list whose records are <b>variable length</b> - the one
     * request here that is not a fixed number of words.
     *
     * <p>Each change is its own two-word header ({@code type}, {@code length} in four byte words)
     * followed by its body, and an {@code XIAddMaster} carries a name after its four fixed fields.
     * The length is what keeps the reader in step: a change this server does not know is skipped by
     * its own length rather than guessed at, which is the difference between refusing one change and
     * desynchronising the connection.
     */
    private record HierarchyChange(List<Change> changes) implements XRequest {

        /** One change: what it is, the device it names, and the name an {@code XIAddMaster} carries. */
        record Change(int type, int deviceId, int returnPointer, int returnKeyboard, String name) {
        }

        static HierarchyChange read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            int count = in.readCard8();
            in.readPad(3);
            List<Change> changes = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int type = in.readCard16();
                int words = in.readCard16();
                int body = (words - 1) * 4;
                switch (type) {
                    case XI_ADD_MASTER -> {
                        int nameLength = in.readCard16();
                        in.readCard8();                     // send_core
                        in.readCard8();                     // enable
                        StringBuilder name = new StringBuilder();
                        for (int c = 0; c < nameLength; c++) {
                            name.append((char) in.readCard8());
                        }
                        int padded = ((nameLength + 3) & ~3) - nameLength;
                        in.readPad(padded);
                        changes.add(new Change(type, 0, 0, 0, name.toString()));
                    }
                    case XI_REMOVE_MASTER -> {
                        int deviceId = in.readCard16();
                        in.readCard8();                     // return_mode
                        in.readCard8();                     // pad
                        int returnPointer = in.readCard16();
                        int returnKeyboard = in.readCard16();
                        changes.add(new Change(type, deviceId, returnPointer, returnKeyboard, ""));
                    }
                    case XI_ATTACH_SLAVE -> {
                        int deviceId = in.readCard16();
                        int master = in.readCard16();
                        changes.add(new Change(type, deviceId, master, 0, ""));
                    }
                    case XI_DETACH_SLAVE -> {
                        int deviceId = in.readCard16();
                        in.readCard16();                    // pad
                        changes.add(new Change(type, deviceId, 0, 0, ""));
                    }
                    default -> {
                        changes.add(new Change(type, 0, 0, 0, ""));
                        in.readPad(body);
                    }
                }
            }
            return new HierarchyChange(changes);
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    private record BarrierRelease(int count, int deviceId, int barrier) implements XRequest {
        static BarrierRelease read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            int count = in.readInt32();
            if (count == 0) {
                // A request that names no barrier has nothing to release and nothing to refuse.
                return new BarrierRelease(0, 0, 0);
            }
            int device = in.readCard16();
            in.readPad(2);
            int barrier = in.readInt32();
            // The rest of the entries (a deviceid, a pad, a barrier and an event id each) are left in
            // the connection's own buffer: the first one is enough to answer with, and what the
            // reference does is refuse the request by the *first* barrier it cannot find.
            return new BarrierRelease(count, device, barrier);
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    private record SelectEvents(int window, List<Mask> masks) implements XRequest {
        record Mask(int deviceId, int[] mask) {
        }

        static SelectEvents read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            int window = in.readInt32();
            int count = in.readCard16();
            in.readPad(2);
            List<Mask> masks = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int deviceId = in.readCard16();
                int words = in.readCard16();
                int[] mask = new int[words];
                for (int word = 0; word < words; word++) {
                    mask[word] = in.readInt32();
                }
                masks.add(new Mask(deviceId, mask));
            }
            return new SelectEvents(window, masks);
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /**
     * {@code XIGetProperty}: which device, which property, and the slice wanted.
     *
     * <p>Six four byte units, and the fields are read as far as the length allows: a request one unit
     * longer than this is refused by a reference with {@code BadLength}, so the {@code delete} flag a
     * longer reading of the request would put after {@code len} is left unread here - and the
     * connection has the whole request buffered, so stopping at the fields that exist is what keeps
     * the stream in step.
     */
    private record GetPropertyRequest(int device, int property, int type, int offset, int length)
            implements XRequest {

        static GetPropertyRequest read(X11Input in) throws IOException {
            in.readCard8();                     // the minor opcode, already known
            in.readCard16();                    // length
            int device = in.readCard16();
            in.readCard16();                    // pad
            int property = in.readInt32();
            int type = in.readInt32();
            int offset = in.readInt32();
            int length = in.readInt32();
            return new GetPropertyRequest(device, property, type, offset, length);
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** {@code XIQueryPointer}: the window the question is about. */
    /**
     * {@code XIListProperties}: which device the client wants the properties of - two units, and the
     * reply is where the work is.
     */
    private record ListPropertiesRequest(int device) implements XRequest {

        static ListPropertiesRequest read(X11Input in) throws IOException {
            in.readCard8();                     // the minor opcode, already known
            in.readCard16();                    // length
            return new ListPropertiesRequest(in.readCard16());
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    private record XIQueryPointerRequest(int window) implements XRequest {
        static XIQueryPointerRequest read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            return new XIQueryPointerRequest(in.readInt32());
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** {@code XIWarpPointer}: where the pointer goes, in the destination's own coordinates. */
    private record XIWarpPointerRequest(int destination, int deviceId, int destinationX,
                                        int destinationY) implements XRequest {
        static XIWarpPointerRequest read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            in.readInt32();                         // source window, unused
            int destination = in.readInt32();
            in.readInt32();                         // source x
            in.readInt32();                         // source y
            in.readCard16();                        // source width
            in.readCard16();                        // source height
            int x = in.readInt16();
            int y = in.readInt16();
            int deviceId = in.readCard16();
            in.readPad(2);
            return new XIWarpPointerRequest(destination, deviceId, x, y);
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /** {@code XIGrabDevice}: the device, the window, and how the grab reports events. */
    private record GrabDevice(int window, int time, int cursor, int deviceId, int eventMask,
                              int thisDeviceMode, int pairedDeviceMode, boolean ownerEvents)
            implements XRequest {
        static GrabDevice read(X11Input in) throws IOException {
            in.readCard8();
            in.readCard16();
            int window = in.readInt32();
            int time = in.readInt32();
            int cursor = in.readInt32();
            int deviceId = in.readCard16();
            int words = in.readCard16();
            int mode = in.readCard8();
            int pairedMode = in.readCard8();
            boolean ownerEvents = in.readBool();
            in.readPad(2);
            int mask = 0;
            for (int word = 0; word < words; word++) {
                // The XI2 mask has one bit per event numbered from one; the core grab wants the core
                // mask for the same events, so what is folded in here is the core bit that event
                // stands for. A word past the first names an event this server does not have.
                int wordValue = in.readInt32();
                if (word == 0) {
                    mask = coreMaskOf(wordValue);
                }
            }
            return new GrabDevice(window, time, cursor, deviceId, mask, mode, pairedMode,
                    ownerEvents);
        }

        /**
         * The core event mask an XI2 mask word stands for.
         *
         * <p>The mapping is the one {@link XInputMasks} defines, run backwards: the two rails number
         * the same events and the grab has to honour the client's choice on whichever one it made
         * it.
         */
        private static int coreMaskOf(int xi2Word) {
            int core = 0;
            for (int bit : new int[] {XInputMasks.KEY_PRESS, XInputMasks.KEY_RELEASE,
                    XInputMasks.BUTTON_PRESS, XInputMasks.BUTTON_RELEASE, XInputMasks.MOTION,
                    XInputMasks.ENTER, XInputMasks.LEAVE, XInputMasks.FOCUS_IN,
                    XInputMasks.FOCUS_OUT}) {
                if ((xi2Word & XInputMasks.bit(bit)) != 0) {
                    core |= coreMaskOfEvent(bit);
                }
            }
            return core;
        }

        private static int coreMaskOfEvent(int eventNumber) {
            return switch (eventNumber) {
                case XInputMasks.KEY_PRESS -> EventMasks.KEY_PRESS;
                case XInputMasks.KEY_RELEASE -> EventMasks.KEY_RELEASE;
                case XInputMasks.BUTTON_PRESS -> EventMasks.BUTTON_PRESS;
                case XInputMasks.BUTTON_RELEASE -> EventMasks.BUTTON_RELEASE;
                case XInputMasks.MOTION -> EventMasks.POINTER_MOTION;
                case XInputMasks.ENTER -> EventMasks.ENTER_WINDOW;
                case XInputMasks.LEAVE -> EventMasks.LEAVE_WINDOW;
                case XInputMasks.FOCUS_IN, XInputMasks.FOCUS_OUT -> EventMasks.FOCUS_CHANGE;
                default -> 0;
            };
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xinput";
        }
    }

    /**
     * The protocol plugin this extension would be served by, which does not exist.
     *
     * <p>{@code x11-protocol-xinput} is in the fork but is not published to the local Maven
     * repository, so there is no generated plugin to hand the registry - and the registry needs one
     * to hand out the opcode, the event range and the error range. Everything this extension
     * understands is decoded by {@link #decodeRequest}; the methods here that would decode are the
     * ones that never run, because {@code decodeRequest} answers first and returns null only for a
     * request this server refuses.
     */
    private static final class XInputPlugin implements XProtocolPlugin {
        private byte majorOpcode;
        private byte firstEvent;
        private byte firstError;

        @Override
        public String getPluginName() {
            return "xinput";
        }

        @Override
        public java.util.Optional<String> getExtensionXName() {
            return java.util.Optional.of("XInputExtension");
        }

        @Override
        public java.util.Optional<String> getExtensionName() {
            return java.util.Optional.of("XInputExtension");
        }

        @Override
        public java.util.Optional<Boolean> getExtensionMultiword() {
            return java.util.Optional.of(false);
        }

        @Override
        public java.util.Optional<Byte> getMajorVersion() {
            return java.util.Optional.of((byte) MAJOR_VERSION);
        }

        @Override
        public java.util.Optional<Byte> getMinorVersion() {
            return java.util.Optional.of((byte) MINOR_VERSION);
        }

        @Override
        public byte getMajorOpcode() {
            return majorOpcode;
        }

        @Override
        public void setMajorOpcode(byte majorOpcode) {
            this.majorOpcode = majorOpcode;
        }

        @Override
        public byte getFirstEvent() {
            return firstEvent;
        }

        @Override
        public void setFirstEvent(byte firstEvent) {
            this.firstEvent = firstEvent;
        }

        @Override
        public byte getFirstError() {
            return firstError;
        }

        @Override
        public void setFirstError(byte firstError) {
            this.firstError = firstError;
        }

        @Override
        public boolean supportedRequest(XRequest request) {
            return false;
        }

        @Override
        public boolean supportedRequest(byte majorOpcode, byte minorOpcode) {
            return false;
        }

        @Override
        public boolean supportedEvent(byte number) {
            return false;
        }

        @Override
        public boolean supportedError(byte code) {
            return false;
        }

        @Override
        public <T extends XRequest> T readRequest(byte majorOpcode, byte minorOpcode, X11Input in)
                throws IOException {
            throw new IOException("XInput2 request " + (minorOpcode & 0xFF)
                    + " is not implemented by this server");
        }

        @Override
        public <T extends com.github.moaxcp.x11.protocol.XEvent> T readEvent(byte number,
                boolean sentEvent, X11Input in) throws IOException {
            throw new IOException("XInput2 event " + (number & 0xFF) + " is not implemented");
        }

        @Override
        public <T extends com.github.moaxcp.x11.protocol.XError> T readError(byte code, X11Input in)
                throws IOException {
            throw new IOException("XInput2 error " + (code & 0xFF) + " is not implemented");
        }

        @Override
        public <T extends XGenericEvent> T readGenericEvent(byte firstEventOffset, boolean sentEvent,
                byte extension, short sequenceNumber, int length, short eventType, X11Input in)
                throws IOException {
            throw new IOException("XInput2 generic event " + eventType + " is not implemented");
        }
    }
}
