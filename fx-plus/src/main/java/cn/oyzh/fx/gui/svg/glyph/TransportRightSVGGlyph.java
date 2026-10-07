package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 向右传输 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-21
 */
public class TransportRightSVGGlyph extends SVGGlyph {

    /**
     * 构造向右传输 SVG 图标控件
     */
    public TransportRightSVGGlyph() {
        super("/fx-svg/transport-right.svg");
    }

    /**
     * 构造指定尺寸的向右传输 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public TransportRightSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
