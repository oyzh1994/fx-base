package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.ClearSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ClearMenuItem extends FXMenuItem {

    public ClearMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new ClearSVGGlyph(iconSize), title, tipText, action);
    }
}
