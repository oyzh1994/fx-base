package cn.oyzh.fx.gui.svg.glyph.alert;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 询问提示 SVG 图标控件
 *
 * @author oyzh
 * @since 2026-01-30
 */
public class QuestionSVGGlyph extends SVGGlyph {

    /**
     * 构造询问提示 SVG 图标控件
     */
    public QuestionSVGGlyph() {
        super("/fx-svg/alert/question-fill.svg");
    }

    /**
     * 构造指定尺寸的询问提示 SVG 图标控件
     *
     * @param size 尺寸
     */
    public QuestionSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
