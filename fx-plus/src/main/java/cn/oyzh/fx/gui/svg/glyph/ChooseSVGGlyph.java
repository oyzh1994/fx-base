package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 选择 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-07-04
 */
public class ChooseSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public ChooseSVGGlyph() {
        super("/fx-svg/choose.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ChooseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
