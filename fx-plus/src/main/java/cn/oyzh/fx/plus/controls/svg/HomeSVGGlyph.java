package cn.oyzh.fx.plus.controls.svg;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class HomeSVGGlyph extends SVGGlyph {

    public HomeSVGGlyph() {
        this.setUrl("/fx-plus/font/home.svg");
    }

    public HomeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
