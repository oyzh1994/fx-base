package cn.oyzh.fx.plus.controls.tree;

import cn.oyzh.fx.common.thread.TaskManager;
import cn.oyzh.fx.plus.adapter.ContextMenuAdapter;
import cn.oyzh.fx.plus.adapter.MouseAdapter;
import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.trees.RichTreeItem;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * 树形结构
 *
 * @author oyzh
 * @since 2022/1/19
 */
@Slf4j
@ToString
public class FlexTreeView extends TreeView implements ThemeAdapter, ContextMenuAdapter, FlexAdapter, MouseAdapter, SelectAdapter<TreeItem<?>>, StateAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
        this.expandedItemCountProperty().addListener((observable, oldValue, newValue) -> this.flushLocal());
    }

    @Override
    public void setOnMousePrimaryClicked(EventHandler<? super MouseEvent> handler) {
        MouseAdapter.super.setOnMousePrimaryClicked(handler);
    }

    @Override
    public EventHandler<? super MouseEvent> getOnMousePrimaryClicked() {
        return MouseAdapter.super.getOnMousePrimaryClicked();
    }

    @Override
    public void setOnMouseSecondClicked(EventHandler<? super MouseEvent> handler) {
        MouseAdapter.super.setOnMouseSecondClicked(handler);
    }

    @Override
    public EventHandler<? super MouseEvent> getOnMouseSecondClicked() {
        return MouseAdapter.super.getOnMouseSecondClicked();
    }

    /**
     * 获取根节点
     *
     * @return 根节点
     */
    public RichTreeItem root() {
        return (RichTreeItem) super.getRoot();
    }

    /**
     * 设置根节点
     *
     * @param root 根节点
     */
    public void root(RichTreeItem root) {
        FXUtil.runWait(() -> super.setRoot(root));
        root.doFilter();
    }

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

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public String getFlexWidth() {
        return FlexAdapter.super.flexWidth();
    }

    @Override
    public void setFlexWidth(String flexWidth) {
        FlexAdapter.super.flexWidth(flexWidth);
    }

    public String getFlexHeight() {
        return FlexAdapter.super.flexHeight();
    }

    @Override
    public void setFlexHeight(String flexHeight) {
        FlexAdapter.super.flexHeight(flexHeight);
    }

    @Override
    public String getFlexX() {
        return FlexAdapter.super.flexX();
    }

    @Override
    public void setFlexX(String flexX) {
        FlexAdapter.super.flexX(flexX);
    }

    @Override
    public String getFlexY() {
        return FlexAdapter.super.flexY();
    }

    @Override
    public void setFlexY(String flexY) {
        FlexAdapter.super.flexY(flexY);
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
                log.warn("row index:{} invalid.", index);
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

    @Override
    public double getRealWidth() {
        return FlexAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        FlexAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return FlexAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        FlexAdapter.super.realHeight(height);
    }

    /**
     * 刷新坐标，防止出现白屏
     */
    public void flushLocal() {
        try {
            TaskManager.startDelayTask("tree:flushLocal:" + this, () -> {
                FXUtil.runLater(() -> {
                    this.layoutChildren();
                    this.localToScreen(this.getBoundsInLocal());
                });
            }, 100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void setStateManager(StateManager manager) {
        FlexAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return FlexAdapter.super.stateManager();
    }
}
