package cn.oyzh.fx.plus.controls.svg;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class LockSVGGlyph extends SVGGlyph {

    public LockSVGGlyph() {
        this.setUrl("/fx-plus/font/lock.svg");
    }

    public LockSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
