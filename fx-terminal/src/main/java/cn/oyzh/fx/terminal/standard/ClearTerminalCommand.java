package cn.oyzh.fx.terminal.standard;

import cn.oyzh.fx.terminal.command.TerminalCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author oyzh
 * @since 2023/7/22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClearTerminalCommand extends TerminalCommand {

    private boolean clearHis;
}
