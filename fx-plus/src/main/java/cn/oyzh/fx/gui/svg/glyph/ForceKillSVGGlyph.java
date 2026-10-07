package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 强制终止 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class ForceKillSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public ForceKillSVGGlyph() {
        super("/fx-svg/force-kill.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ForceKillSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
