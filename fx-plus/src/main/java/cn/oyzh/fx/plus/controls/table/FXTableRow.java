package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.fx.plus.adapter.ContextMenuAdapter;
import cn.oyzh.fx.plus.adapter.MenuItemAdapter;
import javafx.scene.control.TableRow;

/**
 * @author oyzh
 * @since 2024/07/25
 */
public class FXTableRow<T> extends TableRow<T> implements ContextMenuAdapter {

    {
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            T item = this.getItem();
            if (item instanceof MenuItemAdapter adapter) {
                this.showContextMenu(adapter.getMenuItems(), e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
    }
}
