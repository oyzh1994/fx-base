package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.ImportSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ImportDataMenuItem extends FXMenuItem {

    public ImportDataMenuItem(String iconSize, Runnable action) {
        super(new ImportSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.importData"), null, action);
    }
}
