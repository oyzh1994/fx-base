package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 重启 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-13
 */
public class RestartSVGGlyph extends SVGGlyph {

    /**
     * 构造重启 SVG 图标控件
     */
    public RestartSVGGlyph() {
        super("/fx-svg/restart.svg");
    }

    /**
     * 构造指定尺寸的重启 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public RestartSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

}
