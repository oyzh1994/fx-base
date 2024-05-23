package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.DeleteSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class DeleteMenuItem extends FXMenuItem {

    public DeleteMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new DeleteSVGGlyph(iconSize), title, tipText, action);
    }
}
