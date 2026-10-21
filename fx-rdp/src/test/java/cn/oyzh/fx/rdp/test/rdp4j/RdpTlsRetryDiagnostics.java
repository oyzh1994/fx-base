package cn.oyzh.fx.rdp.test.rdp4j;

import com.tangluobo.rdp4j.RdpClient;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 确定性验证 TLS 握手瞬态失败后的重试策略。
 *
 * <p>本地假服务端只完成 X.224 协商并选择 HYBRID，然后立即关闭 TCP，
 * 使 TLS 握手必然失败。修复后的客户端应当用全新连接重试 HYBRID，
 * 而不是降级到 STANDARD。默认重试 2 次，因此应观察到 1 + 3 = 4 次连接。</p>
 */
public final class RdpTlsRetryDiagnostics {

    private static final byte[] NEGOTIATE_HYBRID = {
            0x03, 0x00, 0x00, 0x13,             // TPKT, length 19
            0x0e, (byte) 0xd0, 0x00, 0x00,      // X.224 CC, dst-ref
            0x12, 0x34, 0x00,                   // src-ref, class
            0x02, 0x00, 0x08, 0x00,             // RDP_NEG_RSP, HYBRID
            0x02, 0x00, 0x00, 0x00
    };

    private RdpTlsRetryDiagnostics() {
    }

    public static void main(String[] args) throws Exception {
        AtomicInteger connections = new AtomicInteger();
        CountDownLatch stopped = new CountDownLatch(1);
        try (ServerSocket server = new ServerSocket(0)) {
            Thread acceptor = Thread.ofVirtual().name("fake-rdp").start(() -> {
                try {
                    while (!server.isClosed()) {
                        try (Socket socket = server.accept()) {
                            connections.incrementAndGet();
                            readX224Request(socket);
                            OutputStream out = socket.getOutputStream();
                            out.write(NEGOTIATE_HYBRID);
                            out.flush();
                            // Closing before ServerHello forces the TLS
                            // handshake to fail without completing.
                        } catch (IOException ignored) {
                            if (server.isClosed()) {
                                return;
                            }
                        }
                    }
                } finally {
                    stopped.countDown();
                }
            });

            RdpClient client = new RdpClient(new RdpConnectDiagnostics.HeadlessFrontend());
            CountDownLatch done = new CountDownLatch(1);
            client.setOnDisconnected((reason, error) -> {
                System.out.println("client stopped: " + reason);
                done.countDown();
            });
            Thread worker = Thread.ofVirtual().start(() -> client.connect(
                    "127.0.0.1", server.getLocalPort(), "oyzh", "123456", null,
                    1024, 768, 32, true, false, false));

            if (!done.await(20, TimeUnit.SECONDS)) {
                System.out.println("client did not stop within timeout");
                client.disconnect();
                System.exit(1);
            }
            worker.join(5_000);
            server.close();
            stopped.await(2, TimeUnit.SECONDS);

            int count = connections.get();
            int expected = 1 + Integer.getInteger("rdp.tls.retryLimit", 2) + 1;
            System.out.println("tls retry connections=" + count + ", expected=" + expected);
            if (count < expected) {
                System.out.println("FAIL: client did not retry HYBRID on TLS failure");
                System.exit(1);
            }
            System.out.println("tls-retry=ok");
        }
    }

    private static void readX224Request(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        byte[] header = new byte[4];
        in.readFully(header);
        int length = ((header[2] & 0xff) << 8) | (header[3] & 0xff);
        if (length > 4) {
            in.readFully(new byte[length - 4]);
        }
    }
}
