package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 数据库视图 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-07-03
 */
public class ViewSVGGlyph extends SVGGlyph {

    /**
     * 构造数据库视图 SVG 图标控件
     */
    public ViewSVGGlyph() {
        super("/fx-svg/database/view.svg");
    }

    /**
     * 构造指定尺寸的数据库视图 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ViewSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
