package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class CloseSVGGlyph extends ScalingSVGGlyph {

    public CloseSVGGlyph() {
        super("/fx-svg/close.svg");
    }

    public CloseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 0.6875;
    }
}
