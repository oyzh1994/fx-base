package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class SqlServerSVGGlyph extends SVGGlyph {

    public SqlServerSVGGlyph() {
        super("/fx-svg/database/sqlserver.svg");
    }

    public SqlServerSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
