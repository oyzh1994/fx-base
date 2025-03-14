package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class PortSVGGlyph extends SVGGlyph {

    public PortSVGGlyph() {
        this.setUrl("/fx-svg/port.svg");
    }

    public PortSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
