package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 隐藏 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/10
 */
public class HiddenSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public HiddenSVGGlyph() {
        super("/fx-svg/hidden.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public HiddenSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
