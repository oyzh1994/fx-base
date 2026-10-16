package cn.oyzh.fx.xserver.test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A minimal X client for tests: a socket, the connection setup, and the handful of requests a test
 * needs to build a window a client would recognise.
 *
 * <p>Written against the wire protocol rather than Xlib so a test needs nothing installed, and so a
 * test can send exactly the requests it means to - a toolkit would add its own traffic around them.
 */
public final class RawXClient implements AutoCloseable {

    /** The root window of the screen, for requests that name a parent. */
    private final int root;
    /** The first id this client may use; ids above it are ours to hand out. */
    private final int idBase;
    private int nextId = 1;
    /** Requests sent so far, which is the sequence number the next reply has to carry. */
    private int sequence;

    private final Socket socket;
    private final DataInputStream in;
    private final OutputStream out;

    private RawXClient(Socket socket, int root, int idBase) throws IOException {
        this.socket = socket;
        this.root = root;
        this.idBase = idBase;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = socket.getOutputStream();
    }

    /** Connects to a display's TCP port and completes the connection setup. */
    public static RawXClient connect(int display) throws IOException {
        return connect("127.0.0.1", 6000 + display);
    }

    /**
     * Connects to a server by host and port, for comparing this one against another - a reference
     * server on a different machine answers the same questions, and the answers are the point.
     */
    public static RawXClient connect(String host, int port) throws IOException {
        return connect(host, port, null, null);
    }

    /**
     * The same, offering credentials: the setup request carries an authorization protocol's name and
     * its data, and a server with access control on either accepts them or refuses the connection.
     *
     * <p>SECURITY is what needs this: a client it is handed a token for offers that token under the
     * protocol name {@code XC-QUERY-SECURITY-1} - the extension's own {@code
     * XSecurityAuthorizationName}, and *not* "SECURITY", which a reference refuses outright - and the
     * connection it gets is an *untrusted* one: a different kind of connection, not a different
     * request (F7). A null name offers nothing.
     */
    public static RawXClient connect(String host, int port, String authName, byte[] authData)
            throws IOException {
        // **Proxy.NO_PROXY**, and it is not optional: macOS's java launcher reads the system proxy
        // settings and passes them to the JVM, so on a machine with a SOCKS proxy running, a plain
        // `new Socket()` connects to the proxy instead of to the display - which is a probe failure
        // that reads as "Malformed reply from SOCKS server" and only happens while that proxy is up.
        Socket socket = new Socket(java.net.Proxy.NO_PROXY);
        socket.connect(new InetSocketAddress(host, port), 5000);
        socket.setSoTimeout(5000);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        OutputStream out = socket.getOutputStream();

        byte[] name = authName == null ? new byte[0]
                : authName.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        byte[] data = authData == null ? new byte[0] : authData;
        int namePadded = (name.length + 3) & ~3;
        int dataPadded = (data.length + 3) & ~3;
        byte[] setup = new byte[12 + namePadded + dataPadded];
        setup[0] = 'l';                             // little endian client
        setup[2] = 11;                              // protocol major
        writeShort(setup, 6, name.length);
        writeShort(setup, 8, data.length);
        System.arraycopy(name, 0, setup, 12, name.length);
        System.arraycopy(data, 0, setup, 12 + namePadded, data.length);
        out.write(setup);
        out.flush();

        byte[] header = new byte[8];
        in.readFully(header);
        if ((header[0] & 0xFF) != 1) {
            // A refusal is the status byte, the reason's length in bytes and in words, and then the
            // reason itself - not the 32 byte record a reply has, so it is read as its own shape and
            // reading it as a reply would take the reason's first bytes for a sequence number.
            int reasonLength = header[1] & 0xFF;
            int words = (header[6] & 0xFF) | ((header[7] & 0xFF) << 8);
            byte[] body = new byte[Math.max(0, words * 4)];
            in.readFully(body);
            throw new IOException("the connection setup was refused: "
                    + new String(body, 0, Math.min(reasonLength, body.length),
                            java.nio.charset.StandardCharsets.ISO_8859_1));
        }
        int length = (header[6] & 0xFF) | ((header[7] & 0xFF) << 8);
        byte[] rest = new byte[length * 4];
        in.readFully(rest);

        // The setup reply: id base right after the release, then the screens after the vendor
        // string and the pixmap formats.
        int idBase = readInt(rest, 12 - 8);
        int vendor = readShort(rest, 24 - 8);
        int formats = rest[29 - 8] & 0xFF;
        int screensAt = 40 - 8 + ((vendor + 3) & ~3) + formats * 8;
        return new RawXClient(socket, readInt(rest, screensAt), idBase);
    }

    public int root() {
        return root;
    }

    /**
     * The first resource id this connection may use, which is how a server names it in everything it
     * says about a client - and what a probe compares against when it wants to say *which* client a
     * message belongs to without printing a number that differs between two servers.
     */
    public int resourceIdBase() {
        return idBase;
    }

    /**
     * Shrinks this client's receive buffer, so a test that deliberately stops reading fills the
     * server's output queue for it quickly instead of needing a megabyte of traffic first.
     */
    public void smallReceiveBuffer(int bytes) throws IOException {
        socket.setReceiveBufferSize(bytes);
    }

    /**
     * How long a read waits before reporting silence, for a probe that reads a stream rather than an
     * answer. {@link #readEvent} turns the timeout into {@code null}; the raw reads throw.
     */
    public void socketTimeout(int millis) throws IOException {
        socket.setSoTimeout(millis);
    }

    /**
     * Reads a fixed number of bytes straight off the wire, with no interpretation.
     *
     * <p>For a message whose layout this class cannot assume: RECORD's {@code EnableContext} answers
     * with a stream of replies whose data is a recorded request, reply or event, and what is in them
     * is what the probe is measuring.
     */
    public byte[] readBytes(int count) throws IOException {
        return readFully(count);
    }

    /** An id from this client's range, for a window it creates. */
    public int allocateId() {
        return idBase + nextId++;
    }

    public void createWindow(int window, int parent, int x, int y, int width, int height)
            throws IOException {
        createWindow(window, parent, x, y, width, height, -1);
    }

    /**
     * Creates a window with a depth and a visual of its own, which is how a client asks for 32 bit
     * pixels: {@code CreateWindow}'s depth byte and visual field, rather than the parent's.
     *
     * <p>Needed by anything that wants a window whose visual has an alpha channel (B5) - the visual
     * id comes from the connection setup reply, and a test that wants one has to know it.
     */
    public void createWindowWithVisual(int window, int parent, int x, int y, int width, int height,
                                       int depth, int visual, int backgroundPixel) throws IOException {
        byte[] request = new byte[36];
        request[0] = 1;                             // CreateWindow
        request[1] = (byte) depth;
        writeShort(request, 2, 9);
        writeInt(request, 4, window);
        writeInt(request, 8, parent);
        writeShort(request, 12, x);
        writeShort(request, 14, y);
        writeShort(request, 16, width);
        writeShort(request, 18, height);
        writeShort(request, 20, 0);                 // border width
        writeShort(request, 22, 1);                 // class: InputOutput
        writeInt(request, 24, visual);
        // The value list is in mask-bit order, so CWBackPixel (bit 1) comes before CWEventMask
        // (bit 11) - and a mask bit that is not a real attribute is refused, which reads as "the
        // window was never created" a request later.
        writeInt(request, 28, backgroundPixel >= 0 ? 0x2 | 0x800 : 0x800);
        if (backgroundPixel >= 0) {
            request = java.util.Arrays.copyOf(request, 40);
            writeShort(request, 2, 10);
            writeInt(request, 32, backgroundPixel);
            writeInt(request, 36, 0);
        }
        send(request);
    }

    /** Creates a window, optionally with a background pixel - which a ClearArea then paints it in. */
    public void createWindow(int window, int parent, int x, int y, int width, int height,
                             int backgroundPixel) throws IOException {
        createWindow(window, parent, x, y, width, height, backgroundPixel, 0);
    }

    /**
     * The same with a border width, which is a window's own thing and not the parent's: {@code xlogo}
     * asks for one, and the fill difference the probe was built for only shows up in its window.
     */
    public void createWindow(int window, int parent, int x, int y, int width, int height,
                             int backgroundPixel, int borderWidth) throws IOException {
        boolean withBackground = backgroundPixel >= 0;
        byte[] request = new byte[withBackground ? 36 : 32];
        request[0] = 1;                             // CreateWindow
        request[1] = 0;                             // depth: copy from the parent
        writeShort(request, 2, request.length / 4);
        writeInt(request, 4, window);
        writeInt(request, 8, parent);
        writeShort(request, 12, x);
        writeShort(request, 14, y);
        writeShort(request, 16, width);
        writeShort(request, 18, height);
        writeShort(request, 20, borderWidth);       // border width
        writeShort(request, 22, 1);                 // InputOutput
        writeInt(request, 24, 0);                   // visual: copy from the parent
        writeInt(request, 28, withBackground ? 0x2 : 0);        // CWBackPixel
        if (withBackground) {
            writeInt(request, 32, backgroundPixel);
        }
        send(request);
    }

