package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/12
 */
public class PauseSVGGlyph extends SVGGlyph {

    public PauseSVGGlyph() {
        this.setUrl("/fx-svg/pause.svg");
    }

    public PauseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

}
