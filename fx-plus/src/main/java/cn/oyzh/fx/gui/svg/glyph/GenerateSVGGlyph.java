package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 生成 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-10-16
 */
public class GenerateSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public GenerateSVGGlyph() {
        super("/fx-svg/generate.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public GenerateSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
