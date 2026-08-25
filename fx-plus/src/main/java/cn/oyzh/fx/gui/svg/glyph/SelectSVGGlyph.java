package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class SelectSVGGlyph extends ScalingSVGGlyph {

    public SelectSVGGlyph() {
        super("/fx-svg/select.svg");
    }

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
