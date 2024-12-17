package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class TitleBarTopSVGGlyph extends SVGGlyph {

    {
        this.setEnableWaiting(false);
        this.setActiveColor(Color.GREEN);
    }

    public TitleBarTopSVGGlyph() {
        this.setUrl("/fx-svg/titlebar/top.svg");
    }

    public TitleBarTopSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
