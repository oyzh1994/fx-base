package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class EyeOpenSVGGlyph extends SVGGlyph {

    public EyeOpenSVGGlyph() {
        this.setUrl("/fx-svg/eye-open.svg");
    }

    public EyeOpenSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
