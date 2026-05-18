package cn.oyzh.fx.plus.window;

import atlantafx.base.controls.Popover;
import cn.oyzh.common.object.ObjectWatcherManager;
import javafx.geometry.Point2D;
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
        // 初始化默认属性
        this.initDefault();
        // 监听显示属性
        this.popup().showingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.onWindowClosed();
            }
        });
        this.setProp(PopupManager.REF_ATTR, this);
        ObjectWatcherManager.watch(this);
    }

    public PopupExt(PopupAttribute attribute) {
        this.init(attribute);
        // 初始化默认属性
        this.initDefault();
        this.setProp(PopupManager.REF_ATTR, this);
        ObjectWatcherManager.watch(this);
    }

    /**
     * 初始化默认属性
     */
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
        Point2D pos = owner.localToScreen(0, 0);
        if (pos == null) {
            super.show(owner);
        } else {
            int offset = 4;
            super.show(owner, pos.getX() + offset, pos.getY() + offset);
        }
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
