package cn.oyzh.fx.gui.svg.glyph;


import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class JSONSVGGlyph extends SVGGlyph {

    public JSONSVGGlyph() {
        super("/fx-svg/json.svg");
    }

    public JSONSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
