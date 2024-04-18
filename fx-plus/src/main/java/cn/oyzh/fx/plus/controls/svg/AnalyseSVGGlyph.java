package cn.oyzh.fx.plus.controls.svg;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class AnalyseSVGGlyph extends SVGGlyph {

    public AnalyseSVGGlyph() {
        this.setUrl("/fx-plus/font/NLP_text.svg");
    }

    public AnalyseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
