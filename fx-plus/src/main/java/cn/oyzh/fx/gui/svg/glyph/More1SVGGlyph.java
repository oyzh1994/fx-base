package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class More1SVGGlyph extends SVGGlyph {

    public More1SVGGlyph() {
        this.setUrl("/fx-svg/more1.svg");
    }

    public More1SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
