package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class BoxSVGGlyph extends SVGGlyph {

    public BoxSVGGlyph() {
        this.setUrl("/fx-svg/box.svg");
    }

    public BoxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
