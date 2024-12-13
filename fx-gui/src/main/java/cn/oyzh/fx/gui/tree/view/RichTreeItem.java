package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.fx.plus.adapter.DestroyAdapter;
import cn.oyzh.fx.plus.controls.tree.view.FXTreeItem;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.menu.MenuItemAdapter;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.NonNull;

import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * 富功能树节点
 *
 * @author oyzh
 * @since 2023/11/10
 */
public abstract class RichTreeItem<V extends RichTreeItemValue> extends FXTreeItem<V> implements MenuItemAdapter, DragNodeItem, Comparable<Object>, DestroyAdapter {

    /**
     * bit值设置，减少内存占用
     */
    protected BitSet bitValue;

    /**
     * bit值设置
     *
     * @return BitSet
     */
    protected BitSet bitValue() {
        if (this.bitValue == null) {
            // 18位
            this.bitValue = BitSet.valueOf(new byte[]{0b000_000_000_001_100_101});
        }
        return this.bitValue;
    }

    /**
     * 设置可见
     *
     * @param visible 可见
     */
    public void setVisible(boolean visible) {
        this.bitValue().set(0, visible);
    }

    /**
     * 是否可见
     *
     * @return 结果
     */
    public boolean isVisible() {
        return this.bitValue == null || this.bitValue().get(0);
    }

    /**
     * 设置排序中
     *
     * @param sorting 排序中
     */
    public void setSorting(boolean sorting) {
        this.bitValue().set(1, sorting);
    }

    /**
     * 是否排序中
     *
     * @return 结果
     */
    public boolean isSorting() {
        return this.bitValue().get(1);
    }

    /**
     * 设置可排序
     *
     * @param sortable 可排序
     */
    public void setSortable(boolean sortable) {
        this.bitValue().set(2, sortable);
    }

    /**
     * 是否可排序
     *
     * @return 结果
     */
    public boolean isSortable() {
        return this.bitValue == null || this.bitValue().get(2);
    }

    /**
     * 设置已加载
     *
     * @param loaded 已加载
     */
    public void setLoaded(boolean loaded) {
        this.bitValue().set(3, loaded);
    }

    /**
     * 是否已加载
     *
     * @return 结果
     */
    public boolean isLoaded() {
        return this.bitValue().get(3);
    }

    /**
     * 设置加载中
     *
     * @param loading 加载中
     */
    public void setLoading(boolean loading) {
        this.bitValue().set(4, loading);
    }

    /**
     * 是否加载中
     *
     * @return 结果
     */
    public boolean isLoading() {
        return this.bitValue().get(4);
    }

    /**
     * 设置可过滤
     *
     * @param filterable 可过滤
     */
    public void setFilterable(boolean filterable) {
        this.bitValue().set(5, filterable);
    }

    /**
     * 是否可过滤
     *
     * @return 结果
     */
    public boolean isFilterable() {
        return this.bitValue == null || this.bitValue().get(5);
    }

    /**
     * 设置asr排序
     *
     * @param sortAsc asr排序
     */
    public void setSortAsc(boolean sortAsc) {
        this.bitValue().set(6, sortAsc);
    }

    /**
     * 是否asc排序
     *
     * @return 结果
     */
    public boolean isSortAsc() {
        return this.bitValue().get(6);
    }

    public RichTreeItem(@NonNull RichTreeView treeView) {
        super(treeView);
    }

    public void loadChild() {

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
            if (item instanceof RichTreeItem<?> richItem && richItem.isFilterable()) {
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

    public int unfilteredChildrenSize() {
        return super.getChildren().size();
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
            this.refresh();
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
    public void setChild(TreeItem<?>... items) {
        if (ArrayUtil.isEmpty(items)) {
            this.service().submitFX(() -> this.unfilteredChildren().setAll(items));
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
        if (this.isSortable()) {
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
        if (this.isSortable() && !this.isSorting()) {
            this.setSortAsc(true);
            this.sortChild(true);
        }
    }

    /**
     * 对节点排序，倒序
     */
    public void sortDesc() {
        if (this.isSortable() && !this.isSorting()) {
            this.setSortAsc(false);
            this.sortChild(false);
        }
    }

    /**
     * 子节点排序
     */
    protected void sortChild(boolean sortAsc) {
        // this.service().submitFX(() -> {
        this.setSorting(true);
        try {
            // 执行排序
            ObservableList<RichTreeItem<?>> children = this.richChildren();
            if (!children.isEmpty()) {
                // asc
                if (sortAsc) {
                    children.sort(RichTreeItem::compareTo);
                } else {// desc
                    children.sort(Comparator.reverseOrder());
                }
            }
        } finally {
            this.setSorting(false);
        }
        // });
    }

    /**
     * 执行过滤
     */
    public void doFilter() {
        List<RichTreeItem<?>> items = this.richChildren();
        // this.doFilter(this.getTreeView().itemFilter, items);
        this.service().submit(() -> this.doFilter(this.getTreeView().itemFilter, items));
    }

    /**
     * 执行过滤
     *
     * @param itemFilter 节点过滤器
     */
    public void doFilter(RichTreeItemFilter itemFilter) {
        List<RichTreeItem<?>> items = this.richChildren();
        this.doFilter(itemFilter, items);
        // this.service().submit(() -> this.doFilter(itemFilter, items));
    }

    /**
     * 执行过滤
     *
     * @param itemFilter 节点过滤器
     * @param items      节点列表
     */
    public void doFilter(RichTreeItemFilter itemFilter, List<RichTreeItem<?>> items) {
        if (itemFilter != null) {
            try {
                if (this.isFilterable()) {
                    // if (itemFilter != null) {
                    items.forEach(child -> {
                        child.setVisible(itemFilter.test(child));
                        child.doFilter(itemFilter);
                    });
                    // } else {
                    //     items.forEach(child -> child.setVisible(true));
                    // }
                } else {
                    items.forEach(child -> child.doFilter(itemFilter));
                }
                this.sort();
                // this.reExpanded();
                this.refresh();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
        this.bitValue = null;
    }
}


