package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 终止 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class KillSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public KillSVGGlyph() {
        super("/fx-svg/kill.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public KillSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
