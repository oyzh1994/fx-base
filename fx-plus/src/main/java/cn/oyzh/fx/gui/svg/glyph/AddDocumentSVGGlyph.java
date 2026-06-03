package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class AddDocumentSVGGlyph extends ScalingSVGGlyph {

    public AddDocumentSVGGlyph() {
        super("/fx-svg/add-document.svg");
    }

    public AddDocumentSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double widthScaling() {
        return 0.9;
    }
}

