package cn.oyzh.fx.plus.window;

import atlantafx.base.controls.Popover;
import javafx.scene.Node;
import javafx.stage.PopupWindow;
import javafx.util.Duration;

/**
 * 弹窗扩展
 *
 * @author oyzh
 * @since 2024/07/12
 */
public class PopupExt extends Popover implements PopupAdapter {

    public PopupExt() {
        this.initDefault();
        // 监听显示属性
        this.popup().showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.onWindowClosed();
            }
        });
        this.setProp(PopupManager.REF_ATTR, this);
    }

    public PopupExt(PopupAttribute attribute) {
        this.init(attribute);
        this.initDefault();
        this.setProp(PopupManager.REF_ATTR, this);
    }

    protected void initDefault() {
        this.setAutoFix(true);
        this.setAnimated(true);
        this.setAutoHide(true);
        this.setHideOnEscape(true);
        this.setFadeInDuration(Duration.millis(350));
        this.setFadeOutDuration(Duration.millis(350));
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
    public void showPopup(Node owner, double x, double y) {
        super.show(owner, x, y);
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
