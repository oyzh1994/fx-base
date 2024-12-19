package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class BinarySVGGlyph extends SVGGlyph {

    public BinarySVGGlyph() {
        this.setUrl("/fx-svg/binary.svg");
    }

    public BinarySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
