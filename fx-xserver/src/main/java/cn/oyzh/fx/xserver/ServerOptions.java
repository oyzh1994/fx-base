package cn.oyzh.fx.xserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Command line options for the server. Parsed in X-server style so that the usual
 * {@code --display :1 -screen 0 1280x800x24} invocation works.
 */
public final class ServerOptions {

    /** Which rendering surface to open. */
    public enum Backend {
        /** JavaFX window with a software framebuffer blitted into a {@code WritableImage}. */
        JAVAFX,
        /** Swing window with a {@code BufferedImage}. Used when JavaFX cannot start. */
        SWING,
        /** No window at all: the framebuffer still exists and clients can connect. */
        HEADLESS,
        /**
         * No window on this machine either: the screen is served to VNC clients instead
         * (backlog D2). The framebuffer, the clients and the input all work exactly as they do
         * under the other backends - what changes is who is looking and how they type.
         *
         * <p>It implies {@code --single-window}: a viewer watches one screen, and one port per
         * client connection is not what a VNC client can be pointed at.
         */
        VNC;

        public static Backend parse(String value) {
            return switch (value.toLowerCase()) {
                case "javafx", "fx" -> JAVAFX;
                case "swing", "awt" -> SWING;
                case "headless", "none", "xvfb" -> HEADLESS;
                case "vnc", "rfb" -> VNC;
                default -> throw new IllegalArgumentException(
                        "unknown backend '" + value + "', expected one of javafx|swing|headless|vnc");
            };
        }
    }

    private int displayNumber = 1;
    private int width = 1280;
    private int height = 800;
    /** True once the client asked for a size, so the default can be fitted to the display. */
    private boolean geometrySpecified;
    private int depth = 24;
    private Backend backend = Backend.JAVAFX;
    private boolean tcp = true;
    private boolean unixSocket = true;
    private boolean listenAll = true;
    private int maxClients = 256;
    private boolean authRequired;
    private File authFile;
    private String title = "cn/oyzh/fx/xserver";
    private boolean logRequests;
    private boolean noResetOnLastClose = true;
    private int tickRate = 60;
    /** True when every client shares one window instead of getting one of its own. */
    private boolean singleWindow;

    /**
     * Whether a click moves the input focus to the window it lands in - the window-manager half, **on
     * by default**.
     *
     * <p>It is on because this server is the window manager for what it forwards - it owns the host
     * window, and a forwarded application draws its own title bar and its own menus - and because the
     * half it adds is the one a desktop gives every application: an application whose page or dialog
     * takes the focus and hands it to nobody when it closes is left with the focus on the root window,
     * and a toolkit hides the popup of a window it believes is unfocused, so its context menus stop
     * opening (measured 2026-09-29, and *identical on a reference server*: the application's
     * behaviour, and this is the switch that gives it what a desktop would). With it on, the click
     * that lands in the application's window focuses it again.
     *
     * <p>**A click in an override-redirect window is left alone**, which is the other half of the same
     * rule and not a detail: a window manager never gives the input focus to a menu or a tooltip, and
     * moving it there takes the focus away from the window the popup was opened from. A toolkit hides
     * a popup whose owner it believes unfocused, so the click that was meant to activate an item
     * instead tore the menu down - measured on EasyShell, whose input box context menu opened, took
     * the focus on the press, and was unmapped three milliseconds later, every item dead to the mouse
     * while the keyboard still worked (A23, 2026-09-30).
     *
     * <p>**`--no-focus-on-click` is the comparison switch**, the way `--no-glx` is: a reference server
     * does not focus a window on a click, so a measurement that has to be byte-identical to one wants
     * this off - the sweep of probes and any A/B whose gesture presses a button.
     */
    private boolean focusOnClick = true;
    /**
     * Whether GLX is advertised, which it is **by default** - a client that wants OpenGL needs it, and
     * there is nothing measured that it costs one that does not. That is the other way round from how
     * this started, and the reversal is itself a measurement:
     *
     * <p>It was opt-in for a day on a report that a forwarded JavaFX application lost its animations
     * with GLX advertised - a menu item with no hover colour and a caret that stopped blinking. A17
     * chased that to the end and **closed it**: the caret blinks on both builds, and the hover issues
     * *no drawing request at all* - with or without GLX, the client chooses not to repaint, so the
     * server had nothing to fail at. What is left of the difference is that a toolkit which finds GLX
     * initialises a GL path: JavaFX creates a context, finds no GPU that meets its requirement and
     * falls back to the same software pipeline it would have used anyway (measured with
     * {@code prism.verbose}), so GLX buys a JavaFX client nothing and costs it one context - while it
     * is the *only* way a real GL client can render here at all.
     *
     * <p>**Compatibility, not speed**: this server has no renderer, so a GL client renders in software
     * and blits its frames as ordinary {@code PutImage} requests (B6 measured it - 769 MB in 5.6 s
     * from {@code glxgears}, against 2832 FPS on a reference with a GPU behind it). {@code --no-glx}
     * turns the extension off, which is what comparing two builds wants.
     */
    private boolean glx = true;
    /**
     * How large a screen may be grown to. The setup reply's width and height are sixteen bit
     * fields, so a screen beyond 65535 could not be described to a client at all; the default is a
     * picture the size of a large desktop rather than that hard ceiling, and a framebuffer of
     * 8192x8192 is already 256 MB.
     */
    private int maxScreenWidth = 8192;
    private int maxScreenHeight = 8192;

