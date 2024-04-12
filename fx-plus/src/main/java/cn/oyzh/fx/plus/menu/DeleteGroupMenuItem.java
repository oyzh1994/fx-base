package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class DeleteGroupMenuItem extends DeleteMenuItem {

    public DeleteGroupMenuItem(String iconSize, Runnable action) {
        super(BaseResourceBundle.getBaseString("base.deleteGroup"), BaseResourceBundle.getBaseString("base.deleteTip1"), iconSize, action);
    }
}
