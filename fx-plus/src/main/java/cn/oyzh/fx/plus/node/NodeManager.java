package cn.oyzh.fx.plus.node;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.fx.plus.adapter.DestroyAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.i18n.I18nAdapter;
import cn.oyzh.fx.plus.i18n.I18nSelectAdapter;
import cn.oyzh.fx.plus.opacity.OpacityAdapter;
import cn.oyzh.fx.plus.opacity.OpacityManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.i18n.I18nManager;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Region;

/**
 * 节点管理器
 *
 * @author oyzh
 * @since 2024/04/05
 */

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
        if (node instanceof I18nAdapter adapter) {
            adapter.changeLocale(I18nManager.currentLocale());
        }
        if (node instanceof I18nSelectAdapter<?> adapter) {
            adapter.values(I18nManager.currentLocale());
        }
        if (node instanceof NodeLifeCycle lifeCycle) {
            if (node instanceof Node node1) {
                node1.parentProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        lifeCycle.onNodeDestroy();
                    } else {
                        lifeCycle.onNodeInitialize();
                    }
                });
                node1.sceneProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        lifeCycle.onNodeDestroy();
                    } else {
                        lifeCycle.onNodeInitialize();
                    }
                });
            } else if (node instanceof Tab node1) {
                node1.tabPaneProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        lifeCycle.onNodeDestroy();
                    } else {
                        lifeCycle.onNodeInitialize();
                    }
                });
            } else if (node instanceof TreeItem<?> node1) {
                node1.parentProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        lifeCycle.onNodeDestroy();
                    } else {
                        lifeCycle.onNodeInitialize();
                    }
                });
            }
        }
        // TODO: 默认开启亚像素渲染
        if (node instanceof Region region) {
            region.setSnapToPixel(true);
        }
        // TODO: 延迟执行主题处理，否则可能出现部分组件样式异常
        if (node instanceof ThemeAdapter adapter) {
            // adapter.changeTheme(ThemeManager.currentTheme());
            TaskManager.startDelay(() -> adapter.changeTheme(ThemeManager.currentTheme()), 50);
        }
    }

}
