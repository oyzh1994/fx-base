package cn.oyzh.fx.plus.controls.svg;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class LoadAllSVGGlyph extends SVGGlyph {

    public LoadAllSVGGlyph() {
        this.setUrl("/fx-plus/font/reload time.svg");
    }

    public LoadAllSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
