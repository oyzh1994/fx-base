package cn.oyzh.fx.gui.svg.glyph.alert;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class WarningSVGGlyph extends SVGGlyph {

    public WarningSVGGlyph() {
        super("/fx-svg/alert/warning-fill.svg");
    }

    public WarningSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
