package cn.oyzh.fx.xserver.client;

/**
 * Thrown by a request handler to make the dispatcher send an X protocol error back to the client.
 * This mirrors what Xorg does with {@code BadWindow} &amp; friends: the request is abandoned but the
 * connection stays alive.
 */
public class ProtocolException extends RuntimeException {

    // Core protocol error codes, see "Errors" in the X11 protocol specification.
    public static final byte REQUEST = 1;
    public static final byte VALUE = 2;
    public static final byte WINDOW = 3;
    public static final byte PIXMAP = 4;
    public static final byte ATOM = 5;
    public static final byte CURSOR = 6;
    public static final byte FONT = 7;
    public static final byte MATCH = 8;
    public static final byte DRAWABLE = 9;
    public static final byte ACCESS = 10;
    public static final byte ALLOC = 11;
    public static final byte COLORMAP = 12;
    public static final byte GC = 13;
    public static final byte ID_CHOICE = 14;
    public static final byte NAME = 15;
    public static final byte LENGTH = 16;
    public static final byte IMPLEMENTATION = 17;

    private final byte errorCode;
    private final int badValue;

    public ProtocolException(byte errorCode, int badValue, String message) {
        super(message);
        this.errorCode = errorCode;
        this.badValue = badValue;
    }

    public static ProtocolException badWindow(int id) {
        return new ProtocolException(WINDOW, id, "bad window 0x" + Integer.toHexString(id));
    }

    public static ProtocolException badPixmap(int id) {
        return new ProtocolException(PIXMAP, id, "bad pixmap 0x" + Integer.toHexString(id));
    }

    public static ProtocolException badDrawable(int id) {
        return new ProtocolException(DRAWABLE, id, "bad drawable 0x" + Integer.toHexString(id));
    }

    public static ProtocolException badGc(int id) {
        return new ProtocolException(GC, id, "bad graphics context 0x" + Integer.toHexString(id));
    }

    public static ProtocolException badAtom(int id) {
        return new ProtocolException(ATOM, id, "bad atom 0x" + Integer.toHexString(id));
    }

    public static ProtocolException badColormap(int id) {
        return new ProtocolException(COLORMAP, id, "bad colormap 0x" + Integer.toHexString(id));
    }

    public static ProtocolException badCursor(int id) {
        return new ProtocolException(CURSOR, id, "bad cursor 0x" + Integer.toHexString(id));
    }

    public static ProtocolException badFont(int id) {
        return new ProtocolException(FONT, id, "bad font 0x" + Integer.toHexString(id));
    }

    public static ProtocolException badValue(int value) {
        return new ProtocolException(VALUE, value, "bad value " + value);
    }

    public static ProtocolException badMatch() {
        return new ProtocolException(MATCH, 0, "bad match");
    }

    public static ProtocolException badIdChoice(int id) {
        return new ProtocolException(ID_CHOICE, id, "bad id choice 0x" + Integer.toHexString(id));
    }

    public static ProtocolException badLength() {
        return new ProtocolException(LENGTH, 0, "bad request length");
    }

    public static ProtocolException badAlloc() {
        return new ProtocolException(ALLOC, 0, "insufficient resources");
    }

    public static ProtocolException badName() {
        return new ProtocolException(NAME, 0, "font or color name does not exist");
    }

    /**
     * The same error with a message of the caller's own, for a request where the core protocol's
     * wording would mislead: {@code BadName} is also what an output property that is not there
     * raises, and "font or color name does not exist" in the log for that sends the reader looking
     * for a font.
     */
    public static ProtocolException badName(String message) {
        return new ProtocolException(NAME, 0, message);
    }

    public static ProtocolException unimplemented(String what) {
        return new ProtocolException(IMPLEMENTATION, 0, "not implemented: " + what);
    }

    public byte errorCode() {
        return errorCode;
    }

    public int badValue() {
        return badValue;
    }
}
