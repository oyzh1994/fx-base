package cn.oyzh.fx.plus.tray;

import cn.oyzh.fx.plus.controls.svg.QuitSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * 托盘菜单项
 *
 * @author oyzh
 * @since 2023/3/2
 */
public class QuitTrayItem extends TrayItem {

    public QuitTrayItem(String iconSize, Runnable action) {
        super(I18nHelper.quit(), new QuitSVGGlyph(iconSize), action);
    }
}
