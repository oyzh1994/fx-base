package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.X11Input;
import com.github.moaxcp.x11.protocol.XGenericEvent;
import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.extension.Extension;
import cn.oyzh.fx.xserver.resource.XResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GLX: enough of it for a software OpenGL client to run, which is all of it a server without a GPU
 * needs.
 *
 * <p><b>What a GL client actually asks for, measured 2026-09-27 by tapping the wire
 * ({@code tools/tap.py}) while {@code glxgears} ran against Xvfb</b>: it sends **no GL commands at
 * all**. It renders in its own process - Mesa's {@code drisw} path, software - and pushes each
 * finished frame as an ordinary {@code PutImage}: 769 MB of them in 5.6 seconds, with one
 * {@code GetGeometry} per frame as the synchronisation. The reference has no DRI3, DRI2 or Present
 * either (the client asks for all three and gets nothing), which is exactly why it renders itself.
 * So this extension is a **handshake**, not a renderer: a client that finds GLX and is granted a
 * direct context never sends a GL request.
 *
 * <p><b>The one decision the design rests on is the direct context.</b> {@code CreateContext} carries
 * a {@code direct} flag, and a client told "yes" renders locally and blits; a client told "no" sends
 * its GL commands over the wire instead, and this server has no renderer to receive them. So a direct
 * context is granted and an **indirect one is refused** - a client that wants the wire path is better
 * off failing at the context than at every draw call.
 *
 * <p><b>What is here</b>: the version, the two config lists, the two server strings, the client's own
 * announcement, context create/destroy/make-current, whether a context is direct, the context query
 * and a drawable's attributes. <b>What is not</b>: everything that would need a renderer or a GPU -
 * pbuffers, GLX pixmaps and windows, and {@code CreateContextAttribsARB}, which is the ES2/shaders
 * path. The extension string is therefore **empty**, which is the honest advertisement: a client is
 * told the core version and no extensions, and a client that needs an extension - a toolkit's ES2
 * pipeline included - finds it missing and takes its own fallback rather than being handed a context
 * that cannot draw.
 *
 * <p><b>The layouts here are measured, not guessed</b>, and two of them cost an hour each to get
 * right (they are recorded in {@code docs/backlog.md} B6):
 *
 * <ul>
 *   <li>the opcodes are Mesa's, from {@code glxproto.h}: 3 CreateContext, 5 MakeCurrent, 6 IsDirect,
 *       7 QueryVersion, 14 GetVisualConfigs, 18 QueryExtensionsString, 19 QueryServerString,
 *       20 ClientInfo, 21 GetFBConfigs, 24 CreateNewContext, 25 QueryContext, 26 MakeContextCurrent,
 *       29 GetDrawableAttributes, 33/35 SetClientInfo[2]ARB, 34 CreateContextAttribsARB;</li>
 *   <li>**the two string requests carry no string**: {@code QueryServerString} is screen and name and
 *       nothing else, and the reply carries the length at offset 12 with the text after the thirty two
 *       byte header;</li>
 *   <li>{@code GetVisualConfigs} answers **40 ints per visual** - eighteeen fields and then eleven
 *       {@code (token, value)} pairs - where the GLX 1.2 specification's struct has eighteen and the
 *       header's {@code __GLXvisualConfig} has thirty. The layout was pinned by running
 *       {@code tools/GlxProbe} against Xvfb and correlating its first config with {@code glxinfo}'s
 *       own table for the same visual id: {@code 33, 4, 1, 8, 8, 8, 8, 0, 0, 0, 0, 1, 0, 32, 24, 8,
 *       0, 0} is visual {@code 0x21}'s {@code vid, class, rgba, red, green, blue, alpha, accum r/g/b/a,
 *       doubleBuffer, stereo, bufferSize, depthSize, stencilSize, auxBuffers, level}, and the tail
 *       begins {@code GLX_VISUAL_CAVEAT_EXT, GLX_NONE_EXT};</li>
 *   <li>{@code GetFBConfigs} answers {@code (token, value)} pairs and is **self-describing**, so it
 *       may carry any subset - the reference's first config is 44 pairs and this one's are the ones
 *       that matter.</li>
 * </ul>
 *
 * <p>Deliberately answering rather than refusing: an extension that replies to {@code QueryVersion}
 * and then drops the requests that follow is what leaves a client waiting for ever (the lesson
 * {@code XInputExtension} records), so every request here is answered or refused explicitly.
 */
