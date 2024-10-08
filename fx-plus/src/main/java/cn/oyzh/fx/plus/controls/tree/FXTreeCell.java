package cn.oyzh.fx.plus.controls.tree;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeCell;

/**
 * 树列
 *
 * @author oyzh
 * @since 2023/03/31
 */
public abstract class FXTreeCell<T> extends TreeCell<T> implements StateAdapter, ThemeAdapter {

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
        this.setText(null);
        // this.setDisclosureNode(null);
        if (empty) {
            this.setGraphic(null);
        } else {
            Node node = this.initGraphic();
            if (node != this.getGraphic()) {
                this.setGraphic(node);
            }
        }
    }

    @Override
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }
}
