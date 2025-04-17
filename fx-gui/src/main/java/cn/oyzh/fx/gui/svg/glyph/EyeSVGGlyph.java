package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class EyeSVGGlyph extends SVGGlyph {

    public EyeSVGGlyph() {
        this.setUrl("/fx-svg/eye.svg");
    }

    public EyeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
