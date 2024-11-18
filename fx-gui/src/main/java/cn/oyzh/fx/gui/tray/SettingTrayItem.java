package cn.oyzh.fx.gui.tray;

import cn.oyzh.fx.gui.svg.glyph.SettingSVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.fx.plus.tray.TrayItem;

/**
 * 托盘菜单项
 *
 * @author oyzh
 * @since 2023/3/2
 */
public class SettingTrayItem extends TrayItem {

    public SettingTrayItem(String iconSize, Runnable action) {
        super(I18nHelper.setting(), new SettingSVGGlyph(iconSize), action);
    }
}
