package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.controls.tree.view.FlexTreeView;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TreeItem;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 富功能树
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class RichTreeView extends FlexTreeView {

    /**
     * 节点过滤器
     */
    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    protected RichTreeItemFilter itemFilter;

    @Override
    public RichTreeItem<?> getSelectedItem() {
        return (RichTreeItem<?>) super.getSelectedItem();
    }

    /**
     * 对节点排序，正序
     */
    public synchronized void sortAsc() {
        RichTreeItem<?> root = this.getRoot();
        // 可能为null
        if (root == null) {
            JulLog.warn("root is null");
            return;
        }
        // 获取选中节点
        RichTreeItem<?> item = this.getSelectedItem();
        if (item == null) {
            // 执行排序
            root.sortAsc();
        } else {
            // 执行排序
            item.sortAsc();
            // 重新选中此节点
            this.select(item);
        }
        this.refresh();
    }

    /**
     * 对节点排序，倒序
     */
    public synchronized void sortDesc() {
        RichTreeItem<?> root = this.getRoot();
        // 可能为null
        if (root == null) {
            JulLog.warn("root is null");
            return;
        }
        // 获取选中节点
        RichTreeItem<?> item = this.getSelectedItem();
        if (item == null) {
            // 执行排序
            root.sortDesc();
        } else {
            // 执行排序
            item.sortDesc();
            // 重新选中此节点
            this.select(item);
        }
        this.refresh();
    }

    @Override
    public RichTreeItem<?> getRoot() {
        return (RichTreeItem<?>) super.getRoot();
    }

    @Override
    public void setRoot(TreeItem root) {
        if (root instanceof RichTreeItem<?> item) {
            FXUtil.runWait(() -> super.setRoot(root));
            item.doFilter();
        } else if (root != null) {
            throw new IllegalArgumentException("Root is not a RichTreeItem");
        }
    }

    /**
     * 过滤节点
     */
    public synchronized void filter() {
        RichTreeItem<?> root = this.getRoot();
        // 可能为null
        if (root == null) {
            JulLog.warn("root is null");
            return;
        }
        // 获取选中节点
        TreeItem<?> item = this.getSelectedItem();
        // 清除选中节点
        this.clearSelection();
        // 执行过滤
        root.doFilter();
        // 选中并滚动节点
        this.selectAndScroll(item);
        // 刷新
        this.refresh();
    }

    public void selectedItemChanged(ChangeListener<?> listener) {
        this.getSelectionModel().selectedItemProperty().addListener(listener);
    }
}
