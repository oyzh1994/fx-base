package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class AddHostSVGGlyph extends SVGGlyph {

    public AddHostSVGGlyph() {
        this.setUrl("/fx-svg/addHost.svg");
    }

    public AddHostSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
