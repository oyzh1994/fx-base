package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.ServerSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class ViewInfoMenuItem extends FXMenuItem {

    public ViewInfoMenuItem(String iconSize, Runnable action) {
        super(new ServerSVGGlyph(iconSize), I18nHelper.viewInfo(), null, action);
    }
}
