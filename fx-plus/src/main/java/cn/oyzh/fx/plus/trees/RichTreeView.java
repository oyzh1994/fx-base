package cn.oyzh.fx.plus.trees;

import cn.oyzh.fx.plus.controls.tree.FlexTreeView;
import cn.oyzh.fx.plus.keyboard.KeyListener;
import cn.oyzh.fx.plus.thread.QueueService;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.util.MouseUtil;
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
     * 渲染服务
     */
    protected QueueService service;

    /**
     * 获取渲染服务
     *
     * @return 渲染服务
     */
    public QueueService service() {
        if (this.service == null) {
            this.service = new QueueService();
        }
        return this.service;
    }

    /**
     * 初始化组件
     */
    protected void initTreeView() {
        this.initEvenListener();
    }

    /**
     * 初始化事件监听器
     */
    protected void initEvenListener() {
        // 右键菜单事件
        this.setOnContextMenuRequested(e -> {
            RichTreeItem<?> item = this.getSelectedItem();
            if (item != null) {
                this.showContextMenu(item.getMenuItems(), e.getScreenX() - 10, e.getScreenY() - 10);
            } else {
                this.clearContextMenu();
            }
        });
        // f2按键处理
        KeyListener.listenReleased(this, KeyCode.F2, event -> {
            RichTreeItem<?> item = this.getSelectedItem();
            if (item != null) {
                item.rename();
            }
        });
        // 删除按键处理
        KeyListener.listenReleased(this, KeyCode.DELETE, event -> {
            RichTreeItem<?> item = this.getSelectedItem();
            if (item != null) {
                item.delete();
            }
        });
        // 鼠标按键处理
        this.setOnMousePrimaryClicked(event -> {
            if (MouseUtil.isPrimaryButton(event)) {
                this.clearContextMenu();
                RichTreeItem<?> item = this.getSelectedItem();
                if (item != null) {
                    if (event.getClickCount() == 1) {
                        item.onPrimarySingleClick();
                    } else if (event.getClickCount() >= 2) {
                        item.onPrimaryDoubleClick();
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

    @Override
    public RichTreeItem<?> getSelectedItem() {
        return (RichTreeItem<?>) super.getSelectedItem();
    }

    /**
     * 对节点排序，正序
     */
    public synchronized void sortAsc() {
        // 获取选中节点
        RichTreeItem<?> item = this.getSelectedItem();
        if (item == null) {
            // 执行排序
            this.getRoot().sortAsc();
        } else {
            // 执行排序
            item.sortAsc();
            // 重新选中此节点
            this.select(item);
        }
        this.flushLocal();
    }

    /**
     * 对节点排序，倒序
     */
    public synchronized void sortDesc() {
        // 获取选中节点
        RichTreeItem<?> item = this.getSelectedItem();
        if (item == null) {
            // 执行排序
            this.getRoot().sortDesc();
        } else {
            // 执行排序
            item.sortDesc();
            // 重新选中此节点
            this.select(item);
        }
        this.flushLocal();
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
        // 获取选中节点
        TreeItem<?> item = this.getSelectedItem();
        // 清除选中节点
        this.clearSelection();
        // 执行过滤
        this.getRoot().doFilter();
        // 选中并滚动节点
        this.selectAndScroll(item);
        this.flushLocal();
    }

    /**
     * 展开节点
     */
    public synchronized void expand() {
        RichTreeItem<?> item = this.getSelectedItem();
        if (item != null) {
            item.expend();
            this.select(item);
            this.flushLocal();
        }
    }

    /**
     * 收缩节点
     */
    public synchronized void collapse() {
        RichTreeItem<?> item = this.getSelectedItem();
        if (item != null) {
            item.collapse();
            this.select(item);
            this.flushLocal();
        }
    }

    /**
     * 重新载入
     */
    public synchronized void reload() {
        RichTreeItem<?> item = this.getSelectedItem();
        if (item != null) {
            item.reloadChild();
            this.flushLocal();
        }
    }
}
