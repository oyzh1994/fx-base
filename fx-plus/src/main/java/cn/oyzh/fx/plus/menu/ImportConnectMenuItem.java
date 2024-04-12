package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ImportConnectMenuItem extends ImportMenuItem {

    public ImportConnectMenuItem(String iconSize, Runnable action) {
        super(BaseResourceBundle.getBaseString("base.importConnect"), BaseResourceBundle.getBaseString("base.importTip1"), iconSize, action);
    }
}
