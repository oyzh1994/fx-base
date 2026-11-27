package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 数据库列 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-11-04
 */
public class TypeSVGGlyph extends SVGGlyph {

    /**
     * 构造数据库列 SVG 图标控件
     */
    public TypeSVGGlyph() {
        super("/fx-svg/database/type.svg");
    }

    /**
     * 构造指定尺寸的数据库列 SVG 图标控件
     *
     * @param size 尺寸
     */
    public TypeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
