package cn.oyzh.fx.xserver.net;

import cn.oyzh.fx.xserver.XServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The line of communication a user has with a server that has no window (backlog D1).
 *
 * <p>A detached server is one nobody can click on: the window that would have carried the close
 * button is gone, and the clients behind it go on running. What is left is this - a loopback port
 * that takes one command per connection and answers in one line:
 *
 * <pre>
 *   attach     open a host window for every detached session and present the whole frame
 *   detach     close every session's window and leave its clients running
 *   status     one line about the sessions, the clients and whether anything is detached
 * </pre>
 *
 * <p><b>Why a port and not a signal.</b> {@code SIGHUP} is the unix answer and the JVM cannot take
 * it without internal API ({@code sun.misc.Signal}); a port can be used from a script, from another
 * machine's terminal, and from a test, and it says what it did back. It is bound to <b>loopback
 * only</b> and deliberately has no authentication of its own: it is a way for the user who started
 * the server to say something to it, and the display it belongs to is on the same machine - a
 * control port opened on every interface would be a remote kill switch for a display server.
 *
 * <p>Everything it does goes through {@link XServer}'s own methods, and those take the server lock
 * and touch the display's own state; this class never does either itself. The work happens on the
 * display tick's next frame, which is where windows are opened and closed, so a command and a
 * frame can never disagree about whether a window exists.
 */
public final class ControlListener {

    private static final Logger log = LoggerFactory.getLogger(ControlListener.class);

    private final XServer server;
    private final int port;
    private final AtomicBoolean running = new AtomicBoolean();
    private volatile ServerSocket socket;
    private Thread thread;

    public ControlListener(XServer server, int port) {
        this.server = server;
        this.port = port;
    }

    /** Binds the port and answers commands on its own thread. */
    public void start() throws IOException {
        ServerSocket bound = new ServerSocket();
        bound.setReuseAddress(true);
        bound.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
        socket = bound;
        running.set(true);
        thread = new Thread(this::accept, "xserver-control");
        thread.setDaemon(true);
        thread.start();
        log.info("control port on 127.0.0.1:{}: attach, detach, status", bound.getLocalPort());
    }

    /** The port actually bound, which is what a test that asked for port 0 needs to know. */
    public int boundPort() {
        ServerSocket bound = socket;
        return bound == null ? port : bound.getLocalPort();
    }

    public void stop() {
        running.set(false);
        ServerSocket bound = socket;
        if (bound != null) {
            try {
                bound.close();
            } catch (IOException e) {
                log.debug("closing the control socket", e);
            }
        }
        Thread open = thread;
        if (open != null) {
            open.interrupt();
        }
    }

    private void accept() {
        while (running.get()) {
            try (Socket client = socket.accept()) {
                answer(client);
            } catch (IOException e) {
                if (running.get()) {
                    log.debug("control connection failed", e);
                }
            }
        }
    }

    private void answer(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(),
                StandardCharsets.UTF_8));
        String command = in.readLine();
        OutputStream out = client.getOutputStream();
        PrintWriter writer = new PrintWriter(new java.io.OutputStreamWriter(out,
                StandardCharsets.UTF_8), true);
        // `nc host port <<< attach` sends the line and then waits for the close, so the reply ends
        // with the connection rather than with a sentinel - nothing here expects a second command.
        writer.println(server.controlCommand(command));
        writer.flush();
    }
}
