package cn.oyzh.fx.xserver;

import com.github.moaxcp.x11.protocol.X11Input;
import com.github.moaxcp.x11.protocol.xproto.ConfigureNotifyEvent;
import cn.oyzh.fx.xserver.HostWindowStore;
import cn.oyzh.fx.xserver.ScreenSession;
import cn.oyzh.fx.xserver.WindowIdentity;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ConnectionSetup;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.client.RequestDispatcher;
import cn.oyzh.fx.xserver.clipboard.ClipboardBridge;
import cn.oyzh.fx.xserver.clipboard.HostClipboard;
import cn.oyzh.fx.xserver.display.CursorSprite;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.event.EventDispatcher;
import cn.oyzh.fx.xserver.event.EventMasks;
import cn.oyzh.fx.xserver.event.RawEvent;
import cn.oyzh.fx.xserver.extension.Extension;
import cn.oyzh.fx.xserver.extension.ExtensionRegistry;
import cn.oyzh.fx.xserver.extension.XFixesExtension;
import cn.oyzh.fx.xserver.handler.AtomHandlers;
import cn.oyzh.fx.xserver.handler.DrawHandlers;
import cn.oyzh.fx.xserver.handler.InputHandlers;
import cn.oyzh.fx.xserver.handler.MiscHandlers;
import cn.oyzh.fx.xserver.handler.ResourceHandlers;
import cn.oyzh.fx.xserver.handler.WindowHandlers;
import cn.oyzh.fx.xserver.input.InputState;
import cn.oyzh.fx.xserver.net.ClientListener;
import cn.oyzh.fx.xserver.resource.AtomTable;
import cn.oyzh.fx.xserver.resource.ResourceTable;
import cn.oyzh.fx.xserver.resource.XColormap;
import cn.oyzh.fx.xserver.resource.XCursor;
import cn.oyzh.fx.xserver.resource.XFont;
import cn.oyzh.fx.xserver.resource.XProperty;
import cn.oyzh.fx.xserver.resource.XWindow;
import cn.oyzh.fx.xserver.tray.TrayBridge;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.extension.ProtocolRecorder;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The X server itself: screens, the resource tables, connected clients and the request loop.
 */
public final class XServer {


    /**
     * Client index 0 is reserved for the server, which owns the root window and colormaps.
     */
    public static final int SERVER_CLIENT_INDEX = 0;

    private final ServerOptions options;
    private final ResourceTable resources = new ResourceTable();
    private final AtomTable atoms = new AtomTable();
    private final List<ClientConnection> clients = new CopyOnWriteArrayList<>();
    /**
     * Every screen the server is showing, the master one included.
     */
    private final List<ScreenSession> sessions = new CopyOnWriteArrayList<>();
    private final BitSet usedClientIndices = new BitSet(ClientConnection.MAX_CLIENTS);
    private final Map<String, Selection> selections = new HashMap<>();
    private final List<InputHandlers.PassiveGrab> passiveGrabs =
            new ArrayList<>();

    /**
     * Held while a request executes; {@code GrabServer} holds it across requests.
     */
    private final ReentrantLock serverLock = new ReentrantLock(true);

    /**
     * The master screen: the one single-window mode uses, and the one in-process tests build on.
     */
    private final Screen screen;
    private final List<Screen> screens = new ArrayList<>();
    /**
     * The visuals every screen offers. Shared, because a client compares them by id.
     */
    private final List<VisualInfo> visuals = new ArrayList<>();
    private final VisualInfo rootVisual;
    private final int defaultFontId;

    private final ConnectionSetup connectionSetup = new ConnectionSetup(this);
    private final ExtensionRegistry extensions = new ExtensionRegistry();
    /** The host clipboard bridge, or null when there is no host to bridge to. */
    private ClipboardBridge clipboard;

    /** The notification area, or null when this server has no menu bar to put icons in (C9). */
    private TrayBridge tray;
    /** The host drag bridge, or null when this server has no window to drop on. */
    private cn.oyzh.fx.xserver.drop.DropBridge drops;
    /**
     * Where the host window was per client, or null until the first window opens. Lazy because a
     * server that never opens a window - every test, and a headless one - has nothing to remember
     * and no reason to touch the user's home directory.
     */
    private HostWindowStore windows = null;
    /** Set by {@link #setHostWindowStore}; the store actually used, once something needs one. */
    private HostWindowStore hostWindowStore;
    private final WindowHandlers windowHandlers = new WindowHandlers(this);
    private final DrawHandlers drawHandlers = new DrawHandlers(this);
    private final ResourceHandlers resourceHandlers = new ResourceHandlers(this);
    private final AtomHandlers atomHandlers = new AtomHandlers(this);
    private final InputHandlers inputHandlers = new InputHandlers(this);
    private final MiscHandlers miscHandlers = new MiscHandlers(this);
    private final RequestDispatcher dispatcher = new RequestDispatcher(this, windowHandlers,
            drawHandlers, resourceHandlers, atomHandlers, inputHandlers, miscHandlers);
    private final EventDispatcher events = new EventDispatcher(this);
    /**
     * Input for the master screen. Created in the constructor rather than here because it is bound to
     * a screen, and the screen does not exist yet while the fields are being initialised.
     */
    private final InputState input;

    /**
     * How a window is made for a screen. One per screen, because each window shows one screen: the
     * JavaFX toolkit itself is shared, but a stage is not.
     */
    private java.util.function.Supplier<DisplayBackend> backendFactory;

    /**
     * A backend chosen by an embedder or a test, used for the master screen.
     */
    private DisplayBackend sharedBackend;

    private final ScreenSession masterSession;
    private ClientListener listener;
    /** The loopback command port, or null when the server was started without one (backlog D1). */
    private cn.oyzh.fx.xserver.net.ControlListener control;

    private ClientConnection serverGrabHolder;
    /**
     * How many times the holder called GrabServer without a matching UngrabServer.
     */
    private int serverGrabDepth;

    private volatile boolean running;
    private long startTime;
    /**
     * Ids for resources the server creates itself. Starts at 1 because 0 is {@code None} in the
     * protocol and a resource named 0 could never be referenced.
     */
    private int resourceIdCounter = 1;

    public XServer(ServerOptions options) {
        this.options = options;
        // Also applied here, not only in main(), so a server constructed directly - as tests and
        // embedders do - still gets a screen that fits the display it will be shown on.
        options.fitToDisplay();
        this.usedClientIndices.set(SERVER_CLIENT_INDEX);
        // The depth a geometry can name is accepted for compatibility with the usual
        // `-screen 0 WxHxD` invocation and is the one field of it this server does not honour: the
        // root is always 24 bit TrueColor, whatever the geometry says (measured 2026-10-04: a
        // `--geometry 640x480x16` server answers `depth of root window: 24 planes`, and so does x8).
        // Said out loud rather than silently ignored, so a configuration that wanted a 16 bit root
        // finds out here instead of from a client that asked for one.
        if (options.depth() != ROOT_DEPTH) {
            JulLog.warn("--geometry asked for a {} bit root; this server's root is always {} bit"
                    + " TrueColor, and the depth in a geometry is accepted for compatibility only",
                    options.depth(), ROOT_DEPTH);
        }

        // A modern desktop is TrueColor first: 24 bit for the root, 32 bit ARGB for translucency,
        // plus the low depth visuals clients still probe for.
        int nextVisualId = 0x0200;
        VisualInfo trueColor24 = VisualInfo.trueColor24(nextVisualId++);
        visuals.add(trueColor24);
        visuals.add(VisualInfo.argb32(nextVisualId++));
        visuals.add(VisualInfo.trueColor16(nextVisualId++));
        visuals.add(VisualInfo.trueColor8(nextVisualId++));
        visuals.add(VisualInfo.pseudoColor8(nextVisualId++));
        visuals.add(VisualInfo.staticGray8(nextVisualId++));
        visuals.add(VisualInfo.staticGray1(nextVisualId++));
        this.rootVisual = trueColor24;

        this.screen = createScreen(0);

        XFont defaultFont = new XFont(resourceIdCounter++, null, "fixed");
        resources.add(defaultFont);
        this.defaultFontId = defaultFont.id();

        this.input = new InputState(this, screen);
        screenInputs.put(screen, input);
        this.masterSession = new ScreenSession(screen, input, null);
        sessions.add(masterSession);

        registerExtensions();
    }

    /**
     * Builds a screen with its own framebuffer, root window, default colormap and root cursor, and
     * registers it.
     *
     * <p>Screens share the visual table on purpose: a client compares visuals by id, and RENDER keys
     * its formats by visual id, so two screens handing out different {@link VisualInfo} objects for
     * the same depth would look like two different displays rather than two of the same.
     */
    private Screen createScreen(int number) {
        // The screen is the pixel size, not the size the window looks: with --scale 2 a client is
        // told about twice as many pixels and draws into them, and the host window shows that twice
        // as large a picture at the same size on screen.
        Screen created = new Screen(number, options.pixelWidth(), options.pixelHeight(),
                options.scale(), visuals, ROOT_DEPTH, rootVisual.id());
        // **What a client draws wakes the display loop there and then.** Without this the frame
        // waited for the end of the tick period - up to 16.7 ms at 60 - on top of the wait for the
        // toolkit to paint it, and that extra period is a large part of the difference between this
        // and the VNC pair the same clients are usually watched through, whose server paints the
        // framebuffer as soon as it changes. The period itself is still what a quiet screen is drawn
        // at; see eventLoop.
        created.framebuffer().setDamageListener(this::requestFrame);
        XColormap colormap = new XColormap(resourceIdCounter++, null, created, rootVisual, true);
        resources.add(colormap);
        XWindow root = new XWindow(resourceIdCounter++, null, created, null, ROOT_DEPTH,
                XWindow.INPUT_OUTPUT, rootVisual, 0, 0, options.pixelWidth(),
                options.pixelHeight(), 0);
        root.setMapped(true);
        root.setBackgroundPixel(rootVisual.fromArgb(Screen.DEFAULT_BACKGROUND_ARGB));
        root.setBorderPixel(rootVisual.fromArgb(Screen.DEFAULT_BACKGROUND_ARGB));
        root.setColormap(colormap);
        root.takeNeedsFullExpose();
        resources.add(root);
        created.setRoot(root);
        created.setDefaultColormap(colormap);
        created.setBlackPixel(rootVisual.fromArgb(0xFF000000));
        created.setWhitePixel(rootVisual.fromArgb(0xFFFFFFFF));
        publishXkbRulesNames(created);
        publishWindowManagerHints(created);
        if (options.scale() > 1) {
            publishDpi(created);
        }
        XCursor cursor = XCursor.defaultCursor(resourceIdCounter++, null);
        resources.add(cursor);
        created.setRootCursor(cursor);
        root.setCursor(cursor);
        screens.add(created);
        return created;
    }

    /**
     * The root window's depth. The same on every screen, and what the setup reply advertises.
     */
    private static final int ROOT_DEPTH = 24;

    /**
     * The display's idle state: the screen saver's settings and state, and DPMS's.
     *
     * <p>One per server rather than one per screen, because what would blank is the host window and
     * there is one of them - see {@link cn.oyzh.fx.xserver.display.ScreenSaverState}.
     */
    private final cn.oyzh.fx.xserver.display.ScreenSaverState screenSaver =
            new cn.oyzh.fx.xserver.display.ScreenSaverState();

    private cn.oyzh.fx.xserver.extension.ScreenSaverExtension screenSaverExtension;

    private cn.oyzh.fx.xserver.extension.RandRExtension randrExtension;

    /** SECURITY, which the connection setup needs: a token is checked there, not at a request. */
    private cn.oyzh.fx.xserver.extension.SecurityExtension securityExtension;

    /** The display's idle state, which the core screen-saver requests and MIT-SCREEN-SAVER share. */
    public cn.oyzh.fx.xserver.display.ScreenSaverState screenSaver() {
        return screenSaver;
    }

    /** The MIT-SCREEN-SAVER extension, which the core {@code ForceScreenSaver} notifies through. */
    public cn.oyzh.fx.xserver.extension.ScreenSaverExtension screenSaverExtension() {
        return screenSaverExtension;
    }

    /** The RandR extension, which the screen-growth path notifies through. */
    public cn.oyzh.fx.xserver.extension.RandRExtension randrExtension() {
        return randrExtension;
    }

    private cn.oyzh.fx.xserver.extension.XInputExtension xinputExtension;

    /**
     * The XInput2 extension, which every event delivery asks whether a client wants the device
     * version instead (B4). Null before registration, which is why the callers ask for it through
     * {@link #xinput()} rather than holding it.
     */
    public cn.oyzh.fx.xserver.extension.XInputExtension xinput() {
        return xinputExtension;
    }

