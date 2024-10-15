package cn.oyzh.fx.plus.node;

import cn.oyzh.fx.plus.adapter.DestroyAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.i18n.I18nAdapter;
import cn.oyzh.fx.plus.i18n.I18nManager;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;
import cn.oyzh.fx.plus.opacity.OpacityAdapter;
import cn.oyzh.fx.plus.opacity.OpacityManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import lombok.experimental.UtilityClass;

/**
 * 节点管理器
 *
 * @author oyzh
 * @since 2024/04/05
 */
@UtilityClass
public class NodeManager {

    /**
     * 初始化节点
     *
     * @param node 节点
     */
    public static void init(Object node) {
        if (node instanceof DestroyAdapter adapter) {
            adapter.initDestroyListener();
        }
        if (node instanceof NodeAdapter adapter) {
            adapter.initNode();
        }
        if (node instanceof OpacityAdapter adapter) {
            adapter.changeOpacity(OpacityManager.currentOpacity());
        }
        if (node instanceof FontAdapter adapter) {
            adapter.changeFont(FontManager.currentFont());
        }
        if (node instanceof ThemeAdapter adapter) {
            adapter.changeTheme(ThemeManager.currentTheme());
        }
        if (node instanceof I18nAdapter adapter) {
            adapter.changeLocale(I18nManager.currentLocale());
        }
        if (node instanceof I18nSelectAdapter<?> adapter) {
            adapter.values(I18nManager.currentLocale());
        }
    }
}
