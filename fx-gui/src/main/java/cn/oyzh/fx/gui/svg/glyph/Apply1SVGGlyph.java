package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class Apply1SVGGlyph extends SVGGlyph {

    public Apply1SVGGlyph() {
        this.setUrl("/fx-svg/apply1.svg");
    }

    public Apply1SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
