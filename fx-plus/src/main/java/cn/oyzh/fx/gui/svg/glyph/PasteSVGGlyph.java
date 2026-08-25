package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class PasteSVGGlyph extends ScalingSVGGlyph {

    public PasteSVGGlyph() {
        super("/fx-svg/file-paste.svg");
    }

    public PasteSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double widthScaling() {
        return 0.875;
    }
}
