package cn.oyzh.fx.tty;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.fx.plus.menu.ContextMenuManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.fx.tty.ghostty.TerminalShortcut;
import cn.oyzh.fx.tty.ghostty.TerminalView;
import cn.oyzh.fx.tty.zmodem.TtyZModemTtyConnector;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXJediTermWidget;
import com.jediterm.terminal.ui.FXTerminalAction;
import com.jediterm.terminal.ui.FXTerminalActionPresentation;
import com.jediterm.terminal.ui.TerminalAction;
import com.jediterm.terminal.ui.TerminalActionPresentation;
import com.jediterm.terminal.ui.TerminalActionProvider;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * 终端组件抽象类，在 {@link FXJediTermWidget} 基础上封装会话打开、终端尺寸获取
 * 以及退格码与 Alt 修饰符等设置项的控制。
 *
 * <p>该类同时承载两套终端引擎：</p>
 * <ul>
 *     <li>JediTerm：沿用原有实现与 UI；</li>
 *     <li>Ghostty：在同一个 JavaFX 容器中叠加 {@link TerminalView}，连接器通过
 *     {@link TtyConnectorTerminal} 适配，因此 easyshell 的本地、SSH、Telnet、串口、
 *     Mosh、RLogin 等后端都可以直接复用。</li>
 * </ul>
 *
 * <p>引擎在 {@link #openSession(TtyConnector)} 时确定，可在打开前通过
 * {@link #setEngine(TtyTerminalEngine)} 指定。打开中的会话不会被强制切换，
 * 新会话使用新的引擎。</p>
 *
 * @author oyzh
 * @since 2026-07-07
 */
public abstract class TtyTermWidget extends FXJediTermWidget {

    /**
     * 当前组件指定的引擎，null 表示使用全局默认引擎
     */
    private TtyTerminalEngine engine;

    /**
     * Ghostty 终端视图，null 表示未使用 Ghostty 引擎
     */
    private TerminalView ghosttyView;

    /**
     * Ghostty 会话使用的连接器
     */
    private TtyConnector ghosttyConnector;

    /**
     * Ghostty 当前字体大小
     */
    private float ghosttyFontSize = 14;

    /**
     * Ghostty 初始字体大小，用于重置
     */
    private float ghosttyBaseFontSize = 14;

    /**
     * Ghostty 右键菜单
     */
    private ContextMenu ghosttyContextMenu;

    /**
     * 构造终端组件。
     *
     * @param provider 设置提供者
     */
    public TtyTermWidget(SettingsProvider provider) {
        super(provider);
    }

    /**
     * 创建终端连接器。
     *
     * @return 终端连接器
     * @throws IOException IO 异常
     */
    public abstract TtyConnector createTtyConnector() throws IOException;

    /**
     * 打开终端会话，连接器由子类创建。
     *
     * @throws IOException IO 异常
     */
    public void openSession() throws IOException {
        if (this.canOpenSession()) {
            this.openSession(this.createTtyConnector());
        }
    }

    /**
     * 使用指定连接器打开终端会话。
     *
     * @param ttyConnector 终端连接器
     */
    public void openSession(TtyConnector ttyConnector) {
        if (ttyConnector == null) {
            return;
        }
        TtyTerminalEngine engine = this.getEngine();
        if (engine == TtyTerminalEngine.GHOSTTY) {
            this.openGhosttySession(ttyConnector);
            return;
        }
        if (this.canOpenSession()) {
            // 组件可能在 Ghostty 会话关闭后被复用，需要恢复 JediTerm 面板
            var panel = this.getTerminalPanel();
            if (!panel.isVisible()) {
                panel.setVisible(true);
                panel.setManaged(true);
            }
            FXJediTermWidget session = this.createTerminalSessionFX(ttyConnector);
            session.start();
        }
    }

    /**
     * 使用 Ghostty 引擎打开会话。
     *
     * @param ttyConnector 终端连接器
     */
    private void openGhosttySession(TtyConnector ttyConnector) {
        if (this.ghosttyView != null || this.isSessionRunning()) {
            return;
        }
        var panel = this.getTerminalPanel();
        panel.setVisible(false);
        panel.setManaged(false);
        TerminalView view = null;
        try {
            float initialFontSize = this.getSettingsProvider().getTerminalFontSize();
            if (initialFontSize <= 0) {
                initialFontSize = 14;
            }
            this.ghosttyBaseFontSize = initialFontSize;
            this.ghosttyFontSize = initialFontSize;
            view = new TerminalView((columns, rows) -> new TtyConnectorTerminal(ttyConnector, columns, rows));
            TtyGhosttySettings.apply(view, this.getSettingsProvider());
            // FXStackPane.layoutChildren() 会对每个子节点调用 autosize()，而 TerminalView 的
            // prefWidth/prefHeight 默认来自 80x24 的 Canvas。若不接管 pref 尺寸，布局会在
            // “80 列宽度”和“父容器宽度”之间反复切换，导致终端尺寸高频变化。
            // 绑定到宿主尺寸后 autosize() 与 StackPane 的填充结果一致，尺寸保持稳定。
            view.setMinSize(0, 0);
            view.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            view.prefWidthProperty().bind(this.widthProperty());
            view.prefHeightProperty().bind(this.heightProperty());
            view.setFocusTraversable(true);
            StackPane.setAlignment(view, Pos.TOP_LEFT);
            this.addChild(view);
            this.ghosttyView = view;
            this.ghosttyConnector = ttyConnector;
            try {
                view.setSearchPromptText(I18nHelper.pleaseInputContent());
                this.installGhosttyShortcuts(view);
                this.installGhosttyContextMenu(view);
            } catch (RuntimeException ex) {
                // 快捷键/菜单初始化失败不应阻止终端会话打开
                ex.printStackTrace();
            }
            Platform.runLater(view::requestFocus);
        } catch (RuntimeException ex) {
            if (view != null) {
                view.prefWidthProperty().unbind();
                view.prefHeightProperty().unbind();
            }
            this.ghosttyView = null;
            this.ghosttyConnector = null;
            if (this.ghosttyContextMenu != null) {
                this.ghosttyContextMenu.hide();
                this.ghosttyContextMenu = null;
            }
            panel.setVisible(true);
            panel.setManaged(true);
            throw ex;
        }
    }

    @Override
    public TtyConnector getTtyConnector() {
        TtyConnector connector = this.ghosttyConnector != null ? this.ghosttyConnector : super.getTtyConnector();
        // 对外保持原有语义：ZModem 包装器返回被包装的真实连接器。
        if (connector instanceof TtyZModemTtyConnector zmodem) {
            return zmodem.getConnector();
        }
        return connector;
    }

    /**
     * 获取全局默认终端引擎。
     *
     * @return 默认引擎
     */
    public static TtyTerminalEngine getDefaultEngine() {
        return TtyTerminalEngine.getDefaultEngine();
    }

    /**
     * 设置全局默认终端引擎，只影响之后打开的终端会话。
     *
     * @param engine 终端引擎
     */
    public static void setDefaultEngine(TtyTerminalEngine engine) {
        TtyTerminalEngine.setDefaultEngine(engine);
    }

    /**
     * 获取当前会话使用的终端引擎。
     *
     * @return 终端引擎
     */
    public TtyTerminalEngine getEngine() {
        TtyTerminalEngine current = this.engine;
        return current != null ? current : TtyTerminalEngine.getDefaultEngine();
    }

    /**
     * 指定当前组件的终端引擎，只影响之后打开的会话。
     *
     * @param engine 终端引擎，null 表示使用全局默认引擎
     */
    public void setEngine(TtyTerminalEngine engine) {
        this.engine = engine;
    }

    /**
     * 当前是否正在使用 Ghostty 引擎。
     *
     * @return 是否使用 Ghostty
     */
    public boolean isGhosttyActive() {
        return this.ghosttyView != null;
    }

    /**
     * 获取 Ghostty 终端视图。
     *
     * @return Ghostty 视图，未使用 Ghostty 引擎时返回 null
     */
    public TerminalView getGhosttyView() {
        return this.ghosttyView;
    }

    /**
     * 获取当前终端内容的像素宽度。
     *
     * @return 像素宽度
     */
    public int getPixelWidth() {
        if (this.ghosttyView != null) {
            int width = (int) Math.round(this.ghosttyView.getWidth());
            return width > 0 ? width : (int) Math.round(this.getWidth());
        }
        return (int) this.getTerminalPanel().getWidth();
    }

    /**
     * 获取当前终端内容的像素高度。
     *
     * @return 像素高度
     */
    public int getPixelHeight() {
        if (this.ghosttyView != null) {
            int height = (int) Math.round(this.ghosttyView.getHeight());
            return height > 0 ? height : (int) Math.round(this.getHeight());
        }
        return (int) this.getTerminalPanel().getHeight();
    }

    /**
     * 获取当前终端尺寸。
     *
     * @return 终端尺寸，连接器不支持时返回 null
     */
    public TermSize getTermSize() {
        if (this.ghosttyView != null) {
            var size = this.ghosttyView.getTerminalSize();
            return new TermSize(size.columns(), size.rows());
        }
        if (this.getTtyConnector() instanceof TtyTerminalSizeable size) {
            return size.getTermSize();
        }
        return null;
    }

    @Override
    public boolean canOpenSession() {
        return this.ghosttyView == null && super.canOpenSession();
    }

    @Override
    public void requestFocus() {
        var view = this.ghosttyView;
        if (view != null) {
            FXUtil.runWait(view::requestFocus);
            return;
        }
        super.requestFocus();
    }

    /**
     * 增大终端字体，行为与 JediTerm 的“增大字体”一致。
     */
    public void increaseTerminalFontSize() {
        if (this.ghosttyView == null) {
            return;
        }
        if (this.ghosttyFontSize < 30) {
            this.applyGhosttyFontSize(this.ghosttyFontSize + 1);
        }
    }

    /**
     * 减小终端字体，行为与 JediTerm 的“减小字体”一致。
     */
    public void decreaseTerminalFontSize() {
        if (this.ghosttyView == null) {
            return;
        }
        if (this.ghosttyFontSize >= 10) {
            this.applyGhosttyFontSize(this.ghosttyFontSize - 1);
        }
    }

    /**
     * 恢复终端字体，行为与 JediTerm 的“重置字体”一致。
     */
    public void resetTerminalFontSize() {
        if (this.ghosttyView == null) {
            return;
        }
        this.applyGhosttyFontSize(this.ghosttyBaseFontSize);
    }

    private void applyGhosttyFontSize(float size) {
        var view = this.ghosttyView;
        if (view == null) {
            return;
        }
        this.ghosttyFontSize = size;
        var provider = this.getSettingsProvider();
        if (provider instanceof TtyTermSettingsProvider settingsProvider) {
            settingsProvider.setTerminalFontSize(size);
            view.setFont(TtyGhosttySettings.font(provider));
        } else {
            view.setFont(Font.font(view.getFont().getFamily(), size));
        }
    }

    /**
     * 安装 Ghostty 快捷键，使用 easyshell 的快捷键配置，与 JediTerm 保持一致。
     */
    private void installGhosttyShortcuts(TerminalView view) {
        var provider = this.getSettingsProvider();
        var shortcuts = view.getTerminalShortcuts();
        int index = 0;
        index = this.addGhosttyShortcuts(shortcuts, index, provider.getOpenUrlActionPresentation(), view::openSelectedUrl);
        index = this.addGhosttyShortcuts(shortcuts, index, provider.getCopyActionPresentation(), view::copySelection);
        index = this.addGhosttyShortcuts(shortcuts, index, provider.getPasteActionPresentation(), view::pasteClipboard);
        index = this.addGhosttyShortcuts(shortcuts, index, provider.getSelectAllActionPresentation(), view::selectAll);
        index = this.addGhosttyShortcuts(shortcuts, index, provider.getClearBufferActionPresentation(), view::clearBuffer);
        index = this.addGhosttyShortcuts(shortcuts, index, provider.getPageUpActionPresentation(), view::scrollViewportPageUp);
        index = this.addGhosttyShortcuts(shortcuts, index, provider.getPageDownActionPresentation(), view::scrollViewportPageDown);
        index = this.addGhosttyShortcuts(shortcuts, index, provider.getLineUpActionPresentation(), view::scrollViewportLineUp);
        index = this.addGhosttyShortcuts(shortcuts, index, provider.getLineDownActionPresentation(), view::scrollViewportLineDown);
        index = this.addGhosttyShortcuts(shortcuts, index, provider.getFindActionPresentation(), view::openSearch);
        if (provider instanceof TtyTermSettingsProvider settingsProvider) {
            index = this.addGhosttyShortcuts(shortcuts, index, settingsProvider.getIncrTermSizePresentation(), () -> {
                this.increaseTerminalFontSize();
                return true;
            });
            index = this.addGhosttyShortcuts(shortcuts, index, settingsProvider.getDecrTermSizePresentation(), () -> {
                this.decreaseTerminalFontSize();
                return true;
            });
            index = this.addGhosttyShortcuts(shortcuts, index, settingsProvider.getResetTermSizePresentation(), () -> {
                this.resetTerminalFontSize();
                return true;
            });
        }
    }

    private int addGhosttyShortcuts(java.util.List<TerminalShortcut> shortcuts, int index,
                                    TerminalActionPresentation presentation, BooleanSupplier action) {
        if (presentation instanceof FXTerminalActionPresentation fxPresentation) {
            for (var combination : fxPresentation.getKeyCombinations()) {
                shortcuts.add(index++, new TerminalShortcut(combination, action));
            }
        }
        return index;
    }

    /**
     * 安装 Ghostty 右键菜单，动作与 JediTerm 的终端右键菜单一致。
     */
    private void installGhosttyContextMenu(TerminalView view) {
        var menu = ContextMenuManager.createNewContextMenu(java.util.List.<javafx.scene.control.MenuItem>of());
        this.ghosttyContextMenu = menu;
        menu.setAutoHide(true);
        menu.setHideOnEscape(true);
        EventHandler<MouseEvent> hideOnScenePress = event -> {
            if (menu.isShowing()) {
                menu.hide();
            }
        };
        menu.setOnShowing(event -> {
            this.populateGhosttyContextMenu(menu);
            var scene = view.getScene();
            if (scene != null) {
                scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, hideOnScenePress);
                scene.addEventFilter(MouseEvent.MOUSE_PRESSED, hideOnScenePress);
            }
        });
        menu.setOnHidden(event -> {
            var scene = view.getScene();
            if (scene != null) {
                scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, hideOnScenePress);
            }
            // 仅在焦点仍属于终端/无焦点时恢复焦点，避免抢走用户刚点击的其他控件焦点。
            if (this.ghosttyView != null) {
                var owner = scene == null ? null : scene.getFocusOwner();
                if (owner == null || owner == this.ghosttyView) {
                    this.ghosttyView.requestFocus();
                }
            }
        });
        view.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.PRIMARY && menu.isShowing()) {
                // 点击终端内容时立即关闭菜单；不消费事件，保持正常终端点击行为。
                menu.hide();
            }
        });
        view.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                // 不消费事件，否则 ContextMenu 的自动隐藏机制收不到后续点击，菜单会一直停留。
                this.showGhosttyContextMenu(view, event.getScreenX(), event.getScreenY());
            }
        });
        view.setOnContextMenuRequested(event -> {
            this.showGhosttyContextMenu(view, event.getScreenX(), event.getScreenY());
        });
    }

    private void showGhosttyContextMenu(TerminalView view, double screenX, double screenY) {
        var menu = this.ghosttyContextMenu;
        if (menu != null && !menu.isShowing()) {
            this.populateGhosttyContextMenu(menu);
            menu.show(view, screenX, screenY);
        }
    }

    ContextMenu getGhosttyContextMenu() {
        return this.ghosttyContextMenu;
    }

    void populateGhosttyContextMenu(ContextMenu menu) {
        var view = this.ghosttyView;
        if (view == null) {
            return;
        }
        var provider = this.getSettingsProvider();
        var actions = new ArrayList<TerminalAction>();
        // 查找放在第一项，与当前 JediTerm 菜单保持一致
        addGhosttyAction(actions, provider.getFindActionPresentation(), view::openSearch, null, false, KeyCode.F);
        addGhosttyAction(actions, provider.getOpenUrlActionPresentation(), view::openSelectedUrl,
                view::isSelectionUrl, true, null);
        addGhosttyAction(actions, provider.getCopyActionPresentation(), view::copySelection,
                view::canCopySelection, false, KeyCode.C);
        addGhosttyAction(actions, provider.getPasteActionPresentation(), view::pasteClipboard,
                null, false, KeyCode.P);
        addGhosttyAction(actions, provider.getSelectAllActionPresentation(), view::selectAll,
                null, false, null);
        addGhosttyAction(actions, provider.getClearBufferActionPresentation(), view::clearBuffer,
                () -> !view.isAlternateScreenBufferActive(), true, KeyCode.K);
        addGhosttyAction(actions, provider.getPageUpActionPresentation(), view::scrollViewportPageUp,
                () -> !view.isAlternateScreenBufferActive(), true, null);
        addGhosttyAction(actions, provider.getPageDownActionPresentation(), view::scrollViewportPageDown,
                () -> !view.isAlternateScreenBufferActive(), false, null);
        // 翻页和翻行之间单独分割
        addGhosttyAction(actions, provider.getLineUpActionPresentation(), view::scrollViewportLineUp,
                () -> !view.isAlternateScreenBufferActive(), true, null);
        addGhosttyAction(actions, provider.getLineDownActionPresentation(), view::scrollViewportLineDown,
                () -> !view.isAlternateScreenBufferActive(), false, null);
        if (provider instanceof TtyTermSettingsProvider settingsProvider) {
            addGhosttyAction(actions, settingsProvider.getIncrTermSizePresentation(), () -> {
                this.increaseTerminalFontSize();
                return true;
            }, null, false, null);
            addGhosttyAction(actions, settingsProvider.getDecrTermSizePresentation(), () -> {
                this.decreaseTerminalFontSize();
                return true;
            }, null, false, null);
            addGhosttyAction(actions, settingsProvider.getResetTermSizePresentation(), () -> {
                this.resetTerminalFontSize();
                return true;
            }, null, false, null);
        }
        menu.getItems().clear();
        FXTerminalAction.fillMenu(menu, new TerminalActionProvider() {
            @Override
            public List<TerminalAction> getActions() {
                return actions;
            }

            @Override
            public TerminalActionProvider getNextProvider() {
                return null;
            }

            @Override
            public void setNextProvider(TerminalActionProvider actionProvider) {
            }
        });
    }

    private static void addGhosttyAction(List<TerminalAction> actions, TerminalActionPresentation presentation,
                                         BooleanSupplier action, BooleanSupplier enabled,
                                         boolean separatorBefore, KeyCode mnemonicKey) {
        var fxPresentation = presentation instanceof FXTerminalActionPresentation fx
                ? fx : new FXTerminalActionPresentation(presentation.getName());
        var item = new FXTerminalAction(fxPresentation, event -> action.getAsBoolean());
        if (enabled != null) {
            item.withEnabledSupplier(() -> enabled.getAsBoolean());
        }
        item.separatorBefore(separatorBefore);
        if (mnemonicKey != null) {
            item.withMnemonicKey(mnemonicKey);
        }
        actions.add(item);
    }

    @Override
    public void close() {
        var menu = this.ghosttyContextMenu;
        this.ghosttyContextMenu = null;
        if (menu != null) {
            menu.hide();
        }
        var view = this.ghosttyView;
        this.ghosttyView = null;
        this.ghosttyConnector = null;
        if (view != null) {
            view.prefWidthProperty().unbind();
            view.prefHeightProperty().unbind();
            try {
                view.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            super.close();
            SystemUtil.gcLater();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化退格码
     *
     * @param backspaceType 退格类型
     */
    public void initBackspaceCode(Integer backspaceType) {
        if (this.getSettingsProvider() instanceof TtyTermSettingsProvider provider) {
            provider.setBackspaceCode(TtyTerminalUtil.getBackspaceCode(backspaceType));
        }
    }

    /**
     * 设置alt修饰符
     *
     * @param altSendsEscape alt修饰符
     */
    public void setAltSendsEscape(boolean altSendsEscape) {
        if (this.getSettingsProvider() instanceof TtyTermSettingsProvider provider) {
            provider.setAltSendsEscape(altSendsEscape);
        }
        if (this.ghosttyView != null) {
            this.ghosttyView.setMacOptionAsAlt(altSendsEscape);
        }
    }
}
