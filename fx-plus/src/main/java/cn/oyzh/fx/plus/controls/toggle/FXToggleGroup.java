package cn.oyzh.fx.plus.controls.toggle;

import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

/**
 * 单选框组控件
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class FXToggleGroup extends ToggleGroup {

    /**
     * 获取选中节点的用户数据
     *
     * @param <T> 用户数据类型
     * @return 选中节点的用户数据
     */
    public <T> T selectedUserData() {
        if (this.getSelectedToggle() != null) {
            return (T) this.getSelectedToggle().getUserData();
        }
        return null;
    }

    /**
     * 获取选中节点
     *
     * @param <T> 节点类型
     * @return 选中节点
     */
    public <T extends RadioButton> T selectedToggle() {
        return (T) this.getSelectedToggle();
    }
}
