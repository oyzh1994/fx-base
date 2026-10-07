package cn.oyzh.fx.plus.mouse;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;


/**
 * 鼠标按键处理器
 *
 * @author oyzh
 * @since 2023-10-10
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

    /**
     * 触发事件处理器
     *
     * @param event 鼠标事件
     */
    public void handle(MouseEvent event) {
        if (this.handler != null) {
            this.handler.handle(event);
        }
    }

    /**
     * 获取按钮。
     *
     * @return 按钮
     */
    public MouseButton getButton() {
        return button;
    }

    /**
     * 设置按钮。
     *
     * @param button 按钮
     */
    public void setButton(MouseButton button) {
        this.button = button;
    }

    /**
     * 获取类型。
     *
     * @return 类型
     */
    public EventType<MouseEvent> getType() {
        return type;
    }

    /**
     * 设置类型。
     *
     * @param type 类型
     */
    public void setType(EventType<MouseEvent> type) {
        this.type = type;
    }

    /**
     * 获取处理器。
     *
     * @return 处理器
     */
    public EventHandler<? super MouseEvent> getHandler() {
        return handler;
    }

    /**
     * 设置处理器。
     *
     * @param handler 处理器
     */
    public void setHandler(EventHandler<? super MouseEvent> handler) {
        this.handler = handler;
    }

    /**
     * 获取点击数量。
     *
     * @return 点击数量
     */
    public Integer getClickCount() {
        return clickCount;
    }

    /**
     * 设置点击数量。
     *
     * @param clickCount 点击数量
     */
    public void setClickCount(Integer clickCount) {
        this.clickCount = clickCount;
    }

    /**
     * 是否Alt按下。
     *
     * @return Alt按下
     */
    public boolean isAltDown() {
        return altDown;
    }

    /**
     * 设置Alt按下。
     *
     * @param altDown Alt是否按下
     */
    public void setAltDown(boolean altDown) {
        this.altDown = altDown;
    }

    /**
     * 是否Meta按下。
     *
     * @return Meta按下
     */
    public boolean isMetaDown() {
        return metaDown;
    }

    /**
     * 设置Meta按下。
     *
     * @param metaDown Meta是否按下
     */
    public void setMetaDown(boolean metaDown) {
        this.metaDown = metaDown;
    }

    /**
     * 是否Shift按下。
     *
     * @return Shift按下
     */
    public boolean isShiftDown() {
        return shiftDown;
    }

    /**
     * 设置Shift按下。
     *
     * @param shiftDown Shift是否按下
     */
    public void setShiftDown(boolean shiftDown) {
        this.shiftDown = shiftDown;
    }

    /**
     * 是否控件按下。
     *
     * @return 控件按下
     */
    public boolean isControlDown() {
        return controlDown;
    }

    /**
     * 设置控件按下。
     *
     * @param controlDown Control是否按下
     */
    public void setControlDown(boolean controlDown) {
        this.controlDown = controlDown;
    }
}