    /**
     * Registers the extensions this server implements. Order matters: opcodes are handed out in
     * registration order and clients cache the assignment for the life of the connection.
     */
    private void registerExtensions() {
        // The counts are the number of event and error codes each extension defines; the registry
        // uses them to hand out non-overlapping ranges.
        extensions.register(new cn.oyzh.fx.xserver.extension.BigRequestsExtension(), 0, 0);
        extensions.register(new cn.oyzh.fx.xserver.extension.XCMiscExtension(), 0, 0);
        extensions.register(new cn.oyzh.fx.xserver.extension.ShapeExtension(), 1, 1);
        // DAMAGE: one event (DamageNotify) and one error (BadDamage).
        extensions.register(new cn.oyzh.fx.xserver.extension.DamageExtension(), 1, 1);
        extensions.register(new cn.oyzh.fx.xserver.extension.XTestExtension(), 0, 1);
        extensions.register(new XFixesExtension(), 2, 1);
        // One screen is Xinerama's whole answer here, and it is an answer a client needs rather than
        // an extension it uses: xdotool segfaults in its own startup without it.
        extensions.register(new cn.oyzh.fx.xserver.extension.XineramaExtension(), 0, 1);
        // XKEYBOARD defines one error (the keyboard error) and events this server never sends - but
        // **its event range is still reserved, and reserving it is not bookkeeping**: a client
        // library installs its wire-to-event converter over the whole range a server advertises
        // (`XESetWireToEvent`), and a base of zero would install it over the *core* events instead.
        // libX11's XKB support is inside Xlib itself and every toolkit that asks for a keymap goes
        // through it, so a base of zero is a loaded gun pointed at KeyPress. It is here for the same
        // reason Xinerama is: xdotool asks the keyboard for its keymap through XKB and dereferences
        // a null pointer when told there is no such extension - see the class comment, and A4. A
        // reference answers base 85 (measured 2026-10-03, `xdpyinfo -ext XKEYBOARD` on both
        // servers); the exact number is the server's to choose, and only "outside the core event
        // numbers" matters. **The twelve event codes XKB 1.0 defines**, so nothing hands them out.
        extensions.register(new cn.oyzh.fx.xserver.extension.XkbExtension(), 12, 1);
        // MIT-SCREEN-SAVER defines one event - the state-change notification - and no errors; DPMS
        // is requests and replies only. The screen saver extension is kept by name because the core
        // ForceScreenSaver is what raises its notification.
        this.screenSaverExtension =
                extensions.register(new cn.oyzh.fx.xserver.extension.ScreenSaverExtension(),
                        1, 0);
        extensions.register(new cn.oyzh.fx.xserver.extension.DpmsExtension(), 0, 0);
        // RandR defines two events (the screen change and the generic notify) and four errors
        // (BadCrtc, BadMode, BadOutput, BadProvider, in the order the plugin reads them back). It is
        // kept by name because the screen-growth path is what raises its notification.
        this.randrExtension = extensions.register(
                new cn.oyzh.fx.xserver.extension.RandRExtension(), 2, 4);
        // XInput2 defines one error (the device error) and no events of its own - what it sends are
        // generic events, which live at core event 35 and not in an extension's range - **and it
        // still has to reserve a range, which is the whole of A18**.
        //
        // <p>The range a server advertises is not a description of what it will send. It is where a
        // client library puts its wire-to-event converter: `XESetWireToEvent(dpy, first_event + i,
        // ...)` writes straight into Xlib's conversion table, one slot per event code, and it is
        // called the moment the library is first used on that connection. libXi installs its
        // **device** converters that way, and AWT's toolkit connection calls `XListInputDevices`
        // (minor opcode 2) as its second XInput request at startup - measured on the bed, in a
        // server log where the GDK connection beside it went straight to XI2's `XIQueryDevice`.
        // Answering `first_event = 0`, as this did, therefore put
        // libXi's `XDeviceButtonEvent` converter on top of core `ButtonPress`: Xlib then unpacked
        // every click with the device layout, which carries an extra `deviceid` word the core layout
        // spends on nothing, so every field from `root` on sat eight bytes away from where the
        // reader looked for it. AWT read the click's root **y** as the button number - "Nonexistent
        // button 18" for a click at y = 20, "16" at y = 18, which is A18's original report - and
        // ignored the click, since the button was neither 1 nor 2 nor 3.
        //
        // <p>**Measured 2026-10-03**, by dumping the struct AWT was handed (a
        // `sun.awt.X11.XEventDispatcher` under the frame's content window, reading the raw ninety six
        // bytes with a `MemorySegment`) beside the same click's bytes off a tap: `window` at 32 and
        // `serial` at 8 right, then root at **48** instead of 40, time at **64** instead of 56, x at
        // **72** instead of 64 - the device layout, field for field, on two different clicks. A
        // reference answers 66 here, and `xev` and a fresh Xlib connection (which never touched
        // libXi) read the same button press correctly, which is why the wire looked clean for
        // months. **Sixteen event codes**, XInput 1.x's own count, so nothing else can be handed
        // them. It is kept by name because the event delivery asks it, for every event, whether a
        // client wants the device version instead.
        //
        // <p><b>Thirteen error codes</b>, which is the second half of the same rule: an error the
        // extension did not declare is an error a client cannot name. This extension answers with
        // its own errors - a bad device (code 0) and, since A47, XI 2.3's barrier error (code 12,
        // measured against Xvfb: 141 of its base of 129) - and a client decodes code 12 by looking
        // it up in the range its own `QueryExtension` reply named. Registering one error made the
        // barrier answer land outside that range, which a client can only read as unknown (measured
        // 2026-10-04: ours answered 40 where a reference answers 141).
        this.xinputExtension = extensions.register(
                new cn.oyzh.fx.xserver.extension.XInputExtension(), 16, 13);
        // GLX answers the handshake a software OpenGL client needs, and nothing that would need a
        // renderer: a client granted a direct context renders itself and blits (B6). Off unless
        // `--glx` asks for it, because advertising it changes what JavaFX does with its own drawing
        // (see ServerOptions.glx - a menu stops highlighting and a caret stops blinking).
        if (options.glx()) {
            extensions.register(new cn.oyzh.fx.xserver.extension.GlxExtension(), 0, 0);
        }
        // RENDER defines no events and five errors: BadPictFormat, BadPicture, BadPictOp,
        // BadGlyphSet and BadGlyph, in the order the plugin reads them back.
        var render = new cn.oyzh.fx.xserver.extension.RenderExtension();
        extensions.register(render, render.eventCount(), render.errorCount());
        // Composite defines no events and no errors: it is requests and replies, and the redirect it
        // sets is what stops a window being composited onto the screen (B5's third part). Registered
        // after RENDER because what a compositor draws with is RENDER.
        extensions.register(new cn.oyzh.fx.xserver.extension.CompositeExtension(), 0, 0);
        // SYNC defines two events (CounterNotify and AlarmNotify, in the plugin's numbering) and
        // three errors (BadCounter, BadAlarm and BadFence). Counters, fences and alarms: how a
        // client asks whether something has happened, and how it asks to be told when it does.
        // Registered with Composite because a compositor paces itself with fences and reads windows
        // through Composite.
        extensions.register(new cn.oyzh.fx.xserver.extension.SyncExtension(), 2, 3);
        // Present defines no events of its own: its completion notifications are GenericEvents, which
        // live at core event 35 and not in an extension's range. One error, BadPresent.
        extensions.register(new cn.oyzh.fx.xserver.extension.PresentExtension(), 0, 1);
        // The Generic Event Extension owns no requests but a version handshake, and no events or
        // errors of its own - its whole job is to tell a client that the generic events this server
        // already sends are findable the way the protocol intends (F4). Registered last so no
        // existing extension's opcode moves.
        extensions.register(new cn.oyzh.fx.xserver.extension.XgeExtension(), 0, 0);
        // X-Resource is requests and replies only, so it too owns no event or error codes: it
        // answers who is using the memory, which E4's lazy allocation left with nothing to read
        // (F3). Registered last for the same reason as XGE.
        extensions.register(new cn.oyzh.fx.xserver.extension.ResExtension(), 0, 0);
        // RECORD is how a client is told what another client did (F2). It defines one error
        // (BadContext) and no events, so its event range is empty and QueryExtension reports zero for
        // it. Registered last so no existing extension's opcode moves.
        extensions.register(new cn.oyzh.fx.xserver.extension.RecordExtension(), 0, 1);
        // DOUBLE-BUFFER defines no events and one error, BadBuffer, which is F5's extension.
        extensions.register(new cn.oyzh.fx.xserver.extension.DbeExtension(), 0, 1);
        // SECURITY defines one event, AuthorizationRevoked, and no errors of its own (F7): what an
        // untrusted client is refused is a core BadAccess, which is what a reference answers.
        this.securityExtension = extensions.register(
                new cn.oyzh.fx.xserver.extension.SecurityExtension(), 1, 0);
        // XVideo defines two events (VideoNotify, PortNotify) and three errors (BadPort,
        // BadEncoding, BadControl) and advertises no adaptors, which is F6.
        extensions.register(new cn.oyzh.fx.xserver.extension.XVideoExtension(), 2, 3);
        for (Extension extension : extensions.all()) {
            extension.init(this);
        }
    }

    // ------------------------------------------------------------------
    // Recording: what lets RECORD see every message (F2)
    // ------------------------------------------------------------------

    /**
     * Who is told about every protocol message, or {@link ProtocolRecorder#NONE}.
     *
     * <p>The calls are on the path of every request and every event, so the field is read once per
     * message and the default implementation does nothing at all.
     */
    private volatile cn.oyzh.fx.xserver.extension.ProtocolRecorder recorder =
            cn.oyzh.fx.xserver.extension.ProtocolRecorder.NONE;

    /** The client whose request is being executed right now, and its sequence number. */
    private volatile ClientConnection currentRequestClient;
    private volatile short currentRequestSequence;

    /** Sets the recorder. Null puts the no-op one back. */
    public void recorder(cn.oyzh.fx.xserver.extension.ProtocolRecorder recorder) {
        this.recorder = recorder == null
                ? cn.oyzh.fx.xserver.extension.ProtocolRecorder.NONE : recorder;
    }

    /** One request as it arrived, for the recorder. Called from the client's request loop. */
    public void recordClientRequest(ClientConnection client, byte[] request, short sequenceNumber) {
        recorder.clientRequest(client, request, sequenceNumber);
    }

    /**
     * One message on its way out.
     *
     * <p>The attribution - which client and which sequence number the recorder is told this belongs
     * to - is the request being executed at this moment, which is what a reference records: a reply
     * names the client that asked, and an event produced with no request in flight names nobody.
     */
    public void recordServerMessage(ClientConnection client, byte[] message) {
        recorder.serverMessage(client, message, currentRequestClient, currentRequestSequence);
    }

    /** A connection's setup reply, the client-started element of a recording. */
    public void recordClientStarted(ClientConnection client, byte[] setupReply) {
        recorder.clientStarted(client, setupReply);
    }

    /** A connection ended, the client-died element of a recording. */
    public void recordClientDied(ClientConnection client) {
        recorder.clientDied(client);
    }

