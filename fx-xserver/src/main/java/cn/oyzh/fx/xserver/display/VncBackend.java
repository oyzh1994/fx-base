package cn.oyzh.fx.xserver.display;

import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.display.RfbSession;
import cn.oyzh.common.log.JulLog;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shows the screen to another machine, as a VNC server (backlog D2).
 *
 * <p>The fourth backend, and the odd one out: the other three present the framebuffer to a window on
 * <em>this</em> machine, and this one presents it to whoever asks over the network. It is a backend
 * rather than a feature bolted onto one because the contract already fits - the display tick calls
 * {@link #present} with the rectangle that changed, which is exactly what an RFB update is - and
 * because everything else the server does (the screen, the windows, the input state, the damage
 * rectangles) is then shared with it for nothing.
 *
 * <p><b>There is no window, so nothing here can be closed.</b> {@link #isClosed()} answers no for
 * ever and {@link #isHeadless()} yes: the server runs until it is stopped, which is also what makes
 * the VNC backend compose with {@code --detach-on-close} (D1) without meaning anything different -
 * a server with no window at all is what detaching produces.
 *
 * <p><b>A slow client is that client's own problem.</b> {@link #present} is called from the display
 * tick and does nothing but add a rectangle to each session's pending damage; the socket is written
 * from the session's own thread, and only when that client asks for an update. That is the RFB
 * protocol's own backpressure - and this server's rule (E5) that no other client waits for a slow
 * one - arrived at from two directions at once.
 */
public final class VncBackend implements DisplayBackend {


    private final int port;
    /** The port the browser path listens on (a WebSocket carrying RFB), or -1 for none. */
    private final int webSocketPort;
    private final String title;
    private final boolean loopbackOnly;
    /** The password a client must type, or null when the display is served to anyone. */
    private final String password;

    private final List<RfbSession> sessions = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean();

    private ServerSocket socket;
    /** The browser path, or null when the server was not asked for one. */
    private cn.oyzh.fx.xserver.net.WebSocketListener websocket;
    private Screen screen;
    private InputSink input;
    private volatile int boundPort;
    /** The X display this serves, for the name a viewer shows in its title bar. */
    private volatile int displayNumber;

    /**
     * Where a viewer's clipboard text goes - the server's clipboard bridge - or null when there is
     * no bridge (no host pasteboard, or {@code --no-clipboard}). Set at startup, after the bridge
     * exists; a session reads it when a client sends text, so a viewer that connects before the
     * wiring is done still finds it.
     */
    private volatile java.util.function.Consumer<String> clipboardSink;

    /**
     * @param port          the TCP port to listen on, or 0 for any free one (which a test wants)
     * @param webSocketPort the port a browser connects to, or -1 for no browser path
     * @param title         what the client's window will say it is connected to
     * @param loopbackOnly  true when the server was told to listen on loopback only ({@code --local})
     * @param password      the password a client must type, or null for none
     */
    public VncBackend(int port, int webSocketPort, String title, boolean loopbackOnly,
                      String password) {
        this.port = port;
        this.webSocketPort = webSocketPort;
        this.title = title;
        this.loopbackOnly = loopbackOnly;
        this.password = password;
    }

    @Override
    public String name() {
        return "vnc";
    }

    @Override
    public boolean isHeadless() {
        // No window is opened on this machine; the screen goes over the network instead.
        return true;
    }

    @Override
    public void start(Screen screen, int displayNumber, InputSink input) {
        this.screen = screen;
        this.input = input;
        this.displayNumber = displayNumber;
        try {
            ServerSocket bound = new ServerSocket();
            bound.setReuseAddress(true);
            bound.bind(new InetSocketAddress(loopbackOnly ? InetAddress.getLoopbackAddress() : null,
                    port));
            socket = bound;
            boundPort = bound.getLocalPort();
            running.set(true);
            Thread accept = new Thread(this::accept, "xserver-vnc");
            accept.setDaemon(true);
            accept.start();
            if (webSocketPort >= 0) {
                // The browser path (D2): the same sessions, carried inside WebSocket frames, so a
                // page can be pointed straight at this server - no bridge process in between.
                websocket = new cn.oyzh.fx.xserver.net.WebSocketListener(webSocketPort,
                        loopbackOnly, this::acceptWebSocket);
                websocket.start();
                JulLog.info("VNC over WebSocket on port {}: point noVNC at ws://<this machine>:{}/ and"
                        + " it needs nothing installed on the client machine", websocket.boundPort(),
                        websocket.boundPort());
            }
            JulLog.info("VNC on port {}: connect with vnc://<this machine>:{} - {}",
                    boundPort, boundPort, password == null
                            ? "the display is served to anyone who connects, so this is for a"
                                    + " network you trust (--vnc-password asks for a password)"
                            : "clients are asked for a password (--vnc-password, or the file form"
                                    + " --vnc-password-file)");
        } catch (IOException e) {
            running.set(false);
            throw new IllegalStateException("cannot listen for VNC on port " + port + ": "
                    + e.getMessage(), e);
        }
    }

    /** The port actually bound, which is what a caller that asked for 0 needs to know. */
    public int boundPort() {
        return boundPort;
    }

    /** The browser path's port, or 0 when there is none. */
    public int boundWebSocketPort() {
        cn.oyzh.fx.xserver.net.WebSocketListener browser = websocket;
        return browser == null ? 0 : browser.boundPort();
    }

    /** Where a viewer's clipboard text is handed; see {@link #clipboardSink}. */
    public void setClipboardSink(java.util.function.Consumer<String> sink) {
        this.clipboardSink = sink;
    }

    java.util.function.Consumer<String> clipboardSink() {
        return clipboardSink;
    }

    /**
     * The clipboard's text changed, from a client's copy or from the Mac's. Every viewer is told.
     *
     * <p>Called from the clipboard bridge under the <b>server lock</b>, so this only hands each
     * session the text - the session's own thread does the writing, which is the whole shape of
     * this backend: nobody waits for a viewer.
     */
    public void clipboardTextChanged(String text) {
        for (RfbSession session : sessions) {
            session.setClipboardText(text);
        }
    }

    @Override
    public void present(Screen screen, Rectangle damage, CursorSprite cursor) {
        if (damage == null || sessions.isEmpty()) {
            return;
        }
        for (RfbSession session : sessions) {
            session.addDamage(damage);
        }
    }

    @Override
    public void presentCursor(CursorSprite cursor) {
        if (cursor == null || sessions.isEmpty()) {
            return;
        }
        for (RfbSession session : sessions) {
            // A viewer that asked for the Cursor pseudo-encoding gets the X session's pointer rather
            // than drawing one of its own, and the session decides whether it has sent this one.
            session.setCursor(cursor);
        }
    }

    @Override
    public void screenResized(Screen screen) {
        for (RfbSession session : sessions) {
            session.screenResized();
        }
    }

    @Override
    public void stop() {
        running.set(false);
        cn.oyzh.fx.xserver.net.WebSocketListener browser = websocket;
        if (browser != null) {
            browser.stop();
        }
        ServerSocket bound = socket;
        if (bound != null) {
            try {
                bound.close();
            } catch (IOException e) {
                JulLog.debug("closing the VNC listener", e);
            }
        }
        for (RfbSession session : sessions) {
            session.close();
        }
        sessions.clear();
    }

    @Override
    public boolean isClosed() {
        // Nothing a user can close: the server keeps running until it is stopped.
        return false;
    }

    private void accept() {
        while (running.get()) {
            try {
                SocketTransport transport = new SocketTransport(socket.accept());
                Thread thread = new Thread(() -> serve(transport), "xserver-vnc-client");
                thread.setDaemon(true);
                thread.start();
            } catch (IOException e) {
                if (running.get()) {
                    JulLog.debug("accepting a VNC client failed", e);
                }
            }
        }
    }

    /**
     * A browser that finished its WebSocket upgrade, served exactly like a socket client.
     *
     * <p>Run <b>on the listener's thread for this connection</b> and not off it: the listener closes
     * the connection when this method returns, so a handler that spawned a thread and returned would
     * have its socket closed under the session - measured, and it looks exactly like a client that
     * connects and is dropped.
     */
    private void acceptWebSocket(cn.oyzh.fx.xserver.net.WebSocketListener.Connection
                                         connection) {
        serve(new WebSocketTransport(connection));
    }

    /**
     * One RFB session, whichever transport it arrived on.
     *
     * <p>The name a viewer puts in its window is the X display it is watching, not the screen's index
     * - "xserver :96" is what a user recognises, and the index is 0 for the only screen this backend
     * serves.
     */
    private void serve(RfbSession.RfbTransport transport) {
        try {
            RfbSession session = new RfbSession(this, transport, screen, input,
                    title + " :" + displayNumber, password);
            sessions.add(session);
            try {
                session.run();
            } finally {
                sessions.remove(session);
            }
        } catch (IOException e) {
            JulLog.debug("a VNC client could not be started", e);
            transport.close();
        }
    }

    /** A VNC client on a socket. */
    private static final class SocketTransport implements RfbSession.RfbTransport {

        private final Socket socket;

        SocketTransport(Socket socket) throws IOException {
            this.socket = socket;
            socket.setTcpNoDelay(true);
        }

        @Override
        public String peer() {
            return String.valueOf(socket.getRemoteSocketAddress());
        }

        @Override
        public java.io.InputStream in() throws IOException {
            return socket.getInputStream();
        }

        @Override
        public java.io.OutputStream out() throws IOException {
            return socket.getOutputStream();
        }

        @Override
        public void readTimeout(int millis) throws IOException {
            socket.setSoTimeout(millis);
        }

        @Override
        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                JulLog.debug("closing a VNC socket", e);
            }
        }
    }

    /** The same session, inside WebSocket frames, for a page. */
    private static final class WebSocketTransport implements RfbSession.RfbTransport {

        private final cn.oyzh.fx.xserver.net.WebSocketListener.Connection connection;

        WebSocketTransport(cn.oyzh.fx.xserver.net.WebSocketListener.Connection connection) {
            this.connection = connection;
        }

        @Override
        public String peer() {
            return connection.peer();
        }

        @Override
        public java.io.InputStream in() {
            return connection.in();
        }

        @Override
        public java.io.OutputStream out() {
            return connection.out();
        }

        @Override
        public void readTimeout(int millis) throws IOException {
            connection.readTimeout(millis);
        }

        @Override
        public void close() {
            connection.close();
        }
    }
}
