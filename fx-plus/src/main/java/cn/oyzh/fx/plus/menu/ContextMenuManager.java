package cn.oyzh.fx.plus.menu;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-06-25
 */
public class ContextMenuManager {

    /**
     * 菜单池
     */
    private static final ContextMenuPool POOL = new ContextMenuPool();

    /**
     * 获取菜单
     *
     * @return 菜单
     */
    public static ContextMenu getContextMenu() {
        return POOL.borrowObject();
    }

    /**
     * 获取菜单
     *
     * @param items 列表项
     * @return 菜单
     */
    public static ContextMenu getContextMenu(List<? extends MenuItem> items) {
        ContextMenu contextMenu = POOL.borrowObject();
        contextMenu.getItems().setAll(items);
        return contextMenu;
    }

    /**
     * 归还菜单
     *
     * @param contextMenu 菜单
     */
    public static void returnContextMenu(ContextMenu contextMenu) {
        POOL.returnObject(contextMenu);
    }
}
