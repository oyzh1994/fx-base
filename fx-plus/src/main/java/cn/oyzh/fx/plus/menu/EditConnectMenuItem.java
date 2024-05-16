package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class EditConnectMenuItem extends EditMenuItem{

    public EditConnectMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.editConnect(), null, iconSize, action);
    }
}
