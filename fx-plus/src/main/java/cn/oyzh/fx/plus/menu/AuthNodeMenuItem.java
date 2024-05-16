package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.i18n.I18nAdapter;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class AuthNodeMenuItem extends AuthMenuItem {

    public AuthNodeMenuItem(String iconSize, Runnable action) {
        super(I18nHelper.authNode(), null, iconSize, action);
    }
}
