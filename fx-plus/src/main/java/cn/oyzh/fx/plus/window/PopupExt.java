package cn.oyzh.fx.plus.window;

import atlantafx.base.controls.Popover;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.stage.PopupWindow;
import javafx.util.Duration;
import lombok.NonNull;

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
    public void showPopup(@NonNull Node owner) {
        if (owner instanceof SVGGlyph glyph) {
            Bounds bounds = owner.localToScreen(owner.getBoundsInLocal());
            if (bounds == null) {
                throw new IllegalStateException(
                        "The owner node is not added to the scene. It cannot be used as a popover anchor."
                );
            }
            Bounds boundsInParent = owner.getBoundsInParent();
            int offset = 4;
            int xOffset = 40;
            switch (getArrowLocation()) {
                case BOTTOM_LEFT -> show(
                        owner, bounds.getMinX() + 8, bounds.getMinY() + offset
                );
            }
        } else {
            super.show(owner);
        }
    }

    @Override
    public void showPopup(@NonNull Node owner, double x, double y) {
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