    /**
     * A window changed size, told to every extension that registered an interest.
     *
     * <p>Called from the core {@code ConfigureWindow} path, before anything that draws reacts to it,
     * because an extension holding buffers sized to the window has to resize them first: a client
     * that resizes and then draws into a buffer would otherwise draw into the old size. Only DBE
     * listens today (see {@link Extension#windowResized}).
     */
    public void windowResized(XWindow window, int oldWidth, int oldHeight) {
        for (Extension extension : extensions.all()) {
            extension.windowResized(this, window, oldWidth, oldHeight);
        }
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    public ServerOptions options() {
        return options;
    }

    public ResourceTable resources() {
        return resources;
    }

    public AtomTable atoms() {
        return atoms;
    }

    public Screen screen() {
        return screen;
    }

    public List<ScreenSession> sessions() {
        return sessions;
    }

    /**
     * The input state of every screen the server is showing, the master one included.
     */
    private final Map<Screen, InputState> screenInputs = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * The input state of a screen: its own pointer, focus and grabs.
     *
     * <p>Two screens have two pointers. A backend never reports a move against one screen and have
     * it applied to another, which is what would happen if every screen shared the master's input.
     */
    public InputState inputFor(Screen screen) {
        return screenInputs.getOrDefault(screen, input);
    }

    /**
     * The input state a client's events belong to: its own screen's, or the master one.
     */
    public InputState inputFor(ClientConnection client) {
        ScreenSession session = client == null ? null : client.session();
        return session == null ? input : session.input();
    }

    /**
     * The screen a client's windows live on: its own when it has one, the master screen otherwise.
     * Everything that answers a client - the setup reply, its requests, its input - resolves the
     * screen through here rather than assuming the shared one.
     */
    public Screen screenOf(ClientConnection client) {
        ScreenSession session = client == null ? null : client.session();
        return session == null ? screen : session.screen();
    }

    /**
     * Gives a connection a screen of its own, with its own framebuffer, root window and input.
     *
     * <p>Called from the handshake, before the setup reply tells the client which root to use, so
     * the client's very first request already lands on its own screen.
     */
    /**
     * Says so when this display is scaled and the server is not drawing for it.
     *
     * <p>On a Retina panel the default of 1 is the wrong answer - everything is drawn at half the
     * resolution the display has and then enlarged by the platform - but it stays the default
     * because the other answer is not free: a client that does not follow the resolution lays itself
     * out for a screen half the size it is given, so making 2 the default would shrink every
     * application that has not been started with a scale setting of its own. A line in the log is
     * what the user has instead of a guess made for them.
     */
    private void suggestScale() {
        int host = ServerOptions.hostScale();
        if (host > 1 && options.scale() == 1) {
            JulLog.info("this display is {}x: '--scale auto' draws the screen at {}x pixels per logical "
                            + "pixel, which a client that reads Xft.dpi follows by itself - one that "
                            + "does not has to be started with its own scale setting, or it will lay "
                            + "itself out for a screen half this size (see the README)",
                    host, host);
        }
    }

    /**
     * Publishes the display's resolution to the clients that read it from the resource manager.
     *
     * <p>{@code Xft.dpi} on the root window is what `xrdb -merge` sets and what a toolkit that
     * renders through Xft - GTK, Qt, an xterm with Xft fonts - sizes its interface from. A client
     * that reads it draws at the resolution the screen really has, which is exactly what a forwarded
     * application needs on a Retina panel and what it cannot be told any other way: the server
     * cannot set a client's own toolkit flags. A client that never looks at the resource manager is
     * not affected by it either, which is what makes it safe to publish whenever the screen is
     * scaled.
     *
     * <p>It is only half of HiDPI, and the other half is not a server's to do - see
     * {@code ServerOptions.hostScale} and the README.
     */
    /**
     * Publishes {@code _XKB_RULES_NAMES}, the property that says which rules, model, layout and
     * options built the keyboard a client is typing on.
     *
     * <p>A reference server writes it for itself at startup - measured on a fresh Xvfb, before any
     * client has run: {@code "evdev", "pc105", "us", "", ""} - and clients read it back. It is not
     * decoration: `setxkbmap` reads it to know which rules file describes the loaded keymap, and both
     * `setxkbmap -print` and the property it writes afterwards follow from that one answer. Without
     * it, ours reported the client's own fallback ("base", and a keycodes component of "xfree86")
     * where a reference reports the evdev ones.
     *
     * <p>The value is one STRING of five NUL separated names, exactly as a reference's own bytes have
     * it (measured raw: {@code evdev\0pc105\0us\0\0\0}).
     */
    private void publishXkbRulesNames(Screen screen) {
        int name = atoms.intern("_XKB_RULES_NAMES");
        int string = atoms.intern("STRING");
        byte[] value = new byte[] {'e', 'v', 'd', 'e', 'v', 0, 'p', 'c', '1', '0', '5', 0, 'u', 's',
                0, 0, 0};
        screen.root().properties().put(name, new XProperty(name, string, 8, value));
    }

    /**
     * Publishes what tells a client that this display has a window manager to ask, and which of the
     * window-manager things it will be answered about.
     *
     * <p>**A client does not write {@code _NET_WM_STATE} itself; it asks its window manager, in
     * EWMH's words - but only after checking that there is one to ask.** GDK's
     * {@code gdk_x11_window_set_fullscreen} returns without sending anything unless the root's
     * {@code _NET_SUPPORTED} lists {@code _NET_WM_STATE_FULLSCREEN}, and AWT asks the root for
     * {@code _NET_SUPPORTING_WM_CHECK} before it will use EWMH at all. Without both, a request for
     * the whole screen is not refused, it is *never made*, and from the outside that is a key - F11
     * in a viewer, a browser's full screen - that does nothing. Measured 2026-10-13 on the real
     * machine: {@code eog} forwarded to display :5, F11 pressed, nothing happened and the log showed
     * no request at all; the message {@link AtomHandlers#applyWindowStateMessage} already handles
     * arrives as soon as the client believes there is somebody to send it to.
     *
     * <p>**Only what this server acts on is advertised, because the list is a promise.** A toolkit
     * that finds a state in {@code _NET_SUPPORTED} leaves that state to the manager; full screen is
     * the one state {@link WindowIdentity} acts on - it is what the frame of the host window is
     * made to match - so the list is that state and the property that carries it. The maximised
     * states are deliberately not in it: a client that found them would stop resizing itself for a
     * maximised window and wait for a manager that does not do that, which is worse than the state
     * not being named at all.
     *
     * <p>The check window is EWMH's way of telling a live manager from a stale property on the root:
     * the root points at a window of the manager's own, that window points at itself, and a client
     * that finds the two agreeing knows a manager is there. It is a child of the root and is never
     * mapped - it exists to be pointed at, not to be seen.
     */
    private void publishWindowManagerHints(Screen screen) {
        // **Interning, not looking up**: these are names this server is about to write onto its own
        // root window, and none of them exists yet - a client has not run to ask for them. The lookup
        // that only finds what is already there (the {@code onlyIfExists} overload) answers none for
        // every one of them at startup, which is how this method once published nothing at all -
        // measured 2026-10-13, where the root carried no {@code _NET_SUPPORTED} and the F11 this is
        // for still did nothing.
        int supported = atoms.intern(WindowIdentity.NET_SUPPORTED);
        int check = atoms.intern(WindowIdentity.NET_SUPPORTING_WM_CHECK);
        int atomType = atoms.intern("ATOM");
        int windowType = atoms.intern("WINDOW");
        int state = atoms.intern(WindowIdentity.NET_WM_STATE);
        int fullScreen = atoms.intern(WindowIdentity.NET_WM_STATE_FULLSCREEN);
        XWindow manager = new XWindow(nextServerResourceId(), null, screen, screen.root(),
                screen.root().depth(), XWindow.INPUT_OUTPUT, screen.root().visual(), 0, 0, 1, 1, 0);
        resources.add(manager);
        // **One per screen, not one for the server**: a client's root is its own screen's root in the
        // default mode, and EWMH wants the window the root points at to be a child of that root. It is
        // taken down with the screen - see {@link #closeClientScreen}.
        screen.setWmCheckWindow(manager);
        byte[] self = card32Words(manager.id());
        manager.properties().put(check, new XProperty(check, windowType, 32, self));
        int name = atoms.intern(WindowIdentity.NET_WM_NAME);
        int utf8 = atoms.intern("UTF8_STRING");
        byte[] title = "cn/oyzh/fx/xserver".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        manager.properties().put(name, new XProperty(name, utf8, 8, title));
        screen.root().properties().put(check, new XProperty(check, windowType, 32, self));
        screen.root().properties().put(supported, new XProperty(supported, atomType, 32,
                card32Words(fullScreen, state)));
    }

    /** A property value of card32s, in the big-endian order a property is stored in. */
    private static byte[] card32Words(int... values) {
        byte[] bytes = new byte[values.length * 4];
        for (int i = 0; i < values.length; i++) {
            bytes[i * 4] = (byte) (values[i] >>> 24);
            bytes[i * 4 + 1] = (byte) (values[i] >>> 16);
            bytes[i * 4 + 2] = (byte) (values[i] >>> 8);
            bytes[i * 4 + 3] = (byte) values[i];
        }
        return bytes;
    }

    private void publishDpi(Screen screen) {
        int atom = atoms.intern("RESOURCE_MANAGER", true);
        int string = atoms.intern("STRING", true);
        if (atom == AtomTable.NONE || string == AtomTable.NONE) {
            return;
        }
        // A line of resource database, as xrdb would write it: the value is what a client multiplies
        // its points by, and 96 is the resolution a 1x display is assumed to have.
        byte[] value = ("Xft.dpi:\t" + (96 * options.scale()) + "\n")
                .getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        screen.root().properties().put(atom, new XProperty(atom, string, 8, value));
        JulLog.info("display :{} is scaled {}x: clients that read Xft.dpi will draw at {} dpi",
                options.displayNumber(), options.scale(), 96 * options.scale());
    }

    /**
     * The host clipboard bridge, or null when this server has none.
     *
     * <p>A server with no window has no host to bridge to, so it has no bridge; neither has one that
     * was started with {@code --no-clipboard}. Callers must expect null: the bridge is a convenience
     * for a user sitting in front of the machine, not something the protocol depends on.
     */
    public ClipboardBridge clipboard() {
        return clipboard;
    }

    /**
     * The notification area, or null when this server has no menu bar.
     *
     * <p>Like {@link #clipboard()}, callers must expect null: a headless server and one served over
     * VNC have no host menu bar, and a client's tray icon has nothing to be shown in.
     */
    public TrayBridge tray() {
        return tray;
    }

    /**
     * Serves the notification area, if this server has somewhere to show its icons.
     *
     * <p>The tray is the host's menu bar, so only a backend with one starts the bridge: a headless
     * server has no menu bar, and a VNC server's viewers have their own desktops - showing an icon
     * in the machine the server runs on would be showing it to the wrong person.
     */
    private void startTrayBridge() {
        if (options.noTray() || tray != null) {
            return;
        }
        // The selection is taken at startup rather than when a window appears, and that is what a
        // client needs: an application asks whether a notification area exists while it is starting
        // up and gives up for good if the answer is no, so owning it only once something has been
        // drawn would be owning it too late for the application that wanted it. Whether there is a
        // menu bar is the options' own question - a backend given by a factory is taken at its word
        // by the code that shows the icons.
        if (presentsNoWindowHere()) {
            return;
        }
        tray = new TrayBridge(this);
        tray.start();
    }

    /**
     * The bridge that turns a drop from the host's desktop into an XDND drag for the application
     * under the pointer, or null when this server has no window to be dropped on.
     */
    public cn.oyzh.fx.xserver.drop.DropBridge dropBridge() {
        return drops;
    }

    /**
     * Starts the bridge if this server has somewhere to bridge to.
     *
     * <p>Only a server that presents a window does: a headless one is either a test or a display
     * nobody is looking at, and taking the machine's clipboard in either case would be a side effect
     * nobody asked for.
     */
    private void startClipboardBridge() {
        if (options.noClipboard()) {
            return;
        }
        // A window on this machine has the Mac's own pasteboard as its other end. A VNC server (D2)
        // has no window but does have viewers, and they want the same thing in the other direction -
        // so it gets the bridge too, wired up in both directions below.
        DisplayBackend window = masterSession.backend();
        boolean vnc = window instanceof cn.oyzh.fx.xserver.display.VncBackend;
        if (presentsNoWindowHere() && !vnc) {
            return;
        }
        HostClipboard host = HostClipboard.open();
        if (host.isAvailable()) {
            startClipboardBridge(host);
        }
    }

    /**
     * True when this server's screen is not shown in a window on this machine.
     *
     * <p>The clipboard and drag bridges are the host's own desktop, reached through a window the
     * user is looking at; a headless server has none, and neither has one served over VNC (D2) - a
     * viewer's clipboard and the Mac's pasteboard are two different bridges, and the RFB one is not
     * written yet.
     */
    private boolean presentsNoWindowHere() {
        return options.backend() == ServerOptions.Backend.HEADLESS
                || options.backend() == ServerOptions.Backend.VNC;
    }

    /**
     * Starts the drag bridge, and returns it. Public for the same reason the clipboard bridge's is:
     * a test drives a drop itself, because the platform's half of one cannot be produced in-process.
     */
    public cn.oyzh.fx.xserver.drop.DropBridge startDropBridge() {
        if (drops != null) {
            return drops;
        }
        drops = new cn.oyzh.fx.xserver.drop.DropBridge(this);
        drops.start();
        return drops;
    }

    /** Starts the drag bridge when this server has a window for the host to drop onto. */
    private void startDropBridgeForWindow() {
        if (options.noClipboard() || presentsNoWindowHere()) {
            // The same rule as the clipboard: a server nobody is looking at has no desktop to be
            // dragged from.
            return;
        }
        startDropBridge();
    }

    /**
     * Starts the bridge over a host clipboard the caller supplies, and returns it.
     *
     * <p>Public for the same reason {@code --no-clipboard} exists: which clipboard a server bridges
     * to is a decision about the machine it runs on, and a test that exercised the real one would be
     * a test that rearranges whatever the person running it had copied.
     */
    public ClipboardBridge startClipboardBridge(HostClipboard host) {
        if (clipboard != null) {
            return clipboard;
        }
        clipboard = new ClipboardBridge(this, host);
        clipboard.start();
        // A VNC server has no window but does have viewers, and they are the other end of the same
        // clipboard: X -> viewers is the bridge's listener, viewers -> X is its remoteCopy. Wired
        // here rather than in the caller so that a test (or an embedder) that supplies its own host
        // clipboard gets the same behaviour.
        DisplayBackend window = masterSession.backend();
        if (window instanceof cn.oyzh.fx.xserver.display.VncBackend vncBackend) {
            clipboard.setListener(vncBackend::clipboardTextChanged);
            vncBackend.setClipboardSink(clipboard::remoteCopy);
        }
        return clipboard;
    }


    public ScreenSession openClientScreen(ClientConnection client) {
        Screen privateScreen = createScreen(0);
        // Every screen's root carries it, not just the master's: in the default mode a client sees
        // *its own* root, so a client that asks which rules built the keyboard - `setxkbmap` does,
        // before it changes anything - would find the property missing and fall back to its own
        // defaults, which is a difference from a reference it then writes back into the property
        // (measured: "base"/"xfree86" against a reference's "evdev").
        publishXkbRulesNames(privateScreen);
        InputState privateInput = new InputState(this, privateScreen);
        ScreenSession session = new ScreenSession(privateScreen, privateInput, client);
        screenInputs.put(privateScreen, privateInput);
        sessions.add(session);
        client.setSession(session);
        return session;
    }

    /**
     * Frees a client's screen once its connection is gone. The display tick closes the window.
     */
    private void closeClientScreen(ScreenSession session) {
        if (session == null || session.closed()) {
            return;
        }
        session.markClosed();
        XWindow check = session.screen().wmCheckWindow();
        if (check != null) {
            // The manager window the root pointed at goes with the root it was a child of.
            resources.free(check.id());
        }
        resources.free(session.screen().root().id());
        resources.free(session.screen().defaultColormap().id());
        resources.free(session.screen().rootCursor().id());
        screens.remove(session.screen());
        screenInputs.remove(session.screen());
        JulLog.info("screen of display :{} freed", options.displayNumber());
    }

    public List<Screen> screens() {
        return screens;
    }

    /**
     * Lets every screen deliver the key repeats that have come due, at {@code nowMillis}.
     *
     * <p>The keyboard's repeats are the server's own work - a client that holds a key down is not
     * expected to press it again, and a toolkit that types a run of characters from it would type
     * one - so they are driven from the display tick, under the server lock, once per frame. Taking
     * the time as an argument keeps the whole thing testable, and one call covers every screen
     * because each has its own keyboard.
     */
    public void pollAutoRepeat(long nowMillis) {
        serverLock.lock();
        try {
            for (Screen screen : screens) {
                inputFor(screen).pollAutoRepeat(nowMillis);
            }
        } finally {
            serverLock.unlock();
        }
    }

    public XWindow rootWindow() {
        return screen.root();
    }

    public XColormap rootColormap() {
        return screen.defaultColormap();
    }

    public XCursor rootCursor() {
        return screen.rootCursor();
    }

    public int defaultFontId() {
        return defaultFontId;
    }

    /** SECURITY's authorizations, for the connection setup, or null on a server built without it. */
    public cn.oyzh.fx.xserver.extension.SecurityExtension security() {
        return securityExtension;
    }

    public ExtensionRegistry extensions() {
        return extensions;
    }

    public ConnectionSetup connectionSetup() {
        return connectionSetup;
    }

    public EventDispatcher events() {
        return events;
    }

    public InputState input() {
        return input;
    }

    /**
     * Handlers are shared with the dispatcher so cross-cutting requests can call into them.
     */
    public WindowHandlers windows() {
        return windowHandlers;
    }

    public DrawHandlers draw() {
        return drawHandlers;
    }

    public AtomHandlers atomHandlers() {
        return atomHandlers;
    }

    public List<ClientConnection> clients() {
        return clients;
    }

    public boolean isRunning() {
        return running;
    }

    public long uptimeMillis() {
        return System.currentTimeMillis() - startTime;
    }

    public DisplayBackend backend() {
        return masterSession.backend();
    }

    /**
     * A selection's current owner - zero for none - and when it last changed hands.
     *
     * <p>The timestamp is the one the XFixes selection notification carries as its
     * {@code selectionTimestamp}, which a client compares against the time it used in its own
     * {@code SetSelectionOwner} to decide whether a notification is news or an echo. Only
     * {@code SetSelectionOwner} moves it: a selection whose owner window is destroyed or whose owner
     * disconnects keeps the time it was taken, which is what a reference server was measured doing.
     */
    public record Selection(int owner, int timestamp) {
    }

    /** A selection's current owner, or 0 when nobody owns it. */
    public int selectionOwner(String name) {
        Selection selection = selections.get(name);
        return selection == null ? 0 : selection.owner();
    }

    /** When a selection last changed hands, or the current time when it never has. */
    public int selectionTimestamp(String name) {
        Selection selection = selections.get(name);
        return selection == null ? events.time() : selection.timestamp();
    }

    /**
     * Changes a selection's owner and tells the clients that subscribed to hear about it, in that
     * order: a notification sent before the owner is recorded makes the client query the old one.
     *
     * <p>This is the only way ownership moves, so it is also the only place that has to remember
     * that every {@code SetSelectionOwner} is announced - including the ones that name the owner
     * already there and the ones that name nobody, which a reference server was measured treating as
     * a change and moving the timestamp for.
     *
     * @param selectionTime when the selection changed hands, which becomes the selection's own time
     * @param subtype the XFixes subtype that says <em>why</em> it moved - the three do not carry the
     *     same times. When the selection changes hands the notification <em>is</em> the change, and
     *     a reference server reports both times as that one instant; when an owner goes away the
     *     notification happens now and the selection keeps the time it was taken.
     */
    public void setSelectionOwner(String name, int owner, int selectionTime, byte subtype) {
        selections.put(name, new Selection(owner, selectionTime));
        int atom = atoms.intern(name, true);
        if (atom != AtomTable.NONE) {
            int timestamp = subtype == XFixesExtension.SELECTION_SET_OWNER
                    ? selectionTime : events.time();
            XFixesExtension.notifySelection(this, atom, owner, timestamp, selectionTime, subtype);
            if (clipboard != null && subtype == XFixesExtension.SELECTION_SET_OWNER) {
                // The bridge watches the selection the same way a clipboard manager would, except
                // that it is the one that takes over when it is left free.
                clipboard.selectionOwnerChanged(name, owner);
            }
            if (drops != null && subtype == XFixesExtension.SELECTION_SET_OWNER) {
                // And the drag bridge watches the *drag* selection, because a client taking it is
                // the only sign X gives that a drag has started inside a forwarded application (C5).
                drops.selectionOwnerChanged(name, owner);
            }
        }
    }

    /**
     * The names of the selections {@code windowId} currently owns, so the code that destroys a
     * window or loses a client can release them and say why.
     */
    public List<String> selectionsOwnedBy(int windowId) {
        List<String> owned = new ArrayList<>();
        for (Map.Entry<String, Selection> entry : selections.entrySet()) {
            if (entry.getValue().owner() == windowId) {
                owned.add(entry.getKey());
            }
        }
        return owned;
    }

    /**
     * Releases every selection a window owns and tells the subscribers, keeping the time each was
     * taken: this is the owner going away rather than the selection changing hands, and a reference
     * server reports it that way.
     *
     * @param subtype {@code XFixesExtension.SELECTION_WINDOW_DESTROY} when the window is being
     *     destroyed, {@code SELECTION_CLIENT_CLOSE} when its client is disconnecting
     */
    public void releaseSelectionsOwnedBy(int windowId, byte subtype) {
        for (String name : selectionsOwnedBy(windowId)) {
            setSelectionOwner(name, 0, selectionTimestamp(name), subtype);
        }
    }

    /**
     * Keyboard and pointer grabs waiting for a matching input event.
     */
    public List<InputHandlers.PassiveGrab> passiveGrabs() {
        return passiveGrabs;
    }

    /**
     * Tells every client the keyboard mapping changed, which invalidates their cached keymaps.
     */
    public void broadcastMappingNotify(byte request, byte firstKeycode, byte count) {
        var event = com.github.moaxcp.x11.protocol.xproto.MappingNotifyEvent.builder()
                .firstEventOffset((byte) 0)
                .sentEvent(false)
                .request(request)
                .firstKeycode(firstKeycode)
                .count(count);
        for (ClientConnection client : clients) {
            events.send(client, event.sequenceNumber(client.sequenceNumber()).build());
        }
    }

    /**
     * Serializes request execution. Callers must pair with {@link #unlockServer()} in a finally.
     */
    public void lockServer() {
        serverLock.lock();
    }

    public void unlockServer() {
        if (serverLock.isHeldByCurrentThread()) {
            serverLock.unlock();
        }
    }

    /**
     * The client currently holding a server grab, or null.
     */
    public ClientConnection serverGrabHolder() {
        return serverGrabHolder;
    }

    /**
     * Takes a server grab for {@code client}, holding the lock across requests.
     *
     * <p>Grabs nest: Xlib's {@code XGrabServer} is not reference counted, but a client that calls it
     * twice and then ungrabs twice must end up with the server released. Tracking a depth rather
     * than a flag is what makes that work - with a flag the second ungrab sees a null holder, skips
     * the release, and the lock is held forever, which stalls the whole server.
     */
    public void acquireServerGrab(ClientConnection client) {
        serverLock.lock();
        serverGrabHolder = client;
        serverGrabDepth++;
    }

    /**
     * Releases one grab held by {@code client}.
     *
     * @return true when a hold was released
     */
    public boolean releaseServerGrab(ClientConnection client) {
        if (serverGrabHolder != client || serverGrabDepth <= 0) {
            return false;
        }
        serverGrabDepth--;
        if (serverGrabDepth == 0) {
            serverGrabHolder = null;
        }
        serverLock.unlock();
        return true;
    }

    /**
     * Forgets every grab a client held, used when its connection ends.
     */
    public void clearServerGrab(ClientConnection client) {
        if (serverGrabHolder == client) {
            serverGrabHolder = null;
            serverGrabDepth = 0;
        }
    }

    /**
     * Releases every hold this thread has on the server lock. Used when a client that had grabbed
     * the server disconnects, since nothing will ever send the matching {@code UngrabServer}.
     */
    public void releaseAllLocks() {
        while (serverLock.isHeldByCurrentThread()) {
            serverLock.unlock();
        }
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    /**
     * Uses a backend chosen by the caller instead of the one the options name, for a test that needs
     * a window it can close, or an embedder that drives a display of its own. Call it before
     * {@link #start()}.
     */
    public void setBackend(DisplayBackend backend) {
        this.sharedBackend = backend;
    }

    /**
     * How each screen's window is made, for a test that wants a window it can close without one
     * existing. Replaces the backend the options name.
     */
    public void setBackendFactory(java.util.function.Supplier<DisplayBackend> backendFactory) {
        this.backendFactory = backendFactory;
    }

    public void start() throws IOException {
        running = true;
        startTime = System.currentTimeMillis();
        if (backendFactory == null) {
            backendFactory = () -> BackendFactory.create(options);
        }
        // **Before any window exists, because a JavaFX one would claim the application** (A46).
        claimHostApplication();
        if (!options.perClientWindows()) {
            // **The session is idle before its backend is built, because it is: nothing is connected
            // yet.** Idle is otherwise worked out by the tick, and the tick has not run at this point -
            // so the session's own flag still says "not idle", and a backend that is told *that* opens
            // its window. Which is the flash reported twice: the window appeared as the server started
            // and vanished a moment later, when the first tick found the session idle after all
            // ("我以单窗口模式启动时，会先显示一次窗口…我要跟per-client一样保持静默，直到x客户端连接才显示
            // 真正的窗口"). With the state set first, the backend is built and told there is nothing to
            // show, so no window appears until a client draws in one.
            masterSession.setIdle(true);
            openSessionBackend(masterSession, new Rectangle(0, 0, screen.width(), screen.height()));
        }
        startClipboardBridge();
        startTrayBridge();
        startDropBridgeForWindow();
        suggestScale();
        listener = new ClientListener(this);
        listener.start();
        if (options.controlPort() >= 0) {
            // Useful with or without --detach-on-close: detaching on demand is how a user gets a
            // window out of the way without ending the session, and attaching is the only way back
            // to one that was closed while the flag was on.
            control = new cn.oyzh.fx.xserver.net.ControlListener(this, options.controlPort());
            control.start();
        }
        JulLog.info("fx-xserver listening on display :{} ({}x{}), {}", options.displayNumber(),
                options.width(), options.height(),
                options.perClientWindows() ? "one window per client" : "a single shared window");
        JulLog.info("fx-xserver: display :{} ready, run clients with DISPLAY=:{}", options.displayNumber(),
                options.displayNumber());
    }

    /**
     * Makes this process's platform <em>application</em> AWT's, before a JavaFX toolkit can make it
     * JavaFX's (A46).
     *
     * <p><b>The platform asks the application to quit, and whichever toolkit claimed it is the one
     * that answers - and the two answer differently.</b> On macOS the dock's Quit is an
     * {@code NSApplicationTerminate} request: AWT ends the process, so the JVM's own shutdown hook
     * runs and {@link #stop()} follows, exactly as {@code Ctrl-C} does; a JavaFX (Glass) application
     * turns the same request into a close request on its <b>visible</b> windows and answers the
     * platform with nothing at all when there is no window to close. Measured 2026-10-04 with a
     * minimal JavaFX program: with a hidden stage the dock's Quit did nothing at all; the same
     * program, with AWT touched first, was gone in half a second.
     *
     * <p><b>That is the difference between the two modes, and the report that found it.</b>
     * Per-client mode's clipboard bridge, tray bridge and scale probe all reach into AWT while the
     * server is starting - before the first client has a window - which is why that mode's Quit has
     * always worked. Single-window mode opens its screen's window inside
     * {@link #openSessionBackend} <em>first</em>, so JavaFX claimed the application, and with nothing
     * connected - a window that is deliberately not on the screen, see {@code ScreenSession.idle} -
     * the dock's Quit went nowhere ("单窗口下，macos在dock栏右键程序，点击退出没有反应，但是在
     * per-client模式下正常").
     *
     * <p>The claim is made here, explicitly, rather than left to the bridges that happen to do it
     * today: every one of them can be switched off ({@code --no-clipboard}, {@code --no-tray}, a
     * display whose scale is not worth reporting), and a mode whose Quit worked by accident is a mode
     * whose Quit stops working the next time one of them changes.
     *
     * <p>Nothing about this is macOS-only: AWT is the toolkit the clipboard and the tray are built on
     * everywhere, and a process with both toolkits in it is the ordinary JavaFX-in-Swing shape. A
     * machine with no display has no application to claim, and a machine that will not start AWT is
     * not a machine this can help - the window is JavaFX's either way.
     */
    private void claimHostApplication() {
        if (presentsNoWindowHere()) {
            // Headless, and served to a VNC viewer: there is no window on this desktop, so there is
            // no dock icon to be quit from and no application to claim. A VNC server that does own
            // the application does so through its clipboard bridge, which is a bridge this server
            // has always had.
            return;
        }
        try {
            Toolkit.getDefaultToolkit();
        } catch (Throwable e) {
            // A display-less or broken AWT does not stop the server: JavaFX is what draws the window,
            // and this call is only about who answers the platform's Quit.
            JulLog.debug("the host application stays unclaimed: {}", e.toString());
        }
    }

    /**
     * Starts the display loop on its own thread and returns that thread.
     *
     * <p>The loop must not run on the main thread: on macOS the platform's own event loop needs it,
     * and blocking it stops the JavaFX window from ever appearing. The thread is deliberately not a
     * daemon so the server keeps the process alive after {@code main} returns.
     *
     * <p><b>The thread is also the server's life</b>: it ends when the session is over (single-window
     * mode) or when {@link #stop()} is called, and an entry point that means to end the process has to
     * wait for it rather than return from {@code main} - see {@code Main}, where returning from
     * {@code main} was not enough because the JavaFX toolkit's own keep-alive thread outlives
     * {@code Platform.exit()} and is not a daemon.
     */
    public Thread startEventLoop() {
        Thread loop = new Thread(this::eventLoop, "xserver-display");
        loop.setDaemon(false);
        loop.start();
        return loop;
    }

    /** Guards {@link #frameRequested}, and what the display loop waits on. */
    private final Object frameWake = new Object();
    /**
     * True when something has been drawn since the frame being drawn now started. The next frame is
     * then due as soon as {@link #FRAMES_PER_PERIOD} allows, rather than at the end of the period.
     */
    private boolean frameRequested;

    /**
     * How many frames one tick period may hold when a client is drawing continuously. Drawing early
     * only helps up to the rate the toolkit paints at, and the toolkit coalesces whatever is left
     * over, so this is not a frame rate anybody could see: it is the limit that keeps a client
     * damaging its window in a tight loop from turning the display loop into a spin.
     */
    private static final int FRAMES_PER_PERIOD = 4;

    /**
     * Asks the display loop for a frame now, rather than at the end of its period.
     *
     * <p>Called on whichever thread has just drawn - see {@link Framebuffer#setDamageListener} - so it
     * must not block and must not take the server lock, which that thread holds while it draws. All
     * of it is one flag under the monitor the loop waits on, and the loop reads the flag while
     * holding that monitor, so a call that lands between the loop's check and its wait is not lost.
     */
    private void requestFrame() {
        synchronized (frameWake) {
            frameRequested = true;
            frameWake.notifyAll();
        }
    }

    private void eventLoop() {
        // **The frame rate is kept against an absolute clock, not against how long the last frame
        // happened to take, and a client that draws gets its frame at once rather than at the end of
        // the period.**
        //
        // Two things were wrong with sleeping a fixed number of milliseconds after the work. The work
        // itself was added to every frame, so the loop ran at whatever `--tick-rate` minus that cost
        // came to (measured 2026-10-14, one window under continuous damage: 2 ms of work on a 16.7 ms
        // frame gave 18.7 ms per tick, 53 fps where 60 was asked for); and because that sleep is
        // measured from the end of the work, the delay of its own wake-up was added to the *next*
        // frame as well, so a sleep that woke 1.2 ms late pushed every later frame back by the same
        // 1.2 ms for ever. Sleeping until the next deadline on a clock that does not move with the
        // work fixes both: the cost of a frame is that frame's, and a late wake-up is corrected for by
        // the next sleep rather than accumulated.
        //
        // The period is then only what a quiet screen is drawn at. A frame whose pixels have changed
        // is worth drawing as soon as they have changed, because the toolkit paints when it next looks
        // rather than when it is told: a frame handed over earlier is a frame shown earlier, and what
        // was left to wait for was the rest of the period - a whole period of delay on every frame of a
        // client that draws steadily, which is the lag this removes.
        // {@link #requestFrame} is what a client's own drawing calls, and the wait below ends on it.
        long frameNanos = Math.max(1L, 1_000_000_000L / Math.max(1, options.tickRate()));
        long earliestNanos = Math.max(1_000_000L, frameNanos / FRAMES_PER_PERIOD);
        while (running) {
            long frameStart = System.nanoTime();
            synchronized (frameWake) {
                // Anything drawn from here on is drawn after this frame has taken its damage, and is
                // what the next frame is for.
                frameRequested = false;
            }
            for (ScreenSession session : sessions) {
                tick(session);
            }
            if (!options.perClientWindows() && masterSession.closeDeadline() != 0
                    && System.currentTimeMillis() >= masterSession.closeDeadline()) {
                // Single-window mode: the one window was closed and the clients have had their
                // grace period, so the session is over.
                break;
            }
            synchronized (frameWake) {
                while (running) {
                    long now = System.nanoTime();
                    // The period runs from the start of this frame's work, so that the work is part of
                    // the period rather than added to it, and a frame that overran is not caught up in
                    // a burst of back-to-back ones - that is a stutter, not a frame rate.
                    long due = frameStart + frameNanos;
                    if (frameRequested) {
                        due = Math.min(due, Math.max(now, frameStart + earliestNanos));
                    }
                    if (due <= now) {
                        break;
                    }
                    try {
                        long wait = due - now;
                        frameWake.wait(wait / 1_000_000L, (int) (wait % 1_000_000L));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        running = false;
                        break;
                    }
                }
            }
        }
        stop();
    }

    /**
     * One frame for one screen: opens its window when there is something to show, keeps the window
     * sized to what the client draws, and presents the damage.
     *
     * <p>Everything the server owns is read under the server lock and the backend is called outside
     * it, because those calls wait on the toolkit's own thread - and holding the lock there is what
     * would freeze every other client.
     */
    private void tick(ScreenSession session) {
        if (session.closed()) {
            closeSessionBackend(session);
            sessions.remove(session);
            return;
        }

        Rectangle wanted;
        Rectangle damage;
        CursorSprite cursor;
        boolean viewportChanged = false;
        // Set when the window has nothing left to frame, and acted on below where the lock is not held:
        // closing a window waits on the toolkit's own thread (see the backend calls at the end of this
        // method), and the tick is the only place that decides a session has gone idle.
        boolean hideIdleWindow = false;
        serverLock.lock();
        try {
            // A resize the user asked for goes first: it changes the windows the rectangle below is
            // the union of, so doing it after would put the window and the frame one tick apart.
            applyRequestedSize(session);
            // **A window the client mapped where the screen cannot show all of it is not cropped.**
            // Completing the rule the resize above starts: the screen follows the window, whether the
            // window asked for the size with a drag or arrived with it - see fitContentOnScreen.
            fitContentOnScreen(session.screen());
            // A held key repeats from here rather than from the client: the tick is where the
            // server's own periodic work lives, and the lock is already held.
            inputFor(session.screen()).pollAutoRepeat(System.currentTimeMillis());
            XWindow source = identitySource(session.screen());
            if (source != session.identitySource()) {
                // The window the host window is named after, which is the largest one the client has
                // mapped - so it arrives with the first window the client maps and follows along if
                // the client replaces it.
                boolean firstTopLevel = session.identitySource() == null && source != null;
                session.setIdentitySource(source);
                session.setIdentity(source == null
                        ? WindowIdentity.NONE
                        : WindowIdentity.of(atoms, source));
                if (firstTopLevel) {
                    // The client's first top level window has been mapped, and the host window is
                    // the frame around it: without this it stays behind whatever the user was
                    // looking at when they started the application, grey and unfocused. The raise
                    // itself waits for a frame - see the end of this tick - and happens once per
                    // session: a client's later windows are its own business, and a popup is not a
                    // reason to pull the window out from under the user's hands.
                    session.setRaisePending(true);
                }
            }
            // **One rule for both modes: the window shows the part of the screen the clients' windows
            // occupy.** `--single-window` used to show the whole screen at 1:1, which made the host
            // window's size and the screen's size the same thing - and that is what left that mode
            // with no usable restore or drag (A39: the platform's zoom and every resize gesture were
            // fighting over one rectangle, and macOS undid a fill within a tenth of a second).
            // With the viewport taken from the content instead, single-window mode's window is an
            // ordinary frame around the client windows, exactly like a per-client window, and the
            // maximize/restore path that works there applies to it unchanged.
            //
            // The trade-off, which is being tried rather than assumed: a window no longer shows the
            // whole screen, so a client whose window is small gets a small window and a blank part of
            // the screen is not shown at all. That is what the mode's user asked to see.
            wanted = contentBounds(session.screen());
            boolean nowIdle = session.client() == null ? clients.isEmpty() : wanted == null;
            if (nowIdle != session.idle()) {
                session.setIdle(nowIdle);
                if (nowIdle) {
                    // The frame that takes the server's pointer off a window it will not touch again.
                    markSpriteDamage(session, session.lastCursor());
                    session.setLastCursor(null);
                    // **The window goes with the clients.** Single-window mode's window is the frame
                    // around whatever its clients are drawing, so with none of them connected there is
                    // nothing to frame and no reason to keep a window on the user's desktop - reported
                    // 2026-10-04: "可以做成没有任何x客户端连接的时候，可以默认隐藏主窗口，有连接了再显示
                    // 并置顶聚焦一次". It is opened again by the first window a client maps, which is
                    // also where the raise and the focus are (see raisePending below). Not yet,
                    // though: a session is given a moment to come back before its window is taken away
                    // (see ScreenSession.idleSince), so a client that unmaps one window and maps
                    // another does not make the window flicker.
                    //
                    // The line below still says what it always said - at this instant the window *is*
                    // holding its last frame, and it is this tick that decides whether it keeps it.
                    // Said about a window only where there is one. "The window holds its last frame"
                    // was said for the headless and VNC backends and for a session that has no
                    // backend at all - which is every idle moment of per-client mode's master
                    // session - so the line named a window that does not exist, the same fault as
                    // the "opened a window" claim beside it (see openedMessage).
                    DisplayBackend showing = session.backend();
                    JulLog.info("nothing to show on display :{}: {}", options.displayNumber(),
                            showing != null && !showing.isHeadless()
                                    ? "the window holds its last frame"
                                    : "no window is showing it");
                }
            }
            if (nowIdle && session.backend() != null && !session.backend().isHeadless()
                    && System.currentTimeMillis() - session.idleSince()
                            >= IDLE_WINDOW_GRACE_MILLIS) {
                // Nothing to show for long enough that the client has gone rather than paused between
                // windows: the window that was framing it goes too. It is closed below, where the lock
                // is not held (closing a window waits on the toolkit's thread), and a window with
                // nothing to show in it is not presented in the meantime either way. Only a *window*
                // is closed: a headless or VNC session has none, and a VNC viewer keeps watching a
                // screen that has no clients on it.
                hideIdleWindow = true;
            }
            if (wanted == null || wanted.equals(session.viewport())) {
                session.setPendingViewport(null);
                session.setPendingSince(0);
            } else if (session.pendingSince() == 0) {
                // The rectangle moved: start the clock, and keep the latest version of it while the
                // clock runs. A client dragging something moves it every frame, and following that
                // frame by frame means resizing the host window through the platform's window server
                // that often, which is what makes the drag stutter.
                session.setPendingViewport(new Rectangle(wanted));
                session.setPendingSince(System.currentTimeMillis());
            } else {
                session.setPendingViewport(new Rectangle(wanted));
                if (System.currentTimeMillis() - session.pendingSince() >= VIEWPORT_SETTLE_MILLIS) {
                    Rectangle settled = session.pendingViewport();
                    session.setPendingViewport(null);
                    session.setPendingSince(0);
                    Rectangle was = session.viewport();
                    session.setViewport(settled);
                    session.screen().framebuffer().markAllDirty();
                    viewportChanged = true;
                    // The window following its content is otherwise invisible from the server side,
                    // and a window that jumps or shrinks is exactly what this line accounts for.
                    JulLog.debug("display :{} window now shows {}, was {}", options.displayNumber(),
                            settled, was);
                }
            }
            cursor = session.idle() ? null : session.input().cursorSprite();
            // The sprite belongs to the frame, so its bookkeeping happens before the damage is
            // taken: a pointer that moved has to be in the frame that goes out now rather than the
            // next one, and - because a moved sprite marks damage of its own - a pointer moving
            // over a window whose client is drawing nothing still gets a frame at all. Doing this
            // after the damage was taken made the pointer advance only on the ticks where the
            // client happened to repaint, which reads as a stuttering cursor.
            if (!sameSprite(session.lastCursor(), cursor)) {
                markSpriteDamage(session, session.lastCursor());
                markSpriteDamage(session, cursor);
                session.setLastCursor(cursor);
            }
            // The cursor a client subscribed to hearing about, from here rather than from the
            // pointer's own path: it is the display that knows which window the pointer is in, and
            // the answer is the same whoever asks.
            XCursor displayed = session.input().displayedCursor();
            if (!session.cursorBaselineTaken()) {
                // Nothing is announced until there is a previous value to have changed from.
                session.setCursorBaselineTaken(true);
                session.setNotifiedCursor(displayed);
            } else if (displayed != session.notifiedCursor()) {
                session.setNotifiedCursor(displayed);
                if (displayed != null) {
                    XFixesExtension.notifyCursor(this, session.screen(), displayed.serial(),
                            events.time());
                }
            }
            if (drops != null && session == masterSession) {
                // A drag that is waiting for the target to answer is advanced here, next to the rest
                // of the work that happens on the server's own tick.
                drops.pump();
            }
            rememberWindowPosition(session);
            damage = session.screen().framebuffer().takeDamage();
        } finally {
            serverLock.unlock();
        }

        if (session.detached()) {
            // A session with no window on purpose (backlog D1): whatever window it still had is
            // closed here, where the lock is not held and the toolkit's thread may be waited on,
            // and no new one is opened. Everything above this line still happened - the client's
            // requests were answered, its keys repeat, its damage was taken - so the session is
            // alive; the frame it drew is simply not shown to anybody until one attaches.
            closeSessionBackend(session);
            return;
        }
        if (hideIdleWindow) {
            // The last client is gone: the window that was framing it goes too. Nothing else is done
            // with it - there is no frame to present and no cursor to show - and the next tick that has
            // content opens a window again, on the path below.
            JulLog.debug("display :{} has had nothing to show for {} ms: its window is closed",
                    options.displayNumber(), IDLE_WINDOW_GRACE_MILLIS);
            closeSessionBackend(session);
            return;
        }
        if (session.backend() == null) {
            // Nothing mapped yet: the window is opened by the first window a client maps, so a
            // connection that only asks questions never opens one. The master screen has no window
            // at all in per-client mode - it is not shown to anybody, it only exists so the
            // in-process tests and single-window mode have a screen to build on.
            if (wanted == null || (session.client() == null && options.perClientWindows())) {
                return;
            }
            try {
                openSessionBackend(session, wanted);
            } catch (RuntimeException e) {
                // A window that cannot be opened means nothing this server does will ever be seen.
                // Carrying on would be a display server that answers clients and shows nobody
                // anything - measured: with the JavaFX toolkit on a machine that cannot show a
                // window the tick thread parked for ever and the server went on serving, its only
                // explanation a stack trace from a render thread. Stop, and say why in one line.
                JulLog.error("fx-xserver: cannot show display :" + options.displayNumber()
                        + " with the " + options.backend() + " backend: " + e.getMessage(), e);
                stop();
                return;
            }
        } else {
            if (session.takeScreenResized()) {
                // The screen grew while the lock was held; a backend that wrapped the old pixels has
                // to be told before it is asked to present them.
                session.backend().screenResized(session.screen());
            }
            if (viewportChanged) {
                session.backend().setViewport(wanted);
            }
        }
        DisplayBackend window = session.backend();
        if (window == null) {
            return;
        }
        // The pointer's appearance, every frame and not only when something drew: a backend that
        // shows the pointer to somebody else - the VNC one - has to be able to answer a viewer that
        // has just connected, and on an idle screen there is no damage to ride along with. The
        // backend compares it against what it last sent, so this is a reference check per frame.
        window.presentCursor(cursor);
        // How large the window may be dragged, which is not the screen any more: the screen grows to
        // fit what the user asks for, so a limit at the screen's current edge would stop the drag
        // that was supposed to grow it. What is left is `--max-screen` - the size beyond which a
        // client could not be described the screen at all - less where the content starts on it.
        Dimension maxArea = new Dimension(
                options.maxScreenWidth() - session.viewport().x,
                options.maxScreenHeight() - session.viewport().y);
        if (maxArea.width > 0 && maxArea.height > 0 && !maxArea.equals(session.maxClientArea())) {
            session.setMaxClientArea(maxArea);
            window.setMaxClientArea(maxArea.width, maxArea.height);
        }
        if (session.identity() != session.appliedIdentity()) {
            // Only when it is a new value: a session whose client is not retitling itself produces
            // one identity object, and this is a reference comparison against it.
            WindowIdentity identity = session.identity();
            session.setAppliedIdentity(identity);
            window.setWindowTitle(identity.title());
            window.setWindowIcon(identity.iconArgb(), identity.iconWidth(), identity.iconHeight());
            // What the client asked the window manager for, applied to the frame: full screen is
            // shown as the display, and the urgency hint brings the window forward once - the two
            // things a forwarded application cannot do for itself when the frame is not its own.
            if (identity.fullScreen() != session.appliedFullScreen()) {
                session.setAppliedFullScreen(identity.fullScreen());
                window.setFullScreen(identity.fullScreen());
            }
            if (identity.urgent() != session.appliedUrgent()) {
                session.setAppliedUrgent(identity.urgent());
                if (identity.urgent()) {
                    window.raise();
                }
            }
            // Which window a client's title ends up on is otherwise only visible in the host
            // window's title bar, which nobody looking at a log has.
            JulLog.debug("display :{} window is now \"{}\"{}", options.displayNumber(),
                    identity.title(), identity.iconArgb() == null ? ""
                            : " with a " + identity.iconWidth() + "x" + identity.iconHeight()
                                    + " icon");
        }
        if (session.idle() != session.idleApplied()) {
            session.setIdleApplied(session.idle());
            window.setIdle(session.idle());
        }
        if (window.isClosed()) {
            onWindowClosed(session);
            return;
        }
        if (session.raisePending() && session.framePresented()) {
            // A frame has been on the screen since the client's first window appeared, so bringing
            // the host window forward now shows that window rather than an empty frame. The wait is
            // for *a* frame and not for damage in this tick: the tick a window is mapped in consumes
            // the damage that drew it, and a raise that waited for another one would wait for the
            // client to draw again, which a static window never does.
            session.setRaisePending(false);
            // Raised *and* focused: the window has just appeared for a client that has just started
            // drawing, which is the one moment the user is waiting for it - unlike the urgency hint,
            // which only asks to be looked at (see DisplayBackend.raiseAndFocus).
            window.raiseAndFocus();
            JulLog.debug("display :{} host window brought to the front for a new client window",
                    options.displayNumber());
        }
        if (session.takeFullFrame()) {
            // A window has just attached to a screen that was drawing with nobody watching: the
            // pixels are all still in the framebuffer, but the damage they were drawn with was
            // consumed frame by frame while there was no window, so waiting for more of it would
            // show the client's background until the client happened to draw again.
            damage = new Rectangle(session.viewport());
        }
        if (!session.idle() && damage != null) {
            session.setFramePresented(true);
            window.present(session.screen(), damage, cursor);
        }
    }


    /**
     * How long the rectangle a client's windows occupy has to hold still before the host window is
     * resized to it.
     *
     * <p>Every resize goes through the platform's window server, which is far more expensive than
     * painting a frame, and a client that is dragging something - a tooltip that follows the
     * pointer, an animated resize - moves that rectangle every frame. Resizing on each of those
     * makes the drag stutter, so the rectangle is applied once it settles.
     */
    private static final long VIEWPORT_SETTLE_MILLIS = 0;

    /**
     * How long a session with nothing to show keeps its window before the window is taken away.
     *
     * <p>A client that has gone leaves a window with nothing in it, and the window goes with it - but
     * several toolkits unmap their first window and map another a moment later while they start up, and
     * a window that disappears and comes back in between reads as a flicker rather than as an
     * application. Long enough to cover that, short enough that a client that really has gone is not
     * leaving a window on the desktop.
     */
    private static final long IDLE_WINDOW_GRACE_MILLIS = 1500;
    /**
     * How long the host window has to hold still before where it is gets written to the store. A
     * drag reports a position every frame; only where it came to rest is worth keeping.
     */
    private static final long WINDOW_POSITION_SETTLE_MILLIS = 400;

    /**
     * The part of a screen the client's windows occupy: the union of its mapped top level windows.
     *
     * <p>Their children are inside them without being counted, and so are override-redirect windows -
     * menus, tooltips, drag icons. Those are transient overlays, and a window whose size followed
     * them would grow the moment a context menu opened and shrink back the moment it closed, which
     * is a window that jumps every time the user right-clicks. A window manager sizes the frame to
     * the client's window and lets a popup be clipped by the screen; this is the same rule with the
     * frame standing in for the screen.
     *
     * <p>The exception is a client that has mapped <em>nothing but</em> popups, which would otherwise
     * have no size at all: those are counted after all, so whatever it is showing is shown.
     */
    private static Rectangle contentBounds(Screen screen) {
        Rectangle windows = null;
        Rectangle popups = null;
        for (XWindow child : screen.root().children()) {
            if (!child.isMapped() || child.isInputOnly()) {
                continue;
            }
            // **The window itself, not its border.** A window's border is the server's own one-pixel
            // frame, and counting it here made the viewport one pixel larger than the window on each
            // side - which is a feedback loop in both directions: a minimize/maximize cycle grew the
            // window by two pixels every time (measured with xclock: 302 -> 304 -> 306 -> 308), and in
            // single-window mode it walked the window up two pixels per frame until it filled the
            // screen and flickered, which is the A39 report ("xclock and xeyes keep stretching the
            // window"). The border is drawn by the host window's own frame instead.
            Rectangle bounds = child.absoluteBounds();
            if (child.isOverrideRedirect()) {
                popups = popups == null ? bounds : popups.union(bounds);
            } else {
                windows = windows == null ? bounds : windows.union(bounds);
            }
        }
        Rectangle content = windows != null ? windows : popups;
        return content == null ? null
                : content.intersection(new Rectangle(0, 0, screen.width(), screen.height()));
    }

    /**
     * A window's properties changed. A host window takes its title and icon from one client window,
     * so if that is the one that changed, the host window has to be renamed.
     *
     * <p>Called from the request thread while the property is being written, which is what makes a
     * retitle show up as promptly as the client made it.
     */
    public void windowPropertyChanged(XWindow window, int property) {
        if (!WindowIdentity.isIdentityProperty(atoms.name(property))) {
            // A toolkit writes properties constantly - normal hints, the state, the class - and
            // rebuilding the identity for each of those would copy the icon of a large one for
            // nothing.
            return;
        }
        serverLock.lock();
        try {
            for (ScreenSession session : sessions) {
                if (session.identitySource() == window) {
                    session.setIdentity(WindowIdentity.of(atoms, window));
                }
            }
        } finally {
            serverLock.unlock();
        }
    }

    /**
     * The window a session shows in its title bar: the largest top level window its client has
     * mapped.
     *
     * <p>Largest rather than first, because the first is often a hidden 1x1 window a toolkit keeps
     * for its own use, and override-redirect windows are left out entirely - a menu has no title of
     * its own, and an application that retitles itself does not retitle its menus.
     */
    private static XWindow identitySource(Screen screen) {
        XWindow best = null;
        long bestArea = 0;
        for (XWindow child : screen.root().children()) {
            if (!child.isMapped() || child.isInputOnly() || child.isOverrideRedirect()) {
                continue;
            }
            long area = (long) child.width() * child.height();
            if (best == null || area > bestArea) {
                best = child;
                bestArea = area;
            }
        }
        return best;
    }

    /**
     * Resizes the client's windows to the size the user dragged the host window to.
     *
     * <p>The host window is the frame around the client's window, so stretching it is the gesture a
     * window manager's frame has always had: the client's window becomes that size and is told so
     * with a ConfigureNotify. A client that refuses - one whose window has a fixed size - keeps its
     * own size instead, and the frame is then simply larger than what it frames.
     *
     * <p>The size is capped at the screen: the framebuffer behind it is not resized, so a window
     * stretched past the screen would have nothing to show in the extra area.
     */
    private void applyRequestedSize(ScreenSession session) {
        Dimension requested = session.input().takeRequestedSize();
        if (requested == null) {
            return;
        }
        Screen screen = session.screen();
        // Before anything is measured: a drag that asked for more than the screen can show grows the
        // screen to fit it, so the extents below are worked out against the size there will be rather
        // than the one there was.
        growScreenToFit(screen, requested);
        boolean resized = false;
        boolean capped = false;
        for (XWindow window : screen.root().children()) {
            if (!window.isMapped() || window.isInputOnly() || window.isOverrideRedirect()) {
                continue;
            }
            // **What the client says its window may be.** A frame drag is a *request* to the client,
            // and a client whose hints forbid the size answers by resizing itself back - measured
            // 2026-10-04: xclock sent 21 ConfigureWindow requests during one left-edge drag, and the
            // frame ended up 40 px over and 25 px narrower because it had been asking for sizes the
            // window's own aspect ratio does not allow (A39). Clamping here is what a window manager
            // does, and it is what stops the two sides correcting each other.
            SizeLimits limits = sizeLimits(window);
            int wantedWidth = limits.clampWidth(requested.width);
            int wantedHeight = limits.clampHeight(requested.height);
            if (limits.aspectDriver() != 0) {
                // A declared aspect ratio ties the two dimensions together, and a drag that moves one
                // edge moves the other with it - which is what a window manager does and what xclock
                // was correcting this server for. The dimension the drag changed drives the other.
                if (wantedWidth != visibleExtent(window.x(), window.width(), screen.width())) {
                    wantedHeight = limits.clampHeight(limits.heightForWidth(wantedWidth));
                } else if (wantedHeight != visibleExtent(window.y(), window.height(), screen.height())) {
                    wantedWidth = limits.clampWidth(limits.widthForHeight(wantedHeight));
                }
            }
            // A dimension the drag did not change is left alone rather than set to the same number
            // arrived at differently: a window that hangs off the left of the screen and one that
            // starts inside it need different widths to show the same amount, and rewriting the one
            // the user did not touch would resize the client's window for nothing - which its own
            // layout then reacts to.
            int width = visibleExtent(window.x(), window.width(), screen.width())
                    == wantedWidth
                    ? window.width()
                    : windowExtent(wantedWidth, window.x(), screen.width());
            int height = visibleExtent(window.y(), window.height(), screen.height())
                    == wantedHeight
                    ? window.height()
                    : windowExtent(wantedHeight, window.y(), screen.height());
            capped |= requested.width != visibleExtent(window.x(), width, screen.width())
                    || requested.height != visibleExtent(window.y(), height, screen.height());
            if (width == window.width() && height == window.height()) {
                continue;
            }
            windows().resizeWindow(window, width, height);
            resized = true;
        }
        if (capped) {
            // Debug rather than info: a drag delivers a resize a frame at a time, so holding the
            // pointer past the edge would write this line sixty times a second.
            JulLog.debug("a window on display :{} was dragged to {}x{}, which the {}x{} screen cannot "
                            + "show all of", options.displayNumber(), requested.width,
                    requested.height, screen.width(), screen.height());
        }
        if (resized) {
            screen.framebuffer().markAllDirty();
        }
    }

    /**
     * Grows the screen when a drag asked for more than it can show.
     *
     * <p>This is what "the screen follows the window" means here: the client's window can only be
     * told to be as large as the framebuffer behind it, so the framebuffer has to be the thing that
     * moves first. The growth is bounded by {@code --max-screen}: a screen is described to clients in
     * sixteen bit fields, and every pixel costs four bytes.
     *
     * <p>A screen with no mapped window on it is left alone - the drag would have had nothing to
     * resize - and so is one that is already large enough, which is every drag but the one that
     * reaches the edge.
     *
     * @return true when the screen grew, in which case the backend has to rebuild what it holds
     */
    private boolean growScreenToFit(Screen screen, Dimension requested) {
        int wantedWidth = screen.width();
        int wantedHeight = screen.height();
        boolean anything = false;
        for (XWindow window : screen.root().children()) {
            if (!window.isMapped() || window.isInputOnly() || window.isOverrideRedirect()) {
                continue;
            }
            anything = true;
            // The window's own far edge has to be on the screen for this many pixels of it to be
            // visible, and a window that starts off the left or the top needs no more room than the
            // pixels it is showing.
            wantedWidth = Math.max(wantedWidth, requested.width + Math.max(0, window.x()));
            wantedHeight = Math.max(wantedHeight, requested.height + Math.max(0, window.y()));
        }
        if (!anything) {
            return false;
        }
        return growScreen(screen, wantedWidth, wantedHeight,
                "a client's window was dragged past it");
    }

    /**
     * Grows a screen to the size asked for, up to {@code --max-screen}.
     *
     * <p>The one place a screen's size changes: everything that can ask for more room ends up here,
     * so the pixels are reallocated, the clients that asked to hear about it are told, and every
     * session showing this screen is marked so its backend rebuilds what it holds around the new
     * pixels - in that order, once, whoever asked.
     *
     * @param why the rest of the line the growth is logged with, so a reader can tell a drag from a
     *     window that arrived too large
     * @return true when the screen grew
     */
    private boolean growScreen(Screen screen, int wantedWidth, int wantedHeight, String why) {
        int width = Math.min(wantedWidth, options.maxScreenWidth());
        int height = Math.min(wantedHeight, options.maxScreenHeight());
        if (width == screen.width() && height == screen.height()) {
            if (wantedWidth > width || wantedHeight > height) {
                JulLog.debug("display :{} asked for a {}x{} screen, which --max-screen holds at {}x{}",
                        options.displayNumber(), wantedWidth, wantedHeight, width, height);
            }
            return false;
        }
        JulLog.info("growing the screen of display :{} to {}x{}: {}",
                options.displayNumber(), width, height, why);
        screen.resize(width, height);
        tellClientsTheScreenChanged(screen);
        for (ScreenSession session : sessions) {
            if (session.screen() == screen) {
                session.setScreenResized();
            }
        }
        return true;
    }

    /**
     * **A client's window is not cropped by the screen: it is nudged onto it, and the screen grows to
     * fit what the client has mapped.**
     *
     * <p>Reported 2026-10-13: "without a suitable geometry, EasyShell on the VM is visibly clipped a
     * little on both the left and the right, while xclock and xeyes are fine". Measured: EasyShell's
     * window is 1300x700 and it centres itself on the screen it is told about, so on the default
     * 1280x800 screen it lands at {@code -10+33} - ten pixels off each side - and the host window,
     * which is the frame around what the client's windows occupy, showed the 1280 columns that exist.
     * xclock and xeyes fit on the screen, which is the whole of why they were never seen clipped.
     *
     * <p>The screen is the reason the window cannot be shown: it is 1280 wide, the client was told
     * 1280, and it asked for a window 20 pixels wider anyway. Two things are done about it, and both
     * are things a window manager's placement or this server's own drag path already do:
     *
     * <ul>
     * <li><b>a window that starts off the left or the top is moved onto the screen.</b> Pixels at a
     *     negative position do not exist on an X screen, so no framebuffer size can ever show them -
     *     the client is told with an ordinary ConfigureNotify (see
     *     {@link WindowHandlers#moveWindow}), which is what a
     *     window manager that re-places a window does.</li>
     * <li><b>the screen grows to the far edge of what the client has mapped</b>, so a window wider
     *     or taller than the screen does not have its right or bottom edge cut off. That is C1's rule
     *     - the screen follows the window - applied to a window that <em>arrived</em> too large
     *     rather than to one the user dragged, capped at {@code --max-screen} like the drag.</li>
     * </ul>
     *
     * <p>Together they are what {@code --geometry 1300x800} was being used for: EasyShell's own
     * window ends up at {@code +0+33} on a 1300x800 screen, whole, with no geometry asked for. The
     * work is one pass over the mapped top level windows - the same set {@link #contentBounds}
     * measures - and both halves are no-ops once they have happened, so this costs a loop per tick on
     * a screen that already fits its content.
     */
    private void fitContentOnScreen(Screen screen) {
        int wantedWidth = screen.width();
        int wantedHeight = screen.height();
        for (XWindow window : screen.root().children()) {
            if (!window.isMapped() || window.isInputOnly() || window.isOverrideRedirect()) {
                continue;
            }
            if (window.x() < 0 || window.y() < 0) {
                JulLog.info("moving a {}x{} window of display :{} from {},{} to {},{}: a window that starts "
                                + "off the screen can never be shown in full",
                        window.width(), window.height(), options.displayNumber(),
                        window.x(), window.y(), Math.max(0, window.x()), Math.max(0, window.y()));
                windows().moveWindow(window, Math.max(0, window.x()), Math.max(0, window.y()));
            }
            // The far edge of every window, which is the most of the screen any of them needs: a
            // window that starts inside it needs room for all of its pixels, and one that started off
            // the left has just been moved onto it and needs the same.
            wantedWidth = Math.max(wantedWidth, window.x() + window.width());
            wantedHeight = Math.max(wantedHeight, window.y() + window.height());
        }
        growScreen(screen, wantedWidth, wantedHeight, "a client's window does not fit on it");
    }

    /**
     * Tells the clients that asked to hear it that the root is a different size.
     *
     * <p>Two ways, because there are two kinds of client: the root window's own {@code ConfigureNotify}
     * - the root is a window and its geometry changed - and RandR's {@code ScreenChangeNotify}, which
     * is the one a modern toolkit, browser or terminal listens for. Until RandR was implemented the
     * first was all there was, which is why a client that had asked RandR about the display was never
     * told it had changed.
     */
    private void tellClientsTheScreenChanged(Screen screen) {
        randrExtension.screenChanged(this, screen);
        events.deliverToWindow(screen.root(), EventMasks.STRUCTURE_NOTIFY,
                (client, w, child, x, y) -> ConfigureNotifyEvent.builder()
                        .firstEventOffset((byte) 0)
                        .sentEvent(false)
                        .sequenceNumber(client.sequenceNumber())
                        .event(w.id())
                        .window(screen.root().id())
                        .x((short) 0)
                        .y((short) 0)
                        .width((short) screen.width())
                        .height((short) screen.height())
                        .borderWidth((short) 0)
                        .aboveSibling(0)
                        .overrideRedirect(false)
                        .build());
    }

    /**
     * The sizes a client's {@code WM_NORMAL_HINTS} allows its window to take: the minimum and maximum
     * size, the resize increments (about a base size), and the aspect ratio.
     *
     * <p><b>This is what a frame drag has to respect, and not doing it is what made dragging the left
     * or top edge drift.</b> Measured 2026-10-04 (A39): xclock sent **21 ConfigureWindow requests**
     * during one left-edge drag, because the sizes this server asked it to take were not sizes its own
     * aspect ratio allows - it put itself back, and since the platform had already moved the frame's
     * origin by the pointer's delta, the window ended up 40 px over and only 25 px narrower. A window
     * manager clamps the request to the hints first, so the client has nothing to correct.
     *
     * <p>Only the fields a frame drag can violate are read: {@code PMinSize}, {@code PMaxSize},
     * {@code PResizeInc}/{@code PBaseSize} and {@code PAspect}. A window with no such property, or one
     * whose format is not 32 bit words, allows anything.
     */
    private record SizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight,
                              int widthInc, int heightInc, int baseWidth, int baseHeight,
                              int aspectX, int aspectY, int aspectMaxX, int aspectMaxY) {

        static final SizeLimits NONE =
                new SizeLimits(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        /** The width the client will accept, nearest to {@code width} and inside its increments. */
        int clampWidth(int width) {
            return snap(Math.max(minWidth, maxWidth > 0 ? Math.min(width, maxWidth) : width),
                    widthInc, baseWidth);
        }

        /** The same for the height. */
        int clampHeight(int height) {
            return snap(Math.max(minHeight, maxHeight > 0 ? Math.min(height, maxHeight) : height),
                    heightInc, baseHeight);
        }

        /** Which dimension drives the other when an aspect ratio is declared, or 0 for none. */
        int aspectDriver() {
            return aspectX > 0 && aspectY > 0 ? 1 : 0;
        }

        /** The height that keeps the declared aspect for {@code width}, when there is one. */
        int heightForWidth(int width) {
            return Math.max(minHeight, (int) Math.round((double) width * aspectY / aspectX));
        }

        /** The width that keeps the declared aspect for {@code height}, when there is one. */
        int widthForHeight(int height) {
            return Math.max(minWidth, (int) Math.round((double) height * aspectX / aspectY));
        }

        private static int snap(int size, int increment, int base) {
            if (increment <= 1) {
                return size;
            }
            int steps = Math.max(0, (size - base) / increment);
            return Math.max(1, base + steps * increment);
        }
    }

    /** The size limits a window's {@code WM_NORMAL_HINTS} declares, or {@link SizeLimits#NONE}. */
    private SizeLimits sizeLimits(XWindow window) {
        int atom = atoms.intern("WM_NORMAL_HINTS", true);
        if (atom == 0) {
            return SizeLimits.NONE;
        }
        XProperty property = window.properties().get(atom);
        if (property == null || property.format() != XProperty.FORMAT_32) {
            return SizeLimits.NONE;
        }
        byte[] data = property.data();
        if (data.length < 18 * 4) {
            return SizeLimits.NONE;
        }
        // Big endian: XProperty keeps every value in wire order whatever the writer's byte order was.
        java.nio.IntBuffer words = java.nio.ByteBuffer.wrap(data)
                .order(java.nio.ByteOrder.BIG_ENDIAN).asIntBuffer();
        int flags = words.get(0);
        int minWidth = (flags & 0x10) != 0 ? words.get(5) : 0;
        int minHeight = (flags & 0x10) != 0 ? words.get(6) : 0;
        int maxWidth = (flags & 0x20) != 0 ? words.get(7) : 0;
        int maxHeight = (flags & 0x20) != 0 ? words.get(8) : 0;
        int widthInc = (flags & 0x40) != 0 ? words.get(9) : 0;
        int heightInc = (flags & 0x40) != 0 ? words.get(10) : 0;
        int baseWidth = (flags & 0x100) != 0 ? words.get(15) : 0;
        int baseHeight = (flags & 0x100) != 0 ? words.get(16) : 0;
        int aspectX = 0;
        int aspectY = 0;
        int aspectMaxX = 0;
        int aspectMaxY = 0;
        if ((flags & 0x80) != 0) {
            aspectX = words.get(11);
            aspectY = words.get(12);
            aspectMaxX = words.get(13);
            aspectMaxY = words.get(14);
        }
        return new SizeLimits(minWidth, minHeight, maxWidth, maxHeight, widthInc, heightInc,
                baseWidth, baseHeight, aspectX, aspectY, aspectMaxX, aspectMaxY);
    }

    /** How much of a window of {@code size} at {@code origin} lies on a screen of {@code screenSize}. */
    private static int visibleExtent(int origin, int size, int screenSize) {
        return Math.max(0, Math.min(screenSize, origin + size) - Math.max(0, origin));
    }

    /**
     * The window size that puts {@code wanted} pixels of a window at {@code origin} on the screen.
     *
     * <p>Not the same number: the part of the window that hangs off the start of the screen is not
     * visible and has to be added back for the visible part to be what was asked for, and the window
     * cannot run past the far edge either. A window twenty pixels down the screen can therefore be
     * made 780 pixels tall where the screen is 800 - the other twenty are under the window's own top
     * edge, which no resize can bring back.
     */
    private static int windowExtent(int wanted, int origin, int screenSize) {
        return Math.min(wanted + Math.max(0, -origin), screenSize - Math.max(0, origin));
    }

    /**
     * Opens a screen's window, sized to show {@code viewport}. Never called with the lock held.
     */
    private void openSessionBackend(ScreenSession session, Rectangle viewport) {
        DisplayBackend window = session.client() == null && sharedBackend != null
                ? sharedBackend
                : backendFactory.get();
        session.setClientKey(clientKey(session));
        Point remembered = rememberedWindowPosition(session);
        window.setViewport(viewport);
        window.start(session.screen(), options.displayNumber(), session.input());
        if (remembered != null) {
            // After start, because a backend has no window to move before it has one. The backend
            // clamps it: a position from a display that is no longer attached is not one this one
            // can show.
            window.setWindowPosition(remembered.x, remembered.y);
        }
        session.setBackend(window);
        session.setViewport(viewport);
        // **The window is told what the session is, before it is given the chance to show itself.**
        // A backend that has nothing to show does not open a window at all (see JavaFxBackend's
        // setIdle): whether there is anything to show is the *session's* answer, and a backend that
        // only hears it on a later tick has already opened one - measured 2026-10-04, the window of a
        // single-window server appeared and vanished as the server started, because the backend's own
        // idea of idle is false until it is told. The session's own bookkeeping is set with it, so the
        // tick does not say the same thing twice.
        window.setIdle(session.idle());
        session.setIdleApplied(session.idle());
        JulLog.info("{}", openedMessage(window, options.displayNumber(), viewport, remembered));
    }

    /**
     * What to say about a backend that has just been given a screen.
     *
     * <p><b>A backend that opens no window has to say so, and this used to be the one line that did
     * not.</b> A server on a machine that cannot show anything logs a warning and falls back to the
     * headless backend - and then, when a client mapped its first window, logged "opened a window for
     * display :95 showing ..." directly underneath it. That sentence is what a user follows to find
     * where their application went, and it named a window that does not exist. Measured on
     * 2026-09-26 with {@code xclock} against a {@code -Djava.awt.headless=true} server: the warning
     * and the false claim, one line apart.
     *
     * <p>A pure function so that it can be pinned without a display or a window: the case that went
     * wrong is exactly the one a test on a machine with a display cannot otherwise reach.
     */
    static String openedMessage(DisplayBackend backend, int
            displayNumber, Rectangle viewport, Point remembered) {
        String where = remembered == null ? "" : " at " + remembered.x + "," + remembered.y;
        if (backend.isHeadless()) {
            return "display :" + displayNumber + " is served by the " + backend.name()
                    + " backend, which opens no window: showing " + viewport + where;
        }
        return "opened a window for display :" + displayNumber + " showing " + viewport + where;
    }

    /**
     * What a client is remembered by, or null when it publishes nothing to remember it by.
     *
     * <p>{@code WM_CLASS} and not the title: a title is what the user is looking at and changes with
     * the document, while the class is the application's own name for itself. It is read off the same
     * window the host window's title comes from - the largest mapped top level - and only at the
     * moment the window opens, which is before any title can have changed it.
     */
    private String clientKey(ScreenSession session) {
        XWindow source = session.identitySource();
        if (source == null) {
            return null;
        }
        int atom = atoms.intern("WM_CLASS", true);
        if (atom == AtomTable.NONE) {
            return null;
        }
        XProperty property = source.properties().get(atom);
        if (property == null || property.data().length == 0) {
            return null;
        }
        // Two NUL separated strings, instance then class, and it is the class that names the
        // application: "navigator", "Navigator" for the same browser, and a second window of the
        // same application is the same window as far as this is concerned.
        String[] parts = new String(property.data(),
                java.nio.charset.StandardCharsets.ISO_8859_1).split("\0");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isEmpty()) {
                return parts[i];
            }
        }
        return null;
    }

