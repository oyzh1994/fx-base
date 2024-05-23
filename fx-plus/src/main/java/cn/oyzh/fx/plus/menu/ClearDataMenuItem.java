package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ClearDataMenuItem extends ClearMenuItem {

    public ClearDataMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.clearData(), null, iconSize, action);
    }
}
