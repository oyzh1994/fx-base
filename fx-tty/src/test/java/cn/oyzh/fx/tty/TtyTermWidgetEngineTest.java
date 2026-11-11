package cn.oyzh.fx.tty;

import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.settings.FXDefaultSettingsProvider;
import javafx.application.Platform;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TtyTermWidget} 双引擎切换测试。
 */
final class TtyTermWidgetEngineTest {

    @BeforeAll
    static void initializeJavaFxRuntime() throws Exception {
        var started = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                started.countDown();
            });
        } catch (IllegalStateException _) {
            Platform.runLater(started::countDown);
        }
        assertTrue(started.await(30, TimeUnit.SECONDS), "Timed out waiting for JavaFX runtime startup");
    }

    @Test
    void defaultsToGlobalEngineAndCanOverride() {
        var widget = new TestWidget();
        assertEquals(TtyTerminalEngine.getDefaultEngine(), widget.getEngine());
        widget.setEngine(TtyTerminalEngine.JEDITERM);
        assertEquals(TtyTerminalEngine.JEDITERM, widget.getEngine());
        widget.setEngine(TtyTerminalEngine.GHOSTTY);
        assertEquals(TtyTerminalEngine.GHOSTTY, widget.getEngine());
        assertFalse(widget.isGhosttyActive());
    }

    @Test
    void opensGhosttySessionAndExposesConnector() throws Exception {
        var widget = callOnFxThread(TestWidget::new);
        var connector = new BlockingConnector();
        try {
            callOnFxThread(() -> {
                widget.setEngine(TtyTerminalEngine.GHOSTTY);
                widget.openSession(connector);
                return null;
            });
            assertTrue(callOnFxThread(widget::isGhosttyActive));
            assertNotNull(callOnFxThread(widget::getGhosttyView));
            assertEquals(connector, callOnFxThread(widget::getTtyConnector));
            var size = callOnFxThread(widget::getTermSize);
            assertNotNull(size);
            assertEquals(80, size.getColumns());
            assertEquals(24, size.getRows());

            // 验证 Ghostty 视图会随容器布局填满区域
            callOnFxThread(() -> {
                var scene = new Scene(widget, 800, 600);
                widget.applyCss();
                widget.layout();
                return scene;
            });
            var view = callOnFxThread(widget::getGhosttyView);
            assertTrue(callOnFxThread(() -> view.getWidth() > 0));
            assertTrue(callOnFxThread(() -> view.getHeight() > 0));

            // 回归：FXStackPane 的 autosize() 不能让 Ghostty 视图在 80 列和父容器宽度之间抖动
            var width = callOnFxThread(view::getWidth);
            var height = callOnFxThread(view::getHeight);
            var terminalSize = callOnFxThread(view::getTerminalSize);
            callOnFxThread(() -> {
                for (int i = 0; i < 10; i++) {
                    widget.applyCss();
                    widget.layout();
                }
                return null;
            });
            assertEquals(width, callOnFxThread(view::getWidth), 0.5);
            assertEquals(height, callOnFxThread(view::getHeight), 0.5);
            assertEquals(terminalSize, callOnFxThread(view::getTerminalSize));
        } finally {
            callOnFxThread(() -> {
                widget.close();
                return null;
            });
            connector.close();
        }
        assertFalse(callOnFxThread(widget::isGhosttyActive));
    }

    @Test
    void zModemProgressIsWrittenToGhosttyLocally() throws Exception {
        var widget = callOnFxThread(TestWidget::new);
        var connector = new BlockingConnector();
        try {
            callOnFxThread(() -> {
                widget.setEngine(TtyTerminalEngine.GHOSTTY);
                widget.openSession(connector);
                return null;
            });
            var zmodemTerminal = TtyTerminalUtil.createZModemTerminal(widget);
            zmodemTerminal.saveCursor();
            zmodemTerminal.writeCharacters("\u001B]2;zmodem-ghostty\u0007");
            zmodemTerminal.restoreCursor();

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            String title = null;
            while (System.nanoTime() < deadline) {
                title = callOnFxThread(() -> widget.getGhosttyView().getTitle());
                if ("zmodem-ghostty".equals(title)) {
                    break;
                }
                Thread.sleep(50);
            }
            assertEquals("zmodem-ghostty", title);
        } finally {
            callOnFxThread(() -> {
                widget.close();
                return null;
            });
            connector.close();
        }
    }

    @Test
    void opensJediTermSessionWhenSelected() throws Exception {
        var widget = callOnFxThread(TestWidget::new);
        var connector = new BlockingConnector();
        try {
            callOnFxThread(() -> {
                widget.setEngine(TtyTerminalEngine.JEDITERM);
                widget.openSession(connector);
                return null;
            });
            assertFalse(callOnFxThread(widget::isGhosttyActive));
            assertTrue(callOnFxThread(widget::isSessionRunning));
            assertEquals(connector, callOnFxThread(widget::getTtyConnector));
        } finally {
            callOnFxThread(() -> {
                widget.close();
                return null;
            });
            connector.close();
        }
    }

    private static <T> T callOnFxThread(Callable<T> action) throws Exception {
        var future = new FutureTask<>(action);
        Platform.runLater(future);
        return future.get(30, TimeUnit.SECONDS);
    }

    private static final class TestWidget extends TtyTermWidget {

        private TestWidget() {
            super(new TestSettingsProvider());
        }

        @Override
        public TtyConnector createTtyConnector() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class TestSettingsProvider extends FXDefaultSettingsProvider {
    }

    private static final class BlockingConnector implements TtyConnector {

        private final CountDownLatch closed = new CountDownLatch(1);

        private volatile boolean connected = true;

        private TermSize size;

        @Override
        public int read(char[] buffer, int offset, int length) throws IOException {
            try {
                this.closed.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
            return -1;
        }

        @Override
        public void write(byte[] bytes) {
        }

        @Override
        public void write(String string) {
        }

        @Override
        public boolean isConnected() {
            return this.connected;
        }

        @Override
        public void resize(TermSize termSize) {
            this.size = termSize;
        }

        @Override
        public int waitFor() {
            return 0;
        }

        @Override
        public boolean ready() {
            return true;
        }

        @Override
        public String getName() {
            return "blocking";
        }

        @Override
        public void close() {
            this.connected = false;
            this.closed.countDown();
        }
    }
}
