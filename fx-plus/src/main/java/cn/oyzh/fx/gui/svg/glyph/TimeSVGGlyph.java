package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class TimeSVGGlyph extends SVGGlyph {

    public TimeSVGGlyph() {
        this.setUrl("/fx-svg/time.svg");
    }

    public TimeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
