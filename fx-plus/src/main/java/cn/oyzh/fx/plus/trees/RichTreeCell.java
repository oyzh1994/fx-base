package cn.oyzh.fx.plus.trees;

import cn.oyzh.fx.plus.controls.tree.FXTreeCell;
import cn.oyzh.fx.plus.drag.DragNodeHandler;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.drag.DragUtil;
import cn.oyzh.fx.plus.thread.BackgroundService;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import lombok.Getter;

/**
 * 富功能树节点工厂
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class RichTreeCell<T extends RichTreeItemValue> extends FXTreeCell<T> {

    {
        this.setCursor(Cursor.HAND);
    }

    /**
     * 拖动处理
     */
    private DragNodeHandler dragNodeHandler;

    @Override
    public Node initGraphic() {
        TreeItem<?> item = this.getTreeItem();
        RichTreeView treeView = (RichTreeView) this.getTreeView();
        // 初始化拖动
        if (item instanceof DragNodeItem dragNodeItem && dragNodeItem.allowDragDrop() && this.dragNodeHandler == null) {
            this.dragNodeHandler = new DragNodeHandler();
            BackgroundService.submit(() -> DragUtil.initDragNode(this.dragNodeHandler, this, treeView.dragContent()));
        }
        // 刷新图标
        if (item instanceof RichTreeItem<?> treeItem) {
            treeItem.flushGraphic();
            return treeItem.getValue();
        }
        return null;
    }
}
