package cn.oyzh.fx.gui.test;

import cn.oyzh.fx.plus.drag.DragNodeItem;
import javafx.scene.control.TreeItem;

/**
 * 树拖拽测试中的父节点（基于原生 TreeItem）
 *
 * @author oyzh
 * @since 2025-09-12
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
            this.getChildren().add(subTreeItem);
        }
    }

}
