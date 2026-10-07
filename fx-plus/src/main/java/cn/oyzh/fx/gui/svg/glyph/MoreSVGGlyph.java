package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 更多 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class MoreSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public MoreSVGGlyph() {
        super("/fx-svg/more.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public MoreSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
