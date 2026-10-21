package cn.oyzh.fx.rdp.test.rdp4j;

import com.tangluobo.rdp4j.IContext;
import com.tangluobo.rdp4j.RdpClient;
import com.tangluobo.rdp4j.RdpInput;
import com.tangluobo.rdp4j.State;
import com.tangluobo.rdp4j.frontend.RdpFrontend;
import com.tangluobo.rdp4j.graphics.IntArrayDisplay;
import com.tangluobo.rdp4j.graphics.RdesktopCanvas;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 无头 RDP 连接诊断。连续建立/断开连接，记录每一轮的完整失败原因。
 *
 * <p>默认连接 192.168.22.152，可通过系统属性覆盖：</p>
 * <pre>
 * -Drdp.host=192.168.22.157
 * -Drdp.attempts=20
 * -Drdp.holdMillis=2000
 * -Drdp.gapMillis=1000
 * </pre>
 */
public final class RdpConnectDiagnostics {

    private static final String HOST = System.getProperty("rdp.host", "192.168.22.152");
    private static final int PORT = Integer.getInteger("rdp.port", 3389);
    private static final String USERNAME = System.getProperty("rdp.username", "oyzh");
    private static final String PASSWORD = System.getProperty("rdp.password", "123456");
    private static final int ATTEMPTS = Integer.getInteger("rdp.attempts", 20);
    private static final long HOLD_MILLIS = Long.getLong("rdp.holdMillis", 2_000L);
    private static final long GAP_MILLIS = Long.getLong("rdp.gapMillis", 1_000L);
    private static final long CONNECT_TIMEOUT_MILLIS = Long.getLong("rdp.connectTimeoutMillis", 30_000L);
    private static final boolean SIMULATE_INPUT = Boolean.getBoolean("rdp.simulateInput");

    private RdpConnectDiagnostics() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("RDP diagnostics: " + HOST + ":" + PORT
                + ", attempts=" + ATTEMPTS
                + ", simulateInput=" + SIMULATE_INPUT
                + ", hold=" + HOLD_MILLIS + "ms, gap=" + GAP_MILLIS + "ms");
        List<String> failures = new ArrayList<>();
        int readyCount = 0;
        for (int i = 1; i <= ATTEMPTS; i++) {
            AttemptResult result = runAttempt(i);
            if (result.ready) {
                readyCount++;
            } else {
                failures.add("attempt " + i + ": " + result.failure);
            }
            if (i < ATTEMPTS) {
                Thread.sleep(GAP_MILLIS);
            }
        }
        System.out.println("===== SUMMARY =====");
        System.out.println("ready=" + readyCount + "/" + ATTEMPTS);
        for (String failure : failures) {
            System.out.println(failure);
        }
        if (readyCount != ATTEMPTS) {
            System.exit(1);
        }
    }

    private static AttemptResult runAttempt(int attempt) throws Exception {
        System.out.println("\n===== ATTEMPT " + attempt + "/" + ATTEMPTS + " =====");
        CountDownLatch readyLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(1);
        AtomicReference<String> failure = new AtomicReference<>();
        AtomicReference<Throwable> failureCause = new AtomicReference<>();
        RdpClient client = new RdpClient(new HeadlessFrontend());
        client.setOnConnected(() -> {
            System.out.println("[attempt " + attempt + "] connected callback");
            readyLatch.countDown();
        });
        client.setOnFirstFrame(() -> System.out.println("[attempt " + attempt + "] first frame"));
        client.setOnDisconnected((reason, error) -> {
            System.out.println("[attempt " + attempt + "] disconnected: " + reason);
            if (error != null) {
                error.printStackTrace(System.out);
            }
            failure.compareAndSet(null, reason);
            if (error != null) {
                failureCause.compareAndSet(null, error);
            }
            doneLatch.countDown();
        });

        Thread worker = Thread.ofVirtual().name("rdp-diag-" + attempt).start(() ->
                client.connect(HOST, PORT, USERNAME, PASSWORD, null,
                        1024, 768, 32, true, false, false));

        boolean ready = readyLatch.await(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        if (ready) {
            System.out.println("[attempt " + attempt + "] desktop ready, holding "
                    + HOLD_MILLIS + "ms");
            Thread.sleep(HOLD_MILLIS);
            if (doneLatch.getCount() == 0) {
                System.out.println("[attempt " + attempt + "] disconnected during hold");
                ready = false;
            }
        } else {
            System.out.println("[attempt " + attempt + "] not ready after "
                    + CONNECT_TIMEOUT_MILLIS + "ms");
        }
        client.disconnect();
        // A user-initiated disconnect intentionally suppresses onDisconnected,
        // so only wait for that callback when the attempt never became ready.
        if (!ready && !doneLatch.await(5, TimeUnit.SECONDS)) {
            System.out.println("[attempt " + attempt + "] worker still running after disconnect");
        }
        worker.join(5_000);
        AttemptResult result = new AttemptResult();
        result.ready = ready;
        result.failure = failure.get() == null
                ? "not ready"
                : failure.get();
        if (failureCause.get() != null) {
            result.failure += " <- " + failureCause.get().getClass().getName()
                    + ": " + failureCause.get().getMessage();
        }
        return result;
    }

    private static final class AttemptResult {
        private boolean ready;
        private String failure;
    }

    /**
     * 不依赖 JavaFX/AWT 的 frontend，仅创建离屏画布。
     */
    static final class HeadlessFrontend implements RdpFrontend {
        @Override
        public RdesktopCanvas createCanvas(IContext context, State state) {
            RdesktopCanvas canvas = new RdesktopCanvas(context, state,
                    new IntArrayDisplay(state.getWidth(), state.getHeight()));
            if (SIMULATE_INPUT) {
                canvas.setInput(new HeadlessInput(state));
            }
            return canvas;
        }
    }

    /**
     * Mirrors the part of FxRdpInput that matters for finalization: once the
     * connection announces INPUT readiness, send TS_SYNC_EVENT with the local
     * lock-key state.
     */
    private static final class HeadlessInput implements RdpInput {
        private final State state;

        private HeadlessInput(State state) {
            this.state = state;
        }

        @Override
        public void gainedFocus() {
        }

        @Override
        public void lostFocus() {
        }

        @Override
        public void triggerReadyToSend() {
            synchronizeLockKeys();
        }

        @Override
        public void synchronizeLockKeys() {
            if (state.getRdp() != null) {
                // TS_SYNC_NUM_LOCK | TS_SYNC_CAPS_LOCK
                state.getRdp().sendInput(1, 0, 0, 0x0002 | 0x0004, 0);
            }
        }

        @Override
        public void sendCtrlAltDel() {
        }
    }
}
