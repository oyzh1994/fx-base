package cn.oyzh.fx.plus.trees;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.common.thread.Task;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.thread.BackgroundService;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    protected volatile Byte sortType;

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
        return this.visibleProperty != null && this.visibleProperty().get();
    }

    /**
     * 禁用排序
     */
    private volatile byte sortState;

    /**
     * 是否过滤中
     */
    private volatile boolean filtering;

    /**
     * 是否可过滤
     */
    private volatile boolean filterable;

    /**
     * 真实子节点
     */
    private ObservableList<TreeItem<?>> realChildren;

    public RichTreeItem(RichTreeView treeView) {
        this.treeView = treeView;
    }

    /**
     * 设置是否可过滤
     *
     * @param filterable 是否可过滤
     */
    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
        // 可过滤则初始化真实子节点集合
        if (this.filterable) {
            this.realChildren = FXCollections.observableArrayList();
        }
        this.setVisible(true);
        this.getShowChildren().addListener((ListChangeListener<TreeItem<?>>) change -> this.doFilter());
    }

    /**
     * 排序是否启用
     */
    public boolean isSortEnable() {
        return this.sortState == 0;
    }

    /**
     * 禁用排序
     */
    public void disableSort() {
        this.sortState = 1;
    }

    /**
     * 启用排序
     */
    public void enableSort() {
        this.sortState = 0;
    }

    @Override
    public ObservableList getChildren() {
        return super.getChildren();
    }

    /**
     * 获取显示子节点列表
     *
     * @return 子节点列表
     */
    public ObservableList<TreeItem<?>> getShowChildren() {
        // 如果是可过滤则显示真实子节点列表
        return this.filterable ? this.realChildren : this.getChildren();
    }

    /**
     * 获取富功能子节点列表
     *
     * @return 子节点列表
     */
    public ObservableList<RichTreeItem<?>> getRichChildren() {
        List list = this.getShowChildren();
        return (ObservableList<RichTreeItem<?>>) list;
    }

    /**
     * 获取子节点大小
     *
     * @return 子节点大小
     */
    public int getChildrenSize() {
        return this.getShowChildren().size();
    }

    /**
     * 获取真实子节点大小
     *
     * @return 真实子节点大小
     */
    public int getRealChildrenSize() {
        return this.realChildren == null ? 0 : this.realChildren.size();
    }

    /**
     * 开始等待
     */
    public void startWaiting() {
        if (this.getValue().graphic() instanceof SVGGlyph glyph) {
            glyph.startWaiting();
        }
    }

    /**
     * 开始等待
     *
     * @param task 待执行业务
     */
    public void startWaiting(Task task) {
        if (this.getValue().graphic() instanceof SVGGlyph glyph) {
            glyph.startWaiting(task);
        }
    }

    /**
     * 取消等待
     */
    public void stopWaiting() {
        if (this.getValue().graphic() instanceof SVGGlyph glyph) {
            glyph.stopWaiting();
        }
    }

    /**
     * 是否等待中
     *
     * @return 结果
     */
    public boolean isWaiting() {
        if (this.getValue() != null && this.getValue().graphic() instanceof SVGGlyph glyph) {
            return glyph.isWaiting();
        }
        return false;
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
            FXUtil.runLater(() -> {
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
            treeItem.getShowChildren().remove(this);
        } else {
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
        return CollUtil.getFirst(this.getShowChildren());
    }

    /**
     * 清除子节点
     */
    public void clearChild() {
        FXUtil.runWait(this.getShowChildren()::clear);
    }

    /**
     * 是否包含节点
     *
     * @param item 节点
     * @return 结果
     */
    public boolean containsChild(TreeItem<?> item) {
        if (item != null) {
            return this.getShowChildren().contains(item);
        }
        return false;
    }

    /**
     * 设置子节点
     *
     * @param item 节点
     */
    public synchronized void setChild(@NonNull TreeItem<?> item) {
        FXUtil.runWait(() -> this.getShowChildren().setAll(item));
    }

    /**
     * 设置子节点
     *
     * @param items 节点列表
     */
    public synchronized void setChild(@NonNull List<TreeItem<?>> items) {
        FXUtil.runWait(() -> this.getShowChildren().setAll(items));
    }

    /**
     * 添加子节点
     *
     * @param item 节点
     */
    public synchronized void addChild(@NonNull TreeItem<?> item) {
        FXUtil.runWait(() -> this.getShowChildren().add(item));
    }

    /**
     * 设置多个子节点
     *
     * @param items 节点列表
     */
    public synchronized void addChild(@NonNull List<TreeItem<?>> items) {
        FXUtil.runWait(() -> this.getShowChildren().addAll(items));
    }

    /**
     * 移除子节点
     *
     * @param item 节点
     */
    public synchronized void removeChild(@NonNull TreeItem<?> item) {
        FXUtil.runWait(() -> this.getShowChildren().remove(item));
    }

    /**
     * 移除多个子节点
     *
     * @param items 节点列表
     */
    public synchronized void removeChild(@NonNull List<TreeItem<?>> items) {
        FXUtil.runWait(() -> this.getShowChildren().removeAll(items));
    }

    /**
     * 子节点是否为空
     *
     * @return 结果
     */
    public boolean isChildEmpty() {
        return this.getShowChildren().isEmpty();
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
        if (this.isSortEnable()) {
            this.sortType = 0;
            // 执行排序
            ObservableList<TreeItem<V>> subs = this.getChildren();
            if (!subs.isEmpty()) {
                subs.sort((a, b) -> CharSequence.compare(a.getValue().name(), b.getValue().name()));
            }
        }
    }

    /**
     * 对节点排序，倒序
     */
    public synchronized void sortDesc() {
        if (this.isSortEnable()) {
            this.sortType = 1;
            // 执行排序
            ObservableList<TreeItem<V>> subs = this.getChildren();
            if (!subs.isEmpty()) {
                subs.sort((a, b) -> CharSequence.compare(b.getValue().name(), a.getValue().name()));
            }
        }
    }

    /**
     * 执行过滤
     */
    public void doFilter() {
        BackgroundService.submit(() -> this.doFilter(this.treeView.itemFilter));
    }

    /**
     * 执行过滤
     *
     * @param itemFilter 节点过滤器
     */
    public synchronized void doFilter(RichTreeItemFilter itemFilter) {
        if (this.filtering) {
            return;
        }
        this.filtering = true;
        try {
            List<RichTreeItem<?>> richChildren = new CopyOnWriteArrayList<>(this.getRichChildren());
            if (this.filterable) {
                ObservableList<TreeItem<?>> children = this.getChildren();
                for (RichTreeItem<?> child : richChildren) {
                    if (itemFilter != null) {
                        child.setVisible(itemFilter.apply(child));
                        child.doFilter(itemFilter);
                    } else {
                        child.setVisible(true);
                    }
                }
                Set<TreeItem<?>> shows = new HashSet<>();
                Set<TreeItem<?>> hides = new HashSet<>();
                for (RichTreeItem<?> child : richChildren) {
                    if (child.itemVisible()) {
                        if (!children.contains(child)) {
                            shows.add(child);
                        }
                    } else if (children.contains(child)) {
                        hides.add(child);
                    }
                }
                for (TreeItem<?> child : children) {
                    if (!richChildren.contains(child)) {
                        hides.add(child);
                    }
                }
                if (!hides.isEmpty()) {
                    children.removeAll(hides);
                }
                if (!shows.isEmpty()) {
                    children.addAll(shows);
                }
            } else {
                for (RichTreeItem<?> child : richChildren) {
                    child.doFilter(itemFilter);
                }
            }
            this.sort();
//            this.flushLocal();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            this.filtering = false;
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
