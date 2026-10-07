package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 向上 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class Up1SVGGlyph extends SVGGlyph {

    /**
     * 构造向上 SVG 图标控件
     */
    public Up1SVGGlyph() {
        super("/fx-svg/up1.svg");
    }

    /**
     * 构造指定尺寸的向上 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public Up1SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