    /** Where this session's window was the last time, or null when it has not been seen before. */
    private Point rememberedWindowPosition(ScreenSession session) {
        return session.clientKey() == null ? null : windowStore().position(session.clientKey());
    }

    private synchronized HostWindowStore windowStore() {
        if (windows == null) {
            windows = hostWindowStore != null ? hostWindowStore : HostWindowStore.open();
        }
        return windows;
    }

    /** The store to remember windows in, for a test or an embedder that wants its own. */
    public void setHostWindowStore(HostWindowStore store) {
        this.hostWindowStore = store;
        this.windows = null;
    }

    /**
     * Closes a screen's window. The screen itself was freed when the connection went.
     */
    private void closeSessionBackend(ScreenSession session) {
        DisplayBackend window = session.backend();
        if (window != null) {
            session.setBackend(null);
            window.stop();
        }
    }

    /**
     * Where the user is holding a client's window, written out once it has held still.
     *
     * <p>Written from the tick rather than from the backend that reported the move, for two reasons:
     * the backend is on the toolkit's thread, where writing a file would be work done between the
     * user's drag and the next frame, and a drag reports a position per frame - a file per frame is
     * a file that is never read afterwards. The settle is the same trick the viewport uses.
     */
    private void rememberWindowPosition(ScreenSession session) {
        Point moved = session.input().hostWindowPosition();
        if (moved == null || session.clientKey() == null) {
            return;
        }
        if (session.windowPosition() == null) {
            // The first position a session hears about is where the window opened, not a move: it is
            // the platform choosing a spot, or this server putting the window back where it was last
            // time. Recording it would overwrite what was remembered with the very place the restore
            // was about to change - measured happening, on the first live run.
            session.setWindowPosition(moved);
            return;
        }
        if (!moved.equals(session.windowPosition())) {
            // A position this server has not seen before, which is where a drag starts from.
            session.setWindowPosition(moved);
            session.setPositionHeldSince(System.currentTimeMillis());
            return;
        }
        if (moved.equals(session.persistedWindowPosition()) || session.positionHeldSince() == 0
                || System.currentTimeMillis() - session.positionHeldSince()
                        < WINDOW_POSITION_SETTLE_MILLIS) {
            return;
        }
        // The same position, and it has held still: this is where the user left the window.
        session.setPersistedWindowPosition(moved);
        session.setPositionHeldSince(0);
        windowStore().rememberPosition(session.clientKey(), moved.x, moved.y);
        JulLog.debug("display :{} window for {} is at {},{}", options.displayNumber(),
                session.clientKey(), moved.x, moved.y);
    }

