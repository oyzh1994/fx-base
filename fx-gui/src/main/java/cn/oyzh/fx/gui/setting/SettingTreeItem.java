package cn.oyzh.fx.gui.setting;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2024/12/29
 */
public class SettingTreeItem extends RichTreeItem<SettingLeftItem> {

    public SettingTreeItem(@NonNull RichTreeView treeView, SettingLeftItem value) {
        super(treeView);
        this.setValue(value);
    }

    public SettingTreeItem addItem(SettingLeftItem item) {
        SettingTreeItem treeItem = new SettingTreeItem(this.getTreeView(), item);
        this.addChild(treeItem);
        return treeItem;
    }

    public String getItemId() {
        SettingLeftItem value = this.getValue();
        return value == null ? null : value.getId();
    }
}