    /**
     * Where the host's paths are on the client's filesystem, as {@code hostPrefix=clientPrefix} pairs.
     *
     * <p><b>A drop hands the application a path, and XDND cannot hand it anything else</b> - the
     * target opens the file, it does not download it (measured: a real application answers the drag
     * and then does nothing, because the path it was given is on the other machine). A user whose
     * client can see the host's files - a shared folder, an sshfs or NFS mount - is one mapping away
     * from a drag that works, and without a mapping the path is reported as it is.
     */
    private final List<String[]> dropMounts = new ArrayList<>();
    /** Whether the host clipboard is bridged to the X CLIPBOARD selection. */
    private boolean clipboard = true;

    /** Whether the notification area is served (C9). */
    private boolean tray = true;
    /**
     * How many framebuffer pixels one logical pixel is worth.
     *
     * <p>A client cannot draw more detail than the screen it is told about, so asking for a sharp
     * picture on a HiDPI display means telling it about a bigger screen: at 2, {@code --geometry}
     * stays the size the window looks and the screen, the framebuffer and the client's own windows
     * are all twice that in pixels, which is what a client that scales itself by the reported
     * resolution will render into. A client that ignores the resolution simply gets a screen twice
     * the size and is laid out the same way as before, so this is off by default.
     */
    private int scale = 1;
    /** How long clients get to close themselves after the host window is closed. */
    private int closeGraceMillis = 2000;

    /**
     * Whether closing the host window ends the session or only its window (backlog D1).
     *
     * <p>Off by default, which is what this server has always done and what a user who closes a
     * forwarded application's window expects: the application was in that window. On, the clients
     * keep running with no window and {@code --control-port} can bring one back.
     */
    private boolean detachOnClose;

    /**
     * The loopback port that answers {@code attach}, {@code detach} and {@code status}: -1 for no
     * control port (the default), 0 for any free one, which is what a test or an embedder wants.
     * Loopback only: it is a way for the user to say something to a server they started, not a
     * service.
     */
    private int controlPort = -1;

    /**
     * The port the {@code vnc} backend listens on. 5900 is the registered one, so a client can be
     * pointed at a machine and nothing else; 0 asks for any free port, which is what a test wants.
     */
    private int vncPort = 5900;

    /**
     * The password a VNC client must type, read from a file, or null for none (backlog D2).
     *
     * <p>A file rather than the command line: a password in an argument is a password in
     * {@code ps}. With none, the server offers RFB's security type "None" and any client that
     * accepts it is in - which is what every VNC client but Apple's does, and what a loopback
     * server wants. Apple's Screen Sharing insists on a password, which is why this exists.
     */
    private File vncPasswordFile;

