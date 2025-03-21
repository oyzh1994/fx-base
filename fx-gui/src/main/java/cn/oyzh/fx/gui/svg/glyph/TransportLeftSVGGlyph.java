package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class TransportLeftSVGGlyph extends SVGGlyph {

    public TransportLeftSVGGlyph() {
        this.setUrl("/fx-svg/transport-left.svg");
    }

    public TransportLeftSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
