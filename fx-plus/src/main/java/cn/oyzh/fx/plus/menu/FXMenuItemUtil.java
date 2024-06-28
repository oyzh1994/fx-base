package cn.oyzh.fx.plus.menu;

import cn.oyzh.fx.plus.controls.svg.InfoSVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;
import lombok.experimental.UtilityClass;

/**
 * 菜单工具类
 *
 * @author oyzh
 * @since 2024/6/28
 */
@UtilityClass
public class FXMenuItemUtil {

    public static FXMenuItem viewInfo(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.viewInfo(), new InfoSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem addView(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addView(), new InfoSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem reload(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.reload(), new InfoSVGGlyph(iconSize), null, action);
    }

    public static FXMenuItem addTable(String iconSize, Runnable action) {
        return FXMenuItem.newItem(I18nHelper.addTable(), new InfoSVGGlyph(iconSize), null, action);
    }
}

