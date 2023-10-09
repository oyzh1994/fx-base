package cn.oyzh.fx.terminal.complete;

import cn.oyzh.fx.terminal.Terminal;

/**
 * 补全处理器
 *
 * @author oyzh
 * @since 2023/7/21
 */
public interface TerminalCompleteHandler<T extends Terminal> {

    /**
     * 补全
     *
     * @param input    内容
     * @param terminal 终端
     * @return 结果
     */
    boolean completion(String input, T terminal);
}
