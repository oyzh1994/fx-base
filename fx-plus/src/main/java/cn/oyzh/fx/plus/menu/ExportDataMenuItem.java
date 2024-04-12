package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.ExportSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.TerminalSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ExportDataMenuItem extends FXMenuItem {

    public ExportDataMenuItem(String iconSize, Runnable action) {
        super(new ExportSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.exportData"), null, action);
    }
}
