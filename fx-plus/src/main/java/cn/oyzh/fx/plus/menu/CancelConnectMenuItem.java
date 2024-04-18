package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CancelConnectMenuItem extends CancelMenuItem {

    public CancelConnectMenuItem(String iconSize, Runnable action) {
        super(I18nResourceBundle.i18nString("base.cancelConnect"), null, iconSize, action);
    }
}
