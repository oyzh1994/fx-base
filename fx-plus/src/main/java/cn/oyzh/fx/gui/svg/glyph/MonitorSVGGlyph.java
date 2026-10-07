package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 监视器 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class MonitorSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public MonitorSVGGlyph() {
        super("/fx-svg/monitor.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public MonitorSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
