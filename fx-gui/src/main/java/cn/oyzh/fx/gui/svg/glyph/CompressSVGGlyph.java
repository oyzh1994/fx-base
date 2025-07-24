package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class CompressSVGGlyph extends SVGGlyph {

    public CompressSVGGlyph() {
        this.setUrl("/fx-svg/compress.svg");
    }

    public CompressSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
