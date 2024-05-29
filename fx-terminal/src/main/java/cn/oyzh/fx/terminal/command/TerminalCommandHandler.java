package cn.oyzh.fx.terminal.command;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.terminal.Terminal;
import cn.oyzh.fx.terminal.complete.TerminalCompleteHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteHandler;

/**
 * 终端命令实现
 *
 * @author oyzh
 * @since 2023/7/21
 */
public interface TerminalCommandHandler<C extends TerminalCommand, T extends Terminal> extends TerminalCompleteHandler<T>, TerminalExecuteHandler<C, T> {

    /**
     * 解析命令
     *
     * @param input 输入
     * @return 命令
     * @throws Exception 异常
     */
    TerminalCommand parseCommand(String input) throws Exception;

    /**
     * 命令名称
     *
     * @return 名称
     */
    String commandName();

    /**
     * 命令子名称
     *
     * @return 子名称
     */
    default String commandSubName() {
        return null;
    }

    /**
     * 命令全名称
     *
     * @return 命令全名称
     */
    default String commandFullName() {
        String name = this.commandName();
        String subName = this.commandSubName();
        return subName == null ? name : name + " " + subName;
    }

    /**
     * 命令第一个支持的版本
     *
     * @return 版本
     */
    default String commandSupportedVersion() {
        return "1.0.0";
    }

    /**
     * 命令参数
     *
     * @return 命令参数
     */
    default String commandArg() {
        return "";
    }

    /**
     * 命令描述
     *
     * @return 命令描述
     */
    default String commandDesc() {
        return "";
    }

    /**
     * 命令帮助
     *
     * @param terminal 终端
     * @return 命令帮助
     */
    default String commandHelp(T terminal) {
        StringBuilder builder = new StringBuilder();
        if (StrUtil.isNotBlank(this.commandName())) {
            builder.append(" ").append(this.commandName());
        }
        if (StrUtil.isNotBlank(this.commandArg())) {
            builder.append(" ").append(this.commandArg());
        }
        return builder.isEmpty() ? "" : builder.substring(1);
    }

    /**
     * 命令是否过时
     *
     * @return 是否过时
     */
    default boolean commandDeprecated() {
        return false;
    }

}
