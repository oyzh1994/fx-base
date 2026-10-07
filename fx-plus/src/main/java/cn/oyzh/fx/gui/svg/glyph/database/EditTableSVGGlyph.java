package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 编辑表 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-11-04
 */
public class EditTableSVGGlyph extends SVGGlyph {

    /**
     * 构造编辑表 SVG 图标控件
     */
    public EditTableSVGGlyph() {
        super("/fx-svg/database/edit_table.svg");
    }

    /**
     * 构造指定尺寸的编辑表 SVG 图标控件
     *
     * @param size 尺寸
     */
    public EditTableSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
