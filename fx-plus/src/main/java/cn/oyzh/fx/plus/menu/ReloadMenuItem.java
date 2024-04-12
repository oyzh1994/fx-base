package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.RefreshSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ReloadMenuItem extends FXMenuItem {

    public ReloadMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new RefreshSVGGlyph(iconSize), title, tipText, action);
    }
}
