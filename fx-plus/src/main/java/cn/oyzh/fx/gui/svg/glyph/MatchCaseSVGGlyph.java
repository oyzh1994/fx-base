package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class MatchCaseSVGGlyph extends ScalingSVGGlyph {

    public MatchCaseSVGGlyph() {
        super("/fx-svg/match_case.svg");
    }

    public MatchCaseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.83;
    }
}
