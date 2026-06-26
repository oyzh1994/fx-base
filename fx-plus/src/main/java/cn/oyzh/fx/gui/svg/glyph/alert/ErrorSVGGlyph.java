package cn.oyzh.fx.gui.svg.glyph.alert;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ErrorSVGGlyph extends SVGGlyph {

    public ErrorSVGGlyph() {
        super("/fx-svg/alert/error-fill.svg");
    }

    public ErrorSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
