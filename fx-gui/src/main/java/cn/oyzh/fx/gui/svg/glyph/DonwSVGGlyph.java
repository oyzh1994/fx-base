package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class DonwSVGGlyph extends SVGGlyph {

    public DonwSVGGlyph() {
        this.setUrl("/fx-svg/down.svg");
    }

    public DonwSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
