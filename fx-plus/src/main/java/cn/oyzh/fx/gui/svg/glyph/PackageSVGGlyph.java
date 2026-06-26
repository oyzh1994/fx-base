package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class PackageSVGGlyph extends SVGGlyph {

    public PackageSVGGlyph() {
        super("/fx-svg/package.svg");
    }

    public PackageSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