public final class GlxExtension implements Extension {

    // The GLX opcodes, from Mesa's glxproto.h - the authority, and the reason the first version of
    // the probe was one out from 18 downwards.
    private static final int CREATE_CONTEXT = 3;
    private static final int DESTROY_CONTEXT = 4;
    private static final int MAKE_CURRENT = 5;
    private static final int IS_DIRECT = 6;
    private static final int QUERY_VERSION = 7;
    private static final int SWAP_BUFFERS = 11;
    private static final int GET_VISUAL_CONFIGS = 14;
    private static final int QUERY_EXTENSIONS_STRING = 18;
    private static final int QUERY_SERVER_STRING = 19;
    private static final int CLIENT_INFO = 20;
    private static final int GET_FB_CONFIGS = 21;
    private static final int CREATE_NEW_CONTEXT = 24;
    private static final int QUERY_CONTEXT = 25;
    private static final int MAKE_CONTEXT_CURRENT = 26;
    private static final int GET_DRAWABLE_ATTRIBUTES = 29;
    private static final int SET_CLIENT_INFO_ARB = 33;
    private static final int CREATE_CONTEXT_ATTRIBS_ARB = 34;
    private static final int SET_CLIENT_INFO2_ARB = 35;

    // The GLX tokens, from glxtokens.h.
    private static final int GLX_BUFFER_SIZE = 2;
    private static final int GLX_LEVEL = 3;
    private static final int GLX_RGBA = 4;
    private static final int GLX_DOUBLEBUFFER = 5;
    private static final int GLX_STEREO = 6;
    private static final int GLX_AUX_BUFFERS = 7;
    private static final int GLX_RED_SIZE = 8;
    private static final int GLX_GREEN_SIZE = 9;
    private static final int GLX_BLUE_SIZE = 10;
    private static final int GLX_ALPHA_SIZE = 11;
    private static final int GLX_DEPTH_SIZE = 12;
    private static final int GLX_STENCIL_SIZE = 13;
    private static final int GLX_ACCUM_RED_SIZE = 14;
    private static final int GLX_ACCUM_GREEN_SIZE = 15;
    private static final int GLX_ACCUM_BLUE_SIZE = 16;
    private static final int GLX_ACCUM_ALPHA_SIZE = 17;
    private static final int GLX_CONFIG_CAVEAT = 0x20;
    private static final int GLX_X_VISUAL_TYPE = 0x22;
    private static final int GLX_TRANSPARENT_TYPE = 0x23;
    private static final int GLX_TRANSPARENT_INDEX_VALUE = 0x24;
    private static final int GLX_TRANSPARENT_RED_VALUE = 0x25;
    private static final int GLX_TRANSPARENT_GREEN_VALUE = 0x26;
    private static final int GLX_TRANSPARENT_BLUE_VALUE = 0x27;
    private static final int GLX_TRANSPARENT_ALPHA_VALUE = 0x28;
    private static final int GLX_VISUAL_ID = 0x800b;
    private static final int GLX_DRAWABLE_TYPE = 0x8010;
    private static final int GLX_RENDER_TYPE = 0x8011;
    private static final int GLX_X_RENDERABLE = 0x8012;
    private static final int GLX_FBCONFIG_ID = 0x8013;
    private static final int GLX_NONE_EXT = 0x8000;
    private static final int GLX_TRUE_COLOR = 0x8002;
    private static final int GLX_WINDOW_BIT = 0x0001;
    private static final int GLX_PBUFFER_BIT = 0x0004;
    private static final int GLX_RGBA_BIT = 0x0001;
    private static final int GLX_SAMPLE_BUFFERS_ARB = 100001;
    private static final int GLX_SAMPLES_ARB = 100000;

