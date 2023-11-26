package cn.oyzh.fx.plus.trees;

import cn.oyzh.fx.plus.controls.tree.FXTreeCell;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.drag.DragUtil;
import cn.oyzh.fx.plus.drag.DrapNodeHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 富功能树节点工厂
 *
 * @author oyzh
 * @since 2023/11/10
 */
@Getter
@Slf4j
public class RichTreeCell<T extends RichTreeItemValue> extends FXTreeCell<T> {

    /**
     * 拖动处理
     */
    private DrapNodeHandler drapNodeHandler;

    {
        this.setCursor(Cursor.HAND);
    }

    @Override
    public Node initGraphic() {
        TreeItem<?> item = this.getTreeItem();
        RichTreeView treeView = (RichTreeView) this.getTreeView();
        // 初始化拖动
        if (item instanceof DragNodeItem dragNodeItem && dragNodeItem.allowDragDrop() && this.drapNodeHandler == null) {
            this.drapNodeHandler = new DrapNodeHandler();
            DragUtil.initDragNode(this.drapNodeHandler, this, treeView.dragContent());
        }

        // 刷新图标
        if (item instanceof RichTreeItem<?> treeItem) {
            treeItem.flushGraphic();
            treeView.flushLocal();
            return treeItem.getValue();
        }
        return null;
    }
}
