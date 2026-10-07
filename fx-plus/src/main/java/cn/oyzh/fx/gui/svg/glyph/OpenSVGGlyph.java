package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 打开 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-07-03
 */
public class OpenSVGGlyph extends SVGGlyph {

    /**
     * 构造打开 SVG 图标控件
     */
    public OpenSVGGlyph() {
        super("/fx-svg/open.svg");
    }

    /**
     * 构造指定尺寸的打开 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public OpenSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
