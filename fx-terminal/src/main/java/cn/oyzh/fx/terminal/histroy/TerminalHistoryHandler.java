package cn.oyzh.fx.terminal.histroy;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.terminal.Terminal;

/**
 * @author oyzh
 * @since 2023/08/28
 */
public interface TerminalHistoryHandler {

    /**
     * 获取上一个命令
     *
     * @param terminal 终端
     * @return 命令
     */
    String prevCommand(Terminal terminal);

    /**
     * 获取下一个命令
     *
     * @param terminal 终端
     * @return 命令
     */
    String nextCommand(Terminal terminal);

    /**
     * 保存历史
     *
     * @param input 内容
     */
    default void saveHistory(String input) {
        if (StringUtil.isNotEmpty(input) && this.historyStore() != null) {
            TerminalHistory history = new TerminalHistory();
            history.setLine(input);
            this.historyStore().add(history);
        }
    }

    /**
     * 清除历史
     */
    default void clearHistory() {
        if (this.historyStore() != null) {
            this.historyStore().clear();
        }
    }

    /**
     * 设置历史储存器
     *
     * @param historyStore 历史储存器
     */
    void historyStore(TerminalHistoryStore historyStore);

    /**
     * 获取历史储存器
     *
     * @return 历史储存器
     */
    TerminalHistoryStore historyStore();

}
