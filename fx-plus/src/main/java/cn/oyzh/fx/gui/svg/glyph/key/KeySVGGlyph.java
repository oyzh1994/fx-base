package cn.oyzh.fx.gui.svg.glyph.key;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024-10-16
 */
public class KeySVGGlyph extends SVGGlyph {

    public KeySVGGlyph() {
        super("/fx-svg/key/key.svg");
    }

    public KeySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
