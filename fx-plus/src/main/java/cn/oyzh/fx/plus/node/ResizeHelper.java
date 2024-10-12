package cn.oyzh.fx.plus.node;

import cn.oyzh.fx.common.log.JulLog;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

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
     * 触发节点
     */
    @Getter
    private final Node triggerNode;

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
     * 是否大小改变钟
     */
    @Setter
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
    @Getter
    @Setter
    private EventHandler<MouseEvent> mouseDragged;

    /**
     * 鼠标释放事件
     */
    @Setter
    private EventHandler<MouseEvent> mouseReleased;

    public ResizeHelper(@NonNull Node eventNode, @NonNull Node triggerNode, double minWidth, double maxWidth, @NonNull Cursor originalCursor) {
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.eventNode = eventNode;
        this.triggerNode = triggerNode;
        this.originalCursor = originalCursor;
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
            // 设置鼠标样式
            double xVal = NodeUtil.getWidth(this.eventNode) - event.getSceneX();
            System.out.println(xVal);
            if (this.triggerAble(xVal)) {
                this.setNodeCursor(Cursor.W_RESIZE);
            } else {
                this.setNodeCursor(this.originalCursor);
            }
            event.consume();
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
            this.setNodeCursor(this.originalCursor);
            JulLog.debug("Cursor recover.");
            event.consume();
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
            this.resizeIng(true);
            event.consume();
            JulLog.debug("MousePressed");
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
            this.resizeIng(null);
            this.setNodeCursor(this.originalCursor);
            event.consume();
            JulLog.debug("MouseReleased");
        };
    }

    /**
     * 初始化拉伸事件
     */
    public void initResizeEvent() {
        if (this.mouseMoved() != null) {
            this.eventNode.setOnMouseMoved(this.mouseMoved());
            this.triggerNode.setOnMouseMoved(this.mouseMoved());
        }
        if (this.mouseExited() != null) {
            this.eventNode.setOnMouseExited(this.mouseExited());
        }
        if (this.mousePressed() != null) {
            this.eventNode.setOnMousePressed(this.mousePressed());
        }
        if (this.mouseDragged() != null) {
            this.eventNode.setOnMouseDragged(this.mouseDragged());
        }
        if (this.mouseReleased() != null) {
            this.eventNode.setOnMouseReleased(this.mouseReleased());
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
        return this.triggerThreshold == null ? 0.d : this.triggerThreshold;
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

    /**
     * 是否可拉伸宽度
     *
     * @param event 鼠标事件
     * @return 结果
     */
    public boolean resizeWidthAble(MouseEvent event) {
        return this.resizeWidthAble(event.getSceneX());
    }

    /**
     * 是否可拉伸宽度
     *
     * @param val 当前值
     * @return 结果
     */
    public boolean resizeWidthAble(double val) {
        if (val <= this.minWidth()) {
            return false;
        }
        if (val >= this.maxWidth()) {
            return false;
        }
        double sceneW = NodeUtil.getWidth(this.eventNode.getScene());
        return val < sceneW - 50;
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
