package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.FileSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.MoveSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class FilterMenuItem extends FXMenuItem {

    public FilterMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new FileSVGGlyph(iconSize), title, tipText, action);
    }
}
