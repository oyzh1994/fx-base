package cn.oyzh.fx.xserver.tools;

import cn.oyzh.fx.xserver.test.RawXClient;

/**
 * The DOUBLE-BUFFER extension (Xdbe) as a client uses it - the harness for F5.
 *
 * <p>DBE is what a client uses to draw into a buffer nobody is looking at and then show it in one
 * step: a back buffer is allocated <em>on</em> a window, the client draws into it with the ordinary
 * core requests (a back buffer is a drawable), and {@code SwapBuffers} makes what it drew the
 * window's contents. Every toolkit's "no flicker" path wants it and most fall back to plain drawing
 * today, which is why the backlog has it as a candidate rather than a gap.
 *
 * <p>What the probe does is the whole life of a buffer, and every step is printed whether or not the
 * server answered, because "the extension is missing" and "the request was refused" are different
 * findings: the version handshake (three times, since whether the reply is the server's version or
 * the client's is a per-extension rule this kit measures rather than assumes), what visual information
 * a client can ask for, allocating a buffer on a mapped window, drawing into it and reading both
 * drawables back, the swap under each swap action, the attributes query, the refusals an id that is
 * not a buffer deserves, two buffers swapped in one request, a window resized under its buffers, and
 * the two idiom requests.
 *
 * <p><b>The request layouts are not the specification's.</b> The first version of this probe was
 * written from the Xdbe specification's own request tables and every request was refused; the layouts
 * here were then taken from the protocol header a client compiles against
 * ({@code X11/extensions/dbeproto.h}, on the VM) and checked against a reference server one field
 * position at a time. Three of them differ from that table, and they are the ones that matter:
 * {@code AllocateBackBufferName} is <b>window, buffer, swapAction</b> and has <b>no reply</b>;
 * {@code SwapBuffers} carries a <em>count</em> and then count (window, swapAction) pairs, so there is
 * no single buffer to name at all; and {@code GetBackBufferAttributes} is minor <b>7</b>, where 6 is
 * {@code GetVisualInfo}.
 *
 * <p>Front and back are told apart by colour rather than by ordering: before every swap the window
 * and the buffer are filled with a colour of their own, so one line says which of the two the window
 * shows afterwards, where the back buffer's content went, and whether the window's previous content
 * came back into the buffer - which is the whole question a swap action answers.
 *
 * <p>Usage: {@code DbeProbe &lt;host&gt; &lt;port&gt;}.
 */
public final class DbeProbe {

    /** DBE's own requests, in the protocol's own numbering - which is not the order in the manual. */
    private static final int GET_VERSION = 0;
    private static final int ALLOCATE_BACK_BUFFER = 1;
    private static final int DEALLOCATE_BACK_BUFFER = 2;
    private static final int SWAP_BUFFERS = 3;
    private static final int BEGIN_IDIOM = 4;
    private static final int END_IDIOM = 5;
    private static final int GET_VISUAL_INFO = 6;
    private static final int GET_BACK_BUFFER_ATTRIBUTES = 7;

    /** The swap actions, in the protocol's order. */
    private static final String[] ACTIONS = {"Undefined", "Background", "Untouched", "Copied"};

    private static final int FRONT = 0x00FF00;              // the window's own background, green
    private static final int FRONT_ONLY = 0x101010;         // what the window is filled with
    private static final int BACK_ONLY = 0x202020;          // what the buffer is filled with

    /** A 24 bit plane mask: the depth of both drawables here, and a mask above it is BadMatch. */
    private static final int PLANES = 0xFFFFFF;

