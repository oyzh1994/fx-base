package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 端口 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class PortSVGGlyph extends SVGGlyph {

    /**
     * 构造端口 SVG 图标控件
     */
    public PortSVGGlyph() {
        super("/fx-svg/port.svg");
    }

    /**
     * 构造指定尺寸的端口 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public PortSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