    /**
     * The user closed a window.
     *
     * <p>In single-window mode that is the whole session ending, so every client is asked to quit. A
     * client's own window closing ends that client: it is asked to close through
     * {@code WM_DELETE_WINDOW} and cut off once the grace period is up, while the other clients keep
     * their windows and the server keeps running.
     */
    private void onWindowClosed(ScreenSession session) {
        if (options.detachOnClose()) {
            // The window is gone and the session is not (backlog D1): the clients keep running with
            // nobody showing them, and the control port can put a window back. Only the flags are
            // set here - the tick is what closes the backend, because closing one waits on the
            // toolkit's thread and this is called from the tick with the lock already released.
            detachSession(session);
            JulLog.info("display :{} window closed and --detach-on-close is on: {} client(s) keep"
                            + " running with no window; attach again with the control port",
                    options.displayNumber(), clients.size());
            return;
        }
        if (session.client() == null) {
            askClientsToClose();
            return;
        }
        if (session.closeDeadline() == 0) {
            session.setCloseDeadline(System.currentTimeMillis() + options.closeGraceMillis());
            closeClientWindows(session.screen(), session.client());
            JulLog.info("the window of {} was closed: asked it to quit, {} ms before it is cut off",
                    session.client().peer(), options.closeGraceMillis());
            return;
        }
        if (System.currentTimeMillis() >= session.closeDeadline()) {
            // Closing the connection is what actually ends it: toolkits often ignore the message
            // and hide instead of quitting.
            session.client().close();
        }
    }

