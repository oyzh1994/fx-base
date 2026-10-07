package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MariaDB 数据库 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-09-05
 */
public class MariadbSVGGlyph extends SVGGlyph {

    /**
     * 构造MariaDB 数据库 SVG 图标控件
     */
    public MariadbSVGGlyph() {
        super("/fx-svg/database/mariadb.svg");
    }

    /**
     * 构造指定尺寸的MariaDB 数据库 SVG 图标控件
     *
     * @param size 尺寸
     */
    public MariadbSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
