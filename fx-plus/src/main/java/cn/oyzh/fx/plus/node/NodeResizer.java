package cn.oyzh.fx.plus.node;

import cn.oyzh.common.log.JulLog;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

/**
 * 组件大小改变辅助
 *
 * @author oyzh
 * @since 2023/05/15
 */
@Accessors(chain = true, fluent = true)
public class NodeResizer {

    /**
     * 事件节点
     */
    @Getter
    private final Node eventNode;

    /**
     * 最小宽度
     */
    @Setter
    private Float minWidth;

    /**
     * 最大宽度
     */
    @Setter
    private Float maxWidth;

    /**
     * 大小改变中标志位
     */
    @Getter
    private Boolean resizeIng;

    /**
     * 鼠标按下时间
     */
    private long mousePressedTime;

    /**
     * 原始鼠标样式
     */
    @Setter
    @Getter
    private Cursor originalCursor;

    /**
     * 触发阈值
     */
    @Setter
    private Byte triggerThreshold = 1;

    /**
     * 鼠标移动事件
     */
    @Setter
    private EventHandler<MouseEvent> mouseMoved;

    /**
     * 鼠标离开事件
     */
    @Setter
    private EventHandler<MouseEvent> mouseExited;

    /**
     * 鼠标按下事件
     */
    @Setter
    private EventHandler<MouseEvent> mousePressed;

    /**
     * 鼠标拖动事件
     */
    @Setter
    private EventHandler<MouseEvent> mouseDragged;

    /**
     * 鼠标释放事件
     */
    @Setter
    private EventHandler<MouseEvent> mouseReleased;

    /**
     * 鼠标拖动事件
     */
    @Getter
    @Setter
    private Consumer<Float> resizeTriggered;

    public NodeResizer(@NonNull Node eventNode, @NonNull Cursor originalCursor, Consumer<Float> resizeTriggered) {
        this.eventNode = eventNode;
        this.originalCursor = originalCursor;
        this.resizeTriggered = resizeTriggered;
    }

    /**
     * 限制宽度
     *
     * @param minWidth 最小宽
     * @param maxWidth 最大宽
     */
    public void widthLimit(float minWidth, float maxWidth) {
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
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
            float xOffset = this.calcXOffset(event);
            // 设置鼠标样式
            if (this.triggerAble(xOffset)) {
                this.setNodeCursor(Cursor.W_RESIZE);
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
                float xOffset = this.calcXOffset(event);
                // 设置拉伸参数
                if (this.triggerAble(xOffset)) {
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
                this.resizeTriggered.accept(this.calcNodeWidth(event));
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
        this.minWidth = null;
        this.resizeIng = null;
        this.originalCursor = null;
        this.triggerThreshold = null;
    }

    /**
     * 获取最小宽度
     *
     * @return 最小宽度
     */
    public Float minWidth() {
        return this.minWidth == null ? 0.f : this.minWidth;
    }

    /**
     * 获取最大宽度
     *
     * @return 最大宽度
     */
    public Float maxWidth() {
        return this.maxWidth == null ? 0.f : this.maxWidth;
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

    /**
     * 是否可拉伸
     *
     * @param event 鼠标事件
     * @return 结果
     */
    public boolean resizeAble(MouseEvent event) {
        double nodeW = this.calcNodeWidth(event);
        return nodeW > this.minWidth && nodeW < this.maxWidth;
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
}
