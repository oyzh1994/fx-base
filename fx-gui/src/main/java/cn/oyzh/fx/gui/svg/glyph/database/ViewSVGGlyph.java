package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/5/14
 */
public class ViewSVGGlyph extends SVGGlyph {

    public ViewSVGGlyph() {
        this.setUrl("/fx-svg/view.svg");
    }

    public ViewSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
