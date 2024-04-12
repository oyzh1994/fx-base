package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.AddGroupSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.EditSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class AddGroupMenuItem extends FXMenuItem{

    public AddGroupMenuItem(String iconSize, Runnable action) {
        super(new AddGroupSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.addGroup"), null, action);
    }
}
