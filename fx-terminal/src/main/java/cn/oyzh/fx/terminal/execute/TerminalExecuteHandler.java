package cn.oyzh.fx.terminal.execute;

import cn.oyzh.fx.terminal.Terminal;
import cn.oyzh.fx.terminal.command.TerminalCommand;

/**
 * 终端执行处理
 *
 * @author oyzh
 * @since 2023/7/21
 */
public interface TerminalExecuteHandler<C extends TerminalCommand, T extends Terminal> {

    /**
     * 执行命令
     *
     * @param command  命令
     * @param terminal 终端
     * @return 结果
     */
    TerminalExecuteResult execute(C command, T terminal);

}
