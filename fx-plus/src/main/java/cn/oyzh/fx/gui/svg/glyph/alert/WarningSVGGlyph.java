package cn.oyzh.fx.gui.svg.glyph.alert;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 警告提示 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class WarningSVGGlyph extends SVGGlyph {

    /**
     * 构造警告提示 SVG 图标控件
     */
    public WarningSVGGlyph() {
        super("/fx-svg/alert/warning-fill.svg");
    }

    /**
     * 构造指定尺寸的警告提示 SVG 图标控件
     *
     * @param size 尺寸
     */
    public WarningSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
