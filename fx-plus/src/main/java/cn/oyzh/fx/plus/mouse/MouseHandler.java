package cn.oyzh.fx.plus.mouse;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;


/**
 * 鼠标按键处理器
 *
 * @author oyzh
 * @since 2023/10/10
 */
public class MouseHandler {

    /**
     * 鼠标按钮
     */
    private MouseButton button;

    /**
     * 按键类型
     */
    private EventType<MouseEvent> type;

    /**
     * 事件处理器
     */
    private EventHandler<? super MouseEvent> handler;

    /**
     * 点击次数
     */
    private Integer clickCount;

    /**
     * 是否alt按下
     */
    private boolean altDown;

    /**
     * 是否 meta按下
     */
    private boolean metaDown;

    /**
     * 是否shift按下
     */
    private boolean shiftDown;

    /**
     * 是否control按下
     */
    private boolean controlDown;

    public void handle(MouseEvent event) {
        if (this.handler != null) {
            this.handler.handle(event);
        }
    }

    public MouseButton getButton() {
        return button;
    }

    public void setButton(MouseButton button) {
        this.button = button;
    }

    public EventType<MouseEvent> getType() {
        return type;
    }

    public void setType(EventType<MouseEvent> type) {
        this.type = type;
    }

    public EventHandler<? super MouseEvent> getHandler() {
        return handler;
    }

    public void setHandler(EventHandler<? super MouseEvent> handler) {
        this.handler = handler;
    }

    public Integer getClickCount() {
        return clickCount;
    }

    public void setClickCount(Integer clickCount) {
        this.clickCount = clickCount;
    }

    public boolean isAltDown() {
        return altDown;
    }

    public void setAltDown(boolean altDown) {
        this.altDown = altDown;
    }

    public boolean isMetaDown() {
        return metaDown;
    }

    public void setMetaDown(boolean metaDown) {
        this.metaDown = metaDown;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }

    public void setShiftDown(boolean shiftDown) {
        this.shiftDown = shiftDown;
    }

    public boolean isControlDown() {
        return controlDown;
    }

    public void setControlDown(boolean controlDown) {
        this.controlDown = controlDown;
    }
}
