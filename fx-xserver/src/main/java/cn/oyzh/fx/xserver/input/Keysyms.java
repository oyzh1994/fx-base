package cn.oyzh.fx.xserver.input;

/**
 * X11 keysyms.
 *
 * <p>Latin-1 printable characters use their Unicode code point as the keysym, which is a rule of
 * the protocol rather than a coincidence. Everything else lives in the 0xFE00-0xFFFF range with
 * the well known names below.
 */
public final class Keysyms {

    private Keysyms() {
    }

    public static final int NONE = 0x0000;
    public static final int VOID_SYMBOL = 0xFFFFFF;

    // Function keys and cursor control.
    public static final int BACK_SPACE = 0xFF08;
    public static final int TAB = 0xFF09;
    public static final int LINE_FEED = 0xFF0A;
    public static final int CLEAR = 0xFF0B;
    public static final int RETURN = 0xFF0D;
    public static final int PAUSE = 0xFF13;
    public static final int SCROLL_LOCK = 0xFF14;
    public static final int SYS_REQ = 0xFF15;
    public static final int ESCAPE = 0xFF1B;
    public static final int DELETE = 0xFFFF;

    public static final int HOME = 0xFF50;
    public static final int LEFT = 0xFF51;
    public static final int UP = 0xFF52;
    public static final int RIGHT = 0xFF53;
    public static final int DOWN = 0xFF54;
    public static final int PRIOR = 0xFF55;      // Page Up
    public static final int NEXT = 0xFF56;       // Page Down
    public static final int END = 0xFF57;
    public static final int BEGIN = 0xFF58;
    public static final int INSERT = 0xFF63;
    public static final int MENU = 0xFF67;
    public static final int PRINT = 0xFF61;

    // Modifiers.
    public static final int SHIFT_L = 0xFFE1;
    public static final int SHIFT_R = 0xFFE2;
    public static final int CONTROL_L = 0xFFE3;
    public static final int CONTROL_R = 0xFFE4;
    public static final int CAPS_LOCK = 0xFFE5;
    public static final int SHIFT_LOCK = 0xFFE6;
    public static final int META_L = 0xFFE7;
    public static final int META_R = 0xFFE8;
    public static final int ALT_L = 0xFFE9;
    public static final int ALT_R = 0xFFEA;
    public static final int SUPER_L = 0xFFEB;
    public static final int SUPER_R = 0xFFEC;
    public static final int HYPER_L = 0xFFED;
    public static final int HYPER_R = 0xFFEE;
    public static final int MODE_SWITCH = 0xFF7E;
    public static final int NUM_LOCK = 0xFF7F;

    // Keypad.
    public static final int KP_SPACE = 0xFF80;
    public static final int KP_TAB = 0xFF89;
    public static final int KP_ENTER = 0xFF8D;
    public static final int KP_F1 = 0xFF91;
    public static final int KP_F2 = 0xFF92;
    public static final int KP_F3 = 0xFF93;
    public static final int KP_F4 = 0xFF94;
    public static final int KP_HOME = 0xFF95;
    public static final int KP_LEFT = 0xFF96;
    public static final int KP_UP = 0xFF97;
    public static final int KP_RIGHT = 0xFF98;
    public static final int KP_DOWN = 0xFF99;
    public static final int KP_PRIOR = 0xFF9A;
    public static final int KP_NEXT = 0xFF9B;
    public static final int KP_END = 0xFF9C;
    public static final int KP_BEGIN = 0xFF9D;
    public static final int KP_INSERT = 0xFF9E;
    public static final int KP_DELETE = 0xFF9F;
    public static final int KP_EQUAL = 0xFFBD;
    public static final int KP_MULTIPLY = 0xFFAA;
    public static final int KP_ADD = 0xFFAB;
    public static final int KP_SEPARATOR = 0xFFAC;
    public static final int KP_SUBTRACT = 0xFFAD;
    public static final int KP_DECIMAL = 0xFFAE;
    public static final int KP_DIVIDE = 0xFFAF;

    public static final int KP_0 = 0xFFB0;
    public static final int KP_1 = 0xFFB1;
    public static final int KP_2 = 0xFFB2;
    public static final int KP_3 = 0xFFB3;
    public static final int KP_4 = 0xFFB4;
    public static final int KP_5 = 0xFFB5;
    public static final int KP_6 = 0xFFB6;
    public static final int KP_7 = 0xFFB7;
    public static final int KP_8 = 0xFFB8;
    public static final int KP_9 = 0xFFB9;

    public static final int F1 = 0xFFBE;
    public static final int F2 = 0xFFBF;
    public static final int F3 = 0xFFC0;
    public static final int F4 = 0xFFC1;
    public static final int F5 = 0xFFC2;
    public static final int F6 = 0xFFC3;
    public static final int F7 = 0xFFC4;
    public static final int F8 = 0xFFC5;
    public static final int F9 = 0xFFC6;
    public static final int F10 = 0xFFC7;
    public static final int F11 = 0xFFC8;
    public static final int F12 = 0xFFC9;

    /** Applies the Shift modifier to a keysym, for the cases where a layout has no second symbol. */
    public static int toUpper(int keysym) {
        if (keysym >= 0x61 && keysym <= 0x7A) {
            return keysym - 32;
        }
        return switch (keysym) {
            case '1' -> '!';
            case '2' -> '@';
            case '3' -> '#';
            case '4' -> '$';
            case '5' -> '%';
            case '6' -> '^';
            case '7' -> '&';
            case '8' -> '*';
            case '9' -> '(';
            case '0' -> ')';
            case '-' -> '_';
            case '=' -> '+';
            case '[' -> '{';
            case ']' -> '}';
            case ';' -> ':';
            case '\'' -> '"';
            case '`' -> '~';
            case '\\' -> '|';
            case ',' -> '<';
            case '.' -> '>';
            case '/' -> '?';
            default -> keysym;
        };
    }

    /** Human readable name, used only for diagnostics. */
    public static String name(int keysym) {
        if (keysym >= 0x20 && keysym < 0x7F) {
            return String.valueOf((char) keysym);
        }
        return String.format("0x%04X", keysym);
    }
}
