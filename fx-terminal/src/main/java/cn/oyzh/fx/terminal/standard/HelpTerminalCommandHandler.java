package cn.oyzh.fx.terminal.standard;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.Terminal;
import cn.oyzh.fx.terminal.command.BaseTerminalCommandHandler;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import cn.oyzh.fx.terminal.util.TerminalManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 帮助终端命令处理器
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class HelpTerminalCommandHandler extends BaseTerminalCommandHandler<TerminalCommand, Terminal> {

    @Override
    public String commandName() {
        return "help";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.get", "base.help");
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, Terminal terminal) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        try {
            Collection<TerminalCommandHandler<?, ?>> handlers = TerminalManager.listHandler(terminal.terminalName());
            List<String> list = new ArrayList<>(24);
            //            list.add(I18nHelper.orderNo());
            //            list.add(I18nHelper.cmd());
            //            list.add(I18nHelper.version());
            //            list.add(I18nHelper.status());
            //            list.add(I18nHelper.desc());
            for (TerminalCommandHandler<?, ?> handler : handlers) {
                //                list.add(++index + ")");
                list.add(handler.commandFullName());
                //                String version = handler.commandSupportedVersion();
                //                if (version == null) {
                //                    list.add(">=1.0.0");
                //                } else if (version.contains(".")) {
                //                    list.add(">=" + version);
                //                } else {
                //                    list.add(version);
                //                }
                //                list.add(handler.commandDeprecated() ? I18nHelper.deprecated() : I18nHelper.normal());
                //                list.add(handler.commandDesc());
            }
            //            result.setResult(TextUtil.beautifyFormat(list, 5, 2));
            result.setResult(this.formatOutput(list));
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("execute error", ex);
        }
        return result;
    }

    protected String formatOutput(Collection<String> list) {
        int index = 0;
        List<String> list1 = new ArrayList<>();
        for (String s : list) {
            list1.add(++index + ") " + s);
        }
        return TextUtil.format(list1);
    }
}
