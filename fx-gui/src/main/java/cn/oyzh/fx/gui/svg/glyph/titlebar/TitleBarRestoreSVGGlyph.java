package cn.oyzh.fx.gui.svg.glyph.titlebar;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class TitleBarRestoreSVGGlyph extends SVGGlyph {

    public TitleBarRestoreSVGGlyph() {
        this.setUrl("/fx-svg/titlebar/restore.svg");
    }

    public TitleBarRestoreSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
