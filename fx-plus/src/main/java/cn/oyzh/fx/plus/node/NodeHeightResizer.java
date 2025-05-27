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
 * 组件高度大小改变辅助
 *
 * @author oyzh
 * @since 2025/03/22
 */
public class NodeHeightResizer {

    /**
     * 事件节点
     */
    private final Node eventNode;

    /**
     * 最小高度
     */
    private Float minHeight;

    /**
     * 最大高度
     */
    private Float maxHeight;

    /**
     * 大小改变中标志位
     */
    private Boolean resizeIng;

    /**
     * 鼠标按下时间
     */
    private long mousePressedTime;

    /**
     * 原始鼠标样式
     */
    private Cursor originalCursor;

    /**
     * 触发阈值
     */
    private Byte triggerThreshold = 1;

    /**
     * 鼠标移动事件
     */
    private EventHandler<MouseEvent> mouseMoved;

    /**
     * 鼠标离开事件
     */
    private EventHandler<MouseEvent> mouseExited;

    /**
     * 鼠标按下事件
     */
    private EventHandler<MouseEvent> mousePressed;

    /**
     * 鼠标拖动事件
     */
    private EventHandler<MouseEvent> mouseDragged;

    /**
     * 鼠标释放事件
     */
    private EventHandler<MouseEvent> mouseReleased;

    /**
     * 鼠标拖动事件
     */
    private Consumer<Float> resizeTriggered;

    public Node getEventNode() {
        return eventNode;
    }

    public Float getMaxHeight() {
        return maxHeight;
    }

    public Float getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(Float minHeight) {
        this.minHeight = minHeight;
    }

    public Boolean getResizeIng() {
        return resizeIng;
    }

    public Byte getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(Byte triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public EventHandler<MouseEvent> getMouseMoved() {
        return mouseMoved;
    }

    public void setMouseMoved(EventHandler<MouseEvent> mouseMoved) {
        this.mouseMoved = mouseMoved;
    }

    public EventHandler<MouseEvent> getMouseExited() {
        return mouseExited;
    }

    public void setMouseExited(EventHandler<MouseEvent> mouseExited) {
        this.mouseExited = mouseExited;
    }

    public EventHandler<MouseEvent> getMousePressed() {
        return mousePressed;
    }

    public void setMousePressed(EventHandler<MouseEvent> mousePressed) {
        this.mousePressed = mousePressed;
    }

    public EventHandler<MouseEvent> getMouseDragged() {
        return mouseDragged;
    }

    public void setMouseDragged(EventHandler<MouseEvent> mouseDragged) {
        this.mouseDragged = mouseDragged;
    }

    public EventHandler<MouseEvent> getMouseReleased() {
        return mouseReleased;
    }

    public void setMouseReleased(EventHandler<MouseEvent> mouseReleased) {
        this.mouseReleased = mouseReleased;
    }

    public Consumer<Float> getResizeTriggered() {
        return resizeTriggered;
    }

    public void setResizeTriggered(Consumer<Float> resizeTriggered) {
        this.resizeTriggered = resizeTriggered;
    }

    public NodeHeightResizer(Node eventNode, Cursor originalCursor, Consumer<Float> resizeTriggered) {
        this.eventNode = eventNode;
        this.originalCursor = originalCursor;
        this.resizeTriggered = resizeTriggered;
    }

    /**
     * 限制高度
     *
     * @param minHeight 最小高
     * @param maxHeight 最大高
     */
    public void widthLimit(float minHeight, float maxHeight) {
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    /**
     * 获取鼠标移动事件
     *
     * @return 鼠标移动事件
     */
    public EventHandler<MouseEvent> mouseMoved() {
        if (this.mouseMoved == null) {
            this.mouseMoved = this.defaultMouseMoved();
        }
        return this.mouseMoved;
    }

    /**
     * 获取默认鼠标移动事件
     *
     * @return 鼠标移动事件
     */
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
            JulLog.debug("MouseMoved");
        };
    }

    /**
     * 获取鼠标离开事件
     *
     * @return 鼠标离开事件
     */
    public EventHandler<MouseEvent> mouseExited() {
        if (this.mouseExited == null) {
            this.mouseExited = this.defaultMouseExited();
        }
        return this.mouseExited;
    }

    /**
     * 获取默认鼠标离开事件
     *
     * @return 鼠标离开事件
     */
    public EventHandler<MouseEvent> defaultMouseExited() {
        return event -> {
            if (!this.isResizeIng()) {
                JulLog.debug("Cursor recover.");
                this.setNodeCursor(this.originalCursor);
                JulLog.debug("MouseExited");
            }
        };
    }

