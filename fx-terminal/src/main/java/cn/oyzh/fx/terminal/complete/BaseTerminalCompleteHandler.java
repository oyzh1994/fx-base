package cn.oyzh.fx.terminal.complete;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.util.TextUtil;
import cn.oyzh.fx.terminal.Terminal;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

import java.util.List;

/**
 * 基础补全实现
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class BaseTerminalCompleteHandler<T extends Terminal> implements TerminalCompleteHandler<T> {

    protected List<TerminalCommandHandler<?,?>> findCommandHandlers(String line) {
        List<TerminalCommandHandler<?,?>> handlers;
        if (line.contains(" ")) {
            handlers = TerminalManager.findHandlers(line, 3);
        } else {
            handlers = TerminalManager.findHandlers(line, 1);
        }
        return handlers;
    }

    @Override
    public boolean completion(String line, T terminal) {
        if (StringUtil.isEmpty(line)) {
            HelpTerminalCommandHandler commandHandler = TerminalManager.findHandler(HelpTerminalCommandHandler.class);
            TerminalExecuteResult result = commandHandler.execute(null, terminal);
            terminal.outputLine((String) result.getResult());
            terminal.outputPrompt();
        } else {
            List<TerminalCommandHandler<?,?>> handlers = this.findCommandHandlers(line);
            if (handlers.isEmpty()) {
                this.noMatch(line, terminal);
            } else if (handlers.size() == 1) {
                this.oneMatch(line, terminal, handlers.get(0));
            } else {
                this.multiMatch(line, terminal, handlers);
            }
        }
        return true;
    }

    /**
     * 无匹配结果
     *
     * @param input    输入
     * @param terminal 终端
     */
    protected void noMatch(String input, T terminal) {

    }

    /**
     * 单个匹配结果
     *
     * @param input    输入
     * @param terminal 终端
     * @param handler  处理器
     */
    protected void oneMatch(String input, T terminal, TerminalCommandHandler handler) {
        if (!StringUtil.startWithIgnoreCase(input, handler.commandFullName())) {
            terminal.coverInput(handler.commandFullName());
        } else {
            handler.completion(input, terminal);
        }
    }

    /**
     * 多个匹配结果
     *
     * @param input    输入
     * @param terminal 终端
     * @param handlers 处理器列表
     */
    protected void multiMatch(String input, T terminal, List<TerminalCommandHandler<?,?>> handlers) {
        List<String> commands = handlers.parallelStream().map(TerminalCommandHandler::commandFullName).toList();
        String formatText = TextUtil.beautifyFormat(commands, 4, 0);
        terminal.outputByPrompt(formatText);
        terminal.outputPrompt();
        terminal.output(input);
    }
}
