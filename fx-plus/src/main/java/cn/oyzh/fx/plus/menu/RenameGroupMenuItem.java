package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class RenameGroupMenuItem extends RenameMenuItem {

    public RenameGroupMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.renameGroup(), I18nResourceBundle.i18nString("base.renameTip1"), iconSize, action);
    }
}
