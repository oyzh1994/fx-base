package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tree.view.FXTreeCell;
import cn.oyzh.fx.plus.drag.DragNodeHandler;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;

/**
 * 富功能树节点工厂
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class RichTreeCell<T extends RichTreeItemValue> extends FXTreeCell<T> {

    {
        this.setCacheShape(true);
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
            this.setText(null);
            this.setGraphic(null);
            return;
        }
        TreeItem<?> treeItem = this.getTreeItem();
        if (value.isRichMode()) {
            Node node = this.getGraphic();
            if (node instanceof RichTreeItemBox box) {
                box.init(value);
            } else {
                this.setGraphic(new RichTreeItemBox(value));
            }
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            // 获取图标
            SVGGlyph glyph = value.graphic();
            SVGGlyph graphic = (SVGGlyph) this.getGraphic();
            // 更新图标
            if (graphic == null || graphic != glyph) {
                this.setGraphic(glyph);
                graphic = glyph;
            }
            // 更新图标颜色
            if (graphic != null) {
                Color color = value.graphicColor();
                if (graphic.getColor() != color) {
                    graphic.setColor(color);
                }
            }
            // 刷新文本
            this.setText(value.text());
            this.setContentDisplay(ContentDisplay.LEFT);
        }
        // 初始化拖动
        if (treeItem instanceof DragNodeItem dragItem && dragItem.allowDragDrop() && this.dragNodeHandler == null) {
            this.dragNodeHandler = new DragNodeHandler();
            RichTreeView treeView = (RichTreeView) this.getTreeView();
            this.dragNodeHandler.initEvent(this, treeView.getDragContent());
            // BackgroundService.submit(() -> DragUtil.initDragNode(this.dragNodeHandler, this, treeView.getDragContent()));
        }
    }
}
