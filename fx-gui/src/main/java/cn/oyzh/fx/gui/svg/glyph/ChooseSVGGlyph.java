package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/07/04
 */
public class ChooseSVGGlyph extends SVGGlyph {

    public ChooseSVGGlyph() {
        this.setUrl("/fx-plus/font/choose.svg");
    }

    public ChooseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
