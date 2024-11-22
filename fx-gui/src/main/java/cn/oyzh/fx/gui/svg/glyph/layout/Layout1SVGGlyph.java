package cn.oyzh.fx.gui.svg.glyph.layout;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class Layout1SVGGlyph extends SVGGlyph {

    public Layout1SVGGlyph() {
        this.setUrl("/fx-svg/layout/layout1.svg");
    }

    public Layout1SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
