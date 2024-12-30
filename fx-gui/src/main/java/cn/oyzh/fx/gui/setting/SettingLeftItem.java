package cn.oyzh.fx.gui.setting;

import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author oyzh
 * @since 2024/12/29
 */
@Getter
@Setter
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
}
