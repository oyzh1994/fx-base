package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/12
 */
public class ResourceSVGGlyph extends SVGGlyph {

    public ResourceSVGGlyph() {
        this.setUrl("/fx-svg/resource.svg");
    }

    public ResourceSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

}
