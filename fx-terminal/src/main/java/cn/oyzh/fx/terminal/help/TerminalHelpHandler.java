package cn.oyzh.fx.terminal.help;

import cn.oyzh.fx.terminal.Terminal;

/**
 * 终端帮助处理器
 *
 * @author oyzh
 * @since 2023-10-09
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
