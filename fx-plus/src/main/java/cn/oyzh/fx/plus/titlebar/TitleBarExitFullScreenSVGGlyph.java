package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class TitleBarExitFullScreenSVGGlyph extends SVGGlyph {

    public TitleBarExitFullScreenSVGGlyph() {
        this.setUrl("/fx-svg/titlebar/exit-fullscreen.svg");
    }

    public TitleBarExitFullScreenSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
