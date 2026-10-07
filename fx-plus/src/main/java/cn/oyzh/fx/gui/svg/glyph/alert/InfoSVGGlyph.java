package cn.oyzh.fx.gui.svg.glyph.alert;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 信息提示 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class InfoSVGGlyph extends SVGGlyph {

    /**
     * 构造信息提示 SVG 图标控件
     */
    public InfoSVGGlyph() {
        super("/fx-svg/alert/info-fill.svg");
    }

    /**
     * 构造指定尺寸的信息提示 SVG 图标控件
     *
     * @param size 尺寸
     */
    public InfoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
