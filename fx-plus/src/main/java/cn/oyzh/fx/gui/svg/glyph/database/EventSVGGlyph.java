package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.font.FontManager;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class EventSVGGlyph extends SVGGlyph {

    public EventSVGGlyph() {
        super("/fx-svg/database/event.svg");
    }

    public EventSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
