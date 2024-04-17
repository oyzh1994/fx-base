package cn.oyzh.fx.plus.controls.svg;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ServerSVGGlyph extends SVGGlyph {

    public ServerSVGGlyph() {
        this.setUrl("/fx-plus/font/sever.svg");
    }

    public ServerSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
