package cn.oyzh.fx.gui.setting;

import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import lombok.NonNull;

public class SettingTreeItem  extends RichTreeItem<SettingLeftItem> {

    public SettingTreeItem(@NonNull RichTreeView treeView,SettingLeftItem value) {
        super(treeView);
        this.setValue(value);
    }

    public SettingTreeItem addItem(SettingLeftItem item) {
        SettingTreeItem treeItem = new SettingTreeItem(this.getTreeView(), item);
        this.addChild(treeItem);
        return treeItem;
    }

    public String getItemId(){
       return this.getValue().getId();
    }

}
