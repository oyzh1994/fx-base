package cn.oyzh.fx.terminal.histroy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.terminal.Terminal;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author oyzh
 * @since 2023/08/28
 */
public class BaseTerminalHistoryHandler implements TerminalHistoryHandler {

    /**
     * 当前命令
     */
    private TerminalHistory history;

    /**
     * 历史储存
     */
    @Getter
    @Setter
    @Accessors(fluent = true, chain = false)
    private TerminalHistoryStore historyStore;

    public BaseTerminalHistoryHandler(TerminalHistoryStore historyStore) {
        this.historyStore = historyStore;
    }

    @Override
    public String prevCommand(Terminal terminal) {
        // 获取命令历史
        List<TerminalHistory> histories = this.historyStore().load();
        // 重置命令
        TerminalHistory commandHistory = null;
        if (StringUtil.isEmpty(terminal.getInput())) {
            this.history = null;
        }
        // 寻找命令
        if (this.history != null) {
            int index = histories.indexOf(this.history);
            if (index - 1 >= 0) {
                commandHistory = histories.get(index - 1);
            }
        } else {
            commandHistory = CollectionUtil.getLast(histories);
        }
        if (commandHistory != null) {
            this.history = commandHistory;
            return this.history.getLine();
        }
        return null;
    }

    @Override
    public String nextCommand(Terminal terminal) {
        // 获取命令历史
        List<TerminalHistory> histories = this.historyStore().load();
        if (histories.isEmpty()) {
            return null;
        }
        // 重置命令
        if (StringUtil.isEmpty(terminal.getInput())) {
            this.history = null;
        }
        // 寻找命令
        if (this.history != null) {
            int index = histories.indexOf(this.history);
            if (index != -1 && index + 1 < histories.size()) {
                this.history = histories.get(index + 1);
                return this.history.getLine();
            }
        }
        return null;
    }
}
