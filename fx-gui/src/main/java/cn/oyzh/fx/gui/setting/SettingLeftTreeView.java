package cn.oyzh.fx.gui.setting;

import cn.oyzh.fx.gui.tree.view.RichTreeCell;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
        this.selectedIndexChanged((observable, oldValue, newValue) -> {
            SettingTreeItem item = (SettingTreeItem) this.getSelectedItem();
            SettingMainPane mainPane = this.getSettingMainPane();
            if (mainPane != null && item != null && item.getItemId() != null) {
                NodeGroupUtil.disappear(mainPane, "setting_item");
                Node right = mainPane.getRight();
                if (right != null) {
                    Node node = right.lookup("#" + item.getItemId());
                    if (node != null) {
                        node.setVisible(true);
                    }
                }
            }
        });
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
