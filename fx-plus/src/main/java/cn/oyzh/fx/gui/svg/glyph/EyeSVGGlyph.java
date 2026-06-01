package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class EyeSVGGlyph extends ScalingSVGGlyph {

    public EyeSVGGlyph() {
        super("/fx-svg/eye.svg");
    }

    public EyeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double widthScaling() {
        return 1.2;
    }
}
