package cn.oyzh.fx.xserver.event;

/**
 * XInput2's devices, event numbers and event mask bits (backlog B4).
 *
 * <p>Beside {@link EventMasks} rather than in the extension because three layers need the same
 * numbers and none of them owns the other two: the extension answers the requests, the input path
 * decides what a selection means, and the dispatcher builds the events. This is the one place they
 * can agree.
 *
 * <p><b>Every number here was measured against a reference server, not read off a header</b>
 * ({@code tools/XiProbe}, 2026-09-26). Two of them are not what they look like:
 *
 * <ul>
 *   <li><b>The mask bit of an event is {@code 1 << eventNumber}</b> - so {@code XI_ButtonPress} is
 *       {@code 1<<4}, not the {@code 1<<2} the core protocol gives {@code ButtonPressMask}.
 *       Measured one bit at a time: {@code 1<<3} selects nothing and lets the core events through,
 *       {@code 1<<4} delivers an XI2 {@code ButtonPress}, {@code 1<<6} a {@code Motion}. A mask one
 *       bit off is <em>accepted</em> and changes nothing, which is exactly what a server that cannot
 *       deliver a device event looks like.
 *   <li><b>An XI2 event is a core {@code GenericEvent}</b> (type 35) whose first twelve bytes are a
 *       header - type, the extension's major opcode, the sequence number, the length, the evtype and
 *       the device id - with the event's own body at the timestamp. There is <em>no</em> 32 bit
 *       {@code full_sequence} on the wire, which other GenericEvents do have: read the body as
 *       though it had one and the timestamp comes out as 1 and the detail as the root window's id.
 *   <li>The length field is the event's size in four byte units <b>minus eight</b>, which is how a
 *       sixty-eight byte event carries a length of {@code 9}.
 * </ul>
 */
public final class XInputMasks {

    private XInputMasks() {
    }

    /** {@code XIAllDevices} and {@code XIAllMasterDevices}: what a selection can name instead of one. */
    public static final int ALL_DEVICES = 0;
    public static final int ALL_MASTER_DEVICES = 1;

    /**
     * The two devices this server has, as ids.
     *
     * <p>A reference server numbers them 2 and 3 - the core pointer and keyboard hold 0 and 1 -
     * and the numbers were measured rather than chosen: {@code XIQueryDevice} on Xvfb answers
     * {@code Virtual core pointer} as device 2 and {@code Virtual core keyboard} as device 3, and a
     * client that has cached those ids for the life of its connection is not told otherwise.
     */
    public static final int POINTER_DEVICE = 2;
    public static final int KEYBOARD_DEVICE = 3;

    /**
     * The slaves a reference has and this server did not until A48: the two <b>XTEST</b> devices,
     * which are what XTEST-injected input comes from.
     *
     * <p>Measured on Xvfb 2026-10-04 ({@code XiProbe device 0}): {@code Virtual core XTEST pointer}
     * is device **4** attached to 2, {@code Virtual core XTEST keyboard} is device **5** attached to
     * 3, and an XTEST-driven raw motion carries {@code device 2, sourceid 4} - the master as the
     * device and the slave that produced it as the source. A reference also lists two more slaves
     * (its backend's "Xvfb mouse"/"Xvfb keyboard", 6 and 7), which is what its *own* input comes
     * from; this server's host input is the master's own and is documented as the remaining
     * difference.
     */
    public static final int XTEST_POINTER_DEVICE = 4;
    public static final int XTEST_KEYBOARD_DEVICE = 5;

    /**
     * The two devices this server's <em>own</em> input arrives on, which is the place a reference
     * puts its backend's pair: {@code Xvfb mouse} is device 6 and {@code Xvfb keyboard} 7 there,
     * attached to the core pointer and keyboard exactly as these are.
     *
     * <p>Measured 2026-10-04 ({@code XiProbe device 0}): a reference's own input comes from these two
     * slaves while XTEST comes from the pair above, so an event's {@code sourceid} says which of them
     * produced it - which is the whole reason the field exists. Naming them after the server (as
     * {@code Xvfb} does) is the only choice here that is not a measurement: they are this server's
     * host input, and the name is one line to change.
     */
    public static final int HOST_POINTER_DEVICE = 6;
    public static final int HOST_KEYBOARD_DEVICE = 7;
    public static final String XTEST_POINTER_NAME = "Virtual core XTEST pointer";
    public static final String XTEST_KEYBOARD_NAME = "Virtual core XTEST keyboard";
    /** And what the host's pair is called, mirroring the reference's "Xvfb mouse"/"Xvfb keyboard". */
    public static final String HOST_POINTER_NAME = "xserver mouse";
    public static final String HOST_KEYBOARD_NAME = "xserver keyboard";

