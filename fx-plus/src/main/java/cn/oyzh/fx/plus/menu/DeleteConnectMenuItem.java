package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class DeleteConnectMenuItem extends DeleteMenuItem {

    public DeleteConnectMenuItem(String iconSize, Runnable action) {
        super(BaseResourceBundle.getBaseString("base.deleteConnect"), BaseResourceBundle.getBaseString("base.deleteTip1"), iconSize, action);
    }
}
