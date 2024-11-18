package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/5/14
 */
public class InfoSVGGlyph extends SVGGlyph {

    public InfoSVGGlyph() {
        this.setUrl("/fx-plus/font/info-circle.svg");
    }

    public InfoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
