package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/07/05
 */
public class ExampleSVGGlyph extends SVGGlyph {

    public ExampleSVGGlyph() {
        this.setUrl("/fx-plus/font/example.svg");
    }

    public ExampleSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
