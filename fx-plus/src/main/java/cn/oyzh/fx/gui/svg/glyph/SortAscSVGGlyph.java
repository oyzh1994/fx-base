package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 升序排序 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class SortAscSVGGlyph extends SVGGlyph {

    /**
     * 构造升序排序 SVG 图标控件
     */
    public SortAscSVGGlyph() {
        super("/fx-svg/sort-ascending.svg");
    }

    /**
     * 构造指定尺寸的升序排序 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public SortAscSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
