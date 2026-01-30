package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/07/21
 */
public class DateSVGGlyph extends SVGGlyph {

    public DateSVGGlyph() {
        this.setUrl("/fx-svg/date.svg");
    }

    public DateSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}

