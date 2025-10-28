package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class Up1SVGGlyph extends SVGGlyph {

    public Up1SVGGlyph() {
        this.setUrl("/fx-svg/up1.svg");
    }

    public Up1SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
