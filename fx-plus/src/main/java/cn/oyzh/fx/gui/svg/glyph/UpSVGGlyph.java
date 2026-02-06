package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class UpSVGGlyph extends SVGGlyph {

    public UpSVGGlyph() {
        this.setUrl("/fx-svg/up.svg");
    }

    public UpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
