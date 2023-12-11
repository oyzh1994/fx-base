package cn.oyzh.fx.plus.trees;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.common.thread.Task;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.thread.BackgroundService;
import cn.oyzh.fx.plus.thread.RenderService;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 富功能树节点
 *
 * @author oyzh
 * @since 2023/11/10
 */
@Getter
public class RichTreeItem<V extends RichTreeItemValue> extends TreeItem<V> implements DragNodeItem {

    /**
     * 当前树组件
     */
    @Setter
    protected RichTreeView treeView;

    /**
     * 当前排序类型
     * 0 asc 1 desc -1 无
     */
    private volatile Byte sortType;

    /**
     * 排序中标志位
     */
    private volatile boolean sorting;

    /**
     * 是否可排序
     */
    @Setter
    @Getter
    private volatile boolean sortable = true;

    /**
     * 可见属性
     */
    protected SimpleBooleanProperty visibleProperty;

    /**
     * 获取可见属性
     *
     * @return 可见属性
     */
    public SimpleBooleanProperty visibleProperty() {
        if (this.visibleProperty == null) {
            this.visibleProperty = new SimpleBooleanProperty();
        }
        return this.visibleProperty;
    }

    /**
     * 设置可见状态
     *
     * @param visible 可见状态
     */
    public void setVisible(boolean visible) {
        this.visibleProperty().set(visible);
    }

    /**
     * 是否可见
     *
     * @return 结果
     */
    public boolean isVisible() {
        return this.visibleProperty == null || this.visibleProperty().get();
    }

    /**
     * 是否可过滤
     */
    @Setter
    private volatile boolean filterable;

    public RichTreeItem(RichTreeView treeView) {
        this.treeView = treeView;
    }

    @Override
    protected void updateChildren(ListChangeListener.Change<? extends TreeItem<V>> c) {
        super.updateChildren(c);
        if (!this.sorting) {
            this.doFilter();
        }
    }

    @Override
    public ObservableList getChildren() {
        return super.getChildren().filtered(item -> {
            if (item instanceof RichTreeItem<?> treeItem) {
                if (this.filterable) {
                    return treeItem.itemVisible();
                }
                return true;
            }
            return false;
        });
    }

    /**
     * 获取显示子节点列表
     *
     * @return 子节点列表
     */
    public ObservableList<TreeItem<?>> getRealChildren() {
        List list = super.getChildren();
        // 如果是可过滤则显示真实子节点列表
        return (ObservableList<TreeItem<?>>) list;
    }