    /**
     * Creates a window with the two gravities set, which {@code CreateWindow} takes as two of its
     * value-list attributes rather than as arguments of their own.
     *
     * <p>They are X's own numbers: {@code bitGravity} says what happens to the window's own contents
     * when it is resized, {@code winGravity} where the window goes when its <em>parent</em> is.
     */
    public void createWindowWithGravity(int window, int parent, int x, int y, int width, int height,
                                        int bitGravity, int winGravity) throws IOException {
        byte[] request = new byte[44];
        request[0] = 1;                             // CreateWindow
        writeShort(request, 2, 11);                 // 44 bytes
        writeInt(request, 4, window);
        writeInt(request, 8, parent);
        writeShort(request, 12, x);
        writeShort(request, 14, y);
        writeShort(request, 16, width);
        writeShort(request, 18, height);
        writeShort(request, 22, 1);                 // InputOutput
        writeInt(request, 28, 0x2 | 0x10 | 0x20);   // CWBackPixel | CWBitGravity | CWWinGravity
        writeInt(request, 32, 0xFFFFFF);            // a white background, in value-mask order
        writeInt(request, 36, bitGravity);
        writeInt(request, 40, winGravity);
        send(request);
    }

    /** Fills a rectangle of a window with its background pixel. */
    public void clearArea(int window, int x, int y, int width, int height) throws IOException {
        byte[] request = new byte[16];
        request[0] = 61;                            // ClearArea
        request[1] = 0;                             // no exposures
        request[2] = 4;
        writeInt(request, 4, window);
        writeShort(request, 8, x);
        writeShort(request, 10, y);
        writeShort(request, 12, width);
        writeShort(request, 14, height);
        send(request);
    }

    public void configureWindow(int window, int x, int y, int width, int height)
            throws IOException {
        byte[] request = new byte[28];
        request[0] = 12;                            // ConfigureWindow
        request[2] = 7;
        writeInt(request, 4, window);
        writeInt(request, 8, 0xF);                  // x, y, width, height
        writeInt(request, 12, x);                   // every ConfigureWindow value is 32 bit
        writeInt(request, 16, y);
        writeInt(request, 20, width);
        writeInt(request, 24, height);
        send(request);
    }

    public void destroyWindow(int window) throws IOException {
        byte[] request = new byte[8];
        request[0] = 4;                             // DestroyWindow
        request[2] = 2;
        writeInt(request, 4, window);
        send(request);
    }

    /**
     * GetGeometry: {@code {x, y, width, height}} of a window, with x and y signed and the size
     * unsigned - a window can hang off the left of the screen, and a screen cannot.
     */
    public int[] geometry(int window) throws IOException {
        byte[] request = new byte[8];
        request[0] = 14;                            // GetGeometry
        request[2] = 2;
        writeInt(request, 4, window);
        send(request);
        byte[] reply = readReply();
        return new int[] {readShort(reply, 12), readShort(reply, 14), readShort(reply, 16),
                readShort(reply, 18)};
    }

    /** One pixel of a drawable as ARGB, which is the shape the rest of the server talks in. */
    public int pixelAt(int drawable, int x, int y) throws IOException {
        byte[] image = getImage(drawable, x, y, 1, 1);
        return 0xFF000000 | ((image[34] & 0xFF) << 16) | ((image[33] & 0xFF) << 8)
                | (image[32] & 0xFF);
    }

    /** The root a window belongs to, which is how a test tells two screens apart. */
    public int rootOf(int window) throws IOException {
        byte[] request = new byte[8];
        request[0] = 14;                            // GetGeometry
        request[2] = 2;
        writeInt(request, 4, window);
        send(request);
        return readInt(readReply(), 8);
    }

    /**
     * The extension names this client's {@code ListExtensions} answers with, which is what a server
     * that hides extensions from an untrusted client (SECURITY, F7) filters.
     */
    public java.util.List<String> listExtensions() throws IOException {
        send(new byte[]{99, 0, 1, 0});
        byte[] reply = readReply();
        int count = reply[1] & 0xFF;
        java.util.List<String> names = new java.util.ArrayList<>();
        int at = 32;
        for (int i = 0; i < count; i++) {
            int length = reply[at] & 0xFF;
            names.add(new String(reply, at + 1, length,
                    java.nio.charset.StandardCharsets.ISO_8859_1));
            at += 1 + length;
        }
        return names;
    }

    /** Looks up an extension's major opcode, or 0 when the server does not have it. */
    public int queryExtension(String name) throws IOException {
        byte[] request = new byte[(8 + name.length() + 3) & ~3];
        request[0] = 98;                            // QueryExtension
        writeShort(request, 2, request.length / 4);
        writeShort(request, 4, name.length());
        for (int i = 0; i < name.length(); i++) {
            request[8 + i] = (byte) name.charAt(i);
        }
        send(request);
        byte[] reply = readReply();
        return reply[8] == 0 ? 0 : reply[9] & 0xFF;
    }

    /**
     * QueryExtension with every field it answers: {@code {present, majorOpcode, firstEvent,
     * firstError}}.
     *
     * <p>The first event code is what a caller has to add to an extension event's own number to
     * recognise it on the wire, and it is not derivable from anything the client already knows - a
     * probe that guesses it reads the event as a core one with a nonsense type.
     */
    public int[] queryExtensionInfo(String name) throws IOException {
        byte[] request = new byte[(8 + name.length() + 3) & ~3];
        request[0] = 98;                            // QueryExtension
        writeShort(request, 2, request.length / 4);
        writeShort(request, 4, name.length());
        for (int i = 0; i < name.length(); i++) {
            request[8 + i] = (byte) name.charAt(i);
        }
        send(request);
        byte[] reply = readReply();
        return new int[] {reply[8] & 0xFF, reply[9] & 0xFF, reply[10] & 0xFF, reply[11] & 0xFF};
    }

    /** XFixesQueryVersion: {@code {major, minor}} as the server reports them. */
    public int[] queryXfixesVersion(int opcode, int major, int minor) throws IOException {
        byte[] request = new byte[12];
        request[0] = (byte) opcode;
        request[1] = 0;                             // X_XFixesQueryVersion
        request[2] = 3;
        writeInt(request, 4, major);
        writeInt(request, 8, minor);
        send(request);
        byte[] reply = readReply();
        return new int[] {readInt(reply, 8), readInt(reply, 12)};
    }

    /**
     * XFixesSelectSelectionInput: subscribe to notifications about one selection.
     *
     * @param eventMask bit 0 SetSelectionOwner, bit 1 SelectionWindowDestroy, bit 2
     *     SelectionClientClose
     */
    public void selectSelectionInput(int opcode, int window, int selection, int eventMask)
            throws IOException {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = 2;                             // X_XFixesSelectSelectionInput
        request[2] = 4;
        writeInt(request, 4, window);
        writeInt(request, 8, selection);
        writeInt(request, 12, eventMask);
        send(request);
    }

    /** OpenFont: {@code name} is an X font name, e.g. {@code cursor} for the cursor font. */
    public void openFont(int font, String name) throws IOException {
        byte[] request = new byte[(12 + name.length() + 3) & ~3];
        request[0] = 45;                            // OpenFont
        writeShort(request, 2, request.length / 4);
        writeInt(request, 4, font);
        writeShort(request, 8, name.length());
        for (int i = 0; i < name.length(); i++) {
            request[12 + i] = (byte) name.charAt(i);
        }
        send(request);
    }

    /**
     * CreateGlyphCursor: a cursor drawn from one glyph of a font, which is what
     * {@code XCreateFontCursor} does - {@code source} is the shape's bitmap, {@code mask} the
     * character whose bitmap says where it is transparent.
     */
    public void createGlyphCursor(int cursor, int sourceFont, int maskFont, int sourceChar,
                                  int maskChar, int foreground, int background) throws IOException {
        // Eight units: a pad byte, three ids, then eight colours and characters - thirty two bytes,
        // which is already a whole number of words (the pad byte is the one right after the opcode,
        // inside the header).
        byte[] request = new byte[32];
        request[0] = 94;                            // CreateGlyphCursor
        request[2] = 8;
        writeInt(request, 4, cursor);
        writeInt(request, 8, sourceFont);
        writeInt(request, 12, maskFont);
        writeShort(request, 16, sourceChar);
        writeShort(request, 18, maskChar);
        writeShort(request, 20, (foreground >> 16) & 0xFF);
        writeShort(request, 22, (foreground >> 8) & 0xFF);
        writeShort(request, 24, foreground & 0xFF);
        writeShort(request, 26, (background >> 16) & 0xFF);
        writeShort(request, 28, (background >> 8) & 0xFF);
        writeShort(request, 30, background & 0xFF);
        send(request);
    }

