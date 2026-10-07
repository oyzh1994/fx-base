package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * 定位 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class PositioningSVGGlyph  extends ScalingSVGGlyph {

    /**
     * 构造定位 SVG 图标控件
     */
    public PositioningSVGGlyph() {
        super("/fx-svg/positioning.svg");
    }

    /**
     * 构造指定尺寸的定位 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public PositioningSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 1.05;
    }
}
