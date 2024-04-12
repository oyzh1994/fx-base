package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.controls.svg.DesktopSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * 托盘菜单项
 *
 * @author oyzh
 * @since 2023/3/2
 */
public class DesktopTrayItem extends TrayItem {

    public DesktopTrayItem(String iconSize, Runnable action) {
        super(BaseResourceBundle.getBaseString("base.open"), new DesktopSVGGlyph(iconSize), action);
    }
}
