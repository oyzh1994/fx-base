package cn.oyzh.fx.plus.adapter;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import lombok.NonNull;

import java.util.List;

/**
 * 操作面板适配器
 *
 * @author oyzh
 * @since 2023/5/15
 */
public interface ContextMenuAdapter extends PropAdapter {

    /**
     * 获取操作面板
     *
     * @return ContextMenu
     */
    private ContextMenu _getContextMenu() {
        if (this instanceof Control control) {
            return control.getContextMenu();
        }
        if (this instanceof Tab tab) {
            return tab.getContextMenu();
        }
        return null;
    }

    /**
     * 设置操作面板
     *
     * @param contextMenu 操作面板
     */
    private void _setContextMenu(ContextMenu contextMenu) {
        if (this instanceof Control control) {
            control.setContextMenu(contextMenu);
        } else if (this instanceof Tab tab) {
            tab.setContextMenu(contextMenu);
        }
    }

    /**
     * 显示操作面板
     *
     * @param menuItems 菜单列表
     * @param event     鼠标事件
     */
    default void showContextMenu(List<MenuItem> menuItems, @NonNull MouseEvent event) {
        this.showContextMenu(menuItems, event.getScreenX(), event.getScreenY());
    }

    /**
     * 初始化操作面板
     *
     * @param menuItems 菜单列表
     * @return ContextMenu
     */
    default ContextMenu initContextMenu(List<MenuItem> menuItems) {
        if (menuItems != null && !menuItems.isEmpty()) {
            ContextMenu contextMenu = this._getContextMenu();
            if (contextMenu == null) {
                contextMenu = new ContextMenu();
                this._setContextMenu(contextMenu);
            }
            contextMenu.getItems().setAll(menuItems);
            return contextMenu;
        }
        this._setContextMenu(null);
        return null;
    }

    /**
     * 显示操作面板
     *
     * @param menuItems 菜单列表
     * @param screenX   屏幕x
     * @param screenY   屏幕y
     */
    default void showContextMenu(List<MenuItem> menuItems, Double screenX, Double screenY) {
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
        ContextMenu menu = this._getContextMenu();
        if (menu != null) {
            menu.hide();
        }
    }

    /**
     * 清除操作面板
     */
    default void clearContextMenu() {
        ContextMenu menu = this._getContextMenu();
        if (menu != null) {
            menu.hide();
            this._setContextMenu(null);
        }
    }
}
