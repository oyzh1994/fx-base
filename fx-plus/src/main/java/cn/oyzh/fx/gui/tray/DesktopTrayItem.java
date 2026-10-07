package cn.oyzh.fx.gui.tray;

import cn.oyzh.fx.gui.svg.glyph.DesktopSVGGlyph;
import cn.oyzh.fx.plus.tray.TrayItem;
import cn.oyzh.i18n.I18nHelper;

/**
 * 显示桌面托盘菜单项
 *
 * @author oyzh
 * @since 2024-04-12
 */
public class DesktopTrayItem extends TrayItem {

    /**
     * 构造显示桌面托盘菜单项
     *
     * @param action 点击操作
     */
    public DesktopTrayItem(Runnable action) {
        super(I18nHelper.open(), new DesktopSVGGlyph(), action);
    }
}
