package cn.oyzh.fx.gui.svg.glyph.alert;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class InfoSVGGlyph extends SVGGlyph {

    public InfoSVGGlyph() {
        this.setUrl("/fx-svg/alert/info-fill.svg");
    }

    public InfoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
