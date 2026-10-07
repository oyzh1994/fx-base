package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 数据库 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-08-16
 */
public class DatabaseSVGGlyph extends SVGGlyph {

    /**
     * 构造数据库 SVG 图标控件
     */
    public DatabaseSVGGlyph() {
        super("/fx-svg/database/database.svg");
    }

    /**
     * 构造指定尺寸的数据库 SVG 图标控件
     *
     * @param size 尺寸
     */
    public DatabaseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
