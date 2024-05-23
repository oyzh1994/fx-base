package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.ServerSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class ServerInfoMenuItem extends FXMenuItem {

    public ServerInfoMenuItem(String iconSize, Runnable action) {
        super(new ServerSVGGlyph(iconSize), I18nHelper.serverInfo(), null, action);
    }
}
