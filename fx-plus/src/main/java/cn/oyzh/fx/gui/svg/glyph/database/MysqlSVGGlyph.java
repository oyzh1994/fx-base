package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.font.FontManager;

/**
 * MySQL 数据库 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-09-05
 */
public class MysqlSVGGlyph extends SVGGlyph {

    /**
     * 构造MySQL 数据库 SVG 图标控件
     */
    public MysqlSVGGlyph() {
        super("/fx-svg/database/mysql.svg");
    }

    /**
     * 构造指定尺寸的MySQL 数据库 SVG 图标控件
     *
     * @param size 尺寸
     */
    public MysqlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
