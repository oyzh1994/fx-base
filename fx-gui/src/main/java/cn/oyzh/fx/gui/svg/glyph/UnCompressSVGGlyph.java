package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class UnCompressSVGGlyph extends SVGGlyph {

    public UnCompressSVGGlyph() {
        this.setUrl("/fx-svg/uncompress.svg");
    }

    public UnCompressSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
