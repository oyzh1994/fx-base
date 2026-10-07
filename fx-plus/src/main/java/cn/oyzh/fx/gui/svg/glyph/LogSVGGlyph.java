package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 日志 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class LogSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public LogSVGGlyph() {
        super("/fx-svg/log.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public LogSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
