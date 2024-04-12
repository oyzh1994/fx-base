package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CloseAllConnectTabMenuItem extends FXMenuItem {

    public CloseAllConnectTabMenuItem(Runnable action) {
        super(null, BaseResourceBundle.getBaseString("base.closeAllConnectTab"), null, action);
    }
}
