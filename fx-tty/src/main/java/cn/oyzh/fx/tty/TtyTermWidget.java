package cn.oyzh.fx.tty;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.fx.tty.zmodem.TtyZModemTtyConnector;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXJediTermWidget;
import com.jediterm.terminal.ui.settings.SettingsProvider;

import java.io.IOException;

/**
 * 终端组件抽象类，在 {@link FXJediTermWidget} 基础上封装会话打开、终端尺寸获取
 * 以及退格码与 Alt 修饰符等设置项的控制。
 *
 * @author oyzh
 * @since 2026-07-07
 */
public abstract class TtyTermWidget extends FXJediTermWidget {

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
        if (this.canOpenSession()) {
            FXJediTermWidget session = this.createTerminalSessionFX(ttyConnector);
            session.start();
        }
    }

    @Override
    public TtyConnector getTtyConnector() {
        if (super.getTtyConnector() instanceof TtyZModemTtyConnector connector) {
            return connector.getConnector();
        }
        return super.getTtyConnector();
    }

    /**
     * 获取当前终端尺寸。
     *
     * @return 终端尺寸，连接器不支持时返回 null
     */
    public TermSize getTermSize() {
        if (this.getTtyConnector() instanceof TtyTerminalSizeable size) {
            return size.getTermSize();
        }
        return null;
    }

    @Override
    public void close() {
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
    }
}
