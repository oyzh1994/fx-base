package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class SchemaSVGGlyph extends SVGGlyph {

    public SchemaSVGGlyph() {
        super("/fx-svg/database/schema.svg");
    }

    public SchemaSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
