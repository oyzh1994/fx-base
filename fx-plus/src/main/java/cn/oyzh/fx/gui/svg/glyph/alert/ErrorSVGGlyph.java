package cn.oyzh.fx.gui.svg.glyph.alert;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 错误提示 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-01-30
 */
public class ErrorSVGGlyph extends SVGGlyph {

    /**
     * 构造错误提示 SVG 图标控件
     */
    public ErrorSVGGlyph() {
        super("/fx-svg/alert/error-fill.svg");
    }

    /**
     * 构造指定尺寸的错误提示 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ErrorSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
