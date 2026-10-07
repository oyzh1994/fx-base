package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 权限 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class PermissionSVGGlyph extends SVGGlyph {

    /**
     * 构造权限 SVG 图标控件
     */
    public PermissionSVGGlyph() {
        super("/fx-svg/permissions.svg");
    }

    /**
     * 构造指定尺寸的权限 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public PermissionSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

}
