package cn.oyzh.fx.plus.trees;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.common.thread.Task;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 富功能树节点
 *
 * @author oyzh
 * @since 2023/11/10
 */
@Getter
public class RichTreeItem<V extends RichTreeItemValue> extends TreeItem<V> implements DragNodeItem {

    /**
     * 当前排序类型
     * 0 asc 1 desc
     */
    protected volatile byte sortType;

    /**
     * 当前树组件
     */
    @Setter
    protected RichTreeView treeView;

    /**
     * 是否可见
     */
    @Setter
    protected volatile boolean visible = true;

    /**
     * 是否过滤中
     */
    protected volatile boolean filtering;

    /**
     * 真实子节点
     */
    protected final ObservableList<TreeItem<?>> realChildren = FXCollections.observableArrayList();

    {
        this.realChildren.addListener((ListChangeListener<TreeItem<?>>) change -> this.doFilter());
    }

    public RichTreeItem(RichTreeView treeView) {
        this.treeView = treeView;
    }

    @Override
    public ObservableList getChildren() {
        return super.getChildren();
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
     * 展开节点
     */
    public void extend() {
        FXUtil.runWait(() -> this.setExpanded(true));
    }

    /**
     * 收缩节点
     */
    public void collapse() {
        FXUtil.runWait(() -> this.setExpanded(false));
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
        if (this.getParent() != null) {
            this.getParent().getChildren().remove(this);
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
        return (TreeItem<?>) CollUtil.getFirst(this.getChildren());
    }

    /**
     * 清除子节点
     */
    public void clearChild() {
        FXUtil.runWait(this.realChildren::clear);
    }

    /**
     * 设置子节点
     *
     * @param item 节点
     */
    public void setChild(@NonNull TreeItem<?> item) {
        FXUtil.runWait(() -> this.realChildren.setAll(item));
    }

    /**
     * 设置子节点
     *
     * @param items 节点列表
     */
    public void setChild(@NonNull List<TreeItem<?>> items) {
        FXUtil.runWait(() -> this.realChildren.setAll(items));
    }

    /**
     * 添加子节点
     *
     * @param item 节点
     */
    public void addChild(@NonNull TreeItem<?> item) {
        FXUtil.runWait(() -> this.realChildren.add(item));
    }

    /**
     * 移除子节点
     *
     * @param item 节点
     */
    public void removeChild(@NonNull TreeItem<?> item) {
        FXUtil.runWait(() -> this.realChildren.remove(item));
    }


    /**
     * 移除多个子节点
     *
     * @param items 节点列表
     */
    public void removeChild(@NonNull List<TreeItem<?>> items) {
        // 移除节点
        FXUtil.runWait(() -> this.realChildren.removeAll(items));
    }

    /**
     * 重载子节点
     */
    public void reloadChild() {
    }

    /**
     * 子节点是否为空
     *
     * @return 结果
     */
    public boolean isChildEmpty() {
        return this.realChildren.isEmpty();
    }

    /**
     * 刷新图标
     */
    public void flushGraphic() {
        this.getValue().flushGraphic();
        this.getValue().flushGraphicColor();
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
        if (this.sortType == 0) {
            this.sortAsc();
        } else {
            this.sortDesc();
        }
    }

    /**
     * 从小到大排序
     */
    public void sortAsc() {
        this.sortType = 0;
        if (!this.isChildEmpty()) {
            // 执行排序
            ObservableList<TreeItem<V>> subs = this.getChildren();
            subs.sort((a, b) -> CharSequence.compare(a.getValue().name(), b.getValue().name()));
        }
    }

    /**
     * 从大到小排序
     */
    public void sortDesc() {
        this.sortType = 1;
        if (!this.isChildEmpty()) {
            // 执行排序
            ObservableList<TreeItem<V>> subs = this.getChildren();
            subs.sort((a, b) -> CharSequence.compare(b.getValue().name(), a.getValue().name()));
        }
    }

    /**
     * 执行过滤
     */
    public void doFilter() {
        this.doFilter(this.treeView.itemFilter);
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

            ObservableList children = this.getChildren();
            if (this.realChildren.isEmpty()) {
                children.clear();
            } else if (itemFilter == null) {
                List<TreeItem<?>> shows = new ArrayList<>();
                for (TreeItem<?> child : this.realChildren) {
                    if (!children.contains(child)) {
                        shows.add(child);
                    }
                }
                if (!shows.isEmpty()) {
                    children.addAll(shows);
                }
            } else {
                for (TreeItem<?> child : this.realChildren) {
                    if (child instanceof RichTreeItem<?> treeItem) {
                        treeItem.visible = itemFilter.apply(treeItem);
                        treeItem.doFilter(itemFilter);
                    }
                }
                List<TreeItem<?>> shows = new ArrayList<>();
                List<TreeItem<?>> hides = new ArrayList<>();
                for (TreeItem<?> child : this.realChildren) {
                    if (child instanceof RichTreeItem<?> treeItem) {
                        if (treeItem.itemVisible()) {
                            if (!children.contains(treeItem)) {
                                shows.add(treeItem);
                            }
                        } else if (children.contains(treeItem)) {
                            hides.add(treeItem);
                        }
                    } else if (!children.contains(child)) {
                        shows.add(child);
                    }
                }
                if (!hides.isEmpty()) {
                    children.removeAll(hides);
                }
                if (!shows.isEmpty()) {
                    children.addAll(shows);
                }
            }
            this.sort();
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
    public boolean itemVisible(TreeItem<?> item) {
        if (item instanceof RichTreeItem<?> richTreeItem) {
            if (richTreeItem.visible) {
                return true;
            }
            if (richTreeItem.isChildEmpty()) {
                return false;
            }
            for (TreeItem<?> child : this.realChildren) {
                if (child instanceof RichTreeItem<?> treeItem) {
                    if (treeItem.visible) {
                        return true;
                    }
                    if (treeItem.itemVisible()) {
                        return true;
                    }
                }
            }
        }
        return false;
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
            this.collapseAll(child);
        }
    }
}
