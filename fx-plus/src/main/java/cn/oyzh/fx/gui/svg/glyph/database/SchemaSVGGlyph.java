package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 数据库模式 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-11-04
 */
public class SchemaSVGGlyph extends SVGGlyph {

    /**
     * 构造数据库模式 SVG 图标控件
     */
    public SchemaSVGGlyph() {
        super("/fx-svg/database/schema.svg");
    }

    /**
     * 构造指定尺寸的数据库模式 SVG 图标控件
     *
     * @param size 尺寸
     */
    public SchemaSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
