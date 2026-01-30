package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ToolsSVGGlyph extends SVGGlyph {

    public ToolsSVGGlyph() {
        this.setUrl("/fx-svg/tools.svg");
    }

    public ToolsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
