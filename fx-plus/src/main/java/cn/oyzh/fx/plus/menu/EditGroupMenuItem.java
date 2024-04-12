package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.EditSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class EditGroupMenuItem extends FXMenuItem{

    public EditGroupMenuItem(String iconSize, Runnable action) {
        super(new EditSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.editGroup"), null, action);
    }
}
