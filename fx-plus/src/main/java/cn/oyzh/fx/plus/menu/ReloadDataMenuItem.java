package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ReloadDataMenuItem extends ReloadMenuItem {

    public ReloadDataMenuItem(String iconSize, Runnable action) {
        super(BaseResourceBundle.getBaseString("base.reloadData"), null, iconSize, action);
    }

}
