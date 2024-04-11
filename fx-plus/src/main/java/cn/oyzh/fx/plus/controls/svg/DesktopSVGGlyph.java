package cn.oyzh.fx.plus.controls.svg;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class DesktopSVGGlyph extends SVGGlyph {

    public DesktopSVGGlyph() {
        this.setUrl("/fx-plus/font/desktop.svg");
    }

    public DesktopSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
