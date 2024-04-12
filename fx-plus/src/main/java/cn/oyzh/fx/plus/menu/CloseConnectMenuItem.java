package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.CloseSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.EditSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.QuitSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CloseConnectMenuItem extends FXMenuItem{

    public CloseConnectMenuItem(String iconSize, Runnable action) {
        super(new CloseSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.cancelConnect"), null, action);
    }
}
