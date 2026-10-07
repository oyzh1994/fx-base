package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 时间 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class TimeSVGGlyph extends SVGGlyph {

    /**
     * 构造时间 SVG 图标控件
     */
    public TimeSVGGlyph() {
        super("/fx-svg/time.svg");
    }

    /**
     * 构造指定尺寸的时间 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public TimeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
