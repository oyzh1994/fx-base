package cn.oyzh.fx.plus.controls.treeTable;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeTableRow;

/**
 * 树列
 *
 * @author oyzh
 * @since 2023/03/31
 */
public abstract class FXTreeTableRow<T> extends TreeTableRow<T> implements StateAdapter, ThemeAdapter {

    {
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        NodeManager.init(this);
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
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.setGraphic(null);
        } else if (this.getGraphic() == null) {
            this.setGraphic(this.initGraphic());
        } else {
            this.setGraphic(this.initGraphic());
        }
    }
}
