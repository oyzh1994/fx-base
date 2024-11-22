package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SelectSVGGlyph extends SVGGlyph {

    public SelectSVGGlyph() {
        this.setUrl("/fx-gui/font/select.svg");
    }

    public SelectSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
