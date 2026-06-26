package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TableCell;

/**
 * 表单列
 *
 * @author oyzh
 * @since 2022/12/21
 */
public class FXTableCell<S, T> extends TableCell<S, T> implements NodeAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 行高
     */
    protected double lineHeight;

    public double getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(double lineHeight) {
        this.lineHeight = lineHeight;
    }

    /**
     * 获取表单内容
     *
     * @return 内容
     */
    public S getTableItem() {
        return this.getTableRow() == null ? null : this.getTableRow().getItem();
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        if (item == getItem()) {
            return;
        }
        super.updateItem(item, empty);
        if (item == null) {
            super.setText(null);
            super.setGraphic(null);
        } else {
            if (item instanceof Node) {
                super.setText(null);
                super.setGraphic((Node) item);
            } else {
                super.setText(item.toString());
                super.setGraphic(null);
            }
            // 设置行高
            this.initLineHeight();
        }
    }

    /**
     * 初始化行高
     */
    protected void initLineHeight() {
        if (this.lineHeight > 0) {
            this.getTableRow().setMinHeight(this.lineHeight);
            this.getTableRow().setMaxHeight(this.lineHeight);
            this.getTableRow().setPrefHeight(this.lineHeight);
        }
    }

//    @Override
//    public void initNode() {
//        this.setPadding(Insets.EMPTY);
//        NodeAdapter.super.initNode();
//    }
}