    /**
     * A session with no window, and none to be opened until something asks for one (backlog D1).
     *
     * <p>Only the state is set here. Closing the window that may still be up, and opening the next
     * one, are the display tick's work: both reach into a toolkit's own thread, which must not
     * happen while the server lock is held (this is called from a control connection's thread, or
     * from the tick itself after it has released the lock).
     */
    public void detachSession(ScreenSession session) {
        session.setDetached(true);
        // Nothing to bring forward, and a raise that stayed pending would fire at whatever window
        // came back - the trap C2's raise has when there is no window at all.
        session.setRaisePending(false);
        session.setFullFrame(false);
    }

    /** Puts a window back on a detached session, showing the whole of what it has been drawing. */
    public void attachSession(ScreenSession session) {
        if (!session.detached()) {
            return;
        }
        session.setDetached(false);
        // The damage those pixels were drawn with went with the frames that had no window to present
        // it to, so the frame this one comes back with is the whole viewport - see takeFullFrame.
        session.setFullFrame(true);
    }

    /**
     * The port the control commands are answered on, or 0 when the server has none: the bound one,
     * which is what an embedder that asked for port 0 needs, or the configured one.
     */
    public int controlPort() {
        return control == null ? 0 : control.boundPort();
    }

    /**
     * One command from the control port, answered in one line (backlog D1).
     *
     * <p>The commands are the three things a user with no window can want: put a window back
     * ({@code attach}), take the window away without ending the session ({@code detach}), and ask
     * what is going on ({@code status}). They set state and return - the tick is what opens and
     * closes windows - so the answer says what was asked for rather than what has already happened,
     * which is the honest thing for a mechanism whose work happens on the next frame.
     */
    public String controlCommand(String command) {
        String name = command == null ? "" : command.trim().toLowerCase(java.util.Locale.ROOT);
        serverLock.lock();
        try {
            return switch (name) {
                case "attach" -> {
                    int attached = 0;
                    for (ScreenSession session : sessions) {
                        if (session.detached() && canHaveAWindow(session)) {
                            attachSession(session);
                            attached++;
                        }
                    }
                    yield attached == 0
                            ? "nothing to attach: " + clients.size() + " client(s) connected"
                            : "opening a window for " + attached + " session(s); "
                                    + clients.size() + " client(s) connected";
                }
                case "detach" -> {
                    int detached = 0;
                    for (ScreenSession session : sessions) {
                        if (!session.detached() && canHaveAWindow(session)) {
                            detachSession(session);
                            detached++;
                        }
                    }
                    yield detached == 0
                            ? "nothing to detach: " + clients.size() + " client(s) connected"
                            : "closing the window of " + detached + " session(s); "
                                    + clients.size() + " client(s) keep running";
                }
                case "status" -> {
                    int windows = 0;
                    int detached = 0;
                    for (ScreenSession session : sessions) {
                        if (session.backend() != null) {
                            windows++;
                        }
                        if (session.detached()) {
                            detached++;
                        }
                    }
                    yield "display :" + options.displayNumber() + " sessions=" + sessions.size()
                            + " windows=" + windows + " detached=" + detached
                            + " clients=" + clients.size();
                }
                case "" -> "say attach, detach or status";
                default -> "unknown command \"" + name + "\": attach, detach or status";
            };
        } finally {
            serverLock.unlock();
        }
    }

