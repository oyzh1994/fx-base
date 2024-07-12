package cn.oyzh.fx.plus.window;

import atlantafx.base.controls.Popover;
import javafx.scene.Node;
import javafx.stage.PopupWindow;

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
    public void setContent(Node content) {
        super.setContentNode(content);
    }
}
