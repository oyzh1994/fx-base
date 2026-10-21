package cn.oyzh.fx.rdp.test.rdp4j;

import com.tangluobo.rdp4j.RdpClient;

import java.time.Duration;

/**
 * 验证连接过程中调用 disconnect() 能真正取消旧的连接线程。
 *
 * <p>用于复现“关闭页签后旧连接仍在后台建立，随后与新连接争抢同一 RDP 会话”
 * 导致的 TLS 重置。正常运行输出 {@code cancel=ok} 且退出码为 0。</p>
 */
public final class RdpCancelDiagnostics {

    private RdpCancelDiagnostics() {
    }

    public static void main(String[] args) throws Exception {
        String host = System.getProperty("rdp.host", "192.168.22.152");
        int port = Integer.getInteger("rdp.port", 3389);
        String username = System.getProperty("rdp.username", "oyzh");
        String password = System.getProperty("rdp.password", "123456");
        int attempts = Integer.getInteger("rdp.attempts", 10);
        long cancelAfterMillis = Long.getLong("rdp.cancelAfterMillis", 1L);

        int cancelled = 0;
        for (int i = 1; i <= attempts; i++) {
            RdpClient client = new RdpClient(new RdpConnectDiagnostics.HeadlessFrontend());
            Thread worker = Thread.ofVirtual().name("rdp-cancel-" + i).start(() ->
                    client.connect(host, port, username, password, null,
                            1024, 768, 32, true, false, false));
            Thread.sleep(cancelAfterMillis);
            client.disconnect();
            worker.join(Duration.ofSeconds(5));
            if (worker.isAlive()) {
                System.out.println("attempt " + i + ": cancel=timeout (worker still alive)");
                System.exit(1);
            }
            cancelled++;
            System.out.println("attempt " + i + ": cancel=ok");
        }
        System.out.println("cancel=" + cancelled + "/" + attempts);
    }
}
