package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.StopSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class StopMenuItem extends FXMenuItem {

    public StopMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new StopSVGGlyph(iconSize), title, tipText, action);
    }
}
