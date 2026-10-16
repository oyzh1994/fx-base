package cn.oyzh.fx.xserver.tools;

import java.io.IOException;

/**
 * The GLX questions a client asks, printed field by field and as bytes - the harness for B6.
 *
 * <pre>
 *   GlxProbe &lt;host&gt; &lt;port&gt;
 * </pre>
 *
 * <p>Part of the verification kit ({@code docs/backlog.md} §1). It exists because B6 has no other
 * way to be measured: {@code glxinfo} says "no GLX" and stops, so what a client needs from a server
 * - and what a reference answers - has to be asked one request at a time. Run it against Xvfb (which
 * has GLX 1.4 through Mesa) and against this server and diff the two, as the rest of the kit does.
 *
 * <p><b>What the reference does with a software client, measured 2026-09-27 by tapping the wire
 * while {@code glxgears} ran against Xvfb</b>: the client sends **no GL commands at all**. It asks
 * for GLX, takes a direct (software) context, renders in its own process with Mesa's {@code drisw}
 * path, and pushes each finished frame as an ordinary **{@code PutImage}** - 769 MB of them in five
 * seconds, one {@code GetGeometry} per frame as the synchronisation. So a server that wants software
 * GL clients needs no GL implementation: it needs enough of GLX for the client to choose that path.
 *
 * <p>The requests below are the ones that capture showed a real client making, in its order:
 * {@code QueryVersion}, {@code ClientInfo}, {@code GetVisualConfigs}, {@code CreateContext},
 * {@code IsDirect}, {@code MakeContextCurrent}. {@code GetFBConfigs} and {@code QueryServerString}
 * are here because {@code glxinfo} asks them and they are the same shape of answer.
 *
 * <p>**The opcode of GLX is assigned by the server**, so every run prints it - and a probe that
 * hard-codes one measures a different extension.
 */
public final class GlxProbe {

    // The opcode numbers are Mesa's, from `glxproto.h` on the VM - which is the authority the
    // first version of this probe guessed at and got wrong from 18 onwards (it is
    // QueryExtensionsString, not QueryServerString, and the two are one apart all the way down).
    private static final int CREATE_CONTEXT = 3;
    private static final int IS_DIRECT = 6;
    private static final int QUERY_VERSION = 7;
    private static final int GET_VISUAL_CONFIGS = 14;
    private static final int QUERY_EXTENSIONS_STRING = 18;
    private static final int QUERY_SERVER_STRING = 19;
    private static final int CLIENT_INFO = 20;
    private static final int GET_FB_CONFIGS = 21;
    private static final int QUERY_CONTEXT = 25;
    private static final int MAKE_CONTEXT_CURRENT = 26;
    private static final int GET_DRAWABLE_ATTRIBUTES = 29;

