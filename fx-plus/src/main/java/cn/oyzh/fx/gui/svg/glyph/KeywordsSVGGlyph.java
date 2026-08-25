package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024-10-16
 */
public class KeywordsSVGGlyph extends SVGGlyph {

    public KeywordsSVGGlyph() {
        super("/fx-svg/keywords.svg");
    }

    public KeywordsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
