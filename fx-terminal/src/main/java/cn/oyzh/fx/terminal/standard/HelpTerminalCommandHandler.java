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
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/7/22
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
            Collection<TerminalCommandHandler<?, ?>> handlers = TerminalManager.listHandler();
            List<String> list = new ArrayList<>(24);
            list.add(I18nHelper.orderNo());
            list.add(I18nHelper.cmd());
            list.add(I18nHelper.version());
            list.add(I18nHelper.status());
            list.add(I18nHelper.desc());
            int index = 0;
            for (TerminalCommandHandler<?, ?> handler : handlers) {
                list.add(++index + ")");
                list.add(handler.commandFullName());
                String version = handler.commandSupportedVersion();
                if (version == null) {
                    list.add(">=1.0.0");
                } else if (version.contains(".")) {
                    list.add(">=" + version);
                } else {
                    list.add(version);
                }
                list.add(handler.commandDeprecated() ? I18nHelper.deprecated() : I18nHelper.normal());
                list.add(handler.commandDesc());
            }
            result.setResult(TextUtil.beautifyFormat(list, 5, 2));
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("execute error", ex);
        }
        return result;
    }
}
