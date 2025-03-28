package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/12
 */
public class UnPauseSVGGlyph extends SVGGlyph {

    public UnPauseSVGGlyph() {
        this.setUrl("/fx-svg/un-pause.svg");
    }

    public UnPauseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

}
