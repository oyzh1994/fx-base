package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SQLite 数据库 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-11-04
 */
public class SqliteSVGGlyph extends SVGGlyph {

    /**
     * 构造 SQLite 数据库 SVG 图标控件
     */
    public SqliteSVGGlyph() {
        super("/fx-svg/database/sqlite.svg");
    }

    /**
     * 构造指定尺寸的 SQLite 数据库 SVG 图标控件
     *
     * @param size 尺寸
     */
    public SqliteSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
