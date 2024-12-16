package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class TitleBarMaximumSVGGlyph extends SVGGlyph {

    public TitleBarMaximumSVGGlyph() {
        this.setUrl("/fx-svg/titlebar/maximum.svg");
    }

    public TitleBarMaximumSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
