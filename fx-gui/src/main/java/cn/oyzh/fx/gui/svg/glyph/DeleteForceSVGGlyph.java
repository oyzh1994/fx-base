package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class DeleteForceSVGGlyph extends SVGGlyph {

    public DeleteForceSVGGlyph() {
        this.setUrl("/fx-svg/delete-force.svg");
    }

    public DeleteForceSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
