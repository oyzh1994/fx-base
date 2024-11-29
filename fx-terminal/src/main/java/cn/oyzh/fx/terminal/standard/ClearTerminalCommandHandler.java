package cn.oyzh.fx.terminal.standard;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.terminal.Terminal;
import cn.oyzh.fx.terminal.command.BaseTerminalCommandHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/7/22
 */
public class ClearTerminalCommandHandler extends BaseTerminalCommandHandler<ClearTerminalCommand, Terminal> {

    @Override
    public String commandName() {
        return "clear";
    }

    @Override
    public String commandArg() {
        return "[-his]";
    }

    @Override
    public String commandDesc() {
        return I18nHelper.clearWindow();
    }

    @Override
    public String commandHelp(Terminal terminal) {
        return super.commandHelp(terminal) + "\n-his " + I18nHelper.history();
    }

    @Override
    public TerminalExecuteResult execute(ClearTerminalCommand command, Terminal terminal) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        try {
            terminal.clear();
            if (command != null && command.isClearHis()) {
                terminal.clearHistory();
            }
            result.setResult("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    protected boolean checkArgs(String[] words) throws RuntimeException {
        return words.length == 1 || words.length == 2;
    }

    @Override
    protected ClearTerminalCommand parseCommand(String line, String[] words) throws RuntimeException {
        if (words.length == 2 && StringUtil.equalsIgnoreCase(words[1], "-his")) {
            ClearTerminalCommand command = new ClearTerminalCommand();
            command.setClearHis(true);
            return command;
        }
        return null;
    }
}
