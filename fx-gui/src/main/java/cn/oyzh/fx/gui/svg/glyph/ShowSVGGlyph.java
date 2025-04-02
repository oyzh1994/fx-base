package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ShowSVGGlyph extends SVGGlyph {

    public ShowSVGGlyph() {
        this.setUrl("/fx-svg/show.svg");
    }

    public ShowSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
