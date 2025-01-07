package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class AboutSVGGlyph extends SVGGlyph {

    public AboutSVGGlyph() {
        this.setUrl("/fx-svg/info-circle.svg");
    }

    public AboutSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
