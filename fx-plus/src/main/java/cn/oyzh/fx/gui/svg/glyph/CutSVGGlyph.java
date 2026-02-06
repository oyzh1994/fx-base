package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class CutSVGGlyph extends SVGGlyph {

    public CutSVGGlyph() {
        this.setUrl("/fx-svg/cut.svg");
    }

    public CutSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
