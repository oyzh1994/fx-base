package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class MonitorSVGGlyph extends SVGGlyph {

    public MonitorSVGGlyph() {
        this.setUrl("/fx-svg/monitor.svg");
    }

    public MonitorSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
