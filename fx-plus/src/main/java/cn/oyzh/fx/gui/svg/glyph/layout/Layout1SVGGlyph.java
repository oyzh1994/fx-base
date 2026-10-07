package cn.oyzh.fx.gui.svg.glyph.layout;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 布局一 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class Layout1SVGGlyph extends SVGGlyph {

    /**
     * 构造布局一 SVG 图标控件
     */
    public Layout1SVGGlyph() {
        super("/fx-svg/layout/layout1.svg");
    }

    /**
     * 构造指定尺寸的布局一 SVG 图标控件
     *
     * @param size 尺寸
     */
    public Layout1SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
