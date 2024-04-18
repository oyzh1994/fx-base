package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.LoadAllSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class LoadAllMenuItem extends FXMenuItem {

    public LoadAllMenuItem(String iconSize, Runnable action) {
        super(new LoadAllSVGGlyph(iconSize), I18nResourceBundle.i18nString("base.loadAll"), null, action);
    }
}
