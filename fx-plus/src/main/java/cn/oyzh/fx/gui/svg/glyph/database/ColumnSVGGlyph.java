package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class ColumnSVGGlyph extends SVGGlyph {

    public ColumnSVGGlyph() {
        super("/fx-svg/database/column.svg");
    }

    public ColumnSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