    /**
     * Whether a session is one a window can be opened for at all.
     *
     * <p>The master screen of per-client mode is not: it exists so the tests and single-window mode
     * have a screen to build on, and its clients have windows of their own.
     */
    private boolean canHaveAWindow(ScreenSession session) {
        return session.client() != null || !options.perClientWindows();
    }

    /**
     * Asks every client to close its windows, once, and starts the clock on the grace period.
     */    private void askClientsToClose() {
        if (masterSession.closeDeadline() != 0) {
            return;
        }
        masterSession.setCloseDeadline(System.currentTimeMillis() + options.closeGraceMillis());
        for (ClientConnection client : clients) {
            closeClientWindows(screen, client);
        }
        JulLog.info("the window is closed: asked {} client(s) to close, {} ms before they are cut off",
                clients.size(), options.closeGraceMillis());
    }

    /**
     * Sends {@code WM_DELETE_WINDOW} to a client's windows that asked to be told about it.
     *
     * <p>This is how a window manager asks an application to quit, and the only way one X client can
     * tell another that the session is over: a toolkit repaints nothing and closes nothing unless it
     * is told, so without this the program on the far end of a forwarded display keeps running while
     * the display it was drawing to has gone. A window that never listed the protocol is left out -
     * cutting its connection, which is what happens to it next, is the only notice there is.
     */
    private int closeClientWindows(Screen screen, ClientConnection client) {
        int protocols = atoms.intern("WM_PROTOCOLS", true);
        int deleteWindow = atoms.intern("WM_DELETE_WINDOW", true);
        if (protocols == AtomTable.NONE || deleteWindow == AtomTable.NONE) {
            return 0;
        }
        int asked = 0;
        for (XWindow window : screen.root().children()) {
            if (window.owner() == client && asksToClose(window, protocols, deleteWindow)) {
                JulLog.debug("asking 0x{} to close", Integer.toHexString(window.id()));
                events.send(client, deleteWindowMessage(client, window, protocols, deleteWindow));
                asked++;
            }
        }
        return asked;
    }