    /** How long to wait before calling a request that has no reply accepted. */
    private static final int QUIET_MILLIS = 500;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("usage: DbeProbe <host> <port>");
            return;
        }
        try (RawXClient client = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
            int[] info = client.queryExtensionInfo("DOUBLE-BUFFER");
            System.out.println("DOUBLE-BUFFER present=" + info[0] + " major=" + info[1]
                    + " firstEvent=" + info[2] + " firstError=" + info[3]);
            if (info[0] == 0) {
                return;
            }
            int opcode = info[1];

            for (int[] asked : new int[][]{{1, 0}, {0, 0}, {2, 0}}) {
                client.sendRequest(getVersion(opcode, asked[0], asked[1]));
                byte[] reply = reply(client, info);
                System.out.println("  QueryVersion(" + asked[0] + "." + asked[1] + ") -> "
                        + (reply == null ? "refused"
                                : (reply[8] & 0xFF) + "." + (reply[9] & 0xFF)));
            }

            int window = client.allocateId();
            client.createWindow(window, client.root(), 20, 20, 60, 40, FRONT);
            client.mapWindow(window);
            int gc = client.allocateId();
            client.createGC(gc, window, 0xFFFFFF);
            client.flush();
            Thread.sleep(200);
            System.out.println("  the window before any buffer -> " + pixel(client, window, 5, 5)
                    + " (its own background)");

            // What a client asks before it double buffers: the visuals that support it.
            for (int[] shape : new int[][]{{1, 1}, {2, 2}}) {
                client.sendRequest(getVisualInfo(opcode, client.root(), shape[0]));
                byte[] visuals = reply(client, info);
                if (visuals == null) {
                    System.out.println("  GetVisualInfo(n=" + shape[0] + ") -> refused");
                    continue;
                }
                // The reply is a count of the drawables answered, then *per drawable* a count and
                // that many {visualID, depth, perfLevel} records - measured, and not the flat list
                // the protocol header's comment describes. The count of visuals is the screen's own
                // (a reference has a hundred and forty four, this server four), so what a comparison
                // can use is the shape and the first record, not the number of them.
                int screens = RawXClient.readInt(visuals, 8);
                int at = 32;
                StringBuilder listed = new StringBuilder();
                for (int i = 0; i < screens; i++) {
                    int visualsOfScreen = RawXClient.readInt(visuals, at);
                    at += 4;
                    if (i == 0) {
                        listed.append(visualsOfScreen).append(" visual(s) on the first screen");
                        if (visualsOfScreen > 0) {
                            listed.append(" [visual=0x")
                                    .append(Integer.toHexString(RawXClient.readInt(visuals, at)))
                                    .append(" depth=").append(visuals[at + 4] & 0xFF)
                                    .append(" perfLevel=").append(visuals[at + 5] & 0xFF)
                                    .append(']');
                        }
                    }
                    at += visualsOfScreen * 8;
                }
                System.out.println("  GetVisualInfo(" + (shape[0] == 1 ? "the root" : "the root twice")
                        + ") -> " + screens + " section(s), " + listed);
            }
            // A client that names no drawable at all: what comes back is a reply with nothing in it,
            // and the count of sections is what a client walks - which is why a reply to a request
            // this empty is worth printing rather than assuming.
            client.sendRequest(getVisualInfo(opcode, client.root(), 0));
            byte[] empty = reply(client, info);
            System.out.println("  GetVisualInfo(no drawables) -> "
                    + (empty == null ? "refused"
                            : RawXClient.readInt(empty, 8) + " section(s) of the display, length="
                                    + RawXClient.readInt(empty, 4)));
            System.out.println("  GetVisualInfo(a drawable that is not one) -> "
                    + (sendGetVisualInfo(client, opcode, 0x7FE00000, info) ? "answered" : "refused"));

            int buffer = client.allocateId();
            client.sendRequest(allocate(opcode, window, buffer, 3));
            System.out.println("  AllocateBackBuffer(window, 0x" + Integer.toHexString(buffer)
                    + ", Copied) -> " + outcome(client, info));

            client.sendRequest(attributes(opcode, buffer));
            byte[] attributes = reply(client, info);
            System.out.println("  GetBackBufferAttributes -> " + (attributes == null ? "refused"
                    : "attributes=0x" + Integer.toHexString(RawXClient.readInt(attributes, 8))
                            + " isTheWindow=" + (RawXClient.readInt(attributes, 8) == window)));

            // The back buffer is a drawable, so the ordinary requests address it by its own id.
            client.fillRectangle(buffer, gc, 0, 0, 60, 40);
            client.flush();
            Thread.sleep(100);
            System.out.println("  after drawing into the buffer: window="
                    + pixel(client, window, 5, 5) + " buffer=" + pixel(client, buffer, 5, 5));

            // Each action, with both sides filled with a colour of their own first: the question a
            // swap action answers is what happens to the *back* buffer, and a pair of known colours
            // makes all four answers readable at once.
            for (int action : new int[]{3, 0, 1, 2}) {
                client.changeGC(gc, RawXClient.GCFOREGROUND, FRONT_ONLY + action);
                client.fillRectangle(window, gc, 0, 0, 60, 40);
                client.changeGC(gc, RawXClient.GCFOREGROUND, BACK_ONLY + action);
                client.fillRectangle(buffer, gc, 0, 0, 60, 40);
                client.flush();
                Thread.sleep(50);
                client.sendRequest(swap(opcode, window, action));
                System.out.println("  SwapBuffers(" + ACTIONS[action] + ") -> " + outcome(client, info)
                        + " window=" + pixel(client, window, 5, 5) + " (was " + colour(FRONT_ONLY + action)
                        + ") buffer=" + pixel(client, buffer, 5, 5) + " (was "
                        + colour(BACK_ONLY + action) + ")");
            }

            // What a buffer holds the moment it is allocated, on a window that has been painted:
            // whether the front is handed over or the buffer starts clean is a server decision.
            int fresh = client.allocateId();
            client.changeGC(gc, RawXClient.GCFOREGROUND, 0x404040);
            client.fillRectangle(window, gc, 0, 0, 60, 40);
            client.flush();
            Thread.sleep(50);
            client.sendRequest(allocate(opcode, window, fresh, 3));
            outcome(client, info);
            // The specification leaves a new buffer's content undefined, and the reference answers
            // with whatever the memory held (a colour this probe painted earlier), so what is printed
            // is only whether a freshly allocated buffer holds the front buffer's content or not.
            System.out.println("  a buffer allocated on a window painted " + colour(0x404040)
                    + " -> holds the front's content: "
                    + (pixel(client, fresh, 5, 5).equals(colour(0x404040))));

            // A window that is not mapped, and an action the protocol does not define at all.
            int unmapped = client.allocateId();
            client.createWindow(unmapped, client.root(), 200, 200, 20, 20, FRONT);
            int onUnmapped = client.allocateId();
            client.sendRequest(allocate(opcode, unmapped, onUnmapped, 3));
            System.out.println("  AllocateBackBuffer(on an unmapped window) -> " + outcome(client, info));
            int badAction = client.allocateId();
            client.sendRequest(allocate(opcode, window, badAction, 99));
            System.out.println("  AllocateBackBuffer(swapAction=99) -> " + outcome(client, info));

            // An action the protocol does not define, and a swap for a window that has no buffers.
            client.sendRequest(swap(opcode, window, 99));
            System.out.println("  SwapBuffers(action=99) -> " + outcome(client, info));
            int bare = client.allocateId();
            client.createWindow(bare, client.root(), 100, 100, 20, 20, FRONT);
            client.mapWindow(bare);
            client.sendRequest(swap(opcode, bare, 3));
            System.out.println("  SwapBuffers(a window with no buffers) -> " + outcome(client, info));

            // A second buffer on the same window, and both swapped in one request: SwapBuffers
            // carries a count and a list of (window, action) pairs, not one buffer.
            int second = client.allocateId();
            client.sendRequest(allocate(opcode, window, second, 3));
            System.out.println("  AllocateBackBuffer(a second buffer on the same window) -> "
                    + outcome(client, info));
            client.sendRequest(allocate(opcode, window, buffer, 3));
            System.out.println("  AllocateBackBuffer(the first id again) -> " + outcome(client, info));
            client.changeGC(gc, RawXClient.GCFOREGROUND, 0x00FFFF);
            client.fillRectangle(second, gc, 0, 0, 60, 40);
            client.changeGC(gc, RawXClient.GCFOREGROUND, 0x303030);
            client.fillRectangle(window, gc, 0, 0, 60, 40);
            client.flush();
            Thread.sleep(50);
            client.sendRequest(swapTwo(opcode, window, 3, bare, 3));
            System.out.println("  SwapBuffers(two pairs: this window, then the bare one) -> "
                    + outcome(client, info) + " window=" + pixel(client, window, 5, 5)
                    + " (was " + colour(0x303030) + ", and a swap would have shown "
                    + colour(0x00FFFF) + ") second=" + pixel(client, second, 5, 5));

            // Two buffers on one window, each filled with a colour of its own before every swap:
            // SwapBuffers names a window and not a buffer, so which of the two becomes the front -
            // and what the other one holds afterwards - is the server's answer rather than the
            // protocol's, and it is what a client with two buffers sees.
            for (int action : new int[]{3, 0, 1, 2}) {
                client.changeGC(gc, RawXClient.GCFOREGROUND, 0x500000 + action);
                client.fillRectangle(second, gc, 0, 0, 60, 40);
                client.changeGC(gc, RawXClient.GCFOREGROUND, 0x600000 + action);
                client.fillRectangle(buffer, gc, 0, 0, 60, 40);
                client.changeGC(gc, RawXClient.GCFOREGROUND, 0x700000 + action);
                client.fillRectangle(window, gc, 0, 0, 60, 40);
                client.flush();
                Thread.sleep(50);
                client.sendRequest(swap(opcode, window, action));
                System.out.println("  two buffers, " + ACTIONS[action] + ": window="
                        + pixel(client, window, 5, 5) + " (was " + colour(0x700000 + action)
                        + ") firstBuffer=" + pixel(client, buffer, 5, 5) + " (was "
                        + colour(0x600000 + action) + ") secondBuffer=" + pixel(client, second, 5, 5)
                        + " (was " + colour(0x500000 + action) + ") -> " + outcome(client, info));
            }

            // A swap that names no window at all: the request is legal, and does nothing.
            byte[] emptySwap = new byte[8];
            emptySwap[0] = (byte) opcode;
            emptySwap[1] = SWAP_BUFFERS;
            emptySwap[2] = 2;                           // (2 + 2n) units, n = 0
            client.sendRequest(emptySwap);
            System.out.println("  SwapBuffers(n=0) -> " + outcome(client, info));

            // Refusals: an id that was never a buffer, then the requests made after the buffer is
            // gone, then the allocates that must not be taken.
            int stranger = client.allocateId();
            client.sendRequest(attributes(opcode, stranger));
            byte[] strangerAttributes = reply(client, info);
            System.out.println("  GetBackBufferAttributes(a plain id) -> "
                    + (strangerAttributes == null ? "refused"
                            : "attributes=0x"
                                    + Integer.toHexString(RawXClient.readInt(strangerAttributes, 8))));
            client.sendRequest(allocate(opcode, stranger, client.allocateId(), 3));
            System.out.println("  AllocateBackBuffer(an id that is not a window) -> "
                    + outcome(client, info));

            client.sendRequest(deallocate(opcode, buffer));
            System.out.println("  DeallocateBackBuffer -> " + outcome(client, info));
            client.sendRequest(attributes(opcode, buffer));
            byte[] freed = reply(client, info);
            System.out.println("  GetBackBufferAttributes(a freed buffer) -> "
                    + (freed == null ? "refused"
                            : "attributes=0x" + Integer.toHexString(RawXClient.readInt(freed, 8))));
            client.sendRequest(deallocate(opcode, buffer));
            System.out.println("  DeallocateBackBuffer(twice) -> " + outcome(client, info));

            // A window resized under its buffers: whether a buffer follows its window's size is a
            // server decision this probe measures rather than assumes.
            client.configureWindow(window, 20, 20, 80, 50);
            client.flush();
            Thread.sleep(200);
            client.sendRequest(attributes(opcode, second));
            byte[] wide = reply(client, info);
            System.out.println("  after the window grew to 80x50: attributes of its buffer -> "
                    + (wide == null ? "refused"
                            : "attributes=0x" + Integer.toHexString(RawXClient.readInt(wide, 8))));
            System.out.println("    a pixel outside the old 60x40: window="
                    + pixelOrRefusal(client, window, 70, 45) + " buffer="
                    + pixelOrRefusal(client, second, 70, 45));
            System.out.println("    and inside it, where the buffer held " + colour(0x00FFFF)
                    + ": buffer=" + pixelOrRefusal(client, second, 5, 5));

            // The idioms: hints that the swaps between them belong together.
            client.sendRequest(idiom(opcode, BEGIN_IDIOM));
            System.out.println("  BeginIdiom -> " + outcome(client, info));
            client.sendRequest(swap(opcode, window, 3));
            System.out.println("  SwapBuffers(inside an idiom) -> " + outcome(client, info));
            client.sendRequest(idiom(opcode, END_IDIOM));
            System.out.println("  EndIdiom -> " + outcome(client, info));
            client.sendRequest(idiom(opcode, END_IDIOM));
            System.out.println("  EndIdiom(without a begin) -> " + outcome(client, info));

            // A second client: DBE's ids are the server's, so what a *stranger* can do with another
            // client's buffer - and what it is told - is a measurement rather than an assumption.
            try (RawXClient other = RawXClient.connect(args[0], Integer.parseInt(args[1]))) {
                int[] otherInfo = other.queryExtensionInfo("DOUBLE-BUFFER");
                int otherOpcode = otherInfo[1];
                int otherWindow = other.allocateId();
                other.createWindow(otherWindow, other.root(), 300, 300, 40, 30, FRONT);
                other.mapWindow(otherWindow);
                int otherBuffer = other.allocateId();
                other.sendRequest(allocate(otherOpcode, otherWindow, otherBuffer, 3));
                System.out.println("  a second client's own buffer -> "
                        + outcome(other, otherInfo));
                other.sendRequest(attributes(otherOpcode, buffer));
                byte[] foreign = reply(other, otherInfo);
                System.out.println("  the second client asks for this client's buffer -> "
                        + (foreign == null ? "refused"
                                : "attributes=0x" + Integer.toHexString(RawXClient.readInt(foreign, 8))
                                        + " isTheFirstWindow="
                                        + (RawXClient.readInt(foreign, 8) == window)));
                other.sendRequest(swap(otherOpcode, window, 3));
                System.out.println("  the second client swaps this client's window -> "
                        + outcome(other, otherInfo));
                other.sendRequest(deallocate(otherOpcode, buffer));
                System.out.println("  the second client frees this client's buffer -> "
                        + outcome(other, otherInfo));
                other.sendRequest(attributes(otherOpcode, buffer));
                byte[] stillThere = reply(other, otherInfo);
                System.out.println("    afterwards, the buffer's attributes -> "
                        + (stillThere == null ? "refused"
                                : "attributes=0x"
                                        + Integer.toHexString(RawXClient.readInt(stillThere, 8))));
            }

            // And a minor opcode the extension does not define, which must be refused rather than
            // ignored: a client that is told nothing has no way to know its request went nowhere.
            client.sendRequest(new byte[]{(byte) opcode, 8, 1, 0});
            System.out.println("  unknown minor opcode 8 -> " + outcome(client, info));
        }
    }

    // ------------------------------------------------------------------
    // Requests, in the layouts the protocol header gives
    // ------------------------------------------------------------------

    private static byte[] getVersion(int opcode, int major, int minor) {
        return new byte[]{(byte) opcode, GET_VERSION, 2, 0, (byte) major, (byte) minor, 0, 0};
    }

    private static byte[] getVisualInfo(int opcode, int drawable, int count) {
        byte[] request = new byte[8 + count * 4];
        request[0] = (byte) opcode;
        request[1] = GET_VISUAL_INFO;
        RawXClient.writeShortAt(request, 2, 2 + count);
        RawXClient.writeIntAt(request, 4, count);
        for (int i = 0; i < count; i++) {
            RawXClient.writeIntAt(request, 8 + i * 4, drawable);
        }
        return request;
    }

    private static boolean sendGetVisualInfo(RawXClient client, int opcode, int drawable, int[] info)
            throws Exception {
        client.sendRequest(getVisualInfo(opcode, drawable, 1));
        return reply(client, info) != null;
    }

    private static byte[] allocate(int opcode, int window, int buffer, int action) {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = ALLOCATE_BACK_BUFFER;
        request[2] = 4;                                 // length, in four byte units
        RawXClient.writeIntAt(request, 4, window);
        RawXClient.writeIntAt(request, 8, buffer);
        request[12] = (byte) action;
        return request;
    }

    private static byte[] deallocate(int opcode, int buffer) {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = DEALLOCATE_BACK_BUFFER;
        request[2] = 2;
        RawXClient.writeIntAt(request, 4, buffer);
        return request;
    }

    /** One (window, action) pair: a count, then the window and its action. */
    private static byte[] swap(int opcode, int window, int action) {
        byte[] request = new byte[16];
        request[0] = (byte) opcode;
        request[1] = SWAP_BUFFERS;
        request[2] = 4;                                 // (2 + 2n) units, n = 1
        RawXClient.writeIntAt(request, 4, 1);           // n
        RawXClient.writeIntAt(request, 8, window);
        request[12] = (byte) action;
        return request;
    }

    private static byte[] swapTwo(int opcode, int first, int firstAction, int second,
                                  int secondAction) {
        byte[] request = new byte[24];
        request[0] = (byte) opcode;
        request[1] = SWAP_BUFFERS;
        request[2] = 6;                                 // (2 + 2n) units, n = 2
        RawXClient.writeIntAt(request, 4, 2);
        RawXClient.writeIntAt(request, 8, first);
        request[12] = (byte) firstAction;
        RawXClient.writeIntAt(request, 16, second);
        request[20] = (byte) secondAction;
        return request;
    }

    private static byte[] idiom(int opcode, int which) {
        return new byte[]{(byte) opcode, (byte) which, 1, 0};
    }

    private static byte[] attributes(int opcode, int buffer) {
        byte[] request = new byte[8];
        request[0] = (byte) opcode;
        request[1] = GET_BACK_BUFFER_ATTRIBUTES;
        request[2] = 2;
        RawXClient.writeIntAt(request, 4, buffer);
        return request;
    }

    // ------------------------------------------------------------------
    // Reading what came back
    // ------------------------------------------------------------------

    /**
     * The reply to a request that has one, or null when the server refused it.
     *
     * <p>Read here rather than with {@code RawXClient.readReply} because that one reports a refusal as
     * an exception whose message carries the request's *sequence number*, which is the client's own
     * count and therefore a difference between two runs that is not a difference between two servers.
     * What is printed instead is the error's own identity, named the way a client reads it.
     */
    private static byte[] reply(RawXClient client, int[] info) throws Exception {
        client.flush();
        client.readTimeoutMillis(2000);
        byte[] header = client.readBytes(32);
        if ((header[0] & 0xFF) == 0) {
            System.out.println("    refused: " + errorName(header[1] & 0xFF, info[3]));
            return null;
        }
        int extra = RawXClient.readInt(header, 4) * 4;
        if (extra <= 0) {
            return header;
        }
        byte[] all = new byte[32 + extra];
        System.arraycopy(header, 0, all, 0, 32);
        System.arraycopy(client.readBytes(extra), 0, all, 32, extra);
        return all;
    }

    /**
     * What came back for a request that has no reply: {@code accepted} when the stream stays quiet,
     * because that is the only evidence a one-way request has.
     */
    private static String outcome(RawXClient client, int[] info) throws Exception {
        client.flush();
        client.readTimeoutMillis(QUIET_MILLIS);
        byte[] message = client.readEvent();
        if (message == null) {
            return "accepted";
        }
        if ((message[0] & 0x7F) == 0) {
            return "refused: " + errorName(message[1] & 0xFF, info[3]);
        }
        return "a message of type " + (message[0] & 0x7F) + " came back";
    }

    /**
     * An error named the way a client reads it: a core error by its own code, an extension error by
     * its offset from the extension's base - because the base is what the server assigns, and
     * printing "138" against "2" for the same BadBuffer is a difference that is not one.
     */
    private static String errorName(int code, int firstError) {
        if (firstError != 0 && code >= firstError) {
            return "extension error +" + (code - firstError);
        }
        return "core error " + code + " ("
                + CORE_ERRORS[Math.min(code, CORE_ERRORS.length - 1)] + ")";
    }

    private static final String[] CORE_ERRORS = {"none", "Request", "Value", "Window", "Pixmap",
            "Atom", "Cursor", "Font", "Match", "Drawable", "Access", "Alloc", "Colormap", "GContext",
            "IDChoice", "Name", "Length", "Implementation"};

    /** A colour the way {@link #pixel} prints one, so the probe can say what a side held. */
    private static String colour(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    /** One pixel as {@code #RRGGBB}, the shape the rest of the kit prints colours in. */
    private static String pixel(RawXClient client, int drawable, int x, int y) throws Exception {
        byte[] image = client.getImage(drawable, x, y, 1, 1, PLANES);
        return String.format("#%02X%02X%02X", image[34] & 0xFF, image[33] & 0xFF, image[32] & 0xFF);
    }

    private static String pixelOrRefusal(RawXClient client, int drawable, int x, int y) {
        try {
            return pixel(client, drawable, x, y);
        } catch (Exception refused) {
            return "refused";
        }
    }

    private static String hex(byte[] bytes, int count) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < count && i < bytes.length; i++) {
            out.append(String.format("%02x ", bytes[i]));
        }
        return out.toString().trim();
    }
}
