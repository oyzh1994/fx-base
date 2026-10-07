package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 解压 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class UnCompressSVGGlyph extends SVGGlyph {

    /**
     * 构造解压 SVG 图标控件
     */
    public UnCompressSVGGlyph() {
        super("/fx-svg/uncompress.svg");
    }

    /**
     * 构造指定尺寸的解压 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public UnCompressSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