    /** The names in a {@code QueryServerString} request. */
    private static final int STRING_VENDOR = 1;
    private static final int STRING_VERSION = 2;
    private static final int STRING_EXTENSIONS = 3;

    /** The version this server implements. 1.4 is the modern client's floor; see the class comment. */
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 4;

    private final GlxPlugin plugin = new GlxPlugin();
    private XServer server;

    @Override
    public String name() {
        return "GLX";
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
    }

    @Override
    public XRequest decodeRequest(int minorOpcode, X11Input in) throws IOException {
        // Hand written, every body read whole: the generated classes do not cover GLX (the protocol
        // library has no GLX module), and a request whose body is left unread desynchronises the
        // connection for every request after it.
        // **The header is skipped first**: the input a decoder is handed starts at the request's
        // minor byte, and reading the body straight away shifts every field by three bytes - which
        // is invisible in a request whose body the server ignores (a screen number) and wrong in
        // every one where it does not (a string's name, a context's id, whether it is direct).
        // `GlxProbe` caught it on the first run: QueryServerString answered BadValue for the name.
        in.readCard8();
        in.readCard16();
        return switch (minorOpcode) {
            case QUERY_VERSION -> new TwoInts(minorOpcode, in.readInt32(), in.readInt32());
            case GET_VISUAL_CONFIGS, GET_FB_CONFIGS, QUERY_EXTENSIONS_STRING ->
                    new OneInt(minorOpcode, in.readInt32());
            case QUERY_SERVER_STRING -> new TwoInts(minorOpcode, in.readInt32(), in.readInt32());
            case IS_DIRECT, DESTROY_CONTEXT, QUERY_CONTEXT, GET_DRAWABLE_ATTRIBUTES ->
                    new OneInt(minorOpcode, in.readInt32());
            case CLIENT_INFO, SET_CLIENT_INFO_ARB, SET_CLIENT_INFO2_ARB ->
                    new Skipped(minorOpcode);
            case CREATE_CONTEXT -> new CreateContext(in.readInt32(), in.readInt32(), in.readInt32(),
                    in.readInt32(), in.readInt32());
            case CREATE_NEW_CONTEXT -> new CreateNewContext(in.readInt32(), in.readInt32(),
                    in.readInt32(), in.readInt32(), in.readInt32(), in.readInt32());
            case MAKE_CURRENT -> new MakeCurrent(in.readInt32(), in.readInt32(), in.readInt32());
            case MAKE_CONTEXT_CURRENT -> new MakeContextCurrent(in.readInt32(), in.readInt32(),
                    in.readInt32(), in.readInt32());
            case SWAP_BUFFERS -> new SwapBuffers(in.readInt32(), in.readInt32());
            case CREATE_CONTEXT_ATTRIBS_ARB -> new Skipped(minorOpcode);
            default -> null;
        };
    }

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        switch (request) {
            case TwoInts r -> twoInts(client, r);
            case OneInt r -> oneInt(client, r);
            case CreateContext r -> createContext(client, r.context(), r.visual(), r.direct());
            case CreateNewContext r -> createContext(client, r.context(), visualOfConfig(r.config()),
                    r.direct());
            case MakeCurrent r -> makeCurrent(client, r.context());
            case MakeContextCurrent r -> makeCurrent(client, r.context());
            case SwapBuffers ignored -> {
                // Nothing to swap: the client renders itself and blits with PutImage, which is the
                // whole of what it needs from this server (the class comment has the measurement).
            }
            case Skipped r -> {
                // ClientInfo and its ARB successors are the client saying which version it is. There
                // is nothing here that depends on the answer, and refusing them would be an error a
                // client does not expect - so they are accepted and dropped.
            }
            default -> throw ProtocolException.unimplemented("GLX request "
                    + request.getClass().getSimpleName());
        }
    }

    private void twoInts(ClientConnection client, TwoInts request) {
        switch (request.minor()) {
            case QUERY_VERSION -> queryVersion(client, request.a(), request.b());
            case QUERY_SERVER_STRING -> serverString(client, request.a(), request.b());
            default -> throw ProtocolException.unimplemented("GLX request " + request.minor());
        }
    }

    private void oneInt(ClientConnection client, OneInt request) {
        switch (request.minor()) {
            case GET_VISUAL_CONFIGS -> visualConfigs(client);
            case GET_FB_CONFIGS -> fbConfigs(client);
            case QUERY_EXTENSIONS_STRING -> stringReply(client, extensions(), 0);
            case IS_DIRECT -> isDirect(client, request.value());
            case QUERY_CONTEXT -> queryContext(client, request.value());
            case GET_DRAWABLE_ATTRIBUTES -> drawableAttributes(client, request.value());
            case DESTROY_CONTEXT -> destroyContext(client, request.value());
            default -> throw ProtocolException.unimplemented("GLX request " + request.minor());
        }
    }

    // ------------------------------------------------------------------
    // The answers
    // ------------------------------------------------------------------

    /** {@code GLXQueryVersion}: the version, answered as the client's floor or this server's. */
    private void queryVersion(ClientConnection client, int clientMajor, int clientMinor) {
        byte[] reply = header(client, QUERY_VERSION, 0);
        putCard32(reply, 8, MAJOR_VERSION, client.isBigEndian());
        putCard32(reply, 12, MINOR_VERSION, client.isBigEndian());
        send(client, reply);
    }

    /**
     * {@code GLXQueryServerString} and {@code GLXQueryExtensionsString}: the vendor, version or the
     * extension list.
     *
     * <p>The **extension list is empty**, and that is the advertisement this server can honestly
     * make: it implements the core enough for a software client and nothing that needs a renderer, so
     * a client that wants an extension (a toolkit's ES2 pipeline, say) finds it missing and takes its
     * own fallback rather than being handed a context that cannot draw.
     */
    private void serverString(ClientConnection client, int screen, int name) {
        switch (name) {
            case STRING_VENDOR -> stringReply(client, "cn/oyzh/fx/xserver", 0);
            case STRING_VERSION -> stringReply(client, MAJOR_VERSION + "." + MINOR_VERSION, 0);
            case STRING_EXTENSIONS -> stringReply(client, extensions(), 0);
            default -> throw ProtocolException.badValue(name);
        }
    }

    private static String extensions() {
        return "";
    }

    /**
     * A {@code *String} reply: the length at 12, the padded text after the fixed part.
     *
     * <p>**The length is one more than the text**, because the NUL is part of it: a reference
     * answers 4 for {@code "1.4"} and 604 for a 603 character extension list, and a client reads
     * exactly that many bytes into a string it has already allocated. Measured by diffing the two
     * replies byte for byte after {@code glxinfo} printed this server's version string as
     * {@code 1.4} followed by two characters of whatever came next in memory - the one byte short
     * was the terminator's.
     */
    private void stringReply(ClientConnection client, String text, int minor) {
        byte[] bytes = text.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        byte[] reply = new byte[32 + ((bytes.length + 1 + 3) & ~3)];
        reply[0] = 1;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(reply, 4, (reply.length - 32) / 4, client.isBigEndian());
        putCard32(reply, 12, bytes.length + 1, client.isBigEndian());
        System.arraycopy(bytes, 0, reply, 32, bytes.length);
        send(client, reply);
    }

    /**
     * {@code GLXGetVisualConfigs}: one entry per visual, **forty ints each** - the layout the
     * reference was measured using, and the one field of it that matters is that the first eighteen
     * are the visual you would get from the core protocol with GL properties attached.
     */
    private void visualConfigs(ClientConnection client) {
        List<VisualInfo> visuals = glxVisuals(client);
        int props = 18 + 22;
        byte[] reply = new byte[32 + visuals.size() * props * 4];
        reply[0] = 1;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(reply, 4, (reply.length - 32) / 4, client.isBigEndian());
        putCard32(reply, 8, visuals.size(), client.isBigEndian());
        putCard32(reply, 12, props, client.isBigEndian());
        int at = 32;
        for (VisualInfo visual : visuals) {
            int[] fields = {
                    visual.id(),
                    visual.visualClass(),                   // 4 is TrueColor, as glxinfo prints "tc"
                    visual.isTrueColor() ? 1 : 0,           // rgba
                    visual.bitsPerRgbValue(), visual.bitsPerRgbValue(),
                    visual.bitsPerRgbValue(), visual.hasAlpha() ? 8 : 0,
                    0, 0, 0, 0,                             // accum red, green, blue, alpha
                    1,                                      // doubleBuffer - what a GL client asks for
                    0,                                      // stereo
                    visual.depth(),                         // bufferSize
                    24,                                     // depthSize
                    8,                                      // stencilSize
                    0,                                      // auxBuffers
                    0,                                      // level
            };
            for (int value : fields) {
                putCard32(reply, at, value, client.isBigEndian());
                at += 4;
            }
            // The extended properties are (token, value) pairs, and the tail of the measured layout
            // begins with the visual caveat and the transparent type - both "none" for a plain
            // visual. GLX_DONT_CARE is -1, which is what the reference sends for the transparent
            // colour values of a visual that has none.
            int[] pairs = {
                    GLX_CONFIG_CAVEAT, GLX_NONE_EXT,
                    GLX_TRANSPARENT_TYPE, GLX_NONE_EXT,
                    GLX_TRANSPARENT_RED_VALUE, -1,
                    GLX_TRANSPARENT_GREEN_VALUE, -1,
                    GLX_TRANSPARENT_BLUE_VALUE, -1,
                    GLX_TRANSPARENT_ALPHA_VALUE, -1,
                    GLX_TRANSPARENT_INDEX_VALUE, 0,
                    GLX_SAMPLE_BUFFERS_ARB, 0,
                    GLX_SAMPLES_ARB, 0,
                    0, 0,
                    0, 0,
            };
            for (int value : pairs) {
                putCard32(reply, at, value, client.isBigEndian());
                at += 4;
            }
        }
        send(client, reply);
    }

    /**
     * {@code GLXGetFBConfigs}: the same configs as {@code (token, value)} pairs, which is what makes
     * them self-describing - a client reads tokens, so a server may send the ones it has.
     */
    private void fbConfigs(ClientConnection client) {
        List<VisualInfo> visuals = glxVisuals(client);
        List<int[]> configs = new ArrayList<>();
        for (VisualInfo visual : visuals) {
            configs.add(fbConfig(visual));
        }
        int pairs = configs.isEmpty() ? 0 : configs.get(0).length / 2;
        byte[] reply = new byte[32 + configs.size() * pairs * 8];
        reply[0] = 1;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(reply, 4, (reply.length - 32) / 4, client.isBigEndian());
        putCard32(reply, 8, configs.size(), client.isBigEndian());
        putCard32(reply, 12, pairs, client.isBigEndian());
        int at = 32;
        for (int[] config : configs) {
            for (int value : config) {
                putCard32(reply, at, value, client.isBigEndian());
                at += 4;
            }
        }
        send(client, reply);
    }

    /** One fbconfig: the tokens a client chooses a visual by, and the drawable types it may be used on. */
    private int[] fbConfig(VisualInfo visual) {
        int alpha = visual.hasAlpha() ? 8 : 0;
        return new int[] {
                GLX_VISUAL_ID, visual.id(),
                GLX_FBCONFIG_ID, visual.id(),
                GLX_DRAWABLE_TYPE, GLX_WINDOW_BIT | GLX_PBUFFER_BIT,
                GLX_RENDER_TYPE, GLX_RGBA_BIT,
                GLX_X_RENDERABLE, 1,
                GLX_X_VISUAL_TYPE, GLX_TRUE_COLOR,
                GLX_BUFFER_SIZE, visual.depth(),
                GLX_LEVEL, 0,
                GLX_DOUBLEBUFFER, 1,
                GLX_STEREO, 0,
                GLX_AUX_BUFFERS, 0,
                GLX_RED_SIZE, visual.bitsPerRgbValue(),
                GLX_GREEN_SIZE, visual.bitsPerRgbValue(),
                GLX_BLUE_SIZE, visual.bitsPerRgbValue(),
                GLX_ALPHA_SIZE, alpha,
                GLX_DEPTH_SIZE, 24,
                GLX_STENCIL_SIZE, 8,
                GLX_ACCUM_RED_SIZE, 0,
                GLX_ACCUM_GREEN_SIZE, 0,
                GLX_ACCUM_BLUE_SIZE, 0,
                GLX_ACCUM_ALPHA_SIZE, 0,
                GLX_CONFIG_CAVEAT, GLX_NONE_EXT,
                GLX_TRANSPARENT_TYPE, GLX_NONE_EXT,
                GLX_TRANSPARENT_RED_VALUE, -1,
                GLX_TRANSPARENT_GREEN_VALUE, -1,
                GLX_TRANSPARENT_BLUE_VALUE, -1,
                GLX_TRANSPARENT_ALPHA_VALUE, -1,
                GLX_TRANSPARENT_INDEX_VALUE, 0,
                GLX_SAMPLE_BUFFERS_ARB, 0,
                GLX_SAMPLES_ARB, 0,
        };
    }

    /**
     * The visuals a GL client may choose from: the true-colour ones, which is all this server has.
     *
     * <p>An 8 bit PseudoColor visual is not something a software GL client can draw into here, and
     * offering it would put a config in the list that a client could pick and then find unusable.
     */
    private List<VisualInfo> glxVisuals(ClientConnection client) {
        Screen screen = server.screenOf(client);
        List<VisualInfo> usable = new ArrayList<>();
        for (VisualInfo visual : screen.visuals()) {
            if (visual.isTrueColor()) {
                usable.add(visual);
            }
        }
        return usable;
    }

    /** The visual behind an fbconfig id, which a {@code CreateNewContext} names instead of a visual. */
    private int visualOfConfig(int config) {
        return config;
    }

    /**
     * {@code GLXCreateContext}: a direct context is granted, an indirect one is refused.
     *
     * <p>This is the decision the whole extension rests on (the class comment has the measurement):
     * a client that is told "direct" renders in its own process and blits its frames here, and one
     * told "indirect" would send GL commands this server cannot execute. Refusing at the context is
     * the failure a client can act on.
     */
    private void createContext(ClientConnection client, int context, int visual, boolean direct) {
        if (!direct) {
            throw ProtocolException.unimplemented("GLX indirect rendering - a client that renders"
                    + " itself is the only kind this server can show");
        }
        if (visual == 0) {
            throw ProtocolException.badValue(visual);
        }
        Context resource = new Context(context, client);
        resource.direct = true;
        server.resources().add(resource);
    }

    private void destroyContext(ClientConnection client, int context) {
        if (client.ownsIdRange(context) && server.resources().contains(context)) {
            server.resources().free(context);
        }
    }

    private void makeCurrent(ClientConnection client, int context) {
        // The current context is only read back by QueryContext, which answers the same thing for
        // every context this server can hand out - so nothing has to be remembered beyond the fact
        // that the request was answered.
        contextOf(client, context);
    }

    private void isDirect(ClientConnection client, int context) {
        Context resource = contextOf(client, context);
        byte[] reply = header(client, IS_DIRECT, 0);
        reply[8] = (byte) (resource.direct ? 1 : 0);
        send(client, reply);
    }

    private void queryContext(ClientConnection client, int context) {
        contextOf(client, context);
        byte[] reply = header(client, QUERY_CONTEXT, 0);
        putCard32(reply, 8, 0, client.isBigEndian());        // no attributes to report
        send(client, reply);
    }

    /**
     * {@code GLXGetDrawableAttributes}: the config a drawable was created with.
     *
     * <p>A client asks this to learn which fbconfig its window is on - it is how a context and a
     * drawable are known to agree. The answer is the window's own visual, named both ways a client
     * can ask (the visual id and the config id are the same number here).
     */
    private void drawableAttributes(ClientConnection client, int drawable) {
        XResource resource = server.resources().get(drawable);
        int visual = resource instanceof cn.oyzh.fx.xserver.resource.XWindow window
                && window.visual() != null ? window.visual().id() : 0;
        int[] attributes = {
                GLX_DRAWABLE_TYPE, GLX_WINDOW_BIT,
                GLX_RENDER_TYPE, GLX_RGBA_BIT,
                GLX_X_RENDERABLE, 1,
                GLX_FBCONFIG_ID, visual,
                GLX_VISUAL_ID, visual,
        };
        byte[] reply = header(client, GET_DRAWABLE_ATTRIBUTES, 0);
        putCard32(reply, 8, attributes.length / 2, client.isBigEndian());
        byte[] full = new byte[32 + attributes.length * 4];
        System.arraycopy(reply, 0, full, 0, 32);
        putCard32(full, 4, attributes.length, client.isBigEndian());
        for (int i = 0; i < attributes.length; i++) {
            putCard32(full, 32 + i * 4, attributes[i], client.isBigEndian());
        }
        send(client, full);
    }

    private Context contextOf(ClientConnection client, int id) {
        XResource resource = server.resources().get(id);
        if (resource instanceof Context context && context.owner() == client) {
            return context;
        }
        throw ProtocolException.badValue(id);
    }

    // ------------------------------------------------------------------
    // Replies and the wire
    // ------------------------------------------------------------------

    /** The fixed part of a GLX reply: the type, the sequence number and a zero length. */
    private static byte[] header(ClientConnection client, int minor, int extraUnits) {
        byte[] reply = new byte[32];
        reply[0] = 1;
        putCard16(reply, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(reply, 4, extraUnits, client.isBigEndian());
        return reply;
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

    // ------------------------------------------------------------------
    // Wire shapes
    // ------------------------------------------------------------------

    /** A GLX context: the one thing about it this server has to remember is whether it is direct. */
    private static final class Context extends XResource {
        private static final int RESOURCE_CLASS = XResource.newExtensionClass();
        private boolean direct;

        Context(int id, ClientConnection owner) {
            super(id, RESOURCE_CLASS, owner);
        }

        @Override
        public String typeName() {
            return "glx context";
        }
    }

    /** A request whose body is one whole word. */
    private record OneInt(int minor, int value) implements XRequest {
        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public String getPluginName() {
            return "glx";
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }
    }

    /** A request whose body is two whole words. */
    private record TwoInts(int minor, int a, int b) implements XRequest {
        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public String getPluginName() {
            return "glx";
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }
    }

    private record CreateContext(int context, int visual, int screen, int share, int directFlag)
            implements XRequest {
        boolean direct() {
            return directFlag != 0;
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public String getPluginName() {
            return "glx";
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }
    }

    private record CreateNewContext(int context, int config, int screen, int renderType, int share,
                                    int directFlag) implements XRequest {
        boolean direct() {
            return directFlag != 0;
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public String getPluginName() {
            return "glx";
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }
    }

    private record MakeCurrent(int drawable, int context, int oldTag) implements XRequest {
        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public String getPluginName() {
            return "glx";
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }
    }

    private record MakeContextCurrent(int oldTag, int drawable, int readDrawable, int context)
            implements XRequest {
        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public String getPluginName() {
            return "glx";
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }
    }

    private record SwapBuffers(int tag, int drawable) implements XRequest {
        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public String getPluginName() {
            return "glx";
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }
    }

    /** A request whose body is deliberately not read: the connection has it buffered already. */
    private record Skipped(int minor) implements XRequest {
        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public String getPluginName() {
            return "glx";
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }
    }

    private static final class GlxPlugin implements XProtocolPlugin {
        private byte majorOpcode;
        private byte firstEvent;
        private byte firstError;

        @Override
        public String getPluginName() {
            return "glx";
        }

        @Override
        public java.util.Optional<String> getExtensionXName() {
            return java.util.Optional.of("GLX");
        }

        @Override
        public java.util.Optional<String> getExtensionName() {
            return java.util.Optional.of("GLX");
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
            throw new IOException("GLX request " + (minorOpcode & 0xFF)
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
