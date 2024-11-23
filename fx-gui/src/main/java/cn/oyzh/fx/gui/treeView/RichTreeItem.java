package cn.oyzh.fx.gui.treeView;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.adapter.DestroyAdapter;
import cn.oyzh.fx.plus.adapter.MenuItemAdapter;
import cn.oyzh.fx.plus.controls.treeView.FXTreeItem;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 富功能树节点
 *
 * @author oyzh
 * @since 2023/11/10
 */
@Getter
public class RichTreeItem<V extends RichTreeItemValue> extends FXTreeItem<V> implements MenuItemAdapter, DragNodeItem, Comparable<Object>, DestroyAdapter {

    /**
     * 加载完成标志位
     */
    private Boolean loaded;

    /**
     * 加载中标志位
     */
    private Boolean loading;

    /**
     * 当前排序类型
     * 0 asc 1 desc -1 无
     */
    private Byte sortType;

    /**
     * 排序中标志位
     */
    private Boolean sorting;

    /**
     * 是否可排序
     */
    private Boolean sortable;

    /**
     * 可见属性
     */
    private Boolean visible;

    /**
     * 设置可见状态
     *
     * @param visible 可见状态
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * 是否可见
     *
     * @return 结果
     */
    public boolean isVisible() {
        return this.visible == null || this.visible;
    }

    public void setSorting(boolean sorting) {
        this.sorting = sorting;
    }

    public boolean isSorting() {
        return this.sorting != null && this.visible;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isSortable() {
        return this.sortable == null || this.sortable;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public boolean isLoaded() {
        return this.loaded != null && this.loaded;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isLoading() {
        return this.loading != null && this.loading;
    }

    /**
     * 是否可过滤
     */
    @Setter
    private volatile boolean filterable;

    public RichTreeItem(@NonNull RichTreeView treeView) {
        super(treeView);
    }

    public RichTreeView getTreeView() {
        return (RichTreeView) super.getTreeView();
    }

    @Override
    protected void updateChildren(ListChangeListener.Change<? extends TreeItem<V>> c) {
        super.updateChildren(c);
        if (!this.isSorting()) {
            this.doFilter();
        }
    }

    @Override
    public ObservableList getChildren() {
        return super.getChildren().filtered(item -> {
            if (item instanceof RichTreeItem<?> richItem && richItem.filterable) {
                return richItem.itemVisible();
            }
            return true;
        });
    }

    /**
     * 获取显示子节点列表
     *
     * @return 子节点列表
     */
    public ObservableList<TreeItem<?>> unfilteredChildren() {
        List list = super.getChildren();
        // 如果是可过滤则显示真实子节点列表
        return (ObservableList<TreeItem<?>>) list;
    }

    /**
     * 获取富功能子节点列表
     *
     * @return 子节点列表
     */
    public ObservableList<RichTreeItem<?>> richChildren() {
        List list = super.getChildren();
        return (ObservableList<RichTreeItem<?>>) list;
    }

    @Override
    public void remove() {
        if (this.parent() instanceof RichTreeItem<?> treeItem) {
            treeItem.unfilteredChildren().remove(this);
        } else {
            super.remove();
        }
    }

    @Override
    public TreeItem<?> firstChild() {
        return CollectionUtil.getFirst(this.unfilteredChildren());
    }

    @Override
    public void clearChild() {
        if (this.isChildEmpty()) {
            return;
        }
        // 清理子节点
        Consumer<TreeItem<?>> clearChildren = (treeItem) -> {
            ObservableList<? extends TreeItem<?>> children;
            if (treeItem instanceof RichTreeItem<?> richTreeItem) {
                children = richTreeItem.unfilteredChildren();
            } else {
                children = treeItem.getChildren();
            }
            for (TreeItem<?> child : children) {
                if (child instanceof RichTreeItem<?> item) {
                    item.clearChild();
                } else {
                    child.getChildren().clear();
                }
            }
            children.clear();
        };
        this.service().submitFXLater(() -> {
            ObservableList<TreeItem<V>> children = super.getChildren();
            for (TreeItem<?> child : children) {
                clearChildren.accept(child);
            }
            children.clear();
            this.flushLocal();
        });
    }

    @Override
    public boolean containsChild(TreeItem<?> item) {
        if (item != null) {
            return this.unfilteredChildren().contains(item);
        }
        return false;
    }

    @Override
    public void setChild(TreeItem<?> item) {
        if (item != null) {
            this.service().submitFX(() -> this.unfilteredChildren().setAll(item));
        }
    }

    @Override
    public void setChild(List<TreeItem<?>> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.service().submitFX(() -> this.unfilteredChildren().setAll(items));
        }
    }

    @Override
    public void addChild(TreeItem<?> item) {
        if (item != null) {
            this.service().submitFX(() -> this.unfilteredChildren().add(item));
        }
    }

    @Override
    public void addChild(List<TreeItem<?>> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.service().submitFX(() -> this.unfilteredChildren().addAll(items));
        }
    }

    @Override
    public void removeChild(TreeItem<?> item) {
        if (item != null) {
            this.service().submitFX(() -> this.unfilteredChildren().remove(item));
        }
    }

    @Override
    public void removeChild(List<TreeItem<?>> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.service().submitFX(() -> this.unfilteredChildren().removeAll(items));
        }
    }

