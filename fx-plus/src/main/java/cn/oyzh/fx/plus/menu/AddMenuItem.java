package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.AddSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class AddMenuItem extends FXMenuItem {

    public AddMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new AddSVGGlyph(iconSize), title, tipText, action);
    }
}
