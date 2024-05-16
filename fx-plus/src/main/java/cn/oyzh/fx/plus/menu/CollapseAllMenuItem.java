package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.CollapseAllSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CollapseAllMenuItem extends FXMenuItem{

    public CollapseAllMenuItem(String iconSize, Runnable action) {
        super(new CollapseAllSVGGlyph(iconSize), I18nHelper.collapseAll(), null, action);
    }
}
