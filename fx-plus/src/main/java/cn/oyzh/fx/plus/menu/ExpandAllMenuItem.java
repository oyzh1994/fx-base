package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.ExpandAllSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ExpandSVGLabel;
import cn.oyzh.fx.plus.controls.svg.ExportSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.LoadAllSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class ExpandAllMenuItem extends FXMenuItem {

    public ExpandAllMenuItem(String iconSize, Runnable action) {
        super(new ExpandAllSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.expandAll"), null, action);
    }
}
