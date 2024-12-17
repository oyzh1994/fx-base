package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class TitleBarCloseSVGGlyph extends ActionSVGGlyph {

    public TitleBarCloseSVGGlyph() {
        this.setUrl("/fx-svg/titlebar/close.svg");
    }

    public TitleBarCloseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
