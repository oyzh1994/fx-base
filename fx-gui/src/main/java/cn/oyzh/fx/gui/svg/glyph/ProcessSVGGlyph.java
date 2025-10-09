package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ProcessSVGGlyph extends SVGGlyph {

    public ProcessSVGGlyph() {
        this.setUrl("/fx-svg/process.svg");
    }

    public ProcessSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
