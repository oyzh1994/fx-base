package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 警告 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-02-14
 */
public class WarningSVGGlyph extends SVGGlyph {

    /**
     * 构造警告 SVG 图标控件
     */
    public WarningSVGGlyph() {
        super("/fx-svg/warning.svg");
    }

    /**
     * 构造指定尺寸的警告 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public WarningSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
