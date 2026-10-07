package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 关于 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class AboutSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public AboutSVGGlyph() {
        super("/fx-svg/info-circle.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public AboutSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
