package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 全字匹配 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class WholeWordSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造全字匹配 SVG 图标控件
     */
    public WholeWordSVGGlyph() {
        super("/fx-svg/whole-word.svg");
    }

    /**
     * 构造指定尺寸的全字匹配 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public WholeWordSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.83;
    }
}
