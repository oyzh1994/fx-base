package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.controls.svg.SettingSVGGlyph;
import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * 托盘菜单项
 *
 * @author oyzh
 * @since 2023/3/2
 */
public class SettingTrayItem extends TrayItem {

    public SettingTrayItem(String iconSize, Runnable action) {
        super(BaseResourceBundle.getBaseString("base.setting"), new SettingSVGGlyph(iconSize), action);
    }
}
