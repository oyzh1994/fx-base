package cn.oyzh.fx.plus.controls.tree.table;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.control.TreeTableColumn;

/**
 * @author oyzh
 * @since 2024-11-21
 */
public class FXTreeTableColumn<S, T> extends TreeTableColumn<S, T> implements ThemeAdapter, FontAdapter, TipAdapter, StateAdapter, NodeAdapter, LayoutAdapter {

    {
        NodeManager.init(this);
    }

    public FXTreeTableColumn( ) {
        super();
    }

    public FXTreeTableColumn(String text) {
        super(text);
    }
}
