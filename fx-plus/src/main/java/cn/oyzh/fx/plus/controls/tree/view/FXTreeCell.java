package cn.oyzh.fx.plus.controls.tree.view;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.control.TreeCell;

/**
 * 树列
 *
 * @author oyzh
 * @since 2023/03/31
 */
public abstract class FXTreeCell<T> extends TreeCell<T> implements StateAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.setText(null);
            this.setGraphic(null);
        }
    }
}
