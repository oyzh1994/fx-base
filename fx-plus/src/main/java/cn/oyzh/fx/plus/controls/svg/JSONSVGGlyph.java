package cn.oyzh.fx.plus.controls.svg;


/**
 * @author oyzh
 * @since 2024/4/10
 */
public class JSONSVGGlyph extends SVGGlyph {

    public JSONSVGGlyph() {
        super("/fx-plus/font/json.svg");
    }

    public JSONSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
