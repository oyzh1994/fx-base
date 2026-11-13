package cn.oyzh.fx.gui.svg.glyph.protol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Rsh 协议 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class RshSVGGlyph extends SVGGlyph {

    /**
     * 构造 Rsh 协议 SVG 图标控件
     */
    public RshSVGGlyph() {
        super("/fx-svg/protol/rsh.svg");
    }

    /**
     * 构造指定尺寸的 Rsh 协议 SVG 图标控件
     *
     * @param size 尺寸
     */
    public RshSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
