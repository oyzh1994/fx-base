package cn.oyzh.fx.plus.controls.popup;

import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.stage.Popup;

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

    public void content(Node content) {
        this.getContent().setAll(content);
    }

    public Node content() {
        if (this.getContent().isEmpty()) {
            return null;
        }
        return this.getContent().getFirst();
    }

    /**
     * 显示组件
     *
     * @param ownerNode 父节点
     */
    public void show(Node ownerNode) {
        Point2D point2D = ownerNode.localToScreen(ownerNode.getScaleX(), ownerNode.getScaleY());
        double height = NodeUtil.getHeight(ownerNode);
        // double height = ControlUtil.boundedHeight(ownerNode);
        this.show(ownerNode, point2D.getX(), point2D.getY() + height);
    }

    /**
     * 显示组件
     *
     * @param ownerNode 父节点
     */
    public void showFixed(Node ownerNode, double fixedX, double fixedY) {
        Point2D point2D = ownerNode.localToScreen(ownerNode.getScaleX(), ownerNode.getScaleY());
        double height = NodeUtil.getHeight(ownerNode);
        // double height = ControlUtil.boundedHeight(ownerNode);
        if (NodeUtil.isOrientationRightToLeft(ownerNode)) {
            double width = NodeUtil.getWidth(ownerNode);
            // double width = ControlUtil.boundedWidth(ownerNode);
            FXUtil.runLater(() -> this.show(ownerNode, point2D.getX() - width - fixedX, point2D.getY() + height + fixedY));
        } else {
            FXUtil.runLater(() -> this.show(ownerNode, point2D.getX() + fixedX, point2D.getY() + height + fixedY));
        }
    }
}
