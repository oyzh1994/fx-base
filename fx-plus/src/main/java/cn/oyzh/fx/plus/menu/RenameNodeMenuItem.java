package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class RenameNodeMenuItem extends RenameMenuItem {

    public RenameNodeMenuItem(String iconSize, Runnable action) {
        super(BaseResourceBundle.getBaseString("base.renameNode"), BaseResourceBundle.getBaseString("base.renameTip1"), iconSize, action);
    }
}
