package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 关闭 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class CloseSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public CloseSVGGlyph() {
        super("/fx-svg/close.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public CloseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 0.75;
    }
}
