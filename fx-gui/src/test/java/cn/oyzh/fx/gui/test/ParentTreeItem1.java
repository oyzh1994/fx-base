package cn.oyzh.fx.gui.test;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import javafx.scene.control.TreeItem;

/**
 * @author oyzh
 * @since 2025-08-29
 */
public class ParentTreeItem1 extends TreeItem implements DragNodeItem {

    public ParentTreeItem1(String text) {
        super(text);
    }

    @Override
    public boolean allowDropNode(DragNodeItem item) {
        if (item instanceof SubTreeItem1 subTreeItem) {
            return true;
        }
        return false;
    }

    @Override
    public void onDropNode(DragNodeItem item) {
        if (item instanceof SubTreeItem1 subTreeItem) {
            subTreeItem.getParent().getChildren().remove(subTreeItem);
            this.getChildren().add(subTreeItem);
        }
    }

}