    /**
     * 获取鼠标按下事件
     *
     * @return 鼠标按下事件
     */
    public EventHandler<MouseEvent> mousePressed() {
        if (this.mousePressed == null) {
            this.mousePressed = this.defaultMousePressed();
        }
        return this.mousePressed;
    }

    /**
     * 获取默认鼠标按下事件
     *
     * @return 鼠标按下事件
     */
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
                JulLog.debug("MousePressed");
            } else {
                // 重置拉伸参数
                this.resizeIng = false;
                this.mousePressedTime = -1;
            }
        };
    }

    /**
     * 获取鼠标拖动事件
     *
     * @return 鼠标动事件
     */
    public EventHandler<MouseEvent> mouseDragged() {
        if (this.mouseDragged == null) {
            this.mouseDragged = this.defaultMouseDragged();
        }
        return this.mouseDragged;
    }

    /**
     * 获取默认鼠标拖动事件
     *
     * @return 鼠标拖动事件
     */
    public EventHandler<MouseEvent> defaultMouseDragged() {
        return event -> {
            if (event.getButton() == MouseButton.PRIMARY && this.isResizeIng() && this.resizeTriggered != null && this.resizeAble(event)) {
                this.resizeTriggered.accept(this.calcNodeHeight(event));
            }
        };
    }

    /**
     * 获取鼠标释放事件
     *
     * @return 鼠标释放事件
     */
    public EventHandler<MouseEvent> mouseReleased() {
        if (this.mouseReleased == null) {
            this.mouseReleased = this.defaultMouseReleased();
        }
        return this.mouseReleased;
    }

    /**
     * 获取默认鼠标释放事件
     *
     * @return 鼠标释放事件
     */
    public EventHandler<MouseEvent> defaultMouseReleased() {
        return event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                this.resizeIng = null;
                this.setNodeCursor(this.originalCursor);
                JulLog.debug("MouseReleased");
            }
        };
    }

    /**
     * 初始化拉伸事件
     */
    public void initResizeEvent() {
        if (this.mouseMoved() != null) {
            this.eventNode.addEventFilter(MouseEvent.MOUSE_MOVED, this.mouseMoved());
        }
        if (this.mouseExited() != null) {
            this.eventNode.addEventFilter(MouseEvent.MOUSE_EXITED, this.mouseExited());
        }
        if (this.mousePressed() != null) {
            this.eventNode.addEventFilter(MouseEvent.MOUSE_PRESSED, this.mousePressed());
        }
        if (this.mouseDragged() != null) {
            this.eventNode.addEventFilter(MouseEvent.MOUSE_DRAGGED, this.mouseDragged());
        }
        if (this.mouseReleased() != null) {
            this.eventNode.addEventFilter(MouseEvent.MOUSE_RELEASED, this.mouseReleased());
        }
    }

    /**
     * 清理数据
     */
    public void clear() {
        this.minHeight = null;
        this.resizeIng = null;
        this.originalCursor = null;
        this.triggerThreshold = null;
    }

    /**
     * 获取最小高度
     *
     * @return 最小高度
     */
    public Float minHeight() {
        return this.minHeight == null ? 0.f : this.minHeight;
    }

    /**
     * 获取最大高度
     *
     * @return 最大高度
     */
    public Float maxHeight() {
        return this.maxHeight == null ? 0.f : this.maxHeight;
    }

    /**
     * 获取触发阈值
     *
     * @return 触发阈值
     */
    public Float triggerThreshold() {
        return this.triggerThreshold == null ? 5.0f : this.triggerThreshold;
    }

    /**
     * 是否大小改变中
     *
     * @return 结果
     */
    public boolean isResizeIng() {
        return this.resizeIng != null && this.resizeIng && (System.currentTimeMillis() - this.mousePressedTime) > 150;
    }

    /**
     * 是否可触发
     *
     * @param val 值
     * @return 结果
     */
    public boolean triggerAble(float val) {
        return Math.abs(val) <= this.triggerThreshold();
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

    /**
     * 是否可拉伸
     *
     * @param event 鼠标事件
     * @return 结果
     */
    public boolean resizeAble(MouseEvent event) {
        double nodeH = this.calcNodeHeight(event);
        return nodeH > this.minHeight && nodeH < this.maxHeight;
    }

    /**
     * 设置鼠标样式
     *
     * @param cursor 鼠标样式
     */
    private void setNodeCursor(Cursor cursor) {
        if (this.eventNode.getCursor() != cursor) {
            this.eventNode.setCursor(cursor);
        }
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
