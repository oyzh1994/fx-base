package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 分屏 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class SplitViewSVGGlyph extends SVGGlyph {

    /**
     * 构造分屏 SVG 图标控件
     */
    public SplitViewSVGGlyph() {
        super("/fx-svg/split-view.svg");
    }

    /**
     * 构造指定尺寸的分屏 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public SplitViewSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
