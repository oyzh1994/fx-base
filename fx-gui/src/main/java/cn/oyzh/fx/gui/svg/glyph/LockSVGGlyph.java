package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class LockSVGGlyph extends SVGGlyph {

    public LockSVGGlyph() {
        this.setUrl("/fx-plus/font/lock.svg");
    }

    public LockSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
