package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class TunnelingSVGGlyph extends SVGGlyph {

    public TunnelingSVGGlyph() {
        this.setUrl("/fx-svg/tunneling.svg");
    }

    public TunnelingSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
