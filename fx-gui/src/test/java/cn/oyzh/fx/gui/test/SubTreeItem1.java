package cn.oyzh.fx.gui.test;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import javafx.scene.control.TreeItem;

/**
 * @author oyzh
 * @since 2025-08-29
 */
public class SubTreeItem1 extends TreeItem implements DragNodeItem {

    public SubTreeItem1(String text) {
        super(text);
    }

    @Override
    public boolean allowDrag() {
        return true;
    }
}
