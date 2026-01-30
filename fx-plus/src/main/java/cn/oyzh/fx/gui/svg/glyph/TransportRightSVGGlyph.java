package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class TransportRightSVGGlyph extends SVGGlyph {

    public TransportRightSVGGlyph() {
        this.setUrl("/fx-svg/transport-right.svg");
    }

    public TransportRightSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
