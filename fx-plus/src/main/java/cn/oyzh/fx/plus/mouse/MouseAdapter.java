package cn.oyzh.fx.plus.mouse;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * 鼠标事件
 *
 * @author oyzh
 * @since 2022/5/12
 */
public interface MouseAdapter extends PropAdapter {

    /**
     * 获取鼠标事件处理器
     *
     * @param initIfNull 如果为空，是否初始化
     * @return 鼠标事件处理器
     */
    private MouseEventHandler getOnMouseEventHandler(boolean initIfNull) {
        if (this instanceof Node node) {
            MouseEventHandler handler = null;
            if (node.getOnMouseClicked() instanceof MouseEventHandler) {
                handler = (MouseEventHandler) node.getOnMouseClicked();
            }
            if (handler == null && initIfNull) {
                handler = new MouseEventHandler();
                node.setOnMouseClicked(handler);
            }
            return handler;
        } else {
            throw new UnsupportedOperationException("must be node!");
        }
    }

    /**
     * 获取鼠标主按钮点击事件
     *
     * @return 事件处理器
     */
    default EventHandler<? super MouseEvent> getOnMousePrimaryClicked() {
        MouseEventHandler handler = this.getOnMouseEventHandler(false);
        if (handler == null) {
            return null;
        }
        MouseHandler mouseHandler = handler.getMouseHandler(MouseEvent.MOUSE_CLICKED, MouseButton.PRIMARY, 1);
        if (mouseHandler == null) {
            return null;
        }
        return mouseHandler.handler();
    }

    /**
     * 设置鼠标主按钮点击事件
     *
     * @param handler 事件处理器
     */
    default void setOnMousePrimaryClicked(EventHandler<? super MouseEvent> handler) {
        MouseHandler mouseHandler = new MouseHandler();
        mouseHandler.type(MouseEvent.MOUSE_CLICKED)
                .button(MouseButton.PRIMARY)
                .clickCount(1)
                .handler(handler);
        this.getOnMouseEventHandler(true).addHandler(mouseHandler);
    }

    /**
     * 设置鼠标次按钮点击事件
     *
     * @return 事件处理器
     */
    default EventHandler<? super MouseEvent> getOnMouseSecondClicked() {
        MouseEventHandler handler = this.getOnMouseEventHandler(false);
        if (handler == null) {
            return null;
        }
        MouseHandler mouseHandler = handler.getMouseHandler(MouseEvent.MOUSE_CLICKED, MouseButton.SECONDARY, 1);
        if (mouseHandler == null) {
            return null;
        }
        return mouseHandler.handler();
    }

    /**
     * 设置鼠标次按钮点击事件
     *
     * @param handler 事件处理器
     */
    default void setOnMouseSecondClicked(EventHandler<? super MouseEvent> handler) {
        MouseHandler mouseHandler = new MouseHandler();
        mouseHandler.type(MouseEvent.MOUSE_CLICKED)
                .button(MouseButton.SECONDARY)
                .clickCount(1)
                .handler(handler);
        this.getOnMouseEventHandler(true).addHandler(mouseHandler);
    }
}