    /**
     * The same password, given as a value rather than read from a file.
     *
     * <p>For an embedder: a program that starts this server as a library has the password in hand
     * already and has nowhere to write it, and the file form exists for a *shell*, where an argument
     * is an argument everyone on the machine can read with {@code ps}. Both forms are the same
     * password to the VNC backend; giving both at once is refused rather than silently preferring
     * one, because "which of these two did it use" is exactly what a user should never have to ask.
     */
    private String vncPasswordValue;

    /**
     * The port the browser path listens on: the same RFB sessions carried inside WebSocket frames,
     * or -1 for none (backlog D2). A page cannot open a TCP socket, so this is what lets noVNC - or
     * anything else that speaks RFB over a WebSocket - reach the display with nothing installed.
     */
    private int vncWebSocketPort = -1;

    private void parseDropMount(String value) {
        int equals = value.indexOf('=');
        if (equals <= 0 || equals == value.length() - 1) {
            throw new IllegalArgumentException(
                    "--drop-mount wants hostPrefix=clientPrefix, got " + value);
        }
        dropMounts.add(new String[] {value.substring(0, equals), value.substring(equals + 1)});
    }

    /** The host-to-client path mappings for a drop, in the order they were given. */
    public List<String[]> dropMounts() {
        return List.copyOf(dropMounts);
    }

