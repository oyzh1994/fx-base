package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class DeleteConnectMenuItem extends DeleteMenuItem {

    public DeleteConnectMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.deleteConnect(), I18nHelper.deleteTip1(), iconSize, action);
    }
}
