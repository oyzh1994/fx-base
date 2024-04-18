package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class RenameConnectMenuItem extends RenameMenuItem {

    public RenameConnectMenuItem(String iconSize, Runnable action) {
        super(I18nResourceBundle.i18nString("base.renameConnect"), I18nResourceBundle.i18nString("base.renameTip1"), iconSize, action);
    }
}
