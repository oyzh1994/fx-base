package cn.oyzh.fx.plus.controls;

import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

/**
 * 表单列
 *
 * @author oyzh
 * @since 2022/12/21
 */
public abstract class FXTableCell<S, T> extends TableCell<S, T> {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    /**
     * 初始化图形
     *
     * @return 图形
     */
    public abstract Node initGraphic();

    /**
     * 获取表单内容
     *
     * @return 内容
     */
    public S getTableItem() {
        return this.getTableRow() == null ? null : this.getTableRow().getItem();
    }

    /**
     * 更新节点信息
     *
     * @param item  节点
     * @param empty 是否为空
     */
    @Override
    protected final void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            this.setGraphic(null);
        } else {
            this.setGraphic(this.initGraphic());
        }
    }
}
