package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ForceKillSVGGlyph extends SVGGlyph {

    public ForceKillSVGGlyph() {
        this.setUrl("/fx-svg/force-kill.svg");
    }

    public ForceKillSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
