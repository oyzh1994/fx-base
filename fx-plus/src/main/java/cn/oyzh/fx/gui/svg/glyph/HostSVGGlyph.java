package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class HostSVGGlyph extends SVGGlyph {

    public HostSVGGlyph() {
        this.setUrl("/fx-svg/host.svg");
    }

    public HostSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
