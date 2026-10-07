package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 提交 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class SubmitSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造提交 SVG 图标控件
     */
    public SubmitSVGGlyph() {
        super("/fx-svg/check.svg");
    }

    /**
     * 构造指定尺寸的提交 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public SubmitSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 0.9;
    }

    @Override
    public double widthScaling() {
        return 1.1;
    }

    @Override
    public double heightScaling() {
        return 0.9;
    }
}
