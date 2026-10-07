package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 二进制 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class BinarySVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public BinarySVGGlyph() {
        super("/fx-svg/binary.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public BinarySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
