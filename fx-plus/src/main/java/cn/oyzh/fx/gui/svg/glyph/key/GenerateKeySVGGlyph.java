package cn.oyzh.fx.gui.svg.glyph.key;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024-10-16
 */
public class GenerateKeySVGGlyph extends SVGGlyph {

    public GenerateKeySVGGlyph() {
        super("/fx-svg/key/generate-key.svg");
    }

    public GenerateKeySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
