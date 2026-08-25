package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class EyeOpenSVGGlyph extends ScalingSVGGlyph {

    public EyeOpenSVGGlyph() {
        super("/fx-svg/eye-open.svg");
    }

    public EyeOpenSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.7;
    }
}
