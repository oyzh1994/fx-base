package cn.oyzh.fx.xserver.event;

/**
 * X11 core event masks and event codes.
 *
 * <p>The mask bits are the ones clients pass to {@code CreateWindow} and
 * {@code ChangeWindowAttributes}. {@link #PROPAGATE} is the subset of those bits that keeps
 * propagating to ancestors when the window itself has no interested client.
 */
public final class EventMasks {

    private EventMasks() {
    }

    public static final int NO_EVENT = 0;
    public static final int KEY_PRESS = 1;
    public static final int KEY_RELEASE = 1 << 1;
    public static final int BUTTON_PRESS = 1 << 2;
    public static final int BUTTON_RELEASE = 1 << 3;
    public static final int ENTER_WINDOW = 1 << 4;
    public static final int LEAVE_WINDOW = 1 << 5;
    public static final int POINTER_MOTION = 1 << 6;
    public static final int POINTER_MOTION_HINT = 1 << 7;
    public static final int BUTTON1_MOTION = 1 << 8;
    public static final int BUTTON2_MOTION = 1 << 9;
    public static final int BUTTON3_MOTION = 1 << 10;
    public static final int BUTTON4_MOTION = 1 << 11;
    public static final int BUTTON5_MOTION = 1 << 12;
    public static final int BUTTON_MOTION = 1 << 13;
    public static final int KEYMAP_STATE = 1 << 14;
    public static final int EXPOSURE = 1 << 15;
    public static final int VISIBILITY_CHANGE = 1 << 16;
    public static final int STRUCTURE_NOTIFY = 1 << 17;
    public static final int RESIZE_REDIRECT = 1 << 18;
    public static final int SUBSTRUCTURE_NOTIFY = 1 << 19;
    public static final int SUBSTRUCTURE_REDIRECT = 1 << 20;
    public static final int FOCUS_CHANGE = 1 << 21;
    public static final int PROPERTY_CHANGE = 1 << 22;
    public static final int COLORMAP_CHANGE = 1 << 23;
    public static final int OWNER_GRAB_BUTTON = 1 << 24;

    /** All valid mask bits; anything else is a BadValue. */
    public static final int ALL = OWNER_GRAB_BUTTON | (OWNER_GRAB_BUTTON - 1);

    /** Masks that keep propagating up the window tree. */
    public static final int PROPAGATE = KEY_PRESS | KEY_RELEASE | BUTTON_PRESS | BUTTON_RELEASE
            | POINTER_MOTION | BUTTON1_MOTION | BUTTON2_MOTION | BUTTON3_MOTION | BUTTON4_MOTION
            | BUTTON5_MOTION | BUTTON_MOTION;

    /** Masks that make motion events interesting to a grabbing client. */
    public static final int MOTION = POINTER_MOTION | POINTER_MOTION_HINT | BUTTON1_MOTION
            | BUTTON2_MOTION | BUTTON3_MOTION | BUTTON4_MOTION | BUTTON5_MOTION | BUTTON_MOTION;

    /** Masks a pointer grab honours when deciding where to deliver. */
    public static final int POINTER_GRAB = BUTTON_PRESS | BUTTON_RELEASE | ENTER_WINDOW
            | LEAVE_WINDOW | POINTER_MOTION | POINTER_MOTION_HINT | BUTTON1_MOTION
            | BUTTON2_MOTION | BUTTON3_MOTION | BUTTON4_MOTION | BUTTON5_MOTION | BUTTON_MOTION;

    /** Masks a keyboard grab honours. */
    public static final int KEYBOARD_GRAB = KEY_PRESS | KEY_RELEASE;

    // Event codes as they appear in the second byte of an event and in the setup reply.
    public static final byte KEY_PRESS_CODE = 2;
    public static final byte KEY_RELEASE_CODE = 3;
    public static final byte BUTTON_PRESS_CODE = 4;
    public static final byte BUTTON_RELEASE_CODE = 5;
    public static final byte MOTION_NOTIFY_CODE = 6;
    public static final byte ENTER_NOTIFY_CODE = 7;
    public static final byte LEAVE_NOTIFY_CODE = 8;
    public static final byte FOCUS_IN_CODE = 9;
    public static final byte FOCUS_OUT_CODE = 10;
    public static final byte KEYMAP_NOTIFY_CODE = 11;
    public static final byte EXPOSE_CODE = 12;
    public static final byte GRAPHICS_EXPOSE_CODE = 13;
    public static final byte NO_EXPOSE_CODE = 14;
    public static final byte VISIBILITY_NOTIFY_CODE = 15;
    public static final byte CREATE_NOTIFY_CODE = 16;
    public static final byte DESTROY_NOTIFY_CODE = 17;
    public static final byte UNMAP_NOTIFY_CODE = 18;
    public static final byte MAP_NOTIFY_CODE = 19;
    public static final byte MAP_REQUEST_CODE = 20;
    public static final byte REPARENT_NOTIFY_CODE = 21;
    public static final byte CONFIGURE_NOTIFY_CODE = 22;
    public static final byte CONFIGURE_REQUEST_CODE = 23;
    public static final byte GRAVITY_NOTIFY_CODE = 24;
    public static final byte RESIZE_REQUEST_CODE = 25;
    public static final byte CIRCULATE_NOTIFY_CODE = 26;
    public static final byte CIRCULATE_REQUEST_CODE = 27;
    public static final byte PROPERTY_NOTIFY_CODE = 28;
    public static final byte SELECTION_CLEAR_CODE = 29;
    public static final byte SELECTION_REQUEST_CODE = 30;
    public static final byte SELECTION_NOTIFY_CODE = 31;
    public static final byte COLORMAP_NOTIFY_CODE = 32;
    public static final byte CLIENT_MESSAGE_CODE = 33;
    public static final byte MAPPING_NOTIFY_CODE = 34;
    public static final byte GENERIC_EVENT_CODE = 35;

    /** First event code an extension may use. */
    public static final byte EXTENSION_EVENT_BASE = 64;
}
