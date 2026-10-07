package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 区分大小写 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class MatchCaseSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public MatchCaseSVGGlyph() {
        super("/fx-svg/match_case.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public MatchCaseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.83;
    }
}
