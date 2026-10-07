package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 关键字 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-02-08
 */
public class KeywordsSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public KeywordsSVGGlyph() {
        super("/fx-svg/keywords.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public KeywordsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
