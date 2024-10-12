package cn.oyzh.fx.plus.controls.table;

import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import lombok.Getter;
import lombok.Setter;

/**
 * 表单列
 *
 * @author oyzh
 * @since 2022/12/21
 */
public class LineHeightTableCell<S, T> extends TableCell<S, T> implements ThemeAdapter {

    /**
     * 行高
     */
    @Getter
    @Setter
    private double lineHeight ;

    /**
     * 获取表单内容
     *
     * @return 内容
     */
    public S getTableItem() {
        return this.getTableRow() == null ? null : this.getTableRow().getItem();
    }

    @Override
    protected final void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.setText(null);
            this.setGraphic(null);
        } else {
            if (item instanceof Node node) {
                this.setText(null);
                this.setGraphic(node);
            } else {
                this.setText(item.toString());
                this.setGraphic(null);
            }
            if (this.lineHeight > 0) {
                this.getTableRow().setMinHeight(this.lineHeight);
                this.getTableRow().setMaxHeight(this.lineHeight);
                this.getTableRow().setPrefHeight(this.lineHeight);
            }
        }
    }
}
