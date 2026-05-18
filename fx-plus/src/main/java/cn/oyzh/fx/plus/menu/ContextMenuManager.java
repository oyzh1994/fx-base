package cn.oyzh.fx.plus.menu;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-06-25
 */
public class ContextMenuManager {

//    /**
//     * 菜单池
//     */
//    private static final ContextMenuPool POOL = new ContextMenuPool();

//    /**
//     * 获取菜单
//     *
//     * @return 菜单
//     */
//    public static ContextMenu getContextMenu() {
//        return POOL.borrowObject();
//    }

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
    public static ContextMenu createContextMenu(Object object, List<? extends MenuItem> items) {
        if (contextMenu == null) {
            contextMenu = new FXContextMenu(object);
        } else {
            contextMenu.setTarget(object);
        }
//        ContextMenu contextMenu = POOL.borrowObject();
        contextMenu.getItems().setAll(items);
        return contextMenu;
    }

    /**
     * 归还菜单
     *
     * @param contextMenu 菜单
     */
    public static void returnContextMenu(ContextMenu contextMenu) {
//        POOL.returnObject(contextMenu);
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
}
