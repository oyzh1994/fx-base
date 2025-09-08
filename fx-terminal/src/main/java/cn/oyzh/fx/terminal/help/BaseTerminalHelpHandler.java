package cn.oyzh.fx.terminal.help;

import cn.oyzh.fx.terminal.Terminal;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * @author oyzh
 * @since 2023/7/21
 */
public class BaseTerminalHelpHandler<T extends Terminal> implements TerminalHelpHandler<T> {

    @Override
    public void help(String input, T terminal) {
        String command = input.split(" -?")[0];
        TerminalCommandHandler handler = TerminalManager.findHandler(terminal.terminalName(), command);
        if (handler != null) {
            String help = handler.commandHelp(terminal);
            if (help != null) {
                terminal.outputLine(help);
                terminal.outputPrompt();
            }
        }
    }
}