    /** ChangeWindowAttributes with just {@code CWCursor}: which cursor the window shows. */
    public void setWindowCursor(int window, int cursor) throws IOException {
        byte[] request = new byte[16];
        request[0] = 2;                             // ChangeWindowAttributes
        request[2] = 4;
        writeInt(request, 4, window);
        writeInt(request, 8, 0x4000);               // CWCursor
        writeInt(request, 12, cursor);              // the only value in the list
        send(request);
    }

    /** WarpPointer: moves the pointer into {@code destination} at a window relative point. */
    public void warpPointer(int destination, int x, int y) throws IOException {
        byte[] request = new byte[24];
        request[0] = 41;                            // WarpPointer
        request[2] = 6;
        writeInt(request, 4, 0);                    // source window: None
        writeInt(request, 8, destination);
        writeShort(request, 20, x);
        writeShort(request, 22, y);
        send(request);
    }

    /** XFixesSelectCursorInput: subscribe to notifications about the cursor displayed on a screen. */
    public void selectCursorInput(int opcode, int window, int eventMask) throws IOException {
        byte[] request = new byte[12];
        request[0] = (byte) opcode;
        request[1] = 3;                             // X_XFixesSelectCursorInput
        request[2] = 3;
        writeInt(request, 4, window);
        writeInt(request, 8, eventMask);
        send(request);
    }

    /**
     * XFixesHideCursor: one counted hide of the pointer sprite while it is in this window.
     *
     * <p>Counted is the point: the requests pair up, a window that asked twice needs two shows, and
     * the cursor is hidden in that window rather than everywhere.
     */
    public void hideCursor(int opcode, int window) throws IOException {
        xfixesCursorRequest(opcode, 29, window);    // X_XFixesHideCursor
    }

    /** XFixesShowCursor: takes one hide back off the same window. */
    public void showCursor(int opcode, int window) throws IOException {
        xfixesCursorRequest(opcode, 30, window);    // X_XFixesShowCursor
    }

