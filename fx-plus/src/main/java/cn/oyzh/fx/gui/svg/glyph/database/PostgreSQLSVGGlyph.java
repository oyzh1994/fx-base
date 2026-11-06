package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * postgresql 数据库 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-11-04
 */
public class PostgreSQLSVGGlyph extends SVGGlyph {

    /**
     * 构造postgresql 数据库 SVG 图标控件
     */
    public PostgreSQLSVGGlyph() {
        super("/fx-svg/database/postgresql.svg");
    }

    /**
     * 构造指定尺寸的postgresql 数据库 SVG 图标控件
     *
     * @param size 尺寸
     */
    public PostgreSQLSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
