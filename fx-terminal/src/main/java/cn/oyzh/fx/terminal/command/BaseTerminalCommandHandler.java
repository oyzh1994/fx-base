package cn.oyzh.fx.terminal.command;

import cn.oyzh.fx.terminal.Terminal;
import cn.oyzh.fx.terminal.exception.TerminalException;
import cn.oyzh.fx.terminal.util.TerminalManager;
import cn.oyzh.fx.terminal.util.TerminalUtil;
import org.checkerframework.checker.units.qual.C;

/**
 * 基础终端命令处理实现
 *
 * @author oyzh
 * @since 2023/7/21
 */
public abstract class BaseTerminalCommandHandler<C extends TerminalCommand, T extends Terminal> implements TerminalCommandHandler<C, T> {

    @Override
    public C parseCommand(String line) throws Exception {
        String[] words = TerminalUtil.split(line);
        if (!this.checkArgs(words)) {
            throw new TerminalException("ERR wrong number of arguments for '" + this.commandName() + "' command");
        }
        return this.parseCommand(line, words);
    }

    @Override
    public boolean completion(String line, T terminal) {
        return false;
    }

    protected boolean checkArgs(String[] words) throws RuntimeException {
        return words != null && words.length >= 1;
    }

    protected C parseCommand(String line, String[] words) throws Exception {
        TerminalCommand command = new TerminalCommand();
        command.parseArgs(words);
        return (C) command;
    }
}