    /** The names in a {@code QueryServerString}'s request. */
    private static final int GLX_VENDOR = 1;
    private static final int GLX_VERSION = 2;
    private static final int GLX_EXTENSIONS = 3;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("usage: GlxProbe <host> <port>");
            return;
        }
        try (Wire wire = Wire.connect(args[0], Integer.parseInt(args[1]))) {
            int opcode = queryExtension(wire, "GLX");
            System.out.println("GLX major opcode = " + opcode);
            if (opcode == 0) {
                System.out.println("GLX is not present");
                return;
            }
            queryVersion(wire, opcode);
            extensionsString(wire, opcode);
            serverString(wire, opcode, GLX_VENDOR);
            serverString(wire, opcode, GLX_VERSION);
            serverString(wire, opcode, GLX_EXTENSIONS);
            clientInfo(wire, opcode);
            visualConfigs(wire, opcode);
            fbConfigs(wire, opcode);
            context(wire, opcode);
        }
    }

    /** {@code GLXQueryVersion}: what the server says it implements, as the two numbers it replies. */
    private static void queryVersion(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[12];
        header(request, opcode, QUERY_VERSION, 3);
        Wire.writeInt(request, 4, 1);                   // client major
        Wire.writeInt(request, 8, 4);                   // the client wants 1.4 or lower
        wire.send(request);
        byte[] reply = replyTo(wire);
        System.out.println("QueryVersion(1.4) -> " + Wire.readInt(reply, 8) + "."
                + Wire.readInt(reply, 12) + ", reply " + reply.length + " bytes");
        hex("QueryVersion", reply);
    }

    /**
     * {@code GLXQueryServerString}: the vendor, version or extension list, asked by name.
     *
     * <p>The request is **screen and name only** - no length and no string, which is what the first
     * version of this probe sent (and got {@code BadLength} for). The reply carries the length at
     * offset 12 and the string after the thirty two byte fixed part.
     */
    private static void serverString(Wire wire, int opcode, int name) throws Exception {
        byte[] request = new byte[12];
        header(request, opcode, QUERY_SERVER_STRING, 3);
        Wire.writeInt(request, 4, 0);                   // screen
        Wire.writeInt(request, 8, name);
        wire.send(request);
        byte[] reply = replyTo(wire);
        System.out.println("QueryServerString(" + name + ") -> \"" + stringOf(reply) + "\""
                + ", reply " + reply.length + " bytes");
        hex("QueryServerString", reply);
    }

    /** {@code GLXQueryExtensionsString}: the same list, asked without naming one. */
    private static void extensionsString(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[8];
        header(request, opcode, QUERY_EXTENSIONS_STRING, 2);
        Wire.writeInt(request, 4, 0);                   // screen
        wire.send(request);
        byte[] reply = replyTo(wire);
        System.out.println("QueryExtensionsString -> \"" + stringOf(reply) + "\"");
    }

    /** The string a GLX *String reply carries: its length is at offset 12, the text at 32. */
    private static String stringOf(byte[] reply) {
        int length = Wire.readInt(reply, 12);
        int from = 32;
        if (length <= 0 || from >= reply.length) {
            return "";
        }
        return new String(reply, from, Math.min(length, reply.length - from),
                java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    /** {@code GLXClientInfo}: the client announcing its version. Answered with nothing at all. */
    private static void clientInfo(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[16];
        header(request, opcode, CLIENT_INFO, 4);
        Wire.writeInt(request, 4, 1);                   // major
        Wire.writeInt(request, 8, 4);                   // minor
        Wire.writeInt(request, 12, 0);                  // an empty extension string
        wire.send(request);
        System.out.println("ClientInfo -> " + (refused(wire) ? "refused" : "accepted silently"));
    }

    /**
     * {@code GLXGetVisualConfigs}: the configs a client may draw into, in the GLX 1.2 layout.
     *
     * <p>The reply is a count pair and then a flat list of integers, {@code num_props} per config -
     * so the probe prints the counts and the first config's integers rather than guessing which
     * field is where. What matters to a client is that one of them is RGB, double buffered, with a
     * depth buffer, and names a visual the server also offers.
     */
    private static void visualConfigs(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[8];
        header(request, opcode, GET_VISUAL_CONFIGS, 2);
        Wire.writeInt(request, 4, 0);                   // screen
        wire.send(request);
        byte[] reply = replyTo(wire);
        int visuals = Wire.readInt(reply, 8);
        int props = Wire.readInt(reply, 12);
        System.out.println("GetVisualConfigs -> " + visuals + " config(s), " + props + " int(s) each"
                + ", reply " + reply.length + " bytes");
        if (visuals > 0 && reply.length >= 32 + props * 4) {
            StringBuilder first = new StringBuilder();
            for (int i = 0; i < props && 32 + i * 4 + 4 <= reply.length; i++) {
                first.append(i == 0 ? "" : ", ").append(Wire.readInt(reply, 32 + i * 4));
            }
            System.out.println("  first: " + first);
        }
    }

    /** {@code GLXGetFBConfigs}: the same configs in the GLX 1.3 layout, which {@code glxinfo} uses. */
    private static void fbConfigs(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[8];
        header(request, opcode, GET_FB_CONFIGS, 2);
        Wire.writeInt(request, 4, 0);
        wire.send(request);
        byte[] reply = replyTo(wire);
        int count = Wire.readInt(reply, 8);
        int props = Wire.readInt(reply, 12);
        System.out.println("GetFBConfigs -> " + count + " config(s), " + props * 2
                + " int(s) each, reply " + reply.length + " bytes");
        if (count > 0 && reply.length >= 32 + props * 8) {
            StringBuilder first = new StringBuilder();
            for (int i = 0; i < props * 2 && 32 + i * 4 + 4 <= reply.length; i++) {
                first.append(i == 0 ? "" : ", ").append(Wire.readInt(reply, 32 + i * 4));
            }
            System.out.println("  first: " + first);
        }
    }

    /**
     * A context: created, asked whether it is direct, made current, and queried.
     *
     * <p>Created twice - once asking for a **direct** context and once for an indirect one - because
     * which of the two a server grants is the whole of what a software client does next: granted
     * direct, the client renders in its own process and blits; refused, it sends GL commands over the
     * wire, which is a renderer this server does not have.
     */
    private static void context(Wire wire, int opcode) throws Exception {
        int window = createWindow(wire);
        int visual = firstVisual(wire, opcode);
        int direct = wire.allocateId();
        createContext(wire, opcode, direct, visual, true);
        System.out.println("CreateContext(direct) on 0x" + Integer.toHexString(visual)
                + " -> " + (refused(wire) ? "refused" : "created"));
        isDirect(wire, opcode, direct);
        makeCurrent(wire, opcode, window, direct);
        System.out.println("MakeContextCurrent -> " + (refused(wire) ? "refused" : "accepted"));
        drawableAttributes(wire, opcode, window);
        queryContext(wire, opcode, direct);

        int indirect = wire.allocateId();
        createContext(wire, opcode, indirect, visual, false);
        System.out.println("CreateContext(indirect) -> " + (refused(wire) ? "refused" : "created"));
        isDirect(wire, opcode, indirect);
    }

    private static void createContext(Wire wire, int opcode, int context, int visual, boolean direct)
            throws Exception {
        byte[] request = new byte[24];
        header(request, opcode, CREATE_CONTEXT, 6);
        Wire.writeInt(request, 4, context);
        Wire.writeInt(request, 8, visual);
        Wire.writeInt(request, 12, 0);                  // screen
        Wire.writeInt(request, 16, 0);                  // share with nothing
        Wire.writeInt(request, 20, direct ? 1 : 0);
        wire.send(request);
    }

    private static void isDirect(Wire wire, int opcode, int context) throws Exception {
        byte[] request = new byte[8];
        header(request, opcode, IS_DIRECT, 2);
        Wire.writeInt(request, 4, context);
        wire.send(request);
        byte[] reply = replyTo(wire);
        System.out.println("  IsDirect -> " + (reply[8] & 0xFF));
    }

    /** {@code GLXMakeContextCurrent}: the old tag, then draw, read and the context - four words. */
    private static void makeCurrent(Wire wire, int opcode, int drawable, int context) throws Exception {
        byte[] request = new byte[20];
        header(request, opcode, MAKE_CONTEXT_CURRENT, 5);
        Wire.writeInt(request, 4, 0);                   // no previous tag
        Wire.writeInt(request, 8, drawable);            // draw
        Wire.writeInt(request, 12, drawable);           // read
        Wire.writeInt(request, 16, context);
        wire.send(request);
    }

    private static void drawableAttributes(Wire wire, int opcode, int drawable) throws Exception {
        byte[] request = new byte[8];
        header(request, opcode, GET_DRAWABLE_ATTRIBUTES, 2);
        Wire.writeInt(request, 4, drawable);
        wire.send(request);
        byte[] reply = replyTo(wire);
        int count = Wire.readInt(reply, 8);
        System.out.println("  GetDrawableAttributes -> " + count + " attribute(s)"
                + (count > 0 ? ", first = " + Wire.readInt(reply, 12) + ":" + Wire.readInt(reply, 16)
                        : ""));
    }

    private static void queryContext(Wire wire, int opcode, int context) throws Exception {
        byte[] request = new byte[8];
        header(request, opcode, QUERY_CONTEXT, 2);
        Wire.writeInt(request, 4, context);
        wire.send(request);
        byte[] reply = replyTo(wire);
        int count = Wire.readInt(reply, 8);
        System.out.println("  QueryContext -> " + count + " attribute(s)"
                + (count > 0 ? ", first = " + Wire.readInt(reply, 12) + ":" + Wire.readInt(reply, 16)
                        : ""));
    }

    /** A window to make current: GLX wants a drawable, and an unmapped one is enough to ask. */
    private static int createWindow(Wire wire) throws Exception {
        int window = wire.allocateId();
        byte[] request = new byte[32];
        request[0] = 1;                                 // CreateWindow
        Wire.writeShort(request, 2, 8);
        Wire.writeInt(request, 4, window);
        Wire.writeInt(request, 8, wire.root());
        Wire.writeShort(request, 16, 100);
        Wire.writeShort(request, 18, 100);
        Wire.writeShort(request, 22, 1);                // InputOutput
        Wire.writeInt(request, 28, 0);                  // no attributes
        wire.send(request);
        byte[] map = new byte[8];
        map[0] = 8;                                     // MapWindow
        Wire.writeShort(map, 2, 2);
        Wire.writeInt(map, 4, window);
        wire.send(map);
        return window;
    }

    /**
     * A visual to make a context on: the first FBConfig's {@code GLX_VISUAL_ID} (token 0x800b).
     *
     * <p>Asked for rather than assumed - a context needs a visual the *server* also offers, and the
     * first version of this probe read one out of a {@code GetWindowAttributes} reply whose opcode
     * field it had wrong, so it measured an error instead.
     */
    private static int firstVisual(Wire wire, int opcode) throws Exception {
        byte[] request = new byte[8];
        header(request, opcode, GET_FB_CONFIGS, 2);
        Wire.writeInt(request, 4, 0);
        wire.send(request);
        byte[] reply = replyTo(wire);
        int count = Wire.readInt(reply, 8);
        int pairs = Wire.readInt(reply, 12);
        for (int i = 0; i < pairs; i++) {
            int token = Wire.readInt(reply, 32 + i * 8);
            int value = Wire.readInt(reply, 36 + i * 8);
            if (token == 0x800b) {                      // GLX_VISUAL_ID
                return value;
            }
        }
        throw new IOException("no fbconfig names a visual (" + count + " configs)");
    }

    private static int queryExtension(Wire wire, String name) throws Exception {
        byte[] text = name.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        byte[] request = new byte[8 + ((text.length + 3) & ~3)];
        request[0] = 98;                                // QueryExtension
        Wire.writeShort(request, 2, request.length / 4);
        Wire.writeShort(request, 4, text.length);
        System.arraycopy(text, 0, request, 8, text.length);
        wire.send(request);
        byte[] reply = replyTo(wire);
        return (reply[8] & 0xFF) == 0 ? 0 : (reply[9] & 0xFF);
    }

    /**
     * Reads until a reply or an error turns up, and says which.
     *
     * <p>An error is an answer here - "GLXCreateContext refused" is the measurement - so it is
     * printed and reported rather than thrown, and the zeroes stand in for the reply that is not
     * coming. Anything that is neither (an event) is skipped and counted, because a probe that
     * silently walks past events measures the wrong thing.
     */
    private static byte[] replyTo(Wire wire) throws Exception {
        int skipped = 0;
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                throw new IOException("no answer");
            }
            if ((message[0] & 0xFF) == 1 && Wire.hasExtraData(message)) {
                // **A reply with extra data carries it after the thirty two byte header** - the
                // strings of the two *String replies and the configs of the two config replies. A
                // reader that stops at the header leaves the rest in the stream, and everything
                // after it is read as a message: measured here as a flood of "events" whose type
                // bytes were the extension list itself.
                message = wire.withExtraData(message);
            }
            switch (message[0] & 0x7F) {
                case 1 -> {
                    if (skipped > 0) {
                        System.out.println("  (" + skipped + " event(s) passed while waiting)");
                    }
                    return message;
                }
                case 0 -> {
                    System.out.println("  error " + (message[1] & 0xFF) + " value 0x"
                            + Integer.toHexString(Wire.readInt(message, 4)) + " major "
                            + (message[10] & 0xFF) + " minor " + Wire.readShort(message, 8));
                    return new byte[32];
                }
                default -> skipped++;
            }
        }
    }

    /**
     * Waits for a request with no reply to have been processed, and says whether it was refused.
     *
     * <p>The round trip is a core request that does have one, and the error a refused GLX request
     * produces arrives **before** that reply - so everything read on the way is the answer: an error
     * is reported as one, and anything else is counted. Reading one message and hoping (which this
     * probe did first) leaves the stream one answer out of step and every measurement after it
     * wrong - the kit's own rule, and it cost an hour here too.
     */
    private static boolean refused(Wire wire) throws Exception {
        byte[] focus = new byte[4];
        focus[0] = 43;                                  // GetInputFocus
        Wire.writeShort(focus, 2, 1);
        wire.send(focus);
        boolean refused = false;
        int skipped = 0;
        while (true) {
            byte[] message = wire.readMessage();
            if (message == null) {
                break;
            }
            if ((message[0] & 0x7F) == 0) {
                System.out.println("  refused: error " + (message[1] & 0xFF) + " value 0x"
                        + Integer.toHexString(Wire.readInt(message, 4)) + " major "
                        + (message[10] & 0xFF) + " minor " + Wire.readShort(message, 8));
                refused = true;
                continue;
            }
            if ((message[0] & 0x7F) == 1) {
                break;                                  // the focus reply: everything before it is seen
            }
            skipped++;
        }
        if (skipped > 0) {
            System.out.println("  (" + skipped + " event(s) passed)");
        }
        return refused;
    }

    private static void header(byte[] request, int opcode, int minor, int units) {
        request[0] = (byte) opcode;
        request[1] = (byte) minor;
        Wire.writeShort(request, 2, units);
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
