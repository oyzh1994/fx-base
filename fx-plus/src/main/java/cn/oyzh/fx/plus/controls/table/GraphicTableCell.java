package cn.oyzh.fx.plus.controls.table;

import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;

/**
 * 表单列
 *
 * @author oyzh
 * @since 2022/12/21
 */
public abstract class GraphicTableCell<S, T> extends FXTableCell<S, T> {

    {
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    /**
     * 初始化图形
     *
     * @return 图形
     */
    public abstract Node initGraphic();

    /**
     * 更新节点信息
     *
     * @param item  节点
     * @param empty 是否为空
     */
    @Override
    protected final void updateItem(T item, boolean empty) {
        if (empty) {
            super.updateItem(item, empty);
        } else {
            Object obj = this.initGraphic();
            super.updateItem((T) obj, empty);
        }
    }
}
