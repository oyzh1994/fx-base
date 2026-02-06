package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class EditTableSVGGlyph extends SVGGlyph {

    public EditTableSVGGlyph() {
        this.setUrl("/fx-svg/database/edit_table.svg");
    }

    public EditTableSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
