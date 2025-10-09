package cn.oyzh.fx.gui.test;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import com.sun.javafx.scene.NodeHelper;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeUtil;

import java.util.Objects;

/**
 * @author oyzh
 * @since 2025-08-29
 */
public class ParentTreeItem extends RichTreeItem implements DragNodeItem {

    public ParentTreeItem(RichTreeView treeView) {
        super(treeView);
    }

    @Override
    public boolean allowDropNode(DragNodeItem item) {
        if (item instanceof SubTreeItem subTreeItem) {
            return true;
        }
        return false;
    }

    @Override
    public void onDropNode(DragNodeItem item) {
        if (item instanceof SubTreeItem subTreeItem) {
            subTreeItem.remove();
            // this.flushLocal();
            this.addChild(subTreeItem);
            // this.flushLocal();
            // TreeUtil(this);
        }
    }

}
