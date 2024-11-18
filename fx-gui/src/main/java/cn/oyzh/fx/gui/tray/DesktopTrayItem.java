package cn.oyzh.fx.gui.tray;

import cn.oyzh.fx.gui.svg.glyph.DesktopSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import cn.oyzh.fx.plus.tray.TrayItem;

/**
 * 托盘菜单项
 *
 * @author oyzh
 * @since 2023/3/2
 */
public class DesktopTrayItem extends TrayItem {

    public DesktopTrayItem(String iconSize, Runnable action) {
        super(I18nHelper.open(), new DesktopSVGGlyph(iconSize), action);
    }
}
