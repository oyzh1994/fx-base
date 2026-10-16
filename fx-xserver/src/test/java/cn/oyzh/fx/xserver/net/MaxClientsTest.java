package cn.oyzh.fx.xserver.net;

import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code --max-clients}, which was parsed, documented ("maximum simultaneous clients (default
 * 256)") and had <b>no reader at all</b>: measured on 2026-10-04 with a {@code --max-clients 2}
 * server, four connections were accepted and four setup replies went out.
 *
 * <p>The check lives where the index space is reserved rather than where a connection is registered,
 * because a slot is taken from the accept: a connection that is still in its setup handshake already
 * has a resource id space reserved for it. A refused connection is closed at once, which is what a
 * client sees as the socket ending before any reply.
 */
class MaxClientsTest {

    private static final int DISPLAY = 87;

    @Test
    void aConnectionPastTheLimitIsRefused() throws Exception {
        XServer server = new XServer(ServerOptions.parse(new String[] {
                "--display", ":" + DISPLAY, "--backend", "headless", "--no-unix", "--no-clipboard",
                "--no-tray", "--max-clients", "2"}));
        server.start();
        Socket first = null;
        Socket second = null;
        Socket third = null;
        try {
            first = connect();
            second = connect();
            assertTrue(stillOpen(first), "the first client should have been accepted");
            assertTrue(stillOpen(second), "the second client should have been accepted");

            third = connect();
            assertEquals(-1, third.getInputStream().read(),
                    "the third client should have been closed, not served");

            second.close();
            second = null;
            // The departure is noticed by the connection's own thread, which is not instantaneous.
            Socket fourth = null;
            long deadline = System.currentTimeMillis() + 5000;
            while (fourth == null && System.currentTimeMillis() < deadline) {
                Socket candidate = connect();
                if (stillOpen(candidate)) {
                    fourth = candidate;
                } else {
                    candidate.close();
                    Thread.sleep(50);
                }
            }
            assertTrue(fourth != null, "closing a client should have freed its slot");
            fourth.close();
        } finally {
            close(first);
            close(second);
            close(third);
            server.stop();
        }
    }

    private static Socket connect() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 6000 + DISPLAY), 3000);
        return socket;
    }

    /** True when the server has left the connection open: a refusal closes it, which reads as EOF. */
    private static boolean stillOpen(Socket socket) throws IOException {
        socket.setSoTimeout(400);
        InputStream in = socket.getInputStream();
        try {
            return in.read() != -1;
        } catch (SocketTimeoutException e) {
            return true;
        }
    }

    private static void close(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // done with it
            }
        }
    }
}
