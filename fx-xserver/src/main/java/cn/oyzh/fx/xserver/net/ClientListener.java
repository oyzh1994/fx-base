package cn.oyzh.fx.xserver.net;

import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.common.log.JulLog;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Accepts client connections.
 *
 * <p>Both transports X clients use are offered: TCP on port 6000 + display number, and the
 * Unix domain socket at {@code /tmp/.X11-unix/X<display>} that local clients prefer. A display is
 * only usable once at least one of them is listening.
 */
public final class ClientListener {


    public static final Path UNIX_SOCKET_DIR = Path.of("/tmp/.X11-unix");

    private final XServer server;
    private final List<ServerSocketChannel> channels = new CopyOnWriteArrayList<>();
    private final List<Path> socketPaths = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean();
    private Thread acceptThread;
    private Path unixSocketPath;

    public ClientListener(XServer server) {
        this.server = server;
    }

    public void start() throws IOException {
        int display = server.options().displayNumber();
        if (server.options().tcp()) {
            ServerSocketChannel tcp = ServerSocketChannel.open();
            tcp.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true);
            InetAddress address = server.options().listenAll()
                    ? null
                    : InetAddress.getLoopbackAddress();
            tcp.bind(new InetSocketAddress(address, server.options().tcpPort()), 64);
            tcp.configureBlocking(true);
            channels.add(tcp);
            JulLog.info("listening on TCP port {}", server.options().tcpPort());
        }
        if (server.options().unixSocket()) {
            try {
                startUnixSocket(display);
            } catch (IOException | UnsupportedOperationException e) {
                JulLog.info("unix domain socket unavailable: {}", e.getMessage());
            }
        }
        if (channels.isEmpty()) {
            throw new IOException("no display socket could be opened");
        }
        running.set(true);
        // One thread per listening socket: a blocking accept on the first channel would never
        // return, so a single accept loop could only ever serve one of the transports.
        for (ServerSocketChannel channel : channels) {
            Thread thread = new Thread(() -> acceptLoop(channel),
                    "xserver-accept-" + channel.getLocalAddress());
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void startUnixSocket(int display) throws IOException {
        // **The family is asked for before the directory is made.** `/tmp/.X11-unix` is a POSIX path:
        // on Windows it resolves against the current drive, so `createDirectories` would leave a
        // `\tmp\.X11-unix` behind on a machine that cannot use a unix socket at all - and then the
        // open below would have thrown and the whole attempt been logged as unavailable anyway. The
        // channel is opened first, so what is left to do is only what the platform supports.
        ServerSocketChannel unix = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        Files.createDirectories(UNIX_SOCKET_DIR);
        Path path = UNIX_SOCKET_DIR.resolve("X" + display);
        // A stale socket from a crashed server would make bind fail; only remove real sockets.
        if (Files.exists(path)) {
            Files.delete(path);
        }
        unix.bind(UnixDomainSocketAddress.of(path), 64);
        unix.configureBlocking(true);
        channels.add(unix);
        socketPaths.add(path);
        unixSocketPath = path;
        JulLog.info("listening on unix socket {}", path);
    }

    private void acceptLoop(ServerSocketChannel channel) {
        while (running.get()) {
            try {
                SocketChannel client = channel.accept();
                if (client == null) {
                    continue;
                }
                handle(client);
            } catch (java.nio.channels.AsynchronousCloseException e) {
                return;
            } catch (java.nio.channels.ClosedChannelException e) {
                return;
            } catch (IOException e) {
                if (running.get()) {
                    JulLog.warn("accept failed: {}", e.getMessage());
                }
            }
        }
    }

    private void handle(SocketChannel channel) {
        int index = server.allocateClientIndex();
        if (index < 0) {
            // Name the limit, because the one that is usually reached is the caller's: the default
            // 256 is the index space, and a smaller --max-clients is a choice somebody made.
            JulLog.warn("refusing connection: {} clients are already connected (--max-clients)",
                    server.options().maxClients());
            try {
                channel.close();
            } catch (IOException ignored) {
                // nothing useful to do
            }
            return;
        }
        try {
            channel.setOption(java.net.StandardSocketOptions.TCP_NODELAY, true);
        } catch (IOException | UnsupportedOperationException ignored) {
            // unix sockets do not support TCP options
        }
        try {
            ClientConnection connection = new ClientConnection(server, channel, index);
            Thread thread = new Thread(connection, "xserver-client-" + index);
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            JulLog.warn("failed to set up client: {}", e.getMessage());
            server.releaseClientIndex(index);
            try {
                channel.close();
            } catch (IOException ignored) {
                // nothing useful to do
            }
        }
    }

    public void stop() {
        running.set(false);
        for (ServerSocketChannel channel : channels) {
            try {
                channel.close();
            } catch (IOException ignored) {
                // shutting down
            }
        }
        channels.clear();
        for (Path path : socketPaths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                JulLog.debug("could not remove {}: {}", path, e.getMessage());
            }
        }
        socketPaths.clear();
        unixSocketPath = null;
    }

    /** The unix socket path in use, or null when only TCP is listening. */
    public Path unixSocketPath() {
        return unixSocketPath;
    }
}
