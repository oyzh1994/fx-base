package cn.oyzh.fx.plus.menu;

import cn.oyzh.common.util.CollectionUtil;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

import java.util.List;

/**
 * 操作面板适配器
 *
 * @author oyzh
 * @since 2023/5/15
 */
public interface ContextMenuAdapter {

    /**
     * 显示操作面板
     *
     * @param menuItems 菜单列表
     * @param event     鼠标事件
     */
    default void showContextMenu(List<MenuItem> menuItems, MouseEvent event) {
        this.showContextMenu(menuItems, event.getScreenX(), event.getScreenY());
    }

    /**
     * 初始化操作面板
     *
     * @param menuItems 菜单列表
     * @return ContextMenu
     */
    default ContextMenu initContextMenu(List<? extends MenuItem> menuItems) {
//        ContextMenu contextMenu = this.contextMenu();
        if (CollectionUtil.isNotEmpty(menuItems)) {
//            if (contextMenu == null) {
            ContextMenu contextMenu = ContextMenuManager.createContextMenu(this, menuItems);
            this.setContextMenu(contextMenu);
//            } else if (!contextMenu.getItems().equals(menuItems)) {
//                MenuItemManager.returnMenuItem(contextMenu.getItems());
//                // DestroyUtil.destroy(contextMenu.getItems());
//                contextMenu.getItems().setAll(menuItems);
//            }
            return contextMenu;
        }
//        this.clearContextMenu();
        return null;
    }

    /**
     * 设置操作面板
     * @param contextMenu 操作面板
     */
   default void setContextMenu(ContextMenu contextMenu) {
        ContextMenuManager.setContextMenu(this, contextMenu);
    }

    /**
     * 显示操作面板
     *
     * @param menuItems 菜单列表
     * @param screenX   屏幕x
     * @param screenY   屏幕y
     */
    default void showContextMenu(List<? extends MenuItem> menuItems, Double screenX, Double screenY) {
        ContextMenu contextMenu = this.initContextMenu(menuItems);
        if (contextMenu != null && screenX != null && screenY != null) {
            if (this instanceof Node node) {
                contextMenu.show(node, screenX, screenY);
            } else if (this instanceof Window window) {
                contextMenu.show(window, screenX, screenY);
            }
        }
    }

    /**
     * 隐藏操作面板
     */
    default void hideContextMenu() {
        ContextMenu menu = ContextMenuManager.getContextMenu(this);
        if (menu != null) {
            menu.hide();
        }
    }

    /**
     * 清除操作面板
     */
    default void clearContextMenu() {
        ContextMenuManager.clearContextMenu(this);
    }
}
