package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tree.view.FXTreeCell;
import cn.oyzh.fx.plus.drag.DragNodeHandler;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;

/**
 * 富功能树节点工厂
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class RichTreeCell<T extends RichTreeItemValue> extends FXTreeCell<T> {

    {
//        this.setCache(true);
//        this.setCacheShape(true);
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
//            this.setText(null);
//            this.setGraphic(null);
            return;
        }
        TreeItem<?> treeItem = this.getTreeItem();
        RichTreeView treeView = (RichTreeView) this.getTreeView();
        Node node = this.getGraphic();
        // 富文本模式
        if (value.isRichMode()) {
            if (node instanceof RichTreeItemBox box) {
                box.init(value, treeView.highlightText, treeView.highlightMatchCase);
            } else {
                this.setGraphic(new RichTreeItemBox(value, treeView.highlightText, treeView.highlightMatchCase));
            }
            this.setText(null);
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {// 标准模式
            // 获取图标
            SVGGlyph glyph = value.graphic();
//            Color color = value.graphicColor();
//            SVGGlyph graphic = node instanceof SVGGlyph ? glyph : null;
//            if (graphic == null) {
//                this.setGraphic(glyph);
//                if (glyph != null) {
//                    glyph.setColor(color);
//                }
//            } else if (graphic.getColor() != color) { // 更新图标颜色
//                graphic.setColor(color);
//            }
            // 更新颜色
            glyph.setColor(value.graphicColor());
            // 更新图标
            this.setGraphic(glyph);
            // 更新文本
            this.setText(value.text());
            this.setContentDisplay(ContentDisplay.LEFT);
        }
        // 初始化拖动
        if (treeItem instanceof DragNodeItem dragItem && dragItem.allowDragDrop() && this.dragNodeHandler == null) {
            this.dragNodeHandler = new DragNodeHandler();
            this.dragNodeHandler.initEvent(this, treeView.getDragContent());
            // BackgroundService.submit(() -> DragUtil.initDragNode(this.dragNodeHandler, this, treeView.getDragContent()));
        }
    }
}
