package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.UnLockSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class AuthNodeMenuItem extends AuthMenuItem{

    public AuthNodeMenuItem(  String iconSize, Runnable action) {
        super(BaseResourceBundle.getBaseString("base.authNode"), null, iconSize, action);
    }
}
