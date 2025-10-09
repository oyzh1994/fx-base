package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ExecuteSVGGlyph extends SVGGlyph {

    public ExecuteSVGGlyph() {
        this.setUrl("/fx-svg/execute.svg");
    }

    public ExecuteSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
