package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class UnLockSVGGlyph extends SVGGlyph {

    public UnLockSVGGlyph() {
        this.setUrl("/fx-plus/font/unlock.svg");
    }

    public UnLockSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
