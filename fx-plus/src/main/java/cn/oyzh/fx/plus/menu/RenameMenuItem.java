package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.EditSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ExportSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.RenameSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class RenameMenuItem extends FXMenuItem{

    public RenameMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new RenameSVGGlyph(iconSize), title, tipText, action);
    }
}
