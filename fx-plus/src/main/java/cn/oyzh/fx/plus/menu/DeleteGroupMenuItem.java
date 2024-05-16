package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class DeleteGroupMenuItem extends DeleteMenuItem {

    public DeleteGroupMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.deleteGroup(), I18nHelper.deleteTip1(), iconSize, action);
    }
}
