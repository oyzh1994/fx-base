package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.RepeatSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class RepeatConnectMenuItem extends FXMenuItem{

    public RepeatConnectMenuItem(String iconSize, Runnable action) {
        super(new RepeatSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.repeatConnect"), null, action);
    }
}
