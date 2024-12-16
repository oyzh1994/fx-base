package cn.oyzh.fx.gui.svg.glyph.titlebar;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class TitleBarMinimumSVGGlyph extends SVGGlyph {

    public TitleBarMinimumSVGGlyph() {
        this.setUrl("/fx-svg/titlebar/minimum.svg");
    }

    public TitleBarMinimumSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
