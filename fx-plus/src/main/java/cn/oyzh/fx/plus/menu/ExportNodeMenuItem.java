package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ExportNodeMenuItem extends ExportMenuItem {

    public ExportNodeMenuItem(String iconSize, Runnable action) {
        super(I18nResourceBundle.i18nString("base.exportNode"), null, iconSize, action);
    }

}
