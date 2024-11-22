package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class HomeSVGGlyph extends SVGGlyph {

    public HomeSVGGlyph() {
        this.setUrl("/fx-gui/font/home.svg");
    }

    public HomeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
