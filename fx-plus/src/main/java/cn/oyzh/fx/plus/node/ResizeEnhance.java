package cn.oyzh.fx.plus.node;

import cn.hutool.log.StaticLog;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 组件大小改变增强
 *
 * @author oyzh
 * @since 2023/5/15
 */
@Accessors(chain = true, fluent = true)
public class ResizeEnhance {

    /**
     * 拉伸节点
     */
    @Getter
    private final Node node;

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
    private Double triggerThreshold;

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

    public ResizeEnhance(@NonNull Node node) {
        this.node = node;
        this.originalCursor = node.getCursor();
    }

    public ResizeEnhance(@NonNull Node node, @NonNull Cursor originalCursor) {
        this.node = node;
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
            double xVal = this.node.minWidth(-1) - event.getSceneX();
            if (this.triggerAble(xVal)) {
                this.setNodeCursor(Cursor.W_RESIZE);
            } else {
                this.setNodeCursor(this.originalCursor);
            }
            event.consume();
//            if (log.isDebugEnabled()) {
//                StaticLog.debug("MouseMoved");
//            }
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
//            if (log.isDebugEnabled()) {
            StaticLog.debug("Cursor recover.");
//            }
            event.consume();
//            if (log.isDebugEnabled()) {
            StaticLog.debug("MouseExited");
//            }
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
//            if (log.isDebugEnabled()) {
            StaticLog.debug("MousePressed");
//            }
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
//            if (log.isDebugEnabled()) {
            StaticLog.debug("MouseReleased");
//            }
        };
    }

    /**
     * 初始化拉伸事件
     */
    public void initResizeEvent() {
        if (this.mouseMoved() != null) {
            this.node.setOnMouseMoved(this.mouseMoved());
        }
        if (this.mouseExited() != null) {
            this.node.setOnMouseExited(this.mouseExited());
        }
        if (this.mousePressed() != null) {
            this.node.setOnMousePressed(this.mousePressed());
        }
        if (this.mouseDragged() != null) {
            this.node.setOnMouseDragged(this.mouseDragged());
        }
        if (this.mouseReleased() != null) {
            this.node.setOnMouseReleased(this.mouseReleased());
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
        double sceneW = NodeUtil.getWidth(this.node.getScene());
        return val < sceneW - 50;
    }

    /**
     * 设置鼠标样式
     *
     * @param cursor 鼠标样式
     */
    private void setNodeCursor(Cursor cursor) {
        if (this.node.getCursor() != cursor) {
            this.node.setCursor(cursor);
        }
    }
}
