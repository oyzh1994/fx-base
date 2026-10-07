package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 资源 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-08-12
 */
public class ResourceSVGGlyph extends SVGGlyph {

    /**
     * 构造资源 SVG 图标控件
     */
    public ResourceSVGGlyph() {
        super("/fx-svg/resource.svg");
    }

    /**
     * 构造指定尺寸的资源 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public ResourceSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

}
