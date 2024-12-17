package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class TitleBarFullScreenSVGGlyph extends SVGGlyph {

    public TitleBarFullScreenSVGGlyph() {
        this.setUrl("/fx-svg/titlebar/fullscreen.svg");
    }

    public TitleBarFullScreenSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
