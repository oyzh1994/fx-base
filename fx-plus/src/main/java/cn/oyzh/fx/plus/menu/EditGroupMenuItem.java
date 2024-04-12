package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class EditGroupMenuItem extends EditMenuItem {

    public EditGroupMenuItem(String iconSize, Runnable action) {
        super(BaseResourceBundle.getBaseString("base.editGroup"), null, iconSize, action);
    }
}
