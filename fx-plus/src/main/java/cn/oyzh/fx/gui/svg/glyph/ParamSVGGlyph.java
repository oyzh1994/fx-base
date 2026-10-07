package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 参数 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-02-08
 */
public class ParamSVGGlyph extends SVGGlyph {

    /**
     * 构造参数 SVG 图标控件
     */
    public ParamSVGGlyph() {
        super("/fx-svg/param.svg");
    }

    /**
     * 构造指定尺寸的参数 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public ParamSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
