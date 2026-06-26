package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class DatabaseSVGGlyph extends SVGGlyph {

    public DatabaseSVGGlyph() {
        super("/fx-svg/database/database.svg");
    }

    public DatabaseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
