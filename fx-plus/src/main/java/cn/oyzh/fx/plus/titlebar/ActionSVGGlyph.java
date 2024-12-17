package cn.oyzh.fx.plus.titlebar;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.theme.ThemeManager;

/**
 * @author oyzh
 * @since 2024-12-17
 */
public class ActionSVGGlyph extends SVGGlyph {

    {
        this.initEvents();
    }

    protected void initEvents() {
        if (this.isEnable()) {
            this.setOnMouseEntered(event -> this.setColor(ThemeManager.currentAccentColor()));
            this.setOnMouseExited(event -> this.setColor(ThemeManager.currentForegroundColor()));
        }
    }
}
