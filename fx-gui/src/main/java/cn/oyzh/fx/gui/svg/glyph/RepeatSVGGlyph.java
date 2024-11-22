package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class RepeatSVGGlyph extends SVGGlyph {

    public RepeatSVGGlyph() {
        this.setUrl("/fx-gui/font/repeated.svg");
    }

    public RepeatSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
