package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 向上 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class UpSVGGlyph extends SVGGlyph {

    /**
     * 构造向上 SVG 图标控件
     */
    public UpSVGGlyph() {
        super("/fx-svg/up.svg");
    }

    /**
     * 构造指定尺寸的向上 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public UpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
