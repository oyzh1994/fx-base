package cn.oyzh.fx.plus.node;

import cn.oyzh.common.log.JulLog;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

/**
 * 组件宽度拉伸器
 *
 * @author oyzh
 * @since 2023/05/15
 */
public class NodeWidthResizer extends NodeResizer {

    public NodeWidthResizer(Node eventNode, Cursor originalCursor, Consumer<Float> resizeTriggered) {
        super(eventNode, originalCursor, resizeTriggered);
    }

    @Override
    public EventHandler<MouseEvent> defaultMouseMoved() {
        return event -> {
            if (this.isResizeIng()) {
                event.consume();
                return;
            }
            // 计算偏移
            float xOffset = this.calcXOffset(event);
            // 设置鼠标样式
            if (this.triggerAble(xOffset)) {
                this.setNodeCursor(Cursor.W_RESIZE);
            } else {
                this.setNodeCursor(this.originalCursor);
            }
            if(JulLog.isDebugEnabled()) {
                JulLog.debug("MouseMoved");
            }
        };
    }

    @Override
    public EventHandler<MouseEvent> defaultMousePressed() {
        return event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                // 计算偏移
                float xOffset = this.calcXOffset(event);
                // 设置拉伸参数
                if (this.triggerAble(xOffset)) {
                    this.resizeIng = true;
                    this.mousePressedTime = System.currentTimeMillis();
                    event.consume();
                } else {// 重置拉伸参数
                    this.resizeIng = false;
                    this.mousePressedTime = -1;
                }
                if (JulLog.isDebugEnabled()) {
                    JulLog.debug("MousePressed");
                }
            } else {
                // 重置拉伸参数
                this.resizeIng = false;
                this.mousePressedTime = -1;
            }
        };
    }

    @Override
    public EventHandler<MouseEvent> defaultMouseDragged() {
        return event -> {
            if (event.getButton() == MouseButton.PRIMARY && this.isResizeIng() && this.resizeTriggered != null && this.resizeAble(event)) {
                this.resizeTriggered.accept(this.calcNodeWidth(event));
                event.consume();
            }
        };
    }

    /**
     * 计算x偏移
     *
     * @param event 事件
     * @return x偏移
     */
    public float calcXOffset(MouseEvent event) {
        // 计算距离
        Point2D point2D = this.eventNode.localToScreen(this.eventNode.getLayoutX(), this.eventNode.getLayoutY());
        double nodeX = point2D.getX();
        double nodeW = NodeUtil.getWidth(this.eventNode);
        double screenX = event.getScreenX();
        return (float) (screenX - nodeX - nodeW);
    }

    /**
     * 计算节点宽
     *
     * @param event 事件
     * @return 节点宽
     */
    public float calcNodeWidth(MouseEvent event) {
        // 计算距离
        Point2D point2D = this.eventNode.localToScreen(this.eventNode.getLayoutX(), this.eventNode.getLayoutY());
        double nodeX = point2D.getX();
        double screenX = event.getScreenX();
        return (float) (screenX - nodeX);
    }

    @Override
    public boolean resizeAble(MouseEvent event) {
        double nodeW = this.calcNodeWidth(event);
        return nodeW > this.minValue && nodeW < this.maxValue;
    }

    /**
     * 创建一个新实例
     *
     * @param eventNode       时间节点
     * @param cursor          鼠标样式
     * @param resizeTriggered 触发函数
     * @return NodeWidthResizer
     */
    public static NodeWidthResizer of(Node eventNode, Cursor cursor, Consumer<Float> resizeTriggered) {
        NodeWidthResizer resizer = new NodeWidthResizer(eventNode, cursor, resizeTriggered);
        resizer.initResizeEvent();
        return resizer;
    }

    /**
     * 创建一个新实例
     *
     * @param eventNode       时间节点
     * @param resizeTriggered 触发函数
     * @return NodeWidthResizer
     */
    public static NodeWidthResizer of(Node eventNode, Consumer<Float> resizeTriggered) {
        return of(eventNode, Cursor.DEFAULT, resizeTriggered);
    }

    /**
     * 创建一个新实例
     *
     * @param eventNode       时间节点
     * @param cursor          鼠标样式
     * @param resizeTriggered 触发函数
     * @param minWidth        最小宽
     * @param maxWidth        最大宽
     * @return NodeWidthResizer
     */
    public static NodeWidthResizer of(Node eventNode, Cursor cursor, Consumer<Float> resizeTriggered, float minWidth, float maxWidth) {
        NodeWidthResizer resizer = new NodeWidthResizer(eventNode, cursor, resizeTriggered);
        resizer.widthLimit(minWidth, maxWidth);
        resizer.initResizeEvent();
        return resizer;
    }

    /**
     * 创建一个新实例
     *
     * @param eventNode       时间节点
     * @param resizeTriggered 触发函数
     * @param minWidth        最小宽
     * @param maxWidth        最大宽
     * @return NodeWidthResizer
     */
    public static NodeWidthResizer of(Node eventNode, Consumer<Float> resizeTriggered, float minWidth, float maxWidth) {
        return of(eventNode, Cursor.DEFAULT, resizeTriggered, minWidth, maxWidth);
    }
}
