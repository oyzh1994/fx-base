package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.ExportSVGGlyph;
import cn.oyzh.fx.plus.controls.svg.TransportSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ExportMenuItem extends FXMenuItem {

    public ExportMenuItem(String title, String tipText, String iconSize, Runnable action) {
        super(new ExportSVGGlyph(iconSize), title, tipText, action);
    }
}
