package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.RepeatSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class RepeatConnectMenuItem extends FXMenuItem{

    public RepeatConnectMenuItem(String iconSize, Runnable action) {
        super(new RepeatSVGGlyph(iconSize), I18nHelper.repeatConnect(), null, action);
    }
}
