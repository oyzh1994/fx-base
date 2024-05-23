package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.controls.svg.DesktopSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

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