    @Override
    public boolean isChildEmpty() {
        return this.unfilteredChildren().isEmpty();
    }

    /**
     * 按照类型排序
     */
    public void sort() {
        if (this.isSortable() && this.sortType != null) {
            if (this.isSortAsc()) {
                this.sortAsc();
            } else {
                this.sortDesc();
            }
        }
    }

    /**
     * 对节点排序，正序
     */
    public void sortAsc() {
        if (this.isSortable()) {
            this.sortType = 0;
            this.sortChild();
        }
    }

    /**
     * 对节点排序，倒序
     */
    public void sortDesc() {
        if (this.isSortable()) {
            this.sortType = 1;
            this.sortChild();
        }
    }

    /**
     * 是否asc排序
     *
     * @return 结果
     */
    protected boolean isSortAsc() {
        return this.sortType != null && this.sortType == 0;
    }

    /**
     * 子节点排序
     */
    protected void sortChild() {
        this.service().submitFX(() -> {
            // 执行排序
            ObservableList<RichTreeItem<?>> children = this.richChildren();
            if (!children.isEmpty()) {
                try {
                    this.setSorting(true);
                    // asc
                    if (this.isSortAsc()) {
                        children.sort(RichTreeItem::compareTo);
                    } else {// desc
                        children.sort(Comparator.reverseOrder());
                    }
                } finally {
                    this.setSorting(false);
                }
            }
        });
    }

    /**
     * 执行过滤
     */
    public void doFilter() {
        this.service().submit(() -> this.doFilter(this.getTreeView().itemFilter));
    }

    /**
     * 执行过滤
     *
     * @param itemFilter 节点过滤器
     */
    public synchronized void doFilter(RichTreeItemFilter itemFilter) {
        try {
            List<RichTreeItem<?>> richChildren = new CopyOnWriteArrayList<>(this.richChildren());
            if (this.filterable) {
                richChildren.parallelStream().forEach(child -> {
                    if (itemFilter != null) {
                        child.setVisible(itemFilter.test(child));
                        child.doFilter(itemFilter);
                    } else {
                        child.setVisible(true);
                    }
                });
            } else {
                richChildren.parallelStream().forEach(child -> child.doFilter(itemFilter));
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
        return item.richChildren().parallelStream().anyMatch(RichTreeItem::itemVisible);
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) {
            return 0;
        }
        if (o == null) {
            return 1;
        }
        if (o instanceof RichTreeItem<?> item) {
            if (item.getValue() == this.getValue() || item.getValue() == null || this.getValue() == null) {
                return 0;
            }
            return CharSequence.compare(this.getValue().name(), item.getValue().name());
        }
        return 0;
    }

    @Override
    public void destroy() {
        super.destroy();
        this.sortType = null;
        this.visible = null;
    }
}
