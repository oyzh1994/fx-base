package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.PlaySVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CancelConnectMenuItem extends FXMenuItem{

    public CancelConnectMenuItem(String iconSize, Runnable action) {
        super(new PlaySVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.startConnect"), BaseResourceBundle.getBaseString("base.startTip1"), action);
    }
}
