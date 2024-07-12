package cn.oyzh.fx.plus.controls.popup;

import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.ControlUtil;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.stage.Popup;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2023/12/22
 */
public class FXPopup extends Popup implements ThemeAdapter {

    {
        this.setAutoFix(true);
        this.setAutoHide(true);
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                NodeManager.init(this);
            }
        });
    }

    public void setContent(Node content) {
        this.getContent().setAll(content);
    }

    /**
     * 显示组件
     *
     * @param ownerNode 父节点
     */
    public void show(@NonNull Node ownerNode) {
        Point2D point2D = ownerNode.localToScreen(ownerNode.getScaleX(), ownerNode.getScaleY());
        double height = ControlUtil.boundedHeight(ownerNode);
        this.show(ownerNode, point2D.getX(), point2D.getY() + height);
    }

    /**
     * 显示组件
     *
     * @param ownerNode 父节点
     */
    public void showFixed(@NonNull Node ownerNode, double fixedX, double fixedY) {
        Point2D point2D = ownerNode.localToScreen(ownerNode.getScaleX(), ownerNode.getScaleY());
        double height = ControlUtil.boundedHeight(ownerNode);
        this.show(ownerNode, point2D.getX() + fixedX, point2D.getY() + height + fixedY);
    }
}
