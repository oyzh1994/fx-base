package cn.oyzh.fx.plus.controls.toggle;

import javafx.scene.control.ToggleGroup;

/**
 * @author oyzh
 * @since 2023/1/17
 */
public class FXToggleGroup extends ToggleGroup {

    /**
     * 获取选中节点的用户数据
     *
     * @param <T> 泛型
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
     * @param <T> 泛型
     * @return 选中节点
     */
    public <T> T selectedToggle() {
        return (T) this.getSelectedToggle();
    }
}
