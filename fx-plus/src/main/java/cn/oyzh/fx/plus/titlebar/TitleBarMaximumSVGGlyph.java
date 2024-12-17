package cn.oyzh.fx.plus.titlebar;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class TitleBarMaximumSVGGlyph extends ActionSVGGlyph {

    public TitleBarMaximumSVGGlyph() {
        this.setUrl("/fx-svg/titlebar/maximum.svg");
    }

    public TitleBarMaximumSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
