package cn.oyzh.fx.plus.node;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Consumer;

/**
 * 组件大小改变增强
 *
 * @author oyzh
 * @since 2023/5/15
 */
@Accessors(chain = true, fluent = true)
public class ResizeHelper {

    /**
     * 事件节点
     */
    @Getter
    private final Node eventNode;

    /**
     * 最小宽度
     */
    @Setter
    private Double minWidth;

    /**
     * 最大宽度
     */
    @Setter
    private Double maxWidth;

    /**
     * 大小改变中标志位
     */
    @Getter
    private Boolean resizeIng;

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
    private Double triggerThreshold = 10.d;

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
    private Consumer<Double> resizeTriggered;

    public ResizeHelper(@NonNull Node eventNode, @NonNull Cursor originalCursor, Consumer<Double> resizeTriggered) {
        this.eventNode = eventNode;
        this.originalCursor = originalCursor;
        this.resizeTriggered = resizeTriggered;
    }

    public void widthLimit(double minWidth, double maxWidth) {
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
            double xOffset = this.calcXOffset(event);
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
            if (this.isResizeIng()) {
                return;
            }
            JulLog.debug("Cursor recover.");
            this.setNodeCursor(this.originalCursor);
            JulLog.debug("MouseExited");
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
            // 设置拉伸中标志位
            this.resizeIng = true;
            JulLog.debug("MousePressed");
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
            if (this.resizeTriggered != null && this.resizeAble(event)) {
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
            this.resizeIng = null;
            this.setNodeCursor(this.originalCursor);
            JulLog.debug("MouseReleased");
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
    public Double minWidth() {
        return this.minWidth == null ? 0.d : this.minWidth;
    }

    /**
     * 获取最大宽度
     *
     * @return 最大宽度
     */
    public Double maxWidth() {
        return this.maxWidth == null ? 0.d : this.maxWidth;
    }

    /**
     * 获取触发阈值
     *
     * @return 触发阈值
     */
    public Double triggerThreshold() {
        return this.triggerThreshold == null ? 10.d : this.triggerThreshold;
    }

    /**
     * 是否大小改变中
     *
     * @return 结果
     */
    public boolean isResizeIng() {
        return this.resizeIng != null && this.resizeIng;
    }

    /**
     * 是否可触发
     *
     * @param val 值
     * @return 结果
     */
    public boolean triggerAble(double val) {
        return Math.abs(val) <= this.triggerThreshold();
    }

    public double calcXOffset(MouseEvent event) {
        // 计算距离
        Point2D point2D = this.eventNode.localToScreen(this.eventNode.getLayoutX(), this.eventNode.getLayoutY());
        double nodeX = point2D.getX();
        double nodeW = NodeUtil.getWidth(this.eventNode);
        double screenX = event.getScreenX();
        return screenX - nodeX - nodeW;
    }

    public double calcNodeWidth(MouseEvent event) {
        // 计算距离
        Point2D point2D = this.eventNode.localToScreen(this.eventNode.getLayoutX(), this.eventNode.getLayoutY());
        double nodeX = point2D.getX();
        double screenX = event.getScreenX();
        return screenX - nodeX;
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
