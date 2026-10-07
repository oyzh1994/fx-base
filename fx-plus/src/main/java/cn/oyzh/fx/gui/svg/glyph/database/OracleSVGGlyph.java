package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Oracle 数据库 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-11-04
 */
public class OracleSVGGlyph extends SVGGlyph {

    /**
     * 构造Oracle 数据库 SVG 图标控件
     */
    public OracleSVGGlyph() {
        super("/fx-svg/database/oracle.svg");
    }

    /**
     * 构造指定尺寸的Oracle 数据库 SVG 图标控件
     *
     * @param size 尺寸
     */
    public OracleSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
