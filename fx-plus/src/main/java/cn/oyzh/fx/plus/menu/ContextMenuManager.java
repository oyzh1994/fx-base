package cn.oyzh.fx.plus.menu;

import cn.oyzh.common.object.ObjectWatcherManager;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-06-25
 */
public class ContextMenuManager {

    /**
     * 当前操作面板，全局一个
     */
    private static FXContextMenu contextMenu;

    /**
     * 创建上下文菜单，全局唯一
     *
     * @param items 列表项
     * @return 菜单
     */
    public static FXContextMenu createContextMenu(Object object, List<? extends MenuItem> items) {
        if (contextMenu == null) {
            contextMenu = new FXContextMenu(object);
        } else {
            contextMenu.destroy();
            contextMenu.setTarget(object);
        }
        contextMenu.setItem(items);
        return contextMenu;
    }

    /**
     * 创建新上下文菜单
     *
     * @param items 列表项
     * @return 菜单
     */
    public static ContextMenu createNewContextMenu(List<? extends MenuItem> items) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().setAll(items);
        ObjectWatcherManager.watch(contextMenu);
        return contextMenu;
    }

    /**
     * 获取操作面板
     *
     * @param object 对象
     * @return ContextMenu
     */
    public static ContextMenu getContextMenu(Object object) {
        if (object instanceof Control control) {
            return control.getContextMenu();
        }
        if (object instanceof Tab tab) {
            return tab.getContextMenu();
        }
        return null;
    }

    /**
     * 设置操作面板
     *
     * @param object      对象
     * @param contextMenu 操作面板
     */
    public static void setContextMenu(Object object, ContextMenu contextMenu) {
        if (object instanceof Control control) {
            control.setContextMenu(contextMenu);
        } else if (object instanceof Tab tab) {
            tab.setContextMenu(contextMenu);
        }
    }

    /**
     * 清除操作面板
     *
     * @param object 对象
     */
    public static void clearContextMenu(Object object) {
        ContextMenu menu = null;
        if (object instanceof Control control) {
            menu = control.getContextMenu();
            control.setContextMenu(null);
            control.contextMenuProperty().unbind();
        } else if (object instanceof Tab tab) {
            menu = tab.getContextMenu();
            tab.setContextMenu(null);
            tab.contextMenuProperty().unbind();
        }
        if (menu != null) {
            // 先关闭菜单，断开与 Scene 的关联
            menu.hide();
            // 清理菜单项
            for (MenuItem item : menu.getItems()) {
                if (item instanceof FXMenuItem menuItem) {
                    menuItem.destroy();
                } else {
                    item.setText(null);
                    item.setGraphic(null);
                    item.setOnAction(null);
                    item.setAccelerator(null);
                    item.setId(null);
                    item.setStyle(null);
                }
            }
            menu.getItems().clear();
            // 清理 ContextMenu 自身的事件处理器，断开与 TabSkin 的引用链
            menu.setOnShowing(null);
            menu.setOnShown(null);
            menu.setOnHiding(null);
            menu.setOnHidden(null);
            menu.setOnAction(null);
            menu.setOnCloseRequest(null);
            menu.setId(null);
            menu.setStyle(null);
            menu.setUserData(null);
            // 销毁皮肤，彻底断开与 Scene graph 的关联
            final javafx.scene.control.Skin<?> skin = menu.getSkin();
            if (skin != null) {
                menu.setSkin(null);
                skin.dispose();
            }
        }
    }

    /**
     * 显示操作面板
     *
     * @param contextMenu 上下文
     * @param node        节点
     * @param event       事件
     */
    public static void showContextMenu(ContextMenu contextMenu, Node node, MouseEvent event) {
        contextMenu.show(node, event.getScreenX() - 10, event.getScreenY() - 10);
    }

    /**
     * 显示操作面板
     *
     * @param contextMenu 上下文
     * @param node        节点
     * @param event       事件
     */
    public static void showContextMenu(ContextMenu contextMenu, Node node, ContextMenuEvent event) {
        contextMenu.show(node, event.getScreenX() - 10, event.getScreenY() - 10);
    }

}