    /** Whether a device is one this server has, for the requests that name one. */
    public static boolean isDevice(int deviceId) {
        return deviceId == POINTER_DEVICE || deviceId == KEYBOARD_DEVICE
                || deviceId == XTEST_POINTER_DEVICE || deviceId == XTEST_KEYBOARD_DEVICE
                || deviceId == HOST_POINTER_DEVICE || deviceId == HOST_KEYBOARD_DEVICE;
    }

    /** The device the host's own input arrives on, which is what a master's events name. */
    public static int hostDeviceFor(int deviceId) {
        return deviceId == KEYBOARD_DEVICE ? HOST_KEYBOARD_DEVICE : HOST_POINTER_DEVICE;
    }

    /** {@code DeviceType}: what a device is, in {@code XIQueryDevice}'s answer. */
    public static final int MASTER_POINTER = 1;
    public static final int MASTER_KEYBOARD = 2;
    /** {@code XISlavePointer}/{@code XISlaveKeyboard}: what a device attached to a master is. */
    public static final int SLAVE_POINTER = 3;
    public static final int SLAVE_KEYBOARD = 4;

    /** What a reference server calls its two masters, measured with {@code XIQueryDevice 0}. */
    public static final String POINTER_NAME = "Virtual core pointer";
    public static final String KEYBOARD_NAME = "Virtual core keyboard";
    /**
     * The slave a master's events come from when the master is not the source itself.
     *
     * <p>XTEST input arrives on the XTEST pair (4/5) and everything else on the master, which is
     * this server's own device - see {@link #XTEST_POINTER_DEVICE}.
     */
    public static int slaveOf(int deviceId) {
        return deviceId == POINTER_DEVICE ? XTEST_POINTER_DEVICE : XTEST_KEYBOARD_DEVICE;
    }

    /** The master a slave belongs to, which is what a barrier and a pointer event are about. */
    public static int masterOf(int deviceId) {
        return deviceId == XTEST_POINTER_DEVICE ? POINTER_DEVICE : KEYBOARD_DEVICE;
    }

    // XI2 event numbers, as {@code evtype} in the generic event's header.
    public static final int DEVICE_CHANGED = 1;
    public static final int KEY_PRESS = 2;
    public static final int KEY_RELEASE = 3;
    public static final int BUTTON_PRESS = 4;
    public static final int BUTTON_RELEASE = 5;
    public static final int MOTION = 6;
    public static final int ENTER = 7;
    public static final int LEAVE = 8;
    public static final int FOCUS_IN = 9;
    public static final int FOCUS_OUT = 10;
    /**
     * The raw family (XI 2.1), which is the device's own event <em>before</em> the server has done
     * anything with it - no pointer grab, no window under it, no acceleration.
     *
     * <p>Selected on a <b>root</b> window and delivered there whatever the pointer is over: a raw
     * event names no window at all, which is why its shape is a third one (see
     * {@link Xi2RawEvent}). Measured against Xvfb 2026-10-04: an XTEST-driven motion arrives as
     * {@code XI_RawMotion}, a warp produces nothing, and a key event is a raw event like any other.
     */
    public static final int RAW_KEY_PRESS = 13;
    public static final int RAW_KEY_RELEASE = 14;
    public static final int RAW_BUTTON_PRESS = 15;
    public static final int RAW_BUTTON_RELEASE = 16;
    public static final int RAW_MOTION = 17;
    /**
     * The device-state events, which are about <em>the server's device set</em> rather than about a
     * window's input: a reference refuses them for any device but {@code XIAllDevices}.
     *
     * <p>Measured 2026-10-04 with a selection per case: {@code XI_HierarchyChanged} (bit 11) on
     * {@code XIAllDevices} is accepted and on a master is <b>BadValue with the bit number as its bad
     * value</b> (error 2, value 11), and the same two neighbours (bit 1 changed, bit 12 property) are
     * what GDK selects on {@code XIAllDevices} - which is why that device is where they live.
     */
    public static final int HIERARCHY_CHANGED = 11;
    public static final int PROPERTY_EVENT = 12;

