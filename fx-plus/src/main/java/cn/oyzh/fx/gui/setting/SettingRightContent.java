package cn.oyzh.fx.gui.setting;

import cn.oyzh.fx.plus.controls.box.FXVBox;
import javafx.scene.Node;

/**
 * 设置右侧内容容器
 *
 * @author oyzh
 * @since 2024/12/29
 */
public class SettingRightContent extends FXVBox {

    /**
     * 构造右侧内容容器
     */
    public SettingRightContent() {
    }

    /**
     * 构造右侧内容容器
     *
     * @param children 子节点
     */
    public SettingRightContent(Node... children) {
        super(children);
    }
}
