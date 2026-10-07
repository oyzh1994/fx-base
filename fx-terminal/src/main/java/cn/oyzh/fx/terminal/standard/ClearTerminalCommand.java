package cn.oyzh.fx.terminal.standard;

import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * 清除终端命令
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class ClearTerminalCommand extends TerminalCommand {

    /**
     * 是否清除历史
     */
    private boolean clearHis;

    /**
     * 是否清空历史。
     *
     * @return 清空历史
     */
    public boolean isClearHis() {
        return clearHis;
    }

    /**
     * 设置清空历史。
     *
     * @param clearHis 清空历史
     */
    public void setClearHis(boolean clearHis) {
        this.clearHis = clearHis;
    }
}
