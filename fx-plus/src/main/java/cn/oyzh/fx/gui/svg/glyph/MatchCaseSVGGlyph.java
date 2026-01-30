package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class MatchCaseSVGGlyph extends SVGGlyph {

    public MatchCaseSVGGlyph() {
        this.setUrl("/fx-svg/match_case.svg");
    }

    public MatchCaseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
