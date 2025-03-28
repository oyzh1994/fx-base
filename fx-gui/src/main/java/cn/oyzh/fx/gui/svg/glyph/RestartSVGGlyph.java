package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/12
 */
public class RestartSVGGlyph extends SVGGlyph {

    public RestartSVGGlyph() {
        this.setUrl("/fx-svg/restart.svg");
    }

    public RestartSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

}
