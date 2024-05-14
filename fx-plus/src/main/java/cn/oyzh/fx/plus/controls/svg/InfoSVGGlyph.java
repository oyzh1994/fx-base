package cn.oyzh.fx.plus.controls.svg;

/**
 * @author oyzh
 * @since 2024/5/14
 */
public class InfoSVGGlyph extends SVGGlyph{

    public InfoSVGGlyph() {
        this.setUrl("/fx-plus/font/info-circle.svg");
    }

    public InfoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
