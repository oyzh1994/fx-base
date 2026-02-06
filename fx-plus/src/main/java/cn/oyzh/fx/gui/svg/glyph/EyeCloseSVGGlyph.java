package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class EyeCloseSVGGlyph extends SVGGlyph {

    public EyeCloseSVGGlyph() {
        this.setUrl("/fx-svg/eye-close.svg");
    }

    public EyeCloseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
