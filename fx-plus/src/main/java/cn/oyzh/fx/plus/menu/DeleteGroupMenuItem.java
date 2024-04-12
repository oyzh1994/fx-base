package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.DeleteSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.EditSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class DeleteGroupMenuItem extends FXMenuItem{

    public DeleteGroupMenuItem(String iconSize, Runnable action) {
        super(new DeleteSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.deleteGroup"),  BaseResourceBundle.getBaseString("base.deleteTip1"), action);
    }
}
