package cn.oyzh.fx.plus.menu;

import javafx.scene.control.MenuItem;

import java.util.List;

/**
 * 菜单项适配器，用于提供右键菜单项列表
 *
 * @author oyzh
 * @since 2024/07/25
 */
public interface MenuItemAdapter {

    /**
     * 获取右键菜单按钮列表
     *
     * @return 右键菜单按钮列表
     */
    default List<? extends MenuItem> getMenuItems() {
        return null;
    }
}
