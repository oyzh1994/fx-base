package cn.oyzh.fx.plus.titlebar;

import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class TitleBarTopSVGGlyph extends ActionSVGGlyph {

    {
        this.setEnableWaiting(false);
        this.setActiveColor(Color.GREEN);
    }

    public TitleBarTopSVGGlyph() {
        this.setUrl("/fx-svg/titlebar/top3.svg");
    }

    public TitleBarTopSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
