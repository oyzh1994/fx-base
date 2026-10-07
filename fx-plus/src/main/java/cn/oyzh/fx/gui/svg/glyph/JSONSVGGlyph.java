package cn.oyzh.fx.gui.svg.glyph;


import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * JSON SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-10
 */
public class JSONSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public JSONSVGGlyph() {
        super("/fx-svg/json.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public JSONSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
