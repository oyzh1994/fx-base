package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class WholeWordSVGGlyph extends SVGGlyph {

    public WholeWordSVGGlyph() {
        super("/fx-svg/whole-word.svg");
    }

    public WholeWordSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
