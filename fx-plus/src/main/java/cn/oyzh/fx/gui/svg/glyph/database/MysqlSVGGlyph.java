package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.font.FontManager;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class MysqlSVGGlyph extends SVGGlyph {

    public MysqlSVGGlyph() {
        super("/fx-svg/database/mysql.svg");
    }

    public MysqlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
