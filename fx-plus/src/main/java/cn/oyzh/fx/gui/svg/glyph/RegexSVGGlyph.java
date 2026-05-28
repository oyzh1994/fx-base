package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class RegexSVGGlyph extends SVGGlyph {

    public RegexSVGGlyph() {
        super("/fx-svg/regex.svg");
    }

    public RegexSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
