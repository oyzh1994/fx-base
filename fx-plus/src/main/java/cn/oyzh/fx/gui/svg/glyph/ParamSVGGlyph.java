package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024-10-16
 */
public class ParamSVGGlyph extends SVGGlyph {

    public ParamSVGGlyph() {
        super("/fx-svg/param.svg");
    }

    public ParamSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