    /**
     * 获取富功能子节点列表
     *
     * @return 子节点列表
     */
    public ObservableList<RichTreeItem<?>> getRichChildren() {
        List list = super.getChildren();
        return (ObservableList<RichTreeItem<?>>) list;
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
     * 获取真实子节点大小
     *
     * @return 真实子节点大小
     */
    public int getRealChildrenSize() {
        return this.getRealChildren().size();
    }

    /**
     * 开始等待
     */
    public void startWaiting() {
        if (this.graphic() instanceof SVGGlyph glyph) {
            glyph.startWaiting();
        }
    }

    /**
     * 开始等待
     *
     * @param task 待执行业务
     */
    public void startWaiting(Task task) {
        if (this.graphic() instanceof SVGGlyph glyph) {
            glyph.startWaiting(task);
        }
    }

    /**
     * 取消等待
     */
    public void stopWaiting() {
        if (this.graphic() instanceof SVGGlyph glyph) {
            glyph.stopWaiting();
        }
    }

    /**
     * 是否等待中
     *
     * @return 结果
     */
    public boolean isWaiting() {
        if (this.graphic() instanceof SVGGlyph glyph) {
            return glyph.isWaiting();
        }
        return false;
    }

    /**
     * 获取当前图标
     *
     * @return 图标组件
     */
    public Node graphic() {
        if (this.getValue() == null) {
            return null;
        }
        return this.getValue().graphic();
    }

    /**
     * 自由处理
     * 如果是展开状态，则收缩节点
     * 如果是收缩状态，则展开节点
     */
    public void free() {
        if (this.isExpanded()) {
            this.collapse();
        } else {
            this.extend();
        }
    }

    /**
     * 重新展开
     */
    public void reExpanded() {
        if (this.isExpanded()) {
            BackgroundService.submitFXLater(() -> {
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
        if (parent instanceof RichTreeItem<?> treeItem) {
            treeItem.getRealChildren().remove(this);
        } else if (parent != null) {
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
        return CollUtil.getFirst(this.getRealChildren());
    }

    /**
     * 清除子节点
     */
    public void clearChild() {
        FXUtil.runWait(this.getRealChildren()::clear);
    }

    /**
     * 是否包含节点
     *
     * @param item 节点
     * @return 结果
     */
    public boolean containsChild(TreeItem<?> item) {
        if (item != null) {
            return this.getRealChildren().contains(item);
        }
        return false;
    }

    /**
     * 设置子节点
     *
     * @param item 节点
     */
    public synchronized void setChild(@NonNull TreeItem<?> item) {
        FXUtil.runWait(() -> this.getRealChildren().setAll(item));
    }

    /**
     * 设置子节点
     *
     * @param items 节点列表
     */
    public synchronized void setChild(@NonNull List<TreeItem<?>> items) {
        FXUtil.runWait(() -> this.getRealChildren().setAll(items));
    }

    /**
     * 添加子节点
     *
     * @param item 节点
     */
    public synchronized void addChild(@NonNull TreeItem<?> item) {
        FXUtil.runWait(() -> this.getRealChildren().add(item));
    }

    /**
     * 设置多个子节点
     *
     * @param items 节点列表
     */
    public synchronized void addChild(@NonNull List<TreeItem<?>> items) {
        FXUtil.runWait(() -> this.getRealChildren().addAll(items));
    }

    /**
     * 移除子节点
     *
     * @param item 节点
     */
    public synchronized void removeChild(@NonNull TreeItem<?> item) {
        FXUtil.runWait(() -> this.getRealChildren().remove(item));
    }

    /**
     * 移除多个子节点
     *
     * @param items 节点列表
     */
    public synchronized void removeChild(@NonNull List<TreeItem<?>> items) {
        FXUtil.runWait(() -> this.getRealChildren().removeAll(items));
    }

    /**
     * 子节点是否为空
     *
     * @return 结果
     */
    public boolean isChildEmpty() {
        return this.getRealChildren().isEmpty();
    }

    /**
     * 重载子节点
     */
    public void reloadChild() {
    }

    /**
     * 刷新图标
     */
    public void flushGraphic() {
        if (this.getValue() != null) {
            this.getValue().flushGraphic();
            this.getValue().flushGraphicColor();
        }
    }

    /**
     * 获取右键菜单按钮列表
     *
     * @return 右键菜单按钮列表
     */
    public List<MenuItem> getMenuItems() {
        return null;
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
     * 按照类型排序
     */
    public void sort() {
        if (this.sortType != null) {
            if (this.sortType == 0) {
                this.sortAsc();
            } else {
                this.sortDesc();
            }
        }
    }

    /**
     * 对节点排序，正序
     */
    public synchronized void sortAsc() {
        if (this.isSortable()) {
            this.sortType = 0;
            // 执行排序
            ObservableList<RichTreeItem<?>> children = this.getRichChildren();
            if (!children.isEmpty()) {
                try {
                    this.sorting = true;
                    children.sort(this::sortAsc);
                } finally {
                    this.sorting = false;
                }
            }
        }
    }

    /**
     * 节点排序正序实现
     *
     * @param item1 节点1
     * @param item2 节点2
     * @return 结果
     */
    protected int sortAsc(RichTreeItem<?> item1, RichTreeItem<?> item2) {
        if (item1 == item2 || item1 == null || item2 == null || item1.getValue() == null || item2.getValue() == null || item1.getValue().name() == null || item2.getValue().name() == null) {
            return 0;
        }
        return CharSequence.compare(item1.getValue().name(), item2.getValue().name());
    }

    /**
     * 对节点排序，倒序
     */
    public synchronized void sortDesc() {
        if (this.isSortable()) {
            this.sortType = 1;
            // 执行排序
            ObservableList<RichTreeItem<?>> children = this.getRichChildren();
            if (!children.isEmpty()) {
                try {
                    this.sorting = true;
                    children.sort(this::sortDesc);
                } finally {
                    this.sorting = false;
                }
            }
        }
    }

    /**
     * 节点排序倒序实现
     *
     * @param item1 节点1
     * @param item2 节点2
     * @return 结果
     */
    protected int sortDesc(RichTreeItem<?> item1, RichTreeItem<?> item2) {
        if (item1 == item2 || item1 == null || item2 == null || item1.getValue() == null || item2.getValue() == null || item1.getValue().name() == null || item2.getValue().name() == null) {
            return 0;
        }
        return CharSequence.compare(item2.getValue().name(), item1.getValue().name());
    }

    /**
     * 执行过滤
     */
    public void doFilter() {
        RenderService.submit(() -> this.doFilter(this.treeView.itemFilter));
    }

    /**
     * 执行过滤
     *
     * @param itemFilter 节点过滤器
     */
    public void doFilter(RichTreeItemFilter itemFilter) {
        try {
            List<RichTreeItem<?>> richChildren = new CopyOnWriteArrayList<>(this.getRichChildren());
            if (this.filterable) {
                for (RichTreeItem<?> child : richChildren) {
                    if (itemFilter != null) {
                        child.setVisible(itemFilter.test(child));
                        child.doFilter(itemFilter);
                    } else {
                        child.setVisible(true);
                    }
                }
            } else {
                for (RichTreeItem<?> child : richChildren) {
                    child.doFilter(itemFilter);
                }
            }
            this.sort();
            this.reExpanded();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 节点是否可见
     *
     * @return 结果
     */
    public boolean itemVisible() {
        return this.itemVisible(this);
    }

    /**
     * 节点是否可见
     *
     * @param item 节点
     * @return 结果
     */
    public boolean itemVisible(RichTreeItem<?> item) {
        if (item.isVisible()) {
            return true;
        }
        if (item.isLeaf() || item.isChildEmpty()) {
            return false;
        }
        return item.getRichChildren().parallelStream().anyMatch(RichTreeItem::itemVisible);
    }

    /**
     * 展开节点
     */
    public void extend() {
        if (!this.isExpanded()) {
            FXUtil.runWait(() -> this.setExpanded(true));
        }
    }

    /**
     * 收缩节点
     */
    public void collapse() {
        if (this.isExpanded()) {
            FXUtil.runWait(() -> this.setExpanded(false));
        }
    }

    /**
     * 收缩所有节点
     *
     * @param item 待收缩节点
     */
    public synchronized void collapseAll(TreeItem<?> item) {
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
    public synchronized void expandAll(TreeItem<?> item) {
        item.setExpanded(true);
        for (TreeItem<?> child : item.getChildren()) {
            this.expandAll(child);
        }
    }

    /**
     * 刷新坐标，防止出现白屏
     */
    public void flushLocal() {
        this.getTreeView().flushLocal();
    }

    /**
     * 获取窗口
     *
     * @return 窗口
     */
    public Window window() {
        return this.getTreeView().window();
    }
}
