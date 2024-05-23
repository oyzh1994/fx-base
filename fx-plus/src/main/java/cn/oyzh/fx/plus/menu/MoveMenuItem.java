package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.MoveSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class MoveMenuItem extends FXMenuItem {

    public MoveMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new MoveSVGGlyph(iconSize), title, tipText, action);
    }
}
