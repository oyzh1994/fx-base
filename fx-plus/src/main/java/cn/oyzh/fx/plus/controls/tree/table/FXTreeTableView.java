package cn.oyzh.fx.plus.controls.tree.table;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.Destroyable;
import cn.oyzh.fx.plus.adapter.DestroyAdapter;
import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.menu.ContextMenuAdapter;
import cn.oyzh.fx.plus.mouse.MouseAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import lombok.NonNull;
import lombok.ToString;

import java.util.function.Consumer;

/**
 * 树形结构
 *
 * @author oyzh
 * @since 2022/1/19
 */
@ToString
public class FXTreeTableView extends TreeTableView implements DestroyAdapter, NodeAdapter, ThemeAdapter, ContextMenuAdapter, MouseAdapter, SelectAdapter<TreeItem<?>>, StateAdapter {

    {
        NodeManager.init(this);
    }

//    @Override
//    public void setOnMousePrimaryClicked(EventHandler<? super MouseEvent> handler) {
//        MouseAdapter.super.setOnMousePrimaryClicked(handler);
//    }
//
//    @Override
//    public EventHandler<? super MouseEvent> getOnMousePrimaryClicked() {
//        return MouseAdapter.super.getOnMousePrimaryClicked();
//    }
//
//    @Override
//    public void setOnMouseSecondClicked(EventHandler<? super MouseEvent> handler) {
//        MouseAdapter.super.setOnMouseSecondClicked(handler);
//    }
//
//    @Override
//    public EventHandler<? super MouseEvent> getOnMouseSecondClicked() {
//        return MouseAdapter.super.getOnMouseSecondClicked();
//    }

    /**
     * 重新选择节点
     */
    public void reselect() {
        TreeItem<?> item = this.getSelectedItem();
        if (item == null) {
            this.select(null);
        } else {
            this.clearSelection();
            this.select(item);
        }
    }

    /**
     * 节点变更事件
     *
     * @param consumer 消费器
     */
    public void selectItemChanged(@NonNull Consumer<TreeItem<?>> consumer) {
        this.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!this.isIgnoreChanged()) {
                consumer.accept((TreeItem<?>) newValue);
            }
        });
    }

    @Override
    public void scrollTo(int index) {
        FXUtil.runWait(() -> super.scrollTo(index));
    }

    /**
     * 滚动到目标节点
     *
     * @param item 节点
     */
    public void scrollTo(TreeItem<?> item) {
        if (item != null) {
            int index = this.getRow(item);
            if (index >= 0) {
                this.scrollTo(index);
            } else {
                JulLog.warn("row index:{} invalid.", index);
            }
        }
    }

    /**
     * 选中并滚动到节点
     *
     * @param item 节点
     */
    public void selectAndScroll(TreeItem<?> item) {
        this.select(item);
        this.scrollTo(item);
    }

    /**
     * 节点是否选中
     *
     * @param item 节点
     * @return 结果
     */
    public boolean isSelected(TreeItem<?> item) {
        return item != null && this.getSelectedItem() == item;
    }

    /**
     * 刷新坐标，防止出现白屏
     */
    public void flushLocal() {
        TaskManager.startDelay("tree:flushLocal:" + this.hashCode(), () -> FXUtil.runLater(() -> {
            this.layoutChildren();
            this.localToScreen(this.getBoundsInLocal());
            this.refresh();
        }), 100);
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        ThemeAdapter.super.changeTheme(style);
        this.refresh();
    }

    @Override
    public void initNode() {

    }

    @Override
    public void destroy() {
        if (this.getRoot() instanceof Destroyable destroyable) {
            destroyable.destroy();
            this.setRoot(null);
        }
    }

    /**
     * 定位节点
     */
    public void positionItem() {
        this.scrollTo(this.getSelectedItem());
    }

    // @Override
    // public void setStateManager(StateManager manager) {
    //
    // }
    //
    // @Override
    // public StateManager getStateManager() {
    //     return null;
    // }
}
