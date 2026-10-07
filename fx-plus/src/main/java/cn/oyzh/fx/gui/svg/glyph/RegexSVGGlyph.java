package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 正则表达式 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class RegexSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造正则表达式 SVG 图标控件
     */
    public RegexSVGGlyph() {
        super("/fx-svg/regex.svg");
    }

    /**
     * 构造指定尺寸的正则表达式 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public RegexSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 0.85;
    }
}
