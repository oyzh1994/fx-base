package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.PlaySVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class StartConnectMenuItem extends FXMenuItem{

    public StartConnectMenuItem(String iconSize, Runnable action) {
        super(new PlaySVGGlyph(iconSize), I18nResourceBundle.i18nString("base.startConnect"), I18nResourceBundle.i18nString("base.startTip1"), action);
    }
}
