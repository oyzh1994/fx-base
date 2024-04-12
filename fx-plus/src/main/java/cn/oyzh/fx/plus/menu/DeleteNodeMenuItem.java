package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class DeleteNodeMenuItem extends DeleteMenuItem {

    public DeleteNodeMenuItem(String iconSize, Runnable action) {
        super(BaseResourceBundle.getBaseString("base.deleteNode"), BaseResourceBundle.getBaseString("base.deleteTip1"), iconSize, action);
    }
}