    public static ServerOptions parse(String[] args) {
        ServerOptions options = new ServerOptions();
        List<String> positional = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-help", "--help", "-h" -> {
                    printUsage();
//                    System.exit(0);
                }
                case "--display", "-display" -> options.displayNumber = parseDisplay(next(args, ++i));
                case "--geometry", "-geometry" -> options.parseGeometry(next(args, ++i));
                case "--backend" -> options.backend = Backend.parse(next(args, ++i));
                case "--title" -> options.title = next(args, ++i);
                case "--max-clients" -> options.maxClients = Integer.parseInt(next(args, ++i));
                case "--auth" -> {
                    options.authRequired = true;
                    String value = next(args, ++i);
                    if (!"true".equalsIgnoreCase(value) && !"yes".equalsIgnoreCase(value)) {
                        options.authFile = new File(value);
                    }
                }
                case "--no-auth" -> options.authRequired = false;
                case "--no-tcp" -> options.tcp = false;
                case "--no-unix" -> options.unixSocket = false;
                case "--local" -> options.listenAll = false;
                case "--log-requests" -> options.logRequests = true;
                case "--tick-rate" -> options.tickRate = Integer.parseInt(next(args, ++i));
                case "--close-grace-ms" -> options.closeGraceMillis =
                        Integer.parseInt(next(args, ++i));
                case "--detach-on-close" -> options.detachOnClose = true;
                case "--vnc-port" -> options.vncPort = Integer.parseInt(next(args, ++i));
                case "--vnc-password-file" -> options.vncPasswordFile = new File(next(args, ++i));
                case "--vnc-password" -> options.vncPasswordValue = next(args, ++i);
                case "--vnc-ws-port" -> options.vncWebSocketPort =
                        Integer.parseInt(next(args, ++i));
                case "--control-port" -> options.controlPort = Integer.parseInt(next(args, ++i));
                case "--single-window" -> options.singleWindow = true;
                case "--focus-on-click" -> options.focusOnClick = true;
                case "--no-focus-on-click" -> options.focusOnClick = false;
                case "--max-screen" -> options.parseMaxScreen(next(args, ++i));
                case "--drop-mount" -> options.parseDropMount(next(args, ++i));
                case "--no-clipboard" -> options.clipboard = false;
                case "--no-tray" -> options.tray = false;
                case "--glx" -> options.glx = true;
                case "--no-glx" -> options.glx = false;
                case "--scale" -> options.scale = parseScale(next(args, ++i));
                case "-screen" -> {
                    // consumes "-screen 0 WxHxD" but we only ever expose a single screen
                    i += 2;
                    if (i + 1 < args.length) {
                        options.parseGeometry(args[i + 1]);
                        i++;
                    }
                }
                default -> positional.add(arg);
            }
        }
        // bare form: "xserver :2"
        for (String value : positional) {
            if (value.startsWith(":")) {
                options.displayNumber = parseDisplay(value);
            }
        }
        if (options.backend == Backend.VNC) {
            // One screen, watched by whoever connects: the backend serves the whole framebuffer, and
            // a screen per client - with a VNC port per client - is not a thing a viewer can use.
            options.singleWindow = true;
        }
        if (options.vncPasswordValue != null && options.vncPasswordFile != null) {
            // Two ways to say the same thing, so which one was meant is a question with no answer -
            // refused here, where the message can name both options, rather than silently preferring
            // one and leaving the other as a line in a configuration that does nothing.
            throw new IllegalArgumentException("--vnc-password and --vnc-password-file are the same"
                    + " password two ways: give one of them");
        }
        return options;
    }

    /** Accepts ":1", "1", ":1.0", "host:1". */
    static int parseDisplay(String value) {
        int colon = value.indexOf(':');
        String rest = colon >= 0 ? value.substring(colon + 1) : value;
        int dot = rest.indexOf('.');
        if (dot >= 0) {
            rest = rest.substring(0, dot);
        }
        try {
            return Integer.parseInt(rest.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid display '" + value + "'", e);
        }
    }

    /** Accepts "1280x800" or "1280x800x24". */
    void parseGeometry(String value) {
        String[] parts = value.toLowerCase().split("x");
        if (parts.length < 2) {
            throw new IllegalArgumentException("invalid geometry '" + value + "', expected WxH or WxHxD");
        }
        width = Integer.parseInt(parts[0].trim());
        height = Integer.parseInt(parts[1].trim());
        if (parts.length >= 3) {
            depth = Integer.parseInt(parts[2].trim());
        }
        geometrySpecified = true;
    }

    private static String next(String[] args, int index) {
        if (index >= args.length) {
            throw new IllegalArgumentException("missing value for " + args[index - 1]);
        }
        return args[index];
    }

    private static void printUsage() {
        System.out.println("""
                xserver - a modern X11 display server

                Usage: xserver [display] [options]

                  --display :N          display number (default :1)
                  --geometry WxH[xD]    root window size (default 1280x800x24)
                  --backend NAME        javafx | swing | headless | vnc (default javafx)
                  --title TITLE         window title
                  --max-clients N       maximum simultaneous clients (default 256)
                  --auth [FILE]         require MIT-MAGIC-COOKIE-1 using ~/.Xauthority
                  --no-auth             disable access control (default)
                  --no-tcp              do not listen on TCP port 6000+N
                  --no-unix             do not listen on /tmp/.X11-unix/XN
                  --local               only listen on loopback
                  --log-requests        log every request
                  --focus-on-click      a click moves the input focus to the window it lands in, the
                                        way a window manager does it - an override-redirect window
                                        (a menu) excepted, since focusing one tears the menu down.
                                        **On by default**, because this server is the window manager
                                        for what it forwards and it is what an application whose page
                                        leaves the focus nowhere needs for its menus to open;
                                        --no-focus-on-click is the comparison switch, since a
                                        reference does not do it
                  --glx / --no-glx      advertise GLX, which it does by default: a GL client needs
                                        it, and a toolkit that does not use it only pays one
                                        abandoned context (--no-glx is the comparison switch)
                  --tick-rate N         screen refresh rate (default 60)
                  --close-grace-ms N    time clients get to close themselves once the window is
                                        closed, before the server disconnects them (default 2000)
                  --detach-on-close     closing the window leaves the clients running with no
                                        window instead of ending the session; bring one back with
                                        --control-port
                  --control-port N      listen on 127.0.0.1:N for the commands attach, detach and
                                        status (default: no control port)
                  --vnc-port N          the port --backend vnc serves on, 0 for any free one
                                        (default 5900). vnc serves the whole screen, so it implies
                                        --single-window
                  --vnc-password VALUE  require VALUE as the password from VNC clients. This is the
                                        form for a program that starts this server as a library;
                                        **VALUE is visible to anyone who can run `ps`**, which is
                                        why the file form exists for a shell
                  --vnc-password-file F  require the password in FILE (one line) from VNC clients.
                                        Give one of the two, not both; without either the display is
                                        served to anyone who connects
                  --vnc-ws-port N       also serve the same VNC sessions over WebSocket on port N,
                                        so a browser (noVNC) can connect with nothing installed
                                        (default: no browser path)
                  --single-window       render every client into one window, as a plain X screen
                                        (default: one window per client connection)
                  --scale N             put N framebuffer pixels in each logical pixel, for a
                                        sharp picture on a HiDPI display (default 1). --geometry
                                        stays the size the window looks; the client is told about
                                        a screen N times that in pixels and N times the
                                        resolution, and draws at it if it scales itself by the
                                        resolution. An application that ignores it (compose
                                        desktop is one) can be told: -Dsun.java2d.uiScale=N

                The display is also reported through DISPLAY, so clients can be started with
                  DISPLAY=:1 xeyes
                """);
    }

    /**
     * Shrinks the default screen to fit the display it will be shown on.
     *
     * <p>A 1280x800 screen is wider than a laptop panel once the dock and menu bar are taken out,
     * and a window larger than the display simply runs off the edge with no way to scroll to it.
     * An explicitly requested size is left alone: the caller may know better, and a screen that
     * does not fit on the host is a legitimate thing to ask for.
     */
    public void fitToDisplay() {
        if (geometrySpecified) {
            return;
        }
        try {
            var bounds = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getMaximumWindowBounds();
            if (bounds.width > 0 && bounds.height > 0
                    && (bounds.width < width || bounds.height < height)) {
                width = Math.min(width, bounds.width);
                height = Math.min(height, bounds.height);
            }
        } catch (Throwable e) {
            // Headless, or no display information: keep the requested size.
        }
    }

    /** True when the caller asked for an explicit size. */
    /** Whether a click moves the input focus to the window it lands in (on unless disabled; see the field). */
    public boolean focusOnClick() {
        return focusOnClick;
    }

    public boolean isGeometrySpecified() {
        return geometrySpecified;
    }

    public int displayNumber() {
        return displayNumber;
    }

    public int tcpPort() {
        return 6000 + displayNumber;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int depth() {
        return depth;
    }

    public Backend backend() {
        return backend;
    }

    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    public boolean tcp() {
        return tcp;
    }

    public boolean unixSocket() {
        return unixSocket;
    }

    public boolean listenAll() {
        return listenAll;
    }

    public int maxClients() {
        return maxClients;
    }

    public boolean authRequired() {
        return authRequired;
    }

    public File authFile() {
        return authFile;
    }

    public String title() {
        return title;
    }

    public boolean logRequests() {
        return logRequests;
    }

    public boolean resetOnLastClose() {
        return !noResetOnLastClose;
    }

    public int tickRate() {
        return tickRate;
    }

    /**
     * True when every client connection gets a screen and a window of its own, which is the default:
     * a forwarded application then appears as its own window, sized to what it draws.
     */
    /** The largest a screen may be grown to, which a drag past the screen is capped at. */
    public int maxScreenWidth() {
        return maxScreenWidth;
    }

    public int maxScreenHeight() {
        return maxScreenHeight;
    }

    private void parseMaxScreen(String value) {
        String[] parts = value.toLowerCase(java.util.Locale.ROOT).split("x");
        if (parts.length != 2) {
            throw new IllegalArgumentException("--max-screen wants WxH, not " + value);
        }
        maxScreenWidth = Math.min(65535, Integer.parseInt(parts[0].trim()));
        maxScreenHeight = Math.min(65535, Integer.parseInt(parts[1].trim()));
    }

    /** Whether the host clipboard should be bridged to the X CLIPBOARD selection. */
    public boolean noClipboard() {
        return !clipboard;
    }

    /** Whether the notification area is served - the tray a forwarded application's icon docks in. */
    public boolean noTray() {
        return !tray;
    }

    public boolean perClientWindows() {
        return !singleWindow;
    }

    /** Whether GLX is advertised; on unless {@code --no-glx} turned it off. See the field. */
    public boolean glx() {
        return glx;
    }

    /** How many framebuffer pixels a logical pixel is. */
    public int scale() {
        return scale;
    }

    /**
     * The host's own scale factor: 2 on a Retina panel, 1 nearly everywhere else.
     *
     * <p>Read from the platform's default transform, which is the one piece of HiDPI information the
     * JDK gives up without a toolkit - JavaFX would say the same thing through
     * {@code Screen.getPrimary().getPlatformScaleX()}, and AWT is what this class already asks about
     * the display. {@code --scale auto} is this number.
     *
     * <p>It is what makes the picture sharp, and only half of what a forwarded application needs:
     * the server draws the screen at this many pixels per logical pixel, and a client that draws its
     * interface at 1x then lays itself out for a screen half the size it was given. A client that
     * reads {@code Xft.dpi} off the root window follows by itself; one that does not has to be
     * started with its own scale setting.
     */
    public static int hostScale() {
        try {
            return Math.max(1, (int) Math.round(java.awt.GraphicsEnvironment
                    .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration().getDefaultTransform().getScaleX()));
        } catch (Throwable e) {
            // Headless, or a platform that will not say. 1 is the size everything assumes.
            return 1;
        }
    }

    /** Screen width in framebuffer pixels, which is what a client is told and draws into. */
    public int pixelWidth() {
        return width * scale;
    }

    public int pixelHeight() {
        return height * scale;
    }

    /**
     * Keeps the scale to whole numbers a display can actually be: a fractional one would leave the
     * framebuffer and the window unable to line up pixel for pixel, which is the only thing this
     * option is for.
     */
    private static int parseScale(String value) {
        if ("auto".equalsIgnoreCase(value.trim())) {
            // Resolved here rather than kept as a flag: it is one question to the platform, and
            // every reader of scale() then has a real number to work with.
            return hostScale();
        }
        int scale;
        try {
            scale = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "scale must be a whole number or 'auto', not '" + value + "'");
        }
        if (scale < 1 || scale > 4) {
            throw new IllegalArgumentException("scale must be between 1 and 4, not " + scale);
        }
        return scale;
    }

    /** How long a client has to close its windows itself once the host window has closed. */
    public int closeGraceMillis() {
        return closeGraceMillis;
    }

    /** Whether closing the host window leaves the clients running instead of ending the session. */
    public boolean detachOnClose() {
        return detachOnClose;
    }

    /** The loopback port the control commands are answered on, or 0 when there is none. */
    public int controlPort() {
        return controlPort;
    }

    /** The port the VNC backend serves on, or 0 for any free one. */
    public int vncPort() {
        return vncPort;
    }

    /** The port the browser path serves on, or -1 when there is none. */
    public int vncWebSocketPort() {
        return vncWebSocketPort;
    }

    /**
     * The VNC password a client must type - the value that was given, or the one line in the file -
     * or null when the display is served to anyone who connects.
     *
     * <p>The file is read here rather than at parse time so that a server which never builds a VNC
     * backend never touches it; the value is taken as given and only checked to be non-empty, which is
     * the one mistake that would otherwise turn into a display a *known* key opens (RFB derives a DES
     * key from the password, and a zero key is weak).
     */
    public String vncPassword() {
        if (vncPasswordValue != null) {
            if (vncPasswordValue.isEmpty()) {
                throw new IllegalArgumentException("--vnc-password is empty: give a password, or"
                        + " leave the option out to serve the display to anyone who connects");
            }
            return vncPasswordValue;
        }
        if (vncPasswordFile == null) {
            return null;
        }
        try {
            String password = new String(java.nio.file.Files.readAllBytes(vncPasswordFile.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8).strip();
            if (password.isEmpty()) {
                // A zero password is a DES key of zeros, which is a weak key: the honest answer is to
                // refuse rather than to serve a display that a known key opens.
                throw new IllegalArgumentException("the VNC password file " + vncPasswordFile
                        + " is empty");
            }
            return password;
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("cannot read the VNC password file "
                    + vncPasswordFile + ": " + e.getMessage(), e);
        }
    }
}
