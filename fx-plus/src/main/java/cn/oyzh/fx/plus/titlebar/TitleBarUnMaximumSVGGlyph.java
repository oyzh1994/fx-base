package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class TitleBarUnMaximumSVGGlyph extends ActionSVGGlyph {

    public TitleBarUnMaximumSVGGlyph() {
        this.setUrl("/fx-svg/titlebar/un-maximum.svg");
    }

    public TitleBarUnMaximumSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
