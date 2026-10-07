package cn.oyzh.fx.gui.svg.glyph.layout;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 布局二 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class Layout2SVGGlyph extends SVGGlyph {

    /**
     * 构造布局二 SVG 图标控件
     */
    public Layout2SVGGlyph() {
        super("/fx-svg/layout/layout2.svg");
    }

    /**
     * 构造指定尺寸的布局二 SVG 图标控件
     *
     * @param size 尺寸
     */
    public Layout2SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
