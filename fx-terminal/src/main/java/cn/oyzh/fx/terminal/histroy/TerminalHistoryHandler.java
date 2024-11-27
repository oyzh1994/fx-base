package cn.oyzh.fx.terminal.histroy;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.terminal.Terminal;

import java.util.List;

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
        if (StringUtil.isNotEmpty(input)) {
            TerminalHistory history = new TerminalHistory();
            history.setLine(input);
            this.addHistory(history);
        }
    }

    /**
     * 清除历史
     */
    void clearHistory();

    /**
     * 加载历史
     *
     * @return 历史记录
     */
    List<? extends TerminalHistory> listHistory();

    /**
     * 添加历史
     *
     * @param history 历史
     */
    void addHistory(TerminalHistory history);
}
