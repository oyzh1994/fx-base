package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class PlaySVGGlyph extends SVGGlyph {

    public PlaySVGGlyph() {
        this.setUrl("/fx-gui/font/play-circle.svg");
    }

    public PlaySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
