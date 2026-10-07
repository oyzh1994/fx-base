package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 示例 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-07-05
 */
public class ExampleSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public ExampleSVGGlyph() {
        super("/fx-svg/example.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ExampleSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
