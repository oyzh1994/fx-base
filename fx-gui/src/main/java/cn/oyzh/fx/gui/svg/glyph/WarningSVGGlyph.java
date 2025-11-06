package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-02-14
 */
public class WarningSVGGlyph extends SVGGlyph {

    public WarningSVGGlyph() {
        this.setUrl("/fx-svg/warning.svg");
    }

    public WarningSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
