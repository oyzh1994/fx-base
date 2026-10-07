package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 向左传输 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-21
 */
public class TransportLeftSVGGlyph extends SVGGlyph {

    /**
     * 构造向左传输 SVG 图标控件
     */
    public TransportLeftSVGGlyph() {
        super("/fx-svg/transport-left.svg");
    }

    /**
     * 构造指定尺寸的向左传输 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public TransportLeftSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
