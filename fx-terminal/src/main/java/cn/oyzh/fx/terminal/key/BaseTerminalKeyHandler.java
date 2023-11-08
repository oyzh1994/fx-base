package cn.oyzh.fx.terminal.key;

import cn.oyzh.fx.terminal.Terminal;
import cn.oyzh.fx.terminal.help.TerminalHelpHandler;
import cn.oyzh.fx.terminal.util.TerminalUtil;

/**
 * @author oyzh
 * @since 2023/7/21
 */
@Deprecated
public class BaseTerminalKeyHandler<T extends Terminal> implements TerminalKeyHandler<T> {

    @Override
    public boolean onEnterKeyPressed(T terminal) throws Exception {
        String input = terminal.getInput();
        if (TerminalUtil.hasHelp(input)) {
            TerminalHelpHandler helpHandler = terminal.helpHandler();
            if (helpHandler != null) {
                helpHandler.help(input, terminal);
            }
        } else if (input != null) {
            terminal.saveHistory(input);
            terminal.onCommand(input);
            // terminal.outputLineBreak();
            // terminal.outputPrompt();
        }
        return false;
    }
}
