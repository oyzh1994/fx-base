package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CloseCurrConnectTabMenuItem extends FXMenuItem {

    public CloseCurrConnectTabMenuItem(Runnable action) {
        super(null, BaseResourceBundle.getBaseString("base.closeCurrConnectTab"), null, action);
    }
}
