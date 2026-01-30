package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class DownSVGGlyph extends SVGGlyph {

    public DownSVGGlyph() {
        this.setUrl("/fx-svg/down.svg");
    }

    public DownSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
