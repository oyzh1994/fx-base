package cn.oyzh.fx.plus.controls.tree.view;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.Destroyable;
import cn.oyzh.fx.plus.adapter.DestroyAdapter;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.thread.QueueService;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

/**
 * 富功能树节点
 *
 * @author oyzh
 * @since 2023/11/10
 */
@Getter
public abstract class FXTreeItem<V extends FXTreeItemValue> extends TreeItem<V> implements MenuItemAdapter, DragNodeItem, Comparable<Object>, DestroyAdapter {

    {
        NodeManager.init(this);
    }

    private FXTreeView treeView;

    public FXTreeItem(@NonNull FXTreeView treeView) {
        this.setTreeView(treeView);
    }

    public void setTreeView(FXTreeView treeView) {
        this.treeView = treeView;
    }

    public FXTreeView getTreeView() {
        return treeView;
    }

    /**
     * 获取渲染服务
     *
     * @return 渲染服务
     */
    protected QueueService service() {
        if (this.treeView == null) {
            return null;
        }
        return this.treeView.service();
    }

    /**
     * 获取子节点大小
     *
     * @return 子节点大小
     */
    public int getChildrenSize() {
        return this.getChildren().size();
    }

    /**
     * 开始等待
     */
    public void stopWaiting() {
        if (this.itemGraphic() instanceof SVGGlyph glyph) {
            glyph.stopWaiting();
        }
    }

    /**
     * 开始等待
     *
     * @param task 待执行业务
     */
    public void startWaiting(Runnable task) {
        this.startWaiting(task, true);
    }

    /**
     * 开始等待
     *
     * @param task      待执行业务
     * @param autoClose 自动关闭动画
     */
    public void startWaiting(Runnable task, boolean autoClose) {
        if (this.itemGraphic() instanceof SVGGlyph glyph) {
            glyph.startWaiting();
            TaskManager.startDelay(() -> {
                try {
                    if (task != null) {
                        task.run();
                    }
                } finally {
                    if (autoClose) {
                        glyph.stopWaiting();
                    }
                }
            }, 50);
        }
    }

    /**
     * 开始等待
     *
     * @param task  待执行业务
     * @param delay 延迟时间
     */
    public void startWaiting(Runnable task, int delay) {
        if (this.itemGraphic() instanceof SVGGlyph glyph) {
            glyph.startWaiting();
            TaskManager.startDelay(() -> {
                try {
                    if (task != null) {
                        task.run();
                    }
                } finally {
                    TaskManager.start(glyph::stopWaiting);
                }
            }, delay);
        }
    }

    /**
     * 是否等待中
     *
     * @return 结果
     */
    public boolean isWaiting() {
        if (this.itemGraphic() instanceof SVGGlyph glyph) {
            return glyph.isWaiting();
        }
        return false;
    }

    /**
     * 获取当前图标
     *
     * @return 图标组件
     */
    public SVGGlyph itemGraphic() {
        SVGGlyph glyph = null;
        if (this.getValue() != null) {
            glyph = this.getValue().graphic();
        }
        if (glyph == null) {
            glyph = (SVGGlyph) this.getGraphic();
        }
        return glyph;
    }

    /**
     * 重新展开
     */
    public void reExpanded() {
        if (this.isExpanded()) {
            this.service().submitFX(() -> {
                this.setExpanded(false);
                this.setExpanded(true);
            });
        }
    }

    /**
     * 删除节点
     */
    public void delete() {
    }

    /**
     * 移除节点
     */
    public void remove() {
        TreeItem<?> parent = this.getParent();
        if (parent != null) {
            parent.getChildren().remove(this);
        }
    }

    /**
     * 节点更名
     */
    public void rename() {
    }

    /**
     * 获取首个子节点
     *
     * @return 首个子节点
     */
    public TreeItem<?> firstChild() {
        return CollectionUtil.getFirst(this.getChildren());
    }

    /**
     * 清除子节点
     */
    public void clearChild() {
        if (this.isChildEmpty()) {
            return;
        }
        this.getChildren().clear();
        this.treeView.refresh();
    }

    /**
     * 是否包含节点
     *
     * @param item 节点
     * @return 结果
     */
    public boolean containsChild(TreeItem<?> item) {
        if (item != null) {
            return this.getChildren().contains(item);
        }
        return false;
    }

    /**
     * 设置子节点
     *
     * @param item 节点
     */
    public void setChild(TreeItem<?> item) {
        if (item != null) {
            this.getChildren().setAll((TreeItem) item);
        }
    }

