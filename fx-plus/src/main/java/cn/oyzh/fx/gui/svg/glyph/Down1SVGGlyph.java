package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 向下1 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class Down1SVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public Down1SVGGlyph() {
        super("/fx-svg/down1.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public Down1SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
