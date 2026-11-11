package cn.oyzh.fx.tty;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.tty.ghostty.TerminalView;
import cn.oyzh.fx.tty.zmodem.TtyZModemTtyConnector;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXJediTermWidget;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

import java.io.IOException;

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
        try {
            var view = new TerminalView((columns, rows) -> new TtyConnectorTerminal(ttyConnector, columns, rows));
            TtyGhosttySettings.apply(view, this.getSettingsProvider());
            view.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            view.setFocusTraversable(true);
            StackPane.setAlignment(view, Pos.TOP_LEFT);
            this.addChild(view);
            this.ghosttyView = view;
            this.ghosttyConnector = ttyConnector;
            Platform.runLater(view::requestFocus);
        } catch (RuntimeException ex) {
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

    @Override
    public void close() {
        var view = this.ghosttyView;
        this.ghosttyView = null;
        this.ghosttyConnector = null;
        if (view != null) {
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
