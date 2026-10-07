package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 应用1 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class Apply1SVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public Apply1SVGGlyph() {
        super("/fx-svg/apply1.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public Apply1SVGGlyph(String size) {
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
