package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class RegexSVGGlyph extends ScalingSVGGlyph {

    public RegexSVGGlyph() {
        super("/fx-svg/regex.svg");
    }

    public RegexSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 0.85;
    }
}
