package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.TransportSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class TransportDataMenuItem extends FXMenuItem {

    public TransportDataMenuItem(String iconSize, Runnable action) {
        super(new TransportSVGGlyph(iconSize), BaseResourceBundle.getBaseString("base.transportData"), null, action);
    }
}
