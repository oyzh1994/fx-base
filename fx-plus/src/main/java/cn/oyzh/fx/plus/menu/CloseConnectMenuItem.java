package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CloseConnectMenuItem extends StopMenuItem {

    public CloseConnectMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.closeConnect(), null, iconSize, action);
    }
}
