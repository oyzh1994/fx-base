package cn.oyzh.fx.plus.window;

import atlantafx.base.controls.Popover;
import javafx.scene.Node;
import javafx.stage.PopupWindow;

/**
 * 弹窗扩展
 *
 * @author oyzh
 * @since 2024/07/12
 */
public class PopupExt extends Popover implements PopupWrapper {

    public PopupExt(PopupAttribute attribute) {
        this.init(attribute);
        this.setProp(PopupManager.REF_ATTR, this);
    }

    @Override
    public PopupWindow popup() {
        return this;
    }

    @Override
    public void showPopup(Node owner) {
        super.show(owner);
    }

    @Override
    public Node content() {
        return super.getContentNode();
    }

    @Override
    public void content(Node content) {
        super.setContentNode(content);
    }
}
