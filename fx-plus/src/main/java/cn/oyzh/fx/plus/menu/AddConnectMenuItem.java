package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.AddSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.EditSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class AddConnectMenuItem extends FXMenuItem{

    public AddConnectMenuItem(String iconSize, Runnable action) {
        super(new AddSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.addConnect"), null, action);
    }
}
