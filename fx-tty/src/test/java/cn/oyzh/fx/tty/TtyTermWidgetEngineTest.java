package cn.oyzh.fx.tty;

import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXTerminalActionPresentation;
import com.jediterm.terminal.ui.TerminalActionPresentation;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.ui.settings.FXDefaultSettingsProvider;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import cn.oyzh.fx.plus.theme.ThemeManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

            // 透明背景：宿主窗口背景图应当可以从 Ghostty 空白区域透出
            var backgroundAlpha = callOnFxThread(() -> {
                var theme = view.getTheme();
                var background = theme.background();
                view.setTheme(theme.withBackground(new Color(
                        background.getRed(), background.getGreen(), background.getBlue(), 0)));
                var parameters = new SnapshotParameters();
                parameters.setFill(Color.TRANSPARENT);
                var image = view.snapshot(parameters, null);
                return image.getPixelReader()
                        .getColor((int) image.getWidth() - 10, (int) image.getHeight() - 10)
                        .getOpacity();
            });
            assertEquals(0.0, backgroundAlpha, 0.01);
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
    void ghosttyFontZoomMatchesJediTerm() throws Exception {
        var widget = callOnFxThread(TestWidget::new);
        var connector = new BlockingConnector();
        try {
            callOnFxThread(() -> {
                widget.setEngine(TtyTerminalEngine.GHOSTTY);
                widget.openSession(connector);
                return null;
            });
            var view = callOnFxThread(widget::getGhosttyView);
            var initial = callOnFxThread(() -> view.getFont().getSize());
            callOnFxThread(() -> {
                widget.increaseTerminalFontSize();
                return null;
            });
            assertEquals(initial + 1, callOnFxThread(() -> view.getFont().getSize()), 0.01);
            callOnFxThread(() -> {
                widget.decreaseTerminalFontSize();
                return null;
            });
            assertEquals(initial, callOnFxThread(() -> view.getFont().getSize()), 0.01);
            callOnFxThread(() -> {
                widget.increaseTerminalFontSize();
                widget.increaseTerminalFontSize();
                widget.resetTerminalFontSize();
                return null;
            });
            assertEquals(initial, callOnFxThread(() -> view.getFont().getSize()), 0.01);
        } finally {
            callOnFxThread(() -> {
                widget.close();
                return null;
            });
            connector.close();
        }
    }

    @Test
    void ghosttyContextMenuMatchesJediTermActions() throws Exception {
        var widget = callOnFxThread(TestWidget::new);
        var connector = new BlockingConnector();
        try {
            callOnFxThread(() -> {
                widget.setEngine(TtyTerminalEngine.GHOSTTY);
                widget.openSession(connector);
                return null;
            });
            var menu = callOnFxThread(widget::getGhosttyContextMenu);
            assertNotNull(menu);
            callOnFxThread(() -> {
                widget.populateGhosttyContextMenu(menu);
                return null;
            });
            var items = menu.getItems();
            assertTrue(((MenuItem) items.getFirst()).getText().contains("Find"),
                    "Find must be the first context menu action");
            var pageDownIndex = -1;
            var lineUpIndex = -1;
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) instanceof MenuItem item) {
                    if ("Page Down".equals(item.getText())) {
                        pageDownIndex = i;
                    } else if ("Line Up".equals(item.getText())) {
                        lineUpIndex = i;
                    }
                }
            }
            assertTrue(pageDownIndex >= 0 && lineUpIndex > pageDownIndex);
            assertTrue(items.get(lineUpIndex - 1) instanceof SeparatorMenuItem,
                    "page and line actions must be separated");
            var increase = items.stream()
                    .filter(MenuItem.class::isInstance)
                    .map(MenuItem.class::cast)
                    .filter(item -> "Increase".equals(item.getText()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(increase);
            var lineItems = menu.getItems().stream()
                    .filter(MenuItem.class::isInstance)
                    .map(MenuItem.class::cast)
                    .map(MenuItem::getText)
                    .filter(java.util.Objects::nonNull)
                    .filter(text -> text.toLowerCase(java.util.Locale.ROOT).contains("line"))
                    .count();
            assertTrue(lineItems >= 2, "context menu must contain line up/down actions");
            var view = callOnFxThread(widget::getGhosttyView);
            var initial = callOnFxThread(() -> view.getFont().getSize());
            callOnFxThread(() -> {
                increase.fire();
                return null;
            });
            assertEquals(initial + 1, callOnFxThread(() -> view.getFont().getSize()), 0.01);
        } finally {
            callOnFxThread(() -> {
                widget.close();
                return null;
            });
            connector.close();
        }
    }

    @Test
    void ghosttyContextMenuHidesOnScenePress() throws Exception {
        var widget = callOnFxThread(TestWidget::new);
        var connector = new BlockingConnector();
        Stage stage = null;
        try {
            stage = callOnFxThread(() -> {
                widget.setEngine(TtyTerminalEngine.GHOSTTY);
                widget.openSession(connector);
                var s = new Stage();
                s.setScene(new Scene(widget, 600, 400));
                s.show();
                return s;
            });
            var view = callOnFxThread(widget::getGhosttyView);
            var menu = callOnFxThread(widget::getGhosttyContextMenu);
            assertNotNull(menu);
            callOnFxThread(() -> {
                Event.fireEvent(view, new MouseEvent(
                        MouseEvent.MOUSE_PRESSED,
                        10, 10, 10, 10,
                        MouseButton.SECONDARY,
                        1,
                        false, false, false, false,
                        false, false, true,
                        false, false, false, null));
                return null;
            });
            assertTrue(callOnFxThread(menu::isShowing));
            callOnFxThread(() -> {
                Event.fireEvent(view, new MouseEvent(
                        MouseEvent.MOUSE_PRESSED,
                        10, 10, 10, 10,
                        MouseButton.PRIMARY,
                        1,
                        false, false, false, false,
                        true, false, false,
                        false, false, false, null));
                return null;
            });
            assertFalse(callOnFxThread(menu::isShowing));
        } finally {
            var stageRef = stage;
            callOnFxThread(() -> {
                if (stageRef != null) {
                    stageRef.close();
                }
                widget.close();
                return null;
            });
            connector.close();
        }
    }

    @Test
    void ghosttyAppliesTerminalSettings() throws Exception {
        var provider = new TestSettingsProvider();
        provider.copyOnSelect = true;
        provider.parseHyperlink = false;
        provider.cursorStyle = 2;
        provider.blinkMs = 250;
        provider.useAntialiasing = false;
        var widget = callOnFxThread(() -> new TestWidget(provider));
        var connector = new BlockingConnector();
        try {
            callOnFxThread(() -> {
                widget.setEngine(TtyTerminalEngine.GHOSTTY);
                widget.openSession(connector);
                return null;
            });
            var view = callOnFxThread(widget::getGhosttyView);
            assertTrue(callOnFxThread(view::isCopyOnSelect));
            assertFalse(callOnFxThread(view::isHyperlinksEnabled));
            assertTrue(callOnFxThread(view::isCursorBlinking));
            assertFalse(callOnFxThread(view::isUseAntialiasing));
        } finally {
            callOnFxThread(() -> {
                widget.close();
                return null;
            });
            connector.close();
        }
    }

    @Test
    void ghosttyThemeChangeReappliesProviderColors() throws Exception {
        var provider = new TestSettingsProvider();
        var widget = callOnFxThread(() -> new TestWidget(provider));
        var connector = new BlockingConnector();
        try {
            callOnFxThread(() -> {
                widget.setEngine(TtyTerminalEngine.GHOSTTY);
                widget.openSession(connector);
                return null;
            });
            var view = callOnFxThread(widget::getGhosttyView);
            var before = callOnFxThread(() -> view.getTheme().searchMatchBorderColor());
            callOnFxThread(() -> {
                var theme = view.getTheme();
                view.setTheme(theme.withBackground(new Color(
                        theme.background().getRed(),
                        theme.background().getGreen(),
                        theme.background().getBlue(),
                        0)));
                return null;
            });
            provider.searchBorder = TerminalColor.rgb(0, 255, 0);
            callOnFxThread(() -> {
                widget.changeTheme(ThemeManager.currentTheme());
                return null;
            });
            assertEquals(0.0, callOnFxThread(() -> view.getTheme().background().getOpacity()), 0.001);
            var after = callOnFxThread(() -> view.getTheme().searchMatchBorderColor());
            assertNotEquals(before, after);
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
            assertFalse(callOnFxThread(() -> widget.getTerminalPanel().isScrollBarVisible()));
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
            this(new TestSettingsProvider());
        }

        private TestWidget(SettingsProvider provider) {
            super(provider);
        }

        @Override
        public TtyConnector createTtyConnector() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class TestSettingsProvider extends FXDefaultSettingsProvider implements TtyTermSettingsProvider {

        private float terminalFontSize = 14;
        private boolean copyOnSelect;
        private boolean parseHyperlink = true;
        private int cursorStyle;
        private int blinkMs = 500;
        private boolean useAntialiasing = true;
        private TerminalColor searchBorder = TerminalColor.rgb(255, 0, 0);

        @Override
        public TerminalActionPresentation getIncrTermSizePresentation() {
            return new FXTerminalActionPresentation("Increase",
                    new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.META_DOWN));
        }

        @Override
        public TerminalActionPresentation getDecrTermSizePresentation() {
            return new FXTerminalActionPresentation("Decrease",
                    new KeyCodeCombination(KeyCode.MINUS, KeyCombination.META_DOWN));
        }

        @Override
        public TerminalActionPresentation getResetTermSizePresentation() {
            return new FXTerminalActionPresentation("Reset",
                    new KeyCodeCombination(KeyCode.SLASH, KeyCombination.META_DOWN));
        }

        @Override
        public void setTerminalFontSize(float terminalFontSize) {
            this.terminalFontSize = terminalFontSize;
        }

        @Override
        public float getTerminalFontSize() {
            return this.terminalFontSize;
        }

        @Override
        public boolean copyOnSelect() {
            return this.copyOnSelect;
        }

        @Override
        public boolean isParseHyperlink() {
            return this.parseHyperlink;
        }

        @Override
        public int getCursorStyle() {
            return this.cursorStyle;
        }

        @Override
        public int caretBlinkingMs() {
            return this.blinkMs;
        }

        @Override
        public boolean useAntialiasing() {
            return this.useAntialiasing;
        }

        @Override
        public TextStyle getFoundPatternColor() {
            return new TextStyle(this.searchBorder, TerminalColor.WHITE);
        }
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
