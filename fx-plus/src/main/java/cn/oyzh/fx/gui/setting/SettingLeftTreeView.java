package cn.oyzh.fx.gui.setting;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置左侧树视图
 *
 * @author oyzh
 * @since 2024/12/29
 */
public class SettingLeftTreeView extends RichTreeView {

    @Override
    public SettingLeftTreeItem root() {
        return (SettingLeftTreeItem) super.getRoot();
    }

//    @Override
//    public SettingLeftTreeItem getRoot() {
//        return (SettingLeftTreeItem) super.getRoot();
//    }

    /**
     * 向根节点添加子节点
     *
     * @param item 节点值
     * @return 新增的树节点
     */
    public SettingLeftTreeItem addItem(SettingLeftTreeItemValue item) {
        return this.root().addItem(item);
    }

    @Override
    protected void initTreeView() {
        super.initTreeView();
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        this.setRoot(new SettingLeftTreeItem(this, null));
        this.setShowRoot(false);
        this.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue instanceof SettingLeftTreeItem item) {
                this.doSelect(item.getItemId());
            }
        });
        this.setId("left-tree-view");
    }

    /**
     * 按节点标识递归查找节点值
     *
     * @param itemId 节点标识
     * @return 匹配的节点值，未找到返回 null
     */
    protected SettingLeftTreeItemValue findItem(String itemId) {
        return this.root().findItem(itemId);
    }

    /**
     * 按节点标识选中节点
     *
     * @param itemId 节点标识
     */
    public void selectItem(String itemId) {
        SettingLeftTreeItemValue item = this.findItem(itemId);
        if (item != null) {
            this.doSelect(itemId);
        } else {
            JulLog.warn("item is null");
        }
    }

    /**
     * 执行节点选中，构建面包屑并更新右侧内容。
     *
     * @param itemId 节点标识
     */
    protected void doSelect(String itemId) {
        SettingMainPane mainPane = this.getSettingMainPane();
        if (mainPane != null && itemId != null) {
            SettingLeftTreeItemValue leftItem;
            List<SettingLeftTreeItemValue> items = new ArrayList<>();
            String fxId = itemId;
            do {
                leftItem = this.findItem(fxId);
                if (leftItem != null) {
                    items.add(leftItem);
                    fxId = leftItem.getParentId();
                } else {
                    break;
                }
            } while (true);
            items = items.reversed();
            StringBuilder label = new StringBuilder();
            for (SettingLeftTreeItemValue item : items) {
                label.append(" > ").append(item.getName());
            }
            mainPane.updateRightContent(itemId, label.substring(3));
        }
    }

    /**
     * 向上查找所属的设置主面板
     *
     * @return 设置主面板，未找到返回 null
     */
    public SettingMainPane getSettingMainPane() {
        Node node = this.parent();
        while (node != null) {
            if (node instanceof SettingMainPane pane) {
                return pane;
            }
            node = node.getParent();
        }
        return null;
    }
}
