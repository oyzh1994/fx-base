package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.EditSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.RenameSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class RenameGroupMenuItem extends FXMenuItem{

    public RenameGroupMenuItem(String iconSize, Runnable action) {
        super(new RenameSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.renameGroup"), BaseResourceBundle.getBaseString("base.renameTip1"), action);
    }
}
