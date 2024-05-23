package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class DeleteKeyMenuItem extends DeleteMenuItem {

    public DeleteKeyMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.deleteKey(), I18nHelper.deleteTip1(), iconSize, action);
    }
}