    /** The mask bit a client selects for an event: measured to be {@code 1 << eventNumber}. */
    public static int bit(int eventNumber) {
        return 1 << eventNumber;
    }

    /**
     * The device an XI2 event comes from.
     *
     * <p>The pointer's events and the keyboard's are what a client subscribes to separately - a
     * selection that names {@code XIAllMasterDevices} covers both, which is what a toolkit that
     * wants "the input" asks for.
     */
    public static int deviceOf(int eventNumber) {
        return switch (eventNumber) {
            case KEY_PRESS, KEY_RELEASE, FOCUS_IN, FOCUS_OUT, DEVICE_CHANGED,
                    RAW_KEY_PRESS, RAW_KEY_RELEASE -> KEYBOARD_DEVICE;
            default -> POINTER_DEVICE;
        };
    }

    /** Whether an event number is one of the raw family, which is delivered on the root alone. */
    public static boolean isRaw(int eventNumber) {
        return eventNumber >= RAW_KEY_PRESS && eventNumber <= RAW_MOTION;
    }

    /**
     * The XI2 event a core event mask stands for, or 0 when the core mask has no XI2 twin.
     *
     * <p>The correspondence is one to one except for motion, where the core protocol has eight masks
     * (one plain, one hint and one per button) and XI2 has the single {@code XI_Motion}, and except
     * for the focus, where one core mask covers both the arriving and the leaving event - so
     * {@code FOCUS_CHANGE} maps to {@code XI_FocusIn} here and the caller asks for the leaving one
     * by name.
     */
    public static int eventForCoreMask(int coreMask) {
        if (coreMask == EventMasks.KEY_PRESS) {
            return KEY_PRESS;
        }
        if (coreMask == EventMasks.KEY_RELEASE) {
            return KEY_RELEASE;
        }
        if (coreMask == EventMasks.BUTTON_PRESS) {
            return BUTTON_PRESS;
        }
        if (coreMask == EventMasks.BUTTON_RELEASE) {
            return BUTTON_RELEASE;
        }
        if (coreMask == EventMasks.ENTER_WINDOW) {
            return ENTER;
        }
        if (coreMask == EventMasks.LEAVE_WINDOW) {
            return LEAVE;
        }
        if (coreMask == EventMasks.FOCUS_CHANGE) {
            return FOCUS_IN;
        }
        if ((coreMask & EventMasks.MOTION) != 0) {
            return MOTION;
        }
        return 0;
    }

    /** Whether a client's selection of {@code device} covers an event from {@code eventDevice}. */
    public static boolean selectionCovers(int selectedDevice, int eventDevice) {
        return selectedDevice == ALL_DEVICES || selectedDevice == ALL_MASTER_DEVICES
                || selectedDevice == eventDevice;
    }

    /** Whether a selection's mask words have {@code bit} set. */
    public static boolean hasBit(int[] mask, int bit) {
        int word = bit >>> 5;
        return word < mask.length && (mask[word] & (1 << (bit & 31))) != 0;
    }

    /**
     * The core mask an XI2 event stands for, which is the inverse of {@link #eventForCoreMask}.
     *
     * <p>A passive grab names its mask in XI2's numbering (one bit per event number), while the
     * delivery path here - and the grab table a core {@code GrabKey} uses - works in the core masks.
     * A grab is therefore translated when it is armed; the events the owner then receives are the
     * ones it named, in the rail its own selection on that window chooses.
     */
    public static int coreMaskFor(int eventNumber) {
        return switch (eventNumber) {
            case KEY_PRESS -> EventMasks.KEY_PRESS;
            case KEY_RELEASE -> EventMasks.KEY_RELEASE;
            case BUTTON_PRESS -> EventMasks.BUTTON_PRESS;
            case BUTTON_RELEASE -> EventMasks.BUTTON_RELEASE;
            case MOTION -> EventMasks.MOTION;
            case ENTER -> EventMasks.ENTER_WINDOW;
            case LEAVE -> EventMasks.LEAVE_WINDOW;
            case FOCUS_IN, FOCUS_OUT -> EventMasks.FOCUS_CHANGE;
            default -> 0;
        };
    }
}
