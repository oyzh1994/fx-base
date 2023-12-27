package cn.oyzh.fx.plus.trees;

import cn.oyzh.fx.plus.controls.tree.FlexTreeView;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.util.MouseUtil;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.stage.Window;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 富功能树
 *
 * @author oyzh
 * @since 2023/11/10
 */
@Accessors(chain = true, fluent = true)
public class RichTreeView extends FlexTreeView {

    {
        this.initTreeView();
    }

    /**
     * 初始化组件
     */
    protected void initTreeView() {
        // 选中模式
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            TreeItem<?> item = this.getSelectedItem();
            if (item instanceof RichTreeItem<?> treeItem) {
                this.showContextMenu(treeItem.getMenuItems(), e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
        this.initEvenListener();
    }

    /**
     * 初始化事件监听器
     */
    protected void initEvenListener() {
        // f2按键处理
        KeyListener.listenReleased(this, KeyCode.F2, event -> {
            TreeItem<?> item = this.getSelectedItem();
            if (item instanceof RichTreeItem<?> treeItem) {
                treeItem.rename();
            }
        });
        // 删除按键处理
        KeyListener.listenReleased(this, KeyCode.DELETE, event -> {
            TreeItem<?> item = this.getSelectedItem();
            if (item instanceof RichTreeItem<?> treeItem) {
                treeItem.delete();
            }
        });
        // 鼠标按键处理
        this.setOnMousePrimaryClicked(event -> {
            if (MouseUtil.isPrimaryButton(event)) {
                this.clearContextMenu();
                TreeItem<?> item = this.getSelectedItem();
                if (item instanceof RichTreeItem<?> treeItem) {
                    if (event.getClickCount() == 1) {
                        treeItem.onPrimarySingleClick();
                    } else if (event.getClickCount() >= 2) {
                        treeItem.onPrimaryDoubleClick();
                    }
                }
            }
        });
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
     * 对节点排序，正序
     */
    public synchronized void sortAsc() {
        // 获取选中节点
        TreeItem<?> item = this.getSelectedItem();
        // 执行排序
        if (item instanceof RichTreeItem<?> treeItem) {
            treeItem.sortAsc();
        }
        // 重新选中此节点
        this.select(item);
        this.flushLocal();
    }

    /**
     * 对节点排序，倒序
     */
    public synchronized void sortDesc() {
        // 获取选中节点
        TreeItem<?> item = this.getSelectedItem();
        // 执行排序
        if (item instanceof RichTreeItem<?> treeItem) {
            treeItem.sortDesc();
        }
        // 重新选中此节点
        this.select(item);
        this.flushLocal();
    }

    /**
     * 过滤节点
     */
    public synchronized void filter() {
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
        if (item instanceof RichTreeItem<?> treeItem) {
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
        if (item instanceof RichTreeItem<?> treeItem) {
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
        if (item instanceof RichTreeItem<?> treeItem) {
            treeItem.reloadChild();
        }
        this.flushLocal();
    }
}
