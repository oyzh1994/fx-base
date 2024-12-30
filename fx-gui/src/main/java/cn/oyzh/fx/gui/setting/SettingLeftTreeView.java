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
    public SettingTreeItem getRoot() {
        return (SettingTreeItem) super.getRoot();
    }

    public SettingTreeItem addItem(SettingLeftItem item) {
        return this.getRoot().addItem(item);
    }

    @Override
    protected void initTreeView() {
        super.initTreeView();
        this.setCellFactory((Callback<TreeView<?>, TreeCell<?>>) param -> new RichTreeCell<>());
        this.setRoot(new SettingTreeItem(this, null));
        this.setShowRoot(false);
        this.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue instanceof SettingTreeItem item) {
                this.doSelect(item.getItemId());
            }
        });
        this.setId("left-tree-view");
    }

    protected SettingLeftItem findItem(String itemId) {
        return this.getRoot().findItem(itemId);
    }

    public void selectItem(String itemId) {
        SettingLeftItem item = this.findItem(itemId);
        if (item != null) {
            this.doSelect(itemId);
        } else {
            JulLog.warn("item is null");
        }
    }

    protected void doSelect(String itemId) {
        SettingMainPane mainPane = this.getSettingMainPane();
        if (mainPane != null && itemId != null) {
            SettingLeftItem leftItem;
            List<SettingLeftItem> items = new ArrayList<>();
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
            for (SettingLeftItem item : items) {
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
