package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ApplySVGGlyph extends SVGGlyph {

    public ApplySVGGlyph() {
        this.setUrl("/fx-svg/apply1.svg");
    }

    public ApplySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
