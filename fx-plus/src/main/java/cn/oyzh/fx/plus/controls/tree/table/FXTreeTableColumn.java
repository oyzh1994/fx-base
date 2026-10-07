package cn.oyzh.fx.plus.controls.tree.table;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.control.TreeTableColumn;

/**
 * 树形表格列
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class FXTreeTableColumn<S, T> extends TreeTableColumn<S, T> implements FlexAdapter, ThemeAdapter, FontAdapter, TipAdapter, StateAdapter, NodeAdapter, LayoutAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 构造树表列对象。
     */
    public FXTreeTableColumn() {
        super();
    }

    /**
     * 构造树表列对象。
     *
     * @param text 文本
     */
    public FXTreeTableColumn(String text) {
        super(text);
    }
}
