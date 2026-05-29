package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class Apply1SVGGlyph extends SVGGlyph {

    public Apply1SVGGlyph() {
        super("/fx-svg/apply1.svg");
    }

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
