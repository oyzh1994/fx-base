package cn.oyzh.fx.plus.controls.popup;

import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.stage.Popup;

/**
 * 弹出框控件
 *
 * @author oyzh
 * @since 2023-12-22
 */
public class FXPopup extends Popup implements NodeAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 设置弹出框内容
     *
     * @param content 内容节点
     */
    public void content(Node content) {
        this.getContent().setAll(content);
    }

    /**
     * 获取弹出框内容
     *
     * @return 内容节点，无内容时返回 null
     */
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
     * 以固定偏移显示组件
     *
     * @param ownerNode 父节点
     * @param fixedX    水平方向固定偏移量
     * @param fixedY    垂直方向固定偏移量
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

    @Override
    public void hide() {
        FXUtil.runWait(super::hide);
    }

    @Override
    public void initNode() {
        this.setAutoFix(true);
        this.setAutoHide(true);
        this.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                NodeManager.init(this);
            }
        });
        NodeAdapter.super.initNode();
    }
}
