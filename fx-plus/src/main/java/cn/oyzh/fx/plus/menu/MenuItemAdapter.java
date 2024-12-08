package cn.oyzh.fx.plus.menu;

import javafx.scene.control.MenuItem;

import java.util.List;

/**
 * 操作按钮适配器
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
