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

    protected SettingLeftTreeItemValue findItem(String itemId) {
        return this.root().findItem(itemId);
    }

    public void selectItem(String itemId) {
        SettingLeftTreeItemValue item = this.findItem(itemId);
        if (item != null) {
            this.doSelect(itemId);
        } else {
            JulLog.warn("item is null");
        }
    }

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
