package cn.oyzh.fx.terminal.command;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 终端命令
 *
 * @author oyzh
 * @since 2023/7/21
 */
@Data
@Accessors(chain = true, fluent = true)
public class TerminalCommand {

    /**
     * 参数列表
     */
    private String[] args;

}
