package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 隧道 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class TunnelingSVGGlyph extends SVGGlyph {

    /**
     * 构造隧道 SVG 图标控件
     */
    public TunnelingSVGGlyph() {
        super("/fx-svg/tunneling.svg");
    }

    /**
     * 构造指定尺寸的隧道 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public TunnelingSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
