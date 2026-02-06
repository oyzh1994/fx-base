package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024-10-16
 */
public class GenerateSVGGlyph extends SVGGlyph {

    public GenerateSVGGlyph() {
        super("/fx-svg/generate.svg");
    }

    public GenerateSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
