package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 应用 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ApplySVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public ApplySVGGlyph() {
        super("/fx-svg/apply.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ApplySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
