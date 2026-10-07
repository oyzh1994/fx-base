package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 数据库表 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-11-04
 */
public class TableSVGGlyph extends SVGGlyph {

    /**
     * 构造数据库表 SVG 图标控件
     */
    public TableSVGGlyph() {
        super("/fx-svg/database/table.svg");
    }

    /**
     * 构造指定尺寸的数据库表 SVG 图标控件
     *
     * @param size 尺寸
     */
    public TableSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
