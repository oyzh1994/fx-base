package cn.oyzh.fx.gui.svg.glyph.layout;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class Layout2SVGGlyph extends SVGGlyph {

    public Layout2SVGGlyph() {
        this.setUrl("/fx-svg/layout/layout2.svg");
    }

    public Layout2SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
