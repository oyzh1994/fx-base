package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class LoadAllSVGGlyph extends SVGGlyph {

    public LoadAllSVGGlyph() {
        this.setUrl("/fx-svg/reload time.svg");
    }

    public LoadAllSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
