package cn.oyzh.fx.terminal.standard;

import cn.oyzh.fx.common.util.TextUtil;
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
 * @author oyzh
 * @since 2023/7/22
 */
// @Component
public class HelpTerminalCommandHandler extends BaseTerminalCommandHandler<TerminalCommand, Terminal> {

    // static {
    //     TerminalManager.registerHandler(HelpTerminalCommandHandler.class);
    // }

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
            Collection<TerminalCommandHandler<?,?>> handlers = TerminalManager.listHandler();
            List<String> list = new ArrayList<>();
            // list.add("序号");
            list.add(I18nResourceBundle.i18nString("base.orderNo"));
            // list.add("命令");
            list.add(I18nResourceBundle.i18nString("base.cmd"));
            // list.add("版本要求");
            list.add(I18nResourceBundle.i18nString("base.version"));
            // list.add("状态");
            list.add(I18nResourceBundle.i18nString("base.status"));
            // list.add("描述");
            list.add(I18nResourceBundle.i18nString("base.desc"));
            int index = 0;
            for (TerminalCommandHandler<?,?> handler : handlers) {
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
                list.add(handler.commandDeprecated() ? I18nResourceBundle.i18nString("base.deprecated") : I18nResourceBundle.i18nString("base.normal"));
                // list.add(handler.commandDeprecated() ? "过时" : "正常");
                list.add(handler.commandDesc());
            }
            result.setResult(TextUtil.beautifyFormat(list, 5, 2));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
