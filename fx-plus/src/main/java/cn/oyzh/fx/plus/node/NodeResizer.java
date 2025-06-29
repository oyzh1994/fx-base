package cn.oyzh.fx.plus.node;

import cn.oyzh.common.log.JulLog;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

/**
 * 组件拉伸器
 *
 * @author oyzh
 * @since 2023/05/15
 */
public abstract class NodeResizer {

    /**
     * 事件节点
     */
    protected final Node eventNode;

    /**
     * 原始鼠标样式
     */
    protected final Cursor originalCursor;

    /**
     * 大小改变中标志位
     */
    protected Boolean resizeIng;

    /**
     * 鼠标按下时间
     */
    protected long mousePressedTime;

    /**
     * 触发阈值
     */
    protected Byte triggerThreshold = 3;

    /**
     * 鼠标移动事件
     */
    protected EventHandler<MouseEvent> mouseMoved;

    /**
     * 鼠标离开事件
     */
    protected EventHandler<MouseEvent> mouseExited;

    /**
     * 鼠标按下事件
     */
    protected EventHandler<MouseEvent> mousePressed;

    /**
     * 鼠标拖动事件
     */
    protected EventHandler<MouseEvent> mouseDragged;

    /**
     * 鼠标释放事件
     */
    protected EventHandler<MouseEvent> mouseReleased;

    /**
     * 鼠标拖动事件
     */
    protected final Consumer<Float> resizeTriggered;

    /**
     * 最小值
     */
    protected Float minValue;

    /**
     * 最大值
     */
    protected Float maxValue;

    public Node getEventNode() {
        return eventNode;
    }

    public Cursor getOriginalCursor() {
        return originalCursor;
    }

    public Byte getTriggerThreshold() {
        return triggerThreshold;
    }

    public void setTriggerThreshold(Byte triggerThreshold) {
        this.triggerThreshold = triggerThreshold;
    }

    public NodeResizer(Node eventNode, Cursor originalCursor, Consumer<Float> resizeTriggered) {
        this.eventNode = eventNode;
        this.originalCursor = originalCursor;
        this.resizeTriggered = resizeTriggered;
    }

    /**
     * 限制值
     *
     * @param minValue 最小值
     * @param maxValue 最大值
     */
    public void widthLimit(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
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
    public abstract EventHandler<MouseEvent> defaultMousePressed() ;

    /**
     * 获取默认鼠标移动事件
     *
     * @return 鼠标移动事件
     */
    public abstract EventHandler<MouseEvent> defaultMouseMoved() ;

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
                if (JulLog.isDebugEnabled()) {
                    JulLog.debug("Cursor recover.");
                }
                this.setNodeCursor(this.originalCursor);
                if (JulLog.isDebugEnabled()) {
                    JulLog.debug("MouseExited");
                }
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
    public abstract EventHandler<MouseEvent> defaultMouseDragged() ;

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
                if (JulLog.isDebugEnabled()) {
                    JulLog.debug("MouseReleased");
                }
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
        this.resizeIng = null;
        this.triggerThreshold = null;
    }

    /**
     * 获取最小值
     *
     * @return 最小值
     */
    public Float minValue() {
        return this.minValue == null ? 0.f : this.minValue;
    }

    /**
     * 获取最大值
     *
     * @return 最大值
     */
    public Float maxValue() {
        return this.maxValue == null ? 0.f : this.maxValue;
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
     * 设置鼠标样式
     *
     * @param cursor 鼠标样式
     */
    protected void setNodeCursor(Cursor cursor) {
        if (this.eventNode.getCursor() != cursor) {
            this.eventNode.setCursor(cursor);
        }
    }

    /**
     * 是否可拉伸
     *
     * @param event 鼠标事件
     * @return 结果
     */
    protected abstract boolean resizeAble(MouseEvent event) ;

}
