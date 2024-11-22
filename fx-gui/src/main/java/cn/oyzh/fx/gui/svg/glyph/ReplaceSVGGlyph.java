package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ReplaceSVGGlyph extends SVGGlyph {

    public ReplaceSVGGlyph() {
        this.setUrl("/fx-gui/font/financial_replace.svg");
    }

    public ReplaceSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
