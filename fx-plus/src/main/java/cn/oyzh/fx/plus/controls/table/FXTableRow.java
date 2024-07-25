package cn.oyzh.fx.plus.controls.table;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.plus.adapter.ContextMenuAdapter;
import cn.oyzh.fx.plus.adapter.MenuItemAdapter;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/07/25
 */
public class FXTableRow<T> extends TableRow<T> implements ContextMenuAdapter, MenuItemAdapter {

    {
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            List<? extends MenuItem> items = this.getMenuItems();
            if (CollUtil.isNotEmpty(items)) {
                this.showContextMenu(items, e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
    }
}
