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
 * 组件高度拉伸器
 *
 * @author oyzh
 * @since 2025/03/22
 */
public class NodeHeightResizer extends NodeResizer {

    public NodeHeightResizer(Node eventNode, Cursor originalCursor, Consumer<Float> resizeTriggered) {
        super(eventNode, originalCursor, resizeTriggered);
    }

    @Override
    public EventHandler<MouseEvent> defaultMouseMoved() {
        return event -> {
            if (this.isResizeIng()) {
                return;
            }
            // 计算偏移
            float yOffset = this.calcYOffset(event);
            // 设置鼠标样式
            if (this.triggerAble(yOffset)) {
                this.setNodeCursor(Cursor.S_RESIZE);
            } else {
                this.setNodeCursor(this.originalCursor);
            }
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("MouseMoved");
            }
        };
    }

    @Override
    public EventHandler<MouseEvent> defaultMousePressed() {
        return event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                // 计算偏移
                float yOffset = this.calcYOffset(event);
                // 设置拉伸参数
                if (this.triggerAble(yOffset)) {
                    this.resizeIng = true;
                    this.mousePressedTime = System.currentTimeMillis();
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
                this.resizeTriggered.accept(this.calcNodeHeight(event));
            }
        };
    }

    /**
     * 计算y偏移
     *
     * @param event 事件
     * @return y偏移
     */
    public float calcYOffset(MouseEvent event) {
        // 计算距离
        Point2D point2D = this.eventNode.localToScreen(0, 0);
        double nodeY = point2D.getY();
        double screenY = event.getScreenY();
        return (float) (nodeY - screenY);
    }

    /**
     * 计算节点宽
     *
     * @param event 事件
     * @return 节点宽
     */
    public float calcNodeHeight(MouseEvent event) {
        // 计算距离
        Point2D point2D = this.eventNode.localToScreen(0, 0);
        double nodeY = point2D.getY();
        double screenY = event.getScreenY();
        double nodeH = NodeUtil.getHeight(this.eventNode);
        return (float) (nodeY - screenY + nodeH);
    }

    @Override
    public boolean resizeAble(MouseEvent event) {
        double nodeH = this.calcNodeHeight(event);
        return nodeH > this.minValue && nodeH < this.maxValue;
    }

    /**
     * 创建一个新实例
     *
     * @param eventNode       时间节点
     * @param cursor          鼠标样式
     * @param resizeTriggered 触发函数
     * @return NodeHeightResizer
     */
    public static NodeHeightResizer of(Node eventNode, Cursor cursor, Consumer<Float> resizeTriggered) {
        NodeHeightResizer resizer = new NodeHeightResizer(eventNode, cursor, resizeTriggered);
        resizer.initResizeEvent();
        return resizer;
    }

    /**
     * 创建一个新实例
     *
     * @param eventNode       时间节点
     * @param resizeTriggered 触发函数
     * @return NodeHeightResizer
     */
    public static NodeHeightResizer of(Node eventNode, Consumer<Float> resizeTriggered) {
        return of(eventNode, Cursor.DEFAULT, resizeTriggered);
    }

    /**
     * 创建一个新实例
     *
     * @param eventNode       时间节点
     * @param cursor          鼠标样式
     * @param resizeTriggered 触发函数
     * @param minHeight       最小高
     * @param maxHeight       最大高
     * @return NodeHeightResizer
     */
    public static NodeHeightResizer of(Node eventNode, Cursor cursor, Consumer<Float> resizeTriggered, float minHeight, float maxHeight) {
        NodeHeightResizer resizer = new NodeHeightResizer(eventNode, cursor, resizeTriggered);
        resizer.widthLimit(minHeight, maxHeight);
        resizer.initResizeEvent();
        return resizer;
    }

    /**
     * 创建一个新实例
     *
     * @param eventNode       时间节点
     * @param resizeTriggered 触发函数
     * @param minHeight       最小高
     * @param maxHeight       最大高
     * @return NodeHeightResizer
     */
    public static NodeHeightResizer of(Node eventNode, Consumer<Float> resizeTriggered, float minHeight, float maxHeight) {
        return of(eventNode, Cursor.DEFAULT, resizeTriggered, minHeight, maxHeight);
    }
}