    /** The shape both of the cursor-visibility requests have: the minor opcode and a window. */
    private void xfixesCursorRequest(int opcode, int minor, int window) throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        request[2] = 2;
        writeInt(request, 4, window);
        send(request);
    }

    /**
     * The EWMH {@code _NET_WM_STATE} message a toolkit sends the root when it wants a window
     * fullscreen, maximised or shaded - which is the request, not the state: the window manager is
     * expected to answer by writing the state into the window's property.
     *
     * @param action 0 removes, 1 adds, 2 toggles
     */
    public void sendWindowState(int window, int action, int first, int second) throws IOException {
        byte[] event = new byte[32];
        event[0] = 33;                              // ClientMessage
        event[1] = 32;                              // format
        writeInt(event, 4, window);
        writeInt(event, 8, internAtom("_NET_WM_STATE"));
        writeInt(event, 12, action);
        writeInt(event, 16, first);
        writeInt(event, 20, second);
        sendEvent(root(), event);
    }

    /**
     * Sets a client's urgency hint, which is one bit of {@code WM_HINTS}' flags word: the client is
     * asking the window manager to bring it to the user's attention.
     */
    public void setUrgency(int window, boolean urgent) throws IOException {
        int hints = internAtom("WM_HINTS");
        setCardinalProperty(window, hints, hints, urgent ? 0x100 : 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /** The atom ids in a {@code _NET_WM_STATE} property, in the order the window carries them. */
    public int[] windowState(int window) throws IOException {
        int atom = internAtom("_NET_WM_STATE");
        byte[] reply = getProperty(window, atom, 0, false);
        if (RawXClient.readInt(reply, 8) == 0) {
            return new int[0];
        }
        int count = RawXClient.readInt(reply, 16);
        int[] states = new int[count];
        for (int i = 0; i < count; i++) {
            states[i] = RawXClient.readInt(reply, 32 + i * 4);
        }
        return states;
    }

    /**
     * An extension request with no arguments, and its reply - the shape most of them have.
     *
     * <p>Four bytes: the major opcode, the minor opcode, and a length of one unit. A request that
     * does take arguments cannot be sent this way, and the server would answer a shorter request than
     * it expects with {@code BadLength} - which is how a probe finds out.
     */
    public byte[] extensionRequest(int majorOpcode, int minorOpcode) throws IOException {
        return extensionRequest(majorOpcode, minorOpcode, 1);
    }

    /**
     * The same, in a request of {@code units} four byte words.
     *
     * <p>An extension's request length is its own: Xinerama's QueryVersion carries two argument bytes
     * and is therefore two units, and a server that reads its arguments would take the shorter one
     * as a bad request - which is how this was found.
     */
    public byte[] extensionRequest(int majorOpcode, int minorOpcode, int units) throws IOException {
        byte[] request = new byte[units * 4];
        request[0] = (byte) majorOpcode;
        request[1] = (byte) minorOpcode;
        writeShort(request, 2, units);
        send(request);
        return readReply();
    }

    /**
     * Sends a request that is already built, for a test that needs a layout no helper here covers.
     *
     * <p>One helper per extension request would double the size of this class and still miss the next
     * one; a test that needs one builds the bytes and reads the reply, which is the same shape the
     * hand-written probes use.
     */
    public void sendRequest(byte[] request) throws IOException {
        send(request);
    }

    /**
     * Arms a passive button grab: {@code GrabButton}, which fires when the button is pressed on that
     * window or anything inside it.
     *
     * @param modifiers {@code 0x8000} is {@code AnyModifier}; a specific one must match the state
     *     exactly
     * @param button 0 is {@code AnyButton}
     */
    public void grabButton(int window, boolean ownerEvents, int eventMask, int button, int modifiers,
                           int pointerMode, int keyboardMode) throws IOException {
        byte[] request = new byte[24];
        request[0] = 28;                            // GrabButton
        request[1] = (byte) (ownerEvents ? 1 : 0);
        writeShort(request, 2, 6);
        writeInt(request, 4, window);
        writeShort(request, 8, eventMask);
        request[10] = (byte) pointerMode;
        request[11] = (byte) keyboardMode;
        request[20] = (byte) button;
        writeShort(request, 22, modifiers);
        send(request);
    }

    /** Arms a passive key grab. {@code key} 0 is {@code AnyKey}. */
    public void grabKey(int window, boolean ownerEvents, int modifiers, int key, int pointerMode,
                        int keyboardMode) throws IOException {
        byte[] request = new byte[16];
        request[0] = 33;                            // GrabKey
        request[1] = (byte) (ownerEvents ? 1 : 0);
        writeShort(request, 2, 4);
        writeInt(request, 4, window);
        writeShort(request, 8, modifiers);
        request[10] = (byte) key;
        request[11] = (byte) pointerMode;
        request[12] = (byte) keyboardMode;
        send(request);
    }

    /**
     * Disarms a passive button grab: {@code UngrabButton}. The button is the byte right after the
     * opcode, not the field the window is in - writing it there loses the window.
     */
    public void ungrabButton(int window, int button, int modifiers) throws IOException {
        byte[] request = new byte[12];
        request[0] = 29;                            // UngrabButton
        request[1] = (byte) button;
        writeShort(request, 2, 3);
        writeInt(request, 4, window);
        writeShort(request, 8, modifiers);
        send(request);
    }

    /** Disarms a passive key grab: {@code UngrabKey}, on the same terms as {@link #ungrabButton}. */
    public void ungrabKey(int window, int key, int modifiers) throws IOException {
        byte[] request = new byte[12];
        request[0] = 34;                            // UngrabKey
        request[1] = (byte) key;
        writeShort(request, 2, 3);
        writeInt(request, 4, window);
        writeShort(request, 8, modifiers);
        send(request);
    }

    /** GetKeyboardMapping: the keysyms a keycode carries, one entry per shift level. */
    public int[] keyboardMapping(int firstKeycode, int count) throws IOException {
        byte[] request = new byte[8];
        request[0] = 101;                           // GetKeyboardMapping
        request[2] = 2;
        request[4] = (byte) firstKeycode;
        request[5] = (byte) count;
        send(request);
        byte[] reply = readReply();
        int perKeycode = reply[1] & 0xFF;
        int[] keysyms = new int[count * perKeycode];
        for (int i = 0; i < keysyms.length; i++) {
            keysyms[i] = readInt(reply, 32 + i * 4);
        }
        return keysyms;
    }

    /** GetAtomName: what an atom is called, or "<unset>" for one the server does not know. */
    public String atomName(int atom) throws IOException {
        byte[] request = new byte[8];
        request[0] = 17;                            // GetAtomName
        request[2] = 2;
        writeInt(request, 4, atom);
        send(request);
        byte[] reply = readReply();
        // GetAtomName: the name length is the sixteen bit field at 8, not the 32 bit one a
        // GetProperty reply carries in the same place.
        int length = RawXClient.readShort(reply, 8);
        return length == 0 ? "<unnamed>"
                : new String(reply, 32, length, java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    /** SetSelectionOwner: {@code owner} 0 gives the selection up, 1 names the requesting client. */
    public void setSelectionOwner(int owner, int selection, int time) throws IOException {
        byte[] request = new byte[16];
        request[0] = 22;                            // SetSelectionOwner
        request[2] = 4;
        writeInt(request, 4, owner);
        writeInt(request, 8, selection);
        writeInt(request, 12, time);                // 0 is CurrentTime
        send(request);
    }

    /** GetSelectionOwner: the window id that owns a selection, or 0 when nobody does. */
    public int getSelectionOwner(int selection) throws IOException {
        byte[] request = new byte[8];
        request[0] = 23;                            // GetSelectionOwner
        request[2] = 2;
        writeInt(request, 4, selection);
        send(request);
        return readInt(readReply(), 8);
    }

    /** ConvertSelection: asks the owner for a target, to be put in a property of the requestor. */
    public void convertSelection(int requestor, int selection, int target, int property, int time)
            throws IOException {
        byte[] request = new byte[24];
        request[0] = 24;                            // ConvertSelection
        request[2] = 6;
        writeInt(request, 4, requestor);
        writeInt(request, 8, selection);
        writeInt(request, 12, target);
        writeInt(request, 16, property);
        writeInt(request, 20, time);
        send(request);
    }

    /**
     * GetProperty for a whole property.
     *
     * @return the reply's bytes, with the format at offset 1, the type at 8, {@code bytesAfter} at
     *     12, the item count at 16 and the value from offset 32 - the raw reply, because a test
     *     usually wants more than one of those fields
     */
    public byte[] getProperty(int window, int property, int type, boolean delete)
            throws IOException {
        return getProperty(window, property, type, delete, 0, 0xFFFFFFFF);
    }

    /**
     * GetProperty with the two fields that decide how much is read: {@code longOffset} and
     * {@code longLength}, both in 4 byte units. {@code longLength} 0xFFFFFFFF is the conventional
     * "as much as there is", and is what a client reading a clipboard property sends.
     */
    public byte[] getProperty(int window, int property, int type, boolean delete, int longOffset,
                              int longLength) throws IOException {
        byte[] request = new byte[24];
        request[0] = 20;                            // GetProperty
        request[1] = (byte) (delete ? 1 : 0);
        writeShort(request, 2, 6);
        writeInt(request, 4, window);
        writeInt(request, 8, property);
        writeInt(request, 12, type);
        writeInt(request, 16, longOffset);
        writeInt(request, 20, longLength);
        send(request);
        return readReply();
    }

    /**
     * SendEvent with a {@code SelectionNotify}, which is how the owner of a selection answers: it
     * tells the requestor where the data was put, or {@code property 0} when it refuses the target.
     */
    public void sendSelectionNotify(int destination, int time, int requestor, int selection,
                                    int target, int property) throws IOException {
        byte[] event = new byte[32];
        event[0] = 31;                              // SelectionNotify
        writeShort(event, 2, 0);                    // sequence: the server fills it in
        writeInt(event, 4, time);
        writeInt(event, 8, requestor);
        writeInt(event, 12, selection);
        writeInt(event, 16, target);
        writeInt(event, 20, property);
        sendEvent(destination, event);
    }

    /**
     * The {@code ClientMessage} one client sends another: no event mask, so it goes to whoever
     * created the destination window. This is the shape every EWMH and XDND message takes.
     */
    public void sendClientMessage(int destination, int messageType, int... data) throws IOException {
        byte[] event = new byte[32];
        event[0] = 33;                              // ClientMessage
        event[1] = 32;                              // format
        writeInt(event, 4, destination);
        writeInt(event, 8, messageType);
        for (int i = 0; i < Math.min(5, data.length); i++) {
            writeInt(event, 12 + i * 4, data[i]);
        }
        sendEvent(destination, event);
    }

    /** SendEvent with an event mask, so it goes to whoever selected for that mask. */
    public void sendEventWithMask(int destination, int eventMask, byte[] event) throws IOException {
        byte[] request = new byte[44];
        request[0] = 25;                            // SendEvent
        request[1] = 0;
        writeShort(request, 2, 11);
        writeInt(request, 4, destination);
        writeInt(request, 8, eventMask);
        System.arraycopy(event, 0, request, 12, 32);
        send(request);
    }

    /** SendEvent: {@code event} is 32 bytes in the client's own byte order, as Xlib sends it. */
    public void sendEvent(int destination, byte[] event) throws IOException {
        byte[] request = new byte[44];
        request[0] = 25;                            // SendEvent
        request[1] = 0;                             // propagate: no
        request[2] = 11;
        writeInt(request, 4, destination);
        writeInt(request, 8, 0);                    // event mask: nobody else need hear it
        System.arraycopy(event, 0, request, 12, 32);
        send(request);
    }

    /**
     * Creates and maps a window that publishes a {@code WM_CLASS}, which is the name the server
     * remembers a client's host window by - {@code null} for the class leaves the property off, as a
     * client that publishes none does.
     *
     * @return the window id
     */
    public int mapWindowWithClass(String className, String title) throws IOException {
        int window = allocateId();
        createWindow(window, root(), 0, 0, 200, 200);
        int string = internAtom("STRING");
        if (className != null) {
            // Two NUL separated strings, instance then class, which is what WM_CLASS is.
            setStringProperty(window, internAtom("WM_CLASS"), string,
                    className + "\0" + className + "\0");
        }
        setStringProperty(window, internAtom("WM_NAME"), string, title == null ? "window" : title);
        mapWindow(window);
        flush();
        return window;
    }

    /** Moves the pointer through XTEST, which is how a test plays the part of the user. */
    public void fakeMotion(int opcode, int x, int y) throws IOException {
        fakeInput(opcode, 6, 0, x, y);               // MotionNotify, absolute
    }

    /** Presses or releases a button through XTEST, at wherever the pointer already is. */
    public void fakeButton(int opcode, int button, boolean pressed) throws IOException {
        fakeInput(opcode, pressed ? 4 : 5, button, 0, 0);
    }

    private void fakeInput(int opcode, int type, int detail, int x, int y) throws IOException {
        byte[] request = new byte[36];
        request[0] = (byte) opcode;
        request[1] = 2;                             // X_XTestFakeInput
        request[2] = 9;
        request[4] = (byte) type;
        request[5] = (byte) detail;
        writeShort(request, 24, x);
        writeShort(request, 26, y);
        send(request);
    }

    /**
     * Grabs the pointer, as a menu does while it is open.
     *
     * @param ownerEvents true when the client wants the crossings of its own windows rather than
     *     every event reported against the grab window
     */
    public int grabPointer(int window, boolean ownerEvents, int eventMask) throws IOException {
        return grabPointer(window, ownerEvents, eventMask, GRAB_ASYNC, GRAB_ASYNC);
    }

    /**
     * Grabs the pointer with the device modes spelled out.
     *
     * <p>The modes are the whole of A13, and their numbering is the trap: {@code GrabModeSync} is
     * <b>0</b> and {@code GrabModeAsync} is <b>1</b>, which is the opposite way round from
     * {@code AllowEvents}, where {@code SyncPointer} is 1. A {@code Sync} grab tells the server to
     * stop the device after the event that triggered it, and {@code AllowEvents} is what starts it
     * again - or reprocesses the event it stopped on.
     */
    public int grabPointer(int window, boolean ownerEvents, int eventMask, int pointerMode,
                           int keyboardMode) throws IOException {
        byte[] request = new byte[24];
        request[0] = 26;                            // GrabPointer
        request[1] = (byte) (ownerEvents ? 1 : 0);
        request[2] = 6;
        writeInt(request, 4, window);
        writeShort(request, 8, eventMask);
        request[10] = (byte) pointerMode;
        request[11] = (byte) keyboardMode;
        send(request);
        return readReply()[1] & 0xFF;               // the grab status
    }

    /** {@code GrabModeSync}/{@code GrabModeAsync}, as {@code X.h} numbers them. */
    public static final int GRAB_SYNC = 0;
    public static final int GRAB_ASYNC = 1;

    /**
     * {@code AllowEvents}, with the mode numbered as the protocol numbers it: 0
     * {@code AsyncPointer}, 1 {@code SyncPointer}, 2 {@code ReplayPointer}, 3/4/5 the keyboard's,
     * 6 {@code AsyncBoth}, 7 {@code SyncBoth}.
     */
    public void allowEvents(int mode) throws IOException {
        allowEvents(mode, 0);
    }

    /**
     * The same with a time of its own, which the protocol compares with now and with the grab's own
     * time: a request outside that range is ignored. Zero is {@code CurrentTime} and is what every
     * real client sends.
     */
    public void allowEvents(int mode, int time) throws IOException {
        byte[] request = new byte[8];
        request[0] = 35;                            // AllowEvents
        request[1] = (byte) mode;
        writeShort(request, 2, 2);
        writeInt(request, 4, time);
        send(request);
    }

    /**
     * {@code QueryPointer} against a window: its answer is where the pointer the <em>clients</em>
     * see is, which is not the same as where the device is while one of them is frozen.
     *
     * @return {rootX, rootY}
     */
    public int[] queryPointer(int window) throws IOException {
        byte[] request = new byte[8];
        request[0] = 38;                            // QueryPointer
        request[2] = 2;
        writeInt(request, 4, window);
        send(request);
        byte[] reply = readReply();
        return new int[] {readShort(reply, 16), readShort(reply, 18)};
    }

    public void ungrabPointer() throws IOException {
        byte[] request = new byte[8];
        request[0] = 27;                            // UngrabPointer
        request[2] = 2;
        send(request);
    }

    /**
     * Sends {@code GrabKeyboard} **without** reading the reply it answers with.
     *
     * <p>The reply is deliberately left for the caller: {@code GrabKeyboard} puts the focus on the
     * window it is taken on, which is reported as a pair of focus events, and those events are
     * written *after* the reply - so a caller that reads them first finds the reply, and a caller
     * that reads the reply with {@link #readReply} and then the events finds the events, because
     * {@code readReply} stops at the first reply and leaves everything after it alone. Read the
     * status first, then the events: the other order reads a reply as an event of type 1.
     */
    public void sendGrabKeyboard(int window, boolean ownerEvents) throws IOException {
        byte[] request = new byte[16];
        request[0] = 31;                            // GrabKeyboard
        request[1] = (byte) (ownerEvents ? 1 : 0);
        writeShort(request, 2, 4);
        writeInt(request, 4, window);
        writeInt(request, 8, 0);                    // CurrentTime
        request[12] = 1;                            // pointer mode: Async
        request[13] = 1;                            // keyboard mode: Async
        send(request);
    }

    /** {@code UngrabKeyboard}, which also answers with the focus events before anything else. */
    public void sendUngrabKeyboard() throws IOException {
        byte[] request = new byte[8];
        request[0] = 32;                            // UngrabKeyboard
        writeShort(request, 2, 2);
        writeInt(request, 4, 0);                    // CurrentTime
        send(request);
    }

    /** Creates a graphics context on a screen with the given foreground pixel. */
    public int createGC(int gc, int screenRoot, int foreground) throws IOException {
        byte[] request = new byte[20];
        request[0] = 55;                            // CreateGC
        request[2] = 5;
        writeInt(request, 4, gc);
        writeInt(request, 8, screenRoot);           // the GC takes its screen from this drawable
        writeInt(request, 12, 0x4);                 // GCForeground
        writeInt(request, 16, foreground);
        send(request);
        return gc;
    }

    /**
     * CreatePixmap: a drawable of its own depth, which is how a one-bit glyph mask is captured -
     * a depth 1 pixmap answers {@code GetImage} with one bit per pixel and nothing to separate.
     */
    public int createPixmap(int pixmap, int drawable, int width, int height, int depth)
            throws IOException {
        byte[] request = new byte[16];
        request[0] = 53;                            // CreatePixmap
        request[1] = (byte) depth;
        writeShort(request, 2, 4);
        writeInt(request, 4, pixmap);
        writeInt(request, 8, drawable);
        writeShort(request, 12, width);
        writeShort(request, 14, height);
        send(request);
        return pixmap;
    }

    /**
     * ChangeGC with one value: the mask says which one and the value follows it, in mask bit order.
     * A caller that changes two things at once would have to order the values itself - none does.
     */
    public void changeGC(int gc, int mask, int value) throws IOException {
        byte[] request = new byte[16];
        request[0] = 56;                            // ChangeGC
        writeShort(request, 2, 4);
        writeInt(request, 4, gc);
        writeInt(request, 8, mask);
        writeInt(request, 12, value);
        send(request);
    }

    /** {@code GCFont}, the GC value that decides which font a text request draws with. */
    public static final int GCFONT = 0x4000;

    /** {@code GCForeground}. */
    public static final int GCFOREGROUND = 0x4;

    /** ImageText8: a Latin-1 string drawn with the GC's font at a baseline origin. */
    public void imageText8(int drawable, int gc, int x, int y, byte[] text, int offset, int length)
            throws IOException {
        byte[] request = new byte[(16 + length + 3) & ~3];
        request[0] = 76;                            // ImageText8
        request[1] = (byte) length;
        writeShort(request, 2, request.length / 4);
        writeInt(request, 4, drawable);
        writeInt(request, 8, gc);
        writeShort(request, 12, x);
        writeShort(request, 14, y);
        System.arraycopy(text, offset, request, 16, length);
        send(request);
    }

    /** The bytes of a {@code ListFonts}-family request: the pattern, padded to four bytes. */
    private static byte[] listFontsRequest(int opcode, String pattern, int maxNames) {
        byte[] request = new byte[(8 + pattern.length() + 3) & ~3];
        request[0] = (byte) opcode;
        writeShort(request, 2, request.length / 4);
        writeShort(request, 4, maxNames);
        writeShort(request, 6, pattern.length());
        for (int i = 0; i < pattern.length(); i++) {
            request[8 + i] = (byte) pattern.charAt(i);
        }
        return request;
    }

    /** {@code ListFonts}: the names of the fonts matching a pattern, at most {@code maxNames}. */
    public java.util.List<String> listFonts(String pattern, int maxNames) throws IOException {
        send(listFontsRequest(49, pattern, maxNames));
        byte[] reply = readReply();
        return names(reply, 32, readShort(reply, 8));
    }

    /**
     * {@code ListFontsWithInfo}: the name of every font matching a pattern - one reply each, ended
     * by a terminator whose name is empty - so the names here are the fonts' <em>own</em> rather
     * than the ones the pattern matched.
     *
     * <p>That difference is the point of the request and it is not cosmetic: a font reached through
     * an alias is reported under its real XLFD name, and the XLFD's last two fields are the only
     * place Xlib's font-set machinery can read a font's charset from. A server that answers with
     * the alias name makes every font unclassifiable and every {@code XCreateFontSet} fail, which is
     * what no X input method could be used against this server for (backlog C10).
     */
    public java.util.List<String> listFontsWithInfo(String pattern, int maxNames)
            throws IOException {
        send(listFontsRequest(50, pattern, maxNames));
        java.util.List<String> out = new java.util.ArrayList<>();
        while (true) {
            byte[] reply = readReply();
            int nameLength = reply[1] & 0xFF;
            if (nameLength == 0) {
                return out;                         // the terminator
            }
            // properties_len is at 46 and the properties follow the 60 byte fixed part, so the name
            // is at 60 plus eight bytes a property.
            int propertyCount = readShort(reply, 46);
            out.add(new String(reply, 60 + propertyCount * 8, nameLength,
                    java.nio.charset.StandardCharsets.ISO_8859_1));
        }
    }

    /** A list of length prefixed names, as the {@code ListFonts} family returns them. */
    private static java.util.List<String> names(byte[] reply, int offset, int count) {
        java.util.List<String> out = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            int length = reply[offset] & 0xFF;
            out.add(new String(reply, offset + 1, length,
                    java.nio.charset.StandardCharsets.ISO_8859_1));
            offset += 1 + length;
        }
        return out;
    }

    /**
     * QueryFont, whole. The reply is one of the long ones - the fixed part is 60 bytes and the
     * properties and per-character metrics follow it - so the caller reads the fields it wants out
     * of the bytes rather than this class guessing which they are.
     */
    public byte[] queryFont(int font) throws IOException {
        byte[] request = new byte[8];
        request[0] = 47;                            // QueryFont
        writeShort(request, 2, 2);
        writeInt(request, 4, font);
        send(request);
        return readReply();
    }

    /** Fills a rectangle of a drawable with the GC's foreground. */
    public void fillRectangle(int window, int gc, int x, int y, int width, int height)
            throws IOException {
        // One request, rectangles included: the length field has to cover them or the server reads
        // the request as truncated.
        byte[] request = new byte[20];
        request[0] = 70;                            // PolyFillRectangle
        request[2] = 5;
        writeInt(request, 4, window);
        writeInt(request, 8, gc);
        writeShort(request, 12, x);
        writeShort(request, 14, y);
        writeShort(request, 16, width);
        writeShort(request, 18, height);
        send(request);
    }

    /**
     * {@code PolyFillRectangle} with several rectangles in <b>one request</b>: one damage rectangle
     * over several areas, which is what a test needs to see how an update splits them.
     *
     * <p>Each argument is {@code x, y, width, height}.
     */
    public void fillRectangles(int window, int gc, int[]... rectangles) throws IOException {
        byte[] request = new byte[12 + 8 * rectangles.length];
        request[0] = 70;                            // PolyFillRectangle
        request[2] = (byte) (request.length / 4);
        writeInt(request, 4, window);
        writeInt(request, 8, gc);
        int at = 12;
        for (int[] rectangle : rectangles) {
            writeShort(request, at, rectangle[0]);
            writeShort(request, at + 2, rectangle[1]);
            writeShort(request, at + 4, rectangle[2]);
            writeShort(request, at + 6, rectangle[3]);
            at += 8;
        }
        send(request);
    }

    /**
     * FillPoly, the request every filled shape a toolkit draws reduces to: a closed path, filled by
     * the GC's fill rule (which is where even-odd and winding are chosen - the request's own
     * {@code shape} field is only a hint to the server).
     *
     * <p>Added for the polygon-fill investigation the xlogo difference led to: the fill is where a
     * server's scan conversion shows, and a hand-written client is the only way to ask both servers
     * the same question.
     */
    public void fillPolygon(int window, int gc, int[] points) throws IOException {
        int count = points.length / 2;
        // FillPoly is the odd request out: the shape byte and the coordinate mode come *after* the
        // drawable and the GC rather than in the data byte every other drawing request has, and there
        // is no point count at all - the server derives it from the length. Written from the server's
        // own decoder (VariableLengthDecoders.readFillPoly), which is the only reading that is right:
        // the first version of this helper put the shape at byte 1 and a count at byte 16, and the
        // request was accepted, drew nothing, and read as "the two servers' polygon fills agree".
        int body = 16 + count * 4;
        byte[] request = new byte[(body + 3) & ~3];
        request[0] = 69;                            // FillPoly
        request[1] = 0;                             // pad
        writeShort(request, 2, request.length / 4);
        writeInt(request, 4, window);
        writeInt(request, 8, gc);
        request[12] = 0;                            // shape: Complex
        request[13] = 0;                            // coordinate mode: Origin
        for (int i = 0; i < count; i++) {
            writeShort(request, 16 + i * 4, points[2 * i]);
            writeShort(request, 18 + i * 4, points[2 * i + 1]);
        }
        send(request);
    }

    /**
     * Sends a whole ZPixmap image, which is what a toolkit's software renderer uploads each frame.
     * The data is BGRA, one int per pixel, as the server's image byte order expects.
     */
    public void putImage(int drawable, int gc, int x, int y, int width, int height, byte[] bgra)
            throws IOException {
        int dataLength = width * height * 4;
        byte[] request = new byte[24 + ((dataLength + 3) & ~3)];
        request[0] = 72;                            // PutImage
        request[1] = 2;                             // ZPixmap
        // The length field is 16 bit, so the image has to fit one request unless the client goes
        // through BIG-REQUESTS; tests keep theirs small enough not to need it.
        int units = request.length / 4;
        if (units > 65535) {
            throw new IllegalArgumentException("image too large for a single request: " + units
                    + " units");
        }
        writeShort(request, 2, units);
        writeInt(request, 4, drawable);
        writeInt(request, 8, gc);
        writeShort(request, 12, width);
        writeShort(request, 14, height);
        writeShort(request, 16, x);
        writeShort(request, 18, y);
        request[20] = 0;                            // left pad
        request[21] = 24;                           // depth
        System.arraycopy(bgra, 0, request, 24, Math.min(bgra.length, dataLength));
        out.write(request);
        out.flush();
    }

    /**
     * CopyArea: a rectangle from one drawable to another. Nothing else in the kit sends one, and it
     * is how a caret blink puts a frame on the screen - into a pixmap, then copied to the window -
     * so a window that keeps its own pixels has to take the copy there rather than straight onto the
     * screen (B5, {@code WindowStoreTest}).
     */
    public void copyArea(int srcDrawable, int dstDrawable, int gc, int srcX, int srcY, int dstX,
                         int dstY, int width, int height) throws IOException {
        byte[] request = new byte[28];
        request[0] = 62;                            // CopyArea
        writeShort(request, 2, 7);
        writeInt(request, 4, srcDrawable);
        writeInt(request, 8, dstDrawable);
        writeInt(request, 12, gc);
        writeShort(request, 16, srcX);
        writeShort(request, 18, srcY);
        writeShort(request, 20, dstX);
        writeShort(request, 22, dstY);
        writeShort(request, 24, width);
        writeShort(request, 26, height);
        send(request);
    }

    /** The children of a window, which for a root is every window the client has made. */
    public int[] queryTree(int window) throws IOException {
        byte[] request = new byte[8];
        request[0] = 15;                            // QueryTree
        request[2] = 2;
        writeInt(request, 4, window);
        send(request);
        byte[] reply = readReply();
        int count = readShort(reply, 16);
        int[] children = new int[count];
        for (int i = 0; i < count; i++) {
            children[i] = readInt(reply, 32 + i * 4);
        }
        return children;
    }

    /**
     * Asks for the pointer's history inside a window between two times: one {@code {time, x, y}} per
     * event, oldest first.
     *
     * <p>This is the request a client makes twice - once for the count, once with room for the
     * events - and the reply's {@code nitems} is what says which. Times are 32 bit and wrap, so the
     * caller brackets its own gestures with the same clock the server stamps them with.
     */
    public java.util.List<long[]> getMotionEvents(int window, long start, long stop)
            throws IOException {
        byte[] request = new byte[16];
        request[0] = 39;                            // GetMotionEvents
        request[2] = 4;
        writeInt(request, 4, window);
        writeInt(request, 8, (int) start);
        writeInt(request, 12, (int) stop);
        send(request);
        byte[] reply = readReply();
        int count = readInt(reply, 8);              // nitems
        // A reply whose length field disagrees with its item count desynchronises the stream: every
        // answer after it is read from the wrong offset. Say so here rather than letting a later
        // assertion report the wrong number.
        int length = readInt(reply, 4);
        if (length != count * 2 || reply.length != 32 + length * 4) {
            throw new IOException("GetMotionEvents reply is inconsistent: length=" + length
                    + " items=" + count + " bytes=" + reply.length);
        }
        java.util.List<long[]> events = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            int at = 32 + i * 8;                    // Timecoord: time, x, y
            events.add(new long[]{readInt(reply, at) & 0xFFFFFFFFL,
                    (short) readShort(reply, at + 4), (short) readShort(reply, at + 6)});
        }
        return events;
    }

    /**
     * SHAPE's {@code ShapeMask}: sets or combines a shape from a one bit pixmap, whose set bits are
     * the shape. This is what {@code XShapeCombineMask} sends, and the call xeyes shapes its window
     * with - the mask is built by drawing into a depth 1 pixmap, so using it exercises the whole
     * path from a pixmap's bits to a window's shape.
     */
    public void shapeMask(int opcode, int window, int kind, int operation, int xOffset, int yOffset,
                          int bitmap) throws IOException {
        byte[] request = new byte[20];
        request[0] = (byte) opcode;
        request[1] = 2;                             // minor: ShapeMask
        writeShort(request, 2, 5);
        request[4] = (byte) operation;
        request[5] = (byte) kind;
        request[6] = 0;                             // ordering: UnSorted
        writeInt(request, 8, window);
        writeShort(request, 12, (short) xOffset);
        writeShort(request, 14, (short) yOffset);
        writeInt(request, 16, bitmap);
        send(request);
    }

    /**
     * SHAPE's {@code ShapeRectangles}: replaces or combines one of a window's shapes, in window
     * coordinates. {@code kind} is 0 bounding, 1 clip, 2 input; {@code operation} is 0 set, 1 union,
     * 2 intersect, 3 subtract, 4 invert.
     */
    public void shapeRectangles(int opcode, int window, int kind, int operation, int[][] rectangles)
            throws IOException {
        // An extension request carries its minor opcode where a core request carries its first
        // field, and SHAPE's ShapeRectangles is minor 1. Putting the destination kind there instead
        // makes the server read the request as QueryVersion - which *replies*, so the mistake shows
        // up as a sequence number out of step rather than as a shape that never took.
        byte[] request = new byte[16 + rectangles.length * 8];
        request[0] = (byte) opcode;
        request[1] = 1;                             // minor: ShapeRectangles
        writeShort(request, 2, request.length / 4);
        request[4] = (byte) operation;              // operation comes before the destination kind
        request[5] = (byte) kind;
        request[6] = 0;                             // ordering: UnSorted
        writeInt(request, 8, window);
        writeShort(request, 12, (short) 0);         // x-offset
        writeShort(request, 14, (short) 0);         // y-offset
        for (int i = 0; i < rectangles.length; i++) {
            writeShort(request, 16 + i * 8, rectangles[i][0]);
            writeShort(request, 18 + i * 8, rectangles[i][1]);
            writeShort(request, 20 + i * 8, rectangles[i][2]);
            writeShort(request, 22 + i * 8, rectangles[i][3]);
        }
        send(request);
    }

    /**
     * XFixesCreateRegion: a region made of rectangles, which is how a client builds the shapes and
     * clips it hands to {@code SetWindowShapeRegion} and friends.
     */
    public void createRegion(int opcode, int region, int[][] rectangles) throws IOException {
        byte[] request = new byte[8 + rectangles.length * 8];
        request[0] = (byte) opcode;
        request[1] = 5;                             // X_XFixesCreateRegion
        writeShort(request, 2, request.length / 4);
        writeInt(request, 4, region);
        for (int i = 0; i < rectangles.length; i++) {
            writeShort(request, 8 + i * 8, rectangles[i][0]);
            writeShort(request, 10 + i * 8, rectangles[i][1]);
            writeShort(request, 12 + i * 8, rectangles[i][2]);
            writeShort(request, 14 + i * 8, rectangles[i][3]);
        }
        send(request);
    }

    /** XFixesSetCursorName: the name a client gives a cursor of its own, or "" to take one back. */
    public void setCursorName(int opcode, int cursor, String name) throws IOException {
        byte[] bytes = name.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        byte[] request = new byte[12 + ((bytes.length + 3) & ~3)];
        request[0] = (byte) opcode;
        request[1] = 23;                            // X_XFixesSetCursorName
        writeShort(request, 2, request.length / 4);
        writeInt(request, 4, cursor);
        writeShort(request, 8, bytes.length);       // the name begins at twelve: cursor, nbytes, pad
        System.arraycopy(bytes, 0, request, 12, bytes.length);
        send(request);
    }

    /** DAMAGE's Create: a damage object on a drawable, at one of X's report levels. */
    public void createDamage(int opcode, int damage, int drawable, int level) throws IOException {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = 1;                             // X_DamageCreate
        writeShort(request, 2, 4);
        writeInt(request, 4, damage);
        writeInt(request, 8, drawable);
        request[12] = (byte) level;
        send(request);
    }

    /** DAMAGE's Destroy: the object goes and the client's id is free again. */
    public void destroyDamage(int opcode, int damage) throws IOException {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = 2;                             // X_DamageDestroy
        writeShort(request, 2, 2);
        writeInt(request, 4, damage);
        send(request);
    }

    /**
     * DAMAGE's Subtract with no repair and no parts: what the client has caught up with is thrown
     * away, and at the bounding levels the next change is the one that gets reported.
     */
    public void subtractDamage(int opcode, int damage) throws IOException {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = 3;                             // X_DamageSubtract
        writeShort(request, 2, 4);
        writeInt(request, 4, damage);
        send(request);
    }

    /** XFixesSetRegion: replaces the rectangles a region is made of. */
    public void setRegion(int opcode, int region, int[][] rectangles) throws IOException {
        byte[] request = new byte[8 + rectangles.length * 8];
        request[0] = (byte) opcode;
        request[1] = 11;                            // X_XFixesSetRegion
        writeShort(request, 2, request.length / 4);
        writeInt(request, 4, region);
        for (int i = 0; i < rectangles.length; i++) {
            writeShort(request, 8 + i * 8, rectangles[i][0]);
            writeShort(request, 10 + i * 8, rectangles[i][1]);
            writeShort(request, 12 + i * 8, rectangles[i][2]);
            writeShort(request, 14 + i * 8, rectangles[i][3]);
        }
        send(request);
    }

    /** XFixesSetWindowShapeRegion: one of a window's three shapes, from a region. */
    public void setWindowShapeRegion(int opcode, int window, int kind, int region)
            throws IOException {
        // dest, destination kind, three bytes of pad, x offset, y offset, region: five units.
        byte[] request = new byte[20];
        request[0] = (byte) opcode;
        request[1] = 21;                            // X_XFixesSetWindowShapeRegion
        writeShort(request, 2, 5);
        writeInt(request, 4, window);
        request[8] = (byte) kind;
        writeInt(request, 16, region);
        send(request);
    }

    /** Presses or releases one key, through XTEST. */
    public void fakeKey(int opcode, int keycode, boolean pressed) throws IOException {
        byte[] request = new byte[36];
        request[0] = (byte) opcode;                 // XTEST FakeInput
        request[1] = 2;
        request[2] = 9;
        request[4] = (byte) (pressed ? 2 : 3);      // KeyPress / KeyRelease
        request[5] = (byte) keycode;
        request[35] = 0;                            // the core keyboard
        send(request);
    }

    /**
     * {@code ChangeKeyboardControl}, for the auto-repeat mode: an empty keycode list means every key.
     */
    public void setAutoRepeat(int keycode, boolean on) throws IOException {
        byte[] request = new byte[12];
        request[0] = 102;                           // ChangeKeyboardControl
        request[2] = 3;
        writeInt(request, 4, 0x80);                 // value-mask: AutoRepeatMode
        writeInt(request, 8, (on ? 1 : 0) | (keycode < 0 ? 0 : keycode << 8));
        send(request);
    }

    /** Reads pixels back from a drawable, as ZPixmap bytes. */
    public byte[] getImage(int drawable, int x, int y, int width, int height) throws IOException {
        return getImage(drawable, x, y, width, height, 0xFFFFFFFF);
    }

    /**
     * The same with the plane mask spelled out, which a <em>pixmap</em> needs: a reference server
     * refuses a mask with bits set outside the drawable's depth ({@code BadMatch}), so reading back a
     * one-bit glyph mask asks for plane 0 alone.
     */
    public byte[] getImage(int drawable, int x, int y, int width, int height, int planeMask)
            throws IOException {
        byte[] request = new byte[20];
        request[0] = 73;                            // GetImage
        request[1] = 2;                             // ZPixmap
        request[2] = 5;
        writeInt(request, 4, drawable);
        writeShort(request, 8, x);
        writeShort(request, 10, y);
        writeShort(request, 12, width);
        writeShort(request, 14, height);
        writeInt(request, 16, planeMask);
        send(request);

        return readReply();
    }

    /** ChangeWindowAttributes with an event mask, which is how a client says what it wants to hear. */
    public void selectInput(int window, int mask) throws IOException {
        byte[] request = new byte[16];
        request[0] = 2;                             // ChangeWindowAttributes
        request[2] = 4;
        writeInt(request, 4, window);
        writeInt(request, 8, 0x800);                // CWEventMask
        writeInt(request, 12, mask);
        send(request);
    }

    public void mapWindow(int window) throws IOException {
        byte[] request = new byte[8];
        request[0] = 8;                             // MapWindow
        request[2] = 2;
        writeInt(request, 4, window);
        send(request);
    }

    public void unmapWindow(int window) throws IOException {
        byte[] request = new byte[8];
        request[0] = 10;                            // UnmapWindow
        request[2] = 2;
        writeInt(request, 4, window);
        send(request);
    }

    /** Makes a window override-redirect, which is what a menu or a tooltip is. */
    public void setOverrideRedirect(int window) throws IOException {
        byte[] request = new byte[12];
        request[0] = 2;                             // ChangeWindowAttributes
        request[2] = 3;
        writeInt(request, 4, window);
        writeInt(request, 8, 0x200);                // CWOverrideRedirect
        send(request);
    }

    /**
     * Interns a name and returns the atom, since the reply is the only place it is reported.
     *
     * <p><b>Every method here that reads a reply throws away the events that arrived before it</b> -
     * a reply is found by reading until one turns up. A test that is waiting for an event therefore
     * asks for its atoms, its properties and its geometry *first*: done the other way round, the
     * event being waited for is the one that disappears, and the failure looks like a server that
     * sent nothing.
     */
    public int internAtom(String name) throws IOException {
        byte[] request = new byte[(8 + name.length() + 3) & ~3];
        request[0] = 16;                            // InternAtom
        request[1] = 0;                             // create the atom if it does not exist
        writeShort(request, 2, request.length / 4);
        request[4] = (byte) name.length();
        request[5] = (byte) (name.length() >> 8);
        for (int i = 0; i < name.length(); i++) {
            request[8 + i] = (byte) name.charAt(i);
        }
        send(request);
        return readInt(readReply(), 8);
    }

    /** ChangeProperty in replace mode, with 32 bit items. */
    public void setAtomListProperty(int window, int property, int type, int... atoms)
            throws IOException {
        byte[] request = new byte[(24 + atoms.length * 4 + 3) & ~3];
        request[0] = 18;                            // ChangeProperty
        request[1] = 0;                             // replace
        writeShort(request, 2, request.length / 4);
        writeInt(request, 4, window);
        writeInt(request, 8, property);
        writeInt(request, 12, type);
        request[16] = 32;                           // format
        writeInt(request, 20, atoms.length);
        for (int i = 0; i < atoms.length; i++) {
            writeInt(request, 24 + i * 4, atoms[i]);
        }
        send(request);
    }

    /**
     * SetInputFocus: {@code focus} is a window id, 0 for None or 1 for PointerRoot.
     *
     * <p>Twelve bytes, not sixteen: a reference server answers a request whose length disagrees with
     * the protocol with {@code BadLength}, and this one does not check - it reads the length and hands
     * the payload on - so an over-long request here passes against this server and is rejected by a
     * real one. That is exactly the kind of difference the probes against a reference server exist to
     * catch, and it caught this.
     */
    public void setInputFocus(int focus, int revertTo) throws IOException {
        byte[] request = new byte[12];
        request[0] = 42;                            // SetInputFocus
        request[1] = (byte) revertTo;
        request[2] = 3;
        writeInt(request, 4, focus);
        writeInt(request, 8, 0);                    // time: CurrentTime
        send(request);
    }

    /**
     * How long a read waits for an event before giving up.
     *
     * <p>Five seconds by default, which is what a test that waits for an event wants. A test that
     * has to prove nothing came, or that drains what a setup produced before reading its own, wants
     * it much shorter - waiting out the default costs a test five seconds an assertion.
     */
    public void readTimeoutMillis(int millis) throws IOException {
        socket.setSoTimeout(millis);
    }

    /** GetInputFocus: the focus window, 0 for None or 1 for PointerRoot. */
    public int queryInputFocus() throws IOException {
        byte[] request = new byte[4];
        request[0] = 43;                            // GetInputFocus
        request[2] = 1;
        send(request);
        return readInt(readReply(), 8);
    }

    /** ChangeProperty in replace mode, with 8 bit items: how a window title is published. */
    public void setStringProperty(int window, int property, int type, String value)
            throws IOException {
        byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] request = new byte[(24 + bytes.length + 3) & ~3];
        request[0] = 18;                            // ChangeProperty
        request[1] = 0;                             // replace
        writeShort(request, 2, request.length / 4);
        writeInt(request, 4, window);
        writeInt(request, 8, property);
        writeInt(request, 12, type);
        request[16] = 8;                            // format
        writeInt(request, 20, bytes.length);
        System.arraycopy(bytes, 0, request, 24, bytes.length);
        send(request);
    }

    /** ChangeProperty in replace mode, with 32 bit items: how an icon is published. */
    public void setCardinalProperty(int window, int property, int type, int... values)
            throws IOException {
        byte[] request = new byte[(24 + values.length * 4 + 3) & ~3];
        request[0] = 18;                            // ChangeProperty
        request[1] = 0;                             // replace
        writeShort(request, 2, request.length / 4);
        writeInt(request, 4, window);
        writeInt(request, 8, property);
        writeInt(request, 12, type);
        request[16] = 32;                           // format
        writeInt(request, 20, values.length);
        for (int i = 0; i < values.length; i++) {
            writeInt(request, 24 + i * 4, values[i]);
        }
        send(request);
    }

    public void flush() throws IOException {
        out.flush();
    }

    /** Reads one event, or returns null when the server sends nothing within the socket's timeout. */
    public byte[] readEvent() throws IOException {
        byte[] event = new byte[32];
        try {
            in.readFully(event);
        } catch (java.net.SocketTimeoutException quiet) {
            return null;
        }
        if ((event[0] & 0x7F) == GENERIC_EVENT) {
            // A GenericEvent carries a length field of its own, and an XInput2 device event - which
            // is what one usually is here - is a hundred and twenty bytes. Read as the thirty two a
            // reader assumes, the rest of it stays in the stream and every event after it is read
            // from the wrong offset. This is the same lesson as {@link #longEvents}, for the one
            // event family whose size is written down in the event itself.
            int extra = readInt(event, 4) * 4;
            if (extra <= 0) {
                return event;
            }
            byte[] whole = new byte[32 + extra];
            System.arraycopy(event, 0, whole, 0, 32);
            in.readFully(whole, 32, extra);
            return whole;
        }
        if (longEventTypes.contains(event[0] & 0x7F)) {
            byte[] whole = new byte[48];
            System.arraycopy(event, 0, whole, 0, 32);
            in.readFully(whole, 32, 16);
            return whole;
        }
        return event;
    }

    /** Core event type 35, the GenericEvent an XInput2 device event arrives as. */
    public static final int GENERIC_EVENT = 35;

    /**
     * Registers event types that are longer than the thirty two bytes a reader assumes.
     *
     * <p>An event carries no length field - its size is implied by its type - so a reader that always
     * takes thirty two bytes leaves the rest of a long one in the stream and every event after it is
     * nonsense. DAMAGE's {@code DamageNotify} is the one this kit meets: forty eight bytes, with the
     * damaged area and the drawable's geometry after the fixed part. A probe says which types are
     * long (the extension's own event base is what it knows and this reader cannot), and every read
     * after that brings the whole event.
     */
    public void longEvents(int... types) {
        for (int type : types) {
            longEventTypes.add(type);
        }
    }

    /** The event types registered as longer than thirty two bytes. */
    private final java.util.Set<Integer> longEventTypes = new java.util.HashSet<>();

    /** Reads an event and keeps reading until it is the wanted one, or the server goes quiet. */
    public byte[] readEvent(int type) throws IOException {
        for (byte[] event = readEvent(); event != null; event = readEvent()) {
            if ((event[0] & 0x7F) == type) {
                return event;
            }
        }
        return null;
    }

    /**
     * Reads the next reply, complete, so that every field sits at its protocol offset.
     *
     * <p>A reply is thirty two bytes whatever its length field says: that field counts only the extra
     * data that follows, which is nothing at all for a reply like {@code InternAtom} whose values all
     * fit in the fixed part.
     */
    public byte[] readReply() throws IOException {
        byte[] reply;
        while (true) {
            reply = readFully(32);
            int type = reply[0] & 0xFF;
            if (type == 1) {
                break;                              // the reply
            }
            if (type == 0) {
                // The sequence in an error names the request it belongs to, which is the whole of
                // what makes one diagnosable when several are in flight.
                // The opcodes are printed with it because an error's code is only meaningful
                // against the extension that raised it: 0 is BadDevice to XInput2 and nothing at
                // all to the core protocol, and a reader who cannot see which request it was has a
                // number with no context.
                throw new IOException("the server refused request "
                        + readShort(reply, 2) + ": error " + (reply[1] & 0xFF) + " value 0x"
                        + Integer.toHexString(readInt(reply, 4)) + " major " + (reply[10] & 0xFF)
                        + " minor " + readShort(reply, 8) + " (waiting for "
                        + outstandingSequence() + ")");
            }
            // Anything else is an event the client never asked to read separately - a NoExposure
            // arrives after every drawing request, for one - so it is dropped rather than mistaken
            // for the answer.
        }
        int expected = outstandingSequence() & 0xFFFF;
        int got = readShort(reply, 2);
        if (got != expected) {
            throw new IOException("reply for sequence " + got + " arrived while waiting for "
                    + expected + ": a reply was left unread, and every answer after it is suspect");
        }
        int extra = readInt(reply, 4) * 4;
        if (extra > 0) {
            byte[] all = new byte[32 + extra];
            System.arraycopy(reply, 0, all, 0, 32);
            System.arraycopy(readFully(extra), 0, all, 32, extra);
            return all;
        }
        return reply;
    }

    /** True once the server has closed the connection, which is how a disconnect shows up here. */
    public boolean atEndOfStream() throws IOException {
        try {
            return in.read() < 0;
        } catch (java.net.SocketTimeoutException quiet) {
            return false;
        }
    }

    /** Writes a CARD32 into a request a caller is building, at a protocol offset. */
    public static void writeIntAt(byte[] request, int offset, int value) {
        writeInt(request, offset, value);
    }

    /** Writes a CARD16 into a request a caller is building, at a protocol offset. */
    public static void writeShortAt(byte[] request, int offset, int value) {
        writeShort(request, offset, value);
    }

    public static int readInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8)
                | ((bytes[offset + 2] & 0xFF) << 16) | ((bytes[offset + 3] & 0xFF) << 24);
    }

    public static int readShort(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);
    }

    /**
     * Sends one request, remembering its sequence number.
     *
     * <p>The X protocol numbers every request a connection sends, and every reply and error carries
     * the number of the request that caused it. Checking it is what tells "this reply is the answer
     * to my request" from "this is the answer to an earlier one": a reply left unread desynchronises
     * everything after it, and the symptom is not an error but a *wrong value* - which is how a test
     * that asks for an empty range once came back with the three events of the query before it.
     */
    private void send(byte[] request) throws IOException {
        out.write(request);
        flush();
        sequence++;
    }

    /** The sequence number of the request whose reply is expected next. */
    private int outstandingSequence() {
        return sequence;
    }

    /**
     * How many requests this connection has sent, which is the sequence number of its last one - the
     * number a recording's client-died element carries when it goes away.
     */
    public int sentRequests() {
        return sequence;
    }

    private byte[] readFully(long count) throws IOException {
        byte[] bytes = new byte[(int) count];
        in.readFully(bytes);
        return bytes;
    }

    private static void writeInt(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) value;
        bytes[offset + 1] = (byte) (value >> 8);
        bytes[offset + 2] = (byte) (value >> 16);
        bytes[offset + 3] = (byte) (value >> 24);
    }

    /**
     * The request length is a card16. Writing only its low byte works for every small request
     * and silently truncates a large one - a property value of more than 1020 bytes, which is
     * exactly the size a chunked clipboard transfer is made of.
     */
    private static void writeShort(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) value;
        bytes[offset + 1] = (byte) (value >> 8);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
