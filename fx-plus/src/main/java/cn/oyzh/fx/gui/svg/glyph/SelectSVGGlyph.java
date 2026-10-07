package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 选择 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class SelectSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造选择 SVG 图标控件
     */
    public SelectSVGGlyph() {
        super("/fx-svg/select.svg");
    }

    /**
     * 构造指定尺寸的选择 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public SelectSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.6;
    }

    @Override
    public double widthScaling() {
        return 0.9;
    }
}
