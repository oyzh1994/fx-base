package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class TableSVGGlyph extends SVGGlyph {

    public TableSVGGlyph() {
        super("/fx-svg/database/table.svg");
    }

    public TableSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
