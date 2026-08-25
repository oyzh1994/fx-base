package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ParentDirSVGGlyph extends SVGGlyph {

    public ParentDirSVGGlyph() {
        super("/fx-svg/parent-dir.svg");
    }

    public ParentDirSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
