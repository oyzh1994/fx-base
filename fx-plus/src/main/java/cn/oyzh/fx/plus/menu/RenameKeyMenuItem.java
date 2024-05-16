package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class RenameKeyMenuItem extends RenameMenuItem {

    public RenameKeyMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.renameKey(), I18nResourceBundle.i18nString("base.renameTip1"), iconSize, action);
    }
}
