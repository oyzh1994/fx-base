package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.CloseSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CloseMenuItem extends FXMenuItem{

    public CloseMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new CloseSVGGlyph(iconSize), title, tipText, action);
    }
}
