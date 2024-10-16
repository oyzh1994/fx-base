package cn.oyzh.fx.plus.trees;

import cn.oyzh.fx.common.thread.Task;
import cn.oyzh.fx.common.util.CollectionUtil;
import cn.oyzh.fx.common.util.Destroyable;
import cn.oyzh.fx.plus.adapter.DestroyAdapter;
import cn.oyzh.fx.plus.adapter.MenuItemAdapter;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.node.NodeManager;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

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
public class RichTreeItem<V extends RichTreeItemValue> extends TreeItem<V> implements MenuItemAdapter, DragNodeItem, Comparable<Object>, DestroyAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 加载完成标志位
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    protected boolean loaded;

    /**
     * 加载中标志位
     */
    @Getter
    @Accessors(chain = true, fluent = true)
    protected boolean loading;

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

    public RichTreeItem(@NonNull RichTreeView treeView) {
        this.treeView = treeView;
    }

    /**
     * 获取渲染服务
     *
     * @return 渲染服务
     */
    private RichTreeViewService service() {
        return this.treeView.service();
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
        return this.getRichChildren().filtered(item -> {
            if (this.filterable) {
                return item.itemVisible();
            }
            return true;
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
        if (this.valueGraphic() instanceof SVGGlyph glyph) {
            glyph.startWaiting();
        }
    }

    /**
     * 开始等待
     *
     * @param task 待执行业务
     */
    public void startWaiting(Task task) {
        if (this.valueGraphic() instanceof SVGGlyph glyph) {
            glyph.startWaiting(task);
        }
    }

    /**
     * 取消等待
     */
    public void stopWaiting() {
        if (this.valueGraphic() instanceof SVGGlyph glyph) {
            glyph.stopWaiting();
        }
    }

    /**
     * 是否等待中
     *
     * @return 结果
     */
    public boolean isWaiting() {
        if (this.valueGraphic() instanceof SVGGlyph glyph) {
            return glyph.isWaiting();
        }
        return false;
    }

    /**
     * 获取当前图标
     *
     * @return 图标组件
     */
    public Node valueGraphic() {
        if (this.getValue() == null) {
            return null;
        }
        return this.getValue().graphic();
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
        return CollectionUtil.getFirst(this.getRealChildren());
    }

    /**
     * 清除子节点
     */
    public void clearChild() {
        if (this.isChildEmpty()) {
            return;
        }
        // 清理子节点
        Consumer<TreeItem<?>> clearChildren = (treeItem) -> {
            ObservableList<? extends TreeItem<?>> children;
            if (treeItem instanceof RichTreeItem<?> richTreeItem) {
                children = richTreeItem.getRealChildren();
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
            // if (treeItem instanceof Destroyable destroyable) {
            //     destroyable.destroy();
            // }
        };
        this.service().submitFXLater(() -> {
            ObservableList<TreeItem<V>> children = super.getChildren();
            for (TreeItem<?> child : children) {
                clearChildren.accept(child);
            }
            children.clear();
            this.treeView.flushLocal();
        });
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
    public void setChild(TreeItem<?> item) {
        if (item != null) {
            this.service().submitFX(() -> this.getRealChildren().setAll(item));
        }
    }

    /**
     * 设置子节点
     *
     * @param items 节点列表
     */
    public void setChild(List<TreeItem<?>> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.service().submitFX(() -> this.getRealChildren().setAll(items));
        }
    }

    /**
     * 添加子节点
     *
     * @param item 节点
     */
    public void addChild(TreeItem<?> item) {
        if (item != null) {
            this.service().submitFX(() -> this.getRealChildren().add(item));
        }
    }

    /**
     * 设置多个子节点
     *
     * @param items 节点列表
     */
    public void addChild(List<TreeItem<?>> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.service().submitFX(() -> this.getRealChildren().addAll(items));
        }
    }

    /**
     * 移除子节点
     *
     * @param item 节点
     */
    public void removeChild(TreeItem<?> item) {
        if (item != null) {
            this.service().submitFX(() -> this.getRealChildren().remove(item));
        }
    }

    /**
     * 移除多个子节点
     *
     * @param items 节点列表
     */
    public void removeChild(List<TreeItem<?>> items) {
        if (CollectionUtil.isNotEmpty(items)) {
            this.service().submitFX(() -> this.getRealChildren().removeAll(items));
        }
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
        RichTreeItemValue value = this.getValue();
        if (value != null) {
            value.flushText();
            value.flushGraphic();
            value.flushGraphicColor();
        }
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
        if (this.sortable && this.sortType != null) {
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
            ObservableList<RichTreeItem<?>> children = this.getRichChildren();
            if (!children.isEmpty()) {
                try {
                    this.sorting = true;
                    // asc
                    if (this.isSortAsc()) {
                        children.sort(RichTreeItem::compareTo);
                    } else {// desc
                        children.sort(Comparator.reverseOrder());
                    }
                } finally {
                    this.sorting = false;
                }
            }
        });
    }

    /**
     * 执行过滤
     */
    public void doFilter() {
        this.service().submit(() -> this.doFilter(this.treeView.itemFilter));
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
            this.service().submitFX(() -> this.setExpanded(true));
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
     * 刷新坐标，防止出现白屏
     */
    public void flushLocal() {
        this.service().submitFX(() -> this.getTreeView().flushLocal());
    }

    /**
     * 获取窗口
     *
     * @return 窗口
     */
    public Window window() {
        return this.getTreeView().window();
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

    public boolean supportFilter() {
        return false;
    }

    @Override
    public void destroy() {
        // for (TreeItem<V> child : super.getChildren()) {
        //     if (child instanceof Destroyable destroyable) {
        //         destroyable.destroy();
        //     }
        // }
        Object value = this.getValue();
        if (value instanceof Destroyable destroyable) {
            destroyable.destroy();
        }
        this.setValue(null);
        this.setParent(null);
        this.setGraphic(null);
        this.treeView = null;
        this.visibleProperty = null;
        // if (this.children != null) {
        //     this.children.removeListener(this.childrenListener);
        //     this.children = null;
        // }
        // this.childrenListener = null;
        // this.eventHandlerManager = null;
    }

    public boolean isSelected() {
        return this.getTreeView().isSelected(this);
    }

    /**
     * 刷新值
     */
    public void flushValue() {
        RichTreeItemValue value = this.getValue();
        if (value != null) {
            value.flush();
        }
    }
}
