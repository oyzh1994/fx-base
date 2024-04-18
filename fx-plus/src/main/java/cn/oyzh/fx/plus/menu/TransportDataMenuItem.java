package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.TransportSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class TransportDataMenuItem extends FXMenuItem {

    public TransportDataMenuItem(String iconSize, Runnable action) {
        this(I18nResourceBundle.i18nString("base.transportData"), iconSize, action);
    }

    public TransportDataMenuItem(String title, String iconSize, Runnable action) {
        super(new TransportSVGGlyph(iconSize), title, null, action);
    }

}
