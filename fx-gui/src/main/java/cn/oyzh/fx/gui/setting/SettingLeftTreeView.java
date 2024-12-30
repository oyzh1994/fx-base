package cn.oyzh.fx.gui.setting;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

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

    public void selectItem(String itemId) {
        this.doSelect(itemId);
    }

    protected void doSelect(String fxId) {
        SettingMainPane mainPane = this.getSettingMainPane();
        if (mainPane != null && fxId != null) {
            NodeGroupUtil.disappear(mainPane, "setting_item");
            Node right = mainPane.getRight();
            if (right != null) {
                Node node = right.lookup("#" + fxId);
                if (node != null) {
                    node.setVisible(true);
                } else {
                    JulLog.warn("node:{} is null", fxId);
                }
            } else {
                JulLog.warn("right is null");
            }
        } else {
            JulLog.warn("mainPane or fxId:{} is null");
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
