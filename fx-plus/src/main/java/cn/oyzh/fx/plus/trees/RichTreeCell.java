package cn.oyzh.fx.plus.trees;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tree.FXTreeCell;
import cn.oyzh.fx.plus.drag.DragNodeHandler;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.drag.DragUtil;
import cn.oyzh.fx.plus.thread.BackgroundService;
import javafx.scene.Cursor;
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
        this.setCursor(Cursor.HAND);
    }

    /**
     * 拖动处理
     */
    private DragNodeHandler dragNodeHandler;

    /**
     * 更新节点信息
     *
     * @param value 节点
     * @param empty 是否为空
     */
    @Override
    protected void updateItem(T value, boolean empty) {
        super.updateItem(value, empty);
        if (empty || value == null) {
            return;
        }
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

        // 初始化拖动
        if (this.getTreeItem() instanceof DragNodeItem dragNodeItem && dragNodeItem.allowDragDrop() && this.dragNodeHandler == null) {
            this.dragNodeHandler = new DragNodeHandler();
            RichTreeView treeView = (RichTreeView) this.getTreeView();
            BackgroundService.submit(() -> DragUtil.initDragNode(this.dragNodeHandler, this, treeView.dragContent()));
        }
    }
}
