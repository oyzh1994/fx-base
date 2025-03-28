package cn.oyzh.fx.gui.setting;

import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * @author oyzh
 * @since 2024/12/29
 */
public class SettingLeftItem extends RichTreeItemValue {

    private String id;

    private String name;

    private String parentId;

    public SettingLeftItem(String name ) {
        this.name = name;
    }

    public SettingLeftItem(String name, String id) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    public static SettingLeftItem of(String name, String id){
        return new SettingLeftItem(name, id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
