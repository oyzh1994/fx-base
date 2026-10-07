package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 向下 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class DownSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public DownSVGGlyph() {
        super("/fx-svg/down.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public DownSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
