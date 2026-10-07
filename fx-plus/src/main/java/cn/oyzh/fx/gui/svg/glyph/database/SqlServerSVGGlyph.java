package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SQL Server 数据库 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-09-05
 */
public class SqlServerSVGGlyph extends SVGGlyph {

    /**
     * 构造SQL Server 数据库 SVG 图标控件
     */
    public SqlServerSVGGlyph() {
        super("/fx-svg/database/sqlserver.svg");
    }

    /**
     * 构造指定尺寸的SQL Server 数据库 SVG 图标控件
     *
     * @param size 尺寸
     */
    public SqlServerSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
