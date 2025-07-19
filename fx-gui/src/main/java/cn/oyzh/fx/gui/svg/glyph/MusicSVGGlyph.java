package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class MusicSVGGlyph extends SVGGlyph {

    public MusicSVGGlyph() {
        this.setUrl("/fx-svg/music.svg");
    }

    public MusicSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
