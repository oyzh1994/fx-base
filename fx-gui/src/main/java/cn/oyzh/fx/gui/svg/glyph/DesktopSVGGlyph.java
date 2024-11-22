package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class DesktopSVGGlyph extends SVGGlyph {

    public DesktopSVGGlyph() {
        this.setUrl("/fx-gui/font/desktop.svg");
    }

    public DesktopSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
