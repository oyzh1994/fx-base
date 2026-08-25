package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class MariadbSVGGlyph extends SVGGlyph {

    public MariadbSVGGlyph() {
        super("/fx-svg/database/mariadb.svg");
    }

    public MariadbSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
