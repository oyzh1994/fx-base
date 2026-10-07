package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 访问控制 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class AccessControlSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public AccessControlSVGGlyph() {
        super("/fx-svg/access-control.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public AccessControlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