    /**
     * 设置子节点
     *
     * @param items 节点列表
     */
    public void setChild(TreeItem<?>... items) {
        if (ArrayUtil.isNotEmpty(items)) {
            TreeItem[] treeItems = items;
            this.service().submitFX(() -> this.getChildren().setAll(treeItems));
        }
    }

    /**
     * 设置子节点
     *
     * @param items 节点列表
     */
    public void setChild(List<TreeItem<?>> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.service().submitFX(() -> this.getChildren().setAll((List) items));
        }
    }

    /**
     * 添加子节点
     *
     * @param item 节点
     */
    public void addChild(TreeItem<?> item) {
        if (item != null) {
            this.service().submitFX(() -> this.getChildren().add((TreeItem) item));
        }
    }

    /**
     * 设置多个子节点
     *
     * @param items 节点列表
     */
    public void addChild(List<TreeItem<?>> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.service().submitFX(() -> this.getChildren().addAll((List) items));
        }
    }

    /**
     * 移除子节点
     *
     * @param item 节点
     */
    public void removeChild(TreeItem<?> item) {
        if (item != null) {
            this.service().submitFX(() -> this.getChildren().remove(item));
        }
    }

    /**
     * 移除多个子节点
     *
     * @param items 节点列表
     */
    public void removeChild(List<TreeItem<?>> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.service().submitFX(() -> this.getChildren().removeAll(items));
        }
    }

    /**
     * 子节点是否为空
     *
     * @return 结果
     */
    public boolean isChildEmpty() {
        return this.getChildren().isEmpty();
    }

    /**
     * 重载子节点
     */
    public void reloadChild() {
    }

    @Override
    public Effect getDragEffect() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetY(3);
        shadow.setOffsetX(3);
        shadow.setColor(Color.RED);
        return shadow;
    }

    @Override
    public Effect getDropEffect() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetY(3);
        shadow.setOffsetX(3);
        shadow.setColor(Color.DARKRED);
        return shadow;
    }

    /**
     * 展开节点
     */
    public void expend() {
        if (!this.isExpanded()) {
            QueueService service = this.service();
            if (service != null) {
                service.submitFX(() -> this.setExpanded(true));
            }
        }
    }

    /**
     * 收缩节点
     */
    public void collapse() {
        if (this.isExpanded()) {
            this.service().submitFX(() -> this.setExpanded(false));
        }
    }

    /**
     * 收缩所有节点
     *
     * @param item 待收缩节点
     */
    public void collapseAll(TreeItem<?> item) {
        item.setExpanded(false);
        for (TreeItem<?> child : item.getChildren()) {
            this.collapseAll(child);
        }
    }

    /**
     * 展开所有节点
     *
     * @param item 待展开节点
     */
    public void expandAll(TreeItem<?> item) {
        item.setExpanded(true);
        for (TreeItem<?> child : item.getChildren()) {
            this.expandAll(child);
        }
    }

    /**
     * 刷新treeview
     */
    public void refresh() {
        QueueService service = this.service();
        if (service != null) {
            service.submitFXLater(() -> {
                FXTreeView treeView = this.getTreeView();
                if (treeView != null) {
                    treeView.refresh();
                }
            });
        }
    }

    /**
     * 刷新坐标，防止出现白屏
     */
    public void flushLocal() {
        QueueService service = this.service();
        if (service != null) {
            service.submit(() -> this.getTreeView().flushLocal());
        }
    }

    /**
     * 鼠标主按钮单击事件
     */
    public void onPrimarySingleClick() {

    }

    /**
     * 鼠标主按钮双击事件
     */
    public void onPrimaryDoubleClick() {

    }

    @Override
    public void destroy() {
        for (TreeItem<V> child : super.getChildren()) {
            if (child instanceof Destroyable destroyable) {
                destroyable.destroy();
            }
        }
        Object value = this.getValue();
        if (value instanceof Destroyable destroyable) {
            destroyable.destroy();
        }
        this.setValue(null);
        this.setParent(null);
        this.setGraphic(null);
        this.clearChild();
        this.treeView = null;
    }

    /**
     * 当前节点的父节点
     *
     * @return 父节点
     */
    public TreeItem<?> parent() {
        return this.getParent();
    }

    /**
     * 获取窗口
     *
     * @return 窗口
     */
    public Window window() {
        return this.getTreeView().window();
    }

    public boolean isSelected() {
        return this.getTreeView().isSelected(this);
    }
}
