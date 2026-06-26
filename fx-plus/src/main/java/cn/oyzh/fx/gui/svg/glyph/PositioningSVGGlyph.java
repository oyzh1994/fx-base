package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class PositioningSVGGlyph  extends ScalingSVGGlyph {

    public PositioningSVGGlyph() {
        super("/fx-svg/positioning.svg");
    }

    public PositioningSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 1.05;
    }
}
