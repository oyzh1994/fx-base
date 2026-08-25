package cn.oyzh.fx.gui.setting;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import javafx.scene.control.TreeItem;

/**
 * @author oyzh
 * @since 2024/12/29
 */
public class SettingLeftTreeItem extends RichTreeItem<SettingLeftTreeItemValue> {

    public SettingLeftTreeItem(SettingLeftTreeView treeView, SettingLeftTreeItemValue value) {
        super(treeView);
        this.setValue(value);
    }

    @Override
    public SettingLeftTreeView getTreeView() {
        return (SettingLeftTreeView) super.getTreeView();
    }

    public SettingLeftTreeItem addItem(SettingLeftTreeItemValue item) {
        item.setParentId(this.getItemId());
        SettingLeftTreeItem treeItem = new SettingLeftTreeItem(this.getTreeView(), item);
        this.addChild(treeItem);
        return treeItem;
    }

    public SettingLeftTreeItemValue findItem(String itemId) {
        if (StringUtil.isNotBlank(itemId)) {
            for (TreeItem<?> item : this.unfilteredChildren()) {
                if (item instanceof SettingLeftTreeItem treeItem) {
                    if (itemId.equals(treeItem.getItemId())) {
                        return treeItem.getValue();
                    }
                    SettingLeftTreeItemValue leftItem = treeItem.findItem(itemId);
                    if (leftItem != null) {
                        return leftItem;
                    }
                }
            }
        }
        return null;
    }

    public String getItemId() {
        SettingLeftTreeItemValue value = this.getValue();
        return value == null ? null : value.getId();
    }
}
