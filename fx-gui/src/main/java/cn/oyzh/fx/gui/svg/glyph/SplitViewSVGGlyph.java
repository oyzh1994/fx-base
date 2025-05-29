package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class SplitViewSVGGlyph extends SVGGlyph {

    public SplitViewSVGGlyph() {
        this.setUrl("/fx-svg/splitView.svg");
    }

    public SplitViewSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
