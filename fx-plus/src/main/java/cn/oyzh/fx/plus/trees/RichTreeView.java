package cn.oyzh.fx.plus.trees;

import cn.oyzh.fx.plus.controls.tree.FlexTreeView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.stage.Window;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 富功能树
 *
 * @author oyzh
 * @since 2023/11/10
 */
@Slf4j
@Accessors(chain = true, fluent = true)
public class RichTreeView extends FlexTreeView {

    {
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    /**
     * 拖动内容
     */
    @Getter
    protected String dragContent = "rich_drag";

    /**
     * 节点过滤器
     */
    @Setter
    @Getter
    protected RichTreeItemFilter itemFilter;

    /**
     * 获取窗口
     *
     * @return 窗口
     */
    public Window window() {
        return this.getScene().getWindow();
    }

    @Override
    public void selectAndScroll(TreeItem<?> item) {
        if (item != null) {
            super.selectAndScroll(item);
        } else {
            this.clearSelection();
        }
    }

    /**
     * 对节点排序
     */
    public void sortAsc() {
        // 获取选中节点
        TreeItem<?> item = this.getSelectedItem();
        // 执行排序
        if (item instanceof RichTreeItem treeItem) {
            treeItem.sortAsc();
        }
        // 重新选中此节点
        this.select(item);
        this.flushLocal();
    }

    /**
     * 对节点排序
     */
    public void sortDesc() {
        // 获取选中节点
        TreeItem<?> item = this.getSelectedItem();
        // 执行排序
        if (item instanceof RichTreeItem treeItem) {
            treeItem.sortDesc();
        }
        // 重新选中此节点
        this.select(item);
        this.flushLocal();
    }

    /**
     * 过滤节点
     */
    public void filter() {
        // 获取选中节点
        TreeItem<?> item = this.getSelectedItem();
        // 清除选中节点
        this.clearSelection();
        // 执行过滤
        this.root().doFilter();
        // 选中并滚动节点
        this.selectAndScroll(item);
        this.flushLocal();
    }

    /**
     * 展开节点
     */
    public void expand() {
        TreeItem<?> item = this.getSelectedItem();
        if (item instanceof RichTreeItem treeItem) {
            treeItem.extend();
        }
        if (item != null) {
            this.select(item);
        }
        this.flushLocal();
    }

    /**
     * 收缩节点
     */
    public void collapse() {
        TreeItem<?> item = this.getSelectedItem();
        if (item instanceof RichTreeItem treeItem) {
            treeItem.collapse();
        }
        if (item != null) {
            this.select(item);
        }
        this.flushLocal();
    }

    /**
     * 重新载入
     */
    public void reload() {
        TreeItem<?> item = this.getSelectedItem();
        if (item instanceof RichTreeItem treeItem) {
            treeItem.reloadChild();
        }
        this.flushLocal();
    }
}
