package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CopyKeyMenuItem extends CopyMenuItem {

    public CopyKeyMenuItem(String iconSize, Runnable action) {
        super(I18nResourceBundle.i18nString("base.copy", "base.key"), null, iconSize, action);
    }
}
