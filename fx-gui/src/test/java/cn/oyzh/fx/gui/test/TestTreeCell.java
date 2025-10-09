package cn.oyzh.fx.gui.test;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tree.view.FXTreeCell;
import cn.oyzh.fx.plus.drag.DragNodeHandler;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.UUID;

/**
 * 富功能树节点工厂
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class TestTreeCell<T> extends FXTreeCell<T> {

    {
        this.setCursor(Cursor.HAND);
    }

    /**
     * 拖动处理
     */
    private DragNodeHandler dragNodeHandler;

    @Override
    protected void updateItem(T value, boolean empty) {
        super.updateItem(value, empty);
        if (empty || value == null) {
            return;
        }
        // 更新文本
        this.setText(value.toString());
        this.setContentDisplay(ContentDisplay.LEFT);
        TreeItem<?> treeItem = this.getTreeItem();
        // 初始化拖动
        if (treeItem instanceof DragNodeItem dragItem && dragItem.allowDragDrop() && this.dragNodeHandler == null) {
            this.dragNodeHandler = new DragNodeHandler();
            this.dragNodeHandler.initEvent(this, UUID.randomUUID().toString());
        }
    }
}
