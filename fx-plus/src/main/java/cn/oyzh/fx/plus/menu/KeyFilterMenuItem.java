package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class KeyFilterMenuItem extends FilterMenuItem {

    public KeyFilterMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.keyFilter(), null, iconSize, action);
    }
}
