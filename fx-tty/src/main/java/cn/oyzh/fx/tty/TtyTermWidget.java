package cn.oyzh.fx.tty;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.fx.tty.zmodem.TtyZModemTtyConnector;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXJediTermWidget;
import com.jediterm.terminal.ui.settings.SettingsProvider;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public abstract class TtyTermWidget extends FXJediTermWidget {

    public TtyTermWidget(SettingsProvider provider) {
        super(provider);
    }

    public abstract TtyConnector createTtyConnector() throws IOException;

    public void openSession() throws IOException {
        if (this.canOpenSession()) {
            this.openSession(this.createTtyConnector());
        }
    }

    public void openSession(TtyConnector ttyConnector) {
        if (this.canOpenSession()) {
            FXJediTermWidget session = this.createTerminalSession(ttyConnector);
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
