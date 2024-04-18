package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CloseAllConnectTabMenuItem extends FXMenuItem {

    public CloseAllConnectTabMenuItem(Runnable action) {
        super(null, I18nResourceBundle.i18nString("base.closeAllConnectTab"), null, action);
    }
}
