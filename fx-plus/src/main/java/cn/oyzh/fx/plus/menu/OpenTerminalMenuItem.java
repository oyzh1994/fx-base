package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.TerminalSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class OpenTerminalMenuItem extends FXMenuItem {

    public OpenTerminalMenuItem(String iconSize, Runnable action) {
        super(new TerminalSVGGlyph(iconSize), I18nResourceBundle.i18nString("base.openTerminal"), null, action);
    }
}
