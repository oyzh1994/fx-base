package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 显示 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class ShowSVGGlyph extends SVGGlyph {

    /**
     * 构造显示 SVG 图标控件
     */
    public ShowSVGGlyph() {
        super("/fx-svg/show.svg");
    }

    /**
     * 构造指定尺寸的显示 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public ShowSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
