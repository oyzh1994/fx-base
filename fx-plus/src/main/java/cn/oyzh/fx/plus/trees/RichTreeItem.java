package cn.oyzh.fx.plus.trees;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.fx.common.thread.Task;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tree.FlexTreeView;
import cn.oyzh.fx.plus.drag.DragNodeItem;
import cn.oyzh.fx.plus.util.FXUtil;
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
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 富功能树节点
 *
 * @author oyzh
 * @since 2023/11/10
 */
@Slf4j
public abstract class RichTreeItem extends TreeItem implements DragNodeItem {

    /**
     * 当前排序类型
     * 0 asc 1 desc
     */
    @Getter
    protected byte sortType;

    /**
     * 是否可见
     */
    @Getter
    @Setter
    protected boolean visible;

    /**
     * 获取当前组件
     *
     * @return 树组件
     */
    public abstract FlexTreeView treeView();

    /**
     * 设置当前树组件
     *
     * @param treeView 树组件
     */
    public abstract void treeView(FlexTreeView treeView);

    /**
     * 开始等待
     */
    public void startWaiting() {
        if (this.itemValue().graphic() instanceof SVGGlyph glyph) {
            glyph.startWaiting();
        }
    }

    /**
     * 开始等待
     *
     * @param task 待执行业务
     */
    public void startWaiting(Task task) {
        if (this.itemValue().graphic() instanceof SVGGlyph glyph) {
            glyph.startWaiting(task);
        }
    }

    /**
     * 取消等待
     */
    public void stopWaiting() {
        if (this.itemValue().graphic() instanceof SVGGlyph glyph) {
            glyph.stopWaiting();
        }
    }

    /**
     * 是否等待中
     *
     * @return 结果
     */
    public boolean isWaiting() {
        if (this.itemValue() != null && this.itemValue().graphic() instanceof SVGGlyph glyph) {
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
        } else {
            log.warn("remove fail, this.getParent() is null.");
        }
    }

    /**
     * 节点更名
     */
    public void rename() {
    }

    /**
     * 添加子节点
     *
     * @param item 节点
     */
    public void addChild(@NonNull TreeItem<?> item) {
        FXUtil.runWait(() -> this.getChildren().add(item));
    }

    /**
     * 移除子节点
     *
     * @param item 节点
     */
    public void removeChild(@NonNull TreeItem<?> item) {
        FXUtil.runWait(() -> this.getChildren().remove(item));
    }

    /**
     * 移除多个子节点
     *
     * @param items 节点列表
     */
    public void removeChildes(@NonNull List<TreeItem<?>> items) {
        // 移除节点
        FXUtil.runWait(() -> this.getChildren().removeAll(items));
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
        return CollUtil.isEmpty(this.getChildren());
    }

    /**
     * 刷新图标
     */
    public void flushGraphic() {
        this.itemValue().flushGraphic();
        this.itemValue().flushGraphicColor();
    }

    /**
     * 获取右键菜单按钮列表
     *
     * @return 右键菜单按钮列表
     */
    public abstract List<MenuItem> getMenuItems();

    /**
     * 获取节点值
     *
     * @return RichTreeItemValue
     */
    public RichTreeItemValue itemValue() {
        return (RichTreeItemValue) super.getValue();
    }

    /**
     * 设置节点值
     *
     * @param itemValue 节点值
     */
    public void itemValue(RichTreeItemValue itemValue) {
        super.setValue(itemValue);
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
            ObservableList<RichTreeItem> subs = this.getChildren();
            subs.sort((a, b) -> CharSequence.compare(a.itemValue().name(), b.itemValue().name()));
        }
    }

    /**
     * 从大到小排序
     */
    public void sortDesc() {
        this.sortType = 1;
        if (!this.isChildEmpty()) {
            // 执行排序
            ObservableList<RichTreeItem> subs = this.getChildren();
            subs.sort((a, b) -> CharSequence.compare(b.itemValue().name(), a.itemValue().name()));
        }
    }

    /**
     * 执行过滤
     */
    public void doFilter(RichTreeItemFilter itemFilter) {
        if (!this.isChildEmpty() && itemFilter != null) {
            List<RichTreeItem> childes = this.getChildren();
            for (RichTreeItem child : childes) {
                child.visible = itemFilter.apply(child);
                child.doFilter(itemFilter);
            }
        }
    }
}
