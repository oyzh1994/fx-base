package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 达梦数据库 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-11-04
 */
public class DamengSVGGlyph extends SVGGlyph {

    /**
     * 构造达梦数据库 SVG 图标控件
     */
    public DamengSVGGlyph() {
        super("/fx-svg/database/dameng.svg");
    }

    /**
     * 构造指定尺寸的达梦数据库 SVG 图标控件
     *
     * @param size 尺寸
     */
    public DamengSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
