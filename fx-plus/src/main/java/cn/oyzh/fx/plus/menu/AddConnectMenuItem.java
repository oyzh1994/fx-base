package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class AddConnectMenuItem extends AddMenuItem {

    public AddConnectMenuItem(String iconSize, Runnable action) {
        super(BaseResourceBundle.getBaseString("base.addConnect"), null, iconSize, action);
    }
}
