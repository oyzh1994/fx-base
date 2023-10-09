package cn.oyzh.fx.terminal.help;

import cn.oyzh.fx.terminal.Terminal;

/**
 * @author oyzh
 * @since 2023/7/21
 */
public interface TerminalHelpHandler<T extends Terminal> {

    /**
     * 帮助
     *
     * @param input    内容
     * @param terminal 终端
     */
    void help(String input, T terminal);
}
