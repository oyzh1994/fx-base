package cn.oyzh.fx.gui.tray;

import cn.oyzh.fx.gui.svg.glyph.SettingSVGGlyph;
import cn.oyzh.fx.plus.tray.TrayItem;
import cn.oyzh.i18n.I18nHelper;

/**
 * 设置托盘菜单项
 *
 * @author oyzh
 * @since 2024-04-12
 */
public class SettingTrayItem extends TrayItem {

    /**
     * 构造设置托盘菜单项
     *
     * @param action 点击操作
     */
    public SettingTrayItem(Runnable action) {
        super(I18nHelper.setting(), new SettingSVGGlyph(), action);
    }
}
