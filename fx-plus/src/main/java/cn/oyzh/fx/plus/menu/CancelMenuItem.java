package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.CancelSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CancelMenuItem extends FXMenuItem {

    public CancelMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new CancelSVGGlyph(iconSize), title, tipText, action);
    }
}
