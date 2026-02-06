package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class EventSVGGlyph extends SVGGlyph {

    public EventSVGGlyph() {
        this.setUrl("/fx-svg/database/event.svg");
    }

    public EventSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
