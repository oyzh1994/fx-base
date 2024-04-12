package cn.oyzh.fx.plus.controls.svg;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class PlaySVGGlyph extends SVGGlyph {

    public PlaySVGGlyph() {
        this.setUrl("/fx-plus/font/play-circle.svg");
    }

    public PlaySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
