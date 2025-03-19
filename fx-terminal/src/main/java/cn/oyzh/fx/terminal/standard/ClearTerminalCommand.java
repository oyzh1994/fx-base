package cn.oyzh.fx.terminal.standard;

import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * @author oyzh
 * @since 2023/7/22
 */
public class ClearTerminalCommand extends TerminalCommand {

    private boolean clearHis;

    public boolean isClearHis() {
        return clearHis;
    }

    public void setClearHis(boolean clearHis) {
        this.clearHis = clearHis;
    }
}
