package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.ExportSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ImportSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ImportMenuItem extends FXMenuItem {

    public ImportMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new ImportSVGGlyph(iconSize), title, tipText, action);
    }
}
