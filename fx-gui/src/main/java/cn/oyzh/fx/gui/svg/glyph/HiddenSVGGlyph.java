package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class HiddenSVGGlyph extends SVGGlyph {

    public HiddenSVGGlyph() {
        this.setUrl("/fx-svg/hidden.svg");
    }

    public HiddenSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
