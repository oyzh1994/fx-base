package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class PermissionSVGGlyph extends SVGGlyph {

    public PermissionSVGGlyph() {
        this.setUrl("/fx-svg/permissions.svg");
    }

    public PermissionSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

}
