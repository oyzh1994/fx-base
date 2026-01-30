package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/12
 */
public class LogSVGGlyph extends SVGGlyph {

    public LogSVGGlyph() {
        this.setUrl("/fx-svg/log.svg");
    }

    public LogSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
