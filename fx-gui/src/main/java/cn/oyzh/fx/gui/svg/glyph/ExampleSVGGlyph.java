package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/07/05
 */
public class ExampleSVGGlyph extends SVGGlyph {

    public ExampleSVGGlyph() {
        this.setUrl("/fx-svg/example.svg");
    }

    public ExampleSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
