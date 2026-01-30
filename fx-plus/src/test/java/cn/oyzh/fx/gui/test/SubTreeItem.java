package cn.oyzh.fx.gui.test;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.drag.DragNodeItem;

import java.util.Objects;

/**
 * @author oyzh
 * @since 2025-08-29
 */
public class SubTreeItem extends RichTreeItem implements DragNodeItem {

    public SubTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public boolean allowDrag() {
        return true;
    }
}