    /**
     * Asks every client to close, and gives the ones that were asked the grace period to go.
     *
     * <p>What a server that is stopping owes an application whose display it is: the same courtesy the
     * window-close path gives - {@code WM_DELETE_WINDOW} while there is still a connection to carry
     * it, and only then the cut. Cutting alone leaves an application whose toolkit was never told to
     * quit running on the far machine with a dead display, which is the difference between "the
     * program exited" and "a process is still there" (A27).
     *
     * <p><b>Through {@code clients} and not through the sessions.</b> In single-window mode - which
     * {@code --backend vnc} implies - every client's windows are on the master screen and the master
     * session has no client of its own, so a loop over {@code session.client()} asks **nobody**
     * (measured: {@code --backend vnc} with EasyShell connected sent no ask at all; through the
     * clients it sends four, one per toplevel). {@link #screenOf} is the same rule every other answer
     * to a client uses for finding that client's screen.
     *
     * <p>Nothing is waited for when nobody was asked, and nothing is asked twice: a client that never
     * registered {@code WM_DELETE_WINDOW} has no softer notice than the cut, and one the window-close
     * path has already asked is already inside its own grace.
     */
    private void askClientsToCloseForStop() {
        if (masterSession.closeDeadline() != 0) {
            return;
        }
        int asked = 0;
        for (ClientConnection client : clients) {
            ScreenSession session = client.session();
            if (session != null && session.closeDeadline() != 0) {
                continue;
            }
            asked += closeClientWindows(screenOf(client), client);
        }
        if (asked == 0) {
            return;
        }
        long deadline = System.currentTimeMillis() + options.closeGraceMillis();
        while (!clients.isEmpty() && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * True when a window lists {@code WM_DELETE_WINDOW} in its {@code WM_PROTOCOLS}.
     */
    private static boolean asksToClose(XWindow window, int protocols, int deleteWindow) {
        XProperty property = window.properties().get(protocols);
        if (property == null || property.format() != XProperty.FORMAT_32) {
            return false;
        }
        // Property data is stored in the protocol's canonical big endian order, whatever order the
        // client that set it used.
        byte[] data = property.data();
        for (int offset = 0; offset + 4 <= data.length; offset += 4) {
            int atom = ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16)
                    | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
            if (atom == deleteWindow) {
                return true;
            }
        }
        return false;
    }

    /**
     * The {@code ClientMessage} a window manager sends to ask a window to close: the message type is
     * {@code WM_PROTOCOLS} and its first data item is {@code WM_DELETE_WINDOW}.
     */
    private static RawEvent deleteWindowMessage(ClientConnection client, XWindow window, int protocols,
                                                int deleteWindow) {
        return RawEvent.builder(client, CLIENT_MESSAGE)
                .byteAt(1, 32)
                .card32(4, window.id())
                .card32(8, protocols)
                .card32(12, deleteWindow)
                .build();
    }

    /**
     * The {@code ClientMessage} event code, the one thing about it that is not WM specific.
     */
    private static final int CLIENT_MESSAGE = 33;

    /**
     * Marks the area a sprite covers as needing to be painted again.
     *
     * <p>Nothing in the framebuffer changed there - the sprite is drawn over the frame by the
     * backend - but reusing the damage rectangle is what keeps every backend down to one way of
     * being told what to repaint.
     */
    private static void markSpriteDamage(ScreenSession session, CursorSprite sprite) {
        if (sprite != null) {
            session.screen().framebuffer().markDirty(sprite.x(), sprite.y(),
                    sprite.x() + sprite.width(), sprite.y() + sprite.height());
        }
    }

    /**
     * Whether two sprites would draw the same picture in the same place.
     */
    private static boolean sameSprite(CursorSprite previous, CursorSprite current) {
        if (previous == null || current == null) {
            return previous == current;
        }
        return previous.x() == current.x() && previous.y() == current.y()
                && previous.pixels() == current.pixels();
    }

    public void markScreenDirty() {
        markScreenDirty(screen);
    }

    /**
     * Marks a whole screen dirty, for a change that cannot say which pixels moved.
     *
     * <p>Screens are independent: only the screen a window lives on has to be repainted when it
     * changes, and marking the others would repaint windows that did not move.
     */
    public void markScreenDirty(Screen screen) {
        screen.framebuffer().markAllDirty();
    }

    /**
     * Marks the screen a window lives on.
     */
    public void markScreenDirty(XWindow window) {
        markScreenDirty(window.screen());
    }

    /**
     * Marks the part of a screen that a request drew into, in the drawable's own coordinates.
     *
     * <p>The rectangle comes from the rasterizer, which is the only thing that knows which pixels a
     * request touched; see {@code Rasterizer.drawnBounds}. A request that drew nothing marks
     * nothing, which is why this takes a rectangle rather than a screen.
     */
    public void markScreenDirty(Screen screen, Rectangle drawn) {
        screen.framebuffer().markDirty(drawn.x, drawn.y, drawn.x + drawn.width,
                drawn.y + drawn.height);
    }

    /**
     * Composites a window's own pixels onto the screen, where the window shows (B5).
     *
     * <p>A window whose visual has an alpha channel draws into a store of its own rather than onto
     * the screen, and this is what puts it there: the part of the store the request wrote, composited
     * over what is already on the screen, **clipped to the part of the window that shows** - a covered
     * window goes on drawing into its store, and what it drew appears when something uncovers it
     * ({@code WindowHandlers.repaintUnder} composes it again through the reveal path).
     *
     * @param local the region the request wrote, in the window's own coordinates
     */
    public void compositeOwnPixels(XWindow window, Rectangle local) {
        compositeOwnPixels(window, List.of(local));
    }

    /**
     * The same for a region of the window rather than one rectangle.
     *
     * <p>A move, a shape change and a reveal all hand over a <em>region</em>, and a region can be
     * thousands of rectangles - a shape taken from a mask is one rectangle per row. Intersecting
     * each of them with the window's visible region is a region operation each, which is what made
     * setting a shape on a maximised window take tens of seconds; intersecting them as one region
     * costs one.
     */
    public void compositeOwnPixels(XWindow window, List<Rectangle> local) {
        if (local.isEmpty()) {
            return;
        }
        if (window.isRedirected()) {
            // A redirected window is somebody else's to place (Composite): what it drew stays in its
            // store, which is what `NameWindowPixmap` hands out, and the screen is left alone. The
            // region is reported as damage all the same, and in the window's own coordinates rather
            // than clipped to what shows - a compositor watching a redirected window wants to hear
            // about the pixels it cannot see, because those are the ones it is about to draw.
            for (Rectangle part : local) {
                cn.oyzh.fx.xserver.extension.DamageExtension.drew(this, window, part);
            }
            return;
        }
        if (window.isTransparentUntilDrawn() && window.ownPixelsIfAllocated() == null) {
            // Nothing has been drawn there yet, so there is nothing to put on the screen - and
            // putting the store's initial content there is what the overlay must not do.
            return;
        }
        Screen screen = window.screen();
        cn.oyzh.fx.xserver.draw.Framebuffer target = screen.framebuffer();
        cn.oyzh.fx.xserver.draw.Framebuffer source = window.ownPixels();
        int originX = window.absoluteX();
        int originY = window.absoluteY();
        List<Rectangle> onScreen = new ArrayList<>(local.size());
        for (Rectangle part : local) {
            onScreen.add(new Rectangle(part.x + originX, part.y + originY, part.width,
                    part.height));
        }
        List<Rectangle> visible = cn.oyzh.fx.xserver.resource.ShapeState.intersect(
                onScreen, window.visibleRegion());
        boolean blend = source.keepsAlpha();
        for (Rectangle part : visible) {
            if (blend) {
                for (int y = part.y; y < part.y + part.height; y++) {
                    for (int x = part.x; x < part.x + part.width; x++) {
                        int src = source.pixel(x - originX, y - originY);
                        target.setPixel(x, y, cn.oyzh.fx.xserver.draw.Compositor
                                .compositeOver(src, target.pixel(x, y)));
                    }
                }
            } else {
                // An ordinary window's store is opaque, so there is nothing to blend: its pixels are
                // the screen's pixels wherever it shows, and a straight copy is both the same answer
                // and the only affordable one - every drawing request in the default mode now ends
                // here (B5's general store).
                target.copyFrom(source, part.x - originX, part.y - originY, part.x, part.y,
                        part.width, part.height);
            }
            markScreenDirty(screen, part);
            // The pixels changed, so a client that asked to be told about changes hears it - the same
            // hook every other change to a window's pixels goes through.
            cn.oyzh.fx.xserver.extension.DamageExtension.drew(this, window, part);
        }
    }

    public void stop() {
        running = false;
        // Before anything is torn down: tell the clients, so a toolkit can quit by itself instead of
        // discovering that its display has gone. This is the stop counterpart of the window-close path.
        askClientsToCloseForStop();
        if (clipboard != null) {
            clipboard.stop();
        }
        if (tray != null) {
            tray.stop();
        }
        if (drops != null) {
            drops.stop();
        }
        if (control != null) {
            control.stop();
        }
        if (listener != null) {
            listener.stop();
        }
        for (ClientConnection client : clients) {
            client.close();
        }
        for (ScreenSession session : sessions) {
            closeSessionBackend(session);
        }
        // The JavaFX toolkit holds a non-daemon thread, so something has to end it - but only once
        // no window is left, which is now.
        DisplayBackend window = sharedBackend;
        if (window != null) {
            window.stop();
            window.releaseToolkit();
        }
    }

    // ------------------------------------------------------------------
    // Request execution
    // ------------------------------------------------------------------

    /**
     * Decodes and runs one request. Exceptions carrying a protocol error are turned into error
     * replies; the connection survives them, exactly as clients expect.
     */
    public void executeRequest(ClientConnection client, byte opcode, byte minorOpcode, X11Input in,
                               int requestBytes, byte[] request) {
        serverLock.lock();
        try {
            if (client.isShuttingDown()) {
                return;
            }
            // What a recorded message is attributed to: the request being executed now.
            currentRequestClient = client;
            currentRequestSequence = client.sequenceNumber();
            if (options.logRequests()) {
                JulLog.debug("{} opcode {} minor {}", client, opcode & 0xFF, minorOpcode & 0xFF);
            }
            dispatcher.dispatch(client, opcode, minorOpcode, in, requestBytes, request);
        } catch (ProtocolException e) {
            JulLog.info("{}: {} rejected the request (error {}): {}", client,
                    dispatcher.currentRequest(), e.errorCode(), e.getMessage());
            client.sendError(e.errorCode(), e.badValue());
        } catch (RuntimeException e) {
            JulLog.error(client + " opcode " + (opcode & 0xFF) + " failed", e);
            client.sendError(ProtocolException.IMPLEMENTATION, 0);
        } finally {
            currentRequestClient = null;
            currentRequestSequence = 0;
            serverLock.unlock();
        }
    }

    // ------------------------------------------------------------------
    // Clients
    // ------------------------------------------------------------------

    public void registerClient(ClientConnection client) {
        clients.add(client);
        JulLog.info("client {} connected", client.peer());
    }

    public void removeClient(ClientConnection client) {
        boolean registered = clients.remove(client);
        ScreenSession session = client.session();
        serverLock.lock();
        try {
            usedClientIndices.clear(client.index());
            if (session != null) {
                session.input().clientDisconnected(client);
            }
            input.clientDisconnected(client);
            if (tray != null) {
                tray.clientDisconnected(client);
            }
            // Windows owned by this client get destroyed, which generates destroy notifications.
            dispatcher.destroyClientResources(client);
            // The departure comes after everything the client's own requests caused, which is where a
            // recording has it: the last request's notifications, then the client-died element.
            recordClientDied(client);
        } finally {
            // A client that grabbed the server will never send UngrabServer, so drop every hold it
            // accumulated or the remaining clients would block forever.
            clearServerGrab(client);
            releaseAllLocks();
        }
        // A connection can end before it was ever registered - a handshake that failed after the
        // screen was made - so the screen is freed whether or not the client was in the list.
        closeClientScreen(session);
        if (registered) {
            JulLog.info("client {} disconnected", client.peer());
        }
    }

    /**
     * Reserves a client index, or -1 when the server is full.
     *
     * <p>Two ceilings, and the lower one is the caller's: the index space itself (256 slots, what the
     * top bits of a resource id can name) and {@code --max-clients}, which is the one a user sets and
     * which was parsed and documented but had no reader at all - so a {@code --max-clients 2} server
     * accepted four connections when measured. The count is of *allocated* slots rather than of
     * registered clients, because a slot is taken from the moment a connection is accepted and a
     * connection that is still in its setup handshake has a resource id space reserved for it.
     */
    public synchronized int allocateClientIndex() {
        if (usedClientIndices.cardinality() - 1 >= Math.max(1, options.maxClients())) {
            return -1;
        }
        int index = usedClientIndices.nextClearBit(1);
        if (index >= ClientConnection.MAX_CLIENTS) {
            return -1;
        }
        usedClientIndices.set(index);
        return index;
    }

    public void releaseClientIndex(int index) {
        if (index > SERVER_CLIENT_INDEX) {
            usedClientIndices.clear(index);
        }
    }

    /**
     * Allocates a server owned resource id, used for predefined fonts and similar.
     */
    public synchronized int nextServerResourceId() {
        return resourceIdCounter++;
    }
}
