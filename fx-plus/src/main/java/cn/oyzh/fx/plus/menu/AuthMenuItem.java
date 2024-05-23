package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.UnLockSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class AuthMenuItem extends FXMenuItem{

    public AuthMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new UnLockSVGGlyph(iconSize), title, tipText, action);
    }
}
