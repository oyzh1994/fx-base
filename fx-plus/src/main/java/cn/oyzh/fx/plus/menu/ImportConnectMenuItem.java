package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ImportConnectMenuItem extends ImportMenuItem {

    public ImportConnectMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.importConnect(), I18nResourceBundle.i18nString("base.importTip1"), iconSize, action);
    }
}
