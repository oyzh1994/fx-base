package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tree.view.FXTreeView;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.text.Font;

import java.util.List;

/**
 * 富功能树
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class RichTreeView extends FXTreeView implements FontAdapter {

    /**
     * 高亮文本
     */
    protected String highlight;

    /**
     * 高亮需要匹配大小写
     */
    protected boolean highlightMatchCase;

    /**
     * 获取节点过滤器
     *
     * @return 节点过滤器
     */
    public RichTreeItemFilter getItemFilter() {
        return itemFilter;
    }

    /**
     * 设置节点过滤器
     *
     * @param itemFilter 节点过滤器
     */
    public void setItemFilter(RichTreeItemFilter itemFilter) {
        this.itemFilter = itemFilter;
    }

    /**
     * 高亮是否匹配大小写
     *
     * @return 高亮是否匹配大小写
     */
    public boolean isHighlightMatchCase() {
        return highlightMatchCase;
    }

    /**
     * 设置高亮是否匹配大小写
     *
     * @param highlightMatchCase 高亮是否匹配大小写
     */
    public void setHighlightMatchCase(boolean highlightMatchCase) {
        this.highlightMatchCase = highlightMatchCase;
    }

    /**
     * 获取高亮文本
     *
     * @return 高亮文本
     */
    public String getHighlight() {
        return highlight;
    }

    /**
     * 设置高亮文本
     *
     * @param highlight 高亮文本
     */
    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    /**
     * 节点过滤器
     */
    protected RichTreeItemFilter itemFilter;

    @Override
    public RichTreeItem<?> getSelectedItem() {
        return (RichTreeItem<?>) super.getSelectedItem();
    }

    /**
     * 对节点排序，正序
     */
    public synchronized void sortAsc() {
        RichTreeItem<?> root = this.root();
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
        RichTreeItem<?> root = this.root();
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

    /**
     * 获取根节点
     *
     * @return 根节点
     */
    public RichTreeItem<?> root() {
        return (RichTreeItem<?>) super.getRoot();
    }

    /**
     * 设置根节点
     *
     * @param root 根节点
     */
    public void root(TreeItem<?> root) {
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
        RichTreeItem<?> root = this.root();
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

    /**
     * 监听选中节点变化
     *
     * @param listener 变更监听器
     */
    public void selectedItemChanged(ChangeListener<?> listener) {
        this.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    @Override
    public void changeFont(Font font) {
        List<TreeItem<?>> treeItems = this.getAllItem();
        for (TreeItem<?> treeItem : treeItems) {
            if (treeItem instanceof RichTreeItem<?> richTreeItem
                    && richTreeItem.getValue() != null
                    && richTreeItem.getValue().graphic() != null) {
                SVGGlyph glyph = richTreeItem.getValue().graphic();
                glyph.setSize(font.getSize());
            }
        }
        FontAdapter.super.changeFont(font);
    }
}
