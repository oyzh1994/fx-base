package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class Down1SVGGlyph extends SVGGlyph {

    public Down1SVGGlyph() {
        this.setUrl("/fx-svg/down1.svg");
    }

    public Down1SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
