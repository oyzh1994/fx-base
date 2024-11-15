package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/5/14
 */
public class OpenSVGGlyph extends SVGGlyph {

    public OpenSVGGlyph() {
        this.setUrl("/fx-plus/font/open.svg");
    }

    public OpenSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
