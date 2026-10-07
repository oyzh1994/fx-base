package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 十六进制 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class HexSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public HexSVGGlyph() {
        super("/fx-svg/HEX.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public HexSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
