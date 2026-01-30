package cn.oyzh.fx.gui.svg.glyph.alert;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class QuestionSVGGlyph extends SVGGlyph {

    public QuestionSVGGlyph() {
        this.setUrl("/fx-svg/alert/question-fill.svg");
    }

    public QuestionSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
