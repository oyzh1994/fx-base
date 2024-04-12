package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.DeleteSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.EditSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class DeleteConnectMenuItem extends FXMenuItem{

    public DeleteConnectMenuItem(String iconSize, Runnable action) {
        super(new DeleteSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.deleteConnect"), BaseResourceBundle.getBaseString("base.deleteTip1"), action);
    }
}
