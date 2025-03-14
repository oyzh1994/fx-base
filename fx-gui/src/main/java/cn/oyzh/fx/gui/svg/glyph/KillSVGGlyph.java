package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class KillSVGGlyph extends SVGGlyph {

    public KillSVGGlyph() {
        this.setUrl("/fx-svg/kill.svg");
    }

    public KillSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
