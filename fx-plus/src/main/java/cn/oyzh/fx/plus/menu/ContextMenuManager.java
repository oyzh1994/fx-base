package cn.oyzh.fx.plus.menu;

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
     * 获取菜单
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
        if (object instanceof Control control) {
            control.setContextMenu(null);
        } else if (object instanceof Tab tab) {
            tab.setContextMenu(null);
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
