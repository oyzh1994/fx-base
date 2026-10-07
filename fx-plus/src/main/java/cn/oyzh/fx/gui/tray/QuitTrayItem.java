package cn.oyzh.fx.gui.tray;

import cn.oyzh.fx.gui.svg.glyph.QuitSVGGlyph;
import cn.oyzh.fx.plus.tray.TrayItem;
import cn.oyzh.i18n.I18nHelper;

/**
 * 退出托盘菜单项
 *
 * @author oyzh
 * @since 2023-03-02
 */
public class QuitTrayItem extends TrayItem {

    /**
     * 构造退出托盘菜单项
     *
     * @param action 点击操作
     */
    public QuitTrayItem(Runnable action) {
        super(I18nHelper.quit(), new QuitSVGGlyph(), action);
    }
}
