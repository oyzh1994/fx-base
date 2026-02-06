package cn.oyzh.fx.gui.setting;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import javafx.scene.control.TreeItem;

/**
 * @author oyzh
 * @since 2024/12/29
 */
public class SettingTreeItem extends RichTreeItem<SettingLeftItem> {

    public SettingTreeItem( RichTreeView treeView, SettingLeftItem value) {
        super(treeView);
        this.setValue(value);
    }

    public SettingTreeItem addItem(SettingLeftItem item) {
        item.setParentId(this.getItemId());
        SettingTreeItem treeItem = new SettingTreeItem(this.getTreeView(), item);
        this.addChild(treeItem);
        return treeItem;
    }

    public SettingLeftItem findItem(String itemId) {
        if (StringUtil.isNotBlank(itemId)) {
            for (TreeItem<?> item : this.unfilteredChildren()) {
                if (item instanceof SettingTreeItem treeItem) {
                    if (itemId.equals(treeItem.getItemId())) {
                        return treeItem.getValue();
                    }
                    SettingLeftItem leftItem = treeItem.findItem(itemId);
                    if (leftItem != null) {
                        return leftItem;
                    }
                }
            }
        }
        return null;
    }

    public String getItemId() {
        SettingLeftItem value = this.getValue();
        return value == null ? null : value.getId();
    }
}
